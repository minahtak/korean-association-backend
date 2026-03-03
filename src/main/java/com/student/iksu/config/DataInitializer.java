package com.student.iksu.config;

import com.student.iksu.constant.DegreeLevel;
import com.student.iksu.constant.Role;
import com.student.iksu.entity.Executive;
import com.student.iksu.entity.Member;
import com.student.iksu.entity.Notice;
import com.student.iksu.repository.ExecutiveRepository;
import com.student.iksu.repository.MemberRepository;
import com.student.iksu.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ExecutiveRepository executiveRepository;
    private final PasswordEncoder passwordEncoder;
    private final NoticeRepository noticeRepository;

    @Override
    public void run(String... args) throws Exception {

        // ★ [수정됨] 이 코드는 반드시 run 메서드 안에 있어야 합니다!
        // 데이터가 이미 1명이라도 있으면 초기화 로직을 건너뜁니다. (데이터 보존)
        if (memberRepository.count() > 0) {
            return;
        }

        // ==========================================
        // 1. 관리자 (joinDate 추가됨)
        // ==========================================
        Member admin = new Member(
                "admin", "admin@iksu.org", passwordEncoder.encode("admin1234"),
                "관리자", "히브리대", DegreeLevel.MASTER, "International Relations", null, null,
                LocalDate.of(1998, 1, 1), Role.ADMIN, LocalDateTime.now().minusYears(1)
        );
        memberRepository.save(admin);

        // ==========================================
        // 2. 임원진 - 회장
        // ==========================================
        Member prez = new Member(
                "president", "prez@iksu.org", passwordEncoder.encode("1234"),
                "김민수", "히브리대", DegreeLevel.BACHELOR, "Computer Science", "Economics", null,
                LocalDate.of(1999, 3, 15), Role.STAFF, LocalDateTime.now().minusMonths(6)
        );
        memberRepository.save(prez);
        executiveRepository.save(new Executive(prez, "Chairman (회장)", prez.getName(), prez.getSchool(), "비전 수립 및 총괄", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&h=400&fit=crop"));

        // 부회장
        Member vice = new Member(
                "vice_prez", "vice@iksu.org", passwordEncoder.encode("1234"),
                "이지원", "텔아비브대", DegreeLevel.BACHELOR, "Psychology", null, null,
                LocalDate.of(2000, 7, 20), Role.STAFF, LocalDateTime.now().minusMonths(5)
        );
        memberRepository.save(vice);
        executiveRepository.save(new Executive(vice, "Vice Chair (부회장)", vice.getName(), vice.getSchool(), "학업 교류 지원", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&h=400&fit=crop"));

        // 사무총장
        Member sec = new Member(
                "secretary", "sec@iksu.org", passwordEncoder.encode("1234"),
                "박준영", "바일란대", DegreeLevel.MASTER, "Law", null, null,
                LocalDate.of(2001, 11, 5), Role.STAFF, LocalDateTime.now().minusMonths(4)
        );
        memberRepository.save(sec);
        executiveRepository.save(new Executive(sec, "General Manager (사무국장)", sec.getName(), sec.getSchool(), "행정 및 재정", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&h=400&fit=crop"));

        // ==========================================
        // 3. 일반 회원
        // ==========================================
        memberRepository.save(new Member("tech_guy", "tech@test.com", passwordEncoder.encode("1234"), "정우성", "Technion", DegreeLevel.DOCTORATE, "Physics", null, null, LocalDate.of(1997, 5, 10), Role.USER, LocalDateTime.now().minusDays(10)));
        memberRepository.save(new Member("art_lover", "art@test.com", passwordEncoder.encode("1234"), "한소희", "Bezalel Academy", DegreeLevel.BACHELOR, "Fine Arts", null, null, LocalDate.of(2002, 12, 25), Role.USER, LocalDateTime.now().minusDays(5)));
        memberRepository.save(new Member("mechina_st", "mechina@test.com", passwordEncoder.encode("1234"), "박지민", "히브리대 (Mechina)", DegreeLevel.MECHINA, "General Studies", null, null, LocalDate.of(2006, 2, 14), Role.USER, LocalDateTime.now().minusHours(2)));

        // ==========================================
        // 4. 공지사항
        // ==========================================
        noticeRepository.save(new Notice("🔥 개강총회 안내", "사커필드에서 만나요!", "EVENT", "ALL", "학생회장", LocalDateTime.now(), true, 0));
        noticeRepository.save(new Notice("[필독] 비자 연장 공지", "내무부 파업 관련...", "OFFICIAL", "ALL", "사무국", LocalDateTime.now().minusDays(3), false, 0));
    }
}