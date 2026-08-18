package com.student.iksu.controller;

import com.student.iksu.dto.request.MaterialFormDto;
import com.student.iksu.entity.Material;
import com.student.iksu.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    // MemberRepository는 이제 권한 체크에서 안 쓰이므로 뺄 수도 있지만, 혹시 몰라 둡니다.
    // private final MemberRepository memberRepository;

    /**
     * 1. 족보 창고 목록 조회 (필터링)
     */
    @GetMapping("")
    public String list(Model model,
                       @RequestParam(required = false) String school,
                       @RequestParam(required = false) String major,
                       @RequestParam(required = false) String language,
                       @RequestParam(required = false) String translationType,
                       @RequestParam(required = false) String keyword,
                       @PageableDefault(page = 0, size = 12, sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable) {

        // 빈 문자열("")을 null로 변환
        if (school != null && school.isEmpty()) school = null;
        if (major != null && major.isEmpty()) major = null;
        if (language != null && language.isEmpty()) language = null;
        if (translationType != null && translationType.isEmpty()) translationType = null;
        if (keyword != null && keyword.isEmpty()) keyword = null;

        // 서비스 호출
        Page<Material> materials = materialService.getMaterialList(school, major, language, translationType, keyword, pageable);

        // 화면 전달
        model.addAttribute("materials", materials);

        // 검색 조건 유지
        model.addAttribute("school", school);
        model.addAttribute("major", major);
        model.addAttribute("language", language);
        model.addAttribute("translationType", translationType);
        model.addAttribute("keyword", keyword);

        return "materials/materialList";
    }

    /**
     * 2. 자료 등록 폼
     */
    @GetMapping("/new")
    public String writeForm(Model model) {
        model.addAttribute("materialFormDto", new MaterialFormDto());
        return "materials/materialForm";
    }

    // 3. 자료 저장
    @PostMapping("/new")
    public String write(MaterialFormDto dto, @AuthenticationPrincipal User user) {

        // 안전장치: 이름이 없으면 익명 처리
        if (dto.getWriter() == null || dto.getWriter().trim().isEmpty()) {
            dto.setWriter("익명");
        }

        materialService.saveMaterial(dto);

        return "redirect:/materials";
    }

    // 4. 상세 보기
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        materialService.updateViewCount(id);
        Material material = materialService.findById(id);

        boolean canDelete = false;

        // ▼▼▼ [수정 2] 이름 비교 삭제 -> 관리자 권한만 확인 ▼▼▼
        // 이제 writer는 '홍길동', '익명' 같은 텍스트이므로, 로그인한 관리자 ID와 다릅니다.
        // 따라서 관리자(ADMIN)나 임원(STAFF)이면 무조건 수정/삭제 버튼을 보여줍니다.
        if (user != null) {
            boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isStaff = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

            if (isAdmin || isStaff) canDelete = true;
        }

        model.addAttribute("material", material);
        model.addAttribute("canDelete", canDelete); // isWriter는 이제 필요 없어서 제거

        return "materials/materialDetail";
    }

    // 5. 수정 폼
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        // 관리자 권한 체크
        boolean hasAuth = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));

        if (!hasAuth) {
            return "redirect:/materials/" + id; // 권한 없으면 튕겨냄
        }

        MaterialFormDto dto = materialService.getMaterialDtl(id);
        model.addAttribute("materialFormDto", dto);
        return "materials/materialForm";
    }

    // 6. 수정 실행
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, MaterialFormDto dto, @AuthenticationPrincipal User user) {
        // 관리자 권한 체크
        boolean hasAuth = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));

        if (hasAuth) {
            materialService.update(id, dto);
        }
        return "redirect:/materials/" + id;
    }

    // 7. 삭제 실행
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        // 관리자 권한 체크
        boolean hasAuth = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));

        if (hasAuth) {
            materialService.delete(id);
            redirectAttributes.addFlashAttribute("msg", "🗑자료가 삭제되었습니다.");
        }
        return "redirect:/materials";
    }
}