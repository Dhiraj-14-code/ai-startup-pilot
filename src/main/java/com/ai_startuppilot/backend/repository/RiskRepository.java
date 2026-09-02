package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Risk;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiskRepository extends JpaRepository<Risk,Long> {
    Page<Risk> findByProjectId(Long projectId, Pageable pageable);
}
