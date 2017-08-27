package com.bit.contacts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import com.aes.base64.BackAES;
import com.aes.base64.Password;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;


public class ContactsManagerDbAdater {
	public static final String TAG="ContactsManagerDbAdater";
	public static final String DATABASE_NAME="contactsmanager.db";
	public static final int DATABASE_VERSON=3;
	public static final String TABLE_CONTACTS="contacts";
	public static final String TABLE_GROUPS="groups";
	public static final String TABLECONTACTS=
		"create table contacts("+
		"_id INTEGER PRIMARY KEY,"+//rowID
		"name TEXT  NOT NULL,"+ //姓名
		"contactIcon BLOB,"+ //联系人图标
		"telPhone TEXT NOT NULL,"+ //电话号码
		"groupName TEXT,"+ //所属组名
		"birthday TEXT,"+ //生日
		"address TEXT,"+ //地址
		"email TEXT NOT NULL,"+ //邮箱
		"description TEXT NOT NULL,"+ //好友描述
		"createTime TEXT,"+ //创建时间
		"modifyTime TEXT"+ //修改时间
		");";
	public static final String TABLEGROUPS=
		"create table groups("+
		"_id INTEGER PRIMARY KEY,"+ //rowId
		"groupName TEXT UNIQUE NOT NULL,"+ //组名
		"createTime TEXT,"+ //创建时间
		"modifyTime TEXT"+ //修改时间
		");";
	
	private Context context;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase mSQLiteDatabase = null;
	
	
	
	public ContactsManagerDbAdater(Context context){
		this.context=context;
	}
	
	public void open(){
		dbHelper=new DatabaseHelper(context);
		mSQLiteDatabase=dbHelper.getWritableDatabase();
	}
	
