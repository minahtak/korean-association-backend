package com.student.iksu.constant;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN", "최종 관리자"),
    STAFF("ROLE_STAFF", "학생회 임원"),
    WRITER("ROLE_WRITER", "글쓰기 권한 회원"),
    USER("ROLE_USER", "일반 회원");

    private final String value;
    private final String description;

    Role(String value, String description) {
        this.value = value;
        this.description = description;
    }
}