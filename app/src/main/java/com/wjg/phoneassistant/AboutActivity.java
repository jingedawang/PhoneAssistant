package com.wjg.phoneassistant;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class AboutActivity extends Activity {
	public TextView TextView1=null;
	public TextView TextView2=null;
	public TextView TextView3=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView1=(TextView)findViewById(R.id.textView1);
		TextView1.setText("安卓手机隐私助手 V1.0");
		TextView2=(TextView)findViewById(R.id.textView2);
		TextView2.setText("北京理工大学");
		TextView3=(TextView)findViewById(R.id.textView3);
		TextView3.setText("邱煌彬   王金戈   郝以平  制作");
	}


}
