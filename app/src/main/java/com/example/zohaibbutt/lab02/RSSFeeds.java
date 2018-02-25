package com.example.zohaibbutt.lab02;

/**
 * Created by Zohaib Butt on 23/02/2018.
 */

public class RSSFeeds {
    private int id;
    private String URL;
    private String title;
    private String link;


    public RSSFeeds(String URL, String title, String link) {
        this.URL = URL;
        this.title = title;
        this.link = link;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public String getURL() {
        return URL;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }
}
