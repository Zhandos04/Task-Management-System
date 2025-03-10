package org.example.taskmanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.taskmanagementsystem.entity.TaskPriority;

@Data
public class TaskCreateDTO {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private TaskPriority priority;
    @NotNull
    private String executorEmail;
}

