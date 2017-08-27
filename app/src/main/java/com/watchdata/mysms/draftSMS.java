package com.watchdata.mysms;

import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class draftSMS extends Activity {

	private ListView list_draftSMS;
	Cursor cur_drafeSMS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.draft_sms);
		list_draftSMS = (ListView) findViewById(R.id.list_draftSMS);

		// 为列表注册上下文菜单
		this.registerForContextMenu(list_draftSMS);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		show_draftSMS_list();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void show_draftSMS_list() {
		Uri uri = Uri.parse("content://sms/draft");

		String[] projection = { "_id", "thread_id", "address", "body", "date",
				"type" };

		cur_drafeSMS = getContentResolver().query(uri, projection, null, null,
				"date DESC"); // 查询并按日期倒序

		@SuppressWarnings("deprecation")
		SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,
				R.layout.draft_sms_item, cur_drafeSMS, new String[] {
						"address", "body" }, new int[] { R.id.draft_who,
						R.id.draft_content }) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				View view = super.getView(position, convertView, parent);

				String draft_sms_phone = cur_drafeSMS.getString(cur_drafeSMS
						.getColumnIndex("address"));

				// 加载具体一条草稿显示视图
				View container = view.findViewById(R.id.show_draftSMS);

				// 获取显示界面上的文本框
				TextView draft_who = (TextView) (view
						.findViewById(R.id.draft_who));

				if (draft_sms_phone.equals("")) {
					draft_who.setText("匿名");
				} else {
					draft_who.setText(draft_sms_phone);
				}
				return view;
			}
		};

		list_draftSMS.setAdapter(simpleCursorAdapter);

		// 点击进入发短信界面，并传入草稿内容
		list_draftSMS.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				Log.v("danji", "youfanying");
				Intent intent = new Intent(draftSMS.this,
						draft_forwardSMS.class);
				Bundle bundle = new Bundle();

				int draft_sms_id = cur_drafeSMS.getInt(cur_drafeSMS
						.getColumnIndex("_id"));

				bundle.putInt("draft_sms_id", draft_sms_id);
				intent.putExtras(bundle);
				startActivity(intent);
				
//				draftSMS.this.getContentResolver().delete(
//						Uri.parse("content://sms/" + draft_sms_id), null, null);

			}
		});
	}

	// 长按草稿激活上下文菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.v("changan", "youfanying");

		menu.setHeaderTitle("信息选项");
		menu.add(0, 1, 0, "查看");
		menu.add(0, 2, 0, "删除");

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	// 点击上下文菜单响应
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int draft_sms_id = (int) menuInfo.id;

		switch (item.getItemId()) {
		case 1:
			// 查看，此处和上面单击相同
			Intent intent = new Intent(draftSMS.this, draft_forwardSMS.class);
			Bundle bundle = new Bundle();

			draft_sms_id = cur_drafeSMS.getInt(cur_drafeSMS
					.getColumnIndex("_id"));

			bundle.putInt("draft_sms_id", draft_sms_id);
			intent.putExtras(bundle);

			startActivity(intent);
			break;

		case 2: // 删除草稿
			this.getContentResolver().delete(
					Uri.parse("content://sms/" + draft_sms_id), null, null);
			break;

		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	// 添加menu菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "删除草稿");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent = new Intent(draftSMS.this, draft_delete_SMS.class);
		startActivity(intent);
		return super.onOptionsItemSelected(item);
	}

}
