 package com.mahmoud.gassensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

 public class MainActivity extends AppCompatActivity {

    private Button but_ar,but_en;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        but_ar=findViewById(R.id.but_ar);
        but_en=findViewById(R.id.but_en);

        intent=new Intent(MainActivity.this,LevelsActivity.class);

        but_ar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("lang","ar");
                startActivity(intent);
            }
        });
        but_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("lang","en");
                startActivity(intent);
            }
        });
    }
}
