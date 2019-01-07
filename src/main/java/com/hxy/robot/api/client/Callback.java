package com.hxy.robot.api.client;

import com.hxy.robot.api.request.ApiRequest;
import com.hxy.robot.api.response.ApiResponse;

import java.io.IOException;

public interface Callback<T extends ApiRequest, R extends ApiResponse> {

    void onResponse(T request, R response);

    void onFailure(T request, IOException e);

}