package com.watchdata.mysms;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.aes.base64.AESMessage;
import com.aes.base64.BackAES;
import com.aes.base64.Password;
import com.bit.contacts.ContactsManager;
import com.bit.gesturebuilder.CreateGestureActivity;
import com.wjg.phoneassistant.PrivacyData;
import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class sendSMS extends Activity {

	private Button btnsendSMS;
	private Button btnsmsDraft;
	private EditText smsPhone;
	private EditText smsContent;
	private Button btnadd_contact;
	SmsManager smsManager;
	private int SEND_TYPE = 0;
	private final int SEND_SMS_TYPE = 1;
	private final int SEND_SMS = 2;
	private final int SEND_SMS_ADD = 3;

	// 全部短信
	// private static final String SMS_ALL = "content://sms/";
	// 发件箱
	private static final String SMS_SEND = "content://sms/sent";
	private static final String SMS_DRAFT = "content://sms/draft";

	private SendReceiver sendReceiver = new SendReceiver();
	private DeliverReceiver deliverReceiver = new DeliverReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_sms);

		// 获取SmsManager
		smsManager = SmsManager.getDefault();

		// 获取程序界面上的两个文本框和按钮
		btnsendSMS = (Button) findViewById(R.id.sendSMS);
		btnsmsDraft = (Button) findViewById(R.id.smsDraft);
		smsPhone = (EditText) findViewById(R.id.smsPhone);
		smsContent = (EditText) findViewById(R.id.smsContent);
		btnadd_contact = (Button) findViewById(R.id.btadd_contact);

		// 注册发送成功的广播
		registerReceiver(sendReceiver, new IntentFilter("SENT_SMS_ACTION"));
		// 注册接收成功的广播
		registerReceiver(deliverReceiver, new IntentFilter(
				"DELIVERED_SMS_ACTION"));
		

		
		///来自 通信录的  发送短信
		Intent intent = getIntent();
		smsPhone.setText(intent.getStringExtra("addressFromContacts"));
		

		
		//来自   转发短信  的  intent时   需要取出  短信内容
