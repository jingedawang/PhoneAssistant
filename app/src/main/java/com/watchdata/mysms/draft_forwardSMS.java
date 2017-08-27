package com.watchdata.mysms;

import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class draft_forwardSMS extends Activity {

	private EditText draft_forward_smsPhone;
	private EditText draft_forward_smsContent;
	private Button draft_forward_btadd_contact;
	private Button draft_forward_sendSMS;
	private Button draft_forward_smsDraft;

	private SendReceiver sendReceiver = new SendReceiver();
	private DeliverReceiver deliverReceiver = new DeliverReceiver();

	private static final String SMS_SEND = "content://sms/sent";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.draft_forward_sms);

		draft_forward_smsPhone = (EditText) findViewById(R.id.draft_forward_smsPhone);
		draft_forward_smsContent = (EditText) findViewById(R.id.draft_forward_smsContent);
		draft_forward_btadd_contact = (Button) findViewById(R.id.draft_forward_btadd_contact);
		draft_forward_sendSMS = (Button) findViewById(R.id.draft_forward_sendSMS);
		draft_forward_smsDraft = (Button) findViewById(R.id.draft_forward_smsDraft);

		// 注册发送成功和接收成功的广播
		registerReceiver(sendReceiver, new IntentFilter("SENT_SMS_ACTION"));
		registerReceiver(deliverReceiver, new IntentFilter(
				"DELIVERED_SMS_ACTION"));
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		draft_forward_SMS();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	private void draft_forward_SMS() {
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		int draft_sms_id = bundle.getInt("draft_sms_id");
		
		Uri uri = Uri.parse("content://sms/");
		String[] projection = { "_id", "thread_id", "address", "body", "date",
				"type" };
		
		Cursor cur_draft_forward = getContentResolver().query(uri, projection,
				"_id=? ", new String[] { Integer.toString(draft_sms_id) },
				"date ASC");

		if (cur_draft_forward.moveToFirst()) {
			String draft_sms_address = cur_draft_forward
					.getString(cur_draft_forward.getColumnIndex("address"));
			String draft_sms_body = cur_draft_forward
					.getString(cur_draft_forward.getColumnIndex("body"));

			draft_forward_smsPhone.setText(draft_sms_address);
			draft_forward_smsContent.setText(draft_sms_body);
		}

		draft_forward_sendSMS.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!validate()) {
					return;
				}
				String mobile = draft_forward_smsPhone.getText().toString()
						.trim();
				String content = draft_forward_smsContent.getText().toString()
						.trim();

				// 获取SmsManager
				SmsManager smsManager = SmsManager.getDefault();

				PendingIntent forward_sendIntent = PendingIntent.getBroadcast(
						draft_forwardSMS.this, 0,
						new Intent("SENT_SMS_ACTION"), 0);
				PendingIntent forward_deliveryIntent = PendingIntent
						.getBroadcast(draft_forwardSMS.this, 0, new Intent(
								"DELIVERED_SMS_ACTION"), 0);
				smsManager.sendTextMessage(mobile, null, content,
						forward_sendIntent, forward_deliveryIntent);

				// 写入到短信数据库
				ContentValues contentValues = new ContentValues();
				contentValues.put("address", mobile);
				contentValues.put("body", content);
				contentValues.put("date", System.currentTimeMillis());
				contentValues.put("read", 0);
				contentValues.put("type", 2);

				Uri uri = getContentResolver().insert(Uri.parse(SMS_SEND),
						contentValues); // 插入数据
				String[] projection = { "thread_id" };
				Cursor cur_id = getContentResolver().query(Uri.parse(SMS_SEND),
						projection, "_id=" + uri.getLastPathSegment(), null,
						null);

				if (cur_id.moveToFirst()) {
					int index_thread_id = cur_id.getColumnIndex("thread_id");
					int threadId = cur_id.getInt(index_thread_id);

					// 跳转到显示短信具体内容事件
					Intent intent = new Intent(draft_forwardSMS.this,
							showSMS.class); // 描述起点和目标
					Bundle bundle = new Bundle(); // 创建Bundle对象
					
					bundle.putInt("threadId", threadId); // 装入数据
					intent.putExtras(bundle); // 把Bundle塞入Intent里面
					startActivity(intent); // 开始切换					
				}
			}
		});
	}

	// 合法性验证
	private boolean validate() {
		String mobile = draft_forward_smsPhone.getText().toString().trim();
		String content = draft_forward_smsContent.getText().toString();
		if (mobile.equals("")) {
			Toast toast = Toast.makeText(this, "手机号码不能为空！", Toast.LENGTH_LONG);
			toast.show();
			return false;
		} else if (content.equals("")) {
			Toast toast = Toast.makeText(this, "短信内容不能为空请重新输入！",
					Toast.LENGTH_LONG);
			toast.show();
			return false;
		} else {
			return true;
		}

	}

	private class SendReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "Sent Successfully", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	private class DeliverReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Toast.makeText(context, "Delivered Successfully",
					Toast.LENGTH_SHORT).show();
		}
	}

}
