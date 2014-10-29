package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="request")
public class BeginVideoUploadRequest {
    
    @Element
    public String command = "beginVideoUpload";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {
        @Element
        public String token;
        
        @Element
        public String albumToken;
        
        @Element
        public String clientVideoID;
        
        public Parameters() {
            super();
        }
        
        public Parameters(String token, String albumToken, String clientVideoID) {
            super();
            this.token = token;
            this.albumToken = albumToken;
            this.clientVideoID = clientVideoID;
        }      
    }
    
    public BeginVideoUploadRequest() {
        super();
        parameters = new Parameters();
    }
    
    public BeginVideoUploadRequest(String token, String albumToken, String clientVideoID) {
        super();
        parameters = new Parameters(token, albumToken, clientVideoID);
    }
}
