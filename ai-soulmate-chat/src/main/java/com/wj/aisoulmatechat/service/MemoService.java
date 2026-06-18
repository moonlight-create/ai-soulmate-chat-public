package com.wj.aisoulmatechat.service;

import com.wj.aisoulmatechat.dto.MemoDTO;
import com.wj.aisoulmatechat.entity.MemoEntity;
import com.wj.aisoulmatechat.vo.MemoVO;

import java.util.List;

public interface MemoService {
    String addMemo(MemoDTO memo);

    Boolean deleteByDocId(String docId);

    List<MemoVO> listByUserSoulmateId(Long soulmateId);

}
