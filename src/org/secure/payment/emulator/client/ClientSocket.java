package org.secure.payment.emulator.client;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;

public class ClientSocket extends Socket{
	int clientID;
	
	public ClientSocket() {
	      super();
	}
	
	public ClientSocket(String host, int port, int clientID) throws UnknownHostException, IOException {
	      super(host, port);
	      this.clientID = clientID;
	}
	 
	public ClientSocket(SocketImpl socketImpl) throws SocketException {
		super(socketImpl);
	}
	
	public ClientSocket(SocketImpl socketImpl,int clientID) throws SocketException {
		super(socketImpl);
		this.clientID = clientID;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
	
}
