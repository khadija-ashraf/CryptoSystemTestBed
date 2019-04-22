package org.secure.payment.crypto.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class DigitalSignature {
	private List<byte[]> list;
	private String path = "key/asymmetric/privateKey";
	//The constructor of Message class builds the list that will be written to the file. 
	//The list consists of the message and the signature.
	public DigitalSignature(){
		
	}
	
	//The method that signs the data using the private key that is stored in keyFile path
	public byte[] sign(byte[] data, int clientId) throws InvalidKeyException, Exception{
		Signature rsa = Signature.getInstance("SHA1withRSA"); 
		String keyFile = path+clientId;
		rsa.initSign(getPrivate(keyFile));
		rsa.update(data);
		return rsa.sign();
	}
	
	//Method to retrieve the Private Key from a file
	public PrivateKey getPrivate(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}
	
	//Method to write the List of byte[] to a file
	private void writeToFile(String filename) throws FileNotFoundException, IOException {
		File f = new File(filename);
		f.getParentFile().mkdirs();
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
	    out.writeObject(list);
		out.close();
		System.out.println("Your file is ready.");
	}
	
}

