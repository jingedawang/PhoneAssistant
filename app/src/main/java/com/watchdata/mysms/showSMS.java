package com.watchdata.mysms;

import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.aes.base64.AESMessage;
import com.aes.base64.BackAES;
import com.aes.base64.Password;
import com.bit.contacts.ContactsManagerDbAdater;
import com.bit.gesturebuilder.CreateGestureActivity;
import com.wjg.phoneassistant.PrivacyData;
import com.wjg.phoneassistant.R;

public class showSMS extends Activity {

	private ListView list_ShowSmsDataS;
	private EditText etSmsContent;
	private ContactsManagerDbAdater contactsManagerDbAdapter;
	// 全部短信
	private static final String SMS_ALL = "content://sms/";
	// 查询联系人
	private static final String CONTACTS_LOOKUP = "content://com.android.contacts/phone_lookup/";
	// 读取短信时间
	private SLAdaptor adapter = null;
	Cursor cur_smsdetail;
	Cursor cur_smsdetail_qcontact;
	private int threadId;
	String  content;
	private SendReceiver sendReceiver = new SendReceiver();
	private DeliverReceiver deliverReceiver = new DeliverReceiver();

	// 发送信息或者接收短信刷新会话显示内容
	class SmsContent extends ContentObserver {
		public SmsContent(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			// adapter.notifyDataSetChanged();
			showSMSDetail();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		//打开数据库
		contactsManagerDbAdapter = new ContactsManagerDbAdater(this);
		contactsManagerDbAdapter.open();
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.show_sms);
		list_ShowSmsDataS = (ListView) findViewById(R.id.list_ShowSmsDataS);
		etSmsContent = (EditText) findViewById(R.id.etSmsContent);
		
		// 注册发送成功和接收成功的广播
		registerReceiver(sendReceiver, new IntentFilter("SENT_SMS_ACTION"));
		registerReceiver(deliverReceiver, new IntentFilter(
				"DELIVERED_SMS_ACTION"));

		// 注册短信变化监听
		this.getContentResolver().registerContentObserver(
				Uri.parse("content://sms/"), true,
				new SmsContent(new Handler()));
		//为短信列表注册上下文菜单
		this.registerForContextMenu(list_ShowSmsDataS);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		showSMSDetail();
		
		
	}

