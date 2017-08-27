package com.bit.password;

import com.aes.base64.AESMessage;
import com.aes.base64.BackAES;
import com.aes.base64.Password;
import com.bit.applock.MD5Encoder;
import com.wjg.phoneassistant.R;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PasswordAddActivity extends Activity {

	private LinearLayout layout;
	private Button btn_confirm;
	private EditText edt_account;
	private EditText edt_accountnumber;
	private EditText edt_accountpassword;
	private boolean isFromAdd = true;
	
	String wangzhan,password,account;
	String preWangzhan, prePassword, preAccount;
	sqlite a=new sqlite();
	DatabaseHelper	dbHelper = new DatabaseHelper(PasswordAddActivity.this,"accounts");
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_add);
        Intent it = getIntent();

        layout = (LinearLayout) findViewById(R.id.password_addlayout);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
		edt_account = (EditText) findViewById(R.id.edt_account);
		edt_accountnumber = (EditText) findViewById(R.id.edt_accountnumber);
		edt_accountpassword = (EditText) findViewById(R.id.edt_accountpassword);
		String[] item = new String[3];
		if((item=it.getStringArrayExtra("Item"))!=null) {
			isFromAdd = false;
			preWangzhan = item[0];
			preAccount = item[1];
			prePassword = item[2];
			edt_account.setText(item[0]);
			edt_accountnumber.setText(item[1]);
			edt_accountpassword.setText(item[2]);
			btn_confirm.setVisibility(View.GONE);
			edt_account.setOnFocusChangeListener(new EditFocusChangeListener());
			edt_accountnumber.setOnFocusChangeListener(new EditFocusChangeListener());
			edt_accountpassword.setOnFocusChangeListener(new EditFocusChangeListener());
		}
		
		btn_confirm.setOnClickListener(new ConfirmOnClickListener());
		
		layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				layout.requestFocus();
			}
		});
	}
    
    class EditFocusChangeListener implements OnFocusChangeListener {
    	@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if(hasFocus) {
				btn_confirm.setVisibility(View.VISIBLE);
			}
		}
    }
    
    class ConfirmOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			//String a= sd.getText().toString();
			
			wangzhan = edt_account.getText().toString().trim();
			account = edt_accountnumber.getText().toString().trim();
			password = edt_accountpassword.getText().toString().trim();
			
			if(wangzhan.equals("") && account.equals("")) {
				Toast.makeText(getApplicationContext(), "请填写账户信息", Toast.LENGTH_LONG).show();
				return;
			}
			
			ContentValues sa = new ContentValues();

			jiami();

			sa.put("password",password);
			sa.put("wangzhan",wangzhan);
			sa.put("account",account);
		
			a.Create(dbHelper);
			
			if(isFromAdd) {
				a.Insert(sa, dbHelper);//charu zhi
				Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_SHORT).show(); 
			}
			else {
				a.Update(sa, preWangzhan,preAccount, dbHelper);//gegnxin mima
				Toast.makeText(getApplicationContext(), "已保存", Toast.LENGTH_SHORT).show(); 
			}
			finish();
		}	
    }
	
    private void jiami() {

		/* 此处进行加密！！！
		 * 此处进行加密！！！
		 * 此处进行加密！！！
		 */
		try {
			byte[] encryptResultStr = BackAES.encrypt(password, Password.getKeyForPassword(), 0);
			   password = new String(encryptResultStr);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
    
}