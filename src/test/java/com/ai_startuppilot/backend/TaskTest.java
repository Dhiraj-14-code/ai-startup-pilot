package com.ai_startuppilot.backend;

import com.ai_startuppilot.backend.entity.Task;
import com.ai_startuppilot.backend.enums.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void testIsOverdue_WithPastDueDateAndNotCompleted_ShouldReturnTrue() {
        Task task = new Task();
        task.setDueDate(LocalDateTime.now().minusDays(1));
        task.setStatus(TaskStatus.TODO);
        
        assertTrue(task.isOverdue(), "Task with past due date and TODO status should be overdue");
    }

    @Test
    void testIsOverdue_WithFutureDueDate_ShouldReturnFalse() {
        Task task = new Task();
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setStatus(TaskStatus.TODO);
        
        assertFalse(task.isOverdue(), "Task with future due date should NOT be overdue");
    }

    @Test
    void testIsOverdue_WhenCompleted_ShouldReturnFalse() {
        Task task = new Task();
        task.setDueDate(LocalDateTime.now().minusDays(1));
        task.setStatus(TaskStatus.COMPLETED);
        
        assertFalse(task.isOverdue(), "Completed task should NOT be overdue even if past due date");
    }

    @Test
    void testIsOverdue_WithNullDueDate_ShouldReturnFalse() {
        Task task = new Task();
        task.setDueDate(null);
        task.setStatus(TaskStatus.TODO);
        
        assertFalse(task.isOverdue(), "Task with null due date should NOT be overdue");
    }
}
