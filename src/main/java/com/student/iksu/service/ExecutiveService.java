package com.student.iksu.service;

import com.student.iksu.constant.Role;
import com.student.iksu.dto.request.ExecutiveFormDto;
import com.student.iksu.entity.Executive;
import com.student.iksu.entity.Member;
import com.student.iksu.repository.ExecutiveRepository;
import com.student.iksu.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExecutiveService {

    private final ExecutiveRepository executiveRepository;
    private final MemberRepository memberRepository;

    public List<Executive> getAllExecutives() {
        return executiveRepository.findAll();
    }

    public void promoteMember(Long memberId, ExecutiveFormDto dto) {
        // 로그 출력 (서버 콘솔 확인용)
        System.out.println(">>> promoteMember 실행됨. ID: " + memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        // 1. 이름/학교 정보가 비어있는지 확인 (방어 코드)
        String memberName = member.getName();
        String memberSchool = member.getSchool();

        if (memberName == null) memberName = "이름 없음";
        if (memberSchool == null) memberSchool = "학교 정보 없음";

        System.out.println(">>> 회원 정보 가져옴: " + memberName + " / " + memberSchool);

        // 2. 권한 변경
        member.setRole(Role.STAFF);

        // 3. 임원 저장
        Executive executive = new Executive();
        executive.setMember(member);

        // ★★★ 여기서 값을 넣어줍니다 ★★★
        executive.setName(memberName);
        executive.setSchool(memberSchool);

        executive.setRole(dto.getRole());
        executive.setIntro(dto.getIntro());
        executive.setImageUrl(dto.getImageUrl());

        executiveRepository.save(executive);
        System.out.println(">>> 임원 테이블 저장 완료");
    }

    public void updateExecutive(Long id, ExecutiveFormDto dto) {
        Executive executive = executiveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 임원이 없습니다."));
        executive.setRole(dto.getRole());
        executive.setIntro(dto.getIntro());
        executive.setImageUrl(dto.getImageUrl());
    }

    public void deleteExecutiveByMemberId(Long memberId) {
        Executive exec = executiveRepository.findByMemberId(memberId);
        if (exec != null) {
            executiveRepository.delete(exec);
        }
    }
}