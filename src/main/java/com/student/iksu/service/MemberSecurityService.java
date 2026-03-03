package com.student.iksu.service;

import com.student.iksu.entity.Member;
import com.student.iksu.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberSecurityService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();

        // ★ [수정됨] .getValue() -> .name() 으로 변경
        // 이렇게 하면 "ROLE_" 같은 접두사 없이 "ADMIN", "USER" 글자 그대로 권한이 들어갑니다.
        // 그래야 컨트롤러의 hasAnyAuthority('ADMIN') 과 딱 맞아떨어집니다.
        authorities.add(new SimpleGrantedAuthority(member.getRole().name()));

        return new User(member.getUsername(), member.getPassword(), authorities);
    }
}