//		Intent intent2 = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null)
		{
				String last_sms_body = bundle.getString("last_sms_body");
				smsContent.setText(last_sms_body);
		}
	//	String last_sms_body = bundle.getString("last_sms_body");
	//	smsContent.setText(last_sms_body);
		
	}

	// 点击"发送"按钮，发送短信
	public void btnsendSMS_Click(View v) {
		if (!validate()) {
			return;
		}
		
		sendSMSByNew();
		
	}
		
	private void sendSMSByNew() {

		String mobile = smsPhone.getText().toString().trim();
		String content = smsContent.getText().toString().trim();
		
		/* 此处进行加密！！！
		 * 此处进行加密！！！
		 * 此处进行加密！！！
		 */
		try {
			content = AESMessage.getMessage(content, PrivacyData.getMyPhoneNumber(), mobile);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "加密失败", Toast.LENGTH_LONG).show();
		}
		
		// 创建一个PendingIntent对象,短信发送成功或失败后会产生一条SENT_SMS_ACTION的广播
		PendingIntent sendIntent = PendingIntent.getBroadcast(sendSMS.this, 0,
				new Intent("SENT_SMS_ACTION"), 0);
		// 接收方成功收到短信后，发送方会产生一条DELIVERED_SMS_ACTION的广播
		PendingIntent deliveryIntent = PendingIntent.getBroadcast(sendSMS.this,
				0, new Intent("DELIVERED_SMS_ACTION"), 0);
		if (content.length() > 160) { // 如果字数超过160,需拆分成多条短信发送
			List<String> msgs = smsManager.divideMessage(content);
			for (String msg : msgs) {
				smsManager.sendTextMessage(mobile, null, msg, sendIntent,
						deliveryIntent);
				// smsPhone.setText("");
				// smsContent.setText("");
			}
		} else {
			smsManager.sendTextMessage(mobile, null, content, sendIntent,
					deliveryIntent);
			// smsPhone.setText("");
			// smsContent.setText("");
		}


		
		// 写入到短信数据库
		ContentValues values = new ContentValues();
		values.put("address", mobile); // 发送地址
		values.put("body", content); // 消息内容
		values.put("date", System.currentTimeMillis()); // 创建时间
		values.put("read", 0); // 0：未读； 1：已读
		values.put("type", 2); // 1：接收； 2：发送

		// 返回新创建信息的行数,即短信数据库的 _id
		Uri uri = getContentResolver().insert(Uri.parse(SMS_SEND), values); // 插入数据

		Log.d("LIng", "uri=" + uri);
		Log.d("LIng", "id=" + uri.getLastPathSegment());

		String[] projection = { "thread_id" };
		Cursor cur_id = getContentResolver().query(Uri.parse(SMS_SEND),
				projection, "_id=" + uri.getLastPathSegment(), null, null);

		cur_id.moveToFirst();

		int index_thread_id = cur_id.getColumnIndex("thread_id");
		int threadId = cur_id.getInt(index_thread_id);

		// 跳转到显示短信具体内容事件
		Intent intent = new Intent(sendSMS.this, showSMS.class); // 描述起点和目标
		Bundle bundle = new Bundle(); // 创建Bundle对象

		bundle.putInt("threadId", threadId); // 装入数据
		intent.putExtras(bundle); // 把Bundle塞入Intent里面
		sendSMS.this.startActivity(intent); // 开始切换
		
		finish();

		// getContentResolver().insert(Uri.parse(SMS_SEND), values); //
		// 插入数据
		// Intent intent = new Intent(sendSMS.this, MainActivity.class);
		// // 描述起点和目标
		// sendSMS.this.startActivity(intent); // 开始切换
	}

	// 点击"保存到草稿箱按钮"，保存短信至草稿箱
	public void btnsmsDraft_Click(View v) {
		String mobile = smsPhone.getText().toString().trim();
		String content = smsContent.getText().toString().trim();

//		if (mobile == "" && content != "") {
//			ContentValues values = new ContentValues();
//			//values.put("address", ""); // 发送地址
//			values.put("body", content); // 消息内容
//			values.put("date", System.currentTimeMillis()); // 创建时间
//			values.put("read", 0); // 0：未读； 1：已读
//			values.put("type", 3); // 1：接收； 2：发送；3：草稿
//			// 返回新创建信息的行数,即短信数据库的 _id
//			Uri uri = getContentResolver().insert(Uri.parse(SMS_DRAFT), values); // 插入数据
//		} else if (mobile != "") {
//			ContentValues values = new ContentValues();
//			values.put("address", mobile); // 发送地址
//			values.put("body", content); // 消息内容
//			values.put("date", System.currentTimeMillis()); // 创建时间
//			values.put("read", 0); // 0：未读； 1：已读
//			values.put("type", 3); // 1：接收； 2：发送；3：草稿
//			Uri uri = getContentResolver().insert(Uri.parse(SMS_DRAFT), values); // 插入数据
//		}
		
		if(mobile != ""){
			ContentValues values = new ContentValues();
			values.put("address", mobile); // 发送地址
			values.put("body", content); // 消息内容
			values.put("date", System.currentTimeMillis()); // 创建时间
			values.put("read", 0); // 0：未读； 1：已读
			values.put("type", 3); // 1：接收； 2：发送；3：草稿
			Uri uri = getContentResolver().insert(Uri.parse(SMS_DRAFT), values); // 插入数据
		//	getContentResolver().update(uri, values, where, selectionArgs)(Uri.parse(SMS_DRAFT), values);
		}
		
		Intent intent=new Intent(sendSMS.this, SMSMainActivity.class);
		startActivity(intent);
	}

	// 点击"添加"按钮，添加联系人
	public void btnadd_contact_Click(View v) {
		// TODO Auto-generated method stub
		
		Intent intent = new Intent();
		intent.setClass(sendSMS.this, ContactsManager.class);
		intent.putExtra("isFromAddContact", true);
		sendSMS.this.startActivityForResult(intent, SEND_SMS_ADD);
		
	/*
	 *  //原为查询系统通讯录，现改为查询本应用通讯录
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setData(ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, SEND_SMS_TYPE);
	*/
	}

	// 添加联系人后返回到发送短信界面
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case SEND_SMS_TYPE:
			if (intent == null) {
				return;
			}
			String phoneNumber = null;
			Uri uri = intent.getData();
			if (uri == null) {
				return;
			}
			Cursor cursor = managedQuery(uri, null, null, null, null);
			if (cursor.moveToFirst()) {
				String hasphone = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				String id = cursor.getString(cursor
						.getColumnIndex(ContactsContract.Contacts._ID));
				if (hasphone.equalsIgnoreCase("1")) {
					hasphone = "true";
				} else {
					hasphone = "false";
				}
				if (Boolean.parseBoolean(hasphone)) {
					Cursor cur_phones = getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ "=" + id, null, null);
					while (cur_phones.moveToNext()) {
						
						phoneNumber = cur_phones
								.getString(cur_phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

						setTitle(phoneNumber);
					}
			//		cur_phones.close();
				}
			}
			SEND_TYPE = SEND_SMS_TYPE;
			smsPhone.setText(phoneNumber);
			break;
		case SEND_SMS_ADD:
			String phoneNum = "";
			try {
				phoneNum = intent.getStringExtra("phoneNumber");

				try {
						String result = BackAES.decrypt(phoneNum, Password.getKeyForContacts(), 0);
						phoneNum=result;
						
					}
					catch (Exception e) {
						e.printStackTrace();
						
					}
			}
			catch(Exception ex)
			{}
			
			smsPhone.setText(phoneNum);
			break;
		}

	}

	// 合法性验证
	private boolean validate() {
		String mobile = smsPhone.getText().toString().trim();
		String content = smsContent.getText().toString();
		if (mobile.equals("")) {
			Toast toast = Toast.makeText(this, "手机号码不能为空！", Toast.LENGTH_LONG);
			toast.show();
			return false;
		}
		else if (content.equals("")) {
			Toast toast = Toast.makeText(this, "短信内容不能为空请重新输入！",
					Toast.LENGTH_LONG);
			toast.show();
			return false;
		} 
		else {
			return true;
		}

	}

	private class SendReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
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

	/**
	 * 发送方的短信发送到对方手机上之后,对方手机会返回给运营商一个信号, 运营商再把这个信号发给发送方,发送方此时可确认对方接收成功
	 * 模拟器不支持,真机上需等待片刻
	 * 
	 * @author user
	 * 
	 */
	private class DeliverReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Delivered Successfully.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		Intent intent = getIntent();
		String phoneNum = intent.getStringExtra("addressFromContacts");
		
		
		
		//Intent intent2 = getIntent();
		
		Bundle bundle = intent.getExtras();
		if (bundle != null)
		{
				String last_sms_body = bundle.getString("last_sms_body");
				smsContent.setText(last_sms_body);
		}
	
		
		
		
		
		
		if(phoneNum == null) {
			return;
		}
		
		smsPhone.setText(phoneNum);
		
	}

}
