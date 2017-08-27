package com.wjg.phoneassistant;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class HelpActivity extends Activity {
	public TextView contancthelpview=null;
	public TextView smshelpview=null;
	public TextView passwordhelpview=null;
	public TextView lockhelpview=null;
	public TextView picturehelpview=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
	}

}
