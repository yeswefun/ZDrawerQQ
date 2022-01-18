package com.z.zdrawerqq;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private int cnt;

    public void handleClick(View view) {
        Toast.makeText(this, "click: " + ++cnt, Toast.LENGTH_SHORT).show();
    }
}