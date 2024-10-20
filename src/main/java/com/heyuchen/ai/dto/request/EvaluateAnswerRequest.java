package com.heyuchen.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EvaluateAnswerRequest extends BaseRequest {

    @NotBlank(message = "question must be not empty")
    private String question;
    @NotBlank(message = "answer must be not empty")
    private String answer;
}
