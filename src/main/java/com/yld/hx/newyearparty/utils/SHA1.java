package com.yld.hx.newyearparty.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {
	
	public static String TOSHA1(String inStr) {
		MessageDigest md = null;
		String outStr = null;
		try {
			md = MessageDigest.getInstance("SHA-1"); 
			byte[] digest = md.digest(inStr.getBytes());
			outStr = bytetoString(digest);
		} catch (NoSuchAlgorithmException nsae) {
			nsae.printStackTrace();
		}
		return outStr;
	}

	public static String bytetoString(byte[] b) {
		StringBuilder sbDes = new StringBuilder();  
        String tmp = null;  
        for (int i = 0; i < b.length; i++) {  
            tmp = (Integer.toHexString(b[i] & 0xFF));  
            if (tmp.length() == 1) {  
                sbDes.append("0");  
            }  
            sbDes.append(tmp);  
        }  
        return sbDes.toString();
	}
}
