package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project,Long> {

    Long id(Long id);
}
