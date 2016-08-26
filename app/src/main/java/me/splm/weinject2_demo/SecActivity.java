package me.splm.weinject2_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import me.splm.annotation.WeInject;
import me.splm.gen.auto.WeSecActivity;


@WeInject
public class SecActivity extends AppCompatActivity {
    @WeInject
    public String name;
    @WeInject
    public String sex;
    private TextView showBridgeData_tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sec);
        WeSecActivity.inject(this);
        showBridgeData_tv=(TextView) findViewById(R.id.showBridgeData_tv);
        showBridgeData_tv.setText("ReName:--"+name+"---ReSex:"+sex);
    }
}
