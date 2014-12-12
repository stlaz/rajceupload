package rajce.rajceUploader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import rajce.rajceUploader.network.info.APIStateNewAlbum;
import rajce.rajceUploader.network.info.APIStateUpload;


public class NewAlbum extends Activity {
    private EditText album_name;
    private EditText album_descript;
    private Switch hiddenSwitch;
    private Switch passSwitch;
    private Button submitButton;
    private boolean isHidden;
    private boolean usePass;
    private List<Long> selIDs;
    private ArrayList<RajceAPI.Photo> photos = new ArrayList<RajceAPI.Photo>();
    private ArrayList<RajceAPI.Video> videos = new ArrayList<RajceAPI.Video>();

    private Cursor cc = null;
    private String[] mProjectionImages = { MediaStore.Images.Media.DATA };
    private String[] mProjectionVideos = { MediaStore.Video.Media.DATA };
    private View uploadView;
    private View formView;
    private TextView percentage;

    private RajceAPI api;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);

        formView = findViewById(R.id.new_album);
        uploadView = findViewById(R.id.upload_progress_view);
        percentage = (TextView) findViewById(R.id.percentage);

        api = RajceAPI.getInstance();
        selIDs = MediaSingleton.getInstance().getSelIDs();
        if(selIDs.contains(-1L))
            Log.e("Yayaya", "It works");

        if(selIDs.contains(-1L)) {  // budeme uploadovat fotky
            for(Long elem : selIDs) {
                if(elem == -1) continue;
                String fullPath = null;
                cc = null;
                cc = this.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mProjectionImages,
                        MediaStore.Images.ImageColumns._ID + " = '" + Long.toString(elem) + "'", null,
                        null);

                if ( cc != null && cc.moveToFirst() ) {
                    fullPath = cc.getString(0);
                    photos.add(new RajceAPI.Photo(fullPath));
                }
                Log.e("Mame uz", Boolean.toString(fullPath == null));
            }
        }
        else if(selIDs.contains(-2L)) {
            for(Long elem : selIDs) {
                if(elem == -2) continue;
                String fullPath = null;
                cc = null;
                cc = this.getContentResolver().query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mProjectionVideos,
                        MediaStore.Video.VideoColumns._ID + " = '" + Long.toString(elem) + "'", null,
                        null);
                if( cc != null && cc.moveToFirst() ) {
                    fullPath =cc.getString(0);
                    videos.add(new RajceAPI.Video(fullPath));
                }
            }
        }
