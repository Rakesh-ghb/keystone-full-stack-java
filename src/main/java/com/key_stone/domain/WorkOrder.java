package com.key_stone.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "work_orders",
    indexes = {
        @Index(name = "idx_wo_status", columnList = "status"),
        @Index(name = "idx_wo_assignee", columnList = "assignee_id"),
        @Index(name = "idx_wo_customer", columnList = "customer_id")
    }
)
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkOrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(nullable = false)
    private LocalDateTime slaDueAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "workOrder",
        cascade = CascadeType.ALL,
        orphanRemoval = false
    )
    private List<WorkOrderStatusHistory> history = new ArrayList<>();

    @OneToMany(
        mappedBy = "workOrder",
        cascade = CascadeType.ALL
    )
    private List<PartUsage> partUsages = new ArrayList<>();

    @OneToMany(
        mappedBy = "workOrder",
        cascade = CascadeType.ALL
    )
    private List<TimeLog> timeLogs = new ArrayList<>();

    public WorkOrder() {
    }

    public boolean terminal() {
        return status == WorkOrderStatus.CLOSED
                || status == WorkOrderStatus.CANCELLED;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public BigDecimal partsCost() {
        return partUsages.stream()
                .map(x -> x.getUnitPrice()
                        .multiply(BigDecimal.valueOf(x.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int labourMinutes() {
        return timeLogs.stream()
                .mapToInt(TimeLog::getMinutes)
                .sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public LocalDateTime getSlaDueAt() {
        return slaDueAt;
    }

    public void setSlaDueAt(LocalDateTime slaDueAt) {
        this.slaDueAt = slaDueAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<WorkOrderStatusHistory> getHistory() {
        return history;
    }

    public void setHistory(List<WorkOrderStatusHistory> history) {
        this.history = history;
    }

    public List<PartUsage> getPartUsages() {
        return partUsages;
    }

    public void setPartUsages(List<PartUsage> partUsages) {
        this.partUsages = partUsages;
    }

    public List<TimeLog> getTimeLogs() {
        return timeLogs;
    }

    public void setTimeLogs(List<TimeLog> timeLogs) {
        this.timeLogs = timeLogs;
    }
}