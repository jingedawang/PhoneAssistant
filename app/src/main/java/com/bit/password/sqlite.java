package com.bit.password;

import java.util.ArrayList;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class sqlite {

	public  class   items { 
		public String wangzhan = ""; 
		public String account = ""; 
		public String password = ""; 
	}

	public void Create(DatabaseHelper	dbHelper) {

		dbHelper.getReadableDatabase();
		
	}
			
	public void Delete(DatabaseHelper	dbHelper,String wangzhan,String account) {

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String sql="delete from Notes where  wangzhan= '"+wangzhan+"' and account='"+account+"'";//+account;
		db.execSQL(sql);
	}
    
	public void Insert(ContentValues values,DatabaseHelper	dbHelper ) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insert("Notes", null, values);
	}
	
	public void Update(ContentValues values,String wangzhan,String zhanghao,DatabaseHelper	dbHelper) {
		// TODO Auto-generated method stub

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.update("Notes", values, "wangzhan=? and account=?", new String[]{wangzhan,zhanghao});
	}
				
	//查询账户信息

	public ArrayList<items> QueryAll(DatabaseHelper	dbHelper){

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ArrayList<items> res = new ArrayList<items>(); 
		Cursor cursor = db.query("Notes", new String[]{"account","wangzhan","password"},null, null, null, null, null);
		
		while(cursor.moveToNext()){
			items it=new items();
			it.account = cursor.getString(cursor.getColumnIndex("account"));
			it.wangzhan = cursor.getString(cursor.getColumnIndex("wangzhan"));
			
			
			
			it.password = cursor.getString(cursor.getColumnIndex("password"));
			
			
			
			
			res.add(it);
		}
		return res;
	}
	
}
	
	
	

