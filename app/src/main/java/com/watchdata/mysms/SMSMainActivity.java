package com.watchdata.mysms;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.aes.base64.AESMessage;
import com.aes.base64.BackAES;
import com.aes.base64.Password;
import com.bit.contacts.ContactsManagerDbAdater;
import com.wjg.phoneassistant.PrivacyData;
import com.wjg.phoneassistant.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SMSMainActivity extends Activity {
	// 会话
	private static final String CONVERSATIONS = "content://sms/conversations/";

	// 查询联系人
	private static final String CONTACTS_LOOKUP = "content://com.android.contacts/phone_lookup/";

	private LAdaptor simpleCursorAdapter = null;
	private ListView list;
	private Button newSMS;
	private Cursor cursor_smslist;
	private Cursor cursor_smslist_qcontact;
	private ContactsManagerDbAdater contactsManagerDbAdapter;
	private boolean dialogFlag = false;

	// 来信息监听刷新
	class SmsContent extends ContentObserver {
		public SmsContent(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			showSMSList();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sms_main);
		list = (ListView) findViewById(R.id.mylist1);
		newSMS = (Button) findViewById(R.id.newSMS);
		
		SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
		if(pref.getString("myPhoneNumber", "").equals("")) {
			Intent intent = new Intent();
			intent.setClass(SMSMainActivity.this, setPhoneNumberActicity.class);
			startActivity(intent);
		}
		else {
			PrivacyData.setMyPhoneNumber(pref.getString("myPhoneNumber", ""));
		}

		//打开数据库
		contactsManagerDbAdapter = new ContactsManagerDbAdater(this);
		contactsManagerDbAdapter.open();

		newSMS.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 跳转到发短信事件
				Intent intent = new Intent();
				intent.setClass(SMSMainActivity.this, sendSMS.class);
				SMSMainActivity.this.startActivity(intent);

			}
		});

		// 注册短信变化监听
		this.getContentResolver().registerContentObserver(
				Uri.parse("content://sms/"), true,
				new SmsContent(new Handler()));
		// 为列表注册上下文菜单
		this.registerForContextMenu(list);
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(getSharedPreferences("preferences", MODE_PRIVATE).getString("myPhoneNumber", "").equals("")) {
			//myPhoneNumber为空，强制退出短信界面
			finish();
		}
		showSMSList();
	}

	private void showSMSList() {
		//读取会话信息
		Uri uri = Uri.parse(CONVERSATIONS);

		String[] projection = new String[] {
				"groups.group_thread_id AS _id",
				"groups.msg_count AS msg_count",
				"groups.group_date AS last_date", "sms.body AS last_msg",
				"sms.address AS contact"};
		
		cursor_smslist = getContentResolver().query(uri, projection, null,
				null, "last_date DESC"); // 查询并按日期倒序
		
		// 对cursor进行处理，遇到号码后获取对应的联系人名称
		cursor_smslist_qcontact = new CursorWrapper(cursor_smslist) {
				
			@Override
			public String getString(int columnIndex) {
				if (super.getColumnIndex("contact") == columnIndex) {
					String contact = super.getString(columnIndex);
					// 读取联系人，查询对应的名称
					Uri uri_qcontact = Uri.parse(CONTACTS_LOOKUP + contact);
					Cursor cur = getContentResolver().query(uri_qcontact, null,
							null, null, null);
					if(contact != null && contact.length() > 0) {
						Cursor cur2 = contactsManagerDbAdapter.getCursorBySql(ContactsManagerDbAdater.TABLE_CONTACTS,
								new String[]{"telPhone","name"}, "telPhone=?", new String[]{contact});
						if(cur2.moveToFirst()) {
							String contactName = cur2.getString(cur2
									.getColumnIndex("name"));
							return contactName;
						}
					}
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
		
		simpleCursorAdapter = new LAdaptor(this, R.layout.sms_main_item,
				cursor_smslist_qcontact, new String[] { "contact", "last_date",
						"last_msg", "msg_count" }, new int[] { R.id.name,
						R.id.datatime, R.id.messagebody, R.id.smsCount });
		list.setAdapter(simpleCursorAdapter);

		// 点击显示同一会话所有短信
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				Intent intent = new Intent(SMSMainActivity.this, showSMS.class); // 描述起点和目标
				Bundle bundle = new Bundle(); // 创建Bundle对象

				int index_thread_id = cursor_smslist.getColumnIndex("_id");
				int threadId = cursor_smslist.getInt(index_thread_id);

				bundle.putInt("threadId", threadId); // 装入数据
				intent.putExtras(bundle); // 把Bundle塞入Intent里面
				SMSMainActivity.this.startActivity(intent); // 开始切换
				
			}
		});
	}

	// 长按短信会话条目激活上下文菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
	
		String contactName=cursor_smslist_qcontact.getString(cursor_smslist_qcontact.getColumnIndex("contact"));
		Log.v("Ling", "contactName=" + contactName);
		menu.setHeaderTitle("信息选项");// 标题
		// 添加菜单项
		menu.add(0, 1, 0, "查看");
		menu.add(0, 2, 0, "回复");
		menu.add(0, 3, 0, "转发");
		menu.add(0, 4, 0, "呼叫");
		menu.add(0, 5, 0, "删除会话");
		// menu.add(0, 6, 0, "保存至联系人");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	// 点击短信会话上下文菜单响应
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int position = menuInfo.position;
		int item_threadId = (int) menuInfo.id;

		Log.v("Ling", "sms_list context item seleted ID=" + menuInfo.id);
		Log.v("Ling", "sms_list context item seleted position=" + menuInfo.position);
		// 获得电话号码
		int index_phone = cursor_smslist.getColumnIndex("contact");
		String phone = cursor_smslist.getString(index_phone);

		switch (item.getItemId()) {
		case 1: // 查看短信会话
			Intent intent_check_sms = new Intent(SMSMainActivity.this,
					showSMS.class); // 描述起点和目标
			Bundle bundle_check_sms = new Bundle(); // 创建Bundle对象

			bundle_check_sms.putInt("threadId", item_threadId); // 装入数据
			intent_check_sms.putExtras(bundle_check_sms); // 把Bundle塞入Intent里面
			SMSMainActivity.this.startActivity(intent_check_sms); // 开始切换
			break;
		case 2: // 回复短信
			Intent intent_receive_sms = new Intent(SMSMainActivity.this,
					showSMS.class); // 描述起点和目标
			Bundle bundle_receive_sms = new Bundle(); // 创建Bundle对象
			bundle_receive_sms.putInt("threadId", item_threadId); // 装入数据
			intent_receive_sms.putExtras(bundle_receive_sms); // 把Bundle塞入Intent里面
			this.startActivity(intent_receive_sms); // 开始切换
			break;
						
		case 3: // 转发短信会话最新一条短信
			Intent intent_forward_sms = new Intent(SMSMainActivity.this,
					sendSMS.class);
			Bundle bundle_forward_sms = new Bundle();
			int index_last_sms_body = cursor_smslist.getColumnIndex("last_msg");
			String last_sms_body = cursor_smslist.getString(index_last_sms_body);
			//对于加密过的短信，转发时需要先解密
			String jiemi_str = jiemi2(last_sms_body, phone);
			last_sms_body = jiemi_str == null?last_sms_body:jiemi_str;
			bundle_forward_sms.putString("last_sms_body", last_sms_body);
			intent_forward_sms.putExtras(bundle_forward_sms);
			SMSMainActivity.this.startActivity(intent_forward_sms);
			break;
		case 4: // 呼叫

			// 用intent启动拨打电话
			Intent sms_call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
					+ phone));
			//startActivity(sms_call);
			startActivityForResult(sms_call,0);
			break;
		case 5: // 删除短信会话
			this.getContentResolver().delete(
					Uri.parse("content://sms/conversations/" + item_threadId),
					null, null);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
	
	private String jiemi2(String message, String toMobile) {
		try {
			if(AESMessage.authentication(AESMessage.getMac(message.substring(16), AESMessage.getDynKeyForMac(toMobile, PrivacyData.getMyPhoneNumber())), message)) {
				return AESMessage.getMsg(message, AESMessage.getDynKey(toMobile, PrivacyData.getMyPhoneNumber()));
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getApplicationContext(), "转发时解密有误", Toast.LENGTH_SHORT);
		}
		return null;
	}

	//点击出现menu菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, getResources().getString(R.string.delete)).setIcon(android.R.drawable.ic_menu_delete);
