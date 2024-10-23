package com.heyuchen.ai.service;

import com.heyuchen.ai.constants.ModelProviderEnum;
import com.heyuchen.ai.factory.ChatClientFactory;
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class XhsService {

    private final ChatClientFactory chatClientFactory;

    public XhsService(ChatClientFactory chatClientFactory) {
        this.chatClientFactory = chatClientFactory;
    }

    public JSONObject rate(String text, String keyword, String positive, String negative) {
        ChatClient chatClient = chatClientFactory.getChatClient(ModelProviderEnum.ZHIPU);
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
        String output = chatClient.prompt(promptTemplate.create()).call().content();
        System.out.println(output);
        return parseOutput(output);
    }

    private JSONObject parseOutput(String output) {
        if (output.startsWith("```json") && output.endsWith("```")) {
            output = output.substring(7, output.length() - 3);
            return new JSONObject(output);
        }
        return new JSONObject("{}");
    }
}
