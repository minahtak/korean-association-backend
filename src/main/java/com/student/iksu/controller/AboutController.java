package com.student.iksu.controller;

import com.student.iksu.entity.Executive;
import com.student.iksu.service.ExecutiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/about")
@RequiredArgsConstructor
public class AboutController {

    private final ExecutiveService executiveService;

    @GetMapping
    public String about(Model model) {
        // 모든 임원 멤버 가져오기
        List<Executive> executives = executiveService.getAllExecutives();
        model.addAttribute("executives", executives);

        return "about/about";
    }
}