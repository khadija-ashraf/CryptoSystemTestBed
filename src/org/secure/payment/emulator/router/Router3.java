package org.secure.payment.emulator.router;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.secure.payment.emulator.client.ClientServerSocket;
import org.secure.payment.emulator.client.ClientSocket;
import org.secure.payment.model.DataPacket;
import org.secure.payment.model.PacketType;
import org.secure.payment.model.ProcessedPacket;
import org.secure.payment.model.Routing;
import org.secure.payment.util.ConfigUtil;
import org.secure.payment.util.Utils;

public class Router3 {
	static Semaphore semaphore = new Semaphore(1);
	
	int routerPort; 
	boolean routerOn;
	boolean allowDrop;
	int droppingProbability;
	
	ClientServerSocket serverSocket;
	private final ConcurrentHashMap<Integer,Routing> routingTable = 
			new ConcurrentHashMap<Integer,Routing>();
	
	private final ConcurrentHashMap<Long, ProcessedPacket> processedPackets = 
			new ConcurrentHashMap<Long, ProcessedPacket>();
	
	
	public Router3() {
	}
	
	public Router3(int routerPort) {
		this.routerPort = routerPort;
	}
	
	public Router3(int routerPort, boolean routerOn, boolean allowDrop, int droppingProbability) {
		this.routerPort = routerPort;
		this.routerOn = routerOn;
		this.allowDrop = allowDrop;
		this.droppingProbability = droppingProbability;
	}
	
	public Router3(int routerPort, boolean routerOn) {
		this.routerPort = routerPort;
		this.routerOn = routerOn;
	}
	
	public void setupConnections() throws IOException {
		serverSocket = (ClientServerSocket) new ClientServerSocket(this.routerPort);

		while (routerOn) {
			try {
				ClientSocket clientSocket = (ClientSocket) serverSocket.accept();
				
				ObjectOutputStream outputStream = new ObjectOutputStream(
						clientSocket.getOutputStream());
				ObjectInputStream inputStream = new ObjectInputStream(
						clientSocket.getInputStream());

				Routing link = new Routing(clientSocket.getPort(), clientSocket.getPort(), 
						clientSocket, outputStream, inputStream);
				
				this.addToRoutingTable(link, clientSocket.getPort());
				
				MultithreadedRouter clientThread = new MultithreadedRouter(
						clientSocket, outputStream, inputStream);
				clientThread.start();
				
			} catch (IOException ioe) {
				System.out.println("Exception found on accept. Ignoring. Stack Trace :");
				ioe.printStackTrace();
			}
		}
		serverSocket.close();
		System.out.println("Server Stopped");
	}
	
		
	public class MultithreadedRouter extends Thread {
		Socket clientSocket;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		public MultithreadedRouter() {
			super();
		}
	
		public MultithreadedRouter(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}
		
		public MultithreadedRouter(ObjectOutputStream outputStream, ObjectInputStream inputStream) {
			this.out = outputStream;
			this.in = inputStream;
		}
		
		public MultithreadedRouter(Socket clientSocket,
				ObjectOutputStream outputStream, ObjectInputStream inputStream) {
			
			this.clientSocket = clientSocket;
			this.out = outputStream;
			this.in = inputStream;
		}
	
		public void run() {
			DataPacket dataPacket;
			try {
				while ((dataPacket = (DataPacket) in.readObject()) != null) {	
//					System.out.println("[[LOGGING]] : " + dataPacket.toString()+ "\n");
					
					if(checkNaddProcessedPacket(dataPacket)){
						continue;
					}
					if (dataPacket.getPacketType() == PacketType.COMMAND) {
						System.out.println("[[CONTROL]] : " + dataPacket.toString()+ "\n");
						 byte[] command = dataPacket.getData();
						 String commandString = new String(command);
						 if (commandString.contains("SOURCE_ID")) {
							 String[] commandStr = commandString.split(":");
							 String nextHopIdStr = commandStr[commandStr.length - 1];
							 boolean hasNextHopId = true;
							
							 if (hasNextHopId && nextHopIdStr != null) {
								 Routing link = routingTable.get(clientSocket.getPort());
								
								 if (link != null) {
									 Integer nextHopId = Integer.parseInt(nextHopIdStr);
									 link.setNextHopID(nextHopId);
									
									 routingTable.remove(clientSocket.getPort());
									 routingTable.putIfAbsent(nextHopId, link);
//									 System.out.println("[[CONTROL]] -> SOURCE_ID : "+nextHopId);
								 }
							 }
						 }
					} else {
						System.out.println("[[DATA]] : " + dataPacket.toString()+ "\n");
						// logic for random dropping of packet
						Integer randomNum = Utils.generateRandomInRange(1, 100);
//						System.out.println("Ramdom No : "+randomNum);
						if (allowDrop && randomNum < droppingProbability) {
							// drop the packet
							System.out.println("[[DROP - PacketId]] : "+dataPacket.getPacketId()+"\n");
							continue;
						}
						
						Routing destinationLink = null;
						Integer nexthopId = dataPacket.getDestinationId();
						
						if (routingTable != null && nexthopId != null) {
							try{
								destinationLink = routingTable.get(nexthopId);
							} catch (Exception e) {
								System.out.println("XX nexthopId XX : "+nexthopId);
								e.printStackTrace();
							}
						} 
						
						
						
						if (destinationLink == null) {
							// if no destination found then broadcast
							broadcastFromRouter(dataPacket);
						} else {
							semaphore.acquire();
							destinationLink.getOutputStream().writeObject(dataPacket);
							destinationLink.getOutputStream().flush();
							semaphore.release();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					in.close();
					out.close();
					clientSocket.close();
					System.out.println("...Stopped");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		
		public void broadcastFromRouter(DataPacket dataPacket)
				throws InterruptedException, IOException {

			Iterator<Integer> ite = routingTable.keySet().iterator();
			while (ite.hasNext()) {
				Integer key = ite.next();
				Routing routing = routingTable.get(key);
				// To avoid sending the same packet to the original source client itself
				if ((routing.getNextHopID() == dataPacket.getSourceId())) {
					continue;
				}
				semaphore.acquire();
				routing.getOutputStream().writeObject(dataPacket);
				routing.getOutputStream().flush();
				semaphore.release();
			}
		}
	}
	
	public void setupPhase() throws IOException {
		this.setupConnections();
	}
	
	public void executeRouter() {
		try {
			this.setupPhase();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addToRoutingTable (Routing link, Integer uniqueKey) {
		if (routingTable != null) {
			routingTable.put(new Integer(uniqueKey), link);
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
	
	public static void main(String[] args) {
		boolean ROUTER_ON = true;
		int DROPPING_PROBABILITY = 10;
		boolean ALLOW_DROP = true;
		
		try {
			ConfigUtil.openFile();
		 
			Router3 router1 = new Router3(Integer.parseInt(ConfigUtil
					.read("ROUTER_3_PORT")), ROUTER_ON, ALLOW_DROP,
					DROPPING_PROBABILITY);
			router1.executeRouter();
	
			System.out.println("All Routers are Up!...");
			ConfigUtil.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
