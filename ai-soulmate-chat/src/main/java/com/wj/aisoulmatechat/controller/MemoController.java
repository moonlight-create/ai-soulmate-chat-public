package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.common.result.Result;
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
    public Result<String> add(@RequestBody MemoEntity memo) {
        String res = memoService.addMemo(memo);
        return Result.ok(res);
    }

    /**
     * 删除备忘录（双删 向量数据库+关系数据库）
     */
    @PostMapping("/delete/{docId}")
    public Result<Boolean> delete(@PathVariable("docId") String docId) {
        Boolean flag = memoService.deleteByDocId(docId);
        return Result.ok(flag);
    }

    /**
     * 查询虚拟伴侣下的全部备忘录(查数据库)
     */
    @GetMapping("/list/{soulmateId}")
    public Result<List<MemoEntity>> list(@PathVariable("soulmateId") Long soulmateId) {
        List<MemoEntity> list = memoService.listByUserSoulmateId(soulmateId);
        return Result.ok(list);
    }

}
