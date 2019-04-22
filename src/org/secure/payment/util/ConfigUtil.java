package org.secure.payment.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
	static Properties prop = new Properties();
	static InputStream input = null;

	public static void main(String[] args) {

		try {
			openFile();
			System.out.println(read("ROUTER_1_PORT"));
			close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String read(String propertyName) {
		return prop.getProperty(propertyName);
	}

	public static void openFile() throws IOException {

		input = new FileInputStream("config.properties");
		prop.load(input); 
	}

	public static void close() {

		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
