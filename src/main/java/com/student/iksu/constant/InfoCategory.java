package com.student.iksu.constant;

import lombok.Getter;

@Getter
public enum InfoCategory {
    LIFE("주거/생활"),
    FOOD("맛집/음식"),
    TRAVEL("여행"),
    JOB("인턴/취업"),
    SCHOOL("학교별 정보"),
    FAQ("FAQ/꿀팁"),
    ADMIN("행정/비자");

    private final String description;

    InfoCategory(String description) {
        this.description = description;
    }
}