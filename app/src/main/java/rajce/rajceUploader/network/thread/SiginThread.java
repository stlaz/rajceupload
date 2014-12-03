package rajce.rajceUploader.network.thread;

import android.util.Log;

import java.io.StringReader;
import java.io.StringWriter;
import android.os.Handler;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.xml.sax.InputSource;
import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.network.info.*;
import rajce.rajceUploader.XML.LoginRequest;
import rajce.rajceUploader.XML.LoginResponse;
import rajce.rajceUploader.network.RajceHttp;


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
            serializer.write(new LoginRequest(email, passMD5), sw);
            String result = rajceHttp.sendRequest(sw.toString());
            LoginResponse loginResponse = serializer.read(LoginResponse.class, new StringReader( result ), false);
            if (loginResponse.errorCode == null) {
                rajceAPI.setSessionToken(loginResponse.sessionToken);
                //stat.finish();
                this.mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.finish();
                    }
                });
            } else {
                errorText = loginResponse.result;
                //stat.error(errorText);
                this.mHandler.post(new Runnable() {
                    public void run()
                    {
                        stat.error(errorText);
                    }
                });
            }
        } catch (Exception e) {
            errorText = e.toString();
            rajceAPI.mHandler.post(new Runnable() {
                public void run()
                {
                    stat.error(errorText);
                }
            });
        }
    }
}
