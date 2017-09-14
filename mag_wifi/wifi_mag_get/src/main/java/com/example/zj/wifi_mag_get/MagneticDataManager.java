package com.example.zj.wifi_mag_get;

import com.example.zj.wifi_mag_get.MainActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by LY on 2017/7/20.
 */

public class MagneticDataManager {

    private SensorManager msensormanager;
    private SensorEventListener mSensorListener;
    private Sensor msensor,asensor;
    private float[] temp_m = new float[3];
    public String message,msg_total,msg;

    private float[] accelerometerValues = new float[3];//初始化加速度的三轴变量
    private float[] magnetFieldValues = new float[3];//初始化磁场传感器的三轴变量

    private volatile static MagneticDataManager magneticDataManager = null;
    public static MagneticDataManager getInstance() {
        if (magneticDataManager == null) {
            synchronized (MagneticDataManager.class) {
                if (magneticDataManager == null) {
                    magneticDataManager = new MagneticDataManager();
                }
            }
        }
        return magneticDataManager;
    }

    public void init(MainActivity mainActivity){
        msensormanager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);

        //  注册磁场传感器
        msensor = msensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorListener = new MSensorListener();
        msensormanager.registerListener(mSensorListener, msensor,
                SensorManager.SENSOR_DELAY_NORMAL);

        // 注册加速度传感器
        asensor = msensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        msensormanager.registerListener(mSensorListener,asensor,SensorManager.SENSOR_DELAY_NORMAL);
    }



    private class MSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
//            message = new String();
            message = new String();
            msg = new String();
            msg_total = new String();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magnetFieldValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accelerometerValues = event.values;
            }
            msg_total = getOrientation(accelerometerValues,magnetFieldValues);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public String getOrientation(float[] accelerometerValues, float[] magnetFieldValues){
        String message = new String();
        String msg = new String();
        String msg_total = new String();
        if (accelerometerValues !=null && magnetFieldValues != null){

            float X = magnetFieldValues[0];
            float Y = magnetFieldValues[1];
            float Z = magnetFieldValues[2];
            DecimalFormat df = new DecimalFormat("#,##0.000");
            message += df.format(X) + "  ";
            message += df.format(Y) + "  ";
            message += df.format(Z) + "\n";
//            MainActivity.magnetic.setText("地磁数据"+message + "\n");

            float[] values = new float[3];
            float[] R = new float[9];
            SensorManager.getRotationMatrix(R,null,accelerometerValues,magnetFieldValues);
            SensorManager.getOrientation(R,values);//x指向西方，y指向地磁北极，z指向地心
            //getRotationMatrix：x指向东方，y指向地磁北极，z指向天空
            //Math.toDegree将弧度转化为角度
            values[0] = (float) Math.toDegrees(values[0]);//正北转角Azimuth  z
            values[1] = (float) Math.toDegrees(values[1]);//顶尾翘角Pitch   x
            values[2] = (float) Math.toDegrees(values[2]);//左右转角Roll   y
            msg += df.format(values[0]) + "  ";
            msg += df.format(values[1]) + "  ";
            msg += df.format(values[2]) + "\n";
//            MainActivity.fangwei.setText("方位角"+msg + "\n");

            msg_total += message;
            msg_total += msg;
        }
        return msg_total;
    }
}
