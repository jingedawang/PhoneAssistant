package com.bit.gallery;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class sqlite {

	public  class   items { 
		public String name = ""; 
		public byte[] picture = null; 
	}

	public void Create(DatabaseHelper	dbHelper) {

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
	}
			
	public void Delete(DatabaseHelper dbHelper,String name) {

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String sql="delete from gallery where name = '" + name + "'";;
		db.execSQL(sql);
	}
    
	public void Insert(ContentValues values,DatabaseHelper dbHelper ) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insert("gallery", null, values);
	}
	
	
	public void Update(ContentValues values,String name,String path,DatabaseHelper	dbHelper) {
		// TODO Auto-generated method stub

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.update("gallery", values, "name=? and path = ? ", new String[]{name,path});
	}
	
				
	//查询账户信息

	public ArrayList<items> QueryAll(DatabaseHelper	dbHelper){

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ArrayList<items> res = new ArrayList<items>(); 
		Cursor cursor = db.query("gallery", new String[]{"name","picture"},null, null, null, null, null);
		
		while(cursor.moveToNext()){
			items it=new items();
			it.name = cursor.getString(cursor.getColumnIndex("name"));
			it.picture = cursor.getBlob(cursor.getColumnIndex("picture"));
			res.add(it);
		}
		
		cursor.close();
		return res;
	}
	
}
	
	
	

