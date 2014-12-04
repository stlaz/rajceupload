/**
 * Nazev: UploadThread.java
 * Autor: Tomas Kunovsky
 * Popis: Abstraktni trida pro tridu pro upload videi a tridu pro upload fotografii do zadaneho alba prihlaseneho uzivatele.
 */

package rajce.rajceUploader.network.thread;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.StringReader;
import java.io.StringWriter;

import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.XML.CloseAlbumRequest;
import rajce.rajceUploader.XML.CloseAlbumResponse;
import rajce.rajceUploader.XML.OpenAlbumRequest;
import rajce.rajceUploader.XML.OpenAlbumResponse;
import rajce.rajceUploader.network.RajceHttp;
import rajce.rajceUploader.network.info.APIStateUpload;

public abstract class UploadThread extends Thread {
    protected int albumID;
    protected RajceAPI rajceAPI;
    protected APIStateUpload stat;
    protected String token;
    protected RajceHttp rajceHttp;
    protected String result;

    public UploadThread(int albumID, RajceAPI rajceAPI, String token, APIStateUpload stat) {
        super();
        this.rajceAPI = rajceAPI;
        this.token = token;
        this.stat = stat;
        this.rajceHttp = new RajceHttp();
        this.albumID = albumID;
    }

    /**
     * Pripravi album na serveru pro upload ("otevre ho").
     * @param albumID identifikator ciloveho alba
     * @return null pokud dojde k chybe, jinak token pro album
     */
    protected String openAlbum(int albumID) {
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

    /**
     * Da vedet serveru, ze upload je u konce ("zavre album").
     * @param albumToken token alba do ktereho byly pridany fotky
     * @return 0 pokud vse probehne v poradku
     */
    protected int closeAlbum(String albumToken) {
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
}
