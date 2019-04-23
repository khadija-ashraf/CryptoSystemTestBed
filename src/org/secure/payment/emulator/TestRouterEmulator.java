package org.secure.payment.emulator;

import java.io.IOException;

import org.secure.payment.emulator.router.Router;
import org.secure.payment.util.ConfigUtil;

public class TestRouterEmulator {

	private static final boolean ROUTER_ON = true;
	private static final int DROPPING_PROBABILITY = 10;
	private static final boolean ALLOW_DROP = false;

	public static void main(String args[]) throws IOException {
		loadRouter1();
	}

	private static void loadRouter1() throws IOException {
		ConfigUtil.openFile();

		/***** Start Router Threads *********/
		Router router1 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_1_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router1.executeRouter();

		System.out.println("All Routers are Up!...");
		ConfigUtil.close();
	}
	
}
