package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="request")
public class CloseAlbumRequest {
    
    @Element
    public String command = "closeAlbum";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {
        @Element
        public String token;
        
        @Element
        public String albumToken;
        
        public Parameters() {
            super();
        }
        
        public Parameters(String token, String albumToken) {
            this(); 
            this.token = token;
            this.albumToken = albumToken;
        }        
    }
    
    public CloseAlbumRequest() {
        super();
        parameters = new Parameters();
    }
    
    public CloseAlbumRequest(String token, String albumToken) {
        super();
        parameters = new Parameters(token, albumToken);
    }
}
