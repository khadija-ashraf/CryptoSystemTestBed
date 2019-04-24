package org.secure.payment.emulator.client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.secure.payment.crypto.util.DigitalSignature;
import org.secure.payment.crypto.util.SymmetricAES;
import org.secure.payment.crypto.util.VerifySignature;
import org.secure.payment.model.ClientConnection;
import org.secure.payment.model.ClientType;
import org.secure.payment.model.DataPacket;
import org.secure.payment.model.PacketType;
import org.secure.payment.model.ProcessedPacket;
import org.secure.payment.model.TCPConnectionDetails;
import org.secure.payment.util.CSVUtils;
import org.secure.payment.util.SendEmail;
import org.secure.payment.util.Utils;

import java.util.Date;

public class ClientHandler {
	static Semaphore semaphore = new Semaphore(1);
	
	private static final String COMMAND_SOURCE_ID = "SOURCE_ID"; 
	
	private int clientId; 
	Integer senderClientId;
	Integer receiverClientId;
	Integer merchantBankClientId;
	Integer consumerBankClientId;

	private ClientType clientType;
	private List<ClientConnection> connections;
	
	String csvFile;
	FileWriter fileWriter = null;
	int earnedBitcoin;
	String transactionStr;
	long totalTravelTimeInMills;
	int totalTransmittedBits;

	private final ConcurrentHashMap<Long, ProcessedPacket> processedPackets = 
			new ConcurrentHashMap<Long, ProcessedPacket>();
	
	private final ConcurrentHashMap<Long, ProcessedPacket> processedTranxs = 
			new ConcurrentHashMap<Long, ProcessedPacket>();

	public ClientHandler() {
	}
	
	public ClientHandler (int clientId) {
		this.clientId = clientId;
	}
	
