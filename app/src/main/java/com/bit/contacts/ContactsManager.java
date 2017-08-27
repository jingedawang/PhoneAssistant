package com.bit.contacts;

import java.util.ArrayList;
import java.util.Random;

import com.aes.base64.BackAES;
import com.aes.base64.Password;
import com.watchdata.mysms.sendSMS;
import com.watchdata.mysms.showSMS;
import com.wjg.phoneassistant.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsManager extends Activity{

	public static final String TAG = "ContactsManager";
	private ContactsManagerDbAdater contactsManagerDbAdapter;
	int groupNameIndex;
	private MyCursrTreeAdapter myCursorTreeAdapter;

	private ExpandableListView mExpandableListView;
	private AutoCompleteTextView mSearchEditText;
	private ImageButton mSearchButton;
	private LinearLayout topBar;
	View view;
	PopupWindow pop;

	Button btnSms;
	Button btnEmail;
	Button btnCall;

	// 缓存除了所选联系人所在组的所有组，用在移动联系人上 
	String groups[];

	// 缓存用户所在的组,用在移动联系人上
	String mygroupName;

	// 长按分组上的 菜单
	public static final int MENU_GROUP_ADD = Menu.FIRST;
	public static final int MENU_GROUP_DELETE = Menu.FIRST + 1;
	public static final int MENU_GROUP_MODIFY = Menu.FIRST + 2;
	public static final int MENU_GROUP_ADDCONTACT = Menu.FIRST + 3;

	// 长按联系人菜单
	public static final int MENU_CONTACTS_DELETE = Menu.FIRST;
	public static final int MENU_CONTACTS_MODIFY = Menu.FIRST + 1;
	public static final int MENU_CONTACTS_MOVE = Menu.FIRST + 2;

	// 联系人各个字段索引
	private static final int icon_index = 1;
	private static final int name_index = 2;
	private static final int description_index = 3;
	private static final int telPhone_index = 4;
	private static final int email_index = 5;

	// 组上groupName字段索引
	private static final int groupName_index = 1;
	
	// 来自sendSMS的requestCode
	private final int SEND_SMS_ADD = 3;
	
	// 全部短信
	private static final String SMS_ALL = "content://sms/";

	private Cursor groupCursor;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		initLayout();

		initMyAdapter();

		initPopupWindow();
		// 由于程序的背景不是黑色的，若不设置为0，拖动时会出现黑色
		mExpandableListView.setCacheColorHint(0);
		// 去掉每项下面的黑线(分割线)
		mExpandableListView.setDivider(null);
		// 自定义组的左边的下拉图标状态变化
		mExpandableListView.setGroupIndicator(getResources().getDrawable(
				R.drawable.expander_ic_folder));
	}

	/**
	 * *******************************各种初始化函数*******************************
	 */
	private void initLayout() {
		/*
		 * 由于ExpandableListView与其他组件似乎不能直接嵌套在一个 布局文件里面，但是他有个增加头布局和尾布局的方法，我们可以
		 * 结合LayoutInflater和这个两个方法就可以在含有ExpandableListView 与ListView的布局中添加其他控件了
		 */
		// 初始化搜索栏布局
		topBar = (LinearLayout) getLayoutInflater().inflate(R.layout.top_bar,
				null).findViewById(R.id.top_bar);
		mSearchEditText = (AutoCompleteTextView) topBar
				.findViewById(R.id.search);
		mSearchButton = (ImageButton) topBar.findViewById(R.id.add_contact);
		// 初始化ExpandableListView布局
		mExpandableListView = (ExpandableListView) findViewById(R.id.list);
		registerForContextMenu(mExpandableListView);
		mExpandableListView.addHeaderView(topBar);
		mExpandableListView.setOnTouchListener(mExpandTouchListener);
		
		// 打开数据库
		contactsManagerDbAdapter = new ContactsManagerDbAdater(getApplicationContext());
		contactsManagerDbAdapter.open();
		// 绑定按钮监听
		mSearchButton.setOnClickListener(mSearchListener);
		mExpandableListView.setOnChildClickListener(mContactClickListener);
	}

	private void initPopupWindow() {
		// 联系人菜单弹出框初始化
		view = this.getLayoutInflater().inflate(R.layout.popup_window, null);
		pop = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		pop.setOutsideTouchable(true);
		btnSms = (Button) view.findViewById(R.id.btnSms);
		btnEmail = (Button) view.findViewById(R.id.btnEmail);
		btnCall = (Button) view.findViewById(R.id.btnCall);
	}

	// 给适配器赋值，刷新界面的时候也会用到
	public void initMyAdapter() {
		// 初始化ExpandableListView数据
		
		groupCursor = contactsManagerDbAdapter.getAllGroups();
		
	//	startManagingCursor(groupCursor);
		// get the groupName column index
		groupNameIndex = groupCursor.getColumnIndexOrThrow("groupName");
		// set my adapter
		myCursorTreeAdapter = new MyCursrTreeAdapter(groupCursor, this, true);
		mExpandableListView.setAdapter(myCursorTreeAdapter);

		// 初始化搜索框数据
		String sql = "select name from contacts";
		Cursor contactCursor = contactsManagerDbAdapter.getCursorBySql(sql,
				null);
		//Log.w(TAG, "" + contactCursor.toString());
		contactCursor.moveToFirst();
		ArrayList<String> nameList = new ArrayList<String>();
		for (int i = 0; i < contactCursor.getCount(); i++) {
			nameList.add(contactCursor.getString(contactCursor
					.getColumnIndex("name")));
			contactCursor.moveToNext();
		}
		contactCursor.close();
		// 绑定自动完成框数据
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				ContactsManager.this,
				android.R.layout.simple_dropdown_item_1line, nameList);
		mSearchEditText.setAdapter(adapter);
	}

	/**
	 * *******************************监听器类定义*******************************
	 */
	// 联系人搜索监听器
	private OnClickListener mSearchListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			String name = mSearchEditText.getText().toString();
			// String sql="select * from contacts where name=" + name;
			String sql = "select name from contacts";
			Cursor contactCursor = contactsManagerDbAdapter.getCursorBySql(sql,
					null);
			Log.w(TAG, "" + contactCursor.toString());
			contactCursor.moveToFirst();
			for (int i = 0; i < contactCursor.getCount(); i++) {
				if (contactCursor.getString(
						contactCursor.getColumnIndex("name")).equals(name)) {
					Intent intent = new Intent();
					intent.putExtra("name", name);
					intent.setAction(Intent.ACTION_EDIT);
					intent.setDataAndType(Uri.parse(MyConstant.CONTENT_URI),
							MyConstant.CONTENT_TYPE_EDIT);
					startActivity(intent);
					contactCursor.close();
					return;
				}
				contactCursor.moveToNext();
			}
			contactCursor.close();
			Toast.makeText(ContactsManager.this, getResources().getString(R.string.contactnotfind), Toast.LENGTH_LONG)
					.show();

		}
	};
	// 联系人详细信息查看按钮
	private OnChildClickListener mContactClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			//取该子控件的TextView
			TextView name = (TextView) v.findViewById(R.id.name);
			//Uri uri = Uri.parse(MyConstant.CONTENT_URI);
			//Intent intent = new Intent(Intent.ACTION_EDIT,uri);
			
			Intent it = null;
			try {
				it = getIntent();
			}
			catch(Exception ex)
			{
				
			}
			String phoneNumber = "";
			if(it.getBooleanExtra("isFromAddContact", false)) {
				if(name.getText().toString() != null && name.getText().toString().length() > 0) {
					Cursor cur = contactsManagerDbAdapter.getCursorBySql(ContactsManagerDbAdater.TABLE_CONTACTS,
						new String[]{"name", "telPhone"}, "name=?", new String[]{name.getText().toString()});
					if(cur.moveToFirst()) {
						phoneNumber = cur.getString(cur.getColumnIndex("telPhone"));
					}
				}
				
				Intent intent = new Intent();
				intent.setClass(ContactsManager.this, sendSMS.class);
				intent.putExtra("phoneNumber", phoneNumber);
            	setResult(RESULT_OK, intent);
            	finish();
            	return false;
			}

			Intent intent = new Intent();
			intent.setType(MyConstant.CONTENT_TYPE_EDIT);
			intent.setAction(Intent.ACTION_EDIT);
			intent.setDataAndType(Uri.parse(MyConstant.CONTENT_URI),
					MyConstant.CONTENT_TYPE_EDIT);
			intent.putExtra("name", name.getText().toString());
			//intent.setType(MyConstant.CONTENT_TYPE_EDIT);
			startActivity(intent);
			return false;
		}
	};

	 //屏幕全局触控监听
	private  OnTouchListener mExpandTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (pop.isShowing()) {
				pop.dismiss();
			}
			return false;
		}
	};
	
	/**
	 * ***********************ExpandListView自定义适配器*************************
	 * CursorTreeAdapter符合ExpandableListView的需求，可以绑定需要的数据
	 */
	public class MyCursrTreeAdapter extends CursorTreeAdapter {

		public MyCursrTreeAdapter(Cursor cursor, Context context,
				boolean autoRequery) {
			super(cursor, context, autoRequery);
		}

		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			//Log.v(TAG, "bindGroupView");
			TextView groupName = (TextView) view.findViewById(R.id.groupName);
			String group = cursor.getString(groupName_index);
			groupName.setText(group);

			TextView groupCount = (TextView) view.findViewById(R.id.groupCount);
			int count = contactsManagerDbAdapter
					.getCountContactByGroupName(group);
			groupCount.setText("[" + count + "]");
		}

		@Override
		protected View newGroupView(Context context, Cursor cursor,
				boolean isExpanded, ViewGroup parent) {
			//Log.v(TAG, "newGroupView");
			LayoutInflater inflate = LayoutInflater.from(ContactsManager.this);
			View view = inflate.inflate(R.layout.grouplayout, null);

			bindGroupView(view, context, cursor, isExpanded);

			return view;
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			//Log.v(TAG, "getChildrenCursor");
			String groupName = groupCursor.getString(groupName_index);// 得到当前的组名
			Cursor childCursor = contactsManagerDbAdapter
					.getContactsByGroupName(groupName);
		//	startManagingCursor(childCursor);
			return childCursor;
		}

		@Override
		protected View newChildView(Context context, Cursor cursor,
				boolean isLastChild, ViewGroup parent) {
			//Log.v(TAG, "newChildView");
			LayoutInflater inflate = LayoutInflater.from(ContactsManager.this);
			View view = inflate.inflate(R.layout.childlayout, null);

			bindChildView(view, context, cursor, isLastChild);

			return view;
		}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {
			//Log.v(TAG, "bindChildView");
			ImageView contactIcon = (ImageView) view
					.findViewById(R.id.contactIcon);
			contactIcon.setImageBitmap(getBitmapFromByte(cursor
					.getBlob(icon_index)));

			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(cursor.getString(name_index));

			TextView description = (TextView) view
					.findViewById(R.id.description);
			description.setTextKeepState(cursor.getString(description_index));
			String phoneNumber1 = cursor.getString(telPhone_index);
			try {
					String result = BackAES.decrypt(phoneNumber1, Password.getKeyForContacts(), 0);
					  phoneNumber1=result;
					
				}
				catch (Exception e) {
					e.printStackTrace();
					
				}
			final String phoneNumber=phoneNumber1;	
			final String email = cursor.getString(email_index);
			ImageView mycursor = (ImageView) view.findViewById(R.id.myCursor);
			mycursor.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (pop.isShowing()) {
						pop.dismiss();
					} else {
						//设定弹出框显示位子
						pop.showAsDropDown(v);
						//发送短信按钮
						btnSms.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								pop.dismiss();
								
								Uri uri = Uri.parse(SMS_ALL);
								String[] projection = {"thread_id", "address"};
								//此处判断防止电话号码为空，若为空，则取消此次发短信事件
								if(phoneNumber == null || phoneNumber.length() <= 0) {
									return ;
								}		
								Cursor cur_smsdetail = getContentResolver().query(uri, projection,
										"address=? ", new String[] { phoneNumber },
										"date ASC");
								if(cur_smsdetail.moveToFirst()) {
									Intent intent = new Intent();
									intent.setClass(ContactsManager.this, showSMS.class);
									int threadId = Integer.parseInt(cur_smsdetail.getString(cur_smsdetail.getColumnIndex("thread_id")));
									Bundle bundle = new Bundle();
									bundle.putInt("threadId", threadId);
									intent.putExtras(bundle);
									ContactsManager.this.startActivity(intent);
								}
								else {
									Intent intent = new Intent();
									intent.setClass(ContactsManager.this, sendSMS.class);
									intent.putExtra("addressFromContacts", phoneNumber);
									ContactsManager.this.startActivity(intent);
								}
							}
						});
						//发送Email按钮
						btnEmail.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								pop.dismiss();
								Intent i = new Intent(Intent.ACTION_SENDTO); 
						//		i.setType("text/plain"); //模拟器请使用这行
						//		i.setType("message/rfc822") ; // 真机上使用这行
								i.setData(Uri.parse("mailto:" + email));
						//		i.putExtra(Intent.EXTRA_EMAIL, email);  
						//		i.putExtra(Intent.EXTRA_SUBJECT,"subject goes here");  
						//		i.putExtra(Intent.EXTRA_TEXT,"body goes here");  
								startActivity(Intent.createChooser(i, "Select email application."));
							}
						});
						//拨打电话
						btnCall.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								pop.dismiss();
								Uri uri = Uri.parse("tel:" + phoneNumber);
								Intent it = new Intent(Intent.ACTION_DIAL, uri);
								startActivity(it);
							}
						});
					}
				}
			});
		}
	}

	// 得到存储在数据库中的头像
	public Bitmap getBitmapFromByte(byte[] temp) {
		if (temp != null) {
			Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
			return bitmap;
		} else {
			return getRandomIcon();
		}
	}

	// 没有设置头像的联系人 得到随机图片
	public Bitmap getRandomIcon() {
		Integer allIcon[] = { R.drawable.h001, R.drawable.h002,
				R.drawable.h003, R.drawable.h004, R.drawable.h005,
				R.drawable.h006, R.drawable.h007, R.drawable.h008,
				R.drawable.h009, R.drawable.h010, R.drawable.h011,
				R.drawable.h012, R.drawable.h013, R.drawable.h014,
				R.drawable.h015, R.drawable.h016, R.drawable.h017,
				R.drawable.h018, R.drawable.h019, R.drawable.h020, };
		Random random = new Random();
		int index = random.nextInt(20);
		Resources res = getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, allIcon[index]);
		return bmp;
	}

	// 弹出提示信息
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	// ExpandableListView上长按按钮事件监听 ，并创造一个上下文菜单
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;

		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {// 在组上长按
			String title = ((TextView) info.targetView
					.findViewById(R.id.groupName)).getText().toString();
			menu.setHeaderTitle(title);
			menu.add(0, MENU_GROUP_ADD, 0, "添加分组");
			menu.add(0, MENU_GROUP_DELETE, 0, "删除分组");
			menu.add(0, MENU_GROUP_MODIFY, 0, "重命名");
			menu.add(0, MENU_GROUP_ADDCONTACT, 0, "添加联系人");

		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {// 在联系人上长按
			String title = ((TextView) info.targetView.findViewById(R.id.name))
					.getText().toString();
			Drawable icon = ((ImageView) info.targetView
					.findViewById(R.id.contactIcon)).getDrawable();
			menu.setHeaderTitle(title);
			menu.setHeaderIcon(icon);
			menu.add(0, MENU_CONTACTS_DELETE, 0, "删除联系人");
			menu.add(0, MENU_CONTACTS_MODIFY, 0, "编辑联系人");
			menu.add(0, MENU_CONTACTS_MOVE, 0, "移动联系人到...");
		}

	}
	//设置上下文按钮的逻辑处理
	public boolean onContextItemSelected(MenuItem item) {

		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();
		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			final String name = ((TextView) info.targetView
					.findViewById(R.id.name)).getText().toString();
			switch (item.getItemId()) {
			case MENU_CONTACTS_DELETE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("确定要删除联系人吗？");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								contactsManagerDbAdapter
										.deleteDataFromContacts(name);
								initMyAdapter();
								showToast(getResources().getString(R.string.deletesuccessful));
							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								dialog.dismiss();
							}
						});
				builder.show();
				break;
			}
			case MENU_CONTACTS_MODIFY: {
				Intent intent = new Intent();
				intent.putExtra("name", name);
			intent.setAction(Intent.ACTION_EDIT);
				intent.setDataAndType(Uri.parse(MyConstant.CONTENT_URI),MyConstant.CONTENT_TYPE_EDIT);
				startActivity(intent);
				break;
			}
			case MENU_CONTACTS_MOVE:
				//设置移动联系人的对话框
				createMoveContactDialog(name).show();
				break;
			}

			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			String groupName = ((TextView) info.targetView
					.findViewById(R.id.groupName)).getText().toString();
			switch (item.getItemId()) {
				case MENU_GROUP_ADD:
					createDialog("addGroup", groupName).show();
					break;
				case MENU_GROUP_DELETE:
					createDialog("deleteGroup", groupName).show();
					break;
				case MENU_GROUP_MODIFY:
					createDialog("modifyGroup", groupName).show();
					break;
				case MENU_GROUP_ADDCONTACT: {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_INSERT);
					//添加联系人时把上下文菜单所在的组名也传递过去
					intent .putExtra("groupName",groupName );
					intent.setDataAndType(Uri.parse(MyConstant.CONTENT_URI),
							MyConstant.CONTENT_TYPE_INSERT);
					startActivity(intent);
					break;
				}
			}

			return true;
		}
		return false;

	}
	//设置上下文中移动联系人按钮的对话框
	private Dialog createMoveContactDialog(final String name) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("移动联系人到...");
		builder.setSingleChoiceItems(getSpecAllGroup(name), -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 得到用户要移动到的组
						String newgroupName = groups[which];
						String sql = "update contacts set groupName=? where groupName=? and name=?";
						Object[] Args = { newgroupName, mygroupName, name };
						contactsManagerDbAdapter.updateSyncData(sql, Args);
						initMyAdapter();
						showToast(getResources().getString(R.string.movecontactsuccessfully) + newgroupName);
						dialog.dismiss();
					}
				});
		return builder.create();
	}
	//从数据库得到已经声明的所有组的名字的信息
	private String[] getSpecAllGroup(String name) {
		String sql = "select groupName from contacts where name=?";
		String selectionArgs[] = { name };
		mygroupName = contactsManagerDbAdapter.checkContactGroup(sql,
				selectionArgs);
		Cursor cursor = contactsManagerDbAdapter.getAllGroups();
		int count = cursor.getCount() - 1;
		groups = new String[count];
		int i = 0;
		while (cursor.moveToNext()) {
			String newgroupName = cursor.getString(1);
			if (!newgroupName.equals(mygroupName)) {
				groups[i] = newgroupName;
				i++;
			}
		}
		cursor.close();
		return groups;
	}
	//针对不同情况创建对话框
	private Dialog createDialog(String msg, final String groupName) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (msg.equals("addGroup")) {
			final EditText content = new EditText(this);
			builder.setTitle(getResources().getString(R.string.addgroup));
			builder.setView(content);
			builder.setPositiveButton(getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 添加新的组到数据库
							String groupName = content.getText().toString()
									.trim();
							Cursor cursor = contactsManagerDbAdapter
									.getAllGroups();
							if (!groupName.equals("")) {
								while (cursor.moveToNext()) {
									if (cursor.getString(1).equals(groupName)) {
										showToast(groupName + getResources().getString(R.string.alreadyexist));
										return;
									}
								}
								contactsManagerDbAdapter
										.inserDataToGroups(groupName);
								initMyAdapter();
								showToast(getResources().getString(R.string.addedsuccessfully));
							}
						}
					});
			builder.setNeutralButton(getResources().getString(R.string.cancel1),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();
						}
					});
			return builder.create();
		}
		if (msg.equals("deleteGroup")) {
			builder.setTitle(getResources().getString(R.string.suretodeletegroup));
			builder.setPositiveButton(getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							contactsManagerDbAdapter
									.deleteDataFromGroups(groupName);
							String sql = "delete from contacts where groupName=?";
							Object Args[] = { groupName };
							contactsManagerDbAdapter.updateSyncData(sql, Args);
							initMyAdapter();
							showToast(getResources().getString(R.string.deletesuccessful));
							System.out
									.println(">>>>>>>>>>>>>>>delete>>>>>>>>>>>>>>>>>>>>>");
						}
					});
			builder.setNeutralButton(getResources().getString(R.string.cancel1),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			return builder.create();
		}
		if (msg.equals("modifyGroup")) {
			final EditText content = new EditText(this);
			content.setText(groupName);
			builder.setTitle(getResources().getString(R.string.inputnewgroupname));
			builder.setView(content);
			builder.setPositiveButton(getResources().getString(R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String newgroupName = content.getText().toString()
									.trim();
							Cursor cursor = contactsManagerDbAdapter
									.getAllGroups();
							if (!newgroupName.equals("")) {
								while (cursor.moveToNext()) {
									if (cursor.getString(1)
											.equals(newgroupName)) {
										if (!newgroupName.equals(groupName)) {
											showToast(newgroupName + getResources().getString(R.string.alreadyexist));
											return;
										} else {
											return;
										}
									}
								}
								contactsManagerDbAdapter.updateDataToGroups(
										newgroupName, groupName);
								String sql = "update contacts set groupName=? where groupName=?";
								Object Args[] = { newgroupName, groupName };
								contactsManagerDbAdapter.updateSyncData(sql,
										Args);
								initMyAdapter();
								showToast(getResources().getString(R.string.modifiedsuccessfully));
							}
						}
					});
			builder.setNeutralButton(getResources().getString(R.string.cancel1),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			return builder.create();
		}
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 这里调用这个函数后动态刷新了联系人数据
		initMyAdapter();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (contactsManagerDbAdapter != null) {
			contactsManagerDbAdapter.close();
			contactsManagerDbAdapter = null;
		}
	}

}