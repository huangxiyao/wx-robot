package com.hxy.robot.utils;

import java.util.concurrent.ConcurrentHashMap;

public class BeanRepository {
	private BeanRepository(){
		throw new IllegalAccessError("Utility class");
	}
	
	private static ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
	public static void put(String key, Object value){
		map.put(key, value);
	}
	public static Object get(String key){
		return  map.get(key);
	}
	
	public static Object delete(String key){
		return map.remove(key);
	}
}
