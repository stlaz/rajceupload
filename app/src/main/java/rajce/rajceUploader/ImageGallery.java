package rajce.rajceUploader;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ImageGallery.java
 * Purpose: Image gallery activity.
 */

public class ImageGallery extends FragmentActivity implements
        ActionBar.OnNavigationListener {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    public void setLayout(int layout) {
        this.layout = layout;
    }

    // layout pro aktivitu
    private int layout = -1;
    // kurzor pro pruchod polem obrazku
    private Cursor cc = null;
    // pole pro ulozeni identifikatoru obrazku
    private static String[] mIDs = null;
    // pole pro ulozeni identifikatoru fotek
    private static String[] imgIDs = null;
    // pole pro ulozeni identifikatoru videi
    private static String[] vidIDs = null;
    // pole pro ulozeni identifikatoru nedavnych fotek
    private static String[] recIDs = null;
    // pole pro ulozeni identifikatoru vybranych fotek/videi
    private List<Long> selIDs;
    // LRU cache pro ulozeni nedavno pouzitych obrazku
    private LruCache<String, Bitmap> mMemoryCache;
    private GridView gridview = null;
    private int viewBorder;
    private int viewWidth;

    private ImageAdapter imageAdapter = new ImageAdapter(this, 0L);
    private ImageAdapter videoAdapter = new ImageAdapter(this, 1L);

    private SpinnerAdapter mSpinnerAdapter;

    /**
     * Metoda pro ziskani seznamu identifikatoru vybranych fotek.
     * @return seznam identifikatoru vybranych fotek
     */
    public List<Long> getSelIDs() {
        return selIDs;
    }

    /**
     * V onCreate se nastavi retainer fragment, cache a thread na nahravani obrazku.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        final float scale = getResources().getDisplayMetrics().density;
        viewBorder = (int)(3*scale + 0.5f);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        viewWidth = (int) (metrics.widthPixels / 3.05);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Sloupce pro vyber fotek
        String[] mProjection = { MediaStore.MediaColumns._ID,
                                 MediaStore.Images.ImageColumns.DATE_TAKEN };
        // 1/16 maxima pouzitelne pameti, magic number
        final int cacheSize = maxMemory / 16;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_gallery);
        gridview = (GridView) findViewById(R.id.gridview);

        // vytvoreni/ziskani fragmentu pro ulozeni cache a vybranych fotek
        RetainFragment retainFragment =
                findOrCreateRetainFragment(getFragmentManager());

        mMemoryCache = retainFragment.mRetainedCache;
        // pokud cache jeste neexistuje, tak ji vytvorime
        if (mMemoryCache == null) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // velikost v kB
                    return bitmap.getByteCount() / 1024;
                }
            };
            retainFragment.mRetainedCache = mMemoryCache;
        }

        selIDs = retainFragment.selIDs;
        // pokud seznam vybranych fotek jeste neexistuje, tak ho vytvorime
        if (selIDs == null) {
            selIDs = new ArrayList<Long>();
            retainFragment.selIDs = selIDs;
        }

        // Nastavení textu titulku, odstranění ikony
        //setTitle("FOTOGRAFIE ▼");
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getActionBar().setDisplayShowTitleEnabled(false);

        // ====================================

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


        final String[] dropdownValues = getResources().getStringArray(R.array.action_list_co_vybirame);

        // Specify a SpinnerAdapter to populate the dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1,
                dropdownValues);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(adapter, this);

        // ====================================

        //centerTitleText();

        setupRecIDs(mProjection);
        setupVidIDs(mProjection);
        setupImgIDs(mProjection);
        mIDs = imgIDs;
        // kdyz se nenejadou vubec zadne fotky - vypise se hlaska
        if ( mIDs == null ) {

            // nove textview uprostred stranky
            TextView tv1 = new TextView(this);
            tv1.setText(R.string.no_photos_found);
            tv1.setTextSize(20);
            tv1.setGravity(Gravity.CENTER);

            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            ll.setGravity(Gravity.CENTER);
            ll.addView(tv1);
            setContentView(ll);
            return;
        }
        cc.close();
        gridview.setAdapter(imageAdapter);

        // kliknuti na item na dane pozici
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageView image = (ImageView) v;

                if (selIDs.contains(id)) {
                    selIDs.remove(id);
                    image.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    Toast.makeText(ImageGallery.this, "Deselected: " + id, Toast.LENGTH_SHORT).show();
                }
                else {
                    selIDs.add(id);
                    image.setBackgroundColor(Color.parseColor("#CC3300"));
                    Toast.makeText(ImageGallery.this, "Selected: " + id, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupImgIDs(String[] mProjection) {
        cc = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mProjection, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
        if ( cc != null && cc.moveToFirst() ) {
            imgIDs = new String[cc.getCount()];
            // projedeme vsechny obrazky a vytahneme z nich ID
            for (int i = 0; i < cc.getCount(); i++) {
                cc.moveToPosition(i);
                imgIDs[i] = cc.getString(0);
            }
        }
        else {
            Log.v("NO_PHOTO","No photos found");
        }
    }

    private void setupVidIDs(String[] mProjection) {
        cc = this.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mProjection, null, null,
                MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC");
        if ( cc != null && cc.moveToFirst() ) {
            vidIDs = new String[cc.getCount()];
            // projedeme vsechna videa a vytahneme z nich ID
            for (int i = 0; i < cc.getCount(); i++) {
                cc.moveToPosition(i);
                vidIDs[i] = cc.getString(0);
            }
        }
        else {
            Log.v("NO_VIDEO","No videos found");
        }
    }

    private void setupRecIDs(String[] mProjection) {
        cc = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mProjection, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
        if ( cc != null && cc.moveToFirst() ) {
            // datum nejnovejsi fotky
            String recentDate = cc.getString(1);
            Log.e("Date:", recentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(recentDate));
            // prehodime na pulnoc toho sameho dne
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            recentDate = String.valueOf(cal.getTimeInMillis());
            Log.e("Date2:", recentDate);
            // vybereme vse, co se nafotilo ten samy den
            cc = this.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mProjection,

                    MediaStore.Images.ImageColumns.DATE_TAKEN + " > '" + recentDate + "'", null,
                    MediaStore.MediaColumns._ID + " DESC");
        }
        if ( cc != null && cc.moveToFirst() ) {
            recIDs = new String[cc.getCount()];
            // projedeme vsechny obrazky a vytahneme z nich ID
            for (int i = 0; i < cc.getCount(); i++) {
                cc.moveToPosition(i);
                recIDs[i] = cc.getString(0);
            }
        }
        else {
            Log.v("NO_PHOTO","No recent photos found");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.img_gallery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_next:
                openNext();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void centerTitleText() {
        int titleId = 0;    // id titulku na action baru
        titleId = getResources().getIdentifier("action_bar_title", "id", "android");

        if(titleId > 0) {
            TextView titleTextView = (TextView) findViewById(titleId);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            // Fetch layout parameters of titleTextView (LinearLayout.LayoutParams : Info from HierarchyViewer)
            LinearLayout.LayoutParams txvPars = (LinearLayout.LayoutParams) titleTextView.getLayoutParams();
            txvPars.gravity = Gravity.CENTER_HORIZONTAL;
            txvPars.width = metrics.widthPixels;
            titleTextView.setLayoutParams(txvPars);

            titleTextView.setGravity(Gravity.CENTER);
        }
    }

    /**
     * Prida bitmapu do cache.
     * @param key klic do cache
     * @param bitmap bitmapa pro vlozeni
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Ziska bitmapu z cache
     * @param key klic do cache
     * @return vraci bitmapu nebo null
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * Adapter pro zobrazovani bitmap v gridview
     */
    public class ImageAdapter extends BaseAdapter {

        private Long flag = 0L;

        private Context mContext;

        public ImageAdapter(Context c, Long flag) {
            mContext = c;
            this.flag = flag;
        }

        public int getCount() {
            return mIDs.length;
        }

        public Object getItem(int position) {
            return mIDs[position];
        }

        public long getItemId(int position) {
            return Long.parseLong(mIDs[position]);
        }

        /**
         * Vytvori ImageView s bitmapou jako prvek pro GridView.
         * @param position pozice
         * @param convertView recyklovany ImageView nebo null
         * @return vytvoreny ImageView
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            // neni recyklovany, vytvorime novy
            if (convertView == null) {
                imageView = new ImageView(mContext);

                imageView.setLayoutParams(new GridView.LayoutParams(viewWidth, viewWidth));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setCropToPadding(true);
                imageView.setPadding(viewBorder, viewBorder, viewBorder, viewBorder);
            } else {
                imageView = (ImageView) convertView;
            }
            loadBitmap(Long.parseLong(mIDs[position]), imageView);
            if (selIDs.contains(Long.parseLong(mIDs[position]))) {
                imageView.setBackgroundColor(Color.parseColor("#CC3300"));
            }
            else {
                imageView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
            return imageView;
        }

        /**
         * Nahraje bitmapu z disku/cache a vlozi ji do konkretniho ImageView.
         * @param bmpId identifikator bitmapy na disku/cache
         * @param imageView ImageView, do ktereho se vklada bitmapa
         */
        public void loadBitmap(long bmpId, ImageView imageView) {
            final String imageKey = String.valueOf(bmpId);
            final Bitmap bitmap = getBitmapFromMemCache(imageKey);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                if (cancelPotentialWork(bmpId, imageView)) {
                    Bitmap mPlaceHolder = BitmapFactory.decodeResource(getResources(), R.drawable.graypix);
                    final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                    final AsyncDrawable asyncDrawable =
                            new AsyncDrawable(getResources(), mPlaceHolder, task);
                    imageView.setImageDrawable(asyncDrawable);
                    task.execute(bmpId, flag);
                }
            }
        }

        /**
         * Zkontroluje, jestli uz pro dany ImageView nebezi nejaky nahravaci task.
         * @param data id bitmapy
         * @param imageView ImageView, do ktereho se vklada bitmapa
         */
        public boolean cancelPotentialWork(long data, ImageView imageView) {
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (bitmapWorkerTask != null) {
                final long bitmapData = bitmapWorkerTask.data;
                // pozadavek na jinou praci
                if (bitmapData == 0 || bitmapData != data) {
                    bitmapWorkerTask.cancel(true);
                } else {
                    // pozadavek na stejnou praci
                    return false;
                }
            }
            // zadny task nebyl pro dany ImageView nalezen
            return true;
        }

        /**
         * Metoda pro ziskani beziciho tasku nad ImageView.
         * @param imageView ImageView, do ktereho se vklada bitmapa
         * @return bezici task nebo null
         */
        private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
            if (imageView != null) {
                final Drawable drawable = imageView.getDrawable();
                if (drawable instanceof AsyncDrawable) {
                    final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                    return asyncDrawable.getBitmapWorkerTask();
                }
            }
            return null;
        }

        /**
         * Trida pro zobrazeni placeholderu v imageview.
         * Uklada si take weak reference (kvuli GC) na bezici task.
         */
        class AsyncDrawable extends BitmapDrawable {
            private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

            public AsyncDrawable(Resources res, Bitmap bitmap,
                                 BitmapWorkerTask bitmapWorkerTask) {
                super(res, bitmap);
                bitmapWorkerTaskReference =
                        new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
            }

            public BitmapWorkerTask getBitmapWorkerTask() {
                return bitmapWorkerTaskReference.get();
            }
        }

        /**
         * Trida pro nahravani obrazku z disku mimo UI thread
         */
        class BitmapWorkerTask extends AsyncTask<Long, Void, Bitmap> {
            private final WeakReference<ImageView> imageViewReference;
            private Long data = 0L;

            public BitmapWorkerTask(ImageView imageView) {
                // WeakReference kvuli GC
                imageViewReference = new WeakReference<ImageView>(imageView);
            }

            /**
             * Na pozadi nahraje thumbnail z disku.
             * @param params na nulte pozici identifikator obrazku
             * @return nahrana bitmapa
             */
            @Override
            protected Bitmap doInBackground(Long... params) {
                data = params[0];
                long flag = params[1];
                final Bitmap bitmap;
                // Micro thumbnail k obrazku z MediaStore
                if (flag == 0) {
                    bitmap = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
                            data, MediaStore.Images.Thumbnails.MICRO_KIND, null);
                }
                else {
                    bitmap = MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(),
                            data, MediaStore.Video.Thumbnails.MICRO_KIND, null);
                }
                // vlozime bitmapu do cache pro dalsi pouziti
                addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
                return bitmap;
            }

            /**
             * Po skonceni zkontroluje existenci ImageView a vlozi do nej bitmapu.
             * @param bitmap bitmapa pro vlozeni
             */
            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (isCancelled()) {
                    bitmap = null;
                }

                if (imageViewReference != null && bitmap != null) {
                    final ImageView imageView = imageViewReference.get();
                    final BitmapWorkerTask bitmapWorkerTask =
                            getBitmapWorkerTask(imageView);
                    if (this == bitmapWorkerTask && imageView != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }

        }
    }

    /**
     * Ziska/vytvori fragment pro ulozeni dat.
     * @param fm FragmentManager
     * @return Ziskany/vytvoreny fragment
     */
    public RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainFragment fragment = (RetainFragment) fm.findFragmentByTag("RetainerFragment");
        if (fragment == null) {
            fragment = new RetainFragment();
            fm.beginTransaction().add(fragment, "RetainerFragment").commit();
            //fm.executePendingTransactions();
        }
        return fragment;
    }

    /**
     * Otevre dalsi obrazovku (OldNewDialog)
     */
    private void openNext() {
        Intent i = new Intent(getApplicationContext(), OldNewDialog.class);
        startActivity(i);
    }

    /**
     * Fragment pro ulozeni dat, ktere by mely prezit otoceni telefonu
     */
    public static class RetainFragment extends Fragment {
        public LruCache<String, Bitmap> mRetainedCache;
        public List<Long> selIDs;
        public RetainFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // zajisti preziti fragmentu pri otoceni telefonu
            setRetainInstance(true);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                .getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        /*
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).commit();

          */
        selIDs.clear();
        if (position == 0) {
            // fotky
            mIDs = imgIDs;
            gridview.setAdapter(imageAdapter);
            //imageAdapter.notifyDataSetChanged();
        } else if (position == 1) {
            // videa
            mIDs = vidIDs;
            gridview.setAdapter(videoAdapter);
            //videoAdapter.notifyDataSetChanged();
        } else if (position == 2) {
            // nedavne fotky
            mIDs = recIDs;
            for (int i = 0; i < mIDs.length; i++)
                selIDs.add(Long.parseLong(mIDs[i]));
            gridview.setAdapter(imageAdapter);
            //imageAdapter.notifyDataSetChanged();
        }
        return true;
    }


    /** * A dummy fragment */

    public static class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "placeholder_text";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return textView;
        }
    }


}


