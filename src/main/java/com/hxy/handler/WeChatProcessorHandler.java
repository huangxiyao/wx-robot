/*
 * Copyright (c) 2012-2018, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hxy.handler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.hxy.robot.WeChatBot;
import com.hxy.robot.api.Response;
import com.hxy.robot.api.model.RecieveQQMsg;
import com.hxy.robot.service.robotservice.BaiduQueryService;
import com.hxy.robot.service.robotservice.TuringQueryService;
import com.hxy.robot.utils.BeanRepository;
import com.hxy.robot.utils.MapperRepository;
import com.hxy.robot.utils.SpringContextUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * QQ processor.
 * <ul>
 * <li>Handles QQ message (/qq), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.4, Oct 31, 2017
 * @since 1.0.0
 */
@RestController
@RequestMapping("/")
public class WeChatProcessorHandler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WeChatProcessorHandler.class);

    

    /**
     * Handles QQ message.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/sendMessage",method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Response qq(@RequestBody RecieveQQMsg qqMsg) throws Exception {
    	LOGGER.info("请求参数是：{}",JSON.toJSONString(qqMsg));
    	Response res = new Response();
        
    	String serviceType = qqMsg.getType();
        String msg = qqMsg.getMessage();
        
        Map<String, Object> map = MapperRepository.map;
    	String groupId = null;
    	for(String key : map.keySet()){
    		if(Integer.compare((int) map.get(key), Integer.valueOf(serviceType)) == 0){
    			groupId = key;
    			LOGGER.info("已发送" + msg + "] ，服务类型： [" + serviceType + "]");
    			WeChatBot bot = (WeChatBot) BeanRepository.get("chatBot");
    			bot.api().sendText(groupId, qqMsg.getMessage());
    		}
    	}
        
        
        if (org.codehaus.plexus.util.StringUtils.isBlank(msg)) {
            LOGGER.warn("Empty msg body");
            res.setSuccess(false);
            res.setErrorCode("999998");
        	res.setErrorDesc("SC_BAD_REQUEST");
            return res;
        }

        LOGGER.info("Push QQ groups [msg=" + msg + "]");
        res.setErrorCode("000000");
        res.setErrorDesc("成功！");
        res.setSuccess(true);
        return res;
    }
}
