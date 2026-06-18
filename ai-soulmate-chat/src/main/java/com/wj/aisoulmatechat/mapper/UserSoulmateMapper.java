package com.wj.aisoulmatechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.aisoulmatechat.entity.UserSoulmateEntity;
import com.wj.aisoulmatechat.vo.SoulmateVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserSoulmateMapper extends BaseMapper<UserSoulmateEntity> {
    @Select("SELECT s.id soulmate_id,s.gf_name,s.character_tag,s.detail_prompt,a.avatar_url " +
            "FROM user_soulmate s JOIN soulmate_avatar a ON s.id=a.soulmate_id WHERE s.user_id=#{uid}")
    List<SoulmateVO> listByUid(@Param("uid") Long uid);

    @Select("SELECT s.id soulmate_id,s.gf_name,s.character_tag,s.detail_prompt,s.age,s.sex,s.birth,s.hobby,a.avatar_url " +
            "FROM user_soulmate s LEFT JOIN soulmate_avatar a ON s.id = a.soulmate_id WHERE s.id = #{sid}")
    SoulmateVO getOneVoById(@Param("sid") Long sid);

    /**
     * 根据 userId + soulmateId 查询一条人设
     */
    @Select("select gf_name,sex,age,birth,hobby,character_tag,detail_prompt from user_soulmate where id=#{soulmateId} and user_id=#{userId}")
    SoulmateVO getPersonalityByUidAndSoulmateId(@Param("userId") Long userId, @Param("soulmateId") Long soulmateId);
}
