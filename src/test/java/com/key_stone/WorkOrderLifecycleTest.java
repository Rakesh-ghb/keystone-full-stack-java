package com.key_stone;

import com.key_stone.domain.WorkOrder;
import com.key_stone.domain.WorkOrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderLifecycleTest {

    @Test
    void allowedTransitions() {

        assertTrue(
                WorkOrderStatus.ASSIGNED
                        != WorkOrderStatus.CLOSED
        );

        assertTrue(
                WorkOrderStatus.CLOSED.ordinal() >= 0
        );
    }

    @Test
    void terminalStatesAreTerminal() {

        WorkOrder w = new WorkOrder();

        w.setStatus(WorkOrderStatus.CLOSED);

        assertTrue(w.terminal());
    }
}