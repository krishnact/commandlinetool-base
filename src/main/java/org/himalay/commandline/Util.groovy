package org.himalay.commandline

import java.util.Base64.Decoder

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

import groovy.json.JsonSlurper

class Util implements AutoLogger {
	private static String KEY_PAD= "ABCDEFGHIJKLMNOP";
	
	public def getJsonConf(String confFilePath, boolean quiet = true)
	{
		JsonSlurper js = new JsonSlurper();
		File confFile = new File(confFilePath)
		if ( confFile.exists()){
			if ( !quiet){
				debug("Parsing file  with the path = ${confFile.absolutePath}")
			}else{
				trace("Parsing file  with the path = ${confFile.absolutePath}")
			}
			return js.parse(confFile);//))
		}else{
			if (!quiet){
				info("There is no conf file with the path = ${confFile.absolutePath}")
			}else{
				debug("There is no conf file with the path = ${confFile.absolutePath}")
			}
			return [:]
		}

	}
	
	public static String encrypt( String key, byte[] bytesToEncrypt)
	{
		return encrypt(key, "1234567890123456", bytesToEncrypt);
	}
	
	public static String encrypt( String key, String strToEncrypt)
	{
		return encrypt(key, strToEncrypt.getBytes())
	}
	
	public static byte[] decrypt( String key, byte[] bytesToDecrypt)
	{
		return decrypt(key,"1234567890123456",bytesToDecrypt);
	}
	
	public static String decrypt( String key, String base64ToDecrypt)
	{
		return new String(decrypt(key,base64ToDecrypt.decodeBase64()),"UTF-8")
	}
	
	public static String encrypt(String key, String initVector, byte[] value) {
		try {
			key = (key + KEY_PAD).substring(0,16)
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value);
			

			return encrypted.encodeBase64().toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	public static String decrypt(String key, String initVector, byte[] encrypted) {
		try {
			key = (key + KEY_PAD).substring(0,16)
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(encrypted);

			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

}
