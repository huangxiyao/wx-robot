package com.hxy.robot;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hxy.robot.api.annotation.Bind;
import com.hxy.robot.api.constant.Config;
import com.hxy.robot.api.enums.AccountType;
import com.hxy.robot.api.enums.MsgType;
import com.hxy.robot.api.model.WeChatMessage;
import com.hxy.robot.dao.mapper.TRobotMessageRepositoryMapper;
import com.hxy.robot.dao.model.TRobotMessageRepositoryDao;
import com.hxy.robot.integeration.electronic.ThirdProxy;
import com.hxy.robot.service.robotservice.TuringQueryService;
import com.hxy.robot.utils.MapperRepository;
import com.hxy.robot.utils.SendMapperRepository;
import com.hxy.robot.utils.SpringContextUtil;
import com.hxy.robot.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotImpl extends WeChatBot{
	public BotImpl(Config config) {
        super(config);
    }

    /**
     * 绑定群聊信息
     *
     * @param message
     */
    @Bind(msgType = MsgType.ALL, accountType = AccountType.TYPE_GROUP)
    public void groupMessage(WeChatMessage message) {
        log.info("接收到群 [{}] 的消息: {}", message.getName(), message.getText());
        message.getFromNickName();//群的昵称，可以用来绑定对应的userName，定时发送消息
        message.getFromUserName();//可以用来指定发送消息给哪个群
        
        log.info("NickName:"+message.getFromNickName()+"userName:"+message.getFromUserName());
        log.info("mapperResporty:{}",SendMapperRepository.map);
        String messageText = message.getText();
        message.getFromRemarkName();
        String titleName = "@" + message.getMineNickName();
        if(StringUtils.isNotEmpty(messageText)){
        	if (messageText.startsWith(titleName.trim()) || messageText.contains(titleName.trim())) {
            	log.info("message:{},nickName:{}",messageText,message.getMineNickName());
            	String queryContent = messageText;
            	
            	queryContent = queryContent.replace(titleName, "").trim();
            	while(queryContent.startsWith(" ")){
            		queryContent = queryContent.substring(1).trim();
            	}
            	String result = "";
            	//判断当前机器人服务的群
            	if(SendMapperRepository.get(message.getFromUserName()) == null){
            		this.api().sendText(message.getFromUserName(), "尚未提供对应服务，请申请权限");
            		return;
            	}

				List actionTypeList = SendMapperRepository.get(message.getFromUserName());
            	//默认list中的第一个元素是当前群对应的元素
				int actionType = (int)actionTypeList.get(0);
            	//获取数据库mapper
            	TRobotMessageRepositoryMapper messageRepositoryMapper = SpringContextUtil.getBean(TRobotMessageRepositoryMapper.class);
                
            	switch(actionType){
                	case 0:
                		//智能机器人
                		log.info("智能机器人群访问入口");
                		sendToThreeMessage(message.getFromUserName(),result);
                		
                		break;
                	case 1:
                		//电影票
                		log.info("电影票群访问入口");
                		List<TRobotMessageRepositoryDao> messList = messageRepositoryMapper.selectByServiceType(1);
                		sendMessageToGroupID(message.getFromUserName(),queryContent,messList,null,message.getFromMemberNickName());
                		break;
                	case 2:
                		//党费
                		log.info("党费群访问入口");
                		List<TRobotMessageRepositoryDao> dangFeiMsgList = messageRepositoryMapper.selectByServiceType(2);
                		sendMessageToGroupID(message.getFromUserName(),queryContent,dangFeiMsgList,null,message.getFromMemberNickName());
                		break;
                	default:
                		log.info("默认群访问入口");
                		sendMessageToGroupID(message.getFromUserName(),queryContent,null,null,message.getFromMemberNickName());
                		break;
                }
            }
        }
    }
    
    //向指定的群组发送消息
  	private void sendMessageToGroupID(String fromUserName, final String msgContent, List<TRobotMessageRepositoryDao> msgList, String toUserName,String fromMemberNickName) {
  		log.info("查询请求参数：fromUserName:{}，msgContent:{},msgList:{},toUserName:{},fromMemberNickName:{}",fromUserName,msgContent,msgList,toUserName,fromMemberNickName);
    	//自定义message的处理方式
  			String msg = "";
  			//默认机器人来回答
  			boolean anwserFlag = true;
  			if(msgList != null){
  				for (int i = 0;i < msgList.size();i++) { 
  					String msgQuestion = msgList.get(i).getMsgQuestion();
  				     String value = msgList.get(i).getMsgAnswer(); 
  				     if(value != null){
  				    	 //去掉value两边的空格
  				    	 value = value.toString();
  				     }
  				     log.info("msgQuestion:"+msgQuestion +", value:"+value);
  				     if((StringUtils.isNotEmpty(msgQuestion) && msgQuestion.contains(msgContent.trim()) || (StringUtils.isNotEmpty(msgContent) && msgContent.contains(msgQuestion.trim())))){
  				    	 if(org.apache.commons.lang.StringUtils.contains(value, "http")){
  				    		 //如果value中含有http并且以-1|开头，则代表这个http地址是按照文本来提供显示的，不用来访问到远程
  				    		 if(value.startsWith("http") || org.apache.commons.lang.StringUtils.contains(value, "|http")){
  				    			 int index = value.lastIndexOf("/");
  					    		 String methodName = value.substring(index+1);
  					    		 Map<String,String> paramMap = new HashMap<>();
  					    		 String[] values = value.split("|");
  					    		 if(values.length > 1){
  					    			 //获取http地址的操作类型
  					    			 paramMap.put("queryType", values[0]);
  					    		 }
  					    		 //获取第三方服务
  					    		ThirdProxy thirdProxy = SpringContextUtil.getBean(ThirdProxy.class);
  					    		 value = thirdProxy.query(methodName, paramMap);
  				    		 }
  				    	 }
  				    	 msg = value;
  				    	 msg = msg.replaceAll("\\\\n", "\n");
  				    	 //不让机器人自己回答
  				    	 anwserFlag = false;
  				    	 break; 
  				     }
  				}  
  			}
  			//使用机器人回答问题
  			if(anwserFlag){
  				msg = answerForRobot("", msgContent);
  		    }        	
  			//发送信息到对应的群
  			this.api().sendText(fromUserName, "@"+fromMemberNickName + " "+msg);
  	}

    /**
     * 绑定私聊消息
     *
     * @param message
     */
    @Bind(msgType = {MsgType.TEXT, MsgType.VIDEO, MsgType.IMAGE, MsgType.EMOTICONS}, accountType = AccountType.TYPE_FRIEND)
    public void friendMessage(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            log.info("接收到好友 [{}] 的消息: {}", message.getName(), message.getText());
            //this.api().sendText(message.getFromUserName(), "自动回复: " + message.getText());
        }
    }

    /**
     * 好友验证消息
     *
     * @param message
     */
    @Bind(msgType = MsgType.ADD_FRIEND)
    public void addFriend(WeChatMessage message) {
        log.info("收到好友验证消息: {}", message.getText());
        if (message.getText().contains("java")) {
            //this.api().verify(message.getRaw().getRecommend());
        }
    }
    
    public void sendToThreeMessage(String fromUserName, String msg){
    	
    }
    
    /**
     * 机器人自动回复
     * @param toUserName 群组成员
     * @param queryContent 查询内容
     * @return
     */
    private String answerForRobot(String toUserName, String queryContent){
    	TuringQueryService service = SpringContextUtil.getBean(TuringQueryService.class);
    	return service.chat("",queryContent);
    }
    
}
