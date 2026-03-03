package com.student.iksu.config;

import com.student.iksu.entity.Member;
import com.student.iksu.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice // ★ "모든 컨트롤러는 내 말을 들어라!" 라는 뜻
@RequiredArgsConstructor
public class GlobalModelConfig {

    private final MemberRepository memberRepository;

    // 모든 페이지가 열리기 직전에 무조건 실행되는 함수
    @ModelAttribute
    public void addAttributes(Model model, @AuthenticationPrincipal User user) {

        // 1. 로그인을 했다면?
        if (user != null) {
            // 2. DB에서 이 사람의 진짜 이름(홍길동)을 찾아서
            Member member = memberRepository.findByUsername(user.getUsername()).orElse(null);

            if (member != null) {
                // 3. 'userName'이라는 이름표를 붙여준다!
                model.addAttribute("userName", member.getName());
            }
        }
    }
}