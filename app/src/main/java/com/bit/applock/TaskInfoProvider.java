package com.bit.applock;

import java.util.ArrayList;
import java.util.List;

import com.wjg.phoneassistant.R;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Debug.MemoryInfo;

/**
 * 获取所有的进程信息
 * @author Administrator
 *
 */
public class TaskInfoProvider {
	
	private Context context;
	private PackageManager pm;
	private ActivityManager am;
	public TaskInfoProvider(Context context) {
		this.context = context;
		pm=context.getPackageManager();
		am=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	}

	public List<TaskInfo> getAllTasks(List<RunningAppProcessInfo> runningAppProcessInfos){
		List<TaskInfo> taskinfos = new ArrayList<TaskInfo>();
		for (RunningAppProcessInfo info : runningAppProcessInfos) {
			TaskInfo taskinfo;
			 try {
				taskinfo= new TaskInfo();
				
				int pId = info.pid;
				taskinfo.setpId(pId);
				
				String packName=info.processName;
				taskinfo.setPackName(packName);
				
				ApplicationInfo appinfo=pm.getPackageInfo(packName, 0).applicationInfo;
				Drawable appicon=appinfo.loadIcon(pm);
				taskinfo.setAppIcon(appicon);
				
				if(filterApp(appinfo)){
					taskinfo.setSystemApp(false);
				}else{
					taskinfo.setSystemApp(true);
				}
				
				String appname=appinfo.loadLabel(pm).toString();
				taskinfo.setAppName(appname);
				
				MemoryInfo[] memoryinfos=am.getProcessMemoryInfo(new int[]{pId});
				int memorySize=memoryinfos[0].getTotalSharedDirty();
				taskinfo.setMemorySize(memorySize);
				
				taskinfos.add(taskinfo);
				taskinfo=null;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				taskinfo = new TaskInfo();
				String packname = info.processName;
				taskinfo.setPackName(packname);
				taskinfo.setAppName(packname);
				Drawable appicon = context.getResources().getDrawable(R.drawable.ic_launcher);
				taskinfo.setAppIcon(appicon);
				int pid = info.pid;
				taskinfo.setpId(pid);
				taskinfo.setSystemApp(true);
				MemoryInfo[] memoryinfos = am.getProcessMemoryInfo(new int[]{pid});
				int memorysize = memoryinfos[0].getTotalPrivateDirty();
				taskinfo.setMemorySize(memorysize);	
				taskinfos.add(taskinfo);
				taskinfo = null;
			}
		}
		return taskinfos;
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
