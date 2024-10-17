package com.heyuchen.ai.constants;

public enum ModelProviderEnum {
    OPENAI("openai"),
    BAIDU("baidu"),
    ZHIPU("zhipu");

    private String provider;

    ModelProviderEnum(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
