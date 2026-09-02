package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MilestoneRepository extends JpaRepository<Milestone,Long> {
    Page<Milestone> findByProjectId(Long projectId, Pageable pageable);
}
