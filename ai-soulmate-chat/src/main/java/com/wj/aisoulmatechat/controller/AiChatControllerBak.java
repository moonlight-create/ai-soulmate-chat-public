package com.wj.aisoulmatechat.controller;

import com.wj.aisoulmatechat.config.properties.BasePromptConfigProperties;
import com.wj.aisoulmatechat.dto.UserPromptDTO;
import com.wj.aisoulmatechat.security.LoginUser;
import com.wj.aisoulmatechat.service.SoulmateService;
import lombok.SneakyThrows;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.util.List;

//@Controller
//@RequestMapping("/chat")
public class AiChatControllerBak {

    private final ChatModel dashScopeChatModel;
    private final SoulmateService soulmateService;
    private final BasePromptConfigProperties basePromptConfigProperties;

    public AiChatControllerBak(@Qualifier("dashscopeChatModel") ChatModel dashscopeChatModel, SoulmateService soulmateService, BasePromptConfigProperties basePromptConfigProperties) {
        this.dashScopeChatModel = dashscopeChatModel;
        this.soulmateService = soulmateService;
        this.basePromptConfigProperties = basePromptConfigProperties;

    }

    @SneakyThrows
    @PostMapping("/ai-chat")
    public String aiChat(@RequestParam("prompt") String userPrompt) {
        return dashScopeChatModel.call(userPrompt);
    }

    @SneakyThrows
    @PostMapping(value = "/ai-chat-stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> aiChatStream(@RequestBody UserPromptDTO userPromptDto, Authentication auth) {
        LoginUser loginUser = (LoginUser) auth.getPrincipal();
        Long userId = loginUser.getUser().getId();
        Long soulmateId = userPromptDto.getSoulmateId();
        String userPrompt = userPromptDto.getUserPrompt();
        //拼接基础+动态人设
        String fullSysPrompt = soulmateService.getFullSysPrompt(userId, soulmateId, basePromptConfigProperties.getBase());
        Prompt prompt = new Prompt(List.of(
                                    new SystemMessage(fullSysPrompt),
                                    new UserMessage(userPrompt)
        ));
        return dashScopeChatModel.stream(prompt)
                .map(chatResp -> chatResp.getResult().getOutput().getText());
    }
}
