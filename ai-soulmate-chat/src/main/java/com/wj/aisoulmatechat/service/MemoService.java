package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.entity.MemoEntity;

import java.util.List;

public interface MemoService {
    String addMemo(MemoEntity memo);

    Boolean deleteByDocId(String docId);

    List<MemoEntity> listByUserSoulmateId(Long soulmateId);

}
