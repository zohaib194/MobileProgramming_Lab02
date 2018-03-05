package com.example.zohaibbutt.lab02;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class A2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a1);



        WebView webview = new WebView(this);
        // Enable javascript
        webview.getSettings().setJavaScriptEnabled(true);
        // Get the link for intent
        String link = getIntent().getStringExtra("LINK");

        webview.setWebViewClient(new WebViewClient());

        webview.loadUrl(link);

        setContentView(webview);

    }

}
