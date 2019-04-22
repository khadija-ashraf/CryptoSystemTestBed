package org.secure.payment.crypto.util;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricAES {
	static Cipher cipher;
	private String path = "key/symmetric/secretKey";

	public static void main(String[] args) throws Exception {
		
		SymmetricAES aes = new SymmetricAES();
		SecretKey secretKey = aes.readSecretKeyFromFile(10001);
		
		String plainText = "AES Symmetric Encryption Decryption";
		System.out.println("Plain Text Before Encryption: " + plainText);

		String encryptedText = aes.encrypt(plainText.getBytes(), secretKey);
		System.out.println("Encrypted Text After Encryption: " + encryptedText);

		String decryptedText = aes.decrypt(encryptedText, secretKey);
		System.out.println("Decrypted Text After Decryption: " + decryptedText);
	}

	public SymmetricAES() throws NoSuchAlgorithmException, NoSuchPaddingException{
		cipher = Cipher.getInstance("AES");
	}

	//Method to retrieve the Public Key from a file
	public SecretKeySpec readSecretKeyFromFile(int clientId) throws Exception {
		String filename = path +clientId;

		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
	    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
		return secretKeySpec;
	}
		
	public String encrypt(byte[] plainText, SecretKey secretKey)
			throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedByte = cipher.doFinal(plainText);
		Base64.Encoder encoder = Base64.getEncoder();
		String encryptedText = encoder.encodeToString(encryptedByte);
		return encryptedText;
	}

	public String decrypt(String encryptedText, SecretKey secretKey)
			throws Exception {
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] encryptedTextByte = decoder.decode(encryptedText);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
		String decryptedText = new String(decryptedByte);
		return decryptedText;
	}
}
