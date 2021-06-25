package wordCounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainClass {
	
	public static ConcurrentHashMap<String, Integer> wordCount = new ConcurrentHashMap<>();
	
	public static class WordCounterStatistics implements Runnable {
		
		public void run() {
			
			try {
				System.out.println("Number of words = " + MainClass.wordCount.size());
			} finally {
				System.out.print("Statistics finished \n");
			}
							
		}
	} 
	
	public static class WordCounterReader implements Runnable {
		
		String filename;
		
		WordCounterReader(String filename) {
			this.filename = filename;
		}
		
		public void run() {
			
			try {
				Scanner in = new Scanner(new File(filename));
				
				while (in.hasNext()) {
					String word = in.next().replaceAll("\\p{Punct}", "");
					
					if (MainClass.wordCount.containsKey(word)) {
						MainClass.wordCount.put(word, MainClass.wordCount.get(word) + 1);
					} else {
						MainClass.wordCount.put(word, 1);
					}						
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		
		Long start = System.nanoTime();
		
		var executorService = Executors.newCachedThreadPool();
		var indexScanner = new Scanner(new File("index.txt"));

		// Thread for statistics purposes
		executorService.execute(new WordCounterStatistics());
		
		while (indexScanner.hasNext()) {
			
			String bookName = indexScanner.next();
			executorService.execute(new WordCounterReader(bookName));
		}
				
		executorService.shutdown();
		
		if (executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
			
			wordCount
				.entrySet()
				.stream()
		        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) 
				.limit(500)
				.forEach(x -> System.out.println("Word = " + x.getKey() + " - Freq = " + x.getValue()));

			Long end = System.nanoTime();
			System.out.print("Total elapsed time = " + (end-start)/1e9 + "\n");
		}
		
	}
}
