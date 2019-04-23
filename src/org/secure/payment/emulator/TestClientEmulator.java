package org.secure.payment.emulator;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import org.secure.payment.crypto.util.GenerateAsymKeys;
import org.secure.payment.crypto.util.GenerateSymmetricKey;
import org.secure.payment.emulator.client.ClientHandler;
import org.secure.payment.model.TCPConnectionDetails;
import org.secure.payment.util.ConfigUtil;
import org.secure.payment.util.NetworkUtils;
import org.secure.payment.util.Utils;

public class TestClientEmulator {
	
	static int CLIENT1_ID;
	static int CLIENT2_ID;
	static int CLIENT3_ID;
	static int CLIENT4_ID;
	
	static {
		try {
			// Read unique identifier of each client node  
			ConfigUtil.openFile();
			CLIENT1_ID = Integer.parseInt(ConfigUtil.read("CLIENT_1_ID"));
			CLIENT2_ID = Integer.parseInt(ConfigUtil.read("CLIENT_2_ID"));
			CLIENT3_ID = Integer.parseInt(ConfigUtil.read("CLIENT_3_ID"));
			CLIENT4_ID = Integer.parseInt(ConfigUtil.read("CLIENT_4_ID"));
			
			ConfigUtil.close();
			
			// Generate and distribute asymmetric keys for Digital signature
			GenerateAsymKeys assymetricKeyGenerator = new GenerateAsymKeys(1024);
			assymetricKeyGenerator.generateKeyPairs(CLIENT1_ID);
			assymetricKeyGenerator.generateKeyPairs(CLIENT2_ID);
			assymetricKeyGenerator.generateKeyPairs(CLIENT3_ID);
			assymetricKeyGenerator.generateKeyPairs(CLIENT4_ID);
			
			// Generate and distribute symmetric keys confidentiality, MAC
			GenerateSymmetricKey symmetricKeyGenerator = new GenerateSymmetricKey(16, "AES");
			symmetricKeyGenerator.generateSymmetricKey(CLIENT1_ID);		
			symmetricKeyGenerator.generateSymmetricKey(CLIENT2_ID);		
			symmetricKeyGenerator.generateSymmetricKey(CLIENT3_ID);		
			symmetricKeyGenerator.generateSymmetricKey(CLIENT4_ID);		
			
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			System.err.println(e.getMessage());
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final String TRANSACTION1 = "Bob sends $10 to Alice";
	private static final String TRANSACTION2 = "John sends $20 to Thomas";
	private static final String TRANSACTION3 = "Teller sends $30 to Walmart";
	private static final String TRANSACTION4 = "Robin sends $40 to CarInsurance.com";

	public static void main(String args[]) throws IOException {

		ClientHandler clientHandler = new ClientHandler(CLIENT1_ID);
		ClientHandler clientHandler2 = new ClientHandler(CLIENT2_ID);
		ClientHandler clientHandler3 = new ClientHandler(CLIENT3_ID);
		ClientHandler clientHandler4 = new ClientHandler(CLIENT4_ID);

		try {
			
			setUpPhaseOf4Client1Router(clientHandler, clientHandler2,
					clientHandler3, clientHandler4);

			sendControlInfo(clientHandler, clientHandler2, clientHandler3,
					clientHandler4);

			addOneTrnxCycle(TRANSACTION1, clientHandler, clientHandler2,
					clientHandler3, clientHandler4);


		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 4 ==> Client, 1 => Router
	private static void setUpPhaseOf4Client1Router(ClientHandler clientHandler,
			ClientHandler clientHandler2, ClientHandler clientHandler3,
			ClientHandler clientHandler4) throws IOException,
			UnknownHostException, InterruptedException {

		List<TCPConnectionDetails> client1ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(1));
		clientHandler.setupPhase(client1ConnInfo);

		List<TCPConnectionDetails> client2ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(1));
		clientHandler2.setupPhase(client2ConnInfo);

		List<TCPConnectionDetails> client3ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(1));
		clientHandler3.setupPhase(client3ConnInfo);

		List<TCPConnectionDetails> client4ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(1));
		clientHandler4.setupPhase(client4ConnInfo);
	}

	// 4 ==> Client, 6 => Router
	private static void setUpPhaseOf4Client6Router(ClientHandler clientHandler,
			ClientHandler clientHandler2, ClientHandler clientHandler3,
			ClientHandler clientHandler4) throws IOException,
			UnknownHostException, InterruptedException {

		List<TCPConnectionDetails> client1ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(1, 2));
		clientHandler.setupPhase(client1ConnInfo);

		List<TCPConnectionDetails> client2ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(1, 3, 4, 5));
		clientHandler2.setupPhase(client2ConnInfo);

		List<TCPConnectionDetails> client3ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(2, 3, 4, 6));
		clientHandler3.setupPhase(client3ConnInfo);

		List<TCPConnectionDetails> client4ConnInfo = NetworkUtils
				.getClientConnInfo(Arrays.asList(5, 6));
		clientHandler4.setupPhase(client4ConnInfo);
	}

	private static void sendControlInfo(ClientHandler clientHandler,
			ClientHandler clientHandler2, ClientHandler clientHandler3,
			ClientHandler clientHandler4) throws InterruptedException,
			IOException {

		Thread.sleep(10000);

		clientHandler.sendControlInfoToAdjacentRouters();
		clientHandler2.sendControlInfoToAdjacentRouters();
		clientHandler3.sendControlInfoToAdjacentRouters();
		clientHandler4.sendControlInfoToAdjacentRouters();
	}

	private static void addOneTrnxCycle(String transactionStr,
			ClientHandler clientHandler, ClientHandler clientHandler2,
			ClientHandler clientHandler3, ClientHandler clientHandler4) {
		
		Utils.selectRamdomClient();
		
		System.out.println("\n//********** NEW TRANSACTION CYCLE ************//");
		System.out.println("\nSELECTED CONSUMER: " + Utils.senderClientId);
		System.out.println("SELECTED MERCHANT: " + Utils.receiverClientId);
		System.out.println("===================");
		clientHandler.executeClient(transactionStr, Utils.senderClientId,
				Utils.receiverClientId, Utils.merchantBankClientId, Utils.consumerBankClientId);
		clientHandler2.executeClient(transactionStr, Utils.senderClientId,
				Utils.receiverClientId, Utils.merchantBankClientId, Utils.consumerBankClientId);
		clientHandler3.executeClient(transactionStr, Utils.senderClientId,
				Utils.receiverClientId, Utils.merchantBankClientId, Utils.consumerBankClientId);
		clientHandler4.executeClient(transactionStr, Utils.senderClientId,
				Utils.receiverClientId, Utils.merchantBankClientId, Utils.consumerBankClientId);

	}
}
