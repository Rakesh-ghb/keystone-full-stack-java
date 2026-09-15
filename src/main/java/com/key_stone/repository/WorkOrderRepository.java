package com.key_stone.repository;
import com.key_stone.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;
public interface WorkOrderRepository extends JpaRepository<WorkOrder,Long>{
 List<WorkOrder> findByAssigneeIdOrderByCreatedAtDesc(Long id);
 List<WorkOrder> findByCustomerIdOrderByCreatedAtDesc(Long id);
 List<WorkOrder> findByStatusOrderByCreatedAtDesc(WorkOrderStatus status);
 long countByStatus(WorkOrderStatus status);
 long countBySlaDueAtBeforeAndStatusNotIn(LocalDateTime t, Collection<WorkOrderStatus> statuses);
 List<WorkOrder> findBySlaDueAtBeforeAndStatusNotIn(LocalDateTime t, Collection<WorkOrderStatus> statuses);
 @Query("select w from WorkOrder w where lower(w.title) like lower(concat('%',:q,'%')) or lower(w.code) like lower(concat('%',:q,'%'))")
 List<WorkOrder> search(@Param("q") String q);
}