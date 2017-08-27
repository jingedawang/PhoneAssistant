package com.bit.applock;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;



/**
 * 应用程序管理 业务类
 * @author Administrator
 *
 */
public class AppInfoProvider {
	private static final String TAG = "AppInfoProvider";
	private Context context;
	private PackageManager packManager;
	public AppInfoProvider(Context context) {
		this.context = context;
		packManager=context.getPackageManager();
	}

	/**
	 * 返回当前手机里面安装的所有应用程序信息的集合
	 * @return
	 */
	public List<AppInfo> getAllApps(){
		List<AppInfo> appinfos =new ArrayList<AppInfo>();
		List<PackageInfo> packinfos=packManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES); //获得所有的应用程序包
		for(PackageInfo info:packinfos){
			AppInfo myApp = new AppInfo();
			String packname=info.packageName; //包名
			myApp.setPackname(packname);
			ApplicationInfo appinfo=info.applicationInfo;
			Drawable icon=appinfo.loadIcon(packManager); //应用程序的图标
			myApp.setIcon(icon);
			String appname=appinfo.loadLabel(packManager).toString(); //应用程序的名字
			myApp.setAppname(appname);
			if(filterApp(appinfo)){
				Log.i(TAG,"用户应用");
				if(!myApp.getAppname().equals("AirReader"))
				appinfos.add(myApp);
				
				myApp.setSystemApp(false);
			}else{
				Log.i(TAG,"系统应用");
				myApp.setSystemApp(true);
			}
			
		}
		return appinfos;
	}
	/**
	 * 判断某个应用程序是不是第三方的应用程序
	 * @param info  是第三方的话返回true
	 * @return
	 */
	public boolean filterApp(ApplicationInfo info){
		if((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!=0){
			return true;
		}else if((info.flags & ApplicationInfo.FLAG_SYSTEM)==0){
			return true;
		}
		return false;
	}
}
