package com.bit.applock;

import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

/**
 * 杀死所有进程
 * @author Administrator
 *
 */
public class TaskUtil {
	/**
	 * 杀死所有进程
	 * @author Administrator
	 *
	 */
	public static void killAllProcess(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		 List<RunningAppProcessInfo> runningApps=am.getRunningAppProcesses();
		 for (RunningAppProcessInfo info : runningApps) {
			String packname=info.processName;
			am.killBackgroundProcesses(packname);
		}
	}
	/**
	 * 杀死所有进程
	 * @author Administrator
	 *
	 */
	public static int getProcessCount(Context context){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		 List<RunningAppProcessInfo> runningApps=am.getRunningAppProcesses();
		 return runningApps.size();
	}
	
	/**
	 * 获取当前系统的剩余的可用内存信息 byte long
	 */
	public static String getMemeorySize(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo outInfo = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(outInfo);
		return TextFormater.getDataSize(outInfo.availMem);

	}
}
