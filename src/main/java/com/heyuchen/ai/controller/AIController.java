package com.heyuchen.ai.controller;

import com.heyuchen.ai.constants.ApiResponse;
import com.heyuchen.ai.constants.ModelProviderEnum;
import com.heyuchen.ai.dto.request.CreateQuestionRequest;
import com.heyuchen.ai.factory.ChatClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
class AIController {

    private static final Logger logger = LogManager.getLogger(AIController.class);

    private final ChatClientFactory chatClientFactory;

    AIController(ChatClientFactory chatClientFactory) {
        this.chatClientFactory = chatClientFactory;
    }

    @Autowired
    private EmbeddingModel zhiPuAiEmbeddingModel;
    @Autowired
    private VectorStore vectorStore;

    @DeleteMapping("/delete")
    public void delete(@RequestParam(value = "id", defaultValue = "58c4979f-3d20-464e-b2de-7906a3eaada9") String id) {
        vectorStore.delete(List.of(id));
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

    @PostMapping("generate-question")
    ApiResponse generateQuestion(@RequestBody CreateQuestionRequest request) {
        String text = request.getText();
        String provider = request.getProvider();
        String topic = request.getTopic();

        Document originalDocument = new Document(text);
        TextSplitter textSplitter = new TokenTextSplitter(256, 128, 5, 1000, true);
        List<Document> documentList = textSplitter.split(originalDocument);

        vectorStore.add(documentList);
        List<Document> retrievedDocuments = vectorStore.similaritySearch(SearchRequest.query(topic).withTopK(5).withSimilarityThreshold(0.3));

        if (CollectionUtils.isEmpty(retrievedDocuments)) {
            logger.warn("No any documents retrieved.");
            return ApiResponse.error("No any documents retrieved.");
        }

        ChatClient chatClient = chatClientFactory.getChatClient(ModelProviderEnum.getByProvider(provider));

        Message systemMessage = new SystemMessage("You are an expert in question generation, extracting key information from text and generating deep, enlightening questions.Please generate 5 high-quality questions regarding {topic} base on the {context}. <context> " + retrievedDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n\n")) + "</context>");
        Message userMessage = new UserMessage("<topic>" + topic + "</topic>");
        ChatResponse chatResponse = chatClient.prompt(new Prompt(List.of(systemMessage, userMessage))).call().chatResponse();

        if (CollectionUtils.isEmpty(chatResponse.getResults())) {
            logger.warn("Failed to generate questions from LLM.");
            return ApiResponse.error("Failed to generate questions from LLM.");
        }
        return ApiResponse.success(chatResponse.getResult().getOutput().getContent());
    }
}
