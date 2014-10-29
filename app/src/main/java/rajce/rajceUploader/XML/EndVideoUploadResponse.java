package rajce.rajceUploader.XML;

import java.util.ArrayList;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Attribute;

@Root(name="response")
public class EndVideoUploadResponse extends Error {
    
    @Element
    public String sessionToken;
    
    @Element
    public int photoID;
    
    @ElementList(entry="block")
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
