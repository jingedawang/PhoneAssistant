package com.watchdata.mysms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.watchdata.mysms.deleteSMS.MyAdapter;
import com.watchdata.mysms.deleteSMS.ViewHolder;
import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class draft_delete_SMS extends Activity {
	private ListView draft_deleteSMS_ShowData;
	private Button draft_delete;
	private Button draft_cancel;
	private MyAdapter myAdapter;
	private ArrayList<HashMap<String, String>> arrayList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.draft_delete_sms);
		draft_deleteSMS_ShowData = (ListView) findViewById(R.id.draft_deleteSMS_ShowData);
		draft_delete = (Button) findViewById(R.id.draft_delete);
		draft_cancel = (Button) findViewById(R.id.draft_cancel);

		arrayList = new ArrayList<HashMap<String, String>>();

		/**
		 * 为Adapter准备数据
		 */
		Uri uri = Uri.parse("content://sms/draft");
		String[] projection = { "_id", "thread_id", "address", "body", "date",
				"type" };

		Cursor cur_draft_delete_SMS = getContentResolver().query(uri,
				projection, null, null, "date DESC"); // 查询并按日期倒序

		if (cur_draft_delete_SMS.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				String who = cur_draft_delete_SMS
						.getString(cur_draft_delete_SMS
								.getColumnIndex("address"));
				String content = cur_draft_delete_SMS
						.getString(cur_draft_delete_SMS.getColumnIndex("body"));
				int Id = cur_draft_delete_SMS.getInt(cur_draft_delete_SMS
						.getColumnIndex("_id"));
				map.put("who", who);
				map.put("content", content);
				map.put("flag", "false");
				map.put("Id", Integer.toString(Id));
				arrayList.add(map);
			} while (cur_draft_delete_SMS.moveToNext());
		}

		// 实例化自定义的MyAdapter
		myAdapter = new MyAdapter(arrayList, this);

		// 绑定MyAdapter
		draft_deleteSMS_ShowData.setAdapter(myAdapter);

		// 绑定listview的监听器
		draft_deleteSMS_ShowData
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 取得ViewHolder对象，这样就省去了通过层层的indViewById去实例化我们需要的CheckBox实例的步骤
						ViewHolder holder = (ViewHolder) view.getTag();

						// 改变CheckBox的状态
						holder.choose_draft_deleteSMS.toggle();
						if (holder.choose_draft_deleteSMS.isChecked() == true) {
							arrayList.get(position).put("flag", "true");
						} else {
							arrayList.get(position).put("flag", "false");
						}
					}
				});

		// 确认删除
		draft_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				/**
				 * 删除数据库中信息
				 */
				for (int i = 0; i < arrayList.size(); i++) {
					if (arrayList.get(i).get("flag").equals("true")) {
						// 得到短信会话thread_id
						int Id = Integer.parseInt(arrayList.get(i).get(
								"Id"));
						draft_delete_SMS.this.getContentResolver().delete(
								Uri.parse("content://sms/"
										+ Id), null, null);
					}
				}

				/**
				 * 删除list数组中信息
				 */
				Iterator<HashMap<String, String>> iterator = arrayList
						.iterator();
				while (iterator.hasNext()) {
					HashMap<String, String> temp = iterator.next();
					if (temp.get("flag").equals("true")) {
						iterator.remove();
					}
				}

				// 刷新listview和TextView的显示
				dataChanged();
			}
		});

		// 确认取消
		draft_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(draft_delete_SMS.this, draftSMS.class);
				startActivity(intent);
			}
		});

	}

	public class MyAdapter extends BaseAdapter {

		// 填充数据的list
		private ArrayList<HashMap<String, String>> list;

		// 上下文
		private Context context;

		// 用来导入布局
		private LayoutInflater inflater = null;

		// 构造器
		public MyAdapter(ArrayList<HashMap<String, String>> list,
				Context context) {
			this.context = context;
			this.list = list;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				// 获得ViewHolder对象
				holder = new ViewHolder();

				// 导入布局赋值给convertView
				convertView = inflater.inflate(R.layout.draft_delete_sms_item,
						null);
				holder.smsWho_draft_deleteSMS = (TextView) convertView
						.findViewById(R.id.smsWho_draft_deleteSMS);
				holder.smsContent_draft_deleteSMS = (TextView) convertView
						.findViewById(R.id.smsContent_draft_deleteSMS);
				holder.choose_draft_deleteSMS = (CheckBox) convertView
						.findViewById(R.id.choose_draft_deleteSMS);

				// 为view设置标签
				convertView.setTag(holder);
			} else {
				// 取出holder
				holder = (ViewHolder) convertView.getTag();
			}

			// 设置list中TextView的显示
			holder.smsWho_draft_deleteSMS.setText(list.get(position).get("who")
					.toString());
			holder.smsContent_draft_deleteSMS.setText(list.get(position)
					.get("content").toString());
			// 根据flag来设置checkbox的选中状况
			holder.choose_draft_deleteSMS.setChecked(list.get(position)
					.get("flag").equals("true"));
			return convertView;
		}

	}

	final class ViewHolder {
		TextView smsWho_draft_deleteSMS;
		TextView smsContent_draft_deleteSMS;
		CheckBox choose_draft_deleteSMS;
		int Id;
	}

	// 刷新listview和TextView的显示
	private void dataChanged() {
		// 通知listView刷新
		myAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, "全选");
		menu.add(0, 2, 2, "反选");
		menu.add(0, 3, 3, "取消选择");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: // 全选
			for (int i = 0; i < arrayList.size(); i++) {
				arrayList.get(i).put("flag", "true");
			}
			// 刷新listview和TextView的显示
			dataChanged();
			break;

		case 2: // 反选
			for (int i = 0; i < arrayList.size(); i++) {
				if (arrayList.get(i).get("flag").equals("true")) {
					arrayList.get(i).put("flag", "false");
				} else {
					arrayList.get(i).put("flag", "true");
				}
			}
			// 刷新listview和TextView的显示
			dataChanged();
			break;

		case 3:// 取消选择
			for (int i = 0; i < arrayList.size(); i++) {
				if (arrayList.get(i).get("flag").equals("true")) {
					arrayList.get(i).put("flag", "false");
				}
			}
			// 刷新listview和TextView的显示
			dataChanged();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
