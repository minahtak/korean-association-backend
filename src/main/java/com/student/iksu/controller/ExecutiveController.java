package com.student.iksu.controller;

import com.student.iksu.entity.Executive;
import com.student.iksu.service.ExecutiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // ★ 중요: HTML이 아니라 JSON 데이터를 반환하겠다는 뜻
@RequestMapping("/api/executives") // 주소도 명확하게 변경
@RequiredArgsConstructor
public class ExecutiveController {

    private final ExecutiveService executiveService;

    @GetMapping
    public ResponseEntity<List<Executive>> getAllExecutives() {
        List<Executive> executives = executiveService.getAllExecutives();
        return ResponseEntity.ok(executives);
    }
}