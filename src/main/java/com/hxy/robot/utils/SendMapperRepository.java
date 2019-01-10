package com.hxy.robot.utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发送消息映射方式  类型：[群组]
 */
public class SendMapperRepository {
	private SendMapperRepository(){
		throw new IllegalAccessError("Utility class");
	}
	
	public static ConcurrentHashMap<String, List> map = new ConcurrentHashMap<String, List>();
	public static void put(String key, List value){
		map.put(key, value);
	}
	public static List get(String key){
		return  map.get(key);
	}
	
	public static List delete(String key){
		return map.remove(key);
	}
}
