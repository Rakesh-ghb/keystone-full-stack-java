package com.key_stone.service;

import com.key_stone.domain.WorkOrderStatus;
import com.key_stone.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SlaScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(SlaScheduler.class);

    private final WorkOrderRepository orders;

    public SlaScheduler(WorkOrderRepository orders) {
        this.orders = orders;
    }

    @Scheduled(fixedDelayString = "${app.sla.check-ms:300000}")
    public void check() {

        var list =
                orders.findBySlaDueAtBeforeAndStatusNotIn(
                        LocalDateTime.now(),
                        List.of(
                                WorkOrderStatus.CLOSED,
                                WorkOrderStatus.CANCELLED
                        )
                );

        if (!list.isEmpty()) {
            log.warn(
                    "SLA breach/risk detected for {} work orders",
                    list.size()
            );
        }
    }
}