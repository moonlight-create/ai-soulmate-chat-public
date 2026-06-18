package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.dto.UserSoulmateDTO;
import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.entity.UserSoulmateEntity;
import com.wj.aisoulmatechat.vo.SoulmateVO;

import java.util.List;

public interface SoulmateService {
    List<SoulmateVO> getList(Long userId);
    void saveSoulmate(UserSoulmateDTO soul, String avatarUrl);
    SoulmateVO getById(Long sid);
    boolean deleteById(Long soulmateId);
    void updateById(UserSoulmateDTO userSoulmateDTO, SoulmateAvatarEntity soulmateAvatarEntity);
    String getFullSysPrompt(Long userId, Long soulmateId, String basePrompt);
    void checkAndBuildHistorySummary(String convId);
}
