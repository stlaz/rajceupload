package rajce.rajceUploader;

import android.os.Bundle;

import java.util.List;

/**
 * Created by standa on 12/4/14.
 */
public class MediaSingleton {
    private static MediaSingleton instance = null;
    public static List<Long> selIDs = null;

    protected MediaSingleton() {
    }
    public static MediaSingleton getInstance() {
        if(instance == null) {
            instance = new MediaSingleton();
        }
        return instance;
    }

    public void setList(List<Long> l) {
        selIDs = l;
    }

    public List<Long> getSelIDs() {
        return selIDs;
    }
}
