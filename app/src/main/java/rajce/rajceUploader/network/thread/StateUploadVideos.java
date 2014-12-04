package rajce.rajceUploader.network.thread;


import rajce.rajceUploader.RajceAPI;
import rajce.rajceUploader.network.info.APIStateUpload;
import android.os.Handler;

public class StateUploadVideos {
    private int totalNumberBlocks; //celkovy pocet bloku vsech videi
    private int uploadedNumberBlocks; //uploadovany pocet bloku vsech videi
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

    public StateUploadVideos(int totalNumberBlocks, APIStateUpload stat, RajceAPI rajceAPI,Handler mHandler) {
        this.totalNumberBlocks = totalNumberBlocks;
        this.stat = stat;
        this.rajceAPI = rajceAPI;
        uploadedNumberBlocks = 0;
        this.mHandler = mHandler;
    }

    synchronized public void incUploaded() {
        uploadedNumberBlocks++;
        mHandler.post(new UIThread((uploadedNumberBlocks*100) / totalNumberBlocks, stat));
    }
}
