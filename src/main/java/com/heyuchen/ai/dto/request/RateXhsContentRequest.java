package com.heyuchen.ai.dto.request;

import lombok.Data;

@Data
public class RateXhsContentRequest {
    private String text;
    private String keyword;
    private String positive;
    private String negative;
}
