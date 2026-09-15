package com.key_stone.dto;

import com.key_stone.domain.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos(){}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record LoginResponse(String token, Long userId, String name, String role) {}

    public record CustomerRequest(@NotBlank @Size(max=160) String name,
                                  @Size(max=120) String contactName,
                                  @Email String email, @Size(max=40) String phone) {}
    public record CustomerResponse(Long id,String name,String contactName,String email,String phone) {}

    public record SiteRequest(@NotBlank @Size(max=160) String name,
                              @NotBlank @Size(max=255) String address,
                              String city,String postalCode, @NotNull Long customerId) {}
    public record SiteResponse(Long id,String name,String address,String city,String postalCode,Long customerId,String customerName) {}

    public record WorkOrderRequest(@NotBlank @Size(max=180) String title,
                                   @Size(max=3000) String description,
                                   @NotNull Priority priority,@NotNull Long customerId,@NotNull Long siteId) {}
    public record WorkOrderResponse(Long id,String code,String title,String description,Priority priority,
                                     WorkOrderStatus status,Long customerId,String customerName,Long siteId,String siteName,
                                     Long assigneeId,String assigneeName,LocalDateTime slaDueAt,LocalDateTime createdAt,
                                     BigDecimal partsCost,int labourMinutes, List<HistoryResponse> history) {}
    public record HistoryResponse(Long id,WorkOrderStatus fromStatus,WorkOrderStatus toStatus,
                                  String changedBy,LocalDateTime changedAt,String note) {}
    public record AssignRequest(@NotNull Long technicianId) {}
    public record StatusRequest(@NotNull WorkOrderStatus status,String note) {}
    public record PartUsageRequest(@NotNull Long partId,@Min(1) int quantity) {}
    public record TimeLogRequest(@Min(1) int minutes,String note) {}
    public record PartRequest(@NotBlank String sku,@NotBlank String name,@Min(0) int stockQuantity,
                              @NotNull @DecimalMin("0.0") BigDecimal unitPrice,@Min(0) int reorderLevel) {}
    public record PartResponse(Long id,String sku,String name,int stockQuantity,BigDecimal unitPrice,int reorderLevel) {}
    public record ReportSummary(long total,long newCount,long assigned,long inProgress,long onHold,long completed,
                                long closed,long cancelled,long overdue,double slaCompliance) {}
    public record ErrorResponse(LocalDateTime timestamp,int status,String message,java.util.Map<String,String> fieldErrors) {}
}
