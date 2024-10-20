package com.heyuchen.ai.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:prompt.properties")
public class PromptConfig {

    @Value("${system.generate.question}")
    private String generateQuestionSystem;

    @Value("${user.generate.question}")
    private String generateQuestionUser;

    @Value("${system.evaluate.answer}")
    private String evaluateAnswerSystem;

    @Value("${user.evaluate.answer}")
    private String evaluateAnswerUser;

}
