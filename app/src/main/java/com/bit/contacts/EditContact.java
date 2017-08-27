package com.bit.contacts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aes.base64.BackAES;
import com.aes.base64.Password;
import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


public class EditContact extends Activity {

	ContactsManagerDbAdater contactsManagerDbAdapter;

	public static final String TAG = "EditContact";

	// 用来标识请求照相功能的activity
	private static final int CAMERA_WITH_DATA = 3023;

	// 用来标识请求gallery的activity
	private static final int PHOTO_PICKED_WITH_DATA = 3021;

	// 拍照的照片存储位置
	private static final File PHOTO_DIR = new File(Environment
			.getExternalStorageDirectory()
			+ "/DCIM/Camera");
	//照相机拍照得到的图片
	private File mCurrentPhotoFile;
	//头像
	private PhotoEditorView mEditor;
	//联系人信息
	private Cursor contactInfoCursor;
	//缓存联系人所有信息 
	private MyContacts contactAllInfoCache=null;
	//缓存GroupSpinner数据
	private ArrayAdapter<String> adapter;
	
	//各个组件
	private EditText name;//姓名
	private EditText phoneNumber;//号码
	private Spinner groupSpinner;//组
	private Button birthdayButton;//生日
	private EditText address;//住址
	private EditText email;//邮箱
	private EditText information;//好友描述 
	
	private Button ok;//确定
	private Button cancel;//取消
	
	//用户所有信息
	private String _name;
	private byte[] img;//头像数据
	private String _phoneNumber;
	private String _groupSpinner;
	private String _birthdayButton;
	private String _address;
	private String _email;
	private String _information;
	
	//联系人信息的索引值
	int index_name=1;
	int index_contactIcon=2;
	int index_telePhone=3;
	int index_groupName=4;
	int index_birthday=5;
	int index_address=6;
	int index_email=7;
	int index_description=8;
	
	//int selectedGroupNameIndex;//记录用户选择的组的索引,从新的activity返回时用到
	
	//activity处于两种状态，插入状态或者编辑状态
	private static final int STATE_INSERT=0;//插入状态
	private static final int STATE_EDIT=1;//编辑状态
	
	private int state;//记录当前的状态
	
	
	// 生日，年，月，日
    private int mYear;
    private int mMonth;
    private int mDay;
    
