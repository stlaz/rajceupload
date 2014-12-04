package rajce.rajceUploader;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import rajce.rajceUploader.XML.AlbumListResponse;
import rajce.rajceUploader.network.info.APIStateGetAlbumList;

public class CustomListAdapter extends ArrayAdapter<String> {

    private Activity context;
    private String[] itemname;
    private String[] describe;
    private boolean[] downloaded;
    private Integer[] imgid;
    private int init_count;
    private AlbumListResponse alr;
    private Handler mHandler;
    private ListActivity listActivity;
    private final int REFRESH_COUNT = 10;


    public CustomListAdapter(Activity context, String[] itemname, String[] describe, boolean[] downloaded, Integer[] imgid, Handler mHandler) {
        super(context, R.layout.list_item_album, itemname);
        this.mHandler = mHandler;
        this.mHandler = new Handler();
        this.itemname = itemname;
        this.describe = describe;
        this.downloaded = downloaded;
        this.imgid = imgid;


        // TODO Auto-generated constructor stub

        this.context=context;

    }

    private static class Callback implements APIStateGetAlbumList {
        private int pos;
        private String[] itemname;
        private String[] describe;
        private ListActivity listActivity;
        private ArrayAdapter<String> adapter;
        public Callback(int pos, String[] itemname, String[] describe, ArrayAdapter<String> adapter) {
            this.pos = pos;
            this.itemname = itemname;
            this.describe = describe;
            this.adapter = adapter;

        }

        @Override
        public void setAlbumList(AlbumListResponse albumList) {
            for (int i = 0; i < albumList.albums.size(); i++) {
                itemname[i + pos] = /*Integer.toString(i+pos) +*/ albumList.albums.get(i).albumName;
                this.describe[i + pos] = "Fotek " + albumList.albums.get(i).photoCount + " videí "+ albumList.albums.get(i).videoCount;
            }


            adapter.notifyDataSetChanged();


        }

        @Override
        public void error(String error) {

        }

        @Override
        public void finish() {

        }


    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_item_album, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item_album_name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.item_album_desc);

        txtTitle.setText(this.itemname[position]);
        imageView.setImageResource(imgid[0]);
        extratxt.setText(this.describe[position]);

        if (!downloaded[position]) {

            Callback callback = new Callback(position,itemname, describe, this );
            RajceAPI.getInstance().getAlbumList(callback, position - 1, REFRESH_COUNT , mHandler );
            int bound = Math.min((REFRESH_COUNT + position), downloaded.length);
            for (int i = position; i < bound; i++) {
                downloaded[i] = true;
            }

        }

        return rowView;
    };
}