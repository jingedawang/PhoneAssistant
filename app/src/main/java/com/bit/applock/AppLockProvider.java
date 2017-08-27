package com.bit.applock;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
/**
 * 增加程序锁的内容提供者
 * @author Administrator
 *
 */
public class AppLockProvider extends ContentProvider {
	private static final int INSERT = 10;
	private static final int DELETE = 11;
	private static Uri chageurl=Uri.parse("content://cn.wjg.applockprovider");
	private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH); 
	private AppLockDao dao;
	static{
		matcher.addURI("cn.wjg.applockprovider", "insert", INSERT);
		matcher.addURI("cn.wjg.applockprovider", "delete", DELETE);
	}
	
	@Override
	public boolean onCreate() {
		dao=new AppLockDao(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int resule=matcher.match(uri);
		if(resule==INSERT){
			String packname=(String) values.get("packname");
		
			
			dao.add(packname);
			//通知内容提供者哪一个URI要发生改变
			getContext().getContentResolver().notifyChange(chageurl, null);
		}else{
			throw new IllegalArgumentException("uri地址不正确");
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int resule=matcher.match(uri);
		if(resule==DELETE){
			String packname=selectionArgs[0];
			
			dao.delete(packname);
			getContext().getContentResolver().notifyChange(chageurl, null);
		}else{
			throw new IllegalArgumentException("uri地址不正确");
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
