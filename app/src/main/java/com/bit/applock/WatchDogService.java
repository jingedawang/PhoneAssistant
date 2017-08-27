package com.bit.applock;

import java.util.ArrayList;
import java.util.List;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * 看门狗实现 (监控用户打开的程序)
 * 
 * @author Administrator
 * 
 */
public class WatchDogService extends Service {
	private AppLockDao dao;
	private List<String> lockapps;
	private ActivityManager am;
	private Intent lockappintent;
	private boolean flag;
	private MyBinder binder;
	private List<String> tempstopapps;
	private KeyguardManager keyguardManager;
	
	@Override
	public IBinder onBind(Intent intent) {

		return binder;
	}

	public class MyBinder extends Binder implements IService {

		public void callAppProtectStart(String packname) {
			// TODO Auto-generated method stub
			AppProtectStart(packname);
		}

		public void callAppProtectStop(String packname) {
			// TODO Auto-generated method stub
			AppProtectStop(packname);
		}

	}

	/**
	 * 重新开启对应用的保护
	 * 
	 * @param packname
	 */
	public void AppProtectStart(String packname) {
		if (tempstopapps.contains(packname)) {
			tempstopapps.remove(packname);
		}
	}

	/**
	 * 临时停止对app的保护
	 * 
	 * @param packname
	 */
	public void AppProtectStop(String packname) {
		tempstopapps.add(packname);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	//	System.out.println("开启服务 ");
		getContentResolver().registerContentObserver(
				Uri.parse("content://cn.wjg.applockprovider"), true,
				new MyObserver(new Handler()));
	
		// keyguardManager.inKeyguardRestrictedInputMode();

		dao = new AppLockDao(this);
		binder = new MyBinder();
		tempstopapps = new ArrayList<String>();
		flag = true;

		lockappintent = new Intent(this, LockScreenActivity.class);
		// 服务是不存在任务栈的，要在服务里开启Activity的话必须添加一个flag
		lockappintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
	
		
		lockapps = dao.getPackName();
		am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		new Thread() {

			@Override
			public void run() {
				// 开启看门狗
				while (flag) {
					try {
						keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
						
						// 判断屏幕是否为锁屏状态 如果是锁屏状态
						if (keyguardManager.inKeyguardRestrictedInputMode()) {
							// 清空临时的集合
							tempstopapps.clear();
							
						}

						// 得到当前运行程序的包名
						// 返回系统里面任务栈的信息，现在taskinfo的集合里只有一个元素
						// 内容是当前正在运行进程对应的任务栈
						List<RunningTaskInfo> taskinfos = am.getRunningTasks(1);
						RunningTaskInfo currenttask = taskinfos.get(0);
						// 获取当前用户可见的Activity 的包名
						String packname = currenttask.topActivity
								.getPackageName();
						
						if (lockapps.contains(packname)) { // 如果数据库内存在这个包名，证明需要锁定
				//			System.out.println("检测到有此包名 ");
							// 如果当前的应用程序需要临时的被终止保护
							if (tempstopapps.contains(packname)) {
								sleep(1000);
								continue;
							}

							// 弹出来一个锁定的界面，让用户输入密码
							lockappintent.putExtra("packname", packname);
							lockappintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(lockappintent);
						
						} else {
							// 放行
						}
						sleep(1000);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}

		}.start();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		flag = false;
	}

	private class MyObserver extends ContentObserver {

		public MyObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		// 当数据发生改变的时候调用
		@Override
		public void onChange(boolean selfChange) {

			super.onChange(selfChange);
			// 重新更新lockapps里面的内容
			Log.i("change", "-------------数据库内容变化了");
			lockapps = dao.getPackName();
		}

	}
}
