package com.wj.aisoulmatechat.vo;

import com.wj.aisoulmatechat.entity.ChatMemoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class ChatMemoryGroupVO {
    // 分组标题：今天 / 昨天 / 2026-06-16
    private String groupTitle;
    // 当天所有消息
    private List<ChatMemoryEntity> msgList;
}
