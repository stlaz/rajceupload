package rajce.rajceUploader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rajce.rajceUploader.XML.AlbumListResponse;
import rajce.rajceUploader.network.info.APIStateGetAlbumList;


public class OldNewDialog extends ListActivity {

    private Handler mHandler;
    private final int INIT_COUNT = 10; // o kolika albech se ma na zacatku stahnout info
    private Bundle selectedMedia;
    private List<Long> selIDs;

    private AlbumListResponse initAlbumsList;

    private String[] itemname;
    private String[] describe;
    private Integer[] ids;
    private ArrayList<Boolean> downloaded;

    private Bitmap[] covers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_new_dialog);

//        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));


        mHandler = new Handler();

        RajceAPI api = RajceAPI.getInstance();


        api.getAlbumList(new APIStateGetAlbumList() {
            @Override
            public void setAlbumList(AlbumListResponse albumList) {
                initAlbumsList = albumList;
                itemname = new String[initAlbumsList.totalCount + 1];  // +1 protoze je tam jeste polozka "Vytvorit album"
                describe = new String[initAlbumsList.totalCount + 1];
                ids = new Integer[initAlbumsList.totalCount + 1];
                downloaded = new ArrayList<Boolean>();
                covers = new Bitmap[initAlbumsList.totalCount + 1];
                itemname[0] = "Vytvořit nové album";
                downloaded.add(true);
                for (int i = 1; i < (initAlbumsList.totalCount + 1); i++) {
                    if (i < (INIT_COUNT + 1)) {
                        downloaded.add(true);
                        itemname[i] = initAlbumsList.albums.get(i - 1).albumName;
                        describe[i] = "Fotek " + initAlbumsList.albums.get(i - 1).photoCount + " videí "+ initAlbumsList.albums.get(i - 1).videoCount;
                        ids[i] = initAlbumsList.albums.get(i - 1).id;
                        covers[i] = initAlbumsList.albums.get(i - 1).coverPhoto;
                    } else {
                        itemname[i] = "Načítám album";
                        ids[i] = 0;
                        downloaded.add(false);
                        covers[i] = null;
                    }


                }

                List<String> names =  Collections.synchronizedList(new ArrayList<String>(Arrays.asList(itemname)));
                List<String> describes =  Collections.synchronizedList(new ArrayList<String>(Arrays.asList(describe)));
                List<Integer> id_s =  Collections.synchronizedList(new ArrayList<Integer>(Arrays.asList(ids)));
                List<Boolean> downloadeds =  Collections.synchronizedList(new ArrayList<Boolean>(downloaded));
                List<Bitmap> cover =  Collections.synchronizedList(new ArrayList<Bitmap>(Arrays.asList(covers)));






                CustomListAdapter adapter=new CustomListAdapter(OldNewDialog.this, names, describes, id_s, downloadeds, cover, mHandler);
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
            finish();
        }
        else {

            final Integer position_ = position;
            new AlertDialog.Builder(this)
                    .setTitle("Potvrzení")
                    .setMessage("Nahrát do alba '" + (String)getListAdapter().getItem(position_) + "' ?" )
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            Intent i = new Intent(getApplicationContext(), CurrentAlbum.class);
                            i.putExtra("KEY_ALBUM_NAME",(String)getListAdapter().getItem(position_));
                            i.putExtra("KEY_ALBUM_ID",ids[position_]);
                            startActivity(i);
                            finish();
                        }})
                    .setNegativeButton("Zpět", null).show();

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
