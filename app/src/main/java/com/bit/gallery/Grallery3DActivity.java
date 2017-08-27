package com.bit.gallery;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wjg.phoneassistant.R;

public class Grallery3DActivity extends Activity {

	private TextView tvTitle; 	
	private GalleryView gallery; 	
	private ImageAdapter adapter;
	private DatabaseHelper helper = new DatabaseHelper(this, null, 1);
	private sqlite sqlite = new sqlite();
	private String name = "";
	private String path = "";
	private final static int ITME1 = 1;
	private final static int ITME2 = 2;
	private final static int ADD_FROM_GALLERY = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grallery_layout);
		initRes();
	}
	
	private void initRes(){
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		gallery = (GalleryView) findViewById(R.id.mygallery);
		
		//注册上下文菜单
		registerForContextMenu(gallery);
		adapter = new ImageAdapter(this); 	
		adapter.createReflectedImages();
		gallery.setAdapter(adapter);
		
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				tvTitle.setText(adapter.pictures.get(adapter.pictures.size()-position-1).name);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		gallery.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				// TODO Auto-generated method stub
				int position = ((AdapterContextMenuInfo)menuInfo).position;
				name = adapter.pictures.get(adapter.pictures.size()-position-1).name;
				menu.setHeaderTitle(name);
				menu.add(0, ITME1, ITME1, "添加");
				menu.add(0, ITME2, ITME2, "删除");
			}
			
			
		});
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch (item.getItemId()) {
		case ITME1:
			//选中“添加”
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, ADD_FROM_GALLERY);
			break;
		case ITME2:
			//选中“删除”
			sqlite.Delete(helper, name);
			adapter = new ImageAdapter(this); 	
			adapter.createReflectedImages();
			gallery.setAdapter(adapter);
			break;
		default:
			break;
		}
		
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add("添加");
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, ADD_FROM_GALLERY);
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case ADD_FROM_GALLERY:
			if(data == null) {
				super.onActivityResult(requestCode, resultCode, data);
				return;
			}
			Uri uri = data.getData();

			String name = "";
			
			ContentResolver cr = this.getContentResolver();
			Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DISPLAY_NAME, "_data"}, null, null, null);
			if(cursor.moveToFirst()) {
				name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
				name = name.substring(0, name.indexOf("."));
				path = cursor.getString(cursor.getColumnIndex("_data"));
			}
			cursor.close();
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
				byte[] bytemap = getBitmapByte(bitmap);
				ContentValues value = new ContentValues();
				value.put("picture", bytemap);
				value.put("name", name);
				value.put("path", path);
				sqlite.Insert(value, helper);
				sqlite.Update(value, name,path, helper);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("是否删除原图片")
			.setPositiveButton("是", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					File file = new File(path);
					if(!file.delete()) {
						Toast.makeText(getApplicationContext(), "删除原图片失败", Toast.LENGTH_SHORT).show();
					}
				}
			})
			.setNegativeButton("否", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			builder.create().show();
			
			adapter = new ImageAdapter(this); 	
			adapter.createReflectedImages();
			gallery.setAdapter(adapter);
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//将头像转换成byte[]以便能将图片存到数据库
	public byte[] getBitmapByte(Bitmap bitmap){
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
	
}