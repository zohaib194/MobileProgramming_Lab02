package com.example.zohaibbutt.lab02;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class A1 extends AppCompatActivity {
    static final int GET_RESULT = 1;
    String requestURL;
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
    static final String FREQ_VAL = "freq.value";
    DBHandler db;
    RSSFeeds feedInfo;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1);

        // DB init
        db = new DBHandler(this, null, null, 1);

        // Alarm
        this.alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        this.alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to start at 8:30 a.m.
        //Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.set(Calendar.HOUR_OF_DAY, 8);
        //calendar.set(Calendar.MINUTE, 30);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                1000 * 60 * 2, this.alarmIntent);
        Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();


        // allocating memory for titles and links
        this.titles = new ArrayList<String>();
        this.links = new ArrayList<String>();

        // Shared Preferences
        this.settings = getSharedPreferences(SETTING_VAL, Context.MODE_PRIVATE);
        // Get the shared preferences
        this.requestURL = settings.getString(URL_VAL, "");
        this.viewLimit = settings.getInt(LIMIT_VAL, 0 );
        this.freq = settings.getInt(FREQ_VAL, 0);


        // item onclick method
        this.itemsList = (ListView) findViewById(R.id.LV);
        itemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                Uri uri = Uri.parse(links.get(position));
                Intent intent = new Intent(getApplicationContext(), A2.class);
                startActivity(intent);
            }
        });

        // check database
        if(db.feedInDB(requestURL)){
            titles = db.DBToString("FeedTitle");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(A1.this, android.R.layout.simple_list_item_1, titles);
            itemsList.setAdapter(adapter);
        } else {
            // Get the RSS feed
            new ProcessInBackground().execute();
            // Toast.makeText(A1.this, feedInfo.getLink(), Toast.LENGTH_LONG).show();
        }

    }

    // Go to A3(Settings where user can specify details about RSS)
    public void goToA3(View v){
        this.imageButton = (ImageButton) findViewById(R.id.Setting);
        imageButton.setOnClickListener(new View.OnClickListener() {
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
        if(req != GET_RESULT){
            return;
        }
        if(res != RESULT_OK){
            return;
        }
        // Update the variables with new data.
        requestURL = data.getStringExtra(URL_VAL);
        viewLimit = data.getIntExtra(LIMIT_VAL, 0);
        freq = data.getIntExtra(FREQ_VAL, 0);

        // empty array lists
        this.titles.clear();
        this.links.clear();

         // check database
        if(db.feedInDB(requestURL)){
            titles = db.DBToString("FeedTitle");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(A1.this, android.R.layout.simple_list_item_1, titles);
            itemsList.setAdapter(adapter);
        } else {
            // Update the current feed view.
            new ProcessInBackground().execute();
        }
    }

    public InputStream getInputStream(URL url){
        try{
            return url.openConnection().getInputStream();
        } catch(IOException e){
            return null;
        }
    }

    // Thread that fetch data from XML.
    public class ProcessInBackground extends AsyncTask<Integer, Integer, Exception>{
        Exception exception;
        Integer countItemTags = 0;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try{
                URL url= new URL(requestURL);

                XmlPullParserFactory fact = XmlPullParserFactory.newInstance();

                fact.setNamespaceAware(false);

                XmlPullParser xpp = fact.newPullParser();

                xpp.setInput(getInputStream(url), "UTF_8");

                int eventType = xpp.getEventType();
                boolean inItem = false;
                boolean gotTitle = false, gotLink = false;
                // Checks if the its not the end of XML document
                while(eventType != XmlPullParser.END_DOCUMENT){
                    // Checks if its the start tag
                    if(eventType == XmlPullParser.START_TAG){
                        // Checks if its in the item tag
                        if(xpp.getName().equalsIgnoreCase("item")){
                            inItem = true;
                            countItemTags++;
                        }
                        // Checks if its in the tittle tag
                        else if(xpp.getName().equalsIgnoreCase("title")){
                            if(inItem) { // and if are already in the item tag
                                titles.add(xpp.nextText());
                                gotTitle = true;
                            }
                        }
                        // Checks if its in the link tag
                        else if(xpp.getName().equalsIgnoreCase("link")) {
                            if (inItem) { // and if we are already in the item tag
                                links.add(xpp.nextText());
                                gotLink = true;
                            }
                        }
                        // checks if the XML end tag og item tag
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        inItem = false;
                    }
                    eventType = xpp.next();
                    if(gotTitle && gotLink){
                        feedInfo = new RSSFeeds(requestURL ,titles.get(titles.size() - 1), links.get(links.size() - 1 ));
                        db.addFeed(feedInfo);
                        gotLink = false;
                        gotTitle = false;
                    }
                }
            } catch (MalformedURLException e){
                exception = e;
            } catch (IOException e){
                exception = e;
            } catch (XmlPullParserException e){
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s)
        {
            super.onPostExecute(s);

            // Creates an adapter with layout and the data.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(A1.this, android.R.layout.simple_list_item_1, titles);

            // Sets the adapter to the list view.
            itemsList.setAdapter(adapter);
        }
    }
}
