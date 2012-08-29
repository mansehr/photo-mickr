package se.mansehr.photomickr;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mansehr.photomickr.middletier.TagSource;
import se.mansehr.photomickr.model.Tag;

public class SearchDispatcher extends Thread {

    private Logger log = LoggerFactory.getLogger(SearchDispatcher.class);
    
    Queue<Long> idQueue = new ConcurrentLinkedQueue<>();
    TagCollector[] tagCollectors = new TagCollector[5];
    TagSource ts;

    public SearchDispatcher(TagSource ts) {
        this.ts = ts;
    }

    @Override
    public void start() {
        // Start the thread
        super.start();

        // Set up childthreads
        for (int i = 0; i < tagCollectors.length; ++i) {
            tagCollectors[i] = new TagCollector(i);
            tagCollectors[i].start();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                long startTime = System.currentTimeMillis();
                if (ts.getRecentIds(idQueue, 500) == TagSource.Response.OK) {
                    long runtime = (System.currentTimeMillis() - startTime) / 1000;
                    log.debug("RunTime: {} s", runtime);
                    log.debug("Antal idn: {} stycken", idQueue.size());
                    log.debug("Delta: {} id/s", idQueue.size() / runtime);
                }

                Thread.sleep(2000);
            } catch (InterruptedException e) {
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

        @Override
        public void run() {
            Long id = null;
            List<Tag> tagList = null;
            while (true) {
                try {
                    id = idQueue.poll();
                    if (id != null) {
                        tagList = ts.getTagsFromObject(id);
                        log.debug("{}: ", workerId);
                        for (Tag t : tagList) {
                            log.debug("{} ", t.getTag());
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Dangerous
                    log.error("{}: {}", workerId, e);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e1) {
                    }
                }
            }

        }
    }
}
