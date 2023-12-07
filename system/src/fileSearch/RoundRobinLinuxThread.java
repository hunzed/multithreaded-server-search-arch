package fileSearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class RoundRobinLinuxThread extends Thread{
	ArrayList<String> files;
	String keyword;
	ArrayList<String> findings;
	int id;
	
	public RoundRobinLinuxThread(ArrayList<String> files, String keyword, int id) {
		this.files = files;
		this.keyword = keyword;
		this.findings =new ArrayList<String>();
		this.id = id;
	}
	
	public void run () {
		int index = ServerService.allocator();
		findings.add("Thread " +id + " searched ");
		long time = System.currentTimeMillis();
		
		int count =0;
		
		while (index >= 0) {
			count++;
			String[] command = {"grep", "-c", "-i", keyword, files.get(index)};
	            ProcessBuilder processBuilder = new ProcessBuilder(command);
	            Process process;
				try {
					 process = processBuilder.start();
					 InputStream inputStream = process.getInputStream();
			         Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
			         int resultCount = scanner.nextInt();
			         
			         if (resultCount > 0) {
						 findings.add('*' + files.get(index) + ' ' + resultCount);
					 }
					 else{
						 findings.add(files.get(index) + ' ' + resultCount);
					 }
					
				} catch (IOException e) {
					e.printStackTrace();
				}

			index = ServerService.allocator();
			
		}
		long timetaken = System.currentTimeMillis() - time;
		String threadInfo = findings.get(0) + count + " files in " + timetaken + " ms";
		findings.set(0, threadInfo);
		
		ServerService.appendFindings(findings);
		
	}
	
	
}
