package rajce.rajceUploader;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import rajce.rajceUploader.XML.AlbumListResponse;
import rajce.rajceUploader.network.info.APIStateGetAlbumList;


public class OldNewDialog extends ListActivity {

    private Handler mHandler;
    private final int INIT_COUNT = 10;
    ListView listView, listView2;
    private List<Long> selIDs;
    //private Handler mHandler;

    private AlbumListResponse initAlbumsList;

    private String[] itemname;
    private String[] describe;
    private boolean[] downloaded;

    private Integer[] imgid={
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher,
            R.drawable.ic_launcher
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_new_dialog);

        selIDs = (ArrayList<Long>) getIntent().getExtras().getSerializable("selIDs");
        if (selIDs.contains(-1L))
            Log.e("LISTTAG", "WORKS!");

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));


        mHandler = new Handler();

        RajceAPI api = RajceAPI.getInstance();


        api.getAlbumList(new APIStateGetAlbumList() {
            @Override
            public void setAlbumList(AlbumListResponse albumList) {
                initAlbumsList = albumList;
                itemname = new String[initAlbumsList.totalCount + 1];
                describe = new String[initAlbumsList.totalCount + 1];
                downloaded = new boolean[initAlbumsList.totalCount + 1];
                itemname[0] = "Vytvořit nové album";
                downloaded[0] = true;
                for (int i = 1; i < (initAlbumsList.totalCount + 1); i++) {
                    if (i < (INIT_COUNT + 1)) {
                        downloaded[i] = true;
                        itemname[i] = initAlbumsList.albums.get(i - 1).albumName;
                        describe[i] = "Fotek " + initAlbumsList.albums.get(i - 1).photoCount + " videí "+ initAlbumsList.albums.get(i - 1).videoCount;
                    } else {
                        itemname[i] = "Načítám album";
                        downloaded[i] = false;
                    }


                }

                CustomListAdapter adapter=new CustomListAdapter(OldNewDialog.this, itemname, describe, downloaded, imgid, mHandler);
                OldNewDialog.this.setListAdapter(adapter);

            }

            @Override
            public void error(String error) {

            }

            @Override
            public void finish() {

            }
        }, 0 , INIT_COUNT, mHandler);




    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {

        if (position==0) {
            Intent i = new Intent(getApplicationContext(), NewAlbum.class);
            startActivity(i);
        }
        else {
            String SelectedItem= (String)getListAdapter().getItem(position);
            Toast.makeText(this, SelectedItem, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_old_new_dialog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
