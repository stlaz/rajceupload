package rajce.rajceUploader.XML;

import java.util.ArrayList;
import java.util.Arrays;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;

@Root(name="request")
public class AlbumListRequest {
    
    @Element
    public String command = "getAlbumList";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {
        @Element
        public String token;
        @Element
        public int skip = 0;
        @Element
        public int limit = Integer.MAX_VALUE;

        @ElementList(entry="column")
        public ArrayList<String> columns = new ArrayList<String>();    
            
        public Parameters() {
            super();
            String[] poleStringu = {"descriptionHtml", "coverPhotoID"};
            columns = new ArrayList<String>(Arrays.asList(poleStringu)); 
        }
        
        public Parameters(String token, int skip, int limit) {
            this(); 
            this.token = token;
            this.skip = skip;
            this.limit = limit;
        }        
    }
    
    public AlbumListRequest(String token, int skip, int limit) {
        super();
        parameters = new Parameters(token, skip, limit);
    }
    
    public AlbumListRequest() {
        super();
    }
}