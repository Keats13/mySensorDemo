package com.example.zj.wifi_get;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText name;
    private EditText period;
    private EditText number;
    private Button start;
    private Button stop;
    private Button save;
    private FileHelper helper;
    String NAME;
    private WifiManager mwifimanager;
    private WifiInfo mWifiInfo;
    private StringBuilder sb;
    private List<ScanResult> mWifiList;
    private int TIME = 1000;
    private int count;
    private TextView count_num;

    /************************* 扫描wifi是否开启 ****************************************************************/
    public void Wifiadmin(Context context) {
        mwifimanager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mwifimanager.getConnectionInfo();
        if (!mwifimanager.isWifiEnabled()) {
            new AlertDialog.Builder(this).setTitle("提示：")
                    .setMessage("您的wifi未开启，请确认开启！")
                    .setPositiveButton("OK", listener2)
                    .setNegativeButton("Cancel", listener2).show();
        }
    }

    /************************* 扫描wifi是否开启 ****************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name = (EditText) findViewById(R.id.name);
        period = (EditText) findViewById(R.id.period);
        number = (EditText) findViewById(R.id.number);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        save = (Button) findViewById(R.id.save);
        helper = new FileHelper(getApplicationContext());
        this.Wifiadmin(getBaseContext());
        sb = new StringBuilder();

        start.setOnClickListener(start_listener);
        stop.setOnClickListener(stop_listener);
        save.setOnClickListener(save_listener);

        count_num = (TextView) findViewById(R.id.count_num);

        mwifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
//                handler.postDelayed(this, TIME);
                handler.postDelayed(this,Integer.parseInt(period.getText().toString()));

                mwifimanager.startScan();
                mWifiList = mwifimanager.getScanResults();
                for (int i = 0; i < mWifiList.size(); i++) {
                    sb.append(number.getText().toString()).append("：");
                    sb.append((mWifiList.get(i).SSID).toString()).append(",");
                    sb.append((mWifiList.get(i).level));
                    sb.append(";");
                }
                sb.append("\r\n");
                count++;
                Log.v("!","count="+count);
                count_num.setText(count+"\n");//setText需要转换为字符串类型
            } catch (Exception e) {
                e.printStackTrace();// 123456789
            }

        }
    };

    private View.OnClickListener start_listener = new OnClickListener() {
        public void onClick(View v) {
            start.setEnabled(false);
            stop.setEnabled(true);
            mwifimanager.startScan();
            count = 0;
            handler.postDelayed(runnable,
                    Integer.parseInt(period.getText().toString())); // 每隔..s执行(单位是ms)
        }
    };
    private View.OnClickListener stop_listener = new OnClickListener() {
        public void onClick(View v) {
            start.setEnabled(true);
            stop.setEnabled(false);
            handler.removeCallbacks(runnable);
        }
    };
    private View.OnClickListener save_listener = new OnClickListener() {
        public void onClick(View v) {
            String NAME = name.getText().toString() + ".txt";
            if (helper.hasSD()) {
                try {
                    helper.createSDFile(NAME).getAbsolutePath();
                    helper.writeSDFile(sb.toString(), NAME);
                    sb = new StringBuilder();
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
    DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "ok"开启wifi
                {
                    mwifimanager.setWifiEnabled(true);
                    dialog.cancel();
                    break;
                }
                case AlertDialog.BUTTON_NEGATIVE:// "Cancel"取消
                    dialog.cancel();
                    break;
                default:
                    break;
            }
        }
    };

}
