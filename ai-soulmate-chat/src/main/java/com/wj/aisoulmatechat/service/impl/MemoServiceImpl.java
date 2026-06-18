package com.wj.aisoulmatechat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.aisoulmatechat.common.util.ConversationUtil;
import com.wj.aisoulmatechat.dto.MemoDTO;
import com.wj.aisoulmatechat.entity.MemoEntity;
import com.wj.aisoulmatechat.mapper.MemoMapper;
import com.wj.aisoulmatechat.service.MemoService;
import com.wj.aisoulmatechat.util.IkKeywordUtil;
import com.wj.aisoulmatechat.util.SecurityUserUtil;
import com.wj.aisoulmatechat.vo.MemoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemoServiceImpl extends ServiceImpl<MemoMapper,MemoEntity> implements MemoService {
    private final VectorStore vectorStore;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addMemo(MemoDTO memo) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        memo.setUserId(userId);
        Long soulmateId = memo.getSoulmateId();
        String content = memo.getContent();
        String convId = ConversationUtil.buildSoulmateConvId(userId, soulmateId);

        String keyword = IkKeywordUtil.extractKeywords(content);

        String docId = UUID.randomUUID().toString();

        Map<String,Object> meta = Map.of(
                "userId", userId,
                "soulmateId", soulmateId,
                "conversationId", convId,
                "doc_type", "memo",
                "keyword", keyword
        );

        Document doc = Document.builder()
                .id(docId)
                .text(content)
                .metadata(meta)
                .build();

        memo.setDocId(docId);

        MemoEntity memoEntity = new MemoEntity();
        BeanUtil.copyProperties(memo,memoEntity, CopyOptions.create().ignoreNullValue());

        this.save(memoEntity);

        try {
            vectorStore.add(List.of(doc));
        } catch (Exception e) {
            throw new RuntimeException("向量库写入失败，备忘录创建回滚", e);
        }

        return docId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByDocId(String docId) {
        Long userId = SecurityUserUtil.getCurrentUserId();

        // 查询并校验
        MemoEntity memoEntity = this.lambdaQuery()
                .eq(MemoEntity::getDocId, docId)
                .eq(MemoEntity::getUserId, userId)
                .one();
        if (memoEntity == null) {
            return false;
        }

        // 先删除数据库
        boolean dbDeleted = this.removeById(memoEntity.getId());
        if (!dbDeleted) {
            throw new RuntimeException("数据库删除失败");
        }

        // 异步删除向量库
        try {
            vectorStore.delete(List.of(docId));
        } catch (Exception e) {
            // 记录失败日志
            log.error("向量库删除失败，docId: "+docId, e);
        }

        return true;
    }

    @Override
    public List<MemoVO> listByUserSoulmateId(Long soulmateId) {
        Long userId = SecurityUserUtil.getCurrentUserId();

        List<MemoEntity> entityList = this.lambdaQuery()
                .eq(MemoEntity::getSoulmateId, soulmateId)
                .eq(MemoEntity::getUserId, userId)
                .orderByDesc(MemoEntity::getCreateTime)
                .list();

        return entityList.stream().map(entity -> {
            MemoVO vo = new MemoVO();
            BeanUtil.copyProperties(entity,vo, CopyOptions.create().ignoreNullValue());
            return vo;
        }).toList();
    }

}
