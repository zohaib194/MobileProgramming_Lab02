package com.example.zohaibbutt.lab02;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.xml.sax.Parser;

public class A3 extends AppCompatActivity {
    static final String SETTING_VAL = "my_settings";
    static final String URL_VAL = "url.value";
    static final String LIMIT_VAL = "rate.limit.value";
    static final String FREQ_VAL = "freq.value";
    SharedPreferences sharedPref;
    Button b1;
    EditText T1, T2, T3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a3);

        // Shared Preferences
        this.sharedPref = getSharedPreferences(SETTING_VAL, Context.MODE_PRIVATE);

        // Setting the previously used value to the Edit Text
        this.T1 = findViewById(R.id.T1);
        T1.setText(sharedPref.getString(URL_VAL, ""));
        this.T2 = findViewById(R.id.T2);
        T2.setText(""+sharedPref.getInt(LIMIT_VAL, 0 ));
        this.T3 = findViewById(R.id.T3);
        T3.setText(""+sharedPref.getInt(FREQ_VAL, 0));
        // Button
        this.b1 = findViewById(R.id.B1);

        b1.setOnClickListener(this::onClick);
    }

    public void onClick(View view){
        Intent intent = new Intent();

        intent.putExtra(URL_VAL, ((EditText)findViewById(R.id.T1)).getText().toString());
        intent.putExtra(LIMIT_VAL, Integer.parseInt(((EditText)findViewById(R.id.T2)).getText().toString()));
        intent.putExtra(FREQ_VAL, Integer.parseInt(((EditText)findViewById(R.id.T3)).getText().toString()));

        this.sharedPref = this.getSharedPreferences(SETTING_VAL,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(URL_VAL, ((EditText)findViewById(R.id.T1)).getText().toString());
        editor.putInt(LIMIT_VAL, Integer.parseInt(((EditText)findViewById(R.id.T2)).getText().toString()));
        editor.putInt(FREQ_VAL, Integer.parseInt(((EditText)findViewById(R.id.T3)).getText().toString()));
        editor.commit();

        setResult(RESULT_OK, intent);
        finish();
    }
}
