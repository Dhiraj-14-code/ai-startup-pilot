package com.ai_startuppilot.backend.repository;

import com.ai_startuppilot.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task,Long> {
}
