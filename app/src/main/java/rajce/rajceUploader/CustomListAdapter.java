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

import java.util.List;

import rajce.rajceUploader.XML.AlbumListResponse;
import rajce.rajceUploader.network.info.APIStateGetAlbumList;

public class CustomListAdapter extends ArrayAdapter<String> {

    private Activity context;
    private List<String> itemname;
    private List<String> describe;
    private List<Integer> ids;
    private List<Bitmap> covers;
    private List<Boolean> downloaded;
    private AlbumListResponse alr;
    private Handler mHandler;
    private ListActivity listActivity;
    private final int REFRESH_COUNT = 10;
    private final int defaultCover = R.drawable.ic_launcher;


    public CustomListAdapter(Activity context, List<String> itemname, List<String> describe, List<Integer> ids, List<Boolean> downloaded, List<Bitmap> covers, Handler mHandler) {
        super(context, R.layout.list_item_album, itemname);
        this.mHandler = mHandler;
        this.mHandler = new Handler();
        this.itemname = itemname;
        this.describe = describe;
        this.ids = ids;
        this.downloaded = downloaded;
        this.covers = covers;


        // TODO Auto-generated constructor stub

        this.context=context;

    }

    private static class Callback implements APIStateGetAlbumList {
        private int pos;
        private List<String> itemname;
        private List<String> describe;
        private List<Integer> ids;
        private ListActivity listActivity;
        private ArrayAdapter<String> adapter;
        private List<Bitmap> covers;
        public Callback(int pos, List<String> itemname, List<String> describe, ArrayAdapter<String> adapter, List<Bitmap> covers) {
            this.pos = pos;
            this.itemname = itemname;
            this.describe = describe;
            this.adapter = adapter;
            this.covers = covers;

        }

        @Override
        public void setAlbumList(AlbumListResponse albumList) {
            for (int i = 0; i < albumList.albums.size(); i++) {
                itemname.set(i + pos,albumList.albums.get(i).albumName);
                ids.set(i+pos,albumList.albums.get(i).id);
                describe.set(i + pos,"Fotek " + albumList.albums.get(i).photoCount + " videí "+ albumList.albums.get(i).videoCount);
                this.covers.set(i + pos, albumList.albums.get(i).coverPhoto);
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

        txtTitle.setText(this.itemname.get(position));
        if (this.covers.get(position) == null) {
            imageView.setImageResource(this.defaultCover);
        } else {
            imageView.setImageBitmap(this.covers.get(position));
        }

        extratxt.setText(this.describe.get(position));

        if (!downloaded.get(position)) {

            Callback callback = new Callback(position,itemname, describe, this, covers );
            RajceAPI.getInstance().getAlbumList(callback, position - 1, REFRESH_COUNT , mHandler );
            int bound = Math.min((REFRESH_COUNT + position), downloaded.size());
            for (int i = position; i < bound; i++) {
                downloaded.set(i, true);
            }

        }

        return rowView;
    }
}