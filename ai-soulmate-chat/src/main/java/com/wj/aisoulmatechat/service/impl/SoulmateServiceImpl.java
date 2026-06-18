package com.wj.aisoulmatechat.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.aisoulmatechat.config.properties.BasePromptConfigProperties;
import com.wj.aisoulmatechat.config.properties.MyChatMemoryConfigProperties;
import com.wj.aisoulmatechat.entity.SoulmateAvatarEntity;
import com.wj.aisoulmatechat.entity.UserSoulmateEntity;
import com.wj.aisoulmatechat.mapper.SoulmateAvatarMapper;
import com.wj.aisoulmatechat.mapper.UserSoulmateMapper;
import com.wj.aisoulmatechat.service.SoulmateService;
import com.wj.aisoulmatechat.util.AgeCulUtil;
import com.wj.aisoulmatechat.util.IkKeywordUtil;
import com.wj.aisoulmatechat.util.RedisCacheUtil;
import com.wj.aisoulmatechat.vo.SoulmateVo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SoulmateServiceImpl extends ServiceImpl<UserSoulmateMapper, UserSoulmateEntity> implements SoulmateService {
    private static final Logger log = LoggerFactory.getLogger(SoulmateServiceImpl.class);

    private final UserSoulmateMapper smMapper;
    private final SoulmateAvatarMapper avMapper;
    private final MyChatMemoryConfigProperties myChatMemoryConfigProperties;
    private final BasePromptConfigProperties basePromptConfigProperties;
    private final VectorStore vectorStore;

    @Resource
    @Qualifier("customChatMemoryRepository")
    private ChatMemoryRepository chatMemoryRepository;

    @Resource
    @Qualifier("chatSplitter")
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    @Qualifier("dashscopeChatModel")
    private ChatModel dashScopeChatModel;

    private static final ScheduledExecutorService DELAY_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    // soulmate:user:{userId}:soulmate:{soulmateId}
    private static final String REDIS_KEY_PREFIX = "soulmate:system:user:";
    private static final int CACHE_EXPIRE_DAY = 1;
    private static final int SLICE_LEN = 750;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    public SoulmateServiceImpl(UserSoulmateMapper smMapper, SoulmateAvatarMapper avMapper, MyChatMemoryConfigProperties myChatMemoryConfigProperties, VectorStore vectorStore,BasePromptConfigProperties basePromptConfigProperties) {
        this.smMapper = smMapper;
        this.avMapper = avMapper;
        this.myChatMemoryConfigProperties = myChatMemoryConfigProperties;
        this.basePromptConfigProperties = basePromptConfigProperties;
        this.vectorStore = vectorStore;
    }

    @Override
    public List<SoulmateVo> getList(Long userId) {
        return smMapper.listByUid(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSoulmate(UserSoulmateEntity soul, String avatarUrl) {
        save(soul);
        SoulmateAvatarEntity av=new SoulmateAvatarEntity();
        av.setSoulmateId(soul.getId());
        av.setAvatarUrl(avatarUrl);
        avMapper.insert(av);
    }

    @Override
    public SoulmateVo getById(Long sid) {
        return smMapper.getOneVoById(sid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long soulmateId) {
        //1、删除头像附表
        avMapper.deleteBySoulmateId(soulmateId);
        //2、删除主表伴侣
        smMapper.deleteById(soulmateId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateById(UserSoulmateEntity userSoulmateEntity, SoulmateAvatarEntity soulmateAvatarEntity) {
        //1、删除缓存数据
        this.clearCache(userSoulmateEntity.getUserId(), userSoulmateEntity.getId());
        //2、更新主表 user_soulmate
        smMapper.updateById(userSoulmateEntity);
        //3、更新头像附表 soulmate_avatar
        avMapper.updateById(soulmateAvatarEntity);
        //4、延时再次删除
        DELAY_EXECUTOR.schedule(
                () -> clearCache(userSoulmateEntity.getUserId(), userSoulmateEntity.getId()),
                3, TimeUnit.SECONDS
        );
    }

    /**
     * 拼接最终完整System提示词
     * @param userId 当前登录用户
     * @param soulmateId 伴侣id(user_soulmate.id)
     * @param basePrompt 项目基础通用system前缀
     * @return 拼接好的完整系统prompt
     */
    public String getFullSysPrompt(Long userId, Long soulmateId, String basePrompt) {
        String redisKey = redisCacheUtil.buildSystemPromptKey(userId, soulmateId);
        String extraPrompt = redisCacheUtil.get(redisKey);

        //缓存为空，查库
        if (extraPrompt == null) {
            SoulmateVo entity = smMapper.getPersonalityByUidAndSoulmateId(userId, soulmateId);
            String birth = Optional.ofNullable(entity.getBirth()).orElse("未设置");
            int age = 18;
            if(!"未设置".equals(birth)){
                age = AgeCulUtil.getAge(birth);
            }
            if (entity != null) {
                extraPrompt = String.format("""
                        【你的身份信息】
                        姓名：%s
                        性别：%s
                        年龄：%s
                        生日：%s
                        兴趣爱好：%s
                        【性格标签】%s
                        【详细人设规则】%s
                    """,
                        entity.getGfName(),
                        entity.getSex(),
                        age + "岁",
                        birth,
                        entity.getHobby().isBlank() ? "无" : entity.getHobby(),
                        entity.getCharacterTag().isBlank() ? "无" : entity.getCharacterTag(),
                        entity.getDetailPrompt().isBlank() ? "无" : entity.getDetailPrompt()
                );
//                redisCacheUtil.set(redisKey, basePrompt + "\n" + extraPrompt, CACHE_EXPIRE_DAY);
                redisCacheUtil.set(redisKey, extraPrompt, CACHE_EXPIRE_DAY);
            } else {
                extraPrompt = "";
            }
        }
        //动态人设
        return basePrompt + "\n" + extraPrompt;
    }

    /**
     * 修改人设后，清除对应缓存
     */
    public void clearCache(Long userId, Long soulmateId) {
        String redisKey = redisCacheUtil.buildSystemPromptKey(userId, soulmateId);
        redisCacheUtil.delete(redisKey);
    }

    /**
     * 创建伴侣设定系统提示词key
     * @param userId
     * @param soulmateId
     * @return
     */
    private String buildKey(Long userId, Long soulmateId) {
        return REDIS_KEY_PREFIX + userId + ":soulmate:" + soulmateId;
    }


    /**
     * 每次对话调用前执行计数+摘要判断
     * @param convId
     */
    @Override
    public void checkAndBuildHistorySummary(String convId) {
        String counterKey = "chat:counter:" + convId;
        // 用户每次提问+1
        Long count = redisCacheUtil.increment(counterKey);
        // 提问多少次触发摘要
        int triggerThreshold = myChatMemoryConfigProperties.getMaxBatchSaveMessage();
        if (count < triggerThreshold) {
            return;
        }
        redisCacheUtil.set(counterKey, "0");

        // 全量消息
        List<Message> allMsg = chatMemoryRepository.findByConversationId(convId);
        // 记录上次归档位置key
        String indexKey = "chat:last_archive_idx:" + convId;
        Integer lastIdx = Optional.ofNullable(redisCacheUtil.get(indexKey))
                .map(Integer::parseInt)
                .orElse(0);

        // 没有新增消息直接返回
        if(allMsg.size() <= lastIdx){
            return;
        }

        // 只取【上次归档之后新增的消息】，只摘要新增部分
        List<Message> needSummary = new ArrayList<>(allMsg.subList(lastIdx, allMsg.size()));

        String sourceText = assembleMsgToString(needSummary);

        DashScopeChatOptions firstOptions = DashScopeChatOptions.builder()
                .withModel("qwen-flash")
                .withTemperature(0.1d)
                .withTopP(0.3d)
                .withMaxToken(400)
                .build();
        Prompt prompt = new Prompt(List.of(
                new UserMessage(basePromptConfigProperties.getSummary() + "\\n对话内容：\\n" + sourceText)
        ), firstOptions);

        ChatResponse resp;
        try {
            resp = dashScopeChatModel.call(prompt);
        } catch (Exception e) {
            log.error("会话{}调用大模型生成摘要异常", convId, e);
            return;
        }

        if (resp == null || resp.getResult() == null || resp.getResult().getOutput() == null) {
            log.error("会话{}摘要返回空结果", convId);
            return;
        }
        String summary = resp.getResult().getOutput().getText().trim();
        if (summary.isBlank()) {
            log.warn("会话{}摘要文本为空", convId);
            return;
        }

        //使用分词器将用户信息分词入库metadata，方便后续筛选召回信息
        String keyword = IkKeywordUtil.extractKeywords(summary);

        // 摘要落向量库
        // 固定type，方便后续过滤
        Map<String,Object> meta = new HashMap<>();
        meta.put("conversationId",convId);
        meta.put("doc_type","history_summary");
        meta.put("keyword",keyword);
        Document doc = Document.builder().text(summary).metadata(meta).build();
        vectorStore.add(List.of(doc));

        // 更新归档下标
        redisCacheUtil.set(indexKey, allMsg.size()+"");
    }

    // 拼接消息文本
    private String assembleMsgToString(List<Message> list){
        StringBuilder sb = new StringBuilder();
        for(Message msg : list){
            sb.append(msg.getMessageType()).append(":").append(msg.getText()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 每次对话调用前执行计数+摘要判断(分片入库)
     * @param convId
     */
    public void checkAndBuildHistorySummaryWithChunk(String convId) {
        String counterKey = "chat:counter:" + convId;
        // 用户每次提问+1
        Long count = redisCacheUtil.increment(counterKey);
        // 提问多少次触发摘要
        int triggerThreshold = myChatMemoryConfigProperties.getMaxBatchSaveMessage();
        if (count < triggerThreshold) {
            return;
        }
        redisCacheUtil.set(counterKey, "0");

        // 全量消息
        List<Message> allMsg = chatMemoryRepository.findByConversationId(convId);
        // 记录上次归档位置key
        String indexKey = "chat:last_archive_idx:" + convId;
        Integer lastIdx = Optional.ofNullable(redisCacheUtil.get(indexKey))
                .map(Integer::parseInt)
                .orElse(0);

        // 没有新增消息直接返回
        if(allMsg.size() <= lastIdx){
            return;
        }

        // 只取【上次归档之后新增的消息】，只摘要新增部分
        List<Message> needSummary = new ArrayList<>(allMsg.subList(lastIdx, allMsg.size()));
        String sourceText = assembleMsgToString(needSummary);

        // 分片
//        List<String> textSliceList = sliceSourceText(sourceText, SLICE_LEN);
        List<Document> textSliceList = sliceSourceText(sourceText, SLICE_LEN);

        DashScopeChatOptions firstOptions = DashScopeChatOptions.builder()
                .withModel("qwen-flash")
                .withTemperature(0.1d)
                .withTopP(0.3d)
                .withMaxToken(400)
                .build();

        boolean allSuccess = true;
        for (Document sliceText : textSliceList) {
            Prompt prompt = new Prompt(List.of(
                    new UserMessage("精简总结下面聊天记录，提炼关键信息、用户诉求，300字以内：\n" + sliceText)
            ), firstOptions);
            ChatResponse resp;
            try {
                resp = dashScopeChatModel.call(prompt);
            } catch (Exception e) {
                log.error("会话{}分片摘要调用异常，片段：{}", convId, sliceText, e);
                allSuccess = false;
                continue;
            }
            if (resp == null || resp.getResult() == null || resp.getResult().getOutput() == null) {
                log.error("会话{}分片返回空", convId);
                allSuccess = false;
                continue;
            }
            String summary = resp.getResult().getOutput().getText().trim();
            if (summary.isBlank()) {
                log.warn("会话{}分片摘要为空", convId);
                allSuccess = false;
                continue;
            }
            // 分片摘要入库
            Map<String,Object> meta = new HashMap<>();
            meta.put("conversationId",convId);
            meta.put("doc_type","history_summary");
            Document doc = Document.builder().text(summary).metadata(meta).build();
            vectorStore.add(List.of(doc));
        }

        // 全部分片处理成功，才更新下标；失败保留原下标，下次触发继续处理这批数据
        if(allSuccess){
            redisCacheUtil.set(indexKey, allMsg.size()+"");
            log.info("会话{}本轮增量消息分片摘要全部完成,更新归档下标:{}",convId,allMsg.size());
        }else{
            log.warn("会话{}本轮部分分片摘要失败，暂不更新归档下标，下次重试",convId);
        }
    }

    /**
     * 文本分片工具：按指定长度切割原文
     * @param source 原始长文本
     * @param sliceMaxLen 单段最大字符
     * @return 分段集合
     */
    private List<Document> sliceSourceText(String source, int sliceMaxLen){
        // 原文包装doc
        Document originDoc = Document.builder().text(source).build();
        // 自动分片
        List<Document> list = tokenTextSplitter.split(List.of(originDoc));
        return list;

//        List<String> list = new ArrayList<>();
//        if(source == null || source.isBlank()){
//            return list;
//        }
//        int start = 0;
//        int totalLen = source.length();
//        while(start < totalLen){
//            int end = Math.min(start + sliceMaxLen, totalLen);
//            list.add(source.substring(start, end));
//            start = end;
//        }

    }


}
