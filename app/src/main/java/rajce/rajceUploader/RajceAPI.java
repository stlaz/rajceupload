package rajce.rajceUploader;

import android.widget.TextView;

import java.util.ArrayList;
import rajce.rajceUploader.network.thread.*;
import rajce.rajceUploader.network.info.*;
import rajce.rajceUploader.XML.AlbumListResponse;
import android.os.Handler;
import java.io.File;
import android.os.Environment;
import java.io.FilenameFilter;

public class RajceAPI {
    private String sessionToken = null;  
    public final Handler mHandler = new Handler();
    synchronized public void setSessionToken(String token) {
        sessionToken = token;
    }
    
    public RajceAPI() {
        super();
    }
    
    static public class Photo {
        public String fullFileName;
        public String photoName;
        public String description;
        
        public Photo(String fullFileName) {
            this.fullFileName = fullFileName;    
        }
        
        public Photo(String fullFileName, String photoName, String description) {
            this.fullFileName = fullFileName;   
            this.photoName = photoName;
            this.description = description;
        }
    }

    /**
     * Vrati true, pokud je nutne se prihlasit (ziskat od uzivatele login a heslo).
     * @return
     */
    public boolean isLogin() {
        return sessionToken != null;
    }

    /**
     * Prihlasi uzivatele a zapomatuje si jeho udaje.
     * @param email emailova adresa udana pri registraci
     * @param pass heslo jako
     * @param stat rozhrani pro zpetne volani informujici o vysledku operace
     * @return
     */
    public void sigin(String email, String pass, APIState stat, Handler mHandler) {
        SiginThread siginThread = new SiginThread(this, stat, email, MD5(pass), mHandler);
        siginThread.start();
        try {
            siginThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ziskani informaci o albech na rajce.net.
     * @param stat rozhrani pro zpetne volani informujici o vysledku operace (preda mu i informace o albech)
     * @return
     */
    public void getAlbumList(APIStateGetAlbumList stat) {
        GetAlbumListThread getAlbumListThread = new GetAlbumListThread(this, sessionToken, stat);
        getAlbumListThread.start();        
    }

    /**
     * Zkracena verze metody pro vytvoreni noveho alba.
     * @param stat rozhrani pro zpetne volani informujici o vysledku operace
     * @param name nazev alba
     * @return
     */
    public void newAlbum(APIStateNewAlbum stat, String name) {
        this.newAlbumAdvanced(stat, name, null, true, false, null, null);
    }

    /**
     * Plna verze pro vytvoreni noveho alba.
     * @param stat rozhrani pro zpetne volani informujici o vysledku operace
     * @param name nazev alba
     * @param albumDescription popis alba
     * @param visible false pokud ma byt album neviditelne
     * @param secure true pokud ma byt album chranene heslem a jmenem (nasledujici 2 parametry)
     * @param secureName null pokud je secure false
     * @param securePass null pokud je secure false, jinak plaintext
     * @return
     */
    public void newAlbumAdvanced(APIStateNewAlbum stat, String name, String albumDescription, boolean visible, boolean secure, String secureName, String securePass) {
        int visibleInt = visible ? 1 : 0;
        int secureInt = secure ? 1 : 0;
        NewAlbumThread newAlbumThread = new NewAlbumThread(this, sessionToken, stat, name, albumDescription, visibleInt, secureInt, secureName, securePass);
        newAlbumThread.start();         
    }

    /**
     * Hromadny upload fotek.
     * @param albumID identifikator ciloveho alba
     * @param stat rozhrani pro zpetne volani informujici o vysledku operace
     * @param photos informace o fotografiich pro upload
     * @return
     */
    public void uploadPhotos(int albumID, APIStateUpload stat, ArrayList<Photo> photos) {
        UploadPhotosThread uploadPhotosThread = new UploadPhotosThread(albumID, this, sessionToken, stat, photos);
        uploadPhotosThread.start();        
    }
    
    public static String MD5(String md5) {
       try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
              sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
           }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
 
    private static RajceAPI rajceAPI;
    private static TextView debug;
    
    /**
     * Metoda demonstrujici a testujici funkci tridy.
     * @return
     */     
    public static void testAPI(TextView t) {
        debug = t;
        rajceAPI = new RajceAPI();
       /* if (!rajceAPI.isLogin()) {
            rajceAPI.sigin("tkunovsky@seznam.cz", "vutfit", new APIState() {
                public void error(String error) {
                    debug.append("\n" + error);
                }

                public void finish() {
                    debug.append("\nTest login: OK.");
                    //testAPI1();
                }
            });
        } else {
            //testAPI1();
        }*/
    }
    
    private static void testAPI1() { 
        rajceAPI.getAlbumList(new APIStateGetAlbumList() {
            public void error(String error) {
                debug.append("\n" + error);
            }

            public void finish() {
                debug.append("\nTest get albums: OK.");
            }
            
            public void setAlbumList(AlbumListResponse response) {
                debug.append("\nLast album id: " + response.albums.get(response.albums.size() - 1).id);
                debug.append("\nLast album name: " + response.albums.get(response.albums.size() - 1).albumName);
                testAPI2(response);
            }           

        });
    }
    
    private static void testAPI2(AlbumListResponse oldResponse) { 
        rajceAPI.newAlbum(new APIStateNewAlbum() {
            public void error(String error) {
                debug.append("\n" + error);
            }

            public void finish() {
                debug.append("\nTest new album: OK.");
            }

            public void setAlbumID(int id) {
                debug.append("\nNew album id: " + Integer.toString(id));
                testAPI3(id);
            }
        }, "TestAlbum " + Integer.toString(oldResponse.totalCount));
    }    
   
    private static void testAPI3(int id) {
        ArrayList<Photo> photos = new ArrayList<Photo>();

        File rootsd = Environment.getExternalStorageDirectory();
        File dcim = new File(rootsd.getAbsolutePath() + "/DCIM/100ANDRO");
        File[] imagelist = dcim.listFiles(new FilenameFilter(){

            public boolean accept(File dir, String name)
            {
                return ((name.endsWith(".jpg"))||(name.endsWith(".png")));
            }
        });

        for(int i= 0 ; i< imagelist.length; i++)
        {
            photos.add(new Photo(imagelist[i].getAbsolutePath()));
        }

        rajceAPI.uploadPhotos(id, new APIStateUpload() {
            public void error(String error) {
                debug.append("\n" + error);
            }

            public void finish() {
                debug.append("\nTest upload photos: OK.");
            }

            @Override
            public void changeStat(int newStat) {
                debug.append("\n" + newStat);
            }
        },photos);        
    }
}
