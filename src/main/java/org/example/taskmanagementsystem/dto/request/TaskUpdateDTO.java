package org.example.taskmanagementsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.taskmanagementsystem.entity.TaskPriority;
import org.example.taskmanagementsystem.entity.TaskStatus;

@Data
public class TaskUpdateDTO {
    @NotBlank
    private String title;
    private String description;
    private TaskStatus status;
    @NotNull
    private TaskPriority priority;
    @NotNull
    @Email
    private String executorEmail;
}
