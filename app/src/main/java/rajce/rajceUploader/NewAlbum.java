package rajce.rajceUploader;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;


public class NewAlbum extends Activity {
    private Switch hiddenSwitch;
    private Switch passSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        hiddenSwitch = (Switch) findViewById(R.id.switch1);
        passSwitch = (Switch) findViewById(R.id.switch2);

        final EditText passLogin = (EditText) findViewById(R.id.pass_name);
        final EditText passPass = (EditText) findViewById(R.id.pass_pass);

        hiddenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b);
                else;
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

}
