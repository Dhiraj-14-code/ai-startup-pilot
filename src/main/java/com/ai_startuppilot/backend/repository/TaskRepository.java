package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends JpaRepository<Task,Long> {
    Page<Task> findByProjectId(Long projectId, Pageable pageable);
}
