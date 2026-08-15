package com.ai_startuppilot.backend.entity;

import com.ai_startuppilot.backend.enums.RiskSeverity;
import com.ai_startuppilot.backend.enums.RiskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "risks")
@Getter
@Setter
public class Risk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Risk ka short title
    @Column(nullable = false)
    private String title;

    // Risk ki detailed information
    private String description;

    // Risk kitna serious hai
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskSeverity severity;

    // Risk ki current state
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskStatus status;

    // Har risk ek project se related hoga
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}