package com.key_stone.service;

import com.key_stone.domain.WorkOrderStatus;
import com.key_stone.dto.ApiDtos.ReportSummary;
import com.key_stone.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final WorkOrderRepository repo;

    public ReportService(WorkOrderRepository repo) {
        this.repo = repo;
    }

    public ReportSummary summary() {

        long total = repo.count();

        long n = repo.countByStatus(WorkOrderStatus.NEW);
        long a = repo.countByStatus(WorkOrderStatus.ASSIGNED);
        long ip = repo.countByStatus(WorkOrderStatus.IN_PROGRESS);
        long h = repo.countByStatus(WorkOrderStatus.ON_HOLD);
        long c = repo.countByStatus(WorkOrderStatus.COMPLETED);
        long cl = repo.countByStatus(WorkOrderStatus.CLOSED);
        long ca = repo.countByStatus(WorkOrderStatus.CANCELLED);

        long overdue =
                repo.countBySlaDueAtBeforeAndStatusNotIn(
                        LocalDateTime.now(),
                        List.of(
                                WorkOrderStatus.CLOSED,
                                WorkOrderStatus.CANCELLED
                        )
                );

        long resolved = c + cl;

        double compliance;

        if (resolved == 0) {
            compliance = 100.0;
        } else {
            compliance = Math.max(
                    0,
                    Math.round(
                            (resolved - Math.min(overdue, resolved))
                                    * 10000.0
                                    / resolved
                    ) / 100.0
            );
        }

        return new ReportSummary(
                total,
                n,
                a,
                ip,
                h,
                c,
                cl,
                ca,
                overdue,
                compliance
        );
    }
}