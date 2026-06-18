package com.wj.aisoulmatechat.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MemoVO {
    private Long id;
    private String docId;
    private Long userId;
    private Long soulmateId;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
