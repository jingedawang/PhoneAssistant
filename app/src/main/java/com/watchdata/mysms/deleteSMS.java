package com.watchdata.mysms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;

import com.wjg.phoneassistant.R;

public class deleteSMS extends Activity {

	private ListView list_deleteSMS_ShowData;
	private Button btndelete;
	private Button btncancel;
	private MyAdapter myAdapter;
	private ArrayList<HashMap<String, String>> arrayList;

	// 会话
	private static final String CONVERSATIONS = "content://sms/conversations/";
	// 查询联系人
	private static final String CONTACTS_LOOKUP = "content://com.android.contacts/phone_lookup/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete_sms);
		list_deleteSMS_ShowData = (ListView) findViewById(R.id.deleteSMS_ShowData);
		btndelete = (Button) findViewById(R.id.delete);
		btncancel = (Button) findViewById(R.id.cancel);

		arrayList = new ArrayList<HashMap<String, String>>();

		/**
		 * 为Adapter准备数据
		 */
		Uri uri = Uri.parse(CONVERSATIONS);
		String[] projection = new String[] { "groups.group_thread_id AS _id",
				"groups.msg_count AS msg_count",
				"groups.group_date AS last_date", "sms.body AS last_msg",
				"sms.address AS contact" };
		Cursor cursor_delete = getContentResolver().query(uri, projection,
				null, null, "last_date DESC");

		// 对cursor进行处理，遇到号码后获取对应的联系人名称
		Cursor cursor_delete_qcontact = new CursorWrapper(cursor_delete) {
			public String getString(int columnIndex) {
				if (super.getColumnIndex("contact") == columnIndex) {
					String contact = super.getString(columnIndex);
					// 读取联系人，查询对应的名称
					Uri uri_qcontact = Uri.parse(CONTACTS_LOOKUP + contact);
					Cursor cur = getContentResolver().query(uri_qcontact, null,
							null, null, null);
					if (cur.moveToFirst()) {
						String contactName = cur.getString(cur
								.getColumnIndex("display_name"));
						return contactName;
					}
					return contact;
				}
				return super.getString(columnIndex);
			}
		};

		if (cursor_delete_qcontact.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				String who = cursor_delete_qcontact
						.getString(cursor_delete_qcontact
								.getColumnIndex("contact"));
				String content = cursor_delete_qcontact
						.getString(cursor_delete_qcontact
								.getColumnIndex("last_msg"));
				int threadId = cursor_delete_qcontact
						.getInt(cursor_delete_qcontact.getColumnIndex("_id"));
				map.put("who", who);
				map.put("content", content);
				map.put("flag", "false");
				map.put("threadId", Integer.toString(threadId));
				arrayList.add(map);
			} while (cursor_delete_qcontact.moveToNext());
		}

		// 实例化自定义的MyAdapter
		myAdapter = new MyAdapter(arrayList, this);

		// 绑定MyAdapter
		list_deleteSMS_ShowData.setAdapter(myAdapter);

		// 绑定listview的监听器
		list_deleteSMS_ShowData
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 取得ViewHolder对象，这样就省去了通过层层的indViewById去实例化我们需要的CheckBox实例的步骤
						ViewHolder holder = (ViewHolder) view.getTag();

						// 改变CheckBox的状态
						holder.choose_deleteSMS.toggle();
						if (holder.choose_deleteSMS.isChecked() == true) {
							arrayList.get(position).put("flag", "true");
						} else {
							arrayList.get(position).put("flag", "false");
						}
					}
				});

		// 确认删除
		btndelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {	
				
				/**
				 * 删除数据库中信息
				 */
				for (int i = 0; i < arrayList.size(); i++) {
					if (arrayList.get(i).get("flag").equals("true")) {																		
						//得到短信会话thread_id
						int threadId = Integer.parseInt(arrayList.get(i).get(
								"threadId"));
						deleteSMS.this.getContentResolver().delete(
								Uri.parse("content://sms/conversations/"
										+ threadId), null, null);
					}
				}
				
				
				/**
				 * 删除list数组中信息
				 */
				Iterator<HashMap<String, String>> iterator=arrayList.iterator();
				while(iterator.hasNext()){
					HashMap<String, String> temp=iterator.next();
					if(temp.get("flag").equals("true")){
						iterator.remove();
					}
				}

				// 刷新listview和TextView的显示
				dataChanged();
			}
		});

		//确认取消
		btncancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				deleteSMS.this.finish();
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
				convertView = inflater.inflate(R.layout.delete_sms_item, null);
				holder.smsWho_deleteSMS = (TextView) convertView
						.findViewById(R.id.smsWho_deleteSMS);
				holder.smsContent_deleteSMS = (TextView) convertView
						.findViewById(R.id.smsContent_deleteSMS);
				holder.choose_deleteSMS = (CheckBox) convertView
						.findViewById(R.id.choose_deleteSMS);

				// 为view设置标签
				convertView.setTag(holder);
			} else {
				// 取出holder
				holder = (ViewHolder) convertView.getTag();
			}

			// 设置list中TextView的显示
			holder.smsWho_deleteSMS.setText(list.get(position).get("who")
					.toString());
			holder.smsContent_deleteSMS.setText(list.get(position)
					.get("content").toString());
			// 根据flag来设置checkbox的选中状况
			holder.choose_deleteSMS.setChecked(list.get(position).get("flag")
					.equals("true"));
			return convertView;
		}

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
		case 1: //全选
			for(int i=0;i<arrayList.size();i++){
				arrayList.get(i).put("flag", "true");	
			}
			// 刷新listview和TextView的显示
			dataChanged();
			break;

		case 2: //反选
			for(int i=0;i<arrayList.size();i++){
				if(arrayList.get(i).get("flag").equals("true")){
					arrayList.get(i).put("flag", "false");
				}else {
					arrayList.get(i).put("flag", "true");
				}
			}
			// 刷新listview和TextView的显示
			dataChanged();
			break;

		case 3:// 取消选择
			for(int i=0;i<arrayList.size();i++){
				if(arrayList.get(i).get("flag").equals("true")){
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
	
	final class ViewHolder {
		TextView smsWho_deleteSMS;
		TextView smsContent_deleteSMS;
		CheckBox choose_deleteSMS;
		int threadId;
	}

	// 刷新listview和TextView的显示
	private void dataChanged() {
		// 通知listView刷新
		myAdapter.notifyDataSetChanged();
	}

}