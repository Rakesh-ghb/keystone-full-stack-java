package com.key_stone.service;

import com.key_stone.domain.Customer;
import com.key_stone.domain.Part;
import com.key_stone.domain.PartUsage;
import com.key_stone.domain.Priority;
import com.key_stone.domain.Role;
import com.key_stone.domain.Site;
import com.key_stone.domain.TimeLog;
import com.key_stone.domain.User;
import com.key_stone.domain.WorkOrder;
import com.key_stone.domain.WorkOrderStatus;
import com.key_stone.domain.WorkOrderStatusHistory;
import com.key_stone.dto.ApiDtos.*;
import com.key_stone.repository.CustomerRepository;
import com.key_stone.repository.PartRepository;
import com.key_stone.repository.PartUsageRepository;
import com.key_stone.repository.SiteRepository;
import com.key_stone.repository.TimeLogRepository;
import com.key_stone.repository.UserRepository;
import com.key_stone.repository.WorkOrderRepository;
import com.key_stone.repository.WorkOrderStatusHistoryRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class WorkOrderService {

    private final WorkOrderRepository orders;
    private final CustomerRepository customers;
    private final SiteRepository sites;
    private final UserRepository users;
    private final PartRepository parts;
    private final WorkOrderStatusHistoryRepository historyRepo;
    private final PartUsageRepository usageRepo;
    private final TimeLogRepository timeRepo;

    public WorkOrderService(
            WorkOrderRepository orders,
            CustomerRepository customers,
            SiteRepository sites,
            UserRepository users,
            PartRepository parts,
            WorkOrderStatusHistoryRepository historyRepo,
            PartUsageRepository usageRepo,
            TimeLogRepository timeRepo) {

        this.orders = orders;
        this.customers = customers;
        this.sites = sites;
        this.users = users;
        this.parts = parts;
        this.historyRepo = historyRepo;
        this.usageRepo = usageRepo;
        this.timeRepo = timeRepo;
    }

    // =========================================================
    // GET ALL WORK ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> all(
            String q,
            String role,
            String email) {

        List<WorkOrder> list =
                (q == null || q.isBlank())
                        ? orders.findAll()
                        : orders.search(q);

        User me = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (me.getRole() == Role.TECHNICIAN) {

            list = list.stream()
                    .filter(w ->
                            w.getAssignee() != null &&
                            w.getAssignee()
                                    .getId()
                                    .equals(me.getId()))
                    .toList();
        }

        if (me.getRole() == Role.CUSTOMER) {

            if (me.getCustomer() == null) {
                return List.of();
            }

            list = list.stream()
                    .filter(w ->
                            w.getCustomer() != null &&
                            w.getCustomer()
                                    .getId()
                                    .equals(me.getCustomer().getId()))
                    .toList();
        }

        return list.stream()
                .sorted(
                        Comparator.comparing(
                                WorkOrder::getCreatedAt
                        ).reversed()
                )
                .map(this::map)
                .toList();
    }

    // =========================================================
    // GET ONE WORK ORDER
    // =========================================================

    @Transactional(readOnly = true)
    public WorkOrderResponse get(Long id, String email) {

        WorkOrder w = find(id);

        assertVisible(w, email);

        return map(w);
    }

    // =========================================================
    // CREATE WORK ORDER
    // =========================================================

    @Transactional
    public WorkOrderResponse create(WorkOrderRequest r) {

        Customer c = customers.findById(r.customerId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found"));

        Site s = sites.findById(r.siteId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Site not found"));

        if (s.getCustomer() == null ||
                !s.getCustomer()
                        .getId()
                        .equals(c.getId())) {

            throw new IllegalArgumentException(
                    "Site does not belong to customer");
        }

        WorkOrder w = new WorkOrder();

        w.setCode(nextCode());
        w.setTitle(r.title());
        w.setDescription(r.description());
        w.setPriority(r.priority());
        w.setStatus(WorkOrderStatus.NEW);
        w.setCustomer(c);
        w.setSite(s);

        w.setSlaDueAt(
                LocalDateTime.now()
                        .plusHours(
                                slaHours(r.priority())
                        )
        );

        w = orders.save(w);

        addHistory(
                w,
                WorkOrderStatus.NEW,
                WorkOrderStatus.NEW,
                currentUser(),
                "Created"
        );

        return map(w);
    }

    // =========================================================
    // UPDATE WORK ORDER
    // =========================================================

    @Transactional
    public WorkOrderResponse update(
            Long id,
            WorkOrderRequest r,
            String email) {

        WorkOrder w = find(id);

        assertVisible(w, email);

        if (w.terminal()) {

            throw new IllegalStateException(
                    "Closed/cancelled work orders are immutable");
        }

        Customer c = customers.findById(r.customerId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found"));

        Site s = sites.findById(r.siteId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Site not found"));

        if (s.getCustomer() == null ||
                !s.getCustomer()
                        .getId()
                        .equals(c.getId())) {

            throw new IllegalArgumentException(
                    "Site does not belong to customer");
        }

        w.setTitle(r.title());
        w.setDescription(r.description());
        w.setPriority(r.priority());
        w.setCustomer(c);
        w.setSite(s);

        w.setSlaDueAt(
                w.getCreatedAt()
                        .plusHours(
                                slaHours(r.priority())
                        )
        );

        return map(orders.save(w));
    }

    // =========================================================
    // ASSIGN WORK ORDER
    // =========================================================

    @Transactional
    public WorkOrderResponse assign(
            Long id,
            AssignRequest r,
            String email) {

        WorkOrder w = find(id);

        requireAny(
                Role.ADMIN,
                Role.DISPATCHER,
                email
        );

        if (w.terminal()) {

            throw new IllegalStateException(
                    "Terminal work order");
        }

        User tech = users.findById(r.technicianId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Technician not found"));

        if (tech.getRole() != Role.TECHNICIAN) {

            throw new IllegalArgumentException(
                    "Assignee must be a technician");
        }

        if (!tech.isActive()) {

            throw new IllegalArgumentException(
                    "Technician is inactive");
        }

        w.setAssignee(tech);

        transitionInternal(
                w,
                WorkOrderStatus.ASSIGNED,
                "Assigned to " + tech.getName()
        );

        return map(orders.save(w));
    }

    // =========================================================
    // CHANGE STATUS
    // =========================================================

    @Transactional
    public WorkOrderResponse status(
            Long id,
            StatusRequest r,
            String email) {

        WorkOrder w = find(id);

        User me = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));

        if (me.getRole() == Role.TECHNICIAN &&
                (w.getAssignee() == null ||
                        !w.getAssignee()
                                .getId()
                                .equals(me.getId()))) {

            throw new SecurityException(
                    "You can only act on assigned work");
        }

        if (r.status() == WorkOrderStatus.CLOSED &&
                me.getRole() != Role.ADMIN) {

            throw new SecurityException(
                    "Only admin can close");
        }

        if (me.getRole() == Role.CUSTOMER) {

            throw new SecurityException(
                    "Customer cannot change status");
        }

        transitionInternal(
                w,
                r.status(),
                r.note()
        );

        return map(orders.save(w));
    }

    // =========================================================
    // ADD PART
    // =========================================================

    @Transactional
    public WorkOrderResponse addPart(
            Long id,
            PartUsageRequest r,
            String email) {

        WorkOrder w = find(id);

        User me = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));

        assertTechnicianAssigned(w, me);

        if (w.terminal()) {

            throw new IllegalStateException(
                    "Terminal work order");
        }

        if (r.quantity() <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        Part p = parts.findById(r.partId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Part not found"));

        if (p.getStockQuantity() < r.quantity()) {

            throw new IllegalStateException(
                    "Insufficient stock");
        }

        p.setStockQuantity(
                p.getStockQuantity() - r.quantity()
        );

        parts.save(p);

        PartUsage usage = new PartUsage();

        usage.setWorkOrder(w);
        usage.setPart(p);
        usage.setQuantity(r.quantity());
        usage.setUnitPrice(p.getUnitPrice());

        usageRepo.save(usage);

        return map(w);
    }

    // =========================================================
    // ADD TIME
    // =========================================================

    @Transactional
    public WorkOrderResponse addTime(
            Long id,
            TimeLogRequest r,
            String email) {

        WorkOrder w = find(id);

        User me = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));

        assertTechnicianAssigned(w, me);

        if (w.terminal()) {

            throw new IllegalStateException(
                    "Terminal work order");
        }

        if (r.minutes() <= 0) {

            throw new IllegalArgumentException(
                    "Minutes must be greater than zero");
        }

        TimeLog timeLog = new TimeLog();

        timeLog.setWorkOrder(w);
        timeLog.setTechnician(me);
        timeLog.setMinutes(r.minutes());
        timeLog.setNote(r.note());
        timeLog.setLoggedAt(LocalDateTime.now());

        timeRepo.save(timeLog);

        return map(w);
    }

    // =========================================================
    // TECHNICIANS
    // =========================================================

    @Transactional(readOnly = true)
    public List<User> technicians() {

        return users.findAll()
                .stream()
                .filter(u ->
                        u.getRole() == Role.TECHNICIAN &&
                        u.isActive())
                .toList();
    }

    // =========================================================
    // PARTS
    // =========================================================

    @Transactional(readOnly = true)
    public List<PartResponse> parts() {

        return parts.findAll()
                .stream()
                .map(p ->
                        new PartResponse(
                                p.getId(),
                                p.getSku(),
                                p.getName(),
                                p.getStockQuantity(),
                                p.getUnitPrice(),
                                p.getReorderLevel()
                        )
                )
                .toList();
    }

    // =========================================================
    // CREATE PART
    // =========================================================

    @Transactional
    public PartResponse createPart(PartRequest r) {

        if (parts.existsBySkuIgnoreCase(r.sku())) {

            throw new IllegalArgumentException(
                    "Part SKU already exists");
        }

        Part p = new Part();

        p.setSku(r.sku());
        p.setName(r.name());
        p.setStockQuantity(r.stockQuantity());
        p.setUnitPrice(r.unitPrice());
        p.setReorderLevel(r.reorderLevel());

        p = parts.save(p);

        return new PartResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getStockQuantity(),
                p.getUnitPrice(),
                p.getReorderLevel()
        );
    }

    // =========================================================
    // SECURITY HELPERS
    // =========================================================

    private void assertTechnicianAssigned(
            WorkOrder w,
            User me) {

        if (me.getRole() != Role.TECHNICIAN ||
                w.getAssignee() == null ||
                !w.getAssignee()
                        .getId()
                        .equals(me.getId())) {

            throw new SecurityException(
                    "Only assigned technician can do this");
        }
    }

    private void assertVisible(
            WorkOrder w,
            String email) {

        User me = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));

        if (me.getRole() == Role.CUSTOMER) {

            if (me.getCustomer() == null ||
                    w.getCustomer() == null ||
                    !w.getCustomer()
                            .getId()
                            .equals(me.getCustomer().getId())) {

                throw new SecurityException(
                        "Forbidden");
            }
        }

        if (me.getRole() == Role.TECHNICIAN) {

            if (w.getAssignee() == null ||
                    !w.getAssignee()
                            .getId()
                            .equals(me.getId())) {

                throw new SecurityException(
                        "Forbidden");
            }
        }
    }

    private void requireAny(
            Role a,
            Role b,
            String email) {

        Role r = users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"))
                .getRole();

        if (r != a && r != b) {

            throw new SecurityException(
                    "Forbidden");
        }
    }

    // =========================================================
    // FIND WORK ORDER
    // =========================================================

    private WorkOrder find(Long id) {

        return orders.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Work order not found"));
    }

    // =========================================================
    // WORK ORDER CODE
    // =========================================================

    private String nextCode() {

        return "WO-" +
                String.format(
                        "%06d",
                        orders.count() + 1
                );
    }

    // =========================================================
    // SLA HOURS
    // =========================================================

    private int slaHours(Priority p) {

        return switch (p) {

            case CRITICAL -> 4;

            case HIGH -> 8;

            case MEDIUM -> 24;

            case LOW -> 72;
        };
    }

    // =========================================================
    // CURRENT USER
    // =========================================================

    private User currentUser() {

        if (SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            throw new SecurityException(
                    "Authentication required");
        }

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Current user not found"));
    }

    // =========================================================
    // HISTORY
    // =========================================================

    private void addHistory(
            WorkOrder w,
            WorkOrderStatus from,
            WorkOrderStatus to,
            User who,
            String note) {

        WorkOrderStatusHistory history =
                new WorkOrderStatusHistory();

        history.setWorkOrder(w);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setChangedBy(who);
        history.setChangedAt(LocalDateTime.now());
        history.setNote(note);

        historyRepo.save(history);
    }

    // =========================================================
    // STATUS TRANSITION
    // =========================================================

    private void transitionInternal(
            WorkOrder w,
            WorkOrderStatus to,
            String note) {

        WorkOrderStatus from = w.getStatus();

        if (from == to) {
            return;
        }

        if (w.terminal()) {

            throw new IllegalStateException(
                    "Terminal work order");
        }

        boolean allowed = switch (from) {

            case NEW ->
                    to == WorkOrderStatus.ASSIGNED ||
                    to == WorkOrderStatus.CANCELLED;

            case ASSIGNED ->
                    to == WorkOrderStatus.IN_PROGRESS ||
                    to == WorkOrderStatus.CANCELLED;

            case IN_PROGRESS ->
                    to == WorkOrderStatus.ON_HOLD ||
                    to == WorkOrderStatus.COMPLETED ||
                    to == WorkOrderStatus.CANCELLED;

            case ON_HOLD ->
                    to == WorkOrderStatus.IN_PROGRESS ||
                    to == WorkOrderStatus.CANCELLED;

            case COMPLETED ->
                    to == WorkOrderStatus.CLOSED;

            case CLOSED, CANCELLED ->
                    false;
        };

        if (!allowed) {

            throw new IllegalStateException(
                    "Illegal transition: " +
                    from + " -> " + to);
        }

        w.setStatus(to);

        addHistory(
                w,
                from,
                to,
                currentUser(),
                note
        );
    }

    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private WorkOrderResponse map(WorkOrder w) {

        var hs = w.getHistory()
                .stream()
                .sorted(
                        Comparator.comparing(
                                WorkOrderStatusHistory::getChangedAt
                        )
                )
                .map(h ->
                        new HistoryResponse(
                                h.getId(),
                                h.getFromStatus(),
                                h.getToStatus(),
                                h.getChangedBy().getName(),
                                h.getChangedAt(),
                                h.getNote()
                        )
                )
                .toList();

        return new WorkOrderResponse(
                w.getId(),
                w.getCode(),
                w.getTitle(),
                w.getDescription(),
                w.getPriority(),
                w.getStatus(),
                w.getCustomer().getId(),
                w.getCustomer().getName(),
                w.getSite().getId(),
                w.getSite().getName(),
                w.getAssignee() == null
                        ? null
                        : w.getAssignee().getId(),
                w.getAssignee() == null
                        ? null
                        : w.getAssignee().getName(),
                w.getSlaDueAt(),
                w.getCreatedAt(),
                w.partsCost(),
                w.labourMinutes(),
                hs
        );
    }
}