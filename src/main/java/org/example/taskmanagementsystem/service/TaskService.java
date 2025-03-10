package org.example.taskmanagementsystem.service;

import org.example.taskmanagementsystem.dto.request.TaskCreateDTO;
import org.example.taskmanagementsystem.dto.request.TaskUpdateDTO;
import org.example.taskmanagementsystem.dto.request.CommentDTO;
import org.example.taskmanagementsystem.dto.response.CommentResponseDTO;
import org.example.taskmanagementsystem.dto.response.TaskResponseDTO;
import org.example.taskmanagementsystem.entity.TaskPriority;
import org.example.taskmanagementsystem.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponseDTO createTask(TaskCreateDTO taskCreateDTO);
    Page<TaskResponseDTO> getAllTasks(Pageable pageable);
    Page<TaskResponseDTO> getMyTasks(Pageable pageable);
    TaskResponseDTO getTaskById(Long taskId);
    TaskResponseDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO);
    void deleteTask(Long taskId);
    Page<TaskResponseDTO> getTasksByUser(Long authorId, Pageable pageable);
    TaskResponseDTO updateTaskStatus(Long taskId, TaskStatus status);
    CommentResponseDTO addCommentToTask(Long taskId, CommentDTO commentDTO);

    // для админа
    TaskResponseDTO updateTaskForAdmin(Long id, TaskUpdateDTO taskUpdateDTO);
    void deleteTaskForAdmin(Long id);
    TaskResponseDTO updateTaskStatusForAdmin(Long taskId, TaskStatus status);
    TaskResponseDTO updateTaskPriorityForAdmin(Long taskId, TaskPriority priority);
    CommentResponseDTO addCommentToTaskForAdmin(Long taskId, CommentDTO commentDTO);
}

