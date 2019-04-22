package org.secure.payment.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection {
	
	int clientId;
	Socket socket;

	ObjectOutputStream outputStream;
	ObjectInputStream inputStream;
	
	public ClientConnection (int clientId, Socket socket, ObjectOutputStream outputStream, 
			ObjectInputStream inputStream) {
		this.clientId = clientId;
		this.socket = socket;
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}
	
	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket routerSocket) {
		this.socket = routerSocket;
	}

	public ObjectOutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(ObjectOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public ObjectInputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(ObjectInputStream inputStream) {
		this.inputStream = inputStream;
	}

}