	public void close(){
		dbHelper.close();
	//	Log.i(TAG, "DB close");
	}
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSON);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "create table start...");
			
			db.execSQL(TABLECONTACTS);
			db.execSQL(TABLEGROUPS);
			//创建临时的组
			String tempGroups[]={"亲人","朋友","同学","同事"};
			for(int i=0;i<tempGroups.length;i++){
				String sql="insert into groups values(?,?,null,null)";
				Object[] bindArgs={i+1,tempGroups[i]};
				db.execSQL(sql,bindArgs);
			}
			
			
	/*
			String tempName[]={
					"android",
					"google",
					"windows mobile",
					"microsoft",
					"symbian",
					"nokia",
					"bada",
					"sumsung",
					"IBM",
					"QQ"
			};
			
			Random random=new Random();
			int index=0;
			String publicKey = Password.getKeyForContacts();
			//判断是否取回了publicKey，如果没有，则不发送，直接返回
			//解决bug7-14-1

			String phn="";
			try {
				
				byte[] encryptResultStr = BackAES.encrypt("15927614509", publicKey, 0);
				   phn = new String(encryptResultStr);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			for(int i=0;i<10;i++){
				String sql="insert into contacts values(?,?,null,'"+phn+"',?,'1986-11-03','杭州','lhb@163.com',?,null,null)";
				index=random.nextInt(tempGroups.length);
				//Object[] bindArgs={i+1,tempName[i],tempGroups[index],"this is a scroll text,you can move cursor to here move it..."};
				Object[] bindArgs={i+1,tempName[i],tempGroups[index],"这里可以添加好友描述的相关内容"};
				db.execSQL(sql, bindArgs);
			}
			
			Log.i(TAG, "create table over...");
			
	*/
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "contactsmanager.db Upgrade...");
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_CONTACTS);
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_GROUPS);
			onCreate(db);
			
		}
		
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
	
	
	//table contacts
	public static final String contacts_id="_id";
	public static final String contacts_icon="contactIcon";
	public static final String contacts_name="name";
	public static final String contacts_description="description";
	public static final String contacts_telPhone="telPhone";
	public static final String contacts_email="email";
	String contactProjection[]={
				contacts_id,
				contacts_icon,
				contacts_name,
				contacts_description,
				contacts_telPhone,
				contacts_email
			}; 
	
	//table groups
	public static final String groups_id="_id";
	public static final String groups_groupName="groupName";
	String groupsProjection[]={
			groups_id,
			groups_groupName
		};
	
	//查找所有组
	public Cursor getAllGroups(){
		return mSQLiteDatabase.query(
				TABLE_GROUPS, 
				groupsProjection, 
				null, null, null, null, null);
		
	}
	//得到给定组的所有成员
	public Cursor getContactsByGroupName(String groupName){
		return mSQLiteDatabase.query(
				TABLE_CONTACTS, 
				contactProjection, 
				"groupName='"+groupName+"'", 
				null, null, null, null);
	}
	//统计给定组的人数
	public int getCountContactByGroupName(String groupName){
		int count=0;
		String sql="select count(*) from contacts where groupName='"+groupName+"'";
		Cursor cursor=mSQLiteDatabase.rawQuery(sql, null);
		if(cursor.moveToFirst()){
			count=cursor.getInt(0);
		}
		cursor.close();
		return count;
	}
	
	//同步更新contacts里groupName字段信息
	public void updateSyncData(String sql,Object[] Args){
		mSQLiteDatabase.execSQL(sql, Args);
	}
	
	//查询联系人在哪个组
	public String checkContactGroup(String sql,String selectionArgs[]){
		String groupName="";
		Cursor cursor=mSQLiteDatabase.rawQuery(sql, selectionArgs);
		if(cursor.moveToFirst()){
			groupName=cursor.getString(0);
		}
		cursor.close();
		return groupName;
	}
	
	//查询
	public Cursor getCursorBySql(String sql,String selectionArgs[]){
		return mSQLiteDatabase.rawQuery(sql, selectionArgs);
	}
	
	//查询
	public Cursor getCursorBySql(String table, String[] columns, String selection, String[] selectionArgs) {
		return mSQLiteDatabase.query(table, columns, selection, selectionArgs, null, null, null);
	}
	
	//添加一个组
	public long inserDataToGroups(String groupName){
		
		String formatTime=getSysNowTime();
		ContentValues content=new ContentValues();
		content.put(groups_groupName, groupName);
		content.put("createTime", formatTime);
		content.put("modifyTime", formatTime);
		return mSQLiteDatabase.insert(TABLE_GROUPS, null, content);
		
	}
	
	//删除一个组
	public int deleteDataFromGroups(String groupName){
		return mSQLiteDatabase.delete(TABLE_GROUPS, "groupName='"+groupName+"'", null);
	}
	
	//更新一个组
	public int updateDataToGroups(String newgroupName,String oldgroupName){
		String formatTime=getSysNowTime();
		ContentValues content=new ContentValues();
		content.put(groups_groupName, newgroupName);
		content.put("modifyTime", formatTime);
		return mSQLiteDatabase.update(TABLE_GROUPS, content, "groupName='"+oldgroupName+"'", null);
	}
	
	//添加一个联系人
	public long inserDataToContacts(MyContacts contactInfo){
			String formatTime=getSysNowTime();
			ContentValues content=new ContentValues();
			content.put("name", contactInfo.getName());
			content.put("birthday", contactInfo.getBirthday());
			content.put("address", contactInfo.getAddress());
			String phon=contactInfo.getTelPhone();
			
			
			String publicKey = Password.getKeyForContacts();
			
			/* 此处进行加密！！！
			 * 此处进行加密！！！
			 * 此处进行加密！！！
			 */
			try {
				byte[] encryptResultStr = BackAES.encrypt(phon, publicKey, 0);
				   phon = new String(encryptResultStr);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			content.put("telPhone", phon);
			
			
			content.put("email", contactInfo.getEmail());
			content.put("contactIcon", contactInfo.getContactIcon());
			content.put("description", contactInfo.getDescription());
			content.put("groupName", contactInfo.getGroupName());
			content.put("createTime", formatTime);
			content.put("modifyTime", formatTime);
			return mSQLiteDatabase.insert(TABLE_CONTACTS, null, content);
			
		}
		
	//删除一个联系人
	public int deleteDataFromContacts(String name){
		return mSQLiteDatabase.delete(TABLE_CONTACTS, "name='"+name+"'", null);
	}
	
	//更新联系人
	/**
	 * 
	 * contactInfo:用户重新编辑的联系人信息
	 * name:编辑的是哪个联系人
	 */
	public int updateDataToContacts(MyContacts contactInfo,String name){
		String formatTime=getSysNowTime();
		ContentValues content=new ContentValues();
		content.put("name", contactInfo.getName());
		content.put("birthday", contactInfo.getBirthday());
		content.put("address", contactInfo.getAddress());
		String phon=contactInfo.getTelPhone();
		
		String publicKey = Password.getKeyForContacts();
		
		/* 此处进行加密！！！
		 * 此处进行加密！！！
		 * 此处进行加密！！！
		 */
		try {
			byte[] encryptResultStr = BackAES.encrypt(phon, publicKey, 0);
			   phon = new String(encryptResultStr);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		content.put("telPhone", phon);
		content.put("email", contactInfo.getEmail());
		content.put("contactIcon", contactInfo.getContactIcon());
		content.put("description", contactInfo.getDescription());
		content.put("groupName", contactInfo.getGroupName());
		content.put("modifyTime", formatTime);
		return mSQLiteDatabase.update(TABLE_CONTACTS, content, "name=?", new String[]{name});
	}
	
	//get sysTime
	public String getSysNowTime(){
		Date now=new Date();
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
		String formatTime=format.format(now);
		return formatTime;
	}
}
