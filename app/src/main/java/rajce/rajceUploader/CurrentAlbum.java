package rajce.rajceUploader;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import rajce.rajceUploader.network.info.APIStateNewAlbum;


public class CurrentAlbum extends NewAlbum {

    protected Integer album_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // title - jmeno kam se nahrava
        Intent intent = getIntent();
        if (null != intent) {
            setTitle(getResources().getString(R.string.title_activity_current_album) + ": " + intent.getStringExtra("KEY_ALBUM_NAME"));
            album_id = intent.getIntExtra("KEY_ALBUM_ID",0);
        }

        submitButton.setEnabled(false);
        showProgress(true);
        percentage.setText("0%");

        if(selIDs.contains(-1L)) { // nahravame fotky
            uploadPhotos(album_id);

        }
        else if(selIDs.contains(-2L)) { // nahravame videa
            uploadVideos(album_id);
        }

    }

}
