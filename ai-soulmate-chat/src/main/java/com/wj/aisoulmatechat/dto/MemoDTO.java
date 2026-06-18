package com.wj.aisoulmatechat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MemoDTO {
    private Long id;
    private String docId;
    private Long userId;
    @NotNull(message = "伴侣ID不能为空")
    private Long soulmateId;
    @NotBlank(message = "备忘录内容不能为空")
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
