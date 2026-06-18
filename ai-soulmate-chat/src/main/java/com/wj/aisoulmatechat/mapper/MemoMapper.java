package com.wj.aisoulmatechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.aisoulmatechat.entity.MemoEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemoMapper extends BaseMapper<MemoEntity> {
    /**
     * 根据soulmateId删除该伴侣全部备忘录
     * @param soulmateId 伴侣ID
     * @return 删除影响行数
     */
    @Delete("delete from memo where soulmate_id = #{soulmateId}")
    int deleteBySoulmateId(@Param("soulmateId") Long soulmateId);
}
