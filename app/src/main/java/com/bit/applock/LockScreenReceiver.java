package com.bit.applock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
/**
 * 在屏幕锁屏的时候的广播接收器
 * @author Administrator
 *
 */
public class LockScreenReceiver extends BroadcastReceiver {

	private static final String TAG = "LockScreenReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG, "锁屏");
		//在屏幕锁屏的时候会调用这个onReceive方法
		SharedPreferences sp=context.getSharedPreferences("config", Context.MODE_PRIVATE);
		
		boolean killprocess=sp.getBoolean("killprocess", false);
		
		if(killprocess){
			TaskUtil.killAllProcess(context);
			Log.i(TAG, "杀死所有进程");
		}
		//TaskUtil.killAllProcess(context);

	}

}
