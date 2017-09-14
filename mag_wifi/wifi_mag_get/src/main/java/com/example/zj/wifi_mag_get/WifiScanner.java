package com.example.zj.wifi_mag_get;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinrui on 2017/7/22.
 */

public class WifiScanner implements Runnable{

    public Handler handler;
    public MagneticDataManager magneticDataManager;
    public WifiManager mwifimanager;
    public StringBuffer sb;
    private WifiInfo mWifiInfo;
    private MainActivity activity;
    private StringBuffer wifi_sb;
    private List<ScanResult> mWifiList,mWifiList1;
    private volatile static WifiScanner mWifiScanner = null;

    // 一个类需要被所有的其它类使用，但又要求这个类只能被实例化一次，是个服务类
    public static WifiScanner getInstance() {
        if (mWifiScanner == null) {
            // 同步代码块，使用并发访问的共享资源WifiScanner.class作为同步监视器
            synchronized (WifiScanner.class) {
                if (mWifiScanner == null) {
                    mWifiScanner = new WifiScanner();
                }
            }
        }
        return mWifiScanner;
    }

    // 构造器
    public WifiScanner() {
        sb = new StringBuffer();
        wifi_sb = new StringBuffer();
    }
//    public WifiScanner(MagneticDataManager magneticDataManager, Handler handler) {
//        sb = new StringBuffer();
//        wifi_sb = new StringBuffer();
//        this.magneticDataManager = magneticDataManager;
//        this.handler = handler;
//        mwifimanager = (WifiManager) activity.getApplicationContext()
//                .getSystemService(Context.WIFI_SERVICE);
//        mWifiInfo = mwifimanager.getConnectionInfo();
//    }

    public void init(MainActivity activity) {
        this.activity = activity;
        if (mwifimanager == null) {
            mwifimanager = (WifiManager) activity.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
        }
        if (mwifimanager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            Toast.makeText(activity, "正在开启wifi，请稍后...", Toast.LENGTH_SHORT)
                    .show();
            if (mwifimanager == null) {
                mwifimanager = (WifiManager) activity.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
            }
            if (!mwifimanager.isWifiEnabled()) {
                mwifimanager.setWifiEnabled(true);
            }
        }
        mWifiInfo = mwifimanager.getConnectionInfo();
    }
    public void run() {
//        handler.postDelayed(this,1000);
        handler.postDelayed(this,
                Integer.parseInt(MainActivity.period.getText().toString())); // 每隔..s执行(单位是ms)
        Log.v("!!","coming in");
        doSomething(magneticDataManager.msg_total);
//        Log.v("!!","message:"+magneticDataManager.message);
        MainActivity.magnetic.setText("地磁数据+方位数据："+ magneticDataManager.msg_total + "\n");
//        MainActivity.magnetic.setText("地磁数据："+ magneticDataManager.message + "\n");
        MainActivity.wifi.setText("wifi数据："+ wifi_sb + "\n");
    }

    private void doSomething(String message)
    {
        wifi_sb = new StringBuffer();
		mwifimanager.startScan();
        mWifiList = mwifimanager.getScanResults();

        if (mWifiList == null) {
        } else {
            mWifiList1 = new ArrayList();
            for(ScanResult result : mWifiList){
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for(ScanResult item:mWifiList1){
                    if(item.SSID.equals(result.SSID)&&item.capabilities.equals(result.capabilities)){
                        found = true;break;
                    }
                }
                if(!found){
                    mWifiList1.add(result);
                }
            }
        }

        for (int i = 0; i < mWifiList1.size(); i++) {
//            sb.append(MainActivity.number.getText().toString()).append("：");
            sb.append((mWifiList1.get(i).SSID).toString()).append(",");
            sb.append((mWifiList1.get(i).level));
            sb.append(";");
            wifi_sb.append((mWifiList1.get(i).SSID).toString()).append(",");
            wifi_sb.append((mWifiList1.get(i).level));
            wifi_sb.append(";");
            wifi_sb.append("\r\n");
        }
        sb.append("\r\n");
        sb.append(message);
        sb.append("\r\n");
        MainActivity.count++;
        Log.v("!!","count="+MainActivity.count);
        MainActivity.count_num.setText(MainActivity.count+"\n");//setText需要转换为字符串类型
//        Log.v("!!","sb="+sb);
    }
}
