package com.wjg.phoneassistant;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.bit.gesturebuilder.GestureBuilderActivity;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		Button gotoSettings = (Button) findViewById(R.id.goto_settings);
		gotoSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(WelcomeActivity.this, GestureBuilderActivity.class);
				intent.putExtra("isFromWelcome", true);
				startActivity(intent);
				
			}
		});
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK) {
	        /* 在GestureBuilderActivity销毁前判断上一个Activity是不是Welcome，如果是，需要新建一个Intent用来转移到MainActivity */
			SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
	       if(!pref.getBoolean("isFirstIn", true)){
	        	Intent nextIntent = new Intent();
	        	nextIntent.setClass(WelcomeActivity.this, EntryActivity.class);
	        	startActivity(nextIntent);
	        	Toast.makeText(WelcomeActivity.this, "请输入设置的手势", Toast.LENGTH_SHORT).show();
	       }
		}
		finish();
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
	    if(!pref.getBoolean("isFirstIn", true)) {
	    	Intent nextIntent = new Intent();
	    	nextIntent.setClass(WelcomeActivity.this, EntryActivity.class);
	    	startActivity(nextIntent);
	    	Toast.makeText(WelcomeActivity.this, "请输入设置的手势", Toast.LENGTH_SHORT).show();
	    }
		WelcomeActivity.this.finish();
		
	}

	
	
}
