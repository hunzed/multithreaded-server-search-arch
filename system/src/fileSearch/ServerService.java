package fileSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerService{
	static Socket client;
	static int cores = 0;
	String os;
	static int  curr;
	static ArrayList<String> files;
	static long totSearchTime;
	
	static ArrayList<ArrayList<String>> findings;

	public ServerService(Socket client, String os, int cores) {
		ServerService.client = client;
		ServerService.cores = cores;
		this.os = os;
		ServerService.curr = -1;
		ServerService.findings = new ArrayList<ArrayList<String>>();
		
		
	}
	
	public void run() {
		try {
			System.out.println("client servicing start");
			
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			
			out.println("Welcome to Parallel File Search Server Load Distributor!\r\n"
					+ "Choose one of the following options:\r\n"
					+ "1. Equal Distribution.\r\n"
					+ "2. Round Robin Distribution\r\n"
					+ "3. Quit");
			
			String[] message = in.readLine().split(", ");
			
			ArrayList<String> searchInfo = new ArrayList<String>() ;
			ServerService.findings.add(searchInfo);
			
			String line = "client chose method "+ message[0] + " to search dir "+ message[1] + " for keyword: "+ message[2];
			ServerService.findings.get(0).add(line);
			
			System.out.println(line);
			
			
			if (os.contains("Window")) {
				//
			} else if (os.contains("Linux")) {
				linuxFinder(message[1], message[2], message[0]);
			} else if (os.contains("Mac")) {
				//
			}
			
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void linuxFinder(String path, String keyword, String opt) throws IOException {
		ServerService.totSearchTime = System.currentTimeMillis();
		System.out.println("Running Search On "+ this.os+ " for " + keyword + " in " + path);
		Runtime machine = Runtime.getRuntime();
		
////		BufferedReader fileSearchStream = new BufferedReader(new InputStreamReader(machine.exec("find "+ path +" -maxdepth 1 -type f").getInputStream()));
//		String[] command = {"find", path, "-maxdepth 1", "-type f"};
		ArrayList<String> command = new ArrayList<>();
        command.add("find");
        command.add(path);
        command.add("-maxdepth");
        command.add("1");
        command.add("-type");
        command.add("f");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        InputStream inputStream = process.getInputStream();
        Scanner fileSearchStream = new Scanner(inputStream, StandardCharsets.UTF_8);

        ArrayList<String> files = new ArrayList<>();
        while (fileSearchStream.hasNextLine()) {
            files.add(fileSearchStream.nextLine());
        }

//        // Print all files
//        for (String file : files) {
//            System.out.println(file);
//        }

        fileSearchStream.close();

        // Wait for the process to complete
        int exitCode;
		try {
			exitCode = process.waitFor();
			System.out.println("Process exited with code " + exitCode);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (opt.contains("1")) {
			equalSizeLinux(files, keyword);
		} else if (opt.contains("2")){
			roundrobinLinux(files, keyword);
		} else {
			client.close();
		}
		
	}
	
	public void equalSizeLinux(ArrayList<String> files, String keyword) {
		ServerService.totSearchTime = System.currentTimeMillis();
		EqualSizeLinuxThread[] threads = new EqualSizeLinuxThread[ServerService.cores];
	    int rem = files.size() % ServerService.cores;
	    int dist = files.size() / ServerService.cores;
	    int start = 0;

	    for (int i = 0; i < ServerService.cores; i++) {
	        int end = start + dist - 1;

	        if (rem > 0) {
	            end++;
	            rem--;
	        }
	        threads[i] = new EqualSizeLinuxThread(files, start, end, keyword, i);
	        threads[i].start();
	        start = end + 1;
	    }
	    System.out.println("all threads have rejoined");
	    for (EqualSizeLinuxThread thread: threads) {
	    	try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    
	    ServerService.presentAllFindings();
	    
	}
	
	public static synchronized int allocator() {
		if (files.size()-1 > ServerService.curr ) {
			ServerService.curr ++;
			return ServerService.curr;
		}
		return -1;
	}
	
	public void roundrobinLinux(ArrayList<String> files, String keyword) {
		RoundRobinLinuxThread[] threads = new RoundRobinLinuxThread[ServerService.cores];
		ServerService.files = files;
		for (int i = 0; i < ServerService.cores; i ++) {
			threads[i] = new RoundRobinLinuxThread(files,keyword, i);
			threads[i].start();
		}
		for (RoundRobinLinuxThread thread: threads) {
			try {
				thread.join();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		ServerService.presentAllFindings();
	}
	

	
	public static synchronized void appendFindings(ArrayList<String> tfindings) {
			ServerService.findings.add(tfindings);
	}
	
	public static synchronized void presentAllFindings() {
		ServerService.totSearchTime = System.currentTimeMillis() - ServerService.totSearchTime;
		String line = "total search time  (including threading, reassignment of task - if applicable and search) was " + ServerService.totSearchTime + " ms";
		ServerService.findings.get(0).add(line);
		
		PrintWriter output;
		try {
			output = new PrintWriter(ServerService.client.getOutputStream(), true);
			output.println("--------");
			for (ArrayList<String> tfindings: ServerService.findings) {
				for (String finding: tfindings) {
					try {
						output.println(finding);
//						System.out.println(finding);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("printed results to client side");
			output.println("done");
			output.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ServerService.client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}