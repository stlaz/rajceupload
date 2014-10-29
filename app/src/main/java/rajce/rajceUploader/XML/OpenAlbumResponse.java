package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="response")
public class OpenAlbumResponse extends Error {
    
    @Element
    public String sessionToken;
    
    @Element
    public String albumToken;
    
    public OpenAlbumResponse() {
        super();
    }
}
