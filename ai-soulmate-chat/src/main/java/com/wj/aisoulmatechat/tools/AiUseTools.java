package com.wj.aisoulmatechat.tools;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wj.aisoulmatechat.constants.Constant;
import com.wj.aisoulmatechat.service.AiToolsService;
import com.wj.aisoulmatechat.util.IkKeywordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiUseTools {
    //向量数据库
    private final VectorStore vectorStore;
    private final AiToolsService aiToolsService;

    /**
     * AI伴侣根据用户的提示词，如果Ai伴侣判断有需要作为伴侣特别记忆的点或者用户强调记忆的点则调用此方法入库向量数据库
     * @param content
     * @return
     */
    @Tool(description = Constant.Tools.ARCHIVE_IMPORTANT_SCENE)
    public boolean archiveImportantScene(
            @ToolParam(description =Constant.ToolParams.IMPORTANT_SCENE) String content,
            ToolContext context){
        Long userId = (Long)context.getContext().get("userId");
        Long soulmateId = (Long)context.getContext().get("soulmateId");
        String convId = (String) context.getContext().get("convId");

        //使用分词器将用户信息分词入库metadata，方便后续筛选召回信息
        String keyword = IkKeywordUtil.extractKeywords(content);

        Map<String,Object> meta = Map.of(
                "userId", userId,
                "soulmateId", soulmateId,
                "conversationId", String.valueOf(convId),
                "doc_type", "important_scene",
                "keyword", keyword
        );

        Document doc = Document.builder()
                .text(content)
                .metadata(meta)
                .build();
        vectorStore.add(List.of(doc));

        return true;
    }


    /**
     *  获取天气
     * @param latitude
     * @param longitude
     * @return
     */
    @Tool(description = Constant.Tools.GET_WEATHER)
    public String getWeather(
            @ToolParam(description = "纬度，如北京 39.9042") double latitude,
            @ToolParam(description = "经度，如北京 116.4074") double longitude
    ){

        return aiToolsService.getWeatherByLatLon(latitude, longitude);
    }

    /**
     * 获取当前时间
     * @return
     */
    @Tool(description = Constant.Tools.GET_TIME)
    public String getTime(){
        return new DateUtil().now();
    }

}
