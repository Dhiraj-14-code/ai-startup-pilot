package com.ai_startuppilot.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "teams")
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Ek team ke andar multiple users/members ho sakte hain
    // Ye relationship ka owning side hai
    @ManyToMany
    @JoinTable(
            name = "team_members",

            // team ki ID join table mein store hogi
            joinColumns = @JoinColumn(name = "team_id"),

            // user ki ID join table mein store hogi
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
}