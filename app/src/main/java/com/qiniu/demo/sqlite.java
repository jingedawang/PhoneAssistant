package com.qiniu.demo;

import java.sql.Date;
import java.text.SimpleDateFormat;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class sqlite {
	
	final String SMS_URI_ALL   = "content://sms/"; 
    
	public void Insert(ContentValues values,DatabaseHelper	dbHelper ) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.insert("sms", null, values);
	}
				
	//查询账户信息

	public void QueryAll(Context c, DatabaseHelper dbHelper){
		
//		SQLiteDatabase db = dbHelper.getReadableDatabase();

		ContentResolver cr = c.getContentResolver();   
        String[] projection = new String[]{"_id", "address", "person",    
                "body", "date", "type"};   
        Uri uri = Uri.parse(SMS_URI_ALL);   
        Cursor cur = cr.query(uri, projection, null, null, "date desc"); 
        int i = 0;
        if (cur.moveToFirst()) {   
        	i++;
            String name;    
            String phoneNumber;          
            String smsbody;   
            String date;   
            String type;   
            
            int nameColumn = cur.getColumnIndex("person");   
            int phoneNumberColumn = cur.getColumnIndex("address");   
            int smsbodyColumn = cur.getColumnIndex("body");   
            int dateColumn = cur.getColumnIndex("date");   
            int typeColumn = cur.getColumnIndex("type");   
            
            do{   
                name = cur.getString(nameColumn);                
                phoneNumber = cur.getString(phoneNumberColumn);   
                smsbody = cur.getString(smsbodyColumn);   
                   
                SimpleDateFormat dateFormat = new SimpleDateFormat(   
                        "yyyy-MM-dd hh:mm:ss");   
                Date d = new Date(Long.parseLong(cur.getString(dateColumn)));   
                date = dateFormat.format(d);   
                   
                int typeId = cur.getInt(typeColumn);   
                if(typeId == 1){   
                    type = "接收";   
                } else if(typeId == 2){   
                    type = "发送";   
                } else {   
                    type = "";   
                }   
                
                ContentValues value = new ContentValues();
                value.put("name", name);
                value.put("phoneNumber", phoneNumber);
                value.put("smsbody",smsbody);
                value.put("date", date);
                value.put("type", type);
                Insert(value, dbHelper);
                
                if(smsbody == null) smsbody = "";     
            }while(cur.moveToNext() && i<100);   
        }
		
	}
	
}
	
	
	

