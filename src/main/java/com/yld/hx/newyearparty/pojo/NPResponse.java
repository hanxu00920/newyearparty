package com.yld.hx.newyearparty.pojo;

import java.util.HashMap;

public class NPResponse extends HashMap<String, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5669361195704277953L;
	
	/**
	 * 设置返回信息
	 * @param retCode 响应码
	 * @param retMsg 响应信息
	 */
	public void setResponse(String retCode, String retMsg) {
		this.put("retCode", retCode);
		this.put("retMsg", retMsg);
	}
	
}
