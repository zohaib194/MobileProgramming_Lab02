package com.example.zohaibbutt.lab02;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class A1 extends AppCompatActivity {

    static final int GET_RESULT = 1;
    public static final String receiverIsUp = "Task_Done_Listener";
    public static final String TAG_TITLE_LIST = "TAG.TITLE.LIST";
    public static final String TAG_LINKS_LIST = "TAG.LINKS.LIST";
    public static final String TAG_DESC_LIST = "TAG.TITLE.DESC";
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
    ArrayList<String> description;
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
        this.titles = new ArrayList<>();
        this.links = new ArrayList<>();
        this.description = new ArrayList<>();

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
                String lnk = links.get(position);
                Intent intent = new Intent(getApplicationContext(), A2.class);
                intent.putExtra("LINK", lnk);
                startActivity(intent);
            }
        });
        // Clear the lists if not null
        if (titles != null && links != null && description != null) {
            titles.clear();
            links.clear();
            description.clear();
        }

        // check database
        if(db.feedInDB(requestURL)){
            titles = db.DBToString("FeedTitle");
            links = db.DBToString("FeedLink");
            description = db.DBToString("FeedDesc");
            setItemListAdapter();
        }  else {
            // if URL is not specified than A3 "Settings" activity will start.
            if (requestURL == "") {
                Intent intent = new Intent(getApplicationContext(), A3.class);
                startActivityForResult(intent, GET_RESULT);
            } else { // else url is specified and Rss feeds can be fetched
                Intent i = new Intent(this, BackgroundThread.class);
                i.putStringArrayListExtra(TAG_TITLE_LIST, titles);
                i.putStringArrayListExtra(TAG_LINKS_LIST, links);
                i.putStringArrayListExtra(TAG_DESC_LIST, description);
                i.putExtra(FREQ_VAL, freq);
                startService(i);
                Log.i("Service_running", "service is running!!");
            }

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

    public void setItemListAdapter(){
        // Creates an adapter with layout and the data.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1,  titles.subList(0, ((viewLimit > titles.size()) ? titles.size() : viewLimit))){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                Log.i("Position", ""+position);
                View view = super.getView(position, convertView, parent);
                TextView t1 = view.findViewById(android.R.id.text1);
                TextView t2 = view.findViewById(android.R.id.text2);

               if(description != null && titles != null) {
                   if (description.size() > 0 && titles.size() > 0) {
                       String subDesc = (description.get(position).length() >= 45) ? description.get(position).substring(0, 45) + "..." : description.get(position);
                       t1.setText(titles.get(position));
                       t2.setText(subDesc);
                   }
               }
                return view;
            }
        };

        // Sets the adapter to the list view.
        itemsList.setAdapter(adapter);
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
        if(titles != null && links != null && description != null) {
            this.titles.clear();
            this.links.clear();
            this.description.clear();
        }

         // check database
        if(db.feedInDB(requestURL)){
            titles = db.DBToString("FeedTitle");
            description = db.DBToString("FeedDesc");
            Log.i("feed_In_DB", "Feeds are already in db");
            setItemListAdapter();
        }// else {     // else start the service to fetch RSS

            Intent i = new Intent(this, BackgroundThread.class);
            i.putStringArrayListExtra(TAG_TITLE_LIST, titles);
            i.putStringArrayListExtra(TAG_LINKS_LIST, links);
            i.putStringArrayListExtra(TAG_DESC_LIST, description);
            i.putExtra(FREQ_VAL, freq);
            startService(i);
            Log.i("Service_running", "service is running!!");
    //    }
    }

    // Broadcast receiver that is notified when async task is done and
    public class taskDoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Log.i("Task_Done_Listener", "onReceive");
            links = arg1.getStringArrayListExtra(TAG_LINKS_LIST);
            titles = arg1.getStringArrayListExtra(TAG_TITLE_LIST);
            description = arg1.getStringArrayListExtra(TAG_DESC_LIST);
            setItemListAdapter();
        }
    }

    // Callback method for Async task
    public interface AsyncResponse {
        void processFinish(Exception s);
    }

}
