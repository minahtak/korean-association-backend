package com.student.iksu.service;

import com.student.iksu.dto.request.MaterialFormDto;
import com.student.iksu.dto.request.MyPageUpdateDto;
import com.student.iksu.entity.Material;
import com.student.iksu.entity.Member;
import com.student.iksu.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    // 1. 목록 조회 (필터링 만능 검색)
    @Transactional(readOnly = true)
    public Page<Material> getMaterialList(String school, String major, String language,
                                          String translationType, String keyword, Pageable pageable) {
        return materialRepository.findMaterials(school, major, language, translationType, keyword, pageable);
    }

    // 2. 자료 저장 (수정됨: DTO에서 작성자 이름 가져오기)
    // 파라미터에서 writerName을 제거하고, dto.getWriter()를 사용합니다.
    public void saveMaterial(MaterialFormDto dto) {
        Material material = new Material();

        // DTO 내용 옮겨 담기
        material.setTitle(dto.getTitle());
        material.setContent(dto.getContent());
        material.setGoogleDriveLink(dto.getGoogleDriveLink());

        material.setSchool(dto.getSchool());
        material.setMajor(dto.getMajor());
        material.setSubject(dto.getSubject());

        material.setProfessor(dto.getProfessor());

        material.setCategory(dto.getCategory());

        material.setLanguage(dto.getLanguage());
        material.setTranslationType(dto.getTranslationType());
        // 번역본(AI, Human)이면 true, 원본(Original)이면 false
        material.setTranslated(!"Original".equals(dto.getTranslationType()));

        // ★ [핵심 수정] 관리자 이름 대신, 폼에서 입력한 기여자 이름(또는 익명)을 저장
        material.setWriter(dto.getWriter());

        // 관리 정보
        material.setRegDate(LocalDateTime.now());
        material.setViewCount(0);


        materialRepository.save(material);
    }

    // 3. 상세 조회
    @Transactional(readOnly = true)
    public Material findById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("자료가 없습니다. id=" + id));
    }

    // 4. 조회수 증가
    public void updateViewCount(Long id) {
        Material material = findById(id);
        material.setViewCount(material.getViewCount() + 1);
    }

    // 5. 수정
    public void update(Long id, MaterialFormDto dto) {
        Material material = findById(id);

        // 내용 업데이트
        material.updateMaterial(
                dto.getTitle(), dto.getContent(), dto.getGoogleDriveLink(),
                dto.getSchool(), dto.getMajor(), dto.getSubject(), dto.getProfessor(), // <-- professor 추가
                dto.getLanguage(), dto.getTranslationType()
        );

        // 기여자 이름도 수정할 수 있게 업데이트 (혹시 오타 났을 때를 대비해)
        if (dto.getWriter() != null && !dto.getWriter().isEmpty()) {
            material.setWriter(dto.getWriter());
        }

        // 번역 여부 재계산
        material.setTranslated(!"Original".equals(dto.getTranslationType()));
    }

    // 6. 삭제
    public void delete(Long id) {
        Material material = findById(id);
        materialRepository.delete(material);
    }

    // 7. DTO 변환 (수정 폼용)
    @Transactional(readOnly = true)
    public MaterialFormDto getMaterialDtl(Long id) {
        Material m = findById(id);
        MaterialFormDto dto = new MaterialFormDto();
        dto.setId(m.getId());

        // ★ 수정 폼에 기존 작성자 이름도 띄워주기 위해 담음
        dto.setWriter(m.getWriter());

        dto.setTitle(m.getTitle());
        dto.setContent(m.getContent());
        dto.setGoogleDriveLink(m.getGoogleDriveLink());
        dto.setSchool(m.getSchool());
        dto.setMajor(m.getMajor());
        dto.setSubject(m.getSubject());

        dto.setProfessor(m.getProfessor());

        dto.setCategory(m.getCategory());
        dto.setLanguage(m.getLanguage());
        dto.setTranslationType(m.getTranslationType());
        return dto;
    }

}