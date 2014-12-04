/**
 * Nazev: StateUploads.java
 * Autor: Tomas Kunovsky
 * Popis: Pomocna trida, ktera prepocitava stav uploadu jednotlivych fotek na celkovy stav uploadu.
 */

package rajce.rajceUploader.network.thread;

import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.network.RajceHttp;
import rajce.rajceUploader.network.info.APIStateUpload;
import android.os.Handler;

public class StateUploadPhotos  implements RajceHttp.StatePhotoUpload {
    private int counterUploaded;
    private int count;
    private RajceAPI rajceAPI;
    private APIStateUpload stat;
    private Handler mHandler;
    /*Vlakno informujici o celkove zmene stavu uploadu.*/
    private class UIThread implements Runnable {
        private int newStat;
        private APIStateUpload stat;
        public UIThread(int newStat, APIStateUpload stat) {
            this.newStat = newStat;
            this.stat = stat;
        }
        public void run() {
            stat.changeStat(newStat);
        }
    }
    public StateUploadPhotos(int count, APIStateUpload stat, RajceAPI rajceAPI, Handler mHandler) {
        counterUploaded = 0;
        this.count = count;
        this.stat = stat;
        this.rajceAPI = rajceAPI;
        this.mHandler = mHandler;
    }

    public void incUploaded() {
        counterUploaded++;
    }

    @Override
    synchronized public void changeStat(int newStat) {
        mHandler.post(new UIThread((counterUploaded*100 + newStat) / count, stat ));
    }
}
