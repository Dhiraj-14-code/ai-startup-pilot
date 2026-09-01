package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Risk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRepository extends JpaRepository<Risk,Long> {
    List<Risk> findByProjectId(Long projectId);
}
