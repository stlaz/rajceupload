package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="response")
public class CloseAlbumResponse extends Error  {
    
    @Element
    public String sessionToken;
    
    public CloseAlbumResponse() {
        super();
    }
}
