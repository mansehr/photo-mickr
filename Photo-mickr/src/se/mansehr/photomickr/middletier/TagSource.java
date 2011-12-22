package se.mansehr.photomickr.middletier;

import java.util.Date;
import java.util.List;
import java.util.Queue;

import se.mansehr.photomickr.model.Tag;

public abstract class TagSource {	
	public enum Response { OK, OK_BUT_LAST, NO_CONTACT,  FAIL}

	public Response getIdsFromDay(Date c, Queue<Long> returnList) {
		return getIdsFromDay(c, returnList, Long.MAX_VALUE);
	}
	
	public abstract Response getIdsFromDay(Date c, Queue<Long> returnList, long limit);
	public abstract Response getRecentIds(Queue<Long> returnList, int limit);
	public abstract List<Tag> getTagsFromObject(Long id);


	public abstract boolean setUp();
	
	public abstract boolean tearDown();
}
