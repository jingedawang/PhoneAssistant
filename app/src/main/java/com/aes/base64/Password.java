package com.aes.base64;

public class Password {
	
	private static char privateKey = 'c';
	public final static String flag = "bit";
//	private static String publicKey;
	
	public static char getPrivateKey() {
		return privateKey;
	}
	
//	public static String getPublicKey() {
//		return publicKey;
//	}
	
	public static String getSecretPublicKey(String value) {
		byte[] bt = value.getBytes();
		for(int i=0; i<bt.length; i++) {
			bt[i] = (byte) (bt[i]^(int)privateKey);
		}
		return new String(bt, 0, bt.length);
	}
	
//	public static void setPublicKey(String key) {
//		publicKey = key;
//	}
	
	public static String keyTransfer(String str) {
		char[] c = new String("0000").toCharArray();
		int j = 0;
		for(int i=0; j<4; i++) {
			if(i%10 == 0) {
				if(i>=str.length()-1) {
					break;
				}
				c[j] = str.charAt(i);
				j++;
			}
		}
		return new String(c);
	}
	
	public static String getKeyForContacts() {
		return "wjghypqhb172184";
	}
	
	public static String getKeyForPassword() {
		return "qwertyuiopasdfgh";
	}
	
}
