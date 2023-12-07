package fileSearch;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

	public static void main(String[] args) throws IOException {
		try (ServerSocket server = new ServerSocket(3030)) {
			String os = System.getProperty("os.name");
			int cores = Runtime.getRuntime().availableProcessors();
			System.out.println("Server Started, waiting for clients");
			while (true) {
				new ServerService(server.accept(), os, cores).run();
			}
		}
	}

}
