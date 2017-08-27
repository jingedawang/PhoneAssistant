package com.bit.applock;

import android.graphics.drawable.Drawable;

/**
 * 应用程序实体
 * @author Administrator
 *
 */
public class AppInfo {
	private Drawable icon;
	private String appname;
	private String packname;
	private boolean isSystemApp; //如果是用户自己安装的程序 返回就为flase 系统的为true
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public boolean isSystemApp() {
		return isSystemApp;
	}
	public void setSystemApp(boolean isSystemApp) {
		this.isSystemApp = isSystemApp;
	}
}
