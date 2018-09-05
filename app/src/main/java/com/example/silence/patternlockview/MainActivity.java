package com.example.silence.patternlockview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.patternlocklibrary.PatternLockView;

public class MainActivity extends AppCompatActivity {


    private PatternLockView lockView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lockView=findViewById(R.id.lockView);
        lockView.setLockListener(new PatternLockView.PatternLockListener() {
            @Override
            public void getPswSuccess(String password) {
                Toast.makeText(MainActivity.this,"正确密码是："+password,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void getPswError(String password) {
                Toast.makeText(MainActivity.this,"错误密码是："+password,Toast.LENGTH_SHORT).show();
            }
        });



    }





}
