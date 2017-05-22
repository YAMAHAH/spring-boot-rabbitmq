package com.wlf.demo.pojo;

import java.io.Serializable;

public class Order implements Serializable{

	private static final long serialVersionUID = 3105914349129733809L;
	
	private String id;
	private String otherInfo;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOtherInfo() {
		return otherInfo;
	}
	public void setOtherInfo(String otherInfo) {
		this.otherInfo = otherInfo;
	}
	
}
