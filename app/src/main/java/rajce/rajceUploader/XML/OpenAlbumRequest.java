package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="request")
public class OpenAlbumRequest {
    
    @Element
    public String command = "openAlbum";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {
        @Element
        public String token;
        
        @Element
        public int albumID;
        
        public Parameters() {
            super();
        }
        
        public Parameters(String token, int albumID) {
            this(); 
            this.token = token;
            this.albumID = albumID;
        }        
    }
    
    public OpenAlbumRequest(String token, int albumID) {
        super();
        parameters = new Parameters(token,albumID);
    }
            
    public OpenAlbumRequest() {
        super();
        parameters = new Parameters();    
    }
}
