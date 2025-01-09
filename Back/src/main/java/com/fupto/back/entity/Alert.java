package com.fupto.back.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "alert", schema = "fuptodb")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonBackReference
    private Member member;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Lob
    @Column(name = "message")
    private String message;

    @ColumnDefault("false")
    @Column(name = "is_read")
    private Boolean isRead;

    @ColumnDefault("false")
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ColumnDefault("current_timestamp()")
    @Column(name = "create_date", insertable = false, updatable = false)
    private Instant createDate;
}