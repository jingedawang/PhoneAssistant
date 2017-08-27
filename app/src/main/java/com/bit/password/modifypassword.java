package com.bit.password;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bit.applock.MD5Encoder;
import com.wjg.phoneassistant.R;

public class modifypassword extends Activity {

	private Button btn_confirm1;
	private Button btn_modifyPassword;
	private TextView t1;
	private EditText password11;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.modify);
	       
	        btn_confirm1 = (Button) findViewById(R.id.entry_button11);
	        btn_modifyPassword = (Button) findViewById(R.id.passwordbook_modifypassword);
	        password11 = (EditText) findViewById(R.id.entry_password11);
	        t1 = (TextView) findViewById(R.id.view11);
	        btn_confirm1.setOnClickListener(new ConfirmOnClickListener());
	        btn_modifyPassword.setOnClickListener(new OnClickListener() {
			
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					modifypassword.this.finish();
				}
			});
	      
	}
	
	class ConfirmOnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			String password;
			password = password11.getText().toString().trim();
			password=MD5Encoder.encode(password);
			SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
			{
				Editor editor = pref.edit();
				editor.putString("password", password);
				editor.commit();  	
				finish();
			}
		}
	}
}