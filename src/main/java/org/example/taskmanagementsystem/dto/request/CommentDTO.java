package org.example.taskmanagementsystem.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {
    @Size(min = 10, message = "Текст комментария должен быть не менее 10 символов")
    private String content;
}
