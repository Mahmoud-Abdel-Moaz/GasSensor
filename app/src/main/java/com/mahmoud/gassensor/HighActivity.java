package com.mahmoud.gassensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class HighActivity extends AppCompatActivity {

    TextView txt_high,txt_high1,txt_high2,txt_high3,txt_high4,txt_turnOff;
    Intent intent;
    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high);

        intent=getIntent();
        lang=intent.getStringExtra("lang");

        txt_high=findViewById(R.id.txt_high);
        txt_high1=findViewById(R.id.txt_high1);
        txt_high2=findViewById(R.id.txt_high2);
        txt_high3=findViewById(R.id.txt_high3);
        txt_high4=findViewById(R.id.txt_high4);
        txt_turnOff=findViewById(R.id.txt_turnOff);

        if (lang.equals("ar")){
            txt_high.setText("أرقام شركات الأنقاذ :");
            txt_high1.setText("1. صيانكو : 24030056.");
            txt_high2.setText("2. تاون جاز : 3540548.");
            txt_high3.setText("3. غاز مصر : 35406079.");
            txt_high4.setText("4. بتروتريد : 22601279.");
            txt_turnOff.setText("تم أغلاق الكهرباء");
        }else if (lang.equals("en")){
            txt_high.setText("Help Company numbers:-");
            txt_high1.setText("1. Sianco : 24030056.");
            txt_high2.setText("2. Tawn gas : 3540548.");
            txt_high3.setText("3. Egypt gas : 35406079.");
            txt_high4.setText("4. Petrotrade : 22601279.");
            txt_turnOff.setText("The Electricity Was Turned off");
        }
    }
}
