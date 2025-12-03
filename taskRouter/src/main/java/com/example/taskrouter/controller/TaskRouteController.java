package com.example.taskrouter.controller;

import com.example.taskrouter.dto.MessageRequestDTO;
import com.example.taskrouter.dto.TaskRouteResponse;
import com.example.taskrouter.service.TaskRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskRouteController {
    private final TaskRouteService routerService;

    @PostMapping("/route")
    public ResponseEntity<TaskRouteResponse> route(@Valid @RequestBody MessageRequestDTO task) {
        TaskRouteResponse response = routerService.acceptAndRoute(task);
        return ResponseEntity.ok(response);
    }
}
