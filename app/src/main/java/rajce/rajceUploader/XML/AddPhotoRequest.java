package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="request")
public class AddPhotoRequest {
    
    @Element
    public String command = "addPhoto";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {
        @Element
        public String token;
        
        @Element
        public int width;
        
        @Element
        public int height;
        
        @Element
        public String albumToken;
        
        @Element(required=false)
        public String photoName;
        
        @Element(required=false)
        public String fullFileName;
        
        @Element(required=false)
        public String description; 
        
        public Parameters() {
            super();
        }
        
        public Parameters(String token, int width, int height, String albumToken, String photoName, String fullFileName, String description) {
            super();
            this.token = token;
            this.width = width;
            this.height = height;
            this.albumToken = albumToken;
            this.photoName = photoName;
            this.fullFileName = fullFileName;
            this.description = description;
        }      
    }
    
    public AddPhotoRequest(String token, int width, int height, String albumToken, String photoName, String fullFileName, String description) {
        parameters = new Parameters(token, width, height, albumToken, photoName, fullFileName, description);
    }
    
    public AddPhotoRequest() {
        parameters = new Parameters();
    }
}
