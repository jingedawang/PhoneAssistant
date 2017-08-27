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

public class forwardingSMS extends Activity {

	private EditText forward_smsPhone;
	private EditText forward_smsContent;
	private Button forward_btnsendSMS;

	private SendReceiver sendReceiver = new SendReceiver();
	private DeliverReceiver deliverReceiver = new DeliverReceiver();
	
	private static final String SMS_SEND = "content://sms/sent";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forwarding_sms);

		forward_smsPhone = (EditText) findViewById(R.id.forward_smsPhone);
		forward_smsContent = (EditText) findViewById(R.id.forward_smsContent);
		forward_btnsendSMS = (Button) findViewById(R.id.forward_btnsendSMS);

		registerReceiver(sendReceiver, new IntentFilter("SENT_SMS_ACTION"));
		registerReceiver(deliverReceiver, new IntentFilter(
				"DELIVERED_SMS_ACTION"));

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		forwardingSMS();
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

	public void forwardingSMS() {
		// TODO Auto-generated constructor stub
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		String last_sms_body = bundle.getString("last_sms_body");

		Log.d("LIng", "last_sms_body=" + last_sms_body);

		forward_smsContent.setText(last_sms_body);

		forward_btnsendSMS.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!validata()) {
					return;
				}
				String mobile = forward_smsPhone.getText().toString().trim();
				String content = forward_smsContent.getText().toString().trim();

				SmsManager smsManager = SmsManager.getDefault();

				PendingIntent forward_sendIntent = PendingIntent
						.getBroadcast(forwardingSMS.this, 0, new Intent(
								"SENT_SMS_ACTION"), 0);
				PendingIntent forward_deliveryIntent = PendingIntent
						.getBroadcast(forwardingSMS.this, 0, new Intent(
								"DELIVERED_SMS_ACTION"), 0);
				smsManager.sendTextMessage(mobile, null, content,
						forward_sendIntent, forward_deliveryIntent);

				ContentValues contentValues = new ContentValues();
				contentValues.put("address", mobile);
				contentValues.put("body", content);
				contentValues.put("date", System.currentTimeMillis());
				contentValues.put("read", 0);
				contentValues.put("type", 2);
				
				
				Uri uri = getContentResolver().insert(Uri.parse(SMS_SEND),
						contentValues);
				
				Log.d("LIng", "uri=" + uri);
				Log.d("LIng", "id=" +uri.getLastPathSegment()) ;
				
				String[] projection = { "thread_id" };
				Cursor cur_id = getContentResolver().query(Uri.parse(SMS_SEND), projection, "_id="+uri.getLastPathSegment(),
						null, null);	
				
				cur_id.moveToFirst() ;
				
				int index_thread_id = cur_id.getColumnIndex("thread_id");
				int threadId = cur_id.getInt(index_thread_id);

				Intent intent = new Intent(forwardingSMS.this, showSMS.class);
				Bundle bundle = new Bundle();

				bundle.putInt("threadId", threadId);
				intent.putExtras(bundle);
				forwardingSMS.this.startActivity(intent);

			}
		});

	}

	private boolean validata() {
		String mobile = forward_smsPhone.getText().toString().trim();
		if (mobile.equals("")) {
			Toast toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
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
