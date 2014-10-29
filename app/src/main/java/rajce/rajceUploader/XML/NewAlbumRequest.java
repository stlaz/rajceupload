package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="request")
public class NewAlbumRequest {
    
    @Element
    public String command = "createAlbum";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {
        @Element
        public String token;
        
        @Element
        public String albumName;
        
        @Element(required=false)
        public String albumDescription;
          
        @Element
        public int albumVisible;
        
        @Element
        public int secure;
        
        @Element(required=false)
        public String secureName;

        @Element(required=false)
        public String securePass;
        
        public Parameters() {
            super();
        }
        
        public Parameters(String token, String name, String albumDescription, int visibleInt, int secureInt, String secureName, String securePass) {
            this(); 
            this.token = token;
            this.albumName = name;
            this.albumDescription = albumDescription;
            this.albumVisible = visibleInt;
            this.secure = secureInt;
            this.secureName = secureName;
            this.securePass = securePass;
        }        
    }
    
    public NewAlbumRequest() {}
    
    public NewAlbumRequest(String token, String name, String albumDescription, int visibleInt, int secureInt, String secureName, String securePass) {
        super();
        parameters = new Parameters(token, name, albumDescription, visibleInt, secureInt, secureName, securePass);
    }
}