	private void showSMSDetail() {
		Intent intent = this.getIntent(); // 获取已有的intent对象
		Bundle bundle = intent.getExtras(); // 获取intent里面的bundle对象
		threadId = bundle.getInt("threadId"); // 获得bundle里的thread_id

		Uri uri = Uri.parse(SMS_ALL);
		String[] projection = { "_id", "thread_id", "address", "body", "date",
				"type" };

		cur_smsdetail = getContentResolver().query(uri, projection,
				"thread_id=? ", new String[] { Integer.toString(threadId) },
				"date ASC");

		// 对cursor进行处理，遇到号码后获取对应的联系人名称
		cur_smsdetail_qcontact = new CursorWrapper(cur_smsdetail) {
			public String getString(int columnIndex) {
				if (super.getColumnIndex("address") == columnIndex) {
					String address = super.getString(columnIndex);
					// 读取联系人，查询对应的名称
					Uri uri_qcontact = Uri.parse(CONTACTS_LOOKUP + address);					
					Cursor cur = getContentResolver().query(uri_qcontact, null,
							null, null, null);
					Cursor cur2 = contactsManagerDbAdapter.getCursorBySql(ContactsManagerDbAdater.TABLE_CONTACTS,
							new String[]{"telPhone","name"}, "telPhone=?", new String[]{address});
					if (cur.moveToFirst()) {
						String contactName = cur.getString(cur
								.getColumnIndex("display_name"));
						return contactName;
					}
					else if(cur2.moveToFirst()) {
						String contactName = cur2.getString(cur2
								.getColumnIndex("name"));
						return contactName;
					}

					return address;
				}
				return super.getString(columnIndex);
			}
		};

		adapter = new SLAdaptor(this, R.layout.show_sms_data,
				cur_smsdetail_qcontact, new String[] { "address", "date",
						"body" }, new int[] { R.id.smsWho, R.id.smsTime,
						R.id.smsContent }) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				// 获得当前短信的号码，内容和类型
				View result = super.getView(position, convertView, parent);
				// int index_body = cur_smsdetail.getColumnIndex("body");
				int index_type = cur_smsdetail_qcontact.getColumnIndex("type");
				int index_address = cur_smsdetail_qcontact
						.getColumnIndex("address");

				// String sms_Content = cur_smsdetail.getString(index_body);
				int sms_type = cur_smsdetail_qcontact.getInt(index_type);
				String sms_phoneNmuber = cur_smsdetail_qcontact
						.getString(index_address);

				// 加载具体一条短信显示视图
				View container = result.findViewById(R.id.showSmsData);

				// 获取显示视图界面上的文本框
				TextView smsWho = (TextView) (result.findViewById(R.id.smsWho));

				// 显示短信的子视图，LayoutParams类是用于child view（子视图） 向
				// parentview（父视图）传达自己意愿
				LinearLayout.LayoutParams containerParameter = (LinearLayout.LayoutParams) (container
						.getLayoutParams());

				if (sms_type == 1) { // 收短信，居左边
					smsWho.setText(sms_phoneNmuber);
					container.setBackgroundResource(R.drawable.left);

					containerParameter.gravity = Gravity.LEFT;
				} else if (sms_type == 2) {// 发短信，居右边
					smsWho.setText("我:");
					container.setBackgroundResource(R.drawable.right);
					containerParameter.gravity = Gravity.RIGHT;
				}
				container.setLayoutParams(containerParameter);
				return result;
			}
		};
		list_ShowSmsDataS.setAdapter(adapter);
		list_ShowSmsDataS.setSelection(adapter.getCount());
	}
	
	
	// 长按短信会话条目激活上下文菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		int index_type=cur_smsdetail_qcontact.getColumnIndex("type");
		int index_address=cur_smsdetail_qcontact.getColumnIndex("address");
		int sms_type=cur_smsdetail_qcontact.getInt(index_type);
		String address=cur_smsdetail_qcontact.getString(index_address);
		
		if(sms_type==1){  //接收的消息
			menu.setHeaderTitle("信息选项");
			menu.add(0, 1, 0, "转发");
			menu.add(0, 2, 0, "呼叫");
			menu.add(0, 3, 0, "删除信息");
			menu.add(0, 4, 0, "加密");
			menu.add(0, 5, 0, "解密");
		}else if(sms_type==2){ // 发送的短信
			menu.setHeaderTitle("信息选项");
			menu.add(0, 1, 0, "转发");
			menu.add(0, 3, 0, "删除信息");
			menu.add(0, 4, 0, "加密");
			menu.add(0, 5, 0, "解密");
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
		
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo menuInfo=(AdapterContextMenuInfo)item.getMenuInfo();
		int position=menuInfo.position;
		int sms_id=(int) menuInfo.id;
		
		long longDate = cur_smsdetail.getLong(cur_smsdetail.getColumnIndex("date"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("hhmm");
		Date date = new Date(longDate);
		String time = dateFormat.format(date);
		
		Log.v("Ling", "sms_data context item seleted ID=" + menuInfo.id);
		Log.v("Ling", "sms_data context item seleted position=" + menuInfo.position);
		
		//获得电话号码
		int index_phone=cur_smsdetail.getColumnIndex("address");
		String phone=cur_smsdetail.getString(index_phone);
		
		switch(item.getItemId()){
		case 1:   //收到的短信--转发
			Intent intent_forward_sms=new Intent(showSMS.this, sendSMS.class);
			Bundle bundle_forward_sms=new Bundle();
			int index_last_sms_body=cur_smsdetail.getColumnIndex("body");
			String last_sms_body=cur_smsdetail.getString(index_last_sms_body);
			//对于加密过的短信，转发时需要先解密
			String jiemi_str = jiemi2(last_sms_body, phone);
			last_sms_body = jiemi_str == null?last_sms_body:jiemi_str;
			bundle_forward_sms.putString("last_sms_body", last_sms_body);
			intent_forward_sms.putExtras(bundle_forward_sms);
			showSMS.this.startActivity(intent_forward_sms);
			break;
			
		case 2:   //收到的短信--呼叫
			//用intent启动拨打电话
			Intent intent_call=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phone));
			startActivity(intent_call);
			break;
			
		case 3:   //收到的短信--删除信息
			this.getContentResolver().delete(Uri.parse("content://sms/"+sms_id), null, null);
			break;
			
		case 4:   //收到的短信--加密
			/*
			 * 此处进行手动加密！！
			 */	
			String sms_body=cur_smsdetail.getString(cur_smsdetail.getColumnIndex("body"));
			String mobile = cur_smsdetail.getString(cur_smsdetail.getColumnIndex("address"));	
			content=sms_body;// sms_body;
			//手动加密
			jiami(time, mobile);
			ContentValues values = new ContentValues();
			values.put("body", content); // 消息内容
			this.getContentResolver().update(Uri.parse("content://sms/"+sms_id),values, null, null);
			break;
			
		case 5:   
//			//发送的短信--删除信息
//			this.getContentResolver().delete(Uri.parse("content://sms/"+sms_id), null, null);
//			break;
			   //发送的短信--解密
			String sms_body2=cur_smsdetail.getString(cur_smsdetail.getColumnIndex("body"));
			String mobile2 = cur_smsdetail.getString(cur_smsdetail.getColumnIndex("address"));
			// sms_body;
			/*
			 * 此处进行手动解密！！
			 */
			time = String.format("%04d", Integer.valueOf(time) + 1);
				try {
					if(!jiemi(sms_body2, mobile2)) {
						break;
					}
					ContentValues values2 = new ContentValues();
					values2.put("body", content); // 消息内容
					this.getContentResolver().update(Uri.parse("content://sms/"+sms_id),values2, null, null);
					Toast.makeText(getApplicationContext(), "解密成功", Toast.LENGTH_SHORT).show();
				}
				catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "解密失败", Toast.LENGTH_SHORT).show();
				}
				
			break;
		default:
		return super.onContextItemSelected(item);
		
		}
		return true;
	}

	/**
	 * 封装的加密代码
	 * @param time
	 */
	private void jiami(String time, String mobile) {
		
		/* 此处进行加密！！！
		 * 此处进行加密！！！
		 * 此处进行加密！！！
		 */
		try {
			content = AESMessage.getMessage(content, PrivacyData.getMyPhoneNumber(), mobile);
			Toast.makeText(getApplicationContext(), "加密成功", Toast.LENGTH_SHORT).show();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "加密失败", Toast.LENGTH_SHORT).show();
		}
		
	}
	/**
	 * 封装的解密代码
	 * @param message
	 * @param time
	 * @return
	 * @throws Exception
	 */
	private boolean jiemi(String message, String toMobile) throws Exception {
		if(AESMessage.authentication(AESMessage.getMac(message.substring(16), AESMessage.getDynKeyForMac(toMobile, PrivacyData.getMyPhoneNumber())), message)) {
			content = AESMessage.getMsg(message, AESMessage.getDynKey(toMobile, PrivacyData.getMyPhoneNumber()));
			return true;
		}
		return false;
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

	class SLAdaptor extends SimpleCursorAdapter {

		public SLAdaptor(Context context, int layout, Cursor c, String[] from,
				int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			// 获得当前短信的内容
			View v = super.getView(position, convertView, parent);
			TextView tvTime = (TextView) v.findViewById(R.id.smsTime);
			// 取出时间值
			Cursor c = this.getCursor();
			int index_data = c.getColumnIndexOrThrow("date");
			long longData = c.getLong(index_data);
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
			 * 此处解密代码只负责解密showSMS界面的短信内容！！
			 * 添加方式：msgContent.setText(declassifiedCode);
			 * 
			 */
			try {
				TextView msgContent = (TextView) v.findViewById(R.id.smsContent);
				String mobile = cur_smsdetail.getString(cur_smsdetail.getColumnIndex("address"));
				String message = msgContent.getText().toString();
					if(AESMessage.authentication(AESMessage.getMac(message.substring(16), AESMessage.getDynKeyForMac(mobile, PrivacyData.getMyPhoneNumber())), message)) {
						String msg = AESMessage.getMsg(message, AESMessage.getDynKey(mobile, PrivacyData.getMyPhoneNumber()));
						msgContent.setText(msg);
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			return v;
		}
	}

	// 点击发送短信
	public void btnSend_Click(View v) {
		/*
		 * 此处添加加密代码！！
		 * 此处添加加密代码！！
		 * 此处添加加密代码！！
		 */
		String content = etSmsContent.getText().toString();
		String mobile = cur_smsdetail.getString(cur_smsdetail.getColumnIndex("address"));
		try {
			content = AESMessage.getMessage(content, PrivacyData.getMyPhoneNumber(), mobile);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 获得SmsManager的默认实例
		SmsManager manager = SmsManager.getDefault();

		// 创建一个PendingIntent对象,短信发送成功或失败后会产生一条SENT_SMS_ACTION的广播
		PendingIntent sendIntent = PendingIntent.getBroadcast(showSMS.this, 0,
				new Intent("SENT_SMS_ACTION"), 0);
		// 接收方成功收到短信后，发送方会产生一条DELIVERED_SMS_ACTION的广播
		PendingIntent deliveryIntent = PendingIntent.getBroadcast(showSMS.this,
				0, new Intent("DELIVERED_SMS_ACTION"), 0);
		if (content.length() > 160) { // 如果字数超过160,需拆分成多条短信发送
			List<String> msgs = manager.divideMessage(content);
			for (String msg : msgs) {
				manager.sendTextMessage(mobile, null, msg, sendIntent,
						deliveryIntent);
				etSmsContent.setText(""); // 短信发送后，清空内容界面
			}
		} else {
			manager.sendTextMessage(mobile, null, content, sendIntent,
					deliveryIntent);
			etSmsContent.setText("");
		}

		// 写入到短信数据库——系统自带数据库
		ContentValues values = new ContentValues();
		values.put("address", mobile); // 发送地址
		values.put("body", content); // 消息内容
		values.put("date", System.currentTimeMillis()); // 创建时间
		values.put("read", 0); // 0：未读； 1：已读
		values.put("type", 2); // 1：接收； 2：发送
		getContentResolver().insert(Uri.parse("content://sms/sent"), values); // 插入数据

	}

	private class SendReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "Sent Successfully.",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(context, "Failed to Send.", Toast.LENGTH_SHORT)
						.show();

			}
		}

	}

	private class DeliverReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Toast.makeText(context, "Delivered Successfully.",
					Toast.LENGTH_SHORT).show();
		}

	}
}
