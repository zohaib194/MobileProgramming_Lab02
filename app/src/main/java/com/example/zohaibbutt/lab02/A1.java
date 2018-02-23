package com.example.zohaibbutt.lab02;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1);
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
        Toast.makeText(A1.this, requestURL, Toast.LENGTH_LONG).show();

        // check database

        // empty array lists
        this.titles.clear();
        this.links.clear();

        new ProcessInBackground().execute();
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
        // Update the current feed view.
        new ProcessInBackground().execute();
    }

    public InputStream getInputStream(URL url){
        try{
            return url.openConnection().getInputStream();
        } catch(IOException e){
            return null;
        }
    }

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
                            }
                        }
                        // Checks if its in the link tag
                        else if(xpp.getName().equalsIgnoreCase("link")){
                            if(inItem) { // and if we are already in the item tag
                                links.add(xpp.nextText());
                            }
                        }
                        // checks if the XML end tag og item tag
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                        inItem = false;
                    }

                    eventType = xpp.next();
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

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(A1.this, android.R.layout.simple_list_item_1, titles);

            adapter.notifyDataSetChanged();
            itemsList.setAdapter(adapter);
        }
    }
}
