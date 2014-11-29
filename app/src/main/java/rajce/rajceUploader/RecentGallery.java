package rajce.rajceUploader;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class RecentGallery extends ImageGallery {

    //@Override
    public void onCreate(Bundle savedInstanceState) {
        super.setRecentFlg(true);
        super.setLayout(R.layout.activity_recent_gallery);
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_recent_gallery);
    }
}
