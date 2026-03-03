package com.student.iksu.constant;

import lombok.Getter;

@Getter
public enum DegreeLevel {
    MECHINA("MECHINA", "메키나"),
    BACHELOR("BACHELOR", "학사"),
    MASTER("MASTER", "석사"),
    DOCTORATE("DOCTORATE", "박사"),
    EXCHANGE("EXCHANGE", "교환학생"),
    LANGUAGE("LANGUAGE", "어학연수 (Ulpan)"),
    OTHER("OTHER", "기타");

    private final String value;
    private final String description;

    DegreeLevel(String value, String description) {
        this.value = value;
        this.description = description;
    }
}