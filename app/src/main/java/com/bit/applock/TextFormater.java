package com.bit.applock;

import java.text.DecimalFormat;

/**
 * 文本转化
 * @author Administrator
 *
 */
public class TextFormater {
	/**
	 * 返回byte的数据大小对应的文本
	 * @param siz
	 * @return
	 */
	public static String getDataSize(long size){
		DecimalFormat formater = new DecimalFormat("####.00");
		if(size<1024){
			return size+"bytes";
		}else if(size<1024*1024){
			float kbsize=size/1024f;
			return formater.format(kbsize)+"KB";
		}else if(size<1024*1024*1024){
			float mbsize=size/1024f/1024f;
			return formater.format(mbsize)+"MB";
		}else{
			return "size: error";
		}
	}
	/**
	 * 返回kb的数据大小对应的文本
	 * @param siz
	 * @return
	 */
	public static String getKBDataSize(long size){
		return getDataSize(size*1024);
	}
}
