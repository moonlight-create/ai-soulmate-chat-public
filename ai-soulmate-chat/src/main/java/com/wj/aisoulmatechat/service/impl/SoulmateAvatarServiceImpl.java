package com.wj.aisoulmatechat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.mapper.SoulmateAvatarMapper;
import com.wj.aisoulmatechat.service.SoulmateAvatarService;
import com.wj.aisoulmatechat.vo.SoulmateAvatarVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SoulmateAvatarServiceImpl extends ServiceImpl<SoulmateAvatarMapper, SoulmateAvatarEntity> implements SoulmateAvatarService {
    private final SoulmateAvatarMapper avMapper;
    // 默认头像路径
    //public static final String DEFAULT_AVATAR = "https://picsum.photos/id/64/300/300";
    // 图片存储路径
    private static final String DIR = System.getProperty("user.dir") + "/avatar/soulmate/file";

    private void initDir() {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 上传文件，返回前端访问路径
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        initDir();
        // 生成唯一文件名
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File dest = new File(DIR, fileName);
        file.transferTo(dest);
        // 返回可访问的相对路径
        return "/avatar/soulmate/file/" + fileName;
    }

    @Override
    public String getSoumateAvatar(Long soulmateId) {
        SoulmateAvatarEntity one = getOne(new LambdaQueryWrapper<SoulmateAvatarEntity>()
                .eq(SoulmateAvatarEntity::getSoulmateId, soulmateId)
                .last("limit 1"));
        return one == null ? null : one.getAvatarUrl();
    }

    @Override
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
    public SoulmateAvatarVO getBySoulmateId(Long sid) {
        return avMapper.getOneVoBySoulmateId(sid);
    }
}
