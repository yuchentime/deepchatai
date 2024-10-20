package com.heyuchen.ai.constants;

public enum ModelProviderEnum {
    QWEN("tongyi"),
    DEEPSEEK("deepseek"),
    ZHIPU("zhipu");

    private String provider;

    ModelProviderEnum(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public static ModelProviderEnum getByProvider(String provider) {
        for (ModelProviderEnum modelProviderEnum : ModelProviderEnum.values()) {
            if (modelProviderEnum.getProvider().equals(provider)) {
                return modelProviderEnum;
            }
        }
        return ModelProviderEnum.ZHIPU;
    }
}
