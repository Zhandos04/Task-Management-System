package org.example.taskmanagementsystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.dto.request.TaskCreateDTO;
import org.example.taskmanagementsystem.dto.request.TaskUpdateDTO;
import org.example.taskmanagementsystem.dto.request.CommentDTO;
import org.example.taskmanagementsystem.dto.response.CommentResponseDTO;
import org.example.taskmanagementsystem.dto.response.TaskResponseDTO;
import org.example.taskmanagementsystem.entity.TaskStatus;
import org.example.taskmanagementsystem.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Задачи", description = "Управление задачами")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    @Operation(
            summary = "Создание задачи",
            description = "Пользователь либо Адмимнистратор создает новую задачу. Автором становится текущий пользователь.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Задача создана"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
            }
    )
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskCreateDTO taskCreateDTO) {
        TaskResponseDTO createdTask = taskService.createTask(taskCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/all")
    @Operation(
            summary = "Получение всей задачи",
            description = "Возвращает все задачи",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Задача создана"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
            }
    )
    public ResponseEntity<Page<TaskResponseDTO>> allTasks(@PageableDefault Pageable pageable) {
        Page<TaskResponseDTO> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my")
    @Operation(
            summary = "Получение задач пользователя",
            description = "Возвращает задачи, созданные пользователем",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список задач")
            }
    )
    public ResponseEntity<Page<TaskResponseDTO>> getMyTasks(@PageableDefault Pageable pageable) {
        Page<TaskResponseDTO> tasks = taskService.getMyTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получение задачи по ID",
            description = "Возвращает задачу с указанным идентификатором",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача найдена"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена")
            }
    )
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        TaskResponseDTO taskResponseDTO = taskService.getTaskById(id);
        return ResponseEntity.ok(taskResponseDTO);
    }

    @PutMapping("/edit/{id}")
    @Operation(
            summary = "Обновление задачи",
            description = "Пользователь обновляет существующую задачу",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача обновлена"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                    @ApiResponse(responseCode = "403", description = "Вы не можете изменить задачу другого пользователя")
            }
    )
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody @Valid TaskUpdateDTO taskUpdateDTO) {
        TaskResponseDTO updatedTask = taskService.updateTask(id, taskUpdateDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(
            summary = "Удаление задачи",
            description = "Пользователь удаляет свою задачу",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Задача удалена"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                    @ApiResponse(responseCode = "403", description = "Вы не можете удалить задачу другого пользователя")
            }
    )
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Задача удалена");
    }

    @GetMapping("/by-user/{userId}")
    @Operation(
            summary = "Получение задач по автору или исполнителя",
            description = "Возвращает задачи, созданные указанным пользователем",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список задач")
            }
    )
    public ResponseEntity<Page<TaskResponseDTO>> getTasksByUser(@PathVariable Long userId,
                                                                  @PageableDefault Pageable pageable) {
        Page<TaskResponseDTO> tasks = taskService.getTasksByUser(userId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{taskId}/change-status")
    @Operation(
            summary = "Обновление статуса задачи",
            description = "Пользователь, назначенный исполнителем, изменяет статус своей задачи.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Статус задачи успешно обновлен"),
                    @ApiResponse(responseCode = "404", description = "Задача не найдена"),
                    @ApiResponse(responseCode = "403", description = "Вы не можете изменять статус этой задачи")
            }
    )
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(@PathVariable Long taskId,
                                                            @RequestParam TaskStatus status) {
        TaskResponseDTO updatedTask = taskService.updateTaskStatus(taskId, status);
        return ResponseEntity.ok(updatedTask);
    }

    @PostMapping("/{taskId}/add-comment")
    @Operation(
            summary = "Добавление комментария к задаче",
            description = "Позволяет добавить комментарий к задаче.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Комментарий добавлен"),
                    @ApiResponse(responseCode = "404", description = "Задача или пользователь комментария не найдены"),
                    @ApiResponse(responseCode = "403", description = "Вы не можете оставлять комментарию в эту задачу")
            }
    )
    public ResponseEntity<CommentResponseDTO> addComment(@PathVariable Long taskId, @RequestBody @Valid CommentDTO commentDTO) {
        CommentResponseDTO createdComment = taskService.addCommentToTask(taskId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }
}
