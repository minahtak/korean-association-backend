package com.student.iksu.controller;

import com.student.iksu.dto.request.MaterialFormDto;
import com.student.iksu.entity.Material;
import com.student.iksu.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Material>> getMaterials(
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String major,
            // 프론트엔드가 languages나 language 둘 중 무엇으로 보내도 받도록 alias 처리
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "languages", required = false) String languages,
            @RequestParam(required = false) String translationType,
            @RequestParam(required = false) String keyword,
            // ★ 최신순(id 내림차순 또는 regDate 내림차순)으로 기본 정렬 지정
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // languages로 넘어왔을 경우 language에 할당
        String targetLanguage = (language != null) ? language : languages;

        Page<Material> materials = materialService.getMaterialList(
                school, major, targetLanguage, translationType, keyword, pageable);
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