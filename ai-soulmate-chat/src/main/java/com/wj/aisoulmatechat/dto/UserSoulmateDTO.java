package com.wj.aisoulmatechat.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class UserSoulmateDTO {
    private Long id;
    private Long userId;
    private String gfName;
    private String characterTag;
    private String detailPrompt;
    private String sex;
    private Integer age;
    private String birth;
    private String hobby;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final String DEFAULT_SEX = "女";
    public static final Integer DEFAULT_AGE = 18;
    public static final String DEFAULT_HOBBY = "无";

    public static final DateTimeFormatter BIRTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void setDefaultValue() {
        if (!StringUtils.hasText(this.sex)) {
            this.sex = DEFAULT_SEX;
        }
        if (this.age == null) {
            this.age = DEFAULT_AGE;
        }
        if (!StringUtils.hasText(this.birth)) {
            this.birth = BIRTH_FORMATTER.format(LocalDate.now());
        }
        if (!StringUtils.hasText(this.hobby)) {
            this.hobby = DEFAULT_HOBBY;
        }

    }

}
