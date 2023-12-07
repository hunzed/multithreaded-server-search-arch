package fileSearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class EqualSizeLinuxThread extends Thread {
	ArrayList<String> files;
	int start, end;
	ArrayList<String> findings;
	String keyword;
	int id;
	
	public EqualSizeLinuxThread(ArrayList<String> files, int start, int end, String keyword, int id) {
		this.files = files;
		this.start = start;
		this.end = end;
		this.keyword = keyword;
		this.findings =new ArrayList<String>();
		this.id = id;
	}
	
	public void run () {
		findings.add("Thread " +id + " searched ");
		int count = 0;
		long startTime = System.currentTimeMillis();
		for (int i = start; i < end+1; i++) {
			count++;
			try {
				String[] command = {"grep", "-c", "-i", keyword, files.get(i)};

	            ProcessBuilder processBuilder = new ProcessBuilder(command);
	            Process process;
				try {
					 process = processBuilder.start();
					 InputStream inputStream = process.getInputStream();
			         Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
			         int resultCount = scanner.nextInt();
			         
			         if (resultCount > 0) {
						 findings.add('*' + files.get(i) + ' ' + resultCount);
					 }
					 else{
						 findings.add(files.get(i) + ' ' + resultCount);
					 }
			         
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long totalTime = System.currentTimeMillis() - startTime;
		String threadInfo = findings.get(0) + count + " files in " + totalTime + " ms";
		findings.set(0, threadInfo);
		ServerService.appendFindings(findings);
	}
	

}
