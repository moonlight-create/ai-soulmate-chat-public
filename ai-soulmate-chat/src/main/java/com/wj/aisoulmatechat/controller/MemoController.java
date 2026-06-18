package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.common.result.Result;
import com.wj.aisoulmatechat.dto.MemoDTO;
import com.wj.aisoulmatechat.service.MemoService;
import com.wj.aisoulmatechat.vo.MemoVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memo")
@Validated
public class MemoController {
    @Resource
    private MemoService memoService;

    /**
     * 新增备忘录（向量数据库+关系数据库）
     */
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody MemoDTO memo) {
        String res = memoService.addMemo(memo);
        return Result.ok(res);
    }

    /**
     * 删除备忘录（双删 向量数据库+关系数据库）
     */
    @PostMapping("/delete/{docId}")
    public Result<Boolean> delete(
            @NotBlank(message = "文档ID不能为空") @PathVariable("docId") String docId
    ) {
        Boolean flag = memoService.deleteByDocId(docId);
        return Result.ok(flag);
    }

    /**
     * 查询虚拟伴侣下的全部备忘录(查数据库)
     */
    @GetMapping("/list/{soulmateId}")
    public Result<List<MemoVO>> list(
            @NotNull(message = "伴侣ID不能为空") @PathVariable("soulmateId") Long soulmateId
    ) {
        List<MemoVO> list = memoService.listByUserSoulmateId(soulmateId);
        return Result.ok(list);
    }

}
