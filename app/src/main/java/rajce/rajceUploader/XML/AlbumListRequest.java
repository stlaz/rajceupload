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
        
        public Parameters(String token) {
            this(); 
            this.token = token;
        }        
    }
    
    public AlbumListRequest(String token) {
        super();
        parameters = new Parameters(token);
    }
    
    public AlbumListRequest() {
        super();
    }
}