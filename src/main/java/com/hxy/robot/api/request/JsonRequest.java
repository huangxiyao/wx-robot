package com.hxy.robot.api.request;

import com.hxy.robot.api.response.JsonResponse;

/**
 * JSON请求
 *
 * @author biezhi
 * @date 2018/1/18
 */
public class JsonRequest extends ApiRequest<JsonRequest, JsonResponse> {

    public JsonRequest(String url) {
        super(url, JsonResponse.class);
    }

}