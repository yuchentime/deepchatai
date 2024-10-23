package com.heyuchen.ai.service;

import com.heyuchen.ai.controller.XhsController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class XhsService {

    private static final Logger logger = LogManager.getLogger(XhsService.class);

    @Autowired
    private ZhiPuAiChatModel zhipuChatModel;

    public JSONObject rate(String text, String keyword, String positive, String negative) {
        var template = """
                ## 任务描述
                我将会提供给你一段<文本>，请你根据该段文本的<主题>判断其是否满足<评估要求>，然后给出评分，评分范围1～10。
                ## 主题
                {keyword}
                ## 评估要求
                1. **得分说明** - {positive}
                2. **扣分说明** - {negative}
                ## 评分标准
                - **1-3分**：完全不符合要求。
                - **4-6分**：部分符合要求，存在明显不足。
                - **7-10分**：完全符合要求，内容丰富且有深度。
                ## 输出格式
                采用JSON对象格式输出，其中包含评分字段score和评估理由字段reason。
                ## 文本
                ```
                {text}
                ```
                """;
        PromptTemplate promptTemplate = new PromptTemplate(template, Map.of("text", text, "keyword", keyword, "positive", positive, "negative", negative));
        String modelName = StringUtils.hasText(System.getenv("zhipuai.model")) ? System.getenv("zhipuai.model") : ZhiPuAiApi.ChatModel.GLM_4_AirX.getValue();
        logger.info("current model name is: {}", modelName);
        ChatResponse chatResponse = zhipuChatModel.call(
                new Prompt(
                        promptTemplate.create().getContents(),
                        ZhiPuAiChatOptions.builder()
                                .withModel(modelName)
                                .withTemperature(0.5)
                                .build()
                ));
        if (CollectionUtils.isEmpty(chatResponse.getResults())) {
            return new JSONObject("{}");
        }
//        System.out.println(output);
        return parseOutput(chatResponse.getResult().getOutput().getContent());
    }

    private JSONObject parseOutput(String output) {
        if (output.startsWith("```json") && output.endsWith("```")) {
            output = output.substring(7, output.length() - 3);
            return new JSONObject(output);
        }
        return new JSONObject("{}");
    }
}
