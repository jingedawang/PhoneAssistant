package com.qiniu.demo;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qiniu.auth.JSONObjectRet;
import com.qiniu.io.IO;
import com.qiniu.io.PutExtra;
import com.qiniu.utils.QiniuException;
import com.wjg.phoneassistant.PrivacyData;
import com.wjg.phoneassistant.R;

public class MyActivity extends Activity implements View.OnClickListener{

	public static final int PICK_PICTURE_RESUMABLE = 0;

	// @gist upload_arg
	// 在七牛绑定的对应bucket的域名. 默认是bucket.qiniudn.com
	public static String bucketName = "qiuhuangbin";
	public static String domain = bucketName + ".qiniudn.com";
	//upToken 这里需要自行获取. SDK 将不实现获取过程. 当token过期后才再获取一遍
	public String uptoken = "kfAny7FGMGza1N3NMeURb3wUuoCMd2Cf3iBwkBr1:THRibhY-sgBsOH2ZCSqcC5kxzMw=:eyJzY29wZSI6InFpdWh1YW5nYmluIiwiZGVhZGxpbmUiOjE0MDc4MzY2NzJ9";
	// @endgist

	private Button contacts;
	private Button message;
	private Button passwordbook;
	private Button album;
	private TextView hint;
	private ProgressBar progressBar;
	private DatabaseHelper helper;
	private sqlite sqlite;
	private boolean flag = true;
	
//	private Handler mHandler = new Handler(){  
//        public void handleMessage(Message msg){  
//        	if(msg.what == 0x01) {
//        		progressBar.setVisibility(View.GONE);
//        		String uri = "content://sms";
//    			String databaseName = "sms.db";
//        		doUpload(uri, databaseName);
//        	}
//        }  
//    }; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qiniu_main);
		initWidget();
		helper = new DatabaseHelper(this, null);
		sqlite = new sqlite();
	}

	/**
	 * 初始化控件	
	 **/
	private void initWidget() {
		hint = (TextView) findViewById(R.id.textView1);
		contacts = (Button) findViewById(R.id.backup_contacts);
		contacts.setOnClickListener(this);
		message = (Button) findViewById(R.id.backup_message);
		message.setOnClickListener(this);
		passwordbook = (Button) findViewById(R.id.backup_passwordbook);
		passwordbook.setOnClickListener(this);
		album = (Button) findViewById(R.id.backup_album);
		album.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setIndeterminate(false);
	}

	// @gist upload
	boolean uploading = false;
	/**
	 *普通上传文件
	 * @param uri
	 */
	private void doUpload(String str, String databaseName) {
		if (uploading) {
			hint.setText("上传中，请稍后");
			return;
		}
		String phoneNumber = PrivacyData.getMyPhoneNumber();
		uploading = true;
//		String key = IO.UNDEFINED_KEY;  // 自动生成key
		String key = phoneNumber + "-" + databaseName;
		PutExtra extra = new PutExtra();
		extra.params = new HashMap<String, String>();
		extra.params.put("x:a", "测试中文信息");
		
		hint.setText("上传中");
		
		if(!str.startsWith("content")) {
			
			File file = new File(str);
			IO.putFile(uptoken, key, file, extra, new JSONObjectRet() {
				@Override
				public void onProcess(long current, long total) {
					hint.setText(current + "/" + total);
				}

				@Override
				public void onSuccess(JSONObject resp) {
					uploading = false;
					String hash = resp.optString("hash", "");
					String value = resp.optString("x:a", "");
					String redirect = "http://" + domain + "/" + hash;
					hint.setText("上传成功! ");
//					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirect));
//					startActivity(intent);
				}
	
				@Override
				public void onFailure(QiniuException ex) {
					uploading = false;
					hint.setText("错误:: " + ex.getMessage());
				}
			});
			
		}
		else {
			
			
			
			File file = new File("/data/data/com.wjg.phoneassistant/databases/mysms.db");
			if(file.exists()) {
				file.delete();
				System.out.println("已删除");
			}
			System.out.println("不知是否删除");
			sqlite.QueryAll(MyActivity.this, helper);
			File file1 = new File("/data/data/com.wjg.phoneassistant/databases/mysms.db");
			
			IO.putFile(uptoken, key, file1, extra, new JSONObjectRet() {
				@Override
				public void onProcess(long current, long total) {
					hint.setText(current + "/" + total);
				}

				@Override
				public void onSuccess(JSONObject resp) {
					uploading = false;
					String hash = resp.optString("hash", "");
					String value = resp.optString("x:a", "");
					String redirect = "http://" + domain + "/" + hash;
					hint.setText("上传成功! ");
//					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirect));
//					startActivity(intent);
				}
	
				@Override
				public void onFailure(QiniuException ex) {
					uploading = false;
					hint.setText("错误:: " + ex.getMessage());
				}
			});
		}
	}
	// @endgist  Unauthorized

	@Override
	public void onClick(View view) {
		if (view.equals(contacts)) {
			String file = "/data/data/com.wjg.phoneassistant/databases/contactsmanager.db";
			String databaseName = "contacts.db";
			doUpload(file, databaseName);
			return;
		}
		else if(view.equals(message)) {
			String uri = "content://sms";
			String databaseName = "sms.db";
//			Thread thread = new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					sqlite.QueryAll(MyActivity.this, helper);
//					Message msg = new Message();
//					msg.what = 0x01;
//					mHandler.sendMessage(msg);
//					
//				}
//			});
//			thread.start();
//			progressBar.setVisibility(View.VISIBLE);
//			progressBar.setProgress(0);
			doUpload(uri, databaseName);
			return;
		}
		else if(view.equals(passwordbook)) {
			String file = "/data/data/com.wjg.phoneassistant/databases/accounts";
			String databaseName = "passwordbook.db";
			doUpload(file, databaseName);
			return;
		}
		else if(view.equals(album)) {
			String file = "/data/data/com.wjg.phoneassistant/databases/pictures";
			String databaseName = "album.db";
			doUpload(file, databaseName);
			return;
		}
	}
	
}
