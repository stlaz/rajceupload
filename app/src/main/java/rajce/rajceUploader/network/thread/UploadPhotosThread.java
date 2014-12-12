/**
 * Nazev: UploadPhotosThread.java
 * Autor: Tomas Kunovsky
 * Popis: Vlakno pro upload libovolneho poctu fotografii do zadaneho alba prihlaseneho uzivatele.
 */

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
import rajce.rajceUploader.network.RajceHttp.StatePhotoUpload;
import rajce.rajceUploader.network.info.APIStateUpload;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class UploadPhotosThread  extends UploadThread  {
    private String albumToken;
    private ArrayList<Photo> photos;
    private Handler mHandler;
    
    public UploadPhotosThread(int albumID, RajceAPI rajceAPI, String token, APIStateUpload stat, ArrayList<Photo> photos, Handler mHandler) {
        super(albumID, rajceAPI, token, stat);
        this.photos = photos;
        this.mHandler = mHandler;
    }
    
    @Override
    public void run() {
        albumToken = openAlbum(albumID);
        if (albumToken == null) {
            mHandler.post(new Runnable() {
                public void run()
                {
                    stat.error(result);
                }
            });
        } else {
            if (uploadPhotos() != 0 || closeAlbum(albumToken) != 0) {
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.error(result);
                    }
                });
            } else {
                rajceAPI.setSessionToken(this.token);
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.finish();
                    }
                });
            } 
        }
    }

    /**
     * Provede upload fotografii.
     * @return 0 pokud vse probehne v poradku
     */
    public int uploadPhotos() {
        Serializer serializer = new Persister();
        StateUploadPhotos stateUploads = new StateUploadPhotos(photos.size(), this.stat, rajceAPI, mHandler);
        try {
            for (int i = 0; i < photos.size(); i++) {
                Bitmap image = BitmapFactory.decodeFile(photos.get(i).fullFileName);
                StringWriter sw = new StringWriter();
                serializer.write(new AddPhotoRequest(token, image.getWidth(), image.getHeight(), albumToken, photos.get(i).photoName, photos.get(i).fullFileName, photos.get(i).description), sw);
                Bitmap thumb =  Bitmap.createScaledBitmap (image, 100, 100, true);
                image.recycle();
                String result = rajceHttp.sendPhoto(sw.toString(), thumb, photos.get(i), stateUploads);
                AddPhotoResponse addPhotoResponse = serializer.read(AddPhotoResponse.class, new StringReader( result), false );
                if (addPhotoResponse.errorCode == null) {
                    this.token = addPhotoResponse.sessionToken;
                    rajceAPI.setSessionToken(addPhotoResponse.sessionToken);
                    stateUploads.incUploaded();
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
