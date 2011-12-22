package se.mansehr.photomickr.middletier;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.mansehr.photomickr.Persistator;

public class FlickrMiddletierTest {
	static TagSource ts = new FlickrMiddletier();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ts.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetRecentIds() {
		long startTime = System.currentTimeMillis();
		Queue<Long> idQueue = new ConcurrentLinkedQueue<Long>(); 
		if(ts.getRecentIds(idQueue, 500) == TagSource.Response.OK) {
			long runtime = (System.currentTimeMillis() - startTime) / 1000;
			System.out.println("RunTime: " + runtime + "s");
			System.out.println("Antal idn: " + idQueue.size() + "stycken");
			System.out.println("Delta: " + idQueue.size()/runtime + "id/s");
		}
		if(Persistator.saveObject(idQueue, "flickr.ids") == true) {
			System.out.println("SaveOK");
		} else {
			System.out.println("SaveFailed");
		}
	}
	
	@Test
	public void testExsists() {
		@SuppressWarnings("unchecked")
		Queue<Long> idQueue = (Queue<Long>)Persistator.loadObject("flickr.ids");
		Set<Long> set = new TreeSet<Long>();
		Long l;
		long match = 0;
		while (idQueue.peek() != null) {
			l = idQueue.poll();
			long value = l.longValue();
			System.out.print(""+ ((match & value) == value) + " : ");
			System.out.print(""+ set.contains(l) + " // ");
			set.add(l);
			match |= value;
			System.out.print(""+ match + " " + value);
			System.out.print("\n");
		}
	}

}
