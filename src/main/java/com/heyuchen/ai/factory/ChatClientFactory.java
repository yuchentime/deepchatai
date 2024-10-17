package com.heyuchen.ai.factory;

import com.heyuchen.ai.constants.ModelProviderEnum;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ChatClientFactory {

    private ApplicationContext applicationContext;

    @Autowired
    public ChatClientFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ChatClient getChatClient(ModelProviderEnum modelProvider) {
        switch (modelProvider) {
            case ZHIPU:
                return applicationContext.getBean("zhipuChatClient", ChatClient.class);
            // 可以在此添加更多的case来支持其他模型
            default:
                throw new IllegalArgumentException("未知的模型名称: " + modelProvider.getProvider());
        }
    }
}