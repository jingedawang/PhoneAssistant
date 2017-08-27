/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bit.gesturebuilder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aes.base64.Password;
import com.bit.gesturebuilder.GestureBuilderActivity.NamedGesture;
import com.watchdata.mysms.sendSMS;
import com.watchdata.mysms.showSMS;
import com.wjg.phoneassistant.R;

public class CreateGestureActivity extends Activity {
    private static final float LENGTH_THRESHOLD = 120.0f;

    private Gesture mGesture;
    private View mDoneButton;
    private Button keyFinished;
    
 //   private static GestureLibrary sStore;
	//SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
	//Editor editor = pref.edit();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.create_gesture);
        
        mDoneButton = findViewById(R.id.done);
        keyFinished = (Button) findViewById(R.id.btn_msgPublicKey);
        
        Intent intent = getIntent();
        if(intent.getBooleanExtra("isFromSMS", false) || intent.getBooleanExtra("isFromSendSMS", false)) {
        	
        	//设置显示“Finished”按钮，隐藏“Done”按钮
        	keyFinished.setVisibility(View.VISIBLE);
        	mDoneButton.setVisibility(View.GONE);
        	keyFinished.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Bitmap map = mGesture.toBitmap(6, 6, 0, Color.GREEN);
		            String str = "";
		            for(int x=0; x<3; x++) {
		            	for(int y=0; y<3; y++) {
		            		str = str + map.getPixel(x, y) + y;
		            	}
		            	str = str + x;
		            }
		            Intent intent = new Intent();
		            if(intent.getBooleanExtra("isFromSMS", false)) {
		            	intent.setClass(CreateGestureActivity.this, showSMS.class);
		            }
		            else {
		            	intent.setClass(CreateGestureActivity.this, sendSMS.class);
		            }
		            intent.putExtra("SecretPublicKey", Password.keyTransfer(str));
	            	setResult(RESULT_OK, intent);
	            	finish();
				}
			});
        }
        else {
        	//设置隐藏“Finished”按钮，显示“Done”按钮
        	keyFinished.setVisibility(View.GONE);
        	mDoneButton.setVisibility(View.VISIBLE);
        }
        
        

        GestureOverlayView overlay = (GestureOverlayView) findViewById(R.id.gestures_overlay);
        overlay.addOnGestureListener(new GesturesProcessor());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mGesture != null) {
            outState.putParcelable("gesture", mGesture);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        mGesture = savedInstanceState.getParcelable("gesture");
        if (mGesture != null) {
            final GestureOverlayView overlay =
                    (GestureOverlayView) findViewById(R.id.gestures_overlay);
            overlay.post(new Runnable() {
                public void run() {
                    overlay.setGesture(mGesture);
                }
            });

            mDoneButton.setEnabled(true);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void addGesture(View v) {
        if (mGesture != null) {
            final TextView input = (TextView) findViewById(R.id.gesture_name);
            final CharSequence name = input.getText();
            if (name.length() == 0) {
                input.setError(getString(R.string.error_missing_name));
                return;
            }
            
            
            final GestureLibrary store = GestureBuilderActivity.getStore();
            

            //确定已注册密码  不再进入  初始化界面
            SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
	    	Editor editor = pref.edit();
	    	editor.putBoolean("isFirstIn", false);
			editor.commit();
			
	    	//删除  名字为Entry 的条目  使密码只有一个
			store.removeEntry("登陆手势");
	        store.addGesture(name.toString(), mGesture);
	        store.save();
	        
            
            setResult(RESULT_OK);

            String path = "/data/data/com.wjg.phoneassistant/gesture";
         //   Toast.makeText(this, getString(R.string.save_success, path), Toast.LENGTH_LONG).show();
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
        
    }
    
    @SuppressWarnings({"UnusedDeclaration"})
    public void cancelGesture(View v) {
        setResult(RESULT_CANCELED);
        finish();
    }
    
    private class GesturesProcessor implements GestureOverlayView.OnGestureListener {
        public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
            mDoneButton.setEnabled(false);
            mGesture = null;
        }

        public void onGesture(GestureOverlayView overlay, MotionEvent event) {
        }

        public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
            mGesture = overlay.getGesture();
            if (mGesture.getLength() < LENGTH_THRESHOLD) {
                overlay.clear(false);
            }
            mDoneButton.setEnabled(true);
        }

        public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
        }
    }

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(getIntent().getBooleanExtra("isFromSMS", true)) {
			Intent intent = new Intent();
			intent.putExtra("callCreateGestureActivity", true);
			intent.setClass(CreateGestureActivity.this, showSMS.class);
			setResult(RESULT_OK, intent);
			finish();
		}
		else if(getIntent().getBooleanExtra("isFromSendSMS", false)) {
			Intent intent = new Intent();
			intent.setClass(CreateGestureActivity.this, sendSMS.class);
			intent.putExtra("callCreateGestureActivity", true);
			setResult(RESULT_OK);
		}
		
	}
    
    
}
