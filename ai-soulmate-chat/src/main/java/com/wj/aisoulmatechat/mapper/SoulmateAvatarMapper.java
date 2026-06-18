package com.wj.aisoulmatechat.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.vo.SoulmateAvatarVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SoulmateAvatarMapper extends BaseMapper<SoulmateAvatarEntity> {
    //根据伴侣id删除头像记录
    default int deleteBySoulmateId(Long soulmateId){
        LambdaQueryWrapper<SoulmateAvatarEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SoulmateAvatarEntity::getSoulmateId, soulmateId);
        return delete(wrapper);
    }

    //根据伴侣ID获取头像信息（伴侣头像唯一）
    @Select("SELECT id as avatar_id,soulmate_id,avatar_url FROM soulmate_avatar WHERE soulmate_id = #{sid}")
    SoulmateAvatarVO getOneVoBySoulmateId(@Param("sid") Long sid);
}