    private String editContactName;//缓存要编辑的联系人
	private String groupName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.editcontact);
		contactsManagerDbAdapter=new ContactsManagerDbAdater(this);
		contactsManagerDbAdapter.open();
		//初始化所有组件 
		initAllComponents();
		initGroupSpinnerData();
		Intent intent=getIntent();
		String action=intent.getAction();
		
		if(action.equals(Intent.ACTION_INSERT)){
			
			//添加联系人  获取组名 
			groupName=intent.getStringExtra("groupName");

			state=STATE_INSERT;
		}else if(action.equals(Intent.ACTION_EDIT)){
			state=STATE_EDIT;
		
			//取数据库里面的数据
			editContactName=intent.getStringExtra("name");
			String sql="select * from contacts where name=?";
			String selectionArgs[]={editContactName};
			Log.i(TAG, editContactName);
			contactInfoCursor=contactsManagerDbAdapter.getCursorBySql(sql, selectionArgs);
		//	startManagingCursor(contactInfoCursor);
			//不知道为何只有加了下面的if语句到resume方法后才会正常执行
			if(contactInfoCursor!=null && contactInfoCursor.getCount()>0){
			}
		}else{
			Log.e(TAG, "Unknown action,program will exit...");
			finish();
			return;
		}
		
	}
	
	@Override
	protected void onResume() {
		//中途点击什么主页什么按钮暂时退出程序时的恢复
		super.onResume();
		if(state == STATE_INSERT){
			setTitle("新建联系人");
			getNowTime();
			String birthday=mYear+"-"+mMonth+"-"+mDay;
			birthdayButton.setText(birthday);
			_groupSpinner=groupName;//为 spinner 添加初始值
			int groupIndex=adapter.getPosition(_groupSpinner);
			groupSpinner.setSelection(groupIndex);
			
		}else if(state == STATE_EDIT){
			setTitle("编辑联系人");
			if(contactInfoCursor!=null && contactInfoCursor.getCount()>0){
				if(contactAllInfoCache==null){
						contactInfoCursor.moveToFirst();
						
						//得到数据库中的联系人信息
						_name=contactInfoCursor.getString(1);
						img=contactInfoCursor.getBlob(2);
						
						String phn=contactInfoCursor.getString(3);
						try {
								String result = BackAES.decrypt(phn, Password.getKeyForContacts(), 0);
								phn=result;
								
							}
							catch (Exception e) {
								e.printStackTrace();
								
							}
						_phoneNumber=phn;
						
						
						
						
						
						
						_groupSpinner=contactInfoCursor.getString(4);
						_birthdayButton=contactInfoCursor.getString(5);
						initDateFromDb(_birthdayButton);
						_address=contactInfoCursor.getString(6);
						_email=contactInfoCursor.getString(7);
						_information=contactInfoCursor.getString(8);
					
				}else{
						_name=contactAllInfoCache.getName();
						//img已经在onActivityResult方法内赋了新值，这里再调用contactAllInfoCache的getContactIcon
						//方法会把img给覆盖掉，所以就不再调用了
						//img=contactAllInfoCache.getContactIcon();
						_phoneNumber=contactAllInfoCache.getTelPhone();
						_groupSpinner=contactAllInfoCache.getGroupName();
						_birthdayButton=contactAllInfoCache.getBirthday();
						_address=contactAllInfoCache.getAddress();
						_email=contactAllInfoCache.getEmail();
						_information=contactAllInfoCache.getDescription();
				}
				name.setText(_name);
				mEditor.setPhotoBitmap(getBitmapFromByte(img));
				phoneNumber.setText(_phoneNumber);
				int groupIndex=adapter.getPosition(_groupSpinner);
				groupSpinner.setSelection(groupIndex);
				birthdayButton.setText(_birthdayButton);
				address.setText(_address);
				email.setText(_email);
				information.setTextKeepState(_information);
			}
			
		}
	}

	private void initAllComponents() {
		name=(EditText)findViewById(R.id.name);
		mEditor=(PhotoEditorView)findViewById(R.id.icon);
		phoneNumber=(EditText)findViewById(R.id.phoneNumber);
		groupSpinner=(Spinner)findViewById(R.id.spinner_group);
		birthdayButton=(Button)findViewById(R.id.birthdayButtonPicker);
		address=(EditText)findViewById(R.id.address);
		email=(EditText)findViewById(R.id.email);
		information=(EditText)findViewById(R.id.information);
		ok=(Button)findViewById(R.id.btn_ok);
		cancel=(Button)findViewById(R.id.btn_cancel);
		setComponentsListener();
	}
	
	private void setComponentsListener() {
		//图像上的监听
		mEditor.setEditorListener(new PhotoListener());
		//组上的监听,注意在这之前还要给spinner赋值，别忘了
		
		groupSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				_groupSpinner=adapter.getItem(position).toString();//得到用户选择的组
				//selectedGroupNameIndex=position;//记录下用户选择的组的索引
				parent.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		});
		
		//生日按钮监听
		birthdayButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createTimePickerDialog().show();
			}
		});
		
		//ok监听
		ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//获取已经输入的信息，并存入数据库
				verifyAllData();
			}
		});
		
		//cancel监听
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}

	//从数据库获取生日信息
	private void initDateFromDb(String birthdayButton2) {
		String args[]=birthdayButton2.split("-");
		mYear = Integer.valueOf(args[0]);
        mMonth = Integer.valueOf(args[1]);
        mDay = Integer.valueOf(args[2]);
		
	}

	//当从当前的Activity切换到另一个Activity时调用，切换过去时当前Activity并未结束(Destory)
	//保存当前输入的数据
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.v(TAG, "record original data");
		//缓存联系人信息 
		contactAllInfoCache=new MyContacts();
		//得到最终用户信息
		contactAllInfoCache.setName(name.getText().toString());
		BitmapDrawable bd=(BitmapDrawable)mEditor.getDrawable();
		Bitmap bitMap=bd.getBitmap();
		contactAllInfoCache.setContactIcon(getBitmapByte(bitMap));
		contactAllInfoCache.setTelPhone(phoneNumber.getText().toString());
		contactAllInfoCache.setGroupName(groupSpinner.getSelectedItem().toString());
		contactAllInfoCache.setBirthday(birthdayButton.getText().toString());
		contactAllInfoCache.setAddress(address.getText().toString());
		contactAllInfoCache.setEmail(email.getText().toString());
		contactAllInfoCache.setDescription(information.getText().toString());
		
		outState.putSerializable("originalData", contactAllInfoCache);
	}
	
	//初始化Spinner数据
	public void initGroupSpinnerData(){
		adapter=new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				getAllExistGroup()
				);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpinner.setAdapter(adapter);
	}
	
	
	//得到所有的组，在spinner中显示
	public ArrayList<String> getAllExistGroup(){
		Cursor cursor=contactsManagerDbAdapter.getAllGroups();
		ArrayList<String> groups=new ArrayList<String>();
		if(cursor!=null){
			while(cursor.moveToNext()){
				groups.add(cursor.getString(cursor.getColumnIndexOrThrow("groupName")));
			}
		}
		cursor.close();
		return groups;
	}
	
	//得到系统时间
	private void getNowTime() {
		Calendar time = Calendar.getInstance();
        mYear = time.get(Calendar.YEAR);
        mMonth = time.get(Calendar.MONTH);
        mDay = time.get(Calendar.DAY_OF_MONTH);
	}


	
	protected void verifyAllData() {
		// 得到所有信息
		_name=name.getText().toString().trim();
		BitmapDrawable bd=(BitmapDrawable)mEditor.getDrawable();
		Bitmap bitMap=bd.getBitmap();
		img=getBitmapByte(bitMap);
	
		
		
	//	img=null;
		_phoneNumber=phoneNumber.getText().toString().trim();
		_groupSpinner=groupSpinner.getSelectedItem().toString();
		_birthdayButton=birthdayButton.getText().toString();
		_address=address.getText().toString().trim();
		_email=email.getText().toString().trim();
		_information=information.getText().toString().trim();
		//检查数据的有效性
		if(_name.equals("") || _name==null){
			showToast(getResources().getString(R.string.namecannotbeempty));
			return;
		}
		if(_phoneNumber.equals("") || _name==null){
			showToast(getResources().getString(R.string.numbercannotbeempty));
			return;
		}
		/*if(_email.equals("")){
			showToast("邮箱不能为空");
			return;
		}*/
		if(!"".equals(_email)){
			if(!isEmail(_email)){
				showToast("Email格式有误");
				return;
			}
		}
		
		contactAllInfoCache=new MyContacts();
		//得到最终用户信息
		contactAllInfoCache.setName(_name);
		contactAllInfoCache.setContactIcon(img);
		contactAllInfoCache.setTelPhone(_phoneNumber);
		contactAllInfoCache.setGroupName(_groupSpinner);
		contactAllInfoCache.setBirthday(_birthdayButton);
		contactAllInfoCache.setAddress(_address);
		contactAllInfoCache.setEmail(_email);
		contactAllInfoCache.setDescription(_information);
		
		if(state == STATE_INSERT){
			long count=contactsManagerDbAdapter.inserDataToContacts(contactAllInfoCache);
			if(count>0){
				showToast(getResources().getString(R.string.addcontactsuccessfully));
				finish();
			}else{
				showToast(getResources().getString(R.string.addcontactfailure));
				finish();
			}
				
		}else{
			int count=contactsManagerDbAdapter.updateDataToContacts(contactAllInfoCache, editContactName);
			
			if(count>0){
				showToast(getResources().getString(R.string.updatecontactsuccessfully));
				finish();
			}else{
				showToast(getResources().getString(R.string.updatecontactfailure));
				finish();
			}
		}
	}
	
	public Boolean isEmail(String str){
		String regex="[a-zA-Z_0-9.]{1,}[0-9]{0,}@(([a-zA-Z0-9]-*){1,}\\.){1,3}[a-zA-Z\\-]{1,}";
		return match(regex, str);
	}
	
	public Boolean match(String regex,String str){
		Pattern pattern=Pattern.compile(regex);
		Matcher matcher=pattern.matcher(str);
		return matcher.matches();
	}

	//处理键盘事件

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK ){
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("确定要退出编辑?");
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			});
			builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.show();
			return true;
		}else{
		
			return super.onKeyDown(keyCode, event);
		}
	}
	


	//创建DatePickerDialog
	protected Dialog createTimePickerDialog() {
		
		return new DatePickerDialog(this,
                mDateSetListener,
                mYear, mMonth - 1, mDay);
	}
	
	//DatePickerDialog上的监听
	private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                    int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear + 1;
                mDay = dayOfMonth;
                updateToDisplay();
            }
        };

	//更新时间
	 private void updateToDisplay() {
		 birthdayButton.setText(
	            new StringBuilder()
	                    .append(mYear).append("-")
	                    .append(mMonth).append("-")
	                    .append(mDay));
	                    
	    }

	 
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(contactsManagerDbAdapter!=null){
			contactsManagerDbAdapter.close();
			contactsManagerDbAdapter=null;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	//将头像转换成byte[]以便能将图片存到数据库
	public byte[] getBitmapByte(Bitmap bitmap){
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "transform byte exception");
		}
		return out.toByteArray();
	}
	
	//得到存储在数据库中的头像
	public Bitmap getBitmapFromByte(byte[] temp){
		if(temp!=null){
			Bitmap bitmap=BitmapFactory.decodeByteArray(temp, 0, temp.length);
			return bitmap;
		}else{
			//Bitmap bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.contact_add_icon);
			return null;
		}
	}

	//弹出提示信息
	public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

	public class PhotoListener implements PhotoEditorView.EditorListener,
			DialogInterface.OnClickListener {

		@Override
		public void onRequest(int request) {
			if (request == PhotoEditorView.REQUEST_PICK_PHOTO) {
				if (mEditor.hasSetPhoto()) {
					// 当前已经有了照片
					createPhotoDialog().show();
				}else{
					doPickPhotoAction();
				}
			}
		}

		private Dialog createPhotoDialog() {
			Context context = EditContact.this;
			final Context dialogContext = new ContextThemeWrapper(context,
					android.R.style.Theme_Light);
			String cancel="返回";
			String[] choices;
			choices = new String[3];
			choices[0] = getString(R.string.use_photo_as_primary);
			choices[1] = getString(R.string.removePicture);
			choices[2] = getString(R.string.changePicture);
			final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
					android.R.layout.simple_list_item_1, choices);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					dialogContext);
			builder.setTitle(R.string.attachToContact);
			builder.setSingleChoiceItems(adapter, -1, this);
			builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			});
			return builder.create();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			switch (which) {
			case 0:
				break;// 什么也不做
			case 1:
				// 删除图像
				mEditor.setPhotoBitmap(null);
				break;
			case 2:
				// 替换图像
				doPickPhotoAction();
				break;
			}
		}

		private void doPickPhotoAction() {
			Context context = EditContact.this;

			// Wrap our context to inflate list items using correct theme
			final Context dialogContext = new ContextThemeWrapper(context,
					android.R.style.Theme_Light);
			String cancel="返回";
			String[] choices;
			choices = new String[2];
			choices[0] = getString(R.string.take_photo);
			choices[1] = getString(R.string.pick_photo);
			final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
					android.R.layout.simple_list_item_1, choices);

			final AlertDialog.Builder builder = new AlertDialog.Builder(
					dialogContext);
			builder.setTitle(R.string.attachToContact);
			builder.setSingleChoiceItems(adapter, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							switch (which) {
							case 0:{
								String status=Environment.getExternalStorageState();
								if(status.equals(Environment.MEDIA_MOUNTED)){//判断是否有SD卡
									doTakePhoto();// 用户点击了从照相机获取
								}
								else{
									showToast("没有SD卡");
								}
								break;
								
							}
							case 1:
								doPickPhotoFromGallery();// 从相册中去获取
								break;
							}
						}
					});
			builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
				
			});
			builder.create().show();
		}
	}

	/**
	 * 拍照获取图片
	 * 
	 */
	protected void doTakePhoto() {
		try {
			// Launch camera to take photo for selected contact
			PHOTO_DIR.mkdirs();// 创建照片的存储目录
			mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.photoPickerNotFoundText,
					Toast.LENGTH_LONG).show();
		}
	}

	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	/**
	 * 用当前时间给取得的图片命名
	 * 
	 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date) + ".jpg";
	}

	// 请求Gallery程序，获取图片
	protected void doPickPhotoFromGallery() {
		try {
			// Launch picker to choose photo for selected contact
			final Intent intent = getPhotoPickIntent();
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.photoPickerNotFoundText1,
					Toast.LENGTH_LONG).show();
		}
	}

	// 封装请求Gallery的intent
	public static Intent getPhotoPickIntent() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		intent.putExtra("return-data", true);
		return intent;
	}

	// 因为调用了Camera和Gally所以要判断他们各自的返回情况,他们启动时是这样的startActivityForResult
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case PHOTO_PICKED_WITH_DATA: {// 调用Gallery返回的
			final Bitmap photo = data.getParcelableExtra("data");
			// 下面就是显示照片了
			//缓存用户选择的图片
			img=getBitmapByte(photo);
			Log.v(TAG, "new photo set!");
			mEditor.setPhotoBitmap(photo);
			break;
		}
		case CAMERA_WITH_DATA: {// 照相机程序返回的,再次调用图片剪辑程序去修剪图片
			doCropPhoto(mCurrentPhotoFile);
			break;
		}
		}
	}

	protected void doCropPhoto(File f) {
		try {// f 是照相机拍下的照片
			// 本来这里是要把相机拍下的照片放到媒体库里面的
			// MediaScannerConnection.scanFile(
			// this,
			// new String[] { f.getAbsolutePath() },
			// new String[] { null },
			// null);
			// Launch gallery to crop the photo
			// 启动gallery去剪辑这个照片
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
		} catch (Exception e) {
			Log.e(TAG, "Cannot crop image", e);
			Toast.makeText(this, R.string.photoPickerNotFoundText,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Constructs an intent for image cropping. 调用图片剪辑程序
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 80);
		intent.putExtra("outputY", 80);
		intent.putExtra("return-data", true);
		return intent;
	}
}
