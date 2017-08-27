package com.bit.applock;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.widget.Toast;

/**
 * MD5加密
 * 
 * @author Administrator
 * 
 */
public class MD5Encoder {
	public static String encode(String pwd) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] bytes = digest.digest(pwd.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < bytes.length; i++) {
				String s = Integer.toHexString(0xff & bytes[i]);
				if (s.length() == 1) {
					sb.append("0" + s);
				} else {
					sb.append(s);
				}
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("加密出现问题！！！");
			throw new RuntimeException("buhuifasheng");
		}

	}
}
