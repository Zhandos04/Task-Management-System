package org.example.taskmanagementsystem.dto.response;

import lombok.Data;
import org.example.taskmanagementsystem.entity.TaskPriority;
import org.example.taskmanagementsystem.entity.TaskStatus;

import java.util.List;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private String authorEmail;
    private String executorEmail;
    private List<CommentResponseDTO> comments;

    public TaskResponseDTO(Long id, String title, String description, TaskStatus status, TaskPriority priority, String authorEmail, String executorEmail, List<CommentResponseDTO> comments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.authorEmail = authorEmail;
        this.executorEmail = executorEmail;
        this.comments = comments;
    }
    public TaskResponseDTO() {}
}
