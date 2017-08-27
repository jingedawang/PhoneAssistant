package com.wjg.phoneassistant;

import java.io.File;
import java.util.ArrayList;
import com.bit.guide.GuideActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

/*
 * EntryActivity类作为解锁界面，每次进入应用程序必须通过此解锁。
 * 解锁方式采用手势识别，当输入的手势与settings中已有的手势一致时，解锁成功，进入MainActivity类运行。
 */

public class EntryActivity extends Activity {
	
	private GestureLibrary library;
	private ImageButton btn_entry;
	private boolean closeFlag = false;
	private File file = new File("/data/data/com.wjg.phoneassistant/gestures");		//gesture手势库文件保存路径
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_entry);
		
		SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
		if(pref.getBoolean("isFirstIn", true)) {
			
			Intent intent = new Intent();
			intent.setClass(EntryActivity.this, GuideActivity.class);
			EntryActivity.this.startActivity(intent);
			closeFlag = true;
					
		}
		/*
		 * 每次进入系统都取出本机号码
		 */
		PrivacyData.setMyPhoneNumber(pref.getString("myPhoneNumber", ""));
		
		
		btn_entry = (ImageButton) findViewById(R.id.btn_entry);
		btn_entry.getBackground().setAlpha(0);
		btn_entry.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				/*
				 * 此处设计“进入”按钮的单击事件，直接产生异常退出应用程序。
				 */
				String[] str = new String[2];
				str[2] = "";
				
			}
		});
		
		library = GestureLibraries.fromFile(file);
		library.load();
		
		GestureOverlayView overlayView = (GestureOverlayView) this.findViewById(R.id.gestureOverlayView1);
		overlayView.addOnGesturePerformedListener(new GesturePerformedListener());
		overlayView.setGestureVisible(false);
		
	}
	
	private final class GesturePerformedListener implements OnGesturePerformedListener
	{

		@Override
		public void onGesturePerformed(GestureOverlayView overlay,
				Gesture gesture) {
			// TODO Auto-generated method stub
			ArrayList<Prediction> predictions = library.recognize(gesture);
			if(!predictions.isEmpty())
			{
				Prediction prediction = predictions.get(0);
				if(prediction.score >= 4)		//匹配度大于等于60%
				{
					if("登陆手势".equals(prediction.name.trim()))
					{
						Intent intent = new Intent();
						intent.setClass(EntryActivity.this, MainActivity.class);
						EntryActivity.this.startActivity(intent);
					}
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		/* 回到EntryActivity时重新读取gesture文件 */
		super.onResume();
		library = GestureLibraries.fromFile(file);
		library.load();
	}


	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
		EntryActivity.this.finish();		//不再返回到EntryActivity界面
	}

}
