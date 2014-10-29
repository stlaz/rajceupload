package rajce.rajceUploader.network.thread;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.RajceAPI.Photo;
import rajce.rajceUploader.XML.CloseAlbumRequest;
import rajce.rajceUploader.XML.CloseAlbumResponse;
import rajce.rajceUploader.XML.OpenAlbumRequest;
import rajce.rajceUploader.XML.OpenAlbumResponse;
import rajce.rajceUploader.XML.AddPhotoRequest;
import rajce.rajceUploader.XML.AddPhotoResponse;
import rajce.rajceUploader.network.RajceHttp;
import rajce.rajceUploader.network.RajceHttp.StateUpload;
import rajce.rajceUploader.network.info.APIStateUpload;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public class UploadPhotosThread  extends Thread  {
    private String albumToken;
    private int albumID;
    private RajceAPI rajceAPI;
    private APIStateUpload stat;        
    private String token;
    private RajceHttp rajceHttp;
    private String result;
    private ArrayList<Photo> photos;
    private int uploadCounter;
    
    public UploadPhotosThread(int albumID, RajceAPI rajceAPI, String token, APIStateUpload stat, ArrayList<Photo> photos) {
        super();
        this.rajceAPI = rajceAPI;
        this.token = token;
        this.stat = stat;
        this.rajceHttp = new RajceHttp(); 
        this.albumID = albumID;
        this.photos = photos;
        uploadCounter = 0;
    }
    
    private String openAlbum(int albumID) {
        Serializer serializer = new Persister();
        
        try {
            StringWriter sw = new StringWriter();
            serializer.write(new OpenAlbumRequest(token, albumID), sw);
            String result = rajceHttp.sendRequest(sw.toString());
            OpenAlbumResponse openAlbumResponse = serializer.read(OpenAlbumResponse.class, new StringReader( result ), false );
            if (openAlbumResponse.errorCode == null) {
                rajceAPI.setSessionToken(openAlbumResponse.sessionToken);
                this.token = openAlbumResponse.sessionToken;
                return openAlbumResponse.albumToken;
            } else {
                this.result = openAlbumResponse.result;
                return null;
            }
        } catch (Exception e) {
            this.result = e.toString();
            return null;
        }    
    }
    
    private int closeAlbum(String albumToken) {
        Serializer serializer = new Persister();
        
        try {
            StringWriter sw = new StringWriter();
            serializer.write(new CloseAlbumRequest(token, albumToken), sw);
            String result = rajceHttp.sendRequest(sw.toString());
            
            CloseAlbumResponse closeAlbumResponse = serializer.read(CloseAlbumResponse.class, new StringReader( result ), false );
            if (closeAlbumResponse.errorCode == null) {
                rajceAPI.setSessionToken(closeAlbumResponse.sessionToken);
                this.token = closeAlbumResponse.sessionToken;
                return 0;
            } else {
                this.result = closeAlbumResponse.result;
                return 1;
            }
        } catch (Exception e) {
            this.result = e.toString();
            return 2;
        }    
    }
    
    @Override
    public void run() {
        albumToken = openAlbum(albumID);
        if (albumToken == null) {
            rajceAPI.mHandler.post(new Runnable() {
                public void run()
                {
                    stat.error(result);
                }
            });
        } else {
            if (uploadPhotos() != 0 || closeAlbum(albumToken) != 0) {
                rajceAPI.mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.error(result);
                    }
                });
            } else {
                rajceAPI.setSessionToken(this.token);
                rajceAPI.mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.finish();
                    }
                });
            } 
        }
    }
    
    public int uploadPhotos() {
        Serializer serializer = new Persister();
        try {
            for (int i = 0; i < photos.size(); i++) {
                Bitmap image = BitmapFactory.decodeFile(photos.get(i).fullFileName);
                StringWriter sw = new StringWriter();
                serializer.write(new AddPhotoRequest(token, image.getWidth(), image.getHeight(), albumToken, photos.get(i).photoName, photos.get(i).fullFileName, photos.get(i).description), sw);
                Bitmap thumb =  Bitmap.createScaledBitmap (image, 100, 100, true);
                String result = rajceHttp.sendPhoto(sw.toString(), image, thumb, photos.get(i), new StateUpload() {
                        private int newStat;
                        @Override
                        public void changeStat(int nStat) {
                            this.newStat = nStat;
                            rajceAPI.mHandler.post(new Runnable() {
                                private int lastStat = -1;
                                public void run()
                                {
                                    int statNow = (uploadCounter*100 + newStat) / photos.size();
                                    if (lastStat != statNow) {
                                        stat.changeStat(statNow);
                                        lastStat = statNow;
                                    }
                                }
                            });
                        }
                    });
                AddPhotoResponse addPhotoResponse = serializer.read(AddPhotoResponse.class, new StringReader( result), false );
                if (addPhotoResponse.errorCode == null) {
                    this.token = addPhotoResponse.sessionToken;
                    rajceAPI.setSessionToken(addPhotoResponse.sessionToken);
                    uploadCounter++;
                } else {
                    this.result = addPhotoResponse.result;
                    return 1;   
                }
            }
        } catch (Exception e) {
            this.result = e.toString();
            return 2;
        }
        return 0;
    }
    
}