package com.heyuchen.ai.controller;

import com.heyuchen.ai.constants.ModelProviderEnum;
import com.heyuchen.ai.factory.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class AIController {

	private final ChatClientFactory chatClientFactory;

	AIController(ChatClientFactory chatClientFactory) {
		this.chatClientFactory = chatClientFactory;
	}

	@GetMapping("/ai")
	Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		ChatClient chatClient = chatClientFactory.getChatClient(ModelProviderEnum.ZHIPU);
		return Map.of(
				"completion",
				chatClient.prompt()
						.user(message)
						.call()
						.content());
	}
}
