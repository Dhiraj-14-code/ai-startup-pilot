package com.ai_startuppilot.backend.entity;

import com.ai_startuppilot.backend.enums.MilestoneStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "milestone")
@Getter
@Setter
public class Milestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Milestone ka naam
    @Column(nullable = false)
    private String title;

    // Milestone ke baare mein details
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneStatus status;

    //Milestone complete karne ki deadline
    private LocalDateTime dueDate;

    //Har milesStone ek project ke andar hoga
    @ManyToOne
    @JoinColumn(name = "project_id",nullable = false)
    private Project project;

    @CreatedDate
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
