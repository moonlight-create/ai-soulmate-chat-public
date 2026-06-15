package com.wj.aisoulmatechat.util;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class IkKeywordUtil {

    private static final Set<String> STOP_WORDS;

    static {
        STOP_WORDS = new HashSet<>();
        // 人称代词
        STOP_WORDS.add("我");
        STOP_WORDS.add("你");
        STOP_WORDS.add("他");
        STOP_WORDS.add("她");
        STOP_WORDS.add("它");
        STOP_WORDS.add("我们");
        STOP_WORDS.add("你们");
        // 疑问词
        STOP_WORDS.add("什么");
        STOP_WORDS.add("哪");
        STOP_WORDS.add("怎么");
        STOP_WORDS.add("为啥");
        // 语气助词
        STOP_WORDS.add("呢");
        STOP_WORDS.add("吗");
        STOP_WORDS.add("呀");
        STOP_WORDS.add("吧");
        STOP_WORDS.add("了");
        STOP_WORDS.add("哦");
        // 连接虚词
        STOP_WORDS.add("然后");
        STOP_WORDS.add("就是");
        STOP_WORDS.add("还是");
        STOP_WORDS.add("但是");
        STOP_WORDS.add("不过");
    }

    /**
     * 提取文本核心关键词
     */
    public static String extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        List<String> wordList = new ArrayList<>();
        try (StringReader reader = new StringReader(text)) {
            IKSegmenter ikSegmenter = new IKSegmenter(reader, true);
            Lexeme lexeme;
            while ((lexeme = ikSegmenter.next()) != null) {
                String word = lexeme.getLexemeText().trim();
                if (!STOP_WORDS.contains(word) && word.length() > 1) {
                    wordList.add(word);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return wordList.stream()
                .distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * 判断两组关键词是否存在交集
     */
    public static boolean isMatch(String sourceKeys, String queryKeys) {
        if (sourceKeys == null || queryKeys == null
                || sourceKeys.isBlank() || queryKeys.isBlank()) {
            return false;
        }

        Set<String> sourceSet = new HashSet<>(Arrays.asList(sourceKeys.split(",")));
        Set<String> querySet = new HashSet<>(Arrays.asList(queryKeys.split(",")));

        for (String key : querySet) {
            if (sourceSet.contains(key)) {
                return true;
            }
        }
        return false;
    }

}
