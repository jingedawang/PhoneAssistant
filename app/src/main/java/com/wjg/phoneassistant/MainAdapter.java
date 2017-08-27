package com.wjg.phoneassistant;

import com.wjg.phoneassistant.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	
	private static final int[] icons={R.drawable.contact_main,R.drawable.sms_main,R.drawable.passwordbook_main,R.drawable.lock_main,/*R.drawable.picture_main,*/R.drawable.settings_main};
   
	private static String[] names = {"","","","",""};
   
	public MainAdapter(Context context){
	   names[0] = context.getString(R.string.contact_main);
	   names[1] = context.getString(R.string.sms_main);
	   names[2] = context.getString(R.string.passwordbook_main);
	   names[3] = context.getString(R.string.lock_main);
//	   names[4] = context.getString(R.string.picture_main);
	   names[4] = context.getString(R.string.settings_main);
	   inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	   
	   
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return names.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view= inflater.inflate(R.layout.activity_mainitem, null);
		TextView tv_name=(TextView) view.findViewById(R.id.tv_main_item_name);
		ImageView iv_icon=(ImageView)view.findViewById(R.id.iv_main_item_icon);
		tv_name.setText(names[position]);
		iv_icon.setImageResource(icons[position]);
		return view;
	}
         
}
