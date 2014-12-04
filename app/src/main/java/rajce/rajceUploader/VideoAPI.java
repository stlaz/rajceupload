package rajce.rajceUploader;

import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FilenameFilter;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import java.io.IOException;
import android.media.MediaMetadataRetriever;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;




import android.media.ThumbnailUtils;
import android.provider.MediaStore;


public class VideoAPI {

    public int width;
    public int height;
    public String rotate;
    public String duration;
    public String fullFileName;
    public String dateTime;
    public String vcodec;
    public int bitrate;
    public String error;
    public Bitmap preview;
    public String frameRate;

    private MediaMetadataRetriever retriever;
    private MediaExtractor extractor;
    private MediaFormat format;

    public VideoAPI() {
        super();
    }

    public void setVideo(String path) {
        fullFileName = path;
        retriever = new MediaMetadataRetriever();
        String result;
        MediaExtractor extractor;

        try {
            retriever.setDataSource(path);
            preview = ThumbnailUtils.createVideoThumbnail(fullFileName, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
            extractor = new MediaExtractor();
            result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            if (result != null) {
                width = Integer.parseInt(result);
            } else {
                width = -1;
            }
            result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if (result != null) {
                height = Integer.parseInt(result);
            } else {
                height = -1;
            }
            result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (result != null) {
                rotate = result;
            } else {
                rotate = null;
            }

            extractor.setDataSource(path);
            format = extractor.getTrackFormat(0);
            result = format.getString(MediaFormat.KEY_MIME).replace("video/", "");
            if (result != null) {
                vcodec = result;
            } else {
                vcodec = null;
            }

            result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (result != null) {
                duration = String.format("%.3f", Double.parseDouble(result)).replace(",", ".");
            } else {
                duration = null;
            }

            result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
            if (result != null) {
                bitrate = Integer.parseInt(result)/1000;
            } else {
                bitrate = -1;
            }

            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            result = dt1.format((new Date(new File(this.fullFileName).lastModified())));
            if (result != null) {
                this.dateTime = result;
            } else {
                this.dateTime = null;
            }

            frameRate = (width >= 480) ? "12.000" : "30.000";
        } catch (Exception ex) {
            error = ex.getMessage();
        } finally {
            retriever.release();
        }
    }

    public void savePreview(String path, String name) {
        File rootsd = Environment.getExternalStorageDirectory();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path + "/" + name + ".jpeg");
            preview.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String toString() {
        String result = "";
        result += String.format("Width: %d\n", width);
        result += String.format("Height: %d\n", height);
        result += String.format("Rotate: %s\n", rotate);
        result += String.format("Full file name: %s\n", fullFileName);
        result += String.format("Codec: %s\n", vcodec);
        result += String.format("Duration: %s\n", duration);
        result += String.format("Bitrate: %d\n", bitrate);
        result += String.format("Date time: %s\n", this.dateTime);
        result += String.format("Frame rate: %s\n", frameRate);

        return result;

    }

    private static VideoAPI videoAPI;
    private static TextView debug;
    /**
     * Metoda demonstrujici a testujici funkci tridy.
     * @return
     */
    public static void testAPI(TextView t) {
        debug = t;
        File rootsd = Environment.getExternalStorageDirectory();
        File dcim = new File(rootsd.getAbsolutePath() + "/DCIM/100ANDRO");
        File[] imagelist = dcim.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return ((name.endsWith(".mp4")));
            }
        });

        VideoAPI video = new VideoAPI();
        video.setVideo(imagelist[0].getAbsolutePath());
        log(video.toString());
        video.savePreview(rootsd.getAbsolutePath() + "/DCIM/100ANDRO", "nahled2");
    }

    private static void log(String log) {
        debug.append("\n" + log);
    }


}
