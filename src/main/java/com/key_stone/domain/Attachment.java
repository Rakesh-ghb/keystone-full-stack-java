package com.key_stone.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="attachments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attachment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="work_order_id")
    private WorkOrder workOrder;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="uploaded_by")
    private User uploadedBy;
    @Column(nullable=false)
    private String originalName;
    @Column(nullable=false)
    private String storedName;
    @Column(nullable=false)
    private String contentType;
    @Column(nullable=false)
    private long size;
    @Column(nullable=false)
    private LocalDateTime uploadedAt;
}
