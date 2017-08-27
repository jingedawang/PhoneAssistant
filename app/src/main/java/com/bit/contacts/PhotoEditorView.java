package com.bit.contacts;


import com.wjg.phoneassistant.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;


public class PhotoEditorView extends ImageView implements OnClickListener {
	
	public static final String TAG="PhotoEditorView";
	private boolean mHasSetPhoto = false;//是否设置了图片
	public static final int REQUEST_PICK_PHOTO = 1;

	public PhotoEditorView(Context context) {
		super(context);
		
	}
	public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	protected void onFinishInflate() {//当View和它的所有子对象从XML中导入之后，调用此方法
        super.onFinishInflate();
        this.setOnClickListener(this);
    }
	
	//在ImageView上面点击
	public void onClick(View v) {
		//测试使用用户点击了图片
		if(mListener!=null){
			mListener.onRequest(REQUEST_PICK_PHOTO);
		}
	}
	
	//主要负责监听ImageView是否设置了照片
	public interface EditorListener {
		
		 public void onRequest(int request);
      
   }
	
	private EditorListener mListener;
	
	public void setEditorListener(EditorListener listener) {
		mListener=listener;
	}
	
	//ImageView是否已经有了图片
	public boolean hasSetPhoto() {
        return mHasSetPhoto;
    }
	
	
	//设置默认的图片给ImageView
	public void resetDefaultPhoto(){//这里面我还可以弄个随机的图片给它
		setScaleType(ImageView.ScaleType.FIT_CENTER);
		setImageResource(R.drawable.contact_add_normal);
		setEnabled(true);
		mHasSetPhoto=false;
	}
	
	public void setPhotoBitmap(Bitmap photo) {
		if(photo==null){
			resetDefaultPhoto();
			return;
		}
		//如果不为空，那么就设置新的图片
        setImageBitmap(photo);
        setEnabled(true);
        mHasSetPhoto = true;
	}

}
