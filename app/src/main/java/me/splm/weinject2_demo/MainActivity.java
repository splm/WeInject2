package me.splm.weinject2_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import me.splm.gen.auto.WeSecActivity;


public class MainActivity extends AppCompatActivity {
    private Button go_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        go_btn=(Button) findViewById(R.id.go_btn);
        go_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeSecActivity.getIntance().setName("john").setSex("male").start(MainActivity.this);
            }
        });
    }
}