//		menu.add(0, 2, 2, "草稿").setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, 3, 3, getResources().getString(R.string.newmessage)).setIcon(android.R.drawable.ic_menu_add);
//		menu.add(0, 4, 4, "设置").setIcon(android.R.drawable.ic_menu_preferences);
//		menu.add(0, 5, 5, "查找").setIcon(android.R.drawable.ic_search_category_default);
//		menu.add(0, 6, 6, "推送信息");
//		menu.add(0, 7, 7, "短信备份");

		return true;
	}
	
	//使用onOptionsItemSelected(MenuItem item)方法为menu菜单项注册事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case 1: //menu菜单删除短信
			Intent intent1=new Intent(SMSMainActivity.this, deleteSMS.class);
			startActivity(intent1);
			break;
			
//		case 2:
//			Intent intent2=new Intent(SMSMainActivity.this, draftSMS.class);
//			startActivity(intent2);
//			break;
//			
		case 3://menu菜单新建短信
			Intent intent3 = new Intent();
			intent3.setClass(SMSMainActivity.this, sendSMS.class);
			startActivity(intent3);
			break;
			
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	
	// 修改时间格式
	class LAdaptor extends SimpleCursorAdapter {

		public LAdaptor(Context context, int layout, Cursor c, String[] from,
				int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			// 获得当前短信的内容
			View v = super.getView(position, convertView, parent);
			TextView tvTime = (TextView) v.findViewById(R.id.datatime);
			// 取出时间值
			Cursor c = this.getCursor();
			c.moveToPosition(position);
			int index_data = c.getColumnIndexOrThrow("last_date");
			long longData = c.getLong(index_data);
			int index_phone = cursor_smslist.getColumnIndex("contact");
			String phone = cursor_smslist.getString(index_phone);
			
			// 转换时间格式
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat(
					"hhmm");
			Date date = new Date(longData);
			Date date2 = new Date(longData);
			String text = dateFormat.format(date);
			String time = dateFormat2.format(date2);
			time = String.format("%04d", Integer.valueOf(time) + 1);

			tvTime.setText(text);
			
			/*
			 * 请在下面添加解密代码！！
			 * 此处解密代码只负责解密MainActivity界面的短信内容！！
			 * 添加方式：msgBody.setText(declassifiedCode);
			 * 
			 */
			try {				
				TextView msgBody = (TextView) v.findViewById(R.id.messagebody);
				String message = msgBody.getText().toString();
				int i = 5;
				while(i>=0)
				{
					if(AESMessage.authentication(AESMessage.getMac(message.substring(16), AESMessage.getDynKeyForMac(phone, PrivacyData.getMyPhoneNumber())), message)) {
						String content = AESMessage.getMsg(message, AESMessage.getDynKey(phone, PrivacyData.getMyPhoneNumber()));
						msgBody.setText(content);
						break;
					}
					else {
						i--;
						time = String.format("%04d", Integer.valueOf(time) - 1);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			return v;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		return super.onKeyDown(keyCode, event);
	}
	
}
