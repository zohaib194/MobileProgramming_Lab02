package com.example.zohaibbutt.lab02;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import static com.example.zohaibbutt.lab02.A1.FREQ_VAL;
import static com.example.zohaibbutt.lab02.A1.LIMIT_VAL;
import static com.example.zohaibbutt.lab02.A1.SETTING_VAL;
import static com.example.zohaibbutt.lab02.A1.TAG_DESC_LIST;
import static com.example.zohaibbutt.lab02.A1.TAG_LINKS_LIST;
import static com.example.zohaibbutt.lab02.A1.TAG_TITLE_LIST;
import static com.example.zohaibbutt.lab02.A1.URL_VAL;


// Service that send broadcast to alarm receiver at the specified interval.
public class BackgroundThread extends Service {
    public static ArrayList<String> ttl;
    public static ArrayList<String> lnk;
    public static ArrayList<String> dsc;
    public int frq;
    public static DBHandler db;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    public static final String ACTION_ALARM_RECEIVER = "ACTION_ALARM_RECEIVER";
    private SharedPreferences settings;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get all the data from intent
      /*  lnk = intent.getStringArrayListExtra(TAG_LINKS_LIST);
        ttl = intent.getStringArrayListExtra(TAG_TITLE_LIST);
        dsc = intent.getStringArrayListExtra(TAG_DESC_LIST);
        frq = intent.getIntExtra(FREQ_VAL, 0);*/

        // Shared Preferences
        this.settings = getSharedPreferences(SETTING_VAL, Context.MODE_PRIVATE);
        // Get the shared preferences
        this.frq = settings.getInt(FREQ_VAL, 0);


        Log.i("Service_onStartCommand", "Service is onStartCommand!!");

        if (frq != 0) {
            // Alarm setting
            this.alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(this, AlarmReceiver.class);
            i.setAction(ACTION_ALARM_RECEIVER);

            this.alarmIntent = PendingIntent.getBroadcast(this, 0, i, 0);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 500,
                    1000 * 60 * frq, alarmIntent);

            Log.i("Alarm_set", "Alarm set!");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("Service_onDestroy", "Service is Destroyed!!");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}