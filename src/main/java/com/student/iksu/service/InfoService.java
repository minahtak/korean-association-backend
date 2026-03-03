package com.student.iksu.service;

import com.student.iksu.constant.InfoCategory;
import com.student.iksu.constant.InfoStatus;
import com.student.iksu.dto.request.InfoFormDto;
import com.student.iksu.entity.Info;
import com.student.iksu.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User; // ★ User 객체 임포트 필요
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class InfoService {

    private final InfoRepository infoRepository;

    // 1. 목록 조회 (수정됨: status 파라미터 삭제 -> username, isAdmin 추가)
    @Transactional(readOnly = true)
    public Page<Info> getInfoList(String schoolTag, String categoryStr, String keyword, String status,
                                  String username, boolean isAdmin, // ★ 변경된 부분
                                  Pageable pageable) {

        // 카테고리 문자열 -> Enum 변환
        InfoCategory category = null;
        if (categoryStr != null && !categoryStr.isEmpty() && !categoryStr.equals("All")) {
            try {
                category = InfoCategory.valueOf(categoryStr);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리가 들어오면 null 처리 (전체 조회)
                category = null;
            }
        }

        // 커스텀 쿼리 호출
        return infoRepository.findInfosCustom(schoolTag, category, keyword, status, username, isAdmin, pageable);
    }

    // 2. 저장 로직 (작성해주신 코드 그대로 유지하되, 약간의 안전장치 추가)
    public void saveInfo(InfoFormDto dto, User user) {
        Info info = new Info();
        info.setTitle(dto.getTitle());
        info.setContent(dto.getContent());
        info.setCategory(dto.getCategory());
        info.setSchoolTag(dto.getSchoolTag());
        info.setTargetTag(dto.getTargetTag());

        // 작성자 ID 저장
        info.setWriter(user.getUsername());

        info.setRegDate(LocalDateTime.now());
        info.setViewCount(0);

        // 관리자(ADMIN, STAFF)면 바로 APPROVED, 아니면 PENDING
        boolean isManager = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN") ||
                        a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("STAFF"));

        if (isManager) {
            info.setStatus(InfoStatus.APPROVED); // 관리자는 프리패스!
        } else {
            info.setStatus(InfoStatus.PENDING);  // 일반 유저는 대기
        }

        infoRepository.save(info);
    }

    // 상세 조회
    public Info getInfo(Long id) {
        Info info = infoRepository.findById(id).orElseThrow();
        info.setViewCount(info.getViewCount() + 1);
        return info;
    }

    // 승인/반려 (관리자용)
    public void approveInfo(Long id, InfoStatus status, String reason) {
        Info info = infoRepository.findById(id).orElseThrow();
        info.setStatus(status);
        if (status == InfoStatus.REJECTED) {
            info.setRejectionReason(reason);
        }
    }

    // DTO 변환
    @Transactional(readOnly = true)
    public InfoFormDto getInfoDtl(Long id) {
        Info info = infoRepository.findById(id).orElseThrow();
        InfoFormDto dto = new InfoFormDto();
        dto.setId(info.getId());
        dto.setTitle(info.getTitle());
        dto.setContent(info.getContent());
        dto.setCategory(info.getCategory());
        dto.setSchoolTag(info.getSchoolTag());
        dto.setTargetTag(info.getTargetTag());
        return dto;
    }

    // 수정
    public void updateInfo(Long id, InfoFormDto dto) {
        Info info = infoRepository.findById(id).orElseThrow();
        info.setTitle(dto.getTitle());
        info.setContent(dto.getContent());
        info.setCategory(dto.getCategory());
        info.setSchoolTag(dto.getSchoolTag());
        info.setTargetTag(dto.getTargetTag());
    }

    // 삭제
    public void deleteInfo(Long id) {
        infoRepository.deleteById(id);
    }
}