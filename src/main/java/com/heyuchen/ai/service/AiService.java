package com.heyuchen.ai.service;

import com.heyuchen.ai.configuration.PromptConfig;
import com.heyuchen.ai.constants.ApiResponse;
import com.heyuchen.ai.constants.ModelProviderEnum;
import com.heyuchen.ai.controller.AIController;
import com.heyuchen.ai.dto.request.UploadDocumentRequest;
import com.heyuchen.ai.exception.BizException;
import com.heyuchen.ai.factory.ChatClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger logger = LogManager.getLogger(AiService.class);

    private final VectorStore vectorStore;
    private final ChatClientFactory chatClientFactory;
    private final PromptConfig promptConfig;

    public AiService(VectorStore vectorStore, ChatClientFactory chatClientFactory, PromptConfig promptConfig) {
        this.vectorStore = vectorStore;
        this.chatClientFactory = chatClientFactory;
        this.promptConfig = promptConfig;
    }

    public void uploadText(String text) {

        Document originalDocument = new Document(text);
        TextSplitter textSplitter = new TokenTextSplitter(256, 128, 5, 1000, true);
        List<Document> documentList = textSplitter.split(originalDocument);

        vectorStore.add(documentList);
    }

    public List<String> generateQuestions(String provider, String topic) throws BizException {

//        todo 如果用户不指定topic，那么会传入标题，可能需要让LLM重写

        List<Document> retrievedDocuments = vectorStore.similaritySearch(SearchRequest.query(topic).withTopK(5).withSimilarityThreshold(0.3));

        if (CollectionUtils.isEmpty(retrievedDocuments)) {
            logger.warn("No any documents retrieved.");
            throw new BizException("Not found any information regarding " + topic);
        }

        ChatClient chatClient = chatClientFactory.getChatClient(ModelProviderEnum.getByProvider(provider));
        Prompt prompt = buildQuestionPrompt(topic, retrievedDocuments);
        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

        if (CollectionUtils.isEmpty(chatResponse.getResults())) {
            logger.warn("Failed to generate questions from LLM.");
            throw new BizException("Failed to generate questions from AI.");
        }
        String output = chatResponse.getResult().getOutput().getContent();
        return List.of(StringUtils.delimitedListToStringArray(output, "\n"));
    }

    private Prompt buildQuestionPrompt(String topic, List<Document> retrievedDocuments) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptConfig.getGenerateQuestionSystem() + "{format}");
        String context = retrievedDocuments.stream().map(Document::getContent).collect(Collectors.joining("\n\n"));
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));

        PromptTemplate userPromptTemplate = new PromptTemplate(promptConfig.getGenerateQuestionUser(), Map.of("topic", topic));
        return new Prompt(List.of(systemMessage, userPromptTemplate.createMessage()));
    }

}
