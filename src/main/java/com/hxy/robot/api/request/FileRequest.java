package com.hxy.robot.api.request;

import com.hxy.robot.api.response.FileResponse;

/**
 * 下载文件请求
 *
 * @author biezhi
 * @date 2018/1/18
 */
public class FileRequest extends ApiRequest<FileRequest, FileResponse> {

    public FileRequest(String url) {
        super(url, FileResponse.class);
    }

}
