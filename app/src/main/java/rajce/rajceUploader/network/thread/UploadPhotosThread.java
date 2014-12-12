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
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap image = BitmapFactory.decodeFile(photos.get(i).fullFileName, options);
                image = null;
                StringWriter sw = new StringWriter();
                serializer.write(new AddPhotoRequest(token, options.outWidth, options.outHeight, albumToken, photos.get(i).photoName, photos.get(i).fullFileName, photos.get(i).description), sw);
                Bitmap thumb =  decodeSampledBitmap(photos.get(i).fullFileName, 100, 100);
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

    private Bitmap decodeSampledBitmap(String fullName, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fullName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap sample0 =  BitmapFactory.decodeFile(fullName, options);
        return Bitmap.createScaledBitmap (sample0, 100, 100, true);
    }


    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    
}


