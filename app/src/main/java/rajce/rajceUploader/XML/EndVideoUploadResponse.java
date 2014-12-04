package rajce.rajceUploader.XML;

import java.util.ArrayList;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Attribute;

@Root(name="response")
public class EndVideoUploadResponse extends Error {
    
    @Element(required=false)
    public String sessionToken;
    
    @Element(required=false)
    public int photoID;
    
    @ElementList(entry="block", required=false)
    public ArrayList<Block> blocks = new ArrayList<Block>(); 
    
    public static class Block {
        @Attribute
        public int index;      
        
        public Block() {
        }
    }
    
    public EndVideoUploadResponse() {
        super();
    }
}
