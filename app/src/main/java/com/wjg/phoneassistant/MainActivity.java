package com.wjg.phoneassistant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.bit.applock.AppLockActivity;
import com.bit.contacts.ContactsManager;
import com.bit.gallery.Grallery3DActivity;
import com.bit.gesturebuilder.GestureBuilderActivity;
import com.bit.password.entryactivity;
import com.watchdata.mysms.SMSMainActivity;
//provider
public class MainActivity extends Activity {
	
	private GridView gv_main = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		//设置TextView中的文本使用“幼圆”字体
		TextView txt_main = (TextView)findViewById(R.id.txt_main);  
		Typeface face = Typeface.createFromAsset (getAssets(), "SIMYOU.TTF");
		txt_main.setTypeface (face);
		
		gv_main=(GridView)findViewById(R.id.gv_main);
		gv_main.setAdapter(new MainAdapter(this));
		
		gv_main.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case 0:
					//通讯录
					//跳转到通讯录界面
					Intent intent0 = new Intent();
					intent0.setClass(MainActivity.this, ContactsManager.class);
					MainActivity.this.startActivity(intent0);
					break;
				case 1:
					//短信
					//跳转到短信列表
					Intent intent1 = new Intent();
					intent1.setClass(MainActivity.this, SMSMainActivity.class);
					MainActivity.this.startActivity(intent1);
					break;
				case 2:
					//密码本
					//跳转到密码本界面
					Intent intent2 = new Intent();
					intent2.setClass(MainActivity.this, entryactivity.class);
					MainActivity.this.startActivity(intent2);
					break;
				case 3:
					//程序锁
					//跳转到程序锁界面
					Intent intent3 = new Intent();
					intent3.setClass(MainActivity.this, AppLockActivity.class);
					MainActivity.this.startActivity(intent3);
					break;
	/*			case 4:
					//相册
					//跳转到相册列表
					Intent intent4= new Intent();
					intent4.setClass(MainActivity.this, Grallery3DActivity.class);
					MainActivity.this.startActivity(intent4);
					break;
	*/
				case 4:
					//设置
					//跳转到设置
					Intent intent5 = new Intent();
					intent5.setClass(MainActivity.this, GestureBuilderActivity.class);
					MainActivity.this.startActivity(intent5);
					break;
				}
			}
			
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
	/*
		case R.id.item1:
			Intent intent1 = new Intent(this, com.qiniu.demo.MyActivity.class);
			startActivity(intent1);
			break;
	*/
		case R.id.item2:
			Intent intent2 = new Intent(this, HelpActivity.class);
			startActivity(intent2);
			break;
		case R.id.item3:
			Intent intent3 = new Intent(this,AboutActivity.class);
			startActivity(intent3);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
