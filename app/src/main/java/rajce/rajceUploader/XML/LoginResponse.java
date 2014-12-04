package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="response")
public class LoginResponse extends Error {
    
    @Element(required=false)
    public String sessionToken;
    
    @Element(required=false)
    public int maxWidth;
    
    @Element(required=false)
    public int maxHeight;
    
    @Element(required=false)
    public int quality;
    
    @Element(required=false)
    public String nick;
    
    public LoginResponse() {
        super();
    }
}
