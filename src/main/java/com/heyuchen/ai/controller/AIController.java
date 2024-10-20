package com.heyuchen.ai.controller;

import com.heyuchen.ai.constants.ModelProviderEnum;
import com.heyuchen.ai.factory.ChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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

    private final ChatClientFactory chatClientFactory;

    AIController(ChatClientFactory chatClientFactory) {
        this.chatClientFactory = chatClientFactory;
    }

    @Autowired
    private EmbeddingModel zhiPuAiEmbeddingModel;
    @Autowired
    private VectorStore vectorStore;

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
    void generateQuestion(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String provider = request.get("provider");
        String topic = request.get("topic");

        Document originalDocument = new Document(text);
        TextSplitter textSplitter = new TokenTextSplitter(256, 128, 5, 1000, true);
        List<Document> documentList = textSplitter.split(originalDocument);

        vectorStore.add(documentList);
        List<Document> retrievedDocuments = vectorStore.similaritySearch(SearchRequest.query("").withTopK(5).withSimilarityThreshold(0.3));

        if (CollectionUtils.isEmpty(retrievedDocuments)) {

            return;
        }

        ChatClient chatClient = chatClientFactory.getChatClient(ModelProviderEnum.getByProvider(provider));

        Message systemMessage = new SystemMessage("You are a helpful assistant.To generate 5 questions regarding {topic} base on the context. <context> " + retrievedDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n\n")) + "</context>");
        Message userMessage = new UserMessage("<topic>" + topic + "</topic>");
        chatClient.prompt(new Prompt(Arrays.asList(systemMessage, userMessage)));
    }
}
