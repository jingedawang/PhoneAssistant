package com.bit.applock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;
/**
 * 电话黑名单数据库创建
 * @author Administrator
 *
 */
public class AppLockrDBHelper extends SQLiteOpenHelper{

	public AppLockrDBHelper(Context context) {
		super(context, "applock.db", null, 1);
		
	}
	/**
	 * 第一次创建数据库的时候执行
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table applock (_id integer primary key autoincrement,packname varchar(30))");
	}

	/**
	 * 更新数据库的操作
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
}
