package com.aes.base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public class AESMessage {

	private static String initialSeed = "1234567890abcdef";
	private static String initialSeedForMac = "0123456789abcdef";
	private static String enc;
	
    /**
    * Md5 32位 or 16位 加密
    * @param plainText 
    * @return 16位加密
    */
    public static String Md5(String plainText ) {
	    StringBuffer buf = null;
	    try {
	    	MessageDigest md = MessageDigest.getInstance("MD5"); 
		    md.update(plainText.getBytes());
		    byte b[] = md.digest(); 
		    int i; 
		    buf = new StringBuffer(""); 
		    for(int offset = 0; offset < b.length; offset++) {
		    	i = b[offset];
		    	if(i<0) i+= 256;
		    	if(i<16)
		    		buf.append("0");
		    	buf.append(Integer.toHexString(i));
		    }
	    } catch (NoSuchAlgorithmException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	    return buf.toString().substring(8, 24); 
    }
	/**
	 * MD5加密算法，用于对种子和时间组成的秘钥加密。
	 * @param String val
	 * @return String 加密后字符串
	 * @throws NoSuchAlgorithmException
	 */
	public static String getMD5(String val) throws NoSuchAlgorithmException {  
	    MessageDigest md5 = MessageDigest.getInstance("MD5");  
	    md5.update(val.getBytes());  
	    byte[] m = md5.digest();//加密   
	    return getString(m);  
	}  
    private static String getString(byte[] b) {  
    	StringBuffer sb = new StringBuffer();  
        for(int i = 0; i < b.length; i ++){  
        	sb.append(b[i]);  
        }  
        return sb.toString();  
    }
    /**
     * 计算通信秘钥
     * @param String time
     * @return String 通信秘钥
     * @throws NoSuchAlgorithmException
     */
    public static String getDynKey(String mobile1, String mobile2) throws NoSuchAlgorithmException {
    	if(mobile1.length() >= 11) {
    		mobile1 = mobile1.substring(mobile1.length()-11);
    	}
    	else {
    	//防止小于11位的号码加解密失败
    		StringBuilder zero = new StringBuilder("00000000000");
            mobile1 = zero.substring(0, zero.length() - mobile1.length()) + mobile1;
    	}
    	if(mobile2.length() >= 11) {
    		mobile2 = mobile2.substring(mobile2.length()-11);
    	}
    	else {
    	//防止小于11位的号码加解密失败
    		StringBuilder zero = new StringBuilder("00000000000");
            mobile2 = zero.substring(0, zero.length() - mobile2.length()) + mobile2;
    	}
    	//防止输入的电话号码中含有空格
    	mobile1 = mobile1.replaceAll(" ", "");
    	mobile2 = mobile2.replaceAll(" ", "");
    	long num1 = Long.parseLong(mobile1);
    	long num2 = Long.parseLong(mobile2);
    	long num = num1 + num2;
    	String mobile = Long.toString(num);
    	String dynKey = Md5(initialSeed + mobile);
    	return dynKey;
    }
    /**
     * 计算认证秘钥
     * @param String time
     * @return String 认证秘钥
     * @throws NoSuchAlgorithmException
     */
    public static String getDynKeyForMac(String mobile1, String mobile2) throws NoSuchAlgorithmException {
    	if(mobile1.length() >= 11) {
    		mobile1 = mobile1.substring(mobile1.length()-11);
    	}
    	else {
    	//防止小于11位的号码加解密失败
    		StringBuilder zero = new StringBuilder("00000000000");
            mobile1 = zero.substring(0, zero.length() - mobile1.length()) + mobile1;
    	}
    	if(mobile2.length() >= 11) {
    		mobile2 = mobile2.substring(mobile2.length()-11);
    	}
    	else {
    	//防止小于11位的号码加解密失败
    		StringBuilder zero = new StringBuilder("00000000000");
            mobile2 = zero.substring(0, zero.length() - mobile2.length()) + mobile2;
    	}
    	//防止输入的电话号码中含有空格
    	mobile1 = mobile1.replaceAll(" ", "");
    	mobile2 = mobile2.replaceAll(" ", "");
    	long num1 = Long.parseLong(mobile1);
    	long num2 = Long.parseLong(mobile2);
    	long num = num1 + num2;
    	String mobile = Long.toString(num);
    	return Md5(initialSeedForMac + mobile);
    }
    /**
     * 计算认证字段
     * @param String dynKey
     * @param String msg
     * @param String dynKeyForMac
     * @return String 认证字段
     * @throws Exception
     */
    public static String getMac(String dynKey, String msg, String dynKeyForMac) throws Exception {
    	AESMessage.enc = getEnc(msg, dynKey);
    	return Md5(AESMessage.enc + dynKeyForMac);
    }
    public static String getMac(String enc, String dynKeyForMac) {
    	return Md5(enc + dynKeyForMac);
    }
    /**
     * 计算加密后短信内容
     * @param msg
     * @param dynKey
     * @return String 加密后短信
     * @throws Exception
     */
    public static String getEnc(String msg, String dynKey) throws Exception {
    	return new String(BackAES.encrypt(msg, dynKey, 1));
    }
    /**
     * 计算实际发送短信内容
     * @param String mac
     * @return String 加密短信
     * @throws Exception
     */
    public static String getMessage(String mac) throws Exception {
    	return mac + enc;
    }
    /**
     * 签名认证
     * @param mac
     * @param message
     * @return boolean 是否通过认证
     */
	public static boolean authentication(String mac, String message) {
		if(mac.equals(message.substring(0, 16))) {
			return true;
		}
		return false;
	}
	/**
	 * 完整解密过程
	 * @param message
	 * @param dynKey
	 * @return String 短信明文
	 * @throws Exception
	 */
	public static String getMsg(String message, String dynKey) throws Exception {
		String msg = BackAES.decrypt(message.substring(16), dynKey, 1);
		return msg;
	}
	/**
	 * 完整加密过程
	 * @param msg
	 * @param time
	 * @return String 要发送的密文
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */
	public static String getMessage(String msg, String myMobile, String toMobile) throws NoSuchAlgorithmException, Exception {
		String dynKey = getDynKey(myMobile, toMobile);
		return getMac(dynKey, msg, getDynKeyForMac(myMobile, toMobile)) + getEnc(msg, dynKey);
	}
    
}
