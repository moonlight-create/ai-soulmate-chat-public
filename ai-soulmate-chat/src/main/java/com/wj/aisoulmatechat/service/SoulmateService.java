package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.entity.UserSoulmateEntity;
import com.wj.aisoulmatechat.vo.SoulmateVo;

import java.util.List;

public interface SoulmateService {
    List<SoulmateVo> getList(Long userId);
    void saveSoulmate(UserSoulmateEntity soul, String avatarUrl);
    SoulmateVo getById(Long sid);
    void deleteById(Long soulmateId);
    void updateById(UserSoulmateEntity userSoulmateEntity, SoulmateAvatarEntity soulmateAvatarEntity);
    String getFullSysPrompt(Long userId, Long soulmateId, String basePrompt);
    void checkAndBuildHistorySummary(String convId);
}
