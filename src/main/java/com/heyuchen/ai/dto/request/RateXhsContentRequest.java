package com.heyuchen.ai.dto.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RateXhsContentRequest {
    private String text;
    private String keyword;
    private String positive;
    private String negative;
}
