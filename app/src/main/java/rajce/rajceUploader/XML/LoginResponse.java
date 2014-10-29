package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="response")
public class LoginResponse extends Error {
    
    @Element
    public String sessionToken;
    
    @Element
    public int maxWidth;
    
    @Element
    public int maxHeight;
    
    @Element
    public int quality;
    
    @Element
    public String nick;
    
    public LoginResponse() {
        super();
    }
}
