package com.heyuchen.ai.controller;

import com.heyuchen.ai.configuration.PromptConfig;
import com.heyuchen.ai.constants.ApiResponse;
import com.heyuchen.ai.constants.ModelProviderEnum;
import com.heyuchen.ai.dto.request.CreateQuestionRequest;
import com.heyuchen.ai.dto.request.UploadDocumentRequest;
import com.heyuchen.ai.factory.ChatClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class AIController {

    private static final Logger logger = LogManager.getLogger(AIController.class);

    private final VectorStore vectorStore;
    private final ChatClientFactory chatClientFactory;
    private final PromptConfig promptConfig;

    public AIController(VectorStore vectorStore, ChatClientFactory chatClientFactory, PromptConfig promptConfig) {
        this.vectorStore = vectorStore;
        this.chatClientFactory = chatClientFactory;
        this.promptConfig = promptConfig;
    }

    @PostMapping("/upload-text")
    ApiResponse uploadText(@RequestBody UploadDocumentRequest request) {
        String text = request.getText();

        Document originalDocument = new Document(text);
        TextSplitter textSplitter = new TokenTextSplitter(256, 128, 5, 1000, true);
        List<Document> documentList = textSplitter.split(originalDocument);

        vectorStore.add(documentList);
        return ApiResponse.success("Successfully uploaded.");
    }

    @PostMapping("generate-question")
    ApiResponse generateQuestion(@RequestBody CreateQuestionRequest request) {
        String provider = request.getProvider();
        String topic = request.getTopic();

//        todo 如果用户不指定topic，那么会传入标题，可能需要让LLM重写

        List<Document> retrievedDocuments = vectorStore.similaritySearch(SearchRequest.query(topic).withTopK(5).withSimilarityThreshold(0.3));

        if (CollectionUtils.isEmpty(retrievedDocuments)) {
            logger.warn("No any documents retrieved.");
            return ApiResponse.error("No any documents retrieved.");
        }

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptConfig.getGenerateQuestionSystem());
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", retrievedDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n\n"))));

        PromptTemplate userPromptTemplate = new PromptTemplate(promptConfig.getGenerateQuestionUser(), Map.of("topic", topic));

        ChatClient chatClient = chatClientFactory.getChatClient(ModelProviderEnum.getByProvider(provider));
        ChatResponse chatResponse = chatClient.prompt(new Prompt(List.of(systemMessage, userPromptTemplate.createMessage()))).call().chatResponse();

        if (CollectionUtils.isEmpty(chatResponse.getResults())) {
            logger.warn("Failed to generate questions from LLM.");
            return ApiResponse.error("Failed to generate questions from LLM.");
        }
        String output = chatResponse.getResult().getOutput().getContent();
        List<String> questions = List.of(output.split("\n"));
        return ApiResponse.success(questions);
    }
}
