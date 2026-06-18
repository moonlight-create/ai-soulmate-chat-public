package com.wj.aisoulmatechat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.mapper.SoulmateAvatarMapper;
import com.wj.aisoulmatechat.mapper.UserSoulmateMapper;
import com.wj.aisoulmatechat.service.SoulmateAvatarService;
import com.wj.aisoulmatechat.vo.SoulmateAvatarVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SoulmateAvatarServiceImpl extends ServiceImpl<SoulmateAvatarMapper, SoulmateAvatarEntity> implements SoulmateAvatarService {
    private final UserSoulmateMapper smMapper;
    private final SoulmateAvatarMapper avMapper;

    @Override
    public String getSoumateAvatar(Long soulmateId) {
        SoulmateAvatarEntity one = getOne(new LambdaQueryWrapper<SoulmateAvatarEntity>()
                .eq(SoulmateAvatarEntity::getSoulmateId, soulmateId)
                .last("limit 1"));
        return one == null ? null : one.getAvatarUrl();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateAvatar(Long soulmateId, String avatarUrl) {
        SoulmateAvatarEntity one = getOne(new LambdaQueryWrapper<SoulmateAvatarEntity>()
                .eq(SoulmateAvatarEntity::getSoulmateId, soulmateId)
                .last("limit 1"));

        if (one == null) {
            one = new SoulmateAvatarEntity();
            one.setSoulmateId(soulmateId);
            one.setAvatarUrl(avatarUrl);
            save(one);
        } else {
            one.setAvatarUrl(avatarUrl);
            updateById(one);
        }
    }

    @Override
    public SoulmateAvatarVo getBySoulmateId(Long sid) {
        return avMapper.getOneVoBySoulmateId(sid);
    }
}
