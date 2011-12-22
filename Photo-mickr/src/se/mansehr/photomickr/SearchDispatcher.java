package se.mansehr.photomickr;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import se.mansehr.photomickr.middletier.TagSource;
import se.mansehr.photomickr.model.Tag;

public class SearchDispatcher extends Thread {	
	Queue<Long> idQueue = new ConcurrentLinkedQueue<Long>();
	TagCollector[] tagCollectors = new TagCollector[5];
	TagSource ts;
	
	public SearchDispatcher(TagSource ts) {
		this.ts = ts;
	}
	
	public void start() {
		// Start the thread
		super.start();
		
		// Set up childthreads
		for(int i = 0; i < tagCollectors.length; ++i) {
			tagCollectors[i] = new TagCollector(i);
			tagCollectors[i].start();
		}
	}
	
	public void run() {
		while(true) {
			try {
			long startTime = System.currentTimeMillis();
			if(ts.getRecentIds(idQueue, 500) == TagSource.Response.OK) {
				long runtime = (System.currentTimeMillis() - startTime) / 1000;
				System.out.println("RunTime: " + runtime + "s");
				System.out.println("Antal idn: " + idQueue.size() + "stycken");
				System.out.println("Delta: " + idQueue.size()/runtime + "id/s");
			}
			
			Thread.sleep(2000);
			} catch(InterruptedException e) {
				//
			}
		}
	}

	public void printStatus() {
		// TODO Auto-generated method stub
		
	}
	
	private class TagCollector extends Thread {
		int workerId;
		
		public TagCollector(int id) {
			workerId = id;
		}
		
		public void run() {
			Long id = null;
			List<Tag> tagList = null;
			while(true) {
				try {
					id = idQueue.poll();
					if(id != null) {
						tagList = ts.getTagsFromObject(id);
						System.out.print(workerId + ": ");
						for(Tag t : tagList) {
							System.out.print(t.getTag() + " ");
						}
					}
					Thread.sleep(1000);
				} catch(Exception e) {
					// Dangerous
					System.out.print(workerId + ": Exception");
					try {
						Thread.sleep(10*1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
		}
	}
}
