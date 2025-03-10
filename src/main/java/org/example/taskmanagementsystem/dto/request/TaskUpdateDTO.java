package org.example.taskmanagementsystem.dto.request;

import lombok.Data;
import org.example.taskmanagementsystem.entity.TaskPriority;
import org.example.taskmanagementsystem.entity.TaskStatus;

@Data
public class TaskUpdateDTO {
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private String executorEmail;
}
