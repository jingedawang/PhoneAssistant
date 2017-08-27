package com.watchdata.mysms;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class receiveSMS extends BroadcastReceiver {

	/* 声明静态字符串，并使用android.provider.Telephony.SMS_RECEIVED作为Action为短信的依据 */
	private static final String mACTION = "android.provider.Telephony.SMS_RECEIVED";
	public  String num;
	public  String det;
	public  String tim;
	
	public receiveSMS() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		/*判断传来intent是否为短信*/
		
		if(intent.getAction().equals(mACTION)){
			/*构建一字符串集合变量sb*/
			StringBuilder sb=new StringBuilder();
			/*接收由intent传来的数据*/
			Bundle bundle=intent.getExtras();
			/*判断intent是否有数据*/
			if(bundle!=null){
				/*pdus为android内置短信参数identifier，
				 * 通过bundle.get("")返回一个包含pdus的对象*/
				Object[] myOBJpdus=(Object[])bundle.get("pdus");
				SmsMessage[] messages=new SmsMessage[myOBJpdus.length];
				for(int i=0;i<myOBJpdus.length;i++){
					messages[i]=SmsMessage.createFromPdu((byte[])myOBJpdus[i]);
				}
				/*将送来的短信合并并自定义信息于StringBuilder当中*/
				for(SmsMessage currentMessage:messages){
					sb.append("来自：\n");
					/*取得发信人的电话号码，并存储到num里面*/
					num=currentMessage.getDisplayOriginatingAddress();
					sb.append(currentMessage.getDisplayOriginatingAddress());
					sb.append("\n的短信\n");
					/*取得信息的body，并存储到det里面*/
					
					det = "Mask";
					
				//	det=currentMessage.getDisplayMessageBody();
					sb.append(det);
					/*获取当前接受短信的时间*/
					Calendar calendar=Calendar.getInstance();
					int year=calendar.get(Calendar.DAY_OF_YEAR);
					int day=calendar.get(Calendar.DAY_OF_MONTH);
					int hour=calendar.get(Calendar.HOUR_OF_DAY);
					int minute=calendar.get(Calendar.MINUTE);
					/*把获取的时间保存在tim里面*/
					tim=(String)(day+"天"+hour+"消失"+minute+"分");
				}
			}
			
		}

	}
}
