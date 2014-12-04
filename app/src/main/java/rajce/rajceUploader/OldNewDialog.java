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
    ListView listView, listView2;
    private List<Long> selIDs;
    //private Handler mHandler;

    //private AlbumListResponse existingAlbumsList;

    private String[] itemname= {
            "Vytvořit nové album",
            "Chorvatsko 2014",
            "Chorvatsko 2013",
            "Chorvatsko 2012"
    };

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


        //mHandler = new Handler();

        //RajceAPI api = RajceAPI.getInstance();

        /*
        api.getAlbumList(new APIStateGetAlbumList() {
            @Override
            public void setAlbumList(AlbumListResponse albumList) {
                existingAlbumsList = albumList;
            }

            @Override
            public void error(String error) {

            }

            @Override
            public void finish() {
                itemname = new String[existingAlbumsList.totalCount];
                for (int i=0;i<existingAlbumsList.totalCount;i++)
                {
                    itemname[i] = existingAlbumsList.albums.get(i).albumName;
                }
            }
        },mHandler);

*/

        CustomListAdapter adapter=new CustomListAdapter(this, itemname, imgid);
        setListAdapter(adapter);
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
