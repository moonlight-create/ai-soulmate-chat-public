package com.wj.aisoulmatechat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.aisoulmatechat.entity.MemoEntity;
import com.wj.aisoulmatechat.mapper.MemoMapper;
import com.wj.aisoulmatechat.security.LoginUser;
import com.wj.aisoulmatechat.service.MemoService;
import com.wj.aisoulmatechat.util.IkKeywordUtil;
import com.wj.aisoulmatechat.util.SecurityUserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
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
    public String addMemo(MemoEntity memo) {
//        LoginUser loginUser = getCurrentLoginUser();
//        Long userId = loginUser.getUser().getId();
        Long userId = SecurityUserUtil.getCurrentUserId();
        memo.setUserId(userId);
        Long soulmateId = memo.getSoulmateId();
        String content = memo.getContent();
        String convId = "soulmate:memory:" + userId + ":" + soulmateId;

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
        vectorStore.add(List.of(doc));

        memo.setDocId(docId);
        this.save(memo);

        return docId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByDocId(String docId) {
//        LoginUser loginUser = getCurrentLoginUser();
//        Long userId = loginUser.getUser().getId();
        Long userId = SecurityUserUtil.getCurrentUserId();
        vectorStore.delete(List.of(docId));
        return lambdaUpdate()
                .eq(MemoEntity::getDocId, docId)
                .eq(MemoEntity::getUserId, userId)
                .remove();
    }

    @Override
    public List<MemoEntity> listByUserSoulmateId(Long soulmateId) {
//        LoginUser loginUser = getCurrentLoginUser();
//        Long userId = loginUser.getUser().getId();
        Long userId = SecurityUserUtil.getCurrentUserId();
        return this.lambdaQuery().eq(MemoEntity::getSoulmateId, soulmateId)
                .eq(MemoEntity::getUserId, userId)
                .orderByDesc(MemoEntity::getCreateTime)
                .list();
    }

    //获取当前登录用户
//    private LoginUser getCurrentLoginUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || authentication.getPrincipal() instanceof String) {
//            throw new SecurityException("用户未登录，请重新登录");
//        }
//        return (LoginUser) authentication.getPrincipal();
//    }


}
