package com.student.iksu.service;

import com.student.iksu.dto.request.NoticeFormDto;
import com.student.iksu.entity.Notice;
import com.student.iksu.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 1. 글 등록 (원래 컨트롤러에 있던 거 가져옴)
    public void saveNotice(NoticeFormDto noticeFormDto, String writerId) {
        Notice notice = new Notice();
        notice.setTitle(noticeFormDto.getTitle());
        notice.setContent(noticeFormDto.getContent());
        notice.setTargetSchool(noticeFormDto.getTargetSchool());
        notice.setCategory(noticeFormDto.getCategory());
        notice.setWriter(writerId); // 작성자 ID
        notice.setPinned(noticeFormDto.getIsPinned());

        noticeRepository.save(notice);
    }

    // 2. 목록 조회 (페이징 + 검색 + 필터)
    @Transactional(readOnly = true)
    public Page<Notice> getNoticeList(String school, String category, String keyword, Pageable pageable) {
        return noticeRepository.findNotices(school, category, keyword, pageable);
    }

    // 3. 상세 조회 (ID로 찾기)
    @Transactional(readOnly = true)
    public Notice findById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
    }

    // 4. 조회수 증가 (상세 페이지 볼 때마다 +1)
    public void updateViewCount(Long id) {
        // JPA의 더티 체킹(자동감지) 기능 이용 (별도 저장 코드 필요 없음)
        Notice notice = noticeRepository.findById(id).orElseThrow();
        notice.setViewCount(notice.getViewCount() + 1);
    }

    // 5. 글 수정
    public void update(Long id, NoticeFormDto dto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 엔티티의 내용 변경 -> 트랜잭션이 끝날 때 자동 DB 반영 (Update 쿼리 나감)
        notice.updateNotice(
                dto.getTitle(),
                dto.getContent(),
                dto.getTargetSchool(),
                dto.getCategory(),
                dto.getIsPinned()
        );
    }

    // 6. 글 삭제
    public void delete(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
        noticeRepository.delete(notice);
    }

    // 7. 수정 폼을 위해 DTO로 변환해서 주기
    @Transactional(readOnly = true)
    public NoticeFormDto getNoticeDtl(Long id) {
        Notice notice = findById(id);

        NoticeFormDto dto = new NoticeFormDto();
        dto.setId(notice.getId());
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getContent());
        dto.setTargetSchool(notice.getTargetSchool());
        dto.setCategory(notice.getCategory());
        dto.setIsPinned(notice.isPinned());
        return dto;
    }
}