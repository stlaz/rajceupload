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
            @Element
            public String name;
            @Element
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
            @Element
            public String cameraMake;
            @Element
            public String cameraModel;
            @Element
            public String gps;
            @Element
            public String rotate;
            @Element
            public String fullFileName;
            @Element
            public String dateTime;
            @Element
            public String vcodec;
            @Element
            public String vcodecSrc;
            @Element
            public String colorSpace;
            @Element
            public String duration;
            @Element
            public String durationSrc;
            @Element
            public String begin;
            @Element
            public String end;
            @Element
            public String thumbpos;
            @Element
            public String framerate;
            @Element
            public String framerateSrc;
            @Element
            public int vbitrate;
            @Element
            public int vbitrateSrc;
            @Element
            public String acodec;
            @Element
            public String acodecSrc;
            @Element
            public int asamplingRate;
            @Element
            public int asamplingRateSrc;
            @Element
            public int achannelCount;
            @Element
            public int abitrate;
            @Element
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
