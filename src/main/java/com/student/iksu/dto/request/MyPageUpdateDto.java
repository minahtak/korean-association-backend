package com.student.iksu.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter @Setter
public class MyPageUpdateDto {
    private String name;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String degreeLevel;
    private String school;
    private String major1;
    private String major2;
    private String major3;
}
