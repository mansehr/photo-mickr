package se.mansehr.photomickr.middletier;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.util.IOUtilities;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import se.mansehr.photomickr.SearchDispatcher;
import se.mansehr.photomickr.model.Tag;

public class FlickrMiddletier extends TagSource {

    private Logger log = LoggerFactory.getLogger(SearchDispatcher.class);
    Flickr f;
    RequestContext requestContext;
    String frob = "";
    String token = "";
    Properties properties = null;
    PhotoList fromDayPhotoList = null;
    int recentPage = 1;
    
    public FlickrMiddletier() {
        super("Flickr");
    }

    /*
     * private void getHotList() { TagsInterface tagInterface =
     * f.getTagsInterface(); try { Collection collection =
     * tagInterface.getHotList("day", 20); } catch (IOException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } catch (SAXException e)
     * { // TODO Auto-generated catch block e.printStackTrace(); } catch
     * (FlickrException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); }
     * 
     * }
     */
    @SuppressWarnings("unchecked")
    @Override
    public Response getIdsFromDay(Date startDay, Queue<Long> returnList,
            long limit) {
        // getHotList();
        assert (returnList != null);

        int page = 1, pages = 0;

        PhotosInterface p = f.getPhotosInterface();
        Date endDay = (Date) startDay.clone();
        endDay.setTime(endDay.getTime() + (24 * 60 * 60 * 1000));
        SearchParameters sp = new SearchParameters();

        System.out.println(startDay + " : " + startDay.getTime());
        System.out.println(endDay + " : " + endDay.getTime());

        // sp.setSort(SearchParameters.DATE_POSTED_DESC);
        sp.setMinUploadDate(startDay);
        sp.setMaxUploadDate(endDay);
        do {
            try {
                log.debug("Searching");
                fromDayPhotoList = null;
                fromDayPhotoList = p.getRecent(500, page);// p.search(sp, 100,
                // page);
                pages = fromDayPhotoList.getPages();
                log.debug("Retrieved: {}", fromDayPhotoList.size());
                addToList(fromDayPhotoList.iterator(), returnList, limit);
            } catch (IOException|SAXException|FlickrException e) {
                log.error("getIdsFromDay: ", e);
            }
            log.debug("{} {}",page+"/"+pages,returnList.size());
        } while (++page <= pages && returnList.size() < limit);
        return Response.OK;
    }

    @Override
    public List<Tag> getTagsFromObject(Long id) {
        List<Tag> returnList = new ArrayList<>();
        PhotosInterface p = f.getPhotosInterface();

        return returnList;
    }

    private void addToList(Iterator<Photo> it, Queue<Long> returnList,
            long limit) {
        while (it.hasNext() && returnList.size() < limit) {
            Photo photo = it.next();
            try {
                if (returnList.offer(Long.parseLong(photo.getId())) != true) {
                    log.error("Queue.offer failed. Size: {}", returnList.size());
                }
            } catch (NumberFormatException e) {
                log.error("ParseException");
                // Suppress parse error
            }
        }
    }

    @Override
    public boolean setUp() {
        try {
            return setUpFlickr();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return false;
        }
    }

    private boolean setUpFlickr() throws ParserConfigurationException,
            IOException, SAXException {
        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
            properties.load(in);
        } finally {
            IOUtilities.close(in);
        }
        f = new Flickr(properties.getProperty("apiKey"),
                properties.getProperty("secret"), new REST());
        requestContext = RequestContext.getRequestContext();
        Auth auth = new Auth();
        auth.setPermission(Permission.READ);
        auth.setToken(properties.getProperty("token"));
        requestContext.setAuth(auth);
        Flickr.debugRequest = false;
        Flickr.debugStream = false;
        return true;
    }

    @Override
    public boolean tearDown() {
        log.debug("Tear down: {}", getTagSourceName());
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response getRecentIds(Queue<Long> returnList, int limit) {
        assert (returnList != null);
        assert (limit <= 500);

        PhotosInterface p = f.getPhotosInterface();

        try {
            // TODO: Better control of the recent page
            PhotoList recentPhotoList = p.getRecent(limit, recentPage++);
            log.debug("Recieved recent from Flickr: {}", recentPhotoList.getTotal());
            addToList(recentPhotoList.iterator(), returnList, limit);
        } catch (IOException|SAXException|FlickrException e) {
            log.error("Failed getRecentIds, {}", e);
        }
        return Response.OK;
    }
}
