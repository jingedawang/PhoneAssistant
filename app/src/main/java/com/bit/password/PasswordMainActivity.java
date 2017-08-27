package com.bit.password;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aes.base64.AESMessage;
import com.aes.base64.BackAES;
import com.aes.base64.Password;
import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PasswordMainActivity extends Activity {

	DatabaseHelper	dbHelper = new DatabaseHelper(PasswordMainActivity.this,"accounts");
	sqlite as=new sqlite();
	sqlite aql=new sqlite();
	int id=0;
	SimpleAdapter adapter ;
	private List<Map<String, Object>> data;
	Map<String, Object> item;
	private ListView listView = null;
	private Button butn = null;
//	private Button butn1 = null;
	String wangzhan,account;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password_main);
		listView = (ListView) findViewById(R.id.ListView01);
		butn = (Button) findViewById(R.id.butn);
//		butn1 = (Button) findViewById(R.id.butn1);
		data = new ArrayList<Map<String, Object>>();
		listPackages();
		adapter = new SimpleAdapter(this, data, R.layout.password_listitem,
				new String[] { "wangzhan", "account", "password" }, new int[] {
						R.id.text1, R.id.text2, R.id.text3 });

		listView.setAdapter(adapter);
		butn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PasswordMainActivity.this,
						PasswordAddActivity.class);
				intent.putExtra("isFromAdd", true);
				startActivity(intent);
			}
		});

//		butn1.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(PasswordMainActivity.this,
//						modifypassword.class);
//
//				startActivity(intent);
//			}
//		});

		// 添加 单击事件
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Adapter adapter = arg0.getAdapter();
				Map<String, String> map = (Map<String, String>) adapter
						.getItem(arg2);
				Intent intent = new Intent(PasswordMainActivity.this,
						PasswordAddActivity.class);
				intent.putExtra("Item",
						new String[] { map.get("wangzhan"), map.get("account"),
								map.get("password") });
				startActivity(intent);
			}

		});

		// 添加 长按事件
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {

				int i = ((AdapterContextMenuInfo) menuInfo).position;
				wangzhan = data.get(i).get("wangzhan").toString();
				account = data.get(i).get("account").toString();

				menu.setHeaderTitle("操作");
				menu.add(0, 0, 0, "删除条目");
			}
		});

	}
	
	public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case 0:
                // 删除操作
        	as.Delete(dbHelper, wangzhan, account);
        	Intent intent =new Intent(PasswordMainActivity.this,PasswordMainActivity.class);
			startActivity(intent);
        	finish();
        	 Toast.makeText(PasswordMainActivity.this,
                     "已删除条目",
                     Toast.LENGTH_SHORT).show();
                break;

        default:
                break;
        }

        return super.onContextItemSelected(item);
}
	
	private void listPackages() { 

		ArrayList<sqlite.items> notes =aql.QueryAll(dbHelper);// getaccounts(); // false = no system packages 
		
		final int max = notes.size(); 
		for (int i=0; i<max; i++) { 
			
			item = new HashMap<String, Object>();
			item.put("account", notes.get(i).account);
			item.put("wangzhan", notes.get(i).wangzhan);

			try {
				
				String result = BackAES.decrypt(notes.get(i).password.toString(), Password.getKeyForPassword(), 0);
				notes.get(i).password=result;
				
			}
			catch (Exception e) {
				e.printStackTrace();
				
			}
			item.put("password", notes.get(i).password);
	
			data.add(item); 
		} 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.passwordbook_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.passwordmenu_modifypassword :
			Intent intent = new Intent(PasswordMainActivity.this, modifypassword.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		data = new ArrayList<Map<String, Object>>();
		listPackages();
		adapter = new SimpleAdapter(this, data,
				R.layout.password_listitem, new String[] {"wangzhan","account","password" }, new int[] {
			 R.id.text1 ,R.id.text2,R.id.text3});
		listView.setAdapter(adapter);
	} 
	
}
