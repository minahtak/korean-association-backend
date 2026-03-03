package com.student.iksu.controller;

import com.student.iksu.dto.request.NoticeFormDto;
import com.student.iksu.entity.Member;
import com.student.iksu.entity.Notice;
import com.student.iksu.repository.MemberRepository;
import com.student.iksu.repository.NoticeRepository;
import com.student.iksu.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    private final NoticeService noticeService;

    // 1. 목록 조회 (페이징, 검색, 정렬 기능 추가)
    @GetMapping("")
    public String list(Model model,
                       @RequestParam(required = false) String school,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false, defaultValue = "latest") String sort, // 정렬 기준
                       @PageableDefault(page = 0, size = 10) Pageable pageable) { // 1페이지당 10개

        // 1. 정렬 기준 만들기 (고정글은 무조건 맨 위!)
        Sort sortOption;
        if ("views".equals(sort)) {
            // 조회수 순 (고정글 우선 -> 조회수 높은순 -> 최신순)
            sortOption = Sort.by("isPinned").descending()
                    .and(Sort.by("viewCount").descending())
                    .and(Sort.by("regDate").descending());
        } else {
            // 최신순 (고정글 우선 -> 최신순)
            sortOption = Sort.by("isPinned").descending()
                    .and(Sort.by("regDate").descending());
        }

        // 2. 페이지 요청 객체 만들기 (사용자가 요청한 페이지, 10개씩, 정렬옵션)
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortOption);

        // 3. 데이터 가져오기 (Page 객체)
        Page<Notice> notices = noticeService.getNoticeList(school, category, keyword, sortedPageable);

        // 4. 화면에 전달
        model.addAttribute("notices", notices);
        model.addAttribute("activeFilter", school != null ? school : (category != null ? category : "all"));
        model.addAttribute("school", school);     // 검색바 유지용
        model.addAttribute("category", category); // 검색바 유지용
        model.addAttribute("keyword", keyword);   // 검색바 유지용
        model.addAttribute("sort", sort);         // 정렬버튼 유지용

        return "notices/noticeList";
    }

    // 2. 글쓰기 폼
    @GetMapping("/new")
    public String writeForm(Model model) {
        model.addAttribute("noticeFormDto", new NoticeFormDto());
        return "notices/noticeForm";
    }

    // 3. 글 저장
    @PostMapping("/new")
    public String write(NoticeFormDto noticeFormDto, @AuthenticationPrincipal User user) {
        Member writer = memberRepository.findByUsername(user.getUsername()).orElseThrow();

        Notice notice = new Notice();
        notice.setTitle(noticeFormDto.getTitle());
        notice.setContent(noticeFormDto.getContent());
        notice.setCategory(noticeFormDto.getCategory());
        notice.setTargetSchool(noticeFormDto.getTargetSchool());
        notice.setWriter(writer.getName());
        notice.setRegDate(LocalDateTime.now());
        notice.setViewCount(0);
        notice.setPinned(noticeFormDto.getIsPinned() != null && noticeFormDto.getIsPinned());

        noticeRepository.save(notice);
        return "redirect:/notices";
    }

    // 4. 상세 보기
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다."));

        // 조회수 증가
        notice.setViewCount(notice.getViewCount() + 1);
        noticeRepository.save(notice);

        // 권한 체크 로직
        boolean isWriter = false;
        boolean canDelete = false;

        if (user != null) {
            Member member = memberRepository.findByUsername(user.getUsername()).orElseThrow();

            if (notice.getWriter().equals(member.getName())) {
                isWriter = true;
            }

            boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isStaff = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

            if (isWriter || isAdmin || isStaff) {
                canDelete = true;
            }
        }

        model.addAttribute("notice", notice);
        model.addAttribute("isWriter", isWriter);
        model.addAttribute("canDelete", canDelete);

        return "notices/noticeDetail";
    }

    // 5. 수정 화면
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {
        Notice notice = noticeRepository.findById(id).orElseThrow();
        Member currentMember = memberRepository.findByUsername(user.getUsername()).orElseThrow();

        if (!notice.getWriter().equals(currentMember.getName())) {
            return "redirect:/notices/" + id;
        }

        NoticeFormDto dto = new NoticeFormDto();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setCategory(notice.getCategory());
        dto.setTargetSchool(notice.getTargetSchool());
        dto.setIsPinned(notice.isPinned());

        model.addAttribute("noticeFormDto", dto);
        return "notices/noticeForm";
    }

    // 6. 수정 실행
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, NoticeFormDto dto, @AuthenticationPrincipal User user) {
        Notice notice = noticeRepository.findById(id).orElseThrow();
        Member currentMember = memberRepository.findByUsername(user.getUsername()).orElseThrow();

        if (notice.getWriter().equals(currentMember.getName())) {
            notice.updateNotice(
                    dto.getTitle(),
                    dto.getContent(),
                    dto.getTargetSchool(),
                    dto.getCategory(),
                    dto.getIsPinned() != null && dto.getIsPinned()
            );
            noticeRepository.save(notice);
        }
        return "redirect:/notices/" + id;
    }

    // 7. 삭제 실행
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        Notice notice = noticeRepository.findById(id).orElseThrow();
        Member currentMember = memberRepository.findByUsername(user.getUsername()).orElseThrow();

        boolean isWriter = notice.getWriter().equals(currentMember.getName());
        boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStaff = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

        if (isWriter || isAdmin || isStaff) {
            noticeRepository.delete(notice);
            redirectAttributes.addFlashAttribute("msg", "🗑️ 게시글이 삭제되었습니다.");
        }

        return "redirect:/notices";
    }
}