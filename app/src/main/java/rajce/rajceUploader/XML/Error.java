package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;

public class Error {
    
    @Element(required=false)
    public String result;
    
    @Element(required=false)
    public String errorCode;
    
    public Error() {
        super();
    }
}
