package com.heyuchen.ai.controller;

import com.heyuchen.ai.dto.ApiResponse;
import com.heyuchen.ai.dto.request.RateXhsContentRequest;
import com.heyuchen.ai.dto.response.RateXhsContentResponse;
import com.heyuchen.ai.service.XhsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("xhs")
public class XhsController {

    private static final Logger logger = LogManager.getLogger(XhsController.class);

    private final XhsService xhsService;

    public XhsController(XhsService xhsService) {
        this.xhsService = xhsService;
    }

    @PostMapping("rate")
    public ApiResponse<RateXhsContentResponse> rate(@RequestBody RateXhsContentRequest request) {
//        negative：明显的引流获客行为，且不包含任何实质性内容。可以作为Automa的默认字段参数
//        positive：具有明确的观点输出
        logger.info("keyword:{}", request.getKeyword());
        RateXhsContentResponse response = new RateXhsContentResponse();
        if (!StringUtils.hasText(request.getText()) || !StringUtils.hasText(request.getKeyword())) {
            response.setScore(0);
            response.setReason("没有采集到笔记正文或搜索关键词");
            return ApiResponse.success("参数不能为空", response);
        }
        if (!StringUtils.hasText(request.getPositive())) {
            request.setPositive("无其他要求");
        }
        if (!StringUtils.hasText(request.getNegative())) {
            request.setNegative("无其他要求");
        }
        JSONObject resultJson = xhsService.rate(request.getText(), request.getKeyword(), request.getPositive(), request.getNegative());
        response.setScore(resultJson.getInt("score"));
        response.setReason(resultJson.getString("reason"));
        return ApiResponse.success("success", response);
    }

}
