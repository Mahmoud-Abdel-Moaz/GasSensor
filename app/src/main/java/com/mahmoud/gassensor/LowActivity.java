package com.mahmoud.gassensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class LowActivity extends AppCompatActivity {

    TextView txt_low1,txt_low2;
    String lang;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low);

        txt_low1=findViewById(R.id.txt_low1);
        txt_low2=findViewById(R.id.txt_low2);

        intent=getIntent();
        lang=intent.getStringExtra("lang");
        if (lang.equals("ar")){
            txt_low1.setText("1. أغلاق صنبور الغاز فورا.");
            txt_low2.setText("2. أفتح كل النوافذ والابواب.");
        }else if(lang.equals("en")){
            txt_low1.setText("1. turn off the gas faucet immediately.");
            txt_low2.setText("2. open all windows and doors.");
        }
    }
}