	public void doTransaction() {
		try {
			String tranxString = new String(this.transactionStr);
			String hashCode = Utils.getSHA256HashCode(tranxString);
			
			DataPacket dataPacket = Utils.createTransactionPacket(
					this.transactionStr, this.getClientId(), receiverClientId,
					senderClientId, receiverClientId);

			dataPacket.setHashcode(hashCode);
			ProcessedPacket processedPacket = new ProcessedPacket(
					dataPacket.getSourceId(), dataPacket.getPacketId());
			processedPackets.putIfAbsent(dataPacket.getPacketId(), processedPacket);
			
			dataPacket.setTotalTrasnmittedBits(dataPacket
					.getTotalTrasnmittedBits()
					+ transactionStr.getBytes().length);

			for (ClientConnection connection : this.getConnections()) {
				semaphore.acquire();
				connection.getOutputStream().writeObject(dataPacket);
				connection.getOutputStream().flush();
				semaphore.release();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * This method is responsible for creating and saving all socket connection 
	 * to the adjacent network nodes of this particular client and also it
	 * creates a ledger for keeping processed transactions.
	 * 
	 * @param clientId
	 * @param connectionDetails
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	public void setupConnections(int clientId,
			List<TCPConnectionDetails> connectionDetails)
			throws UnknownHostException, IOException, InterruptedException {

		List<ClientConnection> connectionList = new ArrayList<ClientConnection>();

		for (TCPConnectionDetails details : connectionDetails) {
			ClientConnection clientData = createConnection(details.getHost(),
					details.getPort(), clientId);
			connectionList.add(clientData);
		}

		this.setConnections(connectionList);
		this.createLedger();
	}

	private void createLedger() throws IOException {
		this.csvFile = this.getClientId()+"_legder"+".csv";
		fileWriter = new FileWriter(csvFile);
		//for header
        CSVUtils.writeLine(fileWriter, Arrays.asList("TransactionId", "Amount", "Record", "DigitalSignature"));
	}
	
	public void createThreadsForAllConn() throws IOException, InterruptedException {
		
		List<ClientConnection> connectionList =  this.getConnections();
		for (ClientConnection connection : connectionList) {
			
			// Create a thread for listening incoming data simultaneously from this connection.
			MultithreadedClient clientThread = new MultithreadedClient(
					connection.getClientId(), connection.getOutputStream(), 
					connection.getInputStream(), connectionList, ClientHandler.semaphore,
					processedPackets, processedTranxs, fileWriter, earnedBitcoin);
			
			clientThread.start();
		}
	}

	/**
	 * This method sends this particular client's identification number to all 
	 * adjacent router so that routers can uniquely identify each connection to 
	 * a client by its identifier  
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void sendControlInfoToAdjacentRouters()
			throws InterruptedException, IOException {
		
		List<ClientConnection> connectionList =  this.getConnections();
		for (ClientConnection connection : connectionList) {
			
			// Sending SOURCE_ID
			String command = COMMAND_SOURCE_ID+":"+this.getClientId();
			DataPacket commandDataPacket = new DataPacket(PacketType.COMMAND,
					command.getBytes(), this.getClientId());

			semaphore.acquire();
			connection.getOutputStream().writeObject(commandDataPacket);
			connection.getOutputStream().flush();
			semaphore.release();
			
		}
	}
	
	public ClientConnection createConnection (String host, int port, int clientId) 
			throws UnknownHostException, IOException, InterruptedException {
		
		ClientSocket socket =  createClientSocket(host, port);
		ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
		
		return new ClientConnection(this.getClientId(), socket, outputStream, inputStream);
	}
	
	public ClientSocket createClientSocket(String hostName, int port) throws UnknownHostException, IOException {
		return new ClientSocket(hostName, port, this.getClientId());
	}
	
	public List<ClientConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<ClientConnection> connections) {
		this.connections = connections;
	}
	
	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	
	public long getTotalTravelTimeInMills() {
		return totalTravelTimeInMills;
	}

	public void setTotalTravelTimeInMills(long totalTravelTimeInMills) {
		this.totalTravelTimeInMills = totalTravelTimeInMills;
	}

	public int getTotalTransmittedBits() {
		return totalTransmittedBits;
	}

	public void setTotalTransmittedBits(int totalTransmittedBits) {
		this.totalTransmittedBits = totalTransmittedBits;
	}

	public void checkIfClientIsSenderOrReceiver() {
		
		if (this.getClientId() == this.senderClientId) {
			this.setClientType(ClientType.SENDER);
		} else if (this.getClientId() == this.receiverClientId) {
			this.setClientType(ClientType.RECEIVER);
		} else if (this.getClientId() == this.merchantBankClientId) {
			this.setClientType(ClientType.MERCHANT_BANK);
		}else if (this.getClientId() == this.consumerBankClientId) {
			this.setClientType(ClientType.CONSUMER_BANK);
		}
	}
	
	public void setupPhase (List<TCPConnectionDetails> connectionDetails) throws UnknownHostException, IOException, InterruptedException {
		this.setupConnections(this.getClientId(), connectionDetails);
		this.createThreadsForAllConn();
	}
	
	public void executeClient(String transactionStr, Integer senderClientId, Integer receiverClientId,
			Integer merchantBankClientId, Integer consumerBankClientId) {
		totalTravelTimeInMills = 0;
		totalTransmittedBits = 0;
		
		this.senderClientId = senderClientId;
		this.receiverClientId = receiverClientId;
		this.merchantBankClientId = merchantBankClientId;
		this.consumerBankClientId = consumerBankClientId;
		this.transactionStr = transactionStr;
		
		this.checkIfClientIsSenderOrReceiver();
		
		
		if (this.getClientType().equals(ClientType.SENDER)) {
			System.out.println("NODE : "+ this.getClientId() +" ==> Consumer...");
			this.doTransaction();
			System.out.println("===================\n");
		} else if (this.getClientType().equals(ClientType.RECEIVER)) {
			System.out.println("NODE : "+ this.getClientId() +" ==> Merchant...");
			System.out.println("===================\n");
		} else if (this.getClientType().equals(ClientType.MERCHANT_BANK)) {
			System.out.println("NODE : "+ this.getClientId() +" ==> Merchant Bank...");
			System.out.println("===================\n");
		} else if (this.getClientType().equals(ClientType.CONSUMER_BANK)) {
			System.out.println("NODE : "+ this.getClientId() +" ==> Consumer Bank...");
			System.out.println("===================\n");
		}
	}
	
	public static void main(String[] args) {
	}
	
	

	public class MultithreadedClient extends Thread {
		Socket socket;
		ObjectOutputStream outputStream;
		ObjectInputStream inputStream;
		int clientId;
		List<ClientConnection> connectionList;
		Semaphore semaphore;
		
		ConcurrentHashMap<Long, ProcessedPacket> processedPackets;
		ConcurrentHashMap<Long, ProcessedPacket> processedTranxs;
		
		FileWriter fileWriter;
		int earnedBitcoin;
		
		public MultithreadedClient() {
			super();
		}
	
		public MultithreadedClient(Socket socket) {
			this.socket = socket;
		}
		
		public MultithreadedClient(ObjectOutputStream outputStream, ObjectInputStream inputStream) {
			this.outputStream = outputStream;
			this.inputStream = inputStream;
		}

		public MultithreadedClient(int clientId, 
				ObjectOutputStream outputStream, 
				ObjectInputStream inputStream, 
				List<ClientConnection> connectionList,
				Semaphore semaphore,
				ConcurrentHashMap<Long, ProcessedPacket> processedPackets,
				ConcurrentHashMap<Long, ProcessedPacket> processedTranxs,
				FileWriter fileWriter,
				int earnedBitcoin) {
			
			this.clientId = clientId;
			this.outputStream = outputStream;
			this.inputStream = inputStream;
			this.connectionList = connectionList;
			this.semaphore = semaphore;
			this.processedPackets = processedPackets;
			this.processedTranxs = processedTranxs;
			this.fileWriter = fileWriter;
			this.earnedBitcoin = earnedBitcoin;
		}
		
		@Override
		public void run() {
			
			try {
				try {
					DataPacket receivedPacket;
					while ((receivedPacket = (DataPacket) inputStream.readObject()) != null) {
						//System.out.println("\nReceived from Router : " + receivedPacket.toString());	
						if(checkNaddProcessedPacket(receivedPacket)){
							continue;
						}
						
						if (receivedPacket.getPacketType().equals(PacketType.TRANSACTION)
								&& clientType.equals(ClientType.RECEIVER)) {
							
							System.out.println("\n[[PacketType: "
									+ receivedPacket.getPacketType() + "]] "
									+ "\nSourceId= "+receivedPacket.getSourceId()
									+ ";\nDestId= "+this.clientId
									+ receivedPacket.toString());

							VerifySignature verifySig = new VerifySignature();
							boolean sigVarified = verifySig.verifySignature(
									receivedPacket.getData(),
									receivedPacket.getDigitalSignature(),
									receivedPacket.getSourceId());

							if (sigVarified) {
								System.out.println("Signature verified : "+receivedPacket.getPacketType());
								askApprovalToMerchantBank(receivedPacket);

							} else {
								System.out.println("Signature failed: Transaction ABORTED.");
							}
							
							
						} else if (receivedPacket.getPacketType().equals(PacketType.APPROVAL_REQUEST)
								&& clientType.equals(ClientType.MERCHANT_BANK)) {
							
							System.out.println("\n[[PacketType: "
									+ receivedPacket.getPacketType() + "]] "
									+ "\nSourceId= "+receivedPacket.getSourceId()
									+ ";\nDestId= "+this.clientId
									+ receivedPacket.toString());
							
							VerifySignature verifySig = new VerifySignature();
							boolean sigVarified = verifySig.verifySignature(
									receivedPacket.getData(),
									receivedPacket.getDigitalSignature(),
									receivedPacket.getSourceId());

							if (sigVarified) {
								System.out.println("Signature verified : "+receivedPacket.getPacketType());
								askApprovalToConsumerBank(receivedPacket);

							} else {
								System.out.println("Signature failed: Transaction ABORTED.");
							}
							
							
						} // TODO: ask APPROVAL_REQUEST to the consumer bank
						else if (receivedPacket.getPacketType().equals(PacketType.APPROVAL_REQUEST_RELAY)
								&& clientType.equals(ClientType.CONSUMER_BANK)) {
							
							System.out.println("\n[[PacketType: "
									+ receivedPacket.getPacketType() + "]] "
									+ "\nSourceId= "+receivedPacket.getSourceId()
									+ ";\nDestId= "+this.clientId
									+ receivedPacket.toString());
							
							// If consumer bank finds the transaction valid then,
							// 1. sends the verification to merchant bank
							// 2. add the transaction to consumer bank ledger for sattlement as a proof of money transfer
							// 3. send the consumer a notification email about the money transfer(payment)
							
							// Decrypt
							VerifySignature verifySig = new VerifySignature();
							boolean sigVarified = verifySig.verifySignature(
									receivedPacket.getData(),
									receivedPacket.getDigitalSignature(),
									receivedPacket.getSourceId());

							if (sigVarified) {
								System.out.println("Signature verified : "+receivedPacket.getPacketType());
								
								sendVerificationToReceiver(receivedPacket);
								
								// Get transaction from ciphertext
								String trnxString = null;
								try {
									SymmetricAES aes = new SymmetricAES();
									SecretKey secretKey = aes.readSecretKeyFromFile(receivedPacket.getSenderClientId());

									trnxString = aes.decrypt(receivedPacket.getCipherText(), secretKey);

								} catch (NoSuchAlgorithmException e) {
									e.printStackTrace();
								} catch (NoSuchPaddingException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								//sendEmailToConsumer(receivedPacket, trnxString);
								
								System.out.println("Notified Consumer.");
								// Add transaction into ledger.
								this.addTrnxToLedger(trnxString, receivedPacket.getTransactionId(), receivedPacket.getDigitalSignature());
								
							} else {
								System.out.println("Signature failed: ");
							}
							
						} else if (receivedPacket.getPacketType().equals(PacketType.VERIFICATION)
							&& clientType.equals(ClientType.MERCHANT_BANK)) {
							
							System.out.println("\n[[PacketType: "
									+ receivedPacket.getPacketType() + "]] "
									+ "\nSourceId= "+receivedPacket.getSourceId()
									+ ";\nDestId= "+this.clientId
									+ receivedPacket.toString());
							
							// Decrypt
							VerifySignature verifySig = new VerifySignature();
							boolean sigVarified = verifySig.verifySignature(
									receivedPacket.getData(),
									receivedPacket.getDigitalSignature(),
									receivedPacket.getSourceId());

							if (sigVarified) {
								System.out.println("Signature verified : "+receivedPacket.getPacketType());
								
								if(checkNaddProcessedTrnx(receivedPacket)){
									continue;
								}
								
								// Get transaction from ciphertext
								String trnxString = null;
								try {
									SymmetricAES aes = new SymmetricAES();
									SecretKey secretKey = aes.readSecretKeyFromFile(receivedPacket.getSenderClientId());

									trnxString = aes.decrypt(receivedPacket.getCipherText(), secretKey);

								} catch (NoSuchAlgorithmException e) {
									e.printStackTrace();
								} catch (NoSuchPaddingException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
								//sendEmailToMerchant(receivedPacket, trnxString);
								System.out.println("Notified Merchant.");
								
								// Add transaction into ledger.
								this.addTrnxToLedger(trnxString, receivedPacket.getTransactionId(), receivedPacket.getDigitalSignature());
								
							} else {
								System.out.println("Signature failed: ");
							}
							
							// Calculate and show throughout
							//receivedPacket.setTravelEndTime(System.currentTimeMillis());
							//long totalTravelTimeInMills = receivedPacket.getTravelEndTime() - receivedPacket.getTravelStartTime();
							//int totalTransmittedBits = receivedPacket.getTotalTrasnmittedBits();
							//String throughputStr = String.format("%.2f", totalTransmittedBits/(double)totalTravelTimeInMills);
							//System.out.println("\n[[THROUGHPUT : TRANSACTIONID]] :: "+throughputStr+" bytes/mills" +" || "+receivedPacket.getTransactionId());
							
						} else {
							for (ClientConnection connection : connectionList) {
								semaphore.acquire();
								connection.getOutputStream().writeObject(receivedPacket);
								connection.getOutputStream().flush();
								semaphore.release();
							}
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						fileWriter.close();
						outputStream.close();
						inputStream.close();
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					} 
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			} 
		}

		private void sendEmailToConsumer(DataPacket receivedPacket, String trnxString) {
			String receipientEmail = "khadija.csedu@gmail.com";
			int amountIdx = trnxString.indexOf('$');
			int amountIdxEnd = trnxString.indexOf('$');
			while(trnxString.charAt(amountIdxEnd) != ' ') {
				amountIdxEnd++;
			}
			String[] strArr = trnxString.split(" ");
			String receiver = strArr[strArr.length - 1];
			
			String subject = "Your Payment Scheduled";
			String amountStr = trnxString.substring(amountIdx, amountIdxEnd);
			String emailBody = "Payment Info:\n";
			emailBody += "\nTotal amount: "+amountStr;
			emailBody += "\nReceiver:  "+receiver;
			emailBody += "\nTransaction ID: "+receivedPacket.getTransactionId();
			emailBody += "\nPayment Posting Date: "+new Date();
			
			SendEmail.mail(subject, emailBody, receipientEmail);
		}
		
		private void sendEmailToMerchant(DataPacket receivedPacket, String trnxString) {
			String receipientEmail = "mtest7587@gmail.com"; // pass:mtest7587123456
			int amountIdx = trnxString.indexOf('$');
			int amountIdxEnd = trnxString.indexOf('$');
			while(trnxString.charAt(amountIdxEnd) != ' ') {
				amountIdxEnd++;
			}
			String[] strArr = trnxString.split(" ");
			String sender = strArr[0];
			
			String subject = "Your Selling Receipt";
			String amountStr = trnxString.substring(amountIdx, amountIdxEnd);
			String emailBody = "Payment Info:\n";
			emailBody += "\nTotal amount: "+amountStr;
			emailBody += "\nSender:  "+sender;
			emailBody += "\nTransaction ID: "+receivedPacket.getTransactionId();
			emailBody += "\nPayment Posting Date: "+new Date();
			
			SendEmail.mail(subject, emailBody, receipientEmail);
		}
		
		private void askApprovalToMerchantBank(DataPacket transactionPacket)
				throws Exception {
		
			DataPacket approvalPacket = (DataPacket) transactionPacket.clone(); 
			
			approvalPacket.setPacketType(PacketType.APPROVAL_REQUEST);
			approvalPacket.setSourceId(this.clientId);
			approvalPacket.setDestinationId(null);
			approvalPacket.setPacketId(Utils.generateRandomId());
			
			approvalPacket.setTotalTrasnmittedBits(approvalPacket
					.getTotalTrasnmittedBits()
					+ (approvalPacket.getData().length) * connectionList.size());
			
			// encrypt data with signature
			DigitalSignature message = new DigitalSignature();
			byte[] cipherText = null;
			try {
				cipherText = message.sign(transactionPacket.getData(), this.clientId);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			approvalPacket.setDigitalSignature(cipherText);
			SecretKey secretKey = new SymmetricAES().readSecretKeyFromFile(this.clientId);
			approvalPacket.setEncryptedSecretKey(Utils.convertToBase64(secretKey.getEncoded()));

			for (ClientConnection connection : connectionList) {
				semaphore.acquire();
				connection.getOutputStream().writeObject(approvalPacket);
				connection.getOutputStream().flush();
				semaphore.release();
			}
		}
		
		private void askApprovalToConsumerBank(DataPacket transactionPacket)
				throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {
		
			DataPacket approvalPacket = (DataPacket) transactionPacket.clone(); 
			
			approvalPacket.setPacketType(PacketType.APPROVAL_REQUEST_RELAY);
			approvalPacket.setSourceId(this.clientId);
			approvalPacket.setDestinationId(null);
			approvalPacket.setPacketId(Utils.generateRandomId());
			
			approvalPacket.setTotalTrasnmittedBits(approvalPacket
					.getTotalTrasnmittedBits()
					+ (approvalPacket.getData().length) * connectionList.size());
			// encrypt data with signature
			DigitalSignature message = new DigitalSignature();
			byte[] cipherText = null;
			try {
				cipherText = message.sign(transactionPacket.getData(), this.clientId);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			approvalPacket.setDigitalSignature(cipherText);
			SecretKey secretKey = new SymmetricAES().readSecretKeyFromFile(this.clientId);
			approvalPacket.setEncryptedSecretKey(Utils.convertToBase64(secretKey.getEncoded()));
			
			for (ClientConnection connection : connectionList) {
				semaphore.acquire();
				connection.getOutputStream().writeObject(approvalPacket);
				connection.getOutputStream().flush();
				semaphore.release();
			}
		}
		
		private void sendVerificationToReceiver(DataPacket approvalPacket)
				throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {
			
			DataPacket verificationPacket = (DataPacket) approvalPacket.clone(); 
			
			verificationPacket.setPacketType(PacketType.VERIFICATION);
			verificationPacket.setSourceId(this.clientId);
			verificationPacket.setDestinationId(null);
			verificationPacket.setPacketId(Utils.generateRandomId());
			
			verificationPacket.setTotalTrasnmittedBits(verificationPacket
					.getTotalTrasnmittedBits()
					+ (verificationPacket.getData().length) * connectionList.size());
			// encrypt data with signature
			DigitalSignature message = new DigitalSignature();
			byte[] cipherText = null;
			try {
				cipherText = message.sign(approvalPacket.getData(), this.clientId);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			verificationPacket.setDigitalSignature(cipherText);
			
			SecretKey secretKey = new SymmetricAES().readSecretKeyFromFile(this.clientId);
			verificationPacket.setEncryptedSecretKey(Utils.convertToBase64(secretKey.getEncoded()));
			
			for (ClientConnection connection : connectionList) {
				semaphore.acquire();
				connection.getOutputStream().writeObject(verificationPacket);
				connection.getOutputStream().flush();
				semaphore.release();
			}
		}
		
		private boolean checkNaddProcessedPacket (DataPacket receivedPacket) {
			if (processedPackets.get(receivedPacket.getPacketId()) != null) {
				return true;
			} 
			ProcessedPacket processedPacket = new ProcessedPacket(
					receivedPacket.getSourceId(), receivedPacket.getPacketId());
			processedPackets.putIfAbsent(receivedPacket.getPacketId(), processedPacket);
			return false;
		}
		
		private boolean checkNaddProcessedTrnx (DataPacket receivedPacket) {
			
			ProcessedPacket processedPacket = processedTranxs.get(receivedPacket.getTransactionId());
			if ( processedPacket != null && processedPacket.getPacketType().equals(receivedPacket.getPacketType())) {
				return true;
			} 
			
			ProcessedPacket processedTranx = new ProcessedPacket(
					receivedPacket.getSourceId(), receivedPacket.getTransactionId(), receivedPacket.getPacketType());
			processedTranxs.put(receivedPacket.getTransactionId(), processedTranx);
			return false;
		}
		
		private void addTrnxToLedger(String tranxString, long tranxId, byte[] digitalSignature) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {
			//"TransactionId", "Amount", "Record", "DigitalSignature")
			int amountIdx = tranxString.indexOf('$');
			int amountIdxEnd = tranxString.indexOf('$');
			while(tranxString.charAt(amountIdxEnd) != ' ') {
				amountIdxEnd++;
			}
			String amountStr = tranxString.substring(amountIdx, amountIdxEnd);

			CSVUtils.writeLine(
					fileWriter,
					Arrays.asList(String.valueOf(tranxId), amountStr, tranxString,
							Utils.convertToBase64(digitalSignature)));
			fileWriter.flush();
		}
		/*private void addTrnxToLedger (DataPacket receivedPacket) throws IOException {
			String tranxString = new String(receivedPacket.getData());
			String hashCode = Utils.getSHA256HashCode(tranxString);
			
			CSVUtils.writeLine(
					fileWriter,
					Arrays.asList(tranxString, hashCode,
							String.valueOf(receivedPacket.getTransactionId())));
			fileWriter.flush();
		}*/
	}
}
