package org.secure.payment.emulator.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;

public class ClientServerSocket extends ServerSocket {

	
	public ClientServerSocket() throws IOException {
		super();
	}
	public ClientServerSocket(int routerPort) throws IOException {
		super(routerPort);
	}

	@Override
	public Socket accept() throws IOException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isBound())
			throw new SocketException("Socket is not bound yet");
      	final Socket s = new ClientSocket((SocketImpl) null);
      	implAccept(s);
      	return s;
	}
}
