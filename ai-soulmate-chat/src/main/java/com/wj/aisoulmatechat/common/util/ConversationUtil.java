package com.wj.aisoulmatechat.common.util;

public class ConversationUtil {
    public static final String CONV_ID_PREFIX = "soulmate:memory:";

    /**
     * 生成AI伴侣对话唯一会话ID
     */
    public static String buildSoulmateConvId(Long userId, Long soulmateId) {
        if (userId == null || soulmateId == null) {
            throw new IllegalArgumentException("userId、soulmateId不能为空");
        }
        return CONV_ID_PREFIX + userId + ":" + soulmateId;
    }
}
