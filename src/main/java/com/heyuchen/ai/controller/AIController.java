package com.heyuchen.ai.controller;

import com.heyuchen.ai.dto.ApiResponse;
import com.heyuchen.ai.dto.request.CreateQuestionRequest;
import com.heyuchen.ai.dto.request.EvaluateAnswerRequest;
import com.heyuchen.ai.dto.request.UploadDocumentRequest;
import com.heyuchen.ai.exception.BizException;
import com.heyuchen.ai.service.AiService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AIController {

    private static final Logger logger = LogManager.getLogger(AIController.class);

    private final AiService aiService;

    public AIController(AiService aaiService) {
        this.aiService = aaiService;
    }

    @PostMapping("/upload-text")
    ApiResponse uploadText(@RequestBody UploadDocumentRequest request) {
        aiService.uploadText(request.getTitle(), request.getText());
        return ApiResponse.success("Successfully uploaded.", null);
    }

    @PostMapping("generate-question")
    ApiResponse generateQuestion(@RequestBody CreateQuestionRequest request) throws BizException {
        List<String> questionList = aiService.generateQuestions(request.getProvider(), request.getTitle());
        return ApiResponse.success("Successfully generated questions.", questionList);
    }

    @PostMapping("evaluate-answer")
    ApiResponse evaluateAnswer(@RequestBody EvaluateAnswerRequest request) throws BizException {
        String provider = request.getProvider();
        String question = request.getQuestion();
        String answer = request.getAnswer();
        String evaluatedResult = aiService.evaluateAnswer(provider, question, answer);
        return ApiResponse.success("Successfully generated questions.", evaluatedResult);
    }

}
