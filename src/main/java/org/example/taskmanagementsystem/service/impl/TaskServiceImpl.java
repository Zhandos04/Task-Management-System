package org.example.taskmanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.dto.request.TaskCreateDTO;
import org.example.taskmanagementsystem.dto.request.TaskUpdateDTO;
import org.example.taskmanagementsystem.dto.request.CommentDTO;
import org.example.taskmanagementsystem.dto.response.CommentResponseDTO;
import org.example.taskmanagementsystem.dto.response.TaskResponseDTO;
import org.example.taskmanagementsystem.entity.*;
import org.example.taskmanagementsystem.repository.CommentRepository;
import org.example.taskmanagementsystem.repository.TaskRepository;
import org.example.taskmanagementsystem.repository.UserRepository;
import org.example.taskmanagementsystem.service.TaskService;
import org.example.taskmanagementsystem.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskCreateDTO taskCreateDTO) {
        User author = userRepository.findByEmail(userService.getCurrentUser().getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Автор не найден."));
        User executor = userRepository.findByEmail(taskCreateDTO.getExecutorEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Исполнитель не найден."));

        Task task = modelMapper.map(taskCreateDTO, Task.class);
        task.setStatus(TaskStatus.WAITING);
        task.setAuthor(author);
        task.setExecutor(executor);

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    @Override
    public Page<TaskResponseDTO> getAllTasks(Pageable pageable) {
        Page<Task> tasks = taskRepository.findAll(pageable);
        return tasks.map(this::convertToDTO);
    }

    @Override
    public Page<TaskResponseDTO> getMyTasks(Pageable pageable) {
        User user = userRepository.findByEmail(userService.getCurrentUser().getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден."));
        Page<Task> tasks = taskRepository.findByAuthor_Id(user.getId(), pageable);
        return tasks.map(this::convertToDTO);
    }

    @Override
    public TaskResponseDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        return convertToDTO(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        if (!task.getAuthor().getEmail().equals(userService.getCurrentUser().getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете изменить задачу другого пользователя");
        }

        return updateAndGetTaskResponseDTO(taskUpdateDTO, task);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        if (!task.getAuthor().getEmail().equals(userService.getCurrentUser().getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете удалить задачу другого пользователя");
        }
        taskRepository.delete(task);
    }

    @Override
    public Page<TaskResponseDTO> getTasksByUser(Long authorId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByAuthor_Id(authorId, pageable);
        return tasks.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        User executor = userRepository.findByEmail(userService.getCurrentUser().getUsername())
                        .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден."));
        if (!task.getExecutor().getEmail().equals(executor.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете изменять статус этой задачи");
        }
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Override
    @Transactional
    public CommentResponseDTO addCommentToTask(Long taskId, CommentDTO commentDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        User user = userRepository.findByEmail(userService.getCurrentUser().getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        if (!user.getEmail().equals(task.getExecutor().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете оставлять комментарию в эту задачу");
        }
        return addCommentAndGetCommentResponseDTO(commentDTO, task, user);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskForAdmin(Long id, TaskUpdateDTO taskUpdateDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        return updateAndGetTaskResponseDTO(taskUpdateDTO, task);
    }

    @Override
    @Transactional
    public void deleteTaskForAdmin(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskStatusForAdmin(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskPriorityForAdmin(Long taskId, TaskPriority priority) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        task.setPriority(priority);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Override
    @Transactional
    public CommentResponseDTO addCommentToTaskForAdmin(Long taskId, CommentDTO commentDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));
        User admin = userRepository.findByEmail(userService.getCurrentUser().getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Администратор не найден"));
        return addCommentAndGetCommentResponseDTO(commentDTO, task, admin);
    }

    private CommentResponseDTO addCommentAndGetCommentResponseDTO(CommentDTO commentDTO, Task task, User user) {
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(user);
        comment.setTask(task);
        comment.setCreatedAt(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        CommentResponseDTO commentResponseDTO = modelMapper.map(savedComment, CommentResponseDTO.class);
        commentResponseDTO.setAuthorEmail(savedComment.getAuthor().getEmail());
        return commentResponseDTO;
    }

    private TaskResponseDTO updateAndGetTaskResponseDTO(TaskUpdateDTO taskUpdateDTO, Task task) {
        if (taskUpdateDTO.getTitle() != null) {
            task.setTitle(taskUpdateDTO.getTitle());
        }
        if (taskUpdateDTO.getDescription() != null) {
            task.setDescription(taskUpdateDTO.getDescription());
        }
        if (taskUpdateDTO.getStatus() != null) {
            task.setStatus(taskUpdateDTO.getStatus());
        }
        if (taskUpdateDTO.getPriority() != null) {
            task.setPriority(taskUpdateDTO.getPriority());
        }
        if (taskUpdateDTO.getExecutorEmail() != null) {
            User executor = userRepository.findByEmail(taskUpdateDTO.getExecutorEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Исполнитель не найден."));
            task.setExecutor(executor);
        }
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    private TaskResponseDTO convertToDTO(Task task) {
        TaskResponseDTO dto = modelMapper.map(task, TaskResponseDTO.class);
        dto.setAuthorEmail(task.getAuthor().getEmail());
        dto.setExecutorEmail(task.getExecutor().getEmail());
        List<CommentResponseDTO> commentDTOs = task.getComments().stream()
                .map(comment -> {
                    CommentResponseDTO commentDTO = modelMapper.map(comment, CommentResponseDTO.class);
                    commentDTO.setAuthorEmail(comment.getAuthor().getEmail());
                    return commentDTO;
                })
                .collect(Collectors.toList());
        dto.setComments(commentDTOs);
        return dto;
    }
}
