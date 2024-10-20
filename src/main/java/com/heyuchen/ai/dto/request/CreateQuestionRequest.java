package com.heyuchen.ai.dto.request;

import lombok.Data;

@Data
public class CreateQuestionRequest extends BaseRequest {

    private String topic;
    private String text;

}
