package com.student.iksu.controller;

import com.student.iksu.dto.request.MaterialFormDto;
import com.student.iksu.entity.Material;
import com.student.iksu.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
// ★ @CrossOrigin 삭제함 (SecurityConfig에서 전역으로 처리)
public class MaterialApiController {

    private final MaterialService materialService;

    // 1. 목록 조회 (로그인한 사용자만 가능)
    @GetMapping
    @PreAuthorize("isAuthenticated()") // ★ 로그인 여부 명시적 체크 (보안 강화)
    public ResponseEntity<Page<Material>> getMaterials(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String translationType,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12) Pageable pageable) {

        Page<Material> materials = materialService.getMaterialList(school, major, language, translationType, keyword, pageable);
        return ResponseEntity.ok(materials);
    }

    // 2. 상세 조회 (로그인한 사용자만 가능)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // ★ 로그인 여부 명시적 체크
    public ResponseEntity<Material> getDetail(@PathVariable Long id) {
        materialService.updateViewCount(id);
        return ResponseEntity.ok(materialService.findById(id));
    }

    // 3. 자료 등록 (관리자/임원 권한 체크)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<?> create(@RequestBody MaterialFormDto dto) {
        try {
            materialService.saveMaterial(dto);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 자료 삭제 (관리자/임원 권한 체크)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResponseEntity.ok("deleted");
    }

    // ★ 5. 자료 수정
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MaterialFormDto dto) {
        try {

            materialService.update(id, dto);
            return ResponseEntity.ok("updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}