package com.example.zj.wifi_mag_get;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private Button start;
    private Button stop;
    private Button save;
    private Button data_clear;

    public static EditText name;//文件名
    public static EditText period;// 采样间隔
    public static int count = 0;
    public static TextView count_num;// 计数器
    public static TextView magnetic;// 显示地磁和方向
    public static TextView wifi;// wifi列表

//    private WifiManager mwifimanager;
//    private WifiInfo mWifiInfo;
//    private List<ScanResult> mWifiList;

    private FileHelper helper;
    private MagneticDataManager magneticDataManager;
    private WifiScanner mWifiScanner;
    private StringBuffer sb = new StringBuffer();
    private int TIME = 1000;
    private String NAME = null;
    private Handler handler = new Handler();
    private int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = (EditText) findViewById(R.id.name);// 文件名
        period = (EditText) findViewById(R.id.period);// 采集间隔
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        save = (Button) findViewById(R.id.save);
        data_clear = findViewById(R.id.data_clear);

        start.setOnClickListener(start_listener);
        stop.setOnClickListener(stop_listener);
        save.setOnClickListener(save_listener);
        data_clear.setOnClickListener(clear_listener);

        helper = new FileHelper(getApplicationContext());

        count_num = (TextView) findViewById(R.id.count_num);// 计数器显示
        magnetic = findViewById(R.id.magnetic);// 地磁+方向显示
        wifi = (TextView) findViewById(R.id.wifi);// wifi显示

        // 单例模式getInstance()创建类的实例，一个类只有一个实例
        magneticDataManager = MagneticDataManager.getInstance();
        magneticDataManager.init(this);
        mWifiScanner = WifiScanner.getInstance();
        mWifiScanner.init(this);
        mWifiScanner.magneticDataManager = magneticDataManager;//将参数传入WifiScanner
        mWifiScanner.handler = handler;

        // 检查APP是否已经拥有权限。请求权限后，系统会弹出请求权限的Dialog
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    private OnClickListener start_listener = new OnClickListener() {
        public void onClick(View v) {
            start.setEnabled(false);
            stop.setEnabled(true);
            mWifiScanner.mwifimanager.startScan();
            count = 0;
            handler.postDelayed(mWifiScanner,
                    Integer.parseInt(period.getText().toString())); // 每隔..s执行(单位是ms)
        }
    };
    private OnClickListener stop_listener = new OnClickListener() {
        public void onClick(View v) {
            start.setEnabled(true);
            stop.setEnabled(false);
            handler.removeCallbacks(mWifiScanner);
        }
    };
    private OnClickListener save_listener = new OnClickListener() {
        public void onClick(View v) {
            String NAME = name.getText().toString() + ".txt";
            if (helper.hasSD()) {
                try {
                    helper.createSDFile(NAME).getAbsolutePath();
                    helper.writeSDFile(mWifiScanner.sb.toString(), NAME);
                    mWifiScanner.sb = new StringBuffer();
                    Toast.makeText(MainActivity.this, "已保存！", Toast.LENGTH_LONG)
                            .show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(MainActivity.this, "手机没有SD卡！", Toast.LENGTH_LONG)
                        .show();

        }
    };
    private OnClickListener clear_listener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            String NAME = name.getText().toString() + ".txt";
            if (helper.hasSD()) {
                try {
                    mWifiScanner.sb = new StringBuffer();
                    helper.createSDFile(NAME).getAbsolutePath();
                    helper.writeSDFile(mWifiScanner.sb.toString(), NAME);
                    helper.deleteSDFile(NAME);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(MainActivity.this, "手机没有SD卡！", Toast.LENGTH_LONG)
                        .show();
        }
    };
//    DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
//        public void onClick(DialogInterface dialog, int which) {
//            switch (which) {
//                case AlertDialog.BUTTON_POSITIVE:// "ok"开启wifi
//                {
//                    mwifimanager.setWifiEnabled(true);
//                    dialog.cancel();
//                    break;
//                }
//                case AlertDialog.BUTTON_NEGATIVE:// "Cancel"取消
//                    dialog.cancel();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}

