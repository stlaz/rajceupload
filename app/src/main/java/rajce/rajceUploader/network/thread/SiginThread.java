/**
 * Nazev: SiginThread.java
 * Autor: Tomas Kunovsky
 * Popis: Vlakno pro prihlaseni uzivatele.
 */

package rajce.rajceUploader.network.thread;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.network.info.*;
import rajce.rajceUploader.XML.LoginRequest;
import rajce.rajceUploader.XML.LoginResponse;
import rajce.rajceUploader.network.RajceHttp;

import android.os.Handler;
import android.util.Log;


public class SiginThread extends Thread {
    private RajceAPI rajceAPI;
    private APIState stat;        
    private String email = null;
    private String passMD5 = null;
    private RajceHttp rajceHttp;
    private String errorText;
    private Handler mHandler;

    public SiginThread(RajceAPI rajceAPI, APIState stat, String email, String passMD5, Handler mHandler) {
        super();
        this.rajceAPI = rajceAPI;
        this.stat = stat;
        this.email = email; 
        this.passMD5 = passMD5;
        this.rajceHttp = new RajceHttp();
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        Serializer serializer = new Persister();
        try {
            StringWriter sw = new StringWriter();
            serializer.write(new LoginRequest(email, passMD5), sw);//serializuje data do XML
            String result = rajceHttp.sendRequest(sw.toString()); //posle a nasledne ziska XML ze serveru
            LoginResponse loginResponse = serializer.read(LoginResponse.class, new StringReader( result ), false); //deserializuje XML do objektu
            if (loginResponse.errorCode == null) { //pokus server nevratil chybu
                rajceAPI.setSessionToken(loginResponse.sessionToken); //nastavim session retezec (neco jako cookie)
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.finish();//poslu zpravu pres callback ze vse dopadlo v poradku
                    }
                });
            } else {
                errorText = loginResponse.errorCode;
                mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.error(errorText);//preposlu zpravu o chybe
                    }
                });
            }
        } catch (Exception e) {
            errorText = "-1";
            mHandler.post(new Runnable() {
                public void run()
                {
                    stat.error(errorText);//preposlu zpravu o chybe
                }
            });
        }
    }
}
