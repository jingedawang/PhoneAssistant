package com.bit.contacts;

import java.io.Serializable;

public class MyContacts implements Serializable{
	
	private static final long serialVersionUID = 5684200915400642232L;
	private String name;//姓名
	private byte[] contactIcon;//头像
	private String telPhone;//电话
	private String groupName;//所属组名
	private String birthday;//生日
	private String address;//地址
	private String email;//邮箱
	private String description;//好友描述
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte[] getContactIcon() {
		return contactIcon;
	}
	public void setContactIcon(byte[] contactIcon) {
		this.contactIcon = contactIcon;
	}
	public String getTelPhone() {
		return telPhone;
	}
	public void setTelPhone(String telPhone) {
		this.telPhone = telPhone;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
