package org.secure.payment.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Routing {
	int linkcost;
	int nextHopID;
	int nextHopPort;
	Socket socket;
	
	ObjectOutputStream outputStream;
	ObjectInputStream inputStream;
	
	public Routing (int nextHopID, int nextHopPort) {
		this.nextHopID = nextHopID;
		this.nextHopPort = nextHopPort;
	}
	
	public Routing (int nextHopID, int nextHopPort, Socket socket, 
			ObjectOutputStream outputStream, ObjectInputStream inputStream) {
		this.nextHopID = nextHopID;
		this.nextHopPort = nextHopPort;
		this.socket = socket;
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	public int getLinkcost() {
		return linkcost;
	}

	public void setLinkcost(int linkcost) {
		this.linkcost = linkcost;
	}

	public int getNextHopID() {
		return nextHopID;
	}

	public void setNextHopID(int nextHopID) {
		this.nextHopID = nextHopID;
	}

	public int getNextHopPort() {
		return nextHopPort;
	}

	public void setNextHopPort(int nextHopPort) {
		this.nextHopPort = nextHopPort;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
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

	public String toString() {
		return "NextHopID = " + getNextHopID() 
			+ " ; NextHopPort = " + getNextHopPort();
	}
}