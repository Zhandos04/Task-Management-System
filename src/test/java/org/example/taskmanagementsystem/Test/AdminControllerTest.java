package org.example.taskmanagementsystem.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.taskmanagementsystem.TaskManagementSystemApplication;
import org.example.taskmanagementsystem.dto.request.CommentDTO;
import org.example.taskmanagementsystem.dto.request.TaskUpdateDTO;
import org.example.taskmanagementsystem.dto.response.CommentResponseDTO;
import org.example.taskmanagementsystem.dto.response.TaskResponseDTO;
import org.example.taskmanagementsystem.entity.TaskPriority;
import org.example.taskmanagementsystem.entity.TaskStatus;
import org.example.taskmanagementsystem.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = TaskManagementSystemApplication.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // edit

    @Test
    @DisplayName("✅ Успешное обновление задачи администратором (200 OK)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"}) // ✅ Админ может обновлять задачи
    void updateTaskForAdmin_Success() throws Exception {
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

        when(taskService.updateTaskForAdmin(eq(1L), any(TaskUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/admin/tasks/edit/1")
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
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateTaskForAdmin_NotFound() throws Exception {
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle("Новый заголовок");
        taskUpdateDTO.setDescription("Новое описание");
        taskUpdateDTO.setPriority(TaskPriority.MEDIUM);
        taskUpdateDTO.setStatus(TaskStatus.WAITING);
        taskUpdateDTO.setExecutorEmail("executor@example.com");

        when(taskService.updateTaskForAdmin(eq(99L), any(TaskUpdateDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        mockMvc.perform(put("/admin/tasks/edit/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Задача не найдена"));
    }

    @Test
    @DisplayName("❌ Ошибка: обычный пользователь не может обновлять задачи (403 Forbidden)")
    @WithMockUser(username = "user@example.com")
    void updateTaskForAdmin_Forbidden() throws Exception {
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle("Заголовок");
        taskUpdateDTO.setDescription("Описание");
        taskUpdateDTO.setPriority(TaskPriority.LOW);
        taskUpdateDTO.setStatus(TaskStatus.WAITING);
        taskUpdateDTO.setExecutorEmail("executor@example.com");

        mockMvc.perform(put("/admin/tasks/edit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isForbidden());
    }

    // delete

    @Test
    @DisplayName("✅ Успешное удаление задачи администратором (200 OK)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteTaskForAdmin_Success() throws Exception {
        doNothing().when(taskService).deleteTaskForAdmin(1L);

        mockMvc.perform(delete("/admin/tasks/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Задача удалена"));
    }

    @Test
    @DisplayName("❌ Ошибка: задача не найдена (404 Not Found)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteTaskForAdmin_NotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"))
                .when(taskService).deleteTaskForAdmin(99L);

        mockMvc.perform(delete("/admin/tasks/delete/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Задача не найдена"));
    }

    @Test
    @DisplayName("❌ Ошибка: обычный пользователь не может удалять задачи (403 Forbidden)")
    @WithMockUser(username = "user@example.com")
    void deleteTaskForAdmin_Forbidden() throws Exception {
        mockMvc.perform(delete("/admin/tasks/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // change-status

    @Test
    @DisplayName("✅ Успешное изменение статуса задачи администратором (200 OK)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateTaskStatusForAdmin_Success() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(TaskStatus.COMPLETED);

        when(taskService.updateTaskStatusForAdmin(eq(1L), eq(TaskStatus.COMPLETED))).thenReturn(responseDTO);

        mockMvc.perform(patch("/admin/tasks/1/change-status")
                        .param("status", "COMPLETED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("❌ Ошибка: задача не найдена (404 Not Found)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateTaskStatusForAdmin_NotFound() throws Exception {
        when(taskService.updateTaskStatusForAdmin(eq(99L), eq(TaskStatus.IN_PROGRESS)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Задача не найдена"));

        mockMvc.perform(patch("/admin/tasks/99/change-status")
                        .param("status", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Задача не найдена"));
    }

    @Test
    @DisplayName("❌ Ошибка: обычный пользователь не может менять статус (403 Forbidden)")
    @WithMockUser(username = "user@example.com")
    void updateTaskStatusForAdmin_Forbidden() throws Exception {
        mockMvc.perform(patch("/admin/tasks/1/change-status")
                        .param("status", "IN_PROGRESS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // change-priority

    @Test
    @DisplayName("✅ Успешное изменение приоритета задачи администратором (200 OK)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void updateTaskPriorityForAdmin_Success() throws Exception {
        TaskResponseDTO responseDTO = new TaskResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPriority(TaskPriority.HIGH);

        when(taskService.updateTaskPriorityForAdmin(eq(1L), eq(TaskPriority.HIGH))).thenReturn(responseDTO);

        mockMvc.perform(patch("/admin/tasks/1/change-priority")
                        .param("priority", "HIGH")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @DisplayName("❌ Ошибка: обычный пользователь не может менять приоритет (403 Forbidden)")
    @WithMockUser(username = "user@example.com")
    void updateTaskPriorityForAdmin_Forbidden() throws Exception {
        mockMvc.perform(patch("/admin/tasks/1/change-priority")
                        .param("priority", "HIGH")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // add-comment

    @Test
    @DisplayName("✅ Успешное добавление комментария администратором (201 Created)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void addCommentToTaskForAdmin_Success() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Очень важно!");

        CommentResponseDTO responseDTO = new CommentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setContent(commentDTO.getContent());
        responseDTO.setAuthorEmail("admin@example.com");

        when(taskService.addCommentToTaskForAdmin(eq(1L), any(CommentDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/admin/tasks/1/add-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Очень важно!"));
    }

    @Test
    @DisplayName("❌ Ошибка: комментарий слишком короткий (400 Bad Request)")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void addCommentToTaskForAdmin_ValidationError() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Коротко");

        mockMvc.perform(post("/admin/tasks/1/add-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Ошибка: обычный пользователь не может добавлять комментарии (403 Forbidden)")
    @WithMockUser(username = "user@example.com")
    void addCommentToTaskForAdmin_Forbidden() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Важный комментарий!");

        mockMvc.perform(post("/admin/1/add-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isForbidden());
    }
}
