package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone,Long> {
    List<Milestone> findByProjectId(Long projectId);
}
