package com.ai_startuppilot.backend.entity;

import com.ai_startuppilot.backend.enums.TaskPriority;
import com.ai_startuppilot.backend.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tasks")
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Task ka naam
    @Column(nullable = false)
    private String title;

    // Task ke baare mein details
    private String description;

    // Task ki current state
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    // Task ki importance
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    // Task complete karne ki deadline
    private LocalDateTime dueDate;

    // Har task ek project ka part hoga
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Ek task ek user ko assign kiya ja sakta hai
    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}