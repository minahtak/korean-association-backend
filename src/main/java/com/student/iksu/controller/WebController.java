package com.student.iksu.controller;

import com.student.iksu.entity.Info;
import com.student.iksu.service.InfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final InfoService infoService;

    // 1. 특정 게시글 상세 페이지를 카톡 등에 공유했을 때 (우선순위 높음)
    @GetMapping("/info/{id}")
    public String infoDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        try {
            Info info = infoService.getInfo(id);

            // ① 동적 제목 세팅
            model.addAttribute("ogTitle", info.getTitle() + " | KSAI 생활 가이드");

            // ② 동적 설명 세팅 (HTML 태그 제거 후 80자 제한)
            String plainText = info.getContent().replaceAll("<[^>]*>", "");
            if (plainText.length() > 80) plainText = plainText.substring(0, 80) + "...";
            model.addAttribute("ogDescription", plainText);

            // ③ 본문에서 첫 번째 이미지 URL 추출 로직 (없으면 기본 로고)
            String ogImage = "https://minahtak.com/logo3.png"; // 실제 도메인으로 변경하기!!!!
            Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
            Matcher matcher = pattern.matcher(info.getContent());
            if (matcher.find()) {
                ogImage = matcher.group(1);
            }
            model.addAttribute("ogImage", ogImage);
            model.addAttribute("ogUrl", request.getRequestURL().toString());

        } catch (Exception e) {
            // 게시글이 없거나 에러 시 기본 정보 반환
            setDefaultOg(model, request);
        }

        // forward:/index.html 이 아니라 그냥 "index"를 반환합니다. (타임리프 적용)
        return "index";
    }

    // 2. 그 외 모든 리액트 라우터 경로 (기존 SPA 지원 로직 유지)
    @GetMapping(value = {"/", "/**/{path:[^\\.]*}"})
    public String forwardAll(Model model, HttpServletRequest request) {
        // 메인 화면이나 다른 화면들은 기본 학생회 썸네일을 띄워줍니다.
        setDefaultOg(model, request);
        return "index";
    }

    // 💡 공통 기본 OG 태그 설정 메서드
    private void setDefaultOg(Model model, HttpServletRequest request) {
        model.addAttribute("ogTitle", "KSAI | 이스라엘 한인 학생회");
        model.addAttribute("ogDescription", "이스라엘 유학 정보, 생활 가이드 및 한인 학생회 소식을 전합니다.");
        model.addAttribute("ogImage", "https://minahtak.com/logo3.png"); // 실제 도메인으로 변경하세요!
        model.addAttribute("ogUrl", request.getRequestURL().toString());
    }
}