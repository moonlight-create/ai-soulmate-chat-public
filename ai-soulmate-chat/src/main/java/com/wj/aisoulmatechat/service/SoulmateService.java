package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.entity.SoulmateAvatar;
import com.wj.aisoulmatechat.entity.UserSoulmate;
import com.wj.aisoulmatechat.vo.SoulmateVo;

import java.util.List;

public interface SoulmateService {
    List<SoulmateVo> getList(Long userId);
    void saveSoulmate(UserSoulmate soul, String avatarUrl);
    SoulmateVo getById(Long sid);
    void deleteById(Long soulmateId);
    void updateById(UserSoulmate userSoulmate, SoulmateAvatar soulmateAvatar);
    String getFullSysPrompt(Long userId, Long soulmateId, String basePrompt);
    void checkAndBuildHistorySummary(String convId);
}
