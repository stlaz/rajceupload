package rajce.rajceUploader.XML;

import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Attribute;

@Root(name="request")
public class EndVideoUploadRequest {
    
    @Element
    public String command = "endVideoUpload";
    
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
        
        @Element
        public VideoInfo videoInfo;
        
        public static class VideoInfo {
            @Element(required=false)
            public String name;
            @Element(required=false)
            public String description;
            @Element
            public int fileSizeOriginal;
            @Element
            public int width;
            @Element
            public int height;
            @Element
            public int sourceWidth;
            @Element
            public int sourceHeight;
            @Element(required=false)
            public String cameraMake;
            @Element(required=false)
            public String cameraModel;
            @Element(required=false)
            public String gps;
            @Element(required=false)
            public String rotate;
            @Element(required=false)
            public String fullFileName;
            @Element(required=false)
            public String dateTime;
            @Element(required=false)
            public String vcodec;
            @Element(required=false)
            public String vcodecSrc;
            @Element(required=false)
            public String colorSpace;
            @Element(required=false)
            public String duration;
            @Element(required=false)
            public String durationSrc;
            @Element(required=false)
            public String begin;
            @Element(required=false)
            public String end;
            @Element(required=false)
            public String thumbpos;
            @Element(required=false)
            public String framerate;
            @Element(required=false)
            public String framerateSrc;
            @Element
            public int vbitrate;
            @Element
            public int vbitrateSrc;
            @Element(required=false)
            public String acodec;
            @Element(required=false)
            public String acodecSrc;
            @Element(required=false)
            public int asamplingRate;
            @Element(required=false)
            public int asamplingRateSrc;
            @Element(required=false)
            public int achannelCount;
            @Element(required=false)
            public int abitrate;
            @Element(required=false)
            public int abitrateSrc;
        }
        
        public Parameters() {
            super();
            blocks = new ArrayList<Block>();
            videoInfo = new VideoInfo();
        }
        
        public Parameters(String token, String uploadID, ArrayList<Block> blocks, VideoInfo videoInfo) {
            super();
            this.token = token;
            this.uploadID = uploadID;
            this.blocks = blocks;
            this.videoInfo = videoInfo;
        }      
    }
    
    public EndVideoUploadRequest(String token, String uploadID, ArrayList<Parameters.Block> blocks, Parameters.VideoInfo videoInfo) {
        super();
        parameters = new Parameters(token, uploadID, blocks, videoInfo);
    }
    
    public EndVideoUploadRequest() {
        super();
        parameters = new Parameters();        
    }
}
