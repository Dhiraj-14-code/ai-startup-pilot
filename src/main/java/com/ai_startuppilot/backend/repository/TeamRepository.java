package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team,Long> {
}
