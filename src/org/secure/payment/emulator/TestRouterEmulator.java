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
	
	private static void loadRouter2() throws IOException {
		ConfigUtil.openFile();

		/***** Start Router Threads *********/
		Router router1 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_2_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router1.executeRouter();

		System.out.println("All Routers are Up!...");
		ConfigUtil.close();
	}
	
	private static void loadRouter3() throws IOException {
		ConfigUtil.openFile();

		/***** Start Router Threads *********/
		Router router1 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_3_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router1.executeRouter();

		System.out.println("All Routers are Up!...");
		ConfigUtil.close();
	}
	
	private static void loadRouter4() throws IOException {
		ConfigUtil.openFile();

		/***** Start Router Threads *********/
		Router router1 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_4_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router1.executeRouter();

		System.out.println("All Routers are Up!...");
		ConfigUtil.close();
	}
	private static void loadRouter5() throws IOException {
		ConfigUtil.openFile();

		/***** Start Router Threads *********/
		Router router1 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_5_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router1.executeRouter();

		System.out.println("All Routers are Up!...");
		ConfigUtil.close();
	}
	private static void loadRouter6() throws IOException {
		ConfigUtil.openFile();

		/***** Start Router Threads *********/
		Router router1 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_6_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router1.executeRouter();

		System.out.println("All Routers are Up!...");
		ConfigUtil.close();
	}
	
	public static void loadAllRouters () throws IOException {
		ConfigUtil.openFile();

		/***** Start Router Threads *********/
		Router router1 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_1_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router1.executeRouter();

		Router router2 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_2_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router2.executeRouter();

		Router router3 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_3_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router3.executeRouter();

		Router router4 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_4_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router4.executeRouter();

		Router router5 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_6_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router5.executeRouter();

		Router router6 = new Router(Integer.parseInt(ConfigUtil
				.read("ROUTER_1_PORT")), ROUTER_ON, ALLOW_DROP,
				DROPPING_PROBABILITY);
		router6.executeRouter();

		System.out.println("All Routers are Up!...");
		ConfigUtil.close();
	}
}
