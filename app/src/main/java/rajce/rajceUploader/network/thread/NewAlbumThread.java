/**
 * Nazev: NewAlbumThread.java
 * Autor: Tomas Kunovsky
 * Popis: Vlakno pro vytvoreni noveho alba prihlaseneho uzivatele.
 */

package rajce.rajceUploader.network.thread;

import android.os.Handler;

import java.io.StringReader;
import java.io.StringWriter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.XML.NewAlbumRequest;
import rajce.rajceUploader.XML.NewAlbumResponse;
import rajce.rajceUploader.network.RajceHttp;
import rajce.rajceUploader.network.info.APIStateNewAlbum;

public class NewAlbumThread extends Thread  {
    private RajceAPI rajceAPI;
    private APIStateNewAlbum stat;        
    private String sessionToken;
    private String name;
    private String albumDescription;
    private int visibleInt;
    private int secureInt;
    private String secureName;
    private String securePass;    
    private RajceHttp rajceHttp;
    private NewAlbumResponse newAlbumResponse;
    private String errorText;
    private Handler mHandler;
    
    
    public NewAlbumThread(RajceAPI rajceAPI, String sessionToken, APIStateNewAlbum stat, String name, String albumDescription, int visibleInt, int secureInt, String secureName, String securePass, Handler mHandler) {
        super();
        this.rajceAPI = rajceAPI;
        this.stat = stat;        
        this.sessionToken = sessionToken;
        this.name = name;
        this.albumDescription = albumDescription;
        this.visibleInt = visibleInt;
        this.secureInt = secureInt;
        this.secureName = secureName;
        this.securePass = securePass;    
        this.rajceHttp = new RajceHttp();
        this.mHandler = mHandler;
    }
    
    @Override
    public void run() {
        Serializer serializer = new Persister();
        try {
            StringWriter sw = new StringWriter();
            serializer.write(new NewAlbumRequest(sessionToken, name, albumDescription, visibleInt, secureInt, secureName, securePass), sw);
            String result = rajceHttp.sendRequest(sw.toString());
            newAlbumResponse = serializer.read(NewAlbumResponse.class, new StringReader( result ), false );
            if (newAlbumResponse.errorCode == null) {
                rajceAPI.setSessionToken(newAlbumResponse.sessionToken);
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.finish();
                        stat.setAlbumID(newAlbumResponse.albumID);
                    }
                });
            } else {
                errorText = newAlbumResponse.result;
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.error(errorText);
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
