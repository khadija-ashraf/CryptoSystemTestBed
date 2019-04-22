package org.secure.payment.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.secure.payment.model.ClientConnection;
import org.secure.payment.model.DataPacket;
import org.secure.payment.model.Routing;
import org.secure.payment.model.TCPConnectionDetails;

public class NetworkUtils {
	
	public static void broadcastFromClient(DataPacket dataPacket,
			Semaphore semaphore, List<ClientConnection> connectionList)
			throws InterruptedException, IOException {

		for (ClientConnection connection : connectionList) {
			writeToOutputStream(connection.getOutputStream(), dataPacket,
					semaphore, true);
		}
	}
	
	public static void sendPacketToNextHop(Routing link, DataPacket dataPacket,
			Semaphore semaphore) throws InterruptedException, IOException {
		
		writeToOutputStream(link.getOutputStream(), dataPacket, semaphore, true);
	}
	
	public static void writeToOutputStream(ObjectOutputStream outputStream,
			DataPacket dataPacket, Semaphore semaphore)
			throws InterruptedException, IOException {

		semaphore.acquire();
		outputStream.writeObject(dataPacket);
		outputStream.flush();
		semaphore.release();
	}
	
	public static void writeToOutputStream(ObjectOutputStream outputStream,
			DataPacket dataPacket, Semaphore semaphore, boolean addDelay)
			throws InterruptedException, IOException {

		semaphore.acquire();
		outputStream.writeObject(dataPacket);
		outputStream.flush();
		semaphore.release();
	}
	
	public static List<TCPConnectionDetails> getClientConnInfo(List<Integer> listOfRouters) throws IOException {
		List<TCPConnectionDetails> connectionDetails = new ArrayList<TCPConnectionDetails>();
		ConfigUtil.openFile();
		
		for (Integer routerSeq : listOfRouters) {
			String hostNameProp = "ROUTER_" + routerSeq.intValue() + "_HOST_NAME";
			String  portProp = "ROUTER_" + routerSeq.intValue() + "_PORT";
			
			String hostName = ConfigUtil.read(hostNameProp);
			int port = Integer.parseInt(ConfigUtil.read(portProp));
			TCPConnectionDetails details = new TCPConnectionDetails (hostName, port);
			connectionDetails.add(details);
		}
		//TODO: read connection details from properties file
		
		ConfigUtil.close();
		return connectionDetails;
	}
}
