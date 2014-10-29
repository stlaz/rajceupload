package rajce.rajceUploader.XML;

import java.util.ArrayList;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Attribute;

@Root(name="response")
public class AlbumListResponse extends Error {
    
    @Element
    public String sessionToken;
    
    @Element
    public int totalCount;
    
    @ElementList(entry="album")
    public ArrayList<Album> albums = new ArrayList<Album>(); 
    
    public static class Album {
        @Attribute
        public int id;
        
        @Element
        public String albumName;
        
        @Element(required=false)
        public String description;
        
        @Element(required=false)
        public String descriptionHtml;
        
        @Element
        public String url;
        
        @Element(required=false)
        public String thumbUrl;
        
        @Element(required=false)
        public String thumbUrlBest;
        
        @Element(required=false)
        public String createDate;
        
        @Element(required=false)
        public String updateDate;
        
        @Element
        public int hidden;
        
        @Element
        public int secure;

        @Element
        public int photoCount;
        
        @Element(required=false)
        public int videoCount;
        
        @Element(required=false)
        public String startDateInterval;
        
        @Element(required=false)
        public String endDateInterval;
     
        @Element(required=false)
        public int isFavourite;        
    
        @Element(required=false)
        public int coverPhotoID;        
            
        public Album() {
            super();
        }      
    }
    
    public AlbumListResponse() {
        super();
    }
}
