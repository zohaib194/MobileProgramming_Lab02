package com.example.zohaibbutt.lab02;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class A1 extends AppCompatActivity {

    static final int GET_RESULT = 1;
    public static final String receiverIsUp = "Task_Done_Listener";
    public static final String TAG_TITLE_LIST = "TAG.TITLE.LIST";
    public static final String TAG_LINKS_LIST = "TAG.LINKS.LIST";
    public static final String FREQ_VAL = "freq.value";
    public static final String TAG_DB = "TAG.DB";
    public static String requestURL;
    Integer viewLimit;
    Integer freq;
    ImageButton imageButton;
    SharedPreferences settings;
    ListView itemsList;
    ArrayList<String> titles;
    ArrayList<String> links;
    static final String SETTING_VAL = "my_settings";
    static final String URL_VAL = "url.value";
    static final String LIMIT_VAL = "rate.limit.value";
    private DBHandler db;
    private final IntentFilter intentFilter = new IntentFilter(receiverIsUp);
    private final taskDoneReceiver receiver = new taskDoneReceiver();

    //AlarmReceiver alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1);

        // DB init
        this.db = new DBHandler(this, null, null, 1);

        // allocating memory for titles and links
        this.titles = new ArrayList<String>();
        this.links = new ArrayList<String>();

        // Shared Preferences
        this.settings = getSharedPreferences(SETTING_VAL, Context.MODE_PRIVATE);
        // Get the shared preferences
        requestURL = settings.getString(URL_VAL, "");
        this.viewLimit = settings.getInt(LIMIT_VAL, 0 );
        this.freq = settings.getInt(FREQ_VAL, 0);


        // item onclick method
        this.itemsList = findViewById(R.id.LV);
        itemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
              //  Uri uri = Uri.parse(links.get(position));
                Intent intent = new Intent(getApplicationContext(), A2.class);
                startActivity(intent);
            }
        });

        // check database
        if(db.feedInDB(requestURL)){
            titles = db.DBToString("FeedTitle");
            //setItemListAdapter();
        }  else {

            // TODO check if shared pref are set
            // Update the RSS feed
            Intent i = new Intent(this, BackgroundThread.class);
            i.putStringArrayListExtra(TAG_TITLE_LIST, titles);
            i.putStringArrayListExtra(TAG_LINKS_LIST, links);
            i.putExtra(FREQ_VAL, freq);
           // i.putExtra(TAG_DB, db);
            startService(i);
            Log.i("Service_running", "service is running!!");

            // Toast.makeText(A1.this, feedInfo.getLink(), Toast.LENGTH_LONG).show();
           // setItemListAdapter();
        }

        registerReceiver(this.receiver, intentFilter);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(this.receiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        /*
        unregisterReceiver(this.receiver);
        */
        super.onDestroy();
    }

    public void setItemListAdapter(){
        // Creates an adapter with layout and the data.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);

        // Sets the adapter to the list view.
        itemsList.setAdapter(adapter);
        Toast.makeText(this, "Adapter is set and List view is updated", Toast.LENGTH_LONG).show();

    }

    // Go to A3(Settings where user can specify details about RSS)
    public void goToA3(View v){
        this.imageButton = findViewById(R.id.Setting);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getApplicationContext(), A3.class);
                startActivityForResult(intent, GET_RESULT);
            }
        });
    }

    @Override
    // Get the result from A3
    public void onActivityResult(int req, int res, Intent data){
        if(req != GET_RESULT || res != RESULT_OK){
            return;
        }

        // Update the variables with new data.
        requestURL = data.getStringExtra(URL_VAL);
        viewLimit = data.getIntExtra(LIMIT_VAL, 0);
        freq = data.getIntExtra(FREQ_VAL, 0);

        // empty array lists
        if(titles != null && links != null) {
            this.titles.clear();
            this.links.clear();
        }
         // check database
       /* if(db.feedInDB(requestURL)){
            titles = db.DBToString("FeedTitle");
            Log.i("feed_In_DB", "Feeds are already in db");
            setItemListAdapter();
        } else { */
            Intent i = new Intent(this, BackgroundThread.class);
            i.putStringArrayListExtra(TAG_TITLE_LIST, titles);
            i.putStringArrayListExtra(TAG_LINKS_LIST, links);
            i.putExtra(FREQ_VAL, freq);
            startService(i);
            Log.i("Service_running", "service is running!!");


     //   }
        //registerReceiver(this.receiver, intentFilter);

    }

    public class taskDoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Toast.makeText(arg0, "Task Done", Toast.LENGTH_SHORT).show();
            Log.i("Task_Done_Listener", "onReceive" + titles.size());
            links = arg1.getStringArrayListExtra(TAG_LINKS_LIST);
            titles = arg1.getStringArrayListExtra(TAG_TITLE_LIST);
            setItemListAdapter();
        }
    }

    public interface AsyncResponse {
        void processFinish(Exception s);
    }

}
