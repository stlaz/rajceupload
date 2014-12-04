package rajce.rajceUploader;

import android.app.Activity;
import android.app.ListActivity;
import android.graphics.Bitmap;
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
    private Bitmap[] covers;
    private boolean[] downloaded;
    private AlbumListResponse alr;
    private Handler mHandler;
    private ListActivity listActivity;
    private final int REFRESH_COUNT = 10;
    private final int defaultCover = R.drawable.ic_launcher;


    public CustomListAdapter(Activity context, String[] itemname, String[] describe, boolean[] downloaded, Bitmap[] covers, Handler mHandler) {
        super(context, R.layout.list_item_album, itemname);
        this.mHandler = mHandler;
        this.mHandler = new Handler();
        this.itemname = itemname;
        this.describe = describe;
        this.downloaded = downloaded;
        this.covers = covers;


        // TODO Auto-generated constructor stub

        this.context=context;

    }

    private static class Callback implements APIStateGetAlbumList {
        private int pos;
        private String[] itemname;
        private String[] describe;
        private ListActivity listActivity;
        private ArrayAdapter<String> adapter;
        private Bitmap[] covers;
        public Callback(int pos, String[] itemname, String[] describe, ArrayAdapter<String> adapter, Bitmap[] covers) {
            this.pos = pos;
            this.itemname = itemname;
            this.describe = describe;
            this.adapter = adapter;
            this.covers = covers;

        }

        @Override
        public void setAlbumList(AlbumListResponse albumList) {
            for (int i = 0; i < albumList.albums.size(); i++) {
                itemname[i + pos] = /*Integer.toString(i+pos) +*/ albumList.albums.get(i).albumName;
                this.describe[i + pos] = "Fotek " + albumList.albums.get(i).photoCount + " videí "+ albumList.albums.get(i).videoCount;
                this.covers[i + pos] = albumList.albums.get(i).coverPhoto;
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
        if (this.covers[position] == null) {
            imageView.setImageResource(this.defaultCover);
        } else {
            imageView.setImageBitmap(this.covers[position]);
        }

        extratxt.setText(this.describe[position]);

        if (!downloaded[position]) {

            Callback callback = new Callback(position,itemname, describe, this, covers );
            RajceAPI.getInstance().getAlbumList(callback, position - 1, REFRESH_COUNT , mHandler );
            int bound = Math.min((REFRESH_COUNT + position), downloaded.length);
            for (int i = position; i < bound; i++) {
                downloaded[i] = true;
            }

        }

        return rowView;
    };
}