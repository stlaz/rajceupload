package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="response")
public class NewAlbumResponse extends Error  {
    
    @Element
    public String sessionToken;
    
    @Element
    public String albumToken;
 
    @Element
    public int albumID;
    
    public NewAlbumResponse() {
        super();
    }
}
