package com.mabeijianxi.circle_of_friends.bean;

import java.io.Serializable;

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
