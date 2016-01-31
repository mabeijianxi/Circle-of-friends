package com.mabeijianxi.circle_of_friends.bean;

import java.io.Serializable;
/**
 * Created by mabeijianxi on 2015/12/31.
 * 评价详情
 */
public class BaseDataBean<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int code;
	public T data;
	public String msg;
	public long now;
	public boolean success;

}
