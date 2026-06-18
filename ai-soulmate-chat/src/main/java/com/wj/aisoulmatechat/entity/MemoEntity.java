package com.wj.aisoulmatechat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("memo")
public class MemoEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String docId;

    private Long userId;

    private Long soulmateId;

    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
