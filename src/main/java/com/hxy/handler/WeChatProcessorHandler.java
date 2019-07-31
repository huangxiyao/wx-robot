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

import com.hxy.robot.utils.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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
     * @throws Exception exception
     */
    @RequestMapping(value = "/sendMessage",method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Response qq(@RequestBody RecieveQQMsg qqMsg) throws Exception {
    	LOGGER.info("请求参数是：{}",JSON.toJSONString(qqMsg));

        Response res = new Response();
        if (org.codehaus.plexus.util.StringUtils.isBlank(qqMsg.getMessage())) {
            LOGGER.warn("Empty msg body");
            res.setSuccess(false);
            res.setErrorCode("999998");
            res.setErrorDesc("SC_BAD_REQUEST");
            return res;
        }

        ExecutorService exe = Executors.newFixedThreadPool(2);
        ExecutorCompletionService ecs = new ExecutorCompletionService(exe);
        try {
            LOGGER.info("发送信息到微信客户端，信息是：{}",qqMsg);
            exe.execute(()-> executeWeChatSync(qqMsg));
            LOGGER.info("发送信息到钉钉客户端，信息是：{}",qqMsg);
            exe.execute(()-> executeDingDingSync(qqMsg));
        } catch (Exception e) {
            LOGGER.error("执行任务发生异常",e);
        }


        exe.shutdown();
        while (!exe.isTerminated()) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                LOGGER.info("线程休眠异常");
            }
        }

        res.setErrorCode("000000");
        res.setErrorDesc("成功！");
        res.setSuccess(true);
        return res;
    }

    /**
     * 微信客户端
     * @param qqMsg
     */
    public void executeWeChatSync(RecieveQQMsg qqMsg){
        try{
            String serviceType = qqMsg.getType();
            String msg = qqMsg.getMessage();
            Map<String, List> map = SendMapperRepository.map;
            LOGGER.info("当前已经映射的群信息是：{}",JSON.toJSONString(map));
            String groupId = null;
            for(String key : map.keySet()){
                List typeList  = map.get(key);
                if(typeList != null && !typeList.isEmpty()){
                    for(Object typeKey : typeList){
                        if(Integer.compare(Integer.valueOf(String.valueOf(typeKey)), Integer.valueOf(serviceType)) == 0){
                            groupId = key;
                            WeChatBot bot = (WeChatBot) BeanRepository.get("chatBot");
                            LOGGER.info("已发送" + msg + "] ，服务类型： [" + serviceType + "], 发送群："+MapperRepository.get(groupId));
                            bot.api().sendText(groupId, qqMsg.getMessage());
                        }
                    }
                }
            }
            LOGGER.info("Push WX groups [msg=" + msg + "]");
        }catch (Exception e){
            LOGGER.error("微信消息发送失败，异常信息是",e);
        }

    }

    public void executeDingDingSync(RecieveQQMsg qqMsg){
        try{
            LOGGER.info("进入到executeDingDingSync方法进行执行！");
            DingdingTalkHandler talkHandler =  new DingdingTalkHandler();
            LOGGER.info("进入到executeDingDingSync方法进行执行！8888888");
            talkHandler.sendMessage(qqMsg.getMessage());
        }catch (Exception e){
            LOGGER.error("异常信息是",e);
        }

    }

    @Test
    public void test(){
        ConfigRepository.put("DingDingTokenPhoneNum","");
        ConfigRepository.put("DingDingAccessToken","");
        RecieveQQMsg qqMsg =  new RecieveQQMsg();
        qqMsg.setMessage("你好，\n炸了");
        try{
            qq(qqMsg);
        }catch (Exception e){

        }
    }
}
