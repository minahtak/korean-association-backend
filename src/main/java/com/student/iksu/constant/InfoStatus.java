package com.student.iksu.constant;

import lombok.Getter;

@Getter
public enum InfoStatus {
    PENDING("승인 대기"),
    APPROVED("승인됨"),
    REJECTED("반려됨");

    private final String description;

    InfoStatus(String description) {
        this.description = description;
    }
}