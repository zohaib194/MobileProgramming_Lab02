package com.example.zohaibbutt.lab02;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {

        // Set the alarm here.
        Toast.makeText(arg0.getApplicationContext(), "I'm running", Toast.LENGTH_LONG).show();

        new A1().new ProcessInBackground().execute();
    }

}