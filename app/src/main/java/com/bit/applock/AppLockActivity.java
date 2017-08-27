package com.bit.applock;

import java.util.List;

import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppLockActivity extends Activity {
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(sp.getString("password", "").equals(""))
		{
			finish();
			Toast.makeText(AppLockActivity.this, "还没设置密码！", Toast.LENGTH_SHORT).show();
		}
		
		super.onResume();
	}

	public static AppLockActivity a=null;
	public static  boolean IsAbnormalExit=false;
	
	private ListView lv;
	private List<AppInfo> appInfos;
	private AppInfoProvider provider;
	private MyAppLockAdapter adapter;
	private AppLockDao dao;
	private LinearLayout ll_app_manager_loading;
	private SharedPreferences sp;
	private TextView set_password;
	
	private List<String> lockappinfos; //用来存放加载过的 加锁或未加锁的程序，来提高LIstView效率
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			ll_app_manager_loading.setVisibility(View.INVISIBLE);
			adapter = new MyAppLockAdapter();
			lv.setAdapter(adapter);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_lock);
		a=this;
		IsAbnormalExit=true;
		sp = this.getSharedPreferences("AppLockSetting", Context.MODE_PRIVATE);
		String realpwd=sp.getString("password","");
		if(realpwd.equals("")){
			Intent passIntent = new Intent(AppLockActivity.this,setPassWordActicity.class);
			
			startActivity(passIntent);	
		}

		provider = new AppInfoProvider(this);
		dao = new AppLockDao(this);
		lockappinfos = dao.getPackName();
		lv = (ListView) findViewById(R.id.lv_app_lock);
		ll_app_manager_loading=(LinearLayout) findViewById(R.id.ll_app_manager_loading);
		
		Intent startServiceIntent = new Intent(this,WatchDogService.class);
		startService(startServiceIntent);
		initUI();
		//设置密码
		set_password=(TextView) findViewById(R.id.set_password);
		set_password.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent passIntent = new Intent(AppLockActivity.this,setPassWordActicity.class);
				startActivity(passIntent);				
			}
		});
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//动画效果
				TranslateAnimation ta = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f);
				ta.setDuration(500);
				view.setAnimation(ta);
				ImageView iv=(ImageView) view.findViewById(R.id.iv_app_lock_status);
				
				//传递当前要锁定层序的包名
				AppInfo info=(AppInfo) lv.getItemAtPosition(position);
				String packName =info.getPackname();
				if(dao.find(packName)){  //如果在数据库内找到改包名 已经加锁
					//通过内容提供者删除
					getContentResolver().delete(Uri.parse("content://cn.wjg.applockprovider/delete"), null,new String[]{packName});
					lockappinfos.remove(packName);
					iv.setImageResource(R.drawable.unlock);
				}else{
					ContentValues values = new ContentValues();
					values.put("packname", packName);
					getContentResolver().insert(Uri.parse("content://cn.wjg.applockprovider/insert"), values);
					lockappinfos.add(packName);
					iv.setImageResource(R.drawable.lock);
				}

			}
		});
	}
	
	private void initUI() {
		ll_app_manager_loading.setVisibility(View.VISIBLE);
		new Thread() {
			@Override
			public void run() {
				appInfos = provider.getAllApps();
				handler.sendEmptyMessage(0);
			}

		}.start();
	}

	/**
	 * 适配器
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyAppLockAdapter extends BaseAdapter {

		public int getCount() {
			// TODO Auto-generated method stub
			return appInfos.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return appInfos.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView == null) {
				view = View.inflate(getApplicationContext(),
						R.layout.lock_app_item, null);
			} else {
				view = convertView;
			}
			//更改View对象的状态
			AppInfo info = appInfos.get(position);
			ImageView iv = (ImageView) view.findViewById(R.id.iv_app_icon);
			TextView tv = (TextView) view.findViewById(R.id.tv_app_name);
			ImageView iv_lock_statue=(ImageView) view.findViewById(R.id.iv_app_lock_status);
			
			if(lockappinfos.contains(info.getPackname())){
				iv_lock_statue.setImageResource(R.drawable.lock);
			}else{
				iv_lock_statue.setImageResource(R.drawable.unlock);
			}
			
			iv.setImageDrawable(info.getIcon());
			tv.setText(info.getAppname());
			return view;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
	}
}
