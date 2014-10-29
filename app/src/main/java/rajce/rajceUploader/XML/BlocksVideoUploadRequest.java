package rajce.rajceUploader.XML;

import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Attribute;

@Root(name="request")
public class BlocksVideoUploadRequest {
    
    @Element
    public String command = "blocksVideoUpload";
    
    @Element
    public Parameters parameters;
    
    public static class Parameters {
        @Element
        public String token;
        
        @Element
        public String uploadID;
        
        @ElementList(entry="block")
        public ArrayList<Block> blocks; 
        
        public static class Block {
            @Attribute
            public int index;
            
            @Element
            public int size;
            
            @Element
            public String md5;     
                
            public Block(int index, int size, String md5) {
                super();
                this.index = index;
                this.size = size;
                this.md5 = md5;
            }      
            
            public Block() {
            }
        }
        
        public Parameters() {
            super();
            blocks = new ArrayList<Block>();
        }
        
        public Parameters(String token, String uploadID, ArrayList<Block> blocks) {
            super();
            this.token = token;
            this.uploadID = uploadID;
            this.blocks = blocks;
        }      
    }
    
    public BlocksVideoUploadRequest() {
        super();
        parameters = new Parameters();
    }
    
    public BlocksVideoUploadRequest(String token, String uploadID, ArrayList<Parameters.Block> blocks) {
        super();
        parameters = new Parameters(token, uploadID, blocks);
    }
}
