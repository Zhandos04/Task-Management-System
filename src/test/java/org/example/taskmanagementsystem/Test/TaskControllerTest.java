package org.example.taskmanagementsystem.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.taskmanagementsystem.TaskManagementSystemApplication;
import org.example.taskmanagementsystem.dto.request.CommentDTO;
import org.example.taskmanagementsystem.dto.request.TaskCreateDTO;
import org.example.taskmanagementsystem.dto.request.TaskUpdateDTO;
import org.example.taskmanagementsystem.dto.response.CommentResponseDTO;
import org.example.taskmanagementsystem.dto.response.TaskResponseDTO;
import org.example.taskmanagementsystem.entity.TaskPriority;
import org.example.taskmanagementsystem.entity.TaskStatus;
import org.example.taskmanagementsystem.entity.User;
import org.example.taskmanagementsystem.service.TaskService;
import org.example.taskmanagementsystem.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TaskManagementSystemApplication.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;
    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // create

    @Test
    @DisplayName("✅ Успешное создание задачи (201 Created)")
    @WithMockUser(username = "author@example.com")
    void createTask_Success() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("Новая задача");
        taskCreateDTO.setDescription("Описание задачи");
        taskCreateDTO.setPriority(TaskPriority.HIGH);
        taskCreateDTO.setExecutorEmail("executor@example.com");

        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setTitle(taskCreateDTO.getTitle());
        responseDTO.setDescription(taskCreateDTO.getDescription());
        responseDTO.setPriority(taskCreateDTO.getPriority());
        responseDTO.setStatus(TaskStatus.WAITING);

        User mockUser = new User();
        mockUser.setEmail("author@example.com");

        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(taskService.createTask(any(TaskCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Новая задача"))
                .andExpect(jsonPath("$.description").value("Описание задачи"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    @DisplayName("❌ Ошибка: запрос без токена (401 Unauthorized)")
    void createTask_Unauthorized() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("Новая задача");
        taskCreateDTO.setDescription("Описание задачи");
        taskCreateDTO.setPriority(TaskPriority.HIGH);
        taskCreateDTO.setExecutorEmail("executor@example.com");

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isUnauthorized()); // ❌ Доступ запрещен без токена
    }

    @Test
    @DisplayName("❌ Ошибка: исполнитель не найден (404 Not Found)")
    @WithMockUser(username = "author@example.com")
    void createTask_ExecutorNotFound() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("Задача");
        taskCreateDTO.setDescription("Описание");
        taskCreateDTO.setPriority(TaskPriority.MEDIUM);
        taskCreateDTO.setExecutorEmail("wrong@example.com");

        User mockUser = new User();
        mockUser.setEmail("author@example.com");

        when(userService.getCurrentUser()).thenReturn(mockUser);
        when(taskService.createTask(any(TaskCreateDTO.class)))
                .thenThrow(new UsernameNotFoundException("Исполнитель не найден."));

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Исполнитель не найден."));
    }

    // get all tasks

    @Test
    @DisplayName("✅ Успешное получение всех задач (200 OK)")
    @WithMockUser(username = "user@example.com")
    void getAllTasks_Success() throws Exception {
        List<TaskResponseDTO> taskList = List.of(
                new TaskResponseDTO(1L, "Task 1", "Description 1", TaskStatus.WAITING, TaskPriority.HIGH, "author@example.com", "executor@example.com", List.of()),
                new TaskResponseDTO(2L, "Task 2", "Description 2", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "author@example.com", "executor@example.com", List.of())
        );
        Page<TaskResponseDTO> page = new PageImpl<>(taskList);

        when(taskService.getAllTasks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/tasks/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Task 1"))
                .andExpect(jsonPath("$.content[1].title").value("Task 2"));
    }


    @Test
    @DisplayName("❌ Ошибка: запрос без токена (401 Unauthorized)")
    void getAllTasks_Unauthorized() throws Exception {
        mockMvc.perform(get("/tasks/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // get my tasks

    @Test
    @DisplayName("✅ Успешное получение моих задач (200 OK)")
    @WithMockUser(username = "user@example.com")
    void getMyTasks_Success() throws Exception {
        List<TaskResponseDTO> taskList = List.of(
                new TaskResponseDTO(1L, "My Task 1", "My Description 1", TaskStatus.WAITING, TaskPriority.HIGH, "user@example.com", "executor@example.com", List.of())
        );
        Page<TaskResponseDTO> page = new PageImpl<>(taskList);

        when(taskService.getMyTasks(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/tasks/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("My Task 1"));
    }

    @Test
    @DisplayName("❌ Ошибка: пользователь не найден (404 Not Found)")
    @WithMockUser(username = "unknown@example.com")
    void getMyTasks_UserNotFound() throws Exception {
        when(taskService.getMyTasks(any(Pageable.class)))
                .thenThrow(new UsernameNotFoundException("Пользователь не найден."));

        mockMvc.perform(get("/tasks/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Пользователь не найден."));
    }

    @Test
    @DisplayName("❌ Ошибка: запрос без токена (401 Unauthorized)")
    void getMyTasks_Unauthorized() throws Exception {
        mockMvc.perform(get("/tasks/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // edit

    @Test
    @DisplayName("✅ Успешное обновление задачи (200 OK)")
    @WithMockUser(username = "user@example.com")
    void updateTask_Success() throws Exception {
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle("Обновленный заголовок");
        taskUpdateDTO.setDescription("Обновленное описание");
        taskUpdateDTO.setPriority(TaskPriority.HIGH);
        taskUpdateDTO.setStatus(TaskStatus.IN_PROGRESS);
        taskUpdateDTO.setExecutorEmail("executor@example.com");

        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle(taskUpdateDTO.getTitle());
        responseDTO.setDescription(taskUpdateDTO.getDescription());
        responseDTO.setPriority(taskUpdateDTO.getPriority());
        responseDTO.setStatus(taskUpdateDTO.getStatus());

        when(taskService.updateTask(eq(1L), any(TaskUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/tasks/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Обновленный заголовок"))
                .andExpect(jsonPath("$.description").value("Обновленное описание"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("❌ Ошибка: задача не найдена (404 Not Found)")
    @WithMockUser(username = "user@example.com")
    void updateTask_NotFound() throws Exception {
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle("Заголовок");
        taskUpdateDTO.setDescription("Описание");
        taskUpdateDTO.setPriority(TaskPriority.LOW);
        taskUpdateDTO.setStatus(TaskStatus.WAITING);
        taskUpdateDTO.setExecutorEmail("executor@example.com");

        when(taskService.updateTask(eq(99L), any(TaskUpdateDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        mockMvc.perform(put("/tasks/edit/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Задача не найдена"));
    }

    @Test
    @DisplayName("❌ Ошибка: пользователь пытается изменить чужую задачу (403 Forbidden)")
    @WithMockUser(username = "user@example.com")
    void updateTask_Forbidden() throws Exception {
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle("Новый заголовок");
        taskUpdateDTO.setDescription("Новое описание");
        taskUpdateDTO.setPriority(TaskPriority.HIGH);
        taskUpdateDTO.setStatus(TaskStatus.IN_PROGRESS);
        taskUpdateDTO.setExecutorEmail("executor@example.com");

        when(taskService.updateTask(eq(1L), any(TaskUpdateDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете изменить задачу другого пользователя"));

        mockMvc.perform(put("/tasks/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Вы не можете изменить задачу другого пользователя"));
    }


    // delete

    @Test
    @DisplayName("✅ Успешное удаление задачи (200 OK)")
    @WithMockUser(username = "user@example.com")
    void deleteTask_Success() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Задача удалена"));
    }

    @Test
    @DisplayName("❌ Ошибка: задача не найдена (404 Not Found)")
    @WithMockUser(username = "user@example.com")
    void deleteTask_NotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"))
                .when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/tasks/delete/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Задача не найдена"));
    }

    @Test
    @DisplayName("❌ Ошибка: пользователь пытается удалить чужую задачу (403 Forbidden)")
    @WithMockUser(username = "user@example.com")
    void deleteTask_Forbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете удалить задачу другого пользователя"))
                .when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Вы не можете удалить задачу другого пользователя"));
    }


    // get tasks by user

    @Test
    @DisplayName("✅ Успешное получение задач по пользователю (200 OK)")
    @WithMockUser(username = "user@example.com") // ✅ Авторизованный пользователь
    void getTasksByUser_Success() throws Exception {
        List<TaskResponseDTO> taskList = List.of(
                new TaskResponseDTO(1L, "Task 1", "Description 1", TaskStatus.WAITING, TaskPriority.HIGH, "author@example.com", "executor@example.com", List.of())
        );
        Page<TaskResponseDTO> page = new PageImpl<>(taskList);

        when(taskService.getTasksByUser(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/tasks/by-user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Task 1"));
    }

    @Test
    @DisplayName("❌ Ошибка: запрос без токена (401 Unauthorized)")
    void getTasksByUser_Unauthorized() throws Exception {
        mockMvc.perform(get("/tasks/by-user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // edit task status

    @Test
    @DisplayName("✅ Успешное изменение статуса задачи (200 OK)")
    @WithMockUser(username = "executor@example.com")
    void updateTaskStatus_Success() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(TaskStatus.IN_PROGRESS);

        when(taskService.updateTaskStatus(eq(1L), eq(TaskStatus.IN_PROGRESS))).thenReturn(responseDTO);

        mockMvc.perform(patch("/tasks/1/change-status")
                        .param("status", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("❌ Ошибка: задача не найдена (404 Not Found)")
    @WithMockUser(username = "executor@example.com")
    void updateTaskStatus_NotFound() throws Exception {
        when(taskService.updateTaskStatus(eq(99L), eq(TaskStatus.IN_PROGRESS)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        mockMvc.perform(patch("/tasks/99/change-status")
                        .param("status", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Задача не найдена"));
    }

    @Test
    @DisplayName("❌ Ошибка: недостаточно прав (403 Forbidden)")
    @WithMockUser(username = "not-executor@example.com")
    void updateTaskStatus_Forbidden() throws Exception {
        when(taskService.updateTaskStatus(eq(1L), eq(TaskStatus.IN_PROGRESS)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете изменять статус этой задачи"));

        mockMvc.perform(patch("/tasks/1/change-status")
                        .param("status", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Вы не можете изменять статус этой задачи"));
    }

    // add-comment

    @Test
    @DisplayName("✅ Успешное добавление комментария (201 Created)")
    @WithMockUser(username = "executor@example.com")
    void addComment_Success() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Отличная работа!");

        CommentResponseDTO responseDTO = new CommentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setContent(commentDTO.getContent());
        responseDTO.setAuthorEmail("executor@example.com");

        when(taskService.addCommentToTask(eq(1L), any(CommentDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/tasks/1/add-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Отличная работа!"));
    }

    @Test
    @DisplayName("❌ Ошибка: комментарий слишком короткий (400 Bad Request)")
    @WithMockUser(username = "executor@example.com")
    void addComment_ValidationError() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Коротко");

        mockMvc.perform(post("/tasks/1/add-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Ошибка: недостаточно прав (403 Forbidden)")
    @WithMockUser(username = "unauthorized@example.com")
    void addComment_Forbidden() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Отличная работа!");

        when(taskService.addCommentToTask(eq(1L), any(CommentDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете оставлять комментарию в эту задачу"));

        mockMvc.perform(post("/tasks/1/add-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Вы не можете оставлять комментарию в эту задачу"));
    }
}
