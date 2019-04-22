package org.secure.payment.model;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.secure.payment.crypto.util.DigitalSignature;
import org.secure.payment.crypto.util.SymmetricAES;
import org.secure.payment.util.Utils;

public class DataPacket implements Serializable, Cloneable {

	private static final long serialVersionUID = 5950169519310163575L;

	PacketType packetType;
	Integer sourceId;
	Integer destinationId;
	Integer senderClientId;
	Integer receiverClientId;
	private byte[] data;
	
	private String hashcode;
	private byte[] digitalSignature;
	private String cipherText;
	
	long transactionId;
	long packetId = Utils.generateRandomId();

	long travelStartTime = System.currentTimeMillis();
	long travelEndTime;
	int totalTrasnmittedBits;
	
	private DataPacket() {
	}

	public DataPacket(DataPacket dataPacket) {
		this.data = dataPacket.data;
	}

	public DataPacket(PacketType packetType, byte[] data, Integer sourceId) {
		this.packetType = packetType;
		this.data = data;
		this.sourceId = sourceId;

	}

	public DataPacket(PacketType packetType, Integer sourceId,
			Integer destinationId, Integer senderClientId,
			Integer receiverClientId, byte[] data) {

		this.packetType = packetType;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.senderClientId = senderClientId;
		this.receiverClientId = receiverClientId;
		this.data = data;

	}

	public DataPacket(PacketType packetType, Integer sourceId,
			Integer destinationId, Integer senderClientId,
			Integer receiverClientId, byte[] data, long transactionId) {

		// encrypt data with signature
		DigitalSignature message = new DigitalSignature();
		byte[] digitalSign = null;
		try {
			digitalSign = message.sign(data, sourceId);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String cipherText = null;
		try {
			SymmetricAES aes = new SymmetricAES();
			SecretKey secretKey = aes.readSecretKeyFromFile(sourceId);

			cipherText = aes.encrypt(data, secretKey);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.packetType = packetType;
		this.sourceId = sourceId;
		this.destinationId = destinationId;
		this.senderClientId = senderClientId;
		this.receiverClientId = receiverClientId;
		this.data = data;
		this.transactionId = transactionId;
		this.digitalSignature = digitalSign;
		this.cipherText = cipherText;
	}
	
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DataPacket dataPacket = (DataPacket) o;

		if (packetType != dataPacket.packetType)
			return false;
		if (sourceId != null ? sourceId != dataPacket.sourceId
				: dataPacket.sourceId != null)
			return false;
		if (destinationId != null ? destinationId != dataPacket.destinationId
				: dataPacket.destinationId != null)
			return false;
		if (data != null ? data != dataPacket.data : dataPacket.data != null)
			return false;

		return true;
	}

	public String toString() {
		return  "; consumerId= " + getSenderClientId()
				+ "; packetId= " + getPacketId()
				+ "; tranxId= " + getTransactionId()
				+ "; Hash= " + getHashcode()
				+ "; digitalSig= " + getDigitalSignature()
				+ "; cipher= " + getCipherText()
				+ "; Data= " + getData();
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();	
	}

	public PacketType getPacketType() {
		return packetType;
	}

	public void setPacketType(PacketType packetType) {
		this.packetType = packetType;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public Integer getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(Integer destinationId) {
		this.destinationId = destinationId;
	}

	public Integer getSenderClientId() {
		return senderClientId;
	}

	public void setSenderClientId(Integer senderClientId) {
		this.senderClientId = senderClientId;
	}

	public Integer getReceiverClientId() {
		return receiverClientId;
	}

	public void setReceiverClientId(Integer receiverClientId) {
		this.receiverClientId = receiverClientId;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public long getPacketId() {
		return packetId;
	}

	public void setPacketId(long packetId) {
		this.packetId = packetId;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public long getTravelStartTime() {
		return travelStartTime;
	}

	public void setTravelStartTime(long travelStartTime) {
		this.travelStartTime = travelStartTime;
	}

	public long getTravelEndTime() {
		return travelEndTime;
	}

	public void setTravelEndTime(long travelEndTime) {
		this.travelEndTime = travelEndTime;
	}

	public int getTotalTrasnmittedBits() {
		return totalTrasnmittedBits;
	}

	public void setTotalTrasnmittedBits(int totalTrasnmittedBits) {
		this.totalTrasnmittedBits = totalTrasnmittedBits;
	}

	public String getHashcode() {
		return hashcode;
	}

	public void setHashcode(String hashcode) {
		this.hashcode = hashcode;
	}

	public byte[] getDigitalSignature() {
		return digitalSignature;
	}

	public void setDigitalSignature(byte[]  cipherText) {
		this.digitalSignature = cipherText;
	}

	public String getCipherText() {
		return cipherText;
	}

	public void setCipherText(String cipherText) {
		this.cipherText = cipherText;
	}
}
