package org.example.taskmanagementsystem.dto.response;

import lombok.Data;

@Data
public class CommentResponseDTO {
    private Long id;
    private String content;
    private String authorEmail;
}
