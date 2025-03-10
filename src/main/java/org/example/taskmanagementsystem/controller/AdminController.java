package org.example.taskmanagementsystem.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.dto.request.CommentDTO;
import org.example.taskmanagementsystem.dto.request.TaskUpdateDTO;
import org.example.taskmanagementsystem.dto.response.CommentResponseDTO;
import org.example.taskmanagementsystem.dto.response.TaskResponseDTO;
import org.example.taskmanagementsystem.entity.TaskPriority;
import org.example.taskmanagementsystem.entity.TaskStatus;
import org.example.taskmanagementsystem.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tasks")
@Tag(name = "Администрирование задач", description = "Управление задачами администратором")
@RequiredArgsConstructor
public class AdminController {
    private final TaskService taskService;
    @PutMapping("/edit/{id}")
    @Operation(
            summary = "Обновление задачи",
            description = "Администратор обновляет существующую задачу",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача обновлена"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody @Valid TaskUpdateDTO taskUpdateDTO) {
        TaskResponseDTO updatedTask = taskService.updateTaskForAdmin(id, taskUpdateDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Удаление задачи",
            description = "Администратор удаляет существующую задачу",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача удалена"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTaskForAdmin(id);
        return ResponseEntity.ok("Задача удалена");
    }

    @PatchMapping("/{taskId}/change-status")
    @Operation(
            summary = "Обновление статуса задачи",
            description = "Пользователь, назначенный исполнителем, изменяет статус своей задачи.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус задачи успешно обновлен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(@PathVariable Long taskId,
                                                            @RequestParam TaskStatus status) {
        TaskResponseDTO updatedTask = taskService.updateTaskStatusForAdmin(taskId, status);
        return ResponseEntity.ok(updatedTask);
    }
    @PatchMapping("/{taskId}/change-priority")
    @Operation(
            summary = "Обновление статуса задачи",
            description = "Пользователь, назначенный исполнителем, изменяет статус своей задачи.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус задачи успешно обновлен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    public ResponseEntity<TaskResponseDTO> updateTaskPriority(@PathVariable Long taskId,
                                                            @RequestParam TaskPriority priority) {
        TaskResponseDTO updatedTask = taskService.updateTaskPriorityForAdmin(taskId, priority);
        return ResponseEntity.ok(updatedTask);
    }

    @PostMapping("/{taskId}/add-comment")
    @Operation(
            summary = "Добавление комментария к задаче",
            description = "Позволяет добавить комментарий к задаче.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Комментарий добавлен"),
                    @ApiResponse(responseCode = "404", description = "Задача или пользователь комментария не найдены")
            }
    )
    public ResponseEntity<CommentResponseDTO> addComment(@PathVariable Long taskId, @RequestBody @Valid CommentDTO commentDTO) {
        CommentResponseDTO createdComment = taskService.addCommentToTaskForAdmin(taskId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
}

