package fileSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket server = new Socket("localhost", 3030);
		PrintWriter out = new PrintWriter(server.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("connected to server");
		
		String message = in.readLine();
		while (in.ready()) {
			System.out.println(message);
			message = in.readLine();
		}
		System.out.println(message);
		
		String[] packet = new String[3];
		packet[0] = keyboard.readLine();
		if (!(packet[0].contains("1")||(packet[0].contains("2")))) {
			System.exit(0);
		}
		
		System.out.println("Enter Desired Path: ");
		packet[1] = keyboard.readLine();
		
		System.out.println("Enter Desired Keyword To Find: ");
		packet[2] = keyboard.readLine();
		
		out.println("" + packet[0] +", " + packet[1] +", " + packet[2]);
		
		
		
		System.out.println("Server Searching For Fitting Files");
		
		message = in.readLine();
		System.out.println("waiting for output");;
		while (! message.contains("done")) {
			System.out.println(message);
			message = in.readLine();
		}
		System.out.println(message);
		server.close();
	}
}