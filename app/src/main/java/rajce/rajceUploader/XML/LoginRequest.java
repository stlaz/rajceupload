package rajce.rajceUploader.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="request")
public class LoginRequest {
    
    @Element
    public String command = "login";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {      
        @Element(required=false)
        public String clientID;
        @Element(required=false)
        public String currentVersion;
        @Element(required=false)
        public String lang;
        @Element
        public String login;
        @Element
        public String password;  
        public Parameters() {
            super();
        }
        
        public Parameters(String login, String password) {
            super();
            this.login = login;
            this.password = password;
        }
    }
    
    public LoginRequest() {
        super();
        parameters = new Parameters();
    }
    
    public LoginRequest(String login, String password) {
        super();
        parameters = new Parameters(login, password);
    }
}
