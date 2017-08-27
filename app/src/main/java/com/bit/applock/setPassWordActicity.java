package com.bit.applock;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bit.password.entryactivity;
import com.watchdata.mysms.SMSMainActivity;
import com.wjg.phoneassistant.R;

public class setPassWordActicity extends Activity{

	private Button bt_normalt_dialog_ok,bt_normal_dialog_cancle;
	private EditText et_normal_entry_pwd;
	private TextView te;
	private SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setpass);

		sp=getSharedPreferences("AppLockSetting",Context.MODE_PRIVATE);
		te=(TextView) findViewById(R.id.te);
		bt_normalt_dialog_ok=(Button) findViewById(R.id.bt_normalt_dialog_ok);
		bt_normal_dialog_cancle=(Button) findViewById(R.id.bt_normal_dialog_cancle);
		et_normal_entry_pwd=(EditText) findViewById(R.id.et_normal_entry_pwd);
		
		if(sp.getString("password", "").equals(""))
		{
			if(Locale.getDefault().getLanguage().equals("zh")) {
				te.setText("请设置密码：");
			}
			else {
				te.setText("Enter a Password");
			}	
		}
		bt_normalt_dialog_ok.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String password=et_normal_entry_pwd.getText().toString().trim();
				
				
				if(!password.equals(""))
				{
				
				if(sp.getString("password", "").equals(""))
				{
					Intent passIntent = new Intent(setPassWordActicity.this,AppLockActivity.class);
					startActivity(passIntent);		
				}
				password=MD5Encoder.encode(password);
				Editor ed = sp.edit();
				ed.putString("password", password);
				ed.commit();
				setPassWordActicity.this.finish();
				
				}
				else
				{
					Toast.makeText(setPassWordActicity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
				}
						
			}
		});
		
		bt_normal_dialog_cancle.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
					finish();	
			}
		});
	
		
	}
}
