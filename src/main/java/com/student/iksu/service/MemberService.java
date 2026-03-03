package com.student.iksu.service;

import com.student.iksu.constant.DegreeLevel;
import com.student.iksu.constant.Role;
import com.student.iksu.dto.request.MemberFormDto;
import com.student.iksu.dto.request.MyPageUpdateDto;     // ★ 추가됨
import com.student.iksu.dto.request.AccountUpdateDto;    // ★ 추가됨
import com.student.iksu.dto.request.PasswordChangeDto;   // ★ 추가됨
import com.student.iksu.entity.Member;
import com.student.iksu.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Long join(MemberFormDto dto) {
        validateDuplicateMember(dto);

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        List<String> majors = dto.getMajors();
        String m1 = (majors != null && majors.size() > 0) ? majors.get(0) : null;
        String m2 = (majors != null && majors.size() > 1) ? majors.get(1) : null;
        String m3 = (majors != null && majors.size() > 2) ? majors.get(2) : null;

        DegreeLevel degree = null;
        if (dto.getDegreeLevel() != null) {
            try {
                degree = DegreeLevel.valueOf(dto.getDegreeLevel());
            } catch (IllegalArgumentException e) {
                degree = DegreeLevel.OTHER;
            }
        }

        Member member = new Member(
                dto.getUsername(),
                dto.getEmail(),
                encodedPassword,
                dto.getName(),
                dto.getSchool(),
                degree,
                m1, m2, m3,
                dto.getBirthDate(),
                Role.USER,
                LocalDateTime.now()
        );

        memberRepository.save(member);
        return member.getId();
    }

    public Member findMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public void changeMemberRole(Long memberId, Role newRole) {
        Member member = findMemberById(memberId);
        member.setRole(newRole);
        memberRepository.save(member);
    }

    private void validateDuplicateMember(MemberFormDto dto) {
        if (memberRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
    }

    public boolean checkUsernameDuplicate(String username) {
        return memberRepository.existsByUsername(username);
    }

    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    public String findUsernameByNameAndEmail(String name, String email) {
        return memberRepository.findByNameAndEmail(name, email)
                .map(Member::getUsername)
                .orElseThrow(() -> new IllegalStateException("일치하는 회원 정보가 없습니다."));
    }

    public void updatePassword(String email, String newPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));
        member.setPassword(passwordEncoder.encode(newPassword));
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // ==========================================
    // 마이페이지 관련 로직 추가 영역
    // ==========================================

    // 1. 기본 정보 수정
    @Transactional
    public Member updateMemberInfo(String currentUsername, MyPageUpdateDto dto) {
        Member member = findMember(currentUsername);

        member.setName(dto.getName());
        member.setBirthDate(dto.getBirthDate());
        member.setSchool(dto.getSchool());
        member.setMajor1(dto.getMajor1());
        member.setMajor2(dto.getMajor2());
        member.setMajor3(dto.getMajor3());

        if (dto.getDegreeLevel() != null) {
            try {
                member.setDegreeLevel(DegreeLevel.valueOf(dto.getDegreeLevel()));
            } catch (IllegalArgumentException e) {
                member.setDegreeLevel(DegreeLevel.OTHER);
            }
        }
        return member;
    }

    // 2. 계정 정보 수정 (아이디, 이메일)
    @Transactional
    public Member updateAccountInfo(String currentUsername, AccountUpdateDto dto) {
        Member member = findMember(currentUsername);

        if (!member.getUsername().equals(dto.getUsername()) && checkUsernameDuplicate(dto.getUsername())) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
        if (!member.getEmail().equals(dto.getEmail()) && checkEmailDuplicate(dto.getEmail())) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        member.setUsername(dto.getUsername());
        member.setEmail(dto.getEmail());
        return member;
    }

    // 3. 비밀번호 변경 (현재 비밀번호 확인 포함)
    @Transactional
    public void changePasswordWithCurrent(String currentUsername, PasswordChangeDto dto) {
        Member member = findMember(currentUsername);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalStateException("현재 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    // 4. 회원 탈퇴
    @Transactional
    public void withdrawMember(String currentUsername, String password) {
        Member member = findMember(currentUsername);

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
    }
}