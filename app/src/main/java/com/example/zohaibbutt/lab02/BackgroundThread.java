package com.example.zohaibbutt.lab02;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.zohaibbutt.lab02.A1.FREQ_VAL;
import static com.example.zohaibbutt.lab02.A1.TAG_LINKS_LIST;
import static com.example.zohaibbutt.lab02.A1.TAG_TITLE_LIST;


// Thread that fetch data from XML.

public class BackgroundThread extends Service {

    public static ArrayList<String> ttl;
    public static ArrayList<String> lnk;
    public int frq;
    private boolean done = false;
    public static DBHandler db;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private final AlarmReceiver alarm = new AlarmReceiver();
    public static final String ACTION_ALARM_RECEIVER = "ACTION_ALARM_RECEIVER";
    private final IntentFilter intentFilter = new IntentFilter("FETCH_RSS_FEED");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lnk = intent.getStringArrayListExtra(TAG_LINKS_LIST);
        ttl = intent.getStringArrayListExtra(TAG_TITLE_LIST);
        frq = intent.getIntExtra(FREQ_VAL, 0);

        // db = (DBHandler) intent.getSerializableExtra(TAG_DB);
        db = new DBHandler(this, null, null, 1);
        Toast.makeText(getApplicationContext(), "Service is onStartCommand!!", Toast.LENGTH_LONG).show();
        Log.i("Service_onStartCommand", "Service is onStartCommand!!");

        if (frq != 0) {
            // Alarm
            this.alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(this, AlarmReceiver.class);
            i.setAction(ACTION_ALARM_RECEIVER);

            this.alarmIntent = PendingIntent.getBroadcast(this, 0, i, 0);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * frq,
                    1000 * frq, alarmIntent);

            Log.i("Alarm_set", "Alarm set!");
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service is Destroyed!!", Toast.LENGTH_LONG).show();
        Log.i("Service_onDestroy", "Service is Destroyed!!");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}