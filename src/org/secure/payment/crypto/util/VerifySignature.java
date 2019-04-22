package org.secure.payment.crypto.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

public class VerifySignature {
	private String path = "key/asymmetric/publicKey";
	
	public VerifySignature(){
		
	}
	
	//Method for signature verification that initializes with the Public Key, 
	//updates the data to be verified and then verifies them using the signature
	public boolean verifySignature(byte[] data, byte[] signature, int clientId) throws Exception {
		Signature sig = Signature.getInstance("SHA1withRSA");
		String keyFile = path +clientId;
		sig.initVerify(getPublic(keyFile));
		sig.update(data);
		
		return sig.verify(signature);
	}
	
	//Method to retrieve the Public Key from a file
	public PublicKey getPublic(String filename) throws Exception {
		byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(spec);
	}
}
