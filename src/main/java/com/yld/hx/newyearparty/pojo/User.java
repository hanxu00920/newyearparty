package com.yld.hx.newyearparty.pojo;

public class User {

	private String name;
	private String idLast;
	private String longitude;// 经度
	private String latitude;// 纬度
	private String openid;
	private String nickname;
	private String headimgurl;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIdLast() {
		return idLast;
	}
	public void setIdLast(String idLast) {
		this.idLast = idLast;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getHeadimgurl() {
		return headimgurl;
	}
	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "姓名：" + name + ",身份证号后六位：" + idLast + ",经度：" + longitude + ",纬度：" + latitude + ",OPENID：" + openid + ",微信号：" + nickname + "头像URL：" + headimgurl;
	}
	
	
}
