package com.example.zohaibbutt.lab02;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import java.io.Serializable;
import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper implements Serializable{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";
    public static final String TABLE_NAME = "Feeds";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RssURL = "URL";
    public static final String COLUMN_Feed = "FeedTitle";
    public static final String COLUMN_Link = "FeedLink";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_RssURL + " TEXT, " +
                    COLUMN_Feed + " TEXT, " +
                    COLUMN_Link + " TEXT);";

    private static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }

    public void addFeed(RSSFeeds feed){

        ContentValues value = new ContentValues();

        value.put(COLUMN_RssURL, feed.getURL());
        value.put(COLUMN_Feed, feed.getTitle());
        value.put(COLUMN_Link, feed.getLink());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, value);
        db.close();

    }

    public void deleteFeed(RSSFeeds feed){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_RssURL + "=\'" + feed.getURL() + "\';");
    }

    public boolean feedInDB(String url) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";// + COLUMN_RssURL + " = " + url;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public ArrayList<String> DBToString(String columnName){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> dbString;

        dbString = new ArrayList<String>();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            if(cursor.getString(cursor.getColumnIndex(columnName))  != null){
                dbString.add(cursor.getString(cursor.getColumnIndex(columnName)));
            }
            cursor.moveToNext();
        }

        return dbString;
    }

}
