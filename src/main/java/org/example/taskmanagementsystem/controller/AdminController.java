package org.example.taskmanagementsystem.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.taskmanagementsystem.service.TaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tasks")
@Tag(name = "Администрирование задач", description = "Управление задачами администратором")
@RequiredArgsConstructor
public class AdminController {
    private final TaskService taskService;

}

