package org.secure.payment.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import org.secure.payment.crypto.util.DigitalSignature;
import org.secure.payment.model.DataPacket;
import org.secure.payment.model.PacketType;
import org.secure.payment.model.TCPConnectionDetails;

public class Utils {

	public static Integer senderClientId;
	public static Integer receiverClientId;
	public static Integer merchantBankClientId;
	public static Integer consumerBankClientId;

	public static final Integer CLIENT_ID_MIN_RANGE = 10001;
	public static final Integer CLIENT_ID_MAX_RANGE = 10004;

	public List<Integer> clientIds = new ArrayList<Integer>(Arrays.asList(
			10001, 10002, 10003, 10004));

	public static void selectRamdomClient() {
		Integer randomSenderClient = generateRandomInRange(CLIENT_ID_MIN_RANGE,
				CLIENT_ID_MAX_RANGE);
		/*while (selectedSenderConflicted(randomSenderClient)) {
			randomSenderClient = generateRandomInRange(CLIENT_ID_MIN_RANGE,
					CLIENT_ID_MAX_RANGE);
		}*/
		senderClientId = randomSenderClient;
		
		Integer randomReceiverClient = generateRandomInRange(
				CLIENT_ID_MIN_RANGE, CLIENT_ID_MAX_RANGE);
		while (selectedReceiverConflicted(randomReceiverClient)) {
			randomReceiverClient = generateRandomInRange(CLIENT_ID_MIN_RANGE,
					CLIENT_ID_MAX_RANGE);
		}
		
		receiverClientId = randomReceiverClient;
		
		Integer randomMerchantBankClient = generateRandomInRange(
				CLIENT_ID_MIN_RANGE, CLIENT_ID_MAX_RANGE);
		while (selectedMerchantBankConflicted(randomMerchantBankClient)) {
			randomMerchantBankClient = generateRandomInRange(CLIENT_ID_MIN_RANGE,
					CLIENT_ID_MAX_RANGE);
		}
		
		merchantBankClientId = randomMerchantBankClient;
		
		Integer randomConsumerBankClient = generateRandomInRange(
				CLIENT_ID_MIN_RANGE, CLIENT_ID_MAX_RANGE);
		while (selectedConsumerBankConflicted(randomConsumerBankClient)) {
			randomConsumerBankClient = generateRandomInRange(CLIENT_ID_MIN_RANGE,
					CLIENT_ID_MAX_RANGE);
		}
		
		consumerBankClientId = randomConsumerBankClient;
	}

	public static boolean selectedReceiverConflicted(Integer receiverClientId) {

		if (senderClientId != null
				&& senderClientId.compareTo(receiverClientId) == 0) {
			return true;
		}
		return false;
	}
	
	public static boolean selectedMerchantBankConflicted(Integer merchantBankId) {

		if (merchantBankId != null
				&& (merchantBankId.compareTo(senderClientId) == 0) 
						|| (merchantBankId.compareTo(receiverClientId) == 0))   {
			return true;
		}
		return false;
	}
	
	public static boolean selectedConsumerBankConflicted(Integer consumerBankId) {

		if (consumerBankId != null
				&& (consumerBankId.compareTo(senderClientId) == 0) 
						|| (consumerBankId.compareTo(receiverClientId) == 0)
						|| (consumerBankId.compareTo(merchantBankClientId) == 0))   {
			return true;
		}
		return false;
	}

	
	public static DataPacket createTransactionPacket(String transactionString,
			Integer sourceID, Integer destinationID, Integer senderClientId,
			Integer receiverClientId) {

		PacketType packetType = PacketType.TRANSACTION;
		long transactionId = Utils.generateRandomId();
		
		byte[] data = transactionString.getBytes();

		return new DataPacket(packetType, sourceID,
				destinationID, senderClientId,
				receiverClientId, data, 
				transactionId);
	}

	public static DataPacket createApprovalPacket(Integer sourceID,
			Integer destinationID, Integer senderClientId,
			Integer receiverClientId, byte[] data) {

		PacketType packetType = PacketType.APPROVAL_REQUEST;
		return new DataPacket(packetType, sourceID, destinationID,
				senderClientId, receiverClientId, data);
	}

	public static DataPacket createVerificationPacket(Integer sourceID,
			Integer destinationID, Integer senderClientId,
			Integer receiverClientId, byte[] data) {

		
		String dataStr = new String(data);

		PacketType packetType = PacketType.VERIFICATION;
		return new DataPacket(packetType, sourceID, destinationID,
				senderClientId, receiverClientId, data);
	}

	public static Integer generateRandomInRange(int min, int max) {
		Random generator = new Random();
		return new Integer(generator.nextInt((max - min) + 1) + min);
	}

	public static long generateRandomId() {
		Random generator = new Random();
		return System.currentTimeMillis() + generator.nextLong();
	}

	public static Integer getSenderClientId() {
		return senderClientId;
	}

	public static void setSenderClientId(Integer senderClientId) {
		Utils.senderClientId = senderClientId;
	}

	public static Integer getReceiverClientId() {
		return receiverClientId;
	}

	public static void setReceiverClientId(Integer receiverClientId) {
		Utils.receiverClientId = receiverClientId;
	}
	
	public static Integer getMerchantBankClientId() {
		return merchantBankClientId;
	}

	public static void setMerchantBankClientId(Integer merchantBankClientId) {
		Utils.merchantBankClientId = merchantBankClientId;
	}

	public static Integer getConsumerBankClientId() {
		return consumerBankClientId;
	}

	public static void setConsumerBankClientId(Integer consumerBankClientId) {
		Utils.consumerBankClientId = consumerBankClientId;
	}

	public static String getSHA256HashCode(String tranxString) {
		MessageDigest digest;
		String hashCode = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(tranxString.getBytes("UTF-8"));
			hashCode = DatatypeConverter.printHexBinary(hash);
//			System.out.println(new String(hash, "UTF-8"));
//			System.out.println(hashCode);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return hashCode;
	}
	
	public static void main(String args[]) {
		
	}
}
