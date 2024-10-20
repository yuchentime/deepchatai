package com.heyuchen.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RetrievalRequest extends BaseRequest {

    @NotBlank(message = "title must be not empty")
    private String title;
    @NotBlank(message = "question must be not empty")
    private String question;
}
