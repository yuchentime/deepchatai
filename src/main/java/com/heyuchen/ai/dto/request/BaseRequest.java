package com.heyuchen.ai.dto.request;

import com.heyuchen.ai.constants.ModelProviderEnum;
import lombok.Data;

@Data
public class BaseRequest {

    private String provider = ModelProviderEnum.ZHIPU.getProvider();
}
