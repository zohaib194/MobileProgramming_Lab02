package com.example.zohaibbutt.lab02;

/**
 * Created by Zohaib Butt on 23/02/2018.
 */

public class RSSFeeds {
    private int id;
    private String URL;
    private String title;
    private String link;
    private String desc;


    public RSSFeeds(String URL, String title, String link, String desc) {
        this.URL = URL;
        this.title = title;
        this.desc = desc;
        this.link = link;
        this.desc = desc;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDesc(String desc){
        this.desc = desc;
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

    public String getDesc()
    {
        return desc;
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
