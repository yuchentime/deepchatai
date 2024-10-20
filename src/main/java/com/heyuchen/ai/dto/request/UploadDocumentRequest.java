package com.heyuchen.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadDocumentRequest {

    @NotBlank(message = "title must be not empty")
    private String title;

    @NotBlank(message = "text must be not empty")
    private String text;

}
