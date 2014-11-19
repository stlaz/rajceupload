package rajce.rajceUploader;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.MenuItem;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import java.lang.ref.WeakReference;

/**
 * ImageGallery.java
 * Purpose: Image gallery activity.
 *
 * @author Petr Kubat, xkubat11@stud.fit.vutbr.cz
 */

public class ImageGallery extends Activity {

    // kurzor pro pruchod polem obrazku
    private Cursor cc = null;
    // pole pro ulozeni identifikatoru obrazku
    private static String[] mIDs = null;
    // LRU cache pro ulozeni nedavno pouzitych obrazku
    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * V onCreate se nastavi retainer fragment, cache a thread na nahravani obrazku.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        String[] mProjection = { MediaStore.MediaColumns._ID };
        // 1/16 maxima pouzitelne pameti, magic number
        final int cacheSize = maxMemory / 16;

        super.onCreate(savedInstanceState);

        // vytvoreni/ziskani fragmentu pro ulozeni cache
        RetainFragment retainFragment =
                findOrCreateRetainFragment(getFragmentManager());
        mMemoryCache = retainFragment.mRetainedCache;
        // pokud cache jeste neexistuje, tak ji vztvorime
        if (mMemoryCache == null) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.getByteCount() / 1024;
                }
            };
            retainFragment.mRetainedCache = mMemoryCache;
        }

        setContentView(R.layout.activity_img_gallery);

        // TODO: Tohle pak asi strcit do nejake jine activity
        // Vytahneme z MediaStore vsechny fotky na SD karte
        cc = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mProjection, null, null,
                MediaStore.MediaColumns._ID + " DESC");
        // Thread pro ziskani ID z db fotek do vlastniho pole
        Thread t = new Thread() {
            public void run() {
                try {
                    cc.moveToFirst();
                    mIDs = new String[cc.getCount()];
                    // projedeme vsechny obrazky a vytahneme z nich ID
                    for (int i = 0; i < cc.getCount(); i++) {
                        cc.moveToPosition(i);
                        mIDs[i] = cc.getString(0);
                    }

                } catch (Exception e) {
                    }
            }
        };
        t.start();

        GridView gridview = (GridView) findViewById(R.id.gridview);
        // pockame na dobehnuti threadu pro nahravani
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gridview.setAdapter(new ImageAdapter(this));

        // kliknuti na item na dane pozici
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(ImageGallery.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mIDs.length;
        }

        public Object getItem(int position) {
            return mIDs[position];
        }

        public long getItemId(int position) {
            return position;
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
                imageView.setLayoutParams(new GridView.LayoutParams(-1, -1));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(1, 1, 1, 1);
            } else {
                imageView = (ImageView) convertView;
            }
            loadBitmap(Long.parseLong(mIDs[position]), imageView);
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
                    Bitmap mPlaceHolder = BitmapFactory.decodeResource(getResources(), R.drawable.sample_0);
                    final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                    final AsyncDrawable asyncDrawable =
                            new AsyncDrawable(getResources(), mPlaceHolder, task);
                    imageView.setImageDrawable(asyncDrawable);
                    task.execute(bmpId);
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
                // Micro thumbnail k obrazku z MediaStore
                final Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
                        data, MediaStore.Images.Thumbnails.MICRO_KIND, null);
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
     * Fragment pro ulozeni dat, ktere by mely prezit otoceni telefonu
     */
    public class RetainFragment extends Fragment {
        public LruCache<String, Bitmap> mRetainedCache;
        public RetainFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // zajisti preziti fragmentu pri otoceni telefonu
            setRetainInstance(true);
        }
    }
}
