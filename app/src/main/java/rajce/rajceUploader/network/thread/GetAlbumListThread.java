/**
 * Nazev: GetAlbumListThread.java
 * Autor: Tomas Kunovsky
 * Popis: Vlakno pro ziskani seznamu alb prihlaseneho uzivatele.
 */

package rajce.rajceUploader.network.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.util.Log;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.network.info.*;
import rajce.rajceUploader.XML.AlbumListRequest;
import rajce.rajceUploader.XML.AlbumListResponse;
import rajce.rajceUploader.network.RajceHttp;

public class GetAlbumListThread extends Thread {
    private RajceAPI rajceAPI;
    private APIStateGetAlbumList stat;        
    private String token;
    private RajceHttp rajceHttp;
    private AlbumListResponse albumListResponse;
    private String errorText;
    private Handler mHandler;
    private int skip;
    private int limit;
    
    public GetAlbumListThread(RajceAPI rajceAPI, String token, APIStateGetAlbumList stat, int skip, int limit, Handler mHandler) {
        super();
        this.rajceAPI = rajceAPI;
        this.token = token;
        this.stat = stat;
        this.rajceHttp = new RajceHttp();
        this.mHandler = mHandler;
        this.skip = skip;
        this.limit = limit;
    }
    
    @Override
    public void run() {
        Serializer serializer = new Persister();

        try {
            StringWriter sw = new StringWriter();
            serializer.write(new AlbumListRequest(token, skip, limit), sw);
            String result = rajceHttp.sendRequest(sw.toString());
            albumListResponse = serializer.read(AlbumListResponse.class, new StringReader( result ), false );

            for (int i = 0; i < albumListResponse.albums.size(); i++) {
                if (albumListResponse.albums.get(i).photoCount + albumListResponse.albums.get(i).videoCount == 0) {
                    Log.e("TAG_THUMB", "SKIPPED (no photo or video)");
                    continue;
                }
                URL url = new URL(albumListResponse.albums.get(i).thumbUrl);
                URLConnection con = url.openConnection();
                InputStream instr = con.getInputStream();
                albumListResponse.albums.get(i).coverPhoto = BitmapFactory.decodeStream(instr);
                instr.close();
            }


            if (albumListResponse.errorCode == null) {
                rajceAPI.setSessionToken(albumListResponse.sessionToken);
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.finish();
                        stat.setAlbumList(albumListResponse);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.error(albumListResponse.result);
                    }
                });
            }
        } catch (Exception e) {
            errorText = e.toString();
            mHandler.post(new Runnable() {
                public void run()
                {
                    stat.error(errorText);
                }
            });
        }
    }
}
