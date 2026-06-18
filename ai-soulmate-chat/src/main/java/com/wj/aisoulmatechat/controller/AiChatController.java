package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.common.result.Result;
import com.wj.aisoulmatechat.dto.UserPromptDTO;
import com.wj.aisoulmatechat.service.AiChatService;
import com.wj.aisoulmatechat.util.SecurityUserUtil;
import com.wj.aisoulmatechat.vo.ChatMemoryGroupVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Validated
public class AiChatController {
    private final AiChatService aiChatService;

    @PostMapping("/ai-chat")
    public Result<String> aiChat(
            @NotBlank(message = "prompt提问内容不能为空") @RequestParam("prompt") String userPrompt
    ) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        String res = aiChatService.simpleChat(userId, userPrompt);
        return Result.ok(res);
    }

    @PostMapping(value = "/ai-chat-stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> aiChatStream(@Valid @RequestBody UserPromptDTO userPromptDto) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        Long soulmateId = userPromptDto.getSoulmateId();
        String userPrompt = userPromptDto.getUserPrompt();
        return aiChatService.streamChat(userId, soulmateId, userPrompt);
    }

    @GetMapping("/get-first-msg")
    public Result<String> getFirstMsg(
            @NotNull(message = "soulmateId不能为空") @RequestParam("soulmateId") Long soulmateId
    ) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        String msg = aiChatService.getFirstOpeningMsg(userId, soulmateId);
        return Result.ok(msg);
    }

    @GetMapping("/memory/group_by_day")
    public Result<List<ChatMemoryGroupVO>> getChatGroupByDay(
            @NotNull(message = "soulmateId不能为空") @RequestParam("soulmateId") Long soulmateId
    ) {
        Long userId = SecurityUserUtil.getCurrentUserId();
        List<ChatMemoryGroupVO> list = aiChatService.getMemoryGroupByDay(userId, soulmateId);
        return Result.ok(list);
    }
}
