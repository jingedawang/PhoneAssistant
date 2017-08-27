package com.bit.password;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.renderscript.Type;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.bit.applock.MD5Encoder;
import com.wjg.phoneassistant.R;

public class entryactivity extends Activity{

	private Button btn_confirm;
	private Button btn_cancel;
	private EditText password1;
	private TextView t;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.password_entrylayout);
	        //Intent it = getIntent();
	       
	        btn_confirm = (Button) findViewById(R.id.entry_button1);
	        btn_cancel = (Button) findViewById(R.id.entry_btn_cancel);
	        password1 = (EditText) findViewById(R.id.entry_password1);
	        t = (TextView) findViewById(R.id.view1);
	        btn_confirm.setOnClickListener(new ConfirmOnClickListener());
	        btn_cancel.setOnClickListener(new CancelOnClickListener());
	        SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
	        if(pref.getString("password", "").equals(""))
	        {
	        	password1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
	        	if(Locale.getDefault().getLanguage().equals("zh")) {
	        		t.setText("第一次进入，请设置密码");
	        	}
	        	else {
	        		t.setText("Set a Password");
	        	}
	        }
	}

	 class ConfirmOnClickListener implements OnClickListener{

			@Override
			public void onClick(View v) {
				//String a= sd.getText().toString();
				String password;
				password = password1.getText().toString().trim();
				if(password.equals("")) {
	        		Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_SHORT).show();
	        		return;
	        	}
				password=MD5Encoder.encode(password);
				SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
		        String pas=pref.getString("password", "");
		        if(pas.equals(""))
		        {
		        	Editor editor = pref.edit();
		        	editor.putString("password", password);
		        	editor.commit();
		        	Intent in=new Intent(entryactivity.this,PasswordMainActivity.class);
		        	startActivity(in);
		        	finish();
		        }
		        else if(pas.equals(password))
		        {
		        	Intent in=new Intent(entryactivity.this,PasswordMainActivity.class);
		        	startActivity(in);
		        	finish();
		        }
		        else{
		        	Toast.makeText(entryactivity.this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
		        }
			
			}	
	    }

	 class  CancelOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			entryactivity.this.finish();
		}
		 
	 }
}
