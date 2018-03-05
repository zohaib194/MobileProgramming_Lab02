package com.example.zohaibbutt.lab02;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.zohaibbutt.lab02.A1.AsyncResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.example.zohaibbutt.lab02.A1.TAG_DESC_LIST;
import static com.example.zohaibbutt.lab02.A1.TAG_LINKS_LIST;
import static com.example.zohaibbutt.lab02.A1.TAG_TITLE_LIST;
import static com.example.zohaibbutt.lab02.A1.requestURL;
import static com.example.zohaibbutt.lab02.BackgroundThread.ACTION_ALARM_RECEIVER;
import static com.example.zohaibbutt.lab02.BackgroundThread.db;
import static com.example.zohaibbutt.lab02.BackgroundThread.dsc;
import static com.example.zohaibbutt.lab02.BackgroundThread.lnk;
import static com.example.zohaibbutt.lab02.BackgroundThread.ttl;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent arg1) {

        if (arg1 != null) {
            if (ACTION_ALARM_RECEIVER.equals(arg1.getAction())) {
                Log.i("Alarm_Receiver", "in OnReceive!!");

                // empty array lists
                if (ttl != null && lnk != null && dsc != null) {
                    cleanDataBase();
                    ttl.clear();
                    lnk.clear();
                    dsc.clear();
                }

                // Set the alarm here.
                ProcessInBackground task = new ProcessInBackground(new AsyncResponse() {
                    @Override
                    // callback method so that A1 know that async task is done and can now update listview
                    public void processFinish(Exception s) {
                        if (s == null) {
                            Log.i("CALLBACK_ASYNC_EXCEPTION", "no exception");
                            Intent i = new Intent();
                            i.putStringArrayListExtra(TAG_TITLE_LIST, ttl);
                            i.putStringArrayListExtra(TAG_LINKS_LIST, lnk);
                            i.putStringArrayListExtra(TAG_DESC_LIST, dsc);
                            i.setAction("Task_Done_Listener");
                            arg0.sendBroadcast(i);
                        } else {
                            Log.e("CALLBACK_ASYNC_EXCEPTION", s.toString());
                        }
                    }
                });
                task.execute(ttl, lnk, dsc);
            }
        }
    }
    // clean the database for new rss feeds due to duplicate feeds
    public void cleanDataBase() {
        ttl = db.DBToString("FeedTitle");
        lnk = db.DBToString("FeedLink");
        dsc = db.DBToString("FeedDesc");
        if (ttl.size() > 0) {
            db.deleteFeed(new RSSFeeds(requestURL,
                    ttl.get(ttl.size() - 1),
                    lnk.get(lnk.size() - 1),
                    dsc.get(dsc.size() - 1)));
        }
        Log.i("SIZES_AFTER_DB_CLEAR",
                "titles:" + ttl.size() +
                        ", links:" + lnk.size() +
                        ", Description: "+ dsc.size());

    }
    // Process that fetch RSS feed from a given link
    class ProcessInBackground extends AsyncTask<ArrayList<String>, Void, Exception> {
        private Exception exception;
        AsyncResponse delegate = null;
        private InputStream getInputStream(URL url) {
            try {
                return url.openConnection().getInputStream();
            } catch (IOException e) {
                return null;
            }
        }

        public ProcessInBackground(AsyncResponse resp) {
            delegate = resp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @SafeVarargs
        @Override
        protected final Exception doInBackground(ArrayList<String>... lists) {
            try {
                URL url = new URL(requestURL);

                XmlPullParserFactory fact = XmlPullParserFactory.newInstance();

                fact.setNamespaceAware(false);

                XmlPullParser xpp = fact.newPullParser();

                xpp.setInput(getInputStream(url), "UTF_8");

                int eventType = xpp.getEventType();
                boolean inItem = false;
                boolean atom = true;
                boolean gotTitle = false, gotLink = false, gotDesc = false;
                // Checks if the its not the end of XML document
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    // Checks if its the start tag
                    if (eventType == XmlPullParser.START_TAG) {
                        // Checks if its in the item tag
                        if (xpp.getName().equalsIgnoreCase(((atom) ? "entry" : "item"))) {
                            inItem = true;
                        }
                        // Checks if its in the tittle tag
                        else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (inItem) { // and if are already in the item tag
                                lists[0].add(xpp.nextText());
                                gotTitle = true;
                            }
                        }
                        // Checks if its in the link tag
                        else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (inItem) { // and if we are already in the item tag
                                lists[1].add(((atom) ? xpp.getAttributeValue("", "href") : xpp.nextText()));
                                gotLink = true;
                            }
                        }
                        // Checks if its in the description tag
                        else if (xpp.getName().equalsIgnoreCase(((atom) ? "summary" : "description"))) {
                            if (inItem) { // and if we are already in the item tag
                                lists[2].add(xpp.nextText());
                                gotDesc = true;
                            }
                        }
                        else if(xpp.getName().equalsIgnoreCase("rss")){
                            atom = false;
                        }
                        // checks if the XML end tag og item tag
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        inItem = false;
                    }
                    eventType = xpp.next();
                    // if got all then save them in db
                    if (gotTitle && gotLink && gotDesc) {
                        RSSFeeds feedInfo = new RSSFeeds(requestURL, lists[0].get(lists[0].size() - 1), lists[1].get(lists[1].size() - 1), lists[2].get(lists[2].size() - 1));

                        db.addFeed(feedInfo);
                        gotLink = false;
                        gotTitle = false;
                        gotDesc = false;
                    }
                }
            } catch (MalformedURLException e) {
                exception = e;
                Log.e("MALFORMED_URL_EXCEPTION", e.toString());
            } catch (IOException e) {
                exception = e;
                Log.e("IO_EXCEPTION", e.toString());
            } catch (XmlPullParserException e) {
                exception = e;
                Log.e("XML_PARSER_EXCEPTION", e.toString());
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);
            delegate.processFinish(s);
        }
    }
}