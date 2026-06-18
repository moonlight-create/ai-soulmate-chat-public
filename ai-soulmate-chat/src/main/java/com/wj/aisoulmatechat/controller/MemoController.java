package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.entity.MemoEntity;
import com.wj.aisoulmatechat.service.MemoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memo")
public class MemoController {
    @Resource
    private MemoService memoService;

    /**
     * 新增备忘录（向量数据库+关系数据库）
     */
    @PostMapping("/add")
    public String add(@RequestBody MemoEntity memo) {
        return memoService.addMemo(memo);
    }

    /**
     * 删除备忘录（双删 向量数据库+关系数据库）
     */
    @PostMapping("/delete/{docId}")
    public Boolean delete(@PathVariable("docId") String docId) {
        return memoService.deleteByDocId(docId);
    }

    /**
     * 查询虚拟伴侣下的全部备忘录(查数据库)
     */
    @GetMapping("/list/{soulmateId}")
    public List<MemoEntity> list(@PathVariable("soulmateId") Long soulmateId) {
        return memoService.listByUserSoulmateId(soulmateId);
    }

}
