package com.bit.applock;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppLockDao {
	private Context context;
	private AppLockrDBHelper dbHelper;
	public AppLockDao(Context context) {
		this.context = context;
		dbHelper = new AppLockrDBHelper(context);
	}
	/**
	 * 查找全部包名
	 * @return
	 */
	public List<String> getPackName(){
		SQLiteDatabase db =dbHelper.getReadableDatabase();
		List<String> packnames = new ArrayList<String>();
		if(db.isOpen()){
			Cursor cursor=db.rawQuery("select packname from applock ",null);
			while (cursor.moveToNext()) {
				String packname = cursor.getString(0);
				packnames.add(packname);
			}
			cursor.close();
			db.close();
			}
		return packnames;
	}
	/**
	 * 查询
	 * @param number
	 * @return
	 */
	public boolean find(String packName){
		boolean result =false;
		SQLiteDatabase db=dbHelper.getReadableDatabase();
		if(db.isOpen()){
			Cursor cursor=db.rawQuery("select packname from applock where packname=?",new String[]{packName});
			if(cursor.moveToNext()){
				result=true;
			}cursor.close();
			db.close();
		}
		return result;
	}
	
	/**
	 * 添加
	 * @param number
	 */
	public void add(String packname){
		if(find(packname)){
			return ;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if(db.isOpen()){
			db.execSQL("insert into applock (packname) values (?)",new Object[]{packname});
			db.close();
		}
	}
	/**
	 * 删除
	 * @param number
	 */
	public void delete(String packname){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if(db.isOpen()){
			db.execSQL("delete from applock where packname=?",new Object[]{packname});
			db.close();
		}
	}
	/**
	 * 更新
	 * @param olderNumber
	 * @param newNumber
	 */
	public void update(String olderNumber ,String newNumber){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if(db.isOpen()){
			db.execSQL("update blacknumber set number=? where number=?",new Object[]{newNumber,olderNumber});
			db.close();
		}
	}
}
