package com.aes.base64;


public class Test {

	public static void main(String[] args) {

	//	String content = "AES这是一段测试skdfjl;askfdj;werjf;werf;lergs;lrgj;lsrgj;rthr;lhr;hytjhyhklyhrt;hjrh;ljyyie5iy5o4i;hy;rlhtdglkrtgjl;trgjtgjlrkgjl;ergj;wergj;lgjl;rtgjrtl;hjr;lh;hejk传递klfjsl;gjt;lgtjrh;ltryl;efjkwe;lfj;erlgjre;lgjerw;gljertg;ljetr;lgr;ghtlk;gjtrk;ljt;ljet;ljwe;lrgjl;rgj;klwergjwergkl;rtj;elrgjerl;gjwerl;gjerwl;gwjlskafjkljfasflasf直接选择日期进行预订并支付，我们的工作人员预订好以后，会以短信的方式告知晚会票的取票方式及藏家乐的参与方式如需网络预订，请旅行社和游客朋友提前在九网（www.jowong.com）平台上预订门票。如需现场购票，可提前一天于16:00--20：00在售票大厅购票或在当日7：00后在售票大厅购票进景区AES这是一段测试skdfjl;askfdj;werjf;werf;lergs;lrgj;lsrgj;rthr;lhr;hytjhyhklyhrt;hjrh;ljyyie5iy5o4i;hy;rlhtdglkrtgjl;trgjtgjlrkgjl;ergj;wergj;lgjl;rtgjrtl;hjr;lh;hejk传递klfjsl;gjt;lgtjrh;ltryl;efjkwe;lfj;erlgjre;lgjerw;gljertg;ljetr;lgr;ghtlk;gjtrk;ljt;ljet;ljwe;lrgjl;rgj;klwergjwergkl;rtj;elrgjerl;gjwerl;gjerwl;gwjlskafjkljfasflasf直接选择日期进行预订并支付，我们的工作人员预订好以后，会以短信的方式告知晚会票的取票方式及藏家乐的参与方式如需网络预订，请旅行社和游客朋友提前在九网（www.jowong.com）平台上预订门票。如需现场购票，可提前一天于16:00--20：00在售票大厅购票或在当日7：00后在售票大厅购票进景区AES这是一段测试skdfjl;askfdj;werjf;werf;lergs;lrgj;lsrgj;rthr;lhr;hytjhyhklyhrt;hjrh;ljyyie5iy5o4i;hy;rlhtdglkrtgjl;trgjtgjlrkgjl;ergj;wergj;lgjl;rtgjrtl;hjr;lh;hejk传递klfjsl;gjt;lgtjrh;ltryl;efjkwe;lfj;erlgjre;lgjerw;gljertg;ljetr;lgr;ghtlk;gjtrk;ljt;ljet;ljwe;lrgjl;rgj;klwergjwergkl;rtj;elrgjerl;gjwerl;gjerwl;gwjlskafjkljfasflasf直接选择日期进行预订并支付，我们的工作人员预订好以后，会以短信的方式告知晚会票的取票方式及藏家乐的参与方式如需网络预订，请旅行社和游客朋友提前在九网（www.jowong.com）平台上预订门票。如需现场购票，可提前一天于16:00--20：00在售票大厅购票或在当日7：00后在售票大厅购票进景区AES这是一段测试skdfjl;askfdj;werjf;werf;lergs;lrgj;lsrgj;rthr;lhr;hytjhyhklyhrt;hjrh;ljyyie5iy5o4i;hy;rlhtdglkrtgjl;trgjtgjlrkgjl;ergj;wergj;lgjl;rtgjrtl;hjr;lh;hejk传递klfjsl;gjt;lgtjrh;ltryl;efjkwe;lfj;erlgjre;lgjerw;gljertg;ljetr;lgr;ghtlk;gjtrk;ljt;ljet;ljwe;lrgjl;rgj;klwergjwergkl;rtj;elrgjerl;gjwerl;gjerwl;gwjlskafjkljfasflasf直接选择日期进行预订并支付，我们的工作人员预订好以后，会以短信的方式告知晚会票的取票方式及藏家乐的参与方式如需网络预订，请旅行社和游客朋友提前在九网（www.jowong.com）平台上预订门票。如需现场购票，可提前一天于16:00--20：00在售票大厅购票或在当日7：00后在售票大厅购票进景区";
		String content = "你好吗哈哈哈123456789,....";
		String skey = "12345";
		try {
			//加密
			byte[] encryptResultStr = BackAES.encrypt(content, skey, 0);

//			System.out.println("方法-加密后："+new String(encryptResultStr));
			String decryptString=BackAES.decrypt(new String(encryptResultStr),
					skey, 0);
//			System.out.println("方法-解密后："+decryptString);
			
			/**
			 * String parseByte2HexStr(byte buf[]) //**将二进制转换成16进制
			 *byte[] parseHexStr2Byte(String hexStr) //java将16进制转换为二进制
			 */
			
			
//			String skey2 = "admin";
//			byte[] encryptResultStr2 = BackAES.newencrypt(content, skey2);
//		
//			//System.out.println("方法二加密后："+new String(encryptResultStr2));
//			////.java将2进制数据转换成16进制parseHexStr2Byte
//			String toByteString=BackAES.parseByte2HexStr(encryptResultStr2);//java将16进制转换为二进制
//			System.out.println("方法二加密后转成16进制："+toByteString);
//			
//			
//			// /**将16进制转换成2进制
//
//			byte[] ascToByte=BackAES.parseHexStr2Byte(toByteString);
//			System.out.println("方法二解密密后转成二进制："+new String(ascToByte));
//			
//			byte[] decryptString2=BackAES.newdecrypt(ascToByte,skey2);
//			System.out.println("方法二解密后："+new String(decryptString2));
//			
		
			
			
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		// String result1 = Base64.encode(encryptResultStr, chartCode);
		// System.out.println("Base64转码后：" + result1);
		//
		// System.out.println("============================================");
		// String result2 = Base64.decode(result1, chartCode);
		// System.out.println("Base64解码后：" + result2);

	}
}
