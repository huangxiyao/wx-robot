package com.hxy.handler;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.hxy.robot.utils.ConfigRepository;
import com.hxy.robot.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 钉钉推荐客户端
 */
public class DingdingTalkHandler {

    static Logger  logger = LoggerFactory.getLogger(DingdingTalkHandler.class);


    public static void sendMessage(String content){
    logger.info("进入钉钉客户端工具发送方法》》》》》》》》》》》");

        /**
         * request.setMsgtype("link");
         *         OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
         *         link.setMessageUrl("https://www.dingtalk.com/");
         *         link.setPicUrl("");
         *         link.setTitle("时代的火车向前开");
         *         link.setText("这个即将发布的新版本，创始人陈航（花名“无招”）称它为“红树林”。\n" +
         *                 "而在此之前，每当面临重大升级，产品经理们都会取一个应景的代号，这一次，为什么是“红树林");
         *         request.setLink(link);
         *
         *         request.setMsgtype("markdown");
         *         OapiRobotSendRequest.Markdown markdown = new OapiRobotSendRequest.Markdown();
         *         markdown.setTitle("杭州天气");
         *         markdown.setText("#### 杭州天气 @156xxxx8827\n" +
         *                 "> 9度，西北风1级，空气良89，相对温度73%\n\n" +
         *                 "> ![screenshot](https://gw.alipayobjects.com/zos/skylark-tools/public/files/84111bbeba74743d2771ed4f062d1f25.png)\n"  +
         *                 "> ###### 10点20分发布 [天气](http://www.thinkpage.cn/) \n");
         *         request.setMarkdown(markdown);
         */

        try{
            String accesstoken = ConfigRepository.get("DingDingAccessToken");
            String serviceUrl = "https://oapi.dingtalk.com/robot/send?access_token="+accesstoken;
            DefaultDingTalkClient client = new DefaultDingTalkClient(serviceUrl);
            OapiRobotSendRequest request = new OapiRobotSendRequest();
            request.setMsgtype("text");
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent(content);
            request.setText(text);

            //获取配置要@的手机号
            String digndingTokenPhoneNo = ConfigRepository.get("DingDingTokenPhoneNum");
            List<String> phoneList = new ArrayList<>();
            if(StringUtils.isNotEmpty(digndingTokenPhoneNo)){
                phoneList = Arrays.asList(digndingTokenPhoneNo.split("|"));
                OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
                at.setAtMobiles(phoneList);
                request.setAt(at);
            }
            logger.info("通知钉钉群信息参数是：{}",request);
            OapiRobotSendResponse response = client.execute(request);
            logger.info("发送信息到钉钉群成功");
        }catch (Exception e){
            logger.error("发送信息到顶顶群失败，异常信息是",e);
        }


    }

}
