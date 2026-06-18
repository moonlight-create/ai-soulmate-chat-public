package com.wj.aisoulmatechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.aisoulmatechat.entity.MemoEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemoMapper extends BaseMapper<MemoEntity> {
    // RAG召回后批量根据docId查询多条备忘录
    List<MemoEntity> selectBatchByDocIds(@Param("docIds") List<String> docIds);
}
