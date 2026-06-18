package com.wj.aisoulmatechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserPromptDTO {
    @NotBlank(message = "用户提问不能为空")
    private String userPrompt;
    @NotNull(message = "伴侣ID不能为空")
    private Long soulmateId;
}