//        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        album_name = (EditText) findViewById(R.id.new_name);
        album_descript = (EditText) findViewById(R.id.descript);
        hiddenSwitch = (Switch) findViewById(R.id.switch1);
        passSwitch = (Switch) findViewById(R.id.switch2);
        submitButton = (Button) findViewById(R.id.new_album_submit);

        final EditText passLogin = (EditText) findViewById(R.id.pass_name);
        final EditText passPass = (EditText) findViewById(R.id.pass_pass);

        hiddenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isHidden = b;
            }
        });
        passSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    passLogin.setVisibility(View.VISIBLE);
                    passPass.setVisibility(View.VISIBLE);
                }
                else {
                    passLogin.setVisibility(View.GONE);
                    passPass.setVisibility(View.GONE);
                }
                usePass = b;
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitButton.setEnabled(false);
                showProgress(true);
                setTitle("Nahrávání");
                percentage.setText("0%");
                api.newAlbumAdvanced(new APIStateNewAlbum() {
                    @Override
                    public void setAlbumID(int id) {

                        if(selIDs.contains(-1L)) { // nahravame fotky
                            api.uploadPhotos(id, new APIStateUpload() {
                                @Override
                                public void changeStat(int newStat) {
                                    percentage.setText(newStat + "%");
                                    if(newStat == 100) percentage.setText("Nahrávání dokončeno");
                                    // Dale nasleduje vytvoreni notifikace
                                    Context ctx = getApplicationContext();
                                    Intent notificationIntent = new Intent(ctx, NewAlbum.class);
                                    PendingIntent contentIntent = PendingIntent.getActivity(ctx,
                                            1, notificationIntent,
                                            PendingIntent.FLAG_CANCEL_CURRENT);

                                    NotificationManager nm = (NotificationManager) ctx
                                            .getSystemService(Context.NOTIFICATION_SERVICE);
                                    Resources res = ctx.getResources();
                                    Notification.Builder builder = new Notification.Builder(ctx);

                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                                            .setTicker("Nahrávám")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                            .setContentTitle("Rajče Uploader")
                                            .setContentText(newStat+"%");
                                    Notification n;
                                    if(Build.VERSION.SDK_INT > 15)
                                        n = builder.build();
                                    else n = builder.getNotification();
                                    nm.notify(88, n);
                                }

                                @Override
                                public void error(String error) {

                                }

                                @Override
                                public void finish() {
                                    Toast.makeText(getApplicationContext(), "Fotografie byly úspěšně nahrány", Toast.LENGTH_SHORT);
                                }
                            },  photos,
                                mHandler
                            );

                        }
                        else if(selIDs.contains(-2L)) { // nahravame videa
                            api.uploadVideos(id, new APIStateUpload() {
                                @Override
                                public void changeStat(int newStat) {
                                    Context ctx = getApplicationContext();
                                    Intent notificationIntent = new Intent(ctx, NewAlbum.class);
                                    PendingIntent contentIntent = PendingIntent.getActivity(ctx,
                                            1, notificationIntent,
                                            PendingIntent.FLAG_CANCEL_CURRENT);

                                    NotificationManager nm = (NotificationManager) ctx
                                            .getSystemService(Context.NOTIFICATION_SERVICE);
                                    Resources res = ctx.getResources();
                                    Notification.Builder builder = new Notification.Builder(ctx);

                                    builder.setContentIntent(contentIntent)
                                            .setSmallIcon(R.drawable.ic_launcher)
                                            .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                                            .setTicker("Nahrávám")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(true)
                                            .setContentTitle("Rajče Uploader")
                                            .setContentText(newStat+"%");
                                    Notification n;
                                    if(Build.VERSION.SDK_INT > 15)
                                        n = builder.build();
                                    else n = builder.getNotification();
                                    nm.notify(88, n);
                                }

                                @Override
                                public void error(String error) {

                                }

                                @Override
                                public void finish() {

                                }
                            },
                            videos,
                            mHandler);
                        }

                    }

                    @Override
                    public void error(String error) {

                    }

                    @Override
                    public void finish() {

                    }
                },
                   album_name.getText().toString(),
                   album_descript.getText().toString(),
                   isHidden,
                   usePass,
                   (usePass ? passLogin.getText().toString() : null),
                   (usePass ? passPass.getText().toString() : null),
                   mHandler
                   );
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_album, menu);
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

    public void onSubmitClicked(View view) {
        final int id = 1;
        final NotificationManager mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        // TODO: do titulku muzete hodit treba nazev alba
        mBuilder.setContentTitle("Nahrávání do alba ...")
                .setContentText("Probíhá nahrávání")
                .setSmallIcon(R.drawable.ic_launcher);
        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        int incr;
                        mBuilder.setProgress(0, 0, true);
                        mNotifyManager.notify(id, mBuilder.build());
                        // TODO: Misto foru hodit nahravani
                        // Do the "lengthy" operation 10 times
                        for (incr = 0; incr <= 50; incr+=5) {
                            // Sleeps the thread, simulating an operation
                            // that takes time
                            try {
                                // Sleep for 1 sec
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Log.d("NOTIF", "sleep failure");
                            }
                        }
                        // When the loop is finished, updates the notification
                        mBuilder.setContentText("Nahrávání dokončeno")
                                // Removes the progress bar
                                .setProgress(0,0,false);
                        mNotifyManager.notify(id, mBuilder.build());
                    }
                }
        // Starts the thread by calling the run() method in its Runnable
        ).start();
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        formView.setVisibility(show ? View.GONE : View.VISIBLE);
        formView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                formView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        uploadView.setVisibility(show ? View.VISIBLE : View.GONE);
        uploadView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                uploadView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
