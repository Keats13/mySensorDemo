package com.example.zj.app_save;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener{
    /**
     * Called when the activity is first created.
     */

    private Button mWriteButton, mStopButton,clear_count;// 按钮
    private boolean doWrite = false;// 初始化存储标志位为false
    private SensorManager sm;
    private TextView magnetic,fangxiang,graText,editTextRate,count_num;
    private EditText editText;
    private String filename = "finger";
    int editRate,count_num_dig = 0;
    private Context context = this;
    private int count = 0;// 用于控制刷新频率

    private int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    File f;
    FileOutputStream out;

    private float[] accelerometerValues;//初始化加速度的三轴变量
    private float[] magnetFieldValues;//初始化磁场传感器的三轴变量
    private float[] gravityValues;//初始化重力传感器的三轴变量
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 传感器值
        magnetic = (TextView) findViewById(R.id.magnetic);
        fangxiang = findViewById(R.id.fangxiang);
        graText = findViewById(R.id.gra);

        editText = (EditText) findViewById(R.id.file_name_edit);// 文件名
        editTextRate = (TextView) findViewById(R.id.magneticRate_edit);// 控制传感器显示频率
        count_num = findViewById(R.id.count_num);// 计数器显示

        //注册按钮
        mWriteButton = (Button) findViewById(R.id.Button_Write);
        mStopButton = (Button) findViewById(R.id.Button_Stop);
        clear_count = findViewById(R.id.clear_count);


        //创建一个SensorManager来获取系统的传感器服务
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        /*
         * 最常用的一个方法 注册事件
         * 参数1 ：SensorEventListener监听器
         * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
         * 参数3 ：模式 可选数据变化的刷新频率
         * */
        //  注册磁场传感器
        sm.registerListener(this,
                sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        //根据手机硬件而变化
        //SENSOR_DELAY_FASTEST最灵敏
        //SENSOR_DELAY_GAME较快
        //SENSOR_DELAY_NORMAL比较慢
        //SENSOR_DELAY_UI最慢
        // 注册加速度传感器
        sm.registerListener(this,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        // 注册重力传感器
        sm.registerListener(this,sm.getDefaultSensor(Sensor.TYPE_GRAVITY),SensorManager.SENSOR_DELAY_NORMAL);


        // 检查APP是否已经拥有权限。请求权限后，系统会弹出请求权限的Dialog
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }

        // 开始测试并打开存储开关
        mWriteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!doWrite) {
                    filename = editText.getText().toString();
                    doWrite = true;//打开存储标志位
                    Toast.makeText(context, "Build " + filename, Toast.LENGTH_SHORT).show();
                    try {
                        //将存储路径定为SD卡下MAGNETIC文件夹
                        String filePath = Environment.getExternalStorageDirectory().getPath()+"/MAGNETIC";
                        File file_dir = null;
                        //创建此抽象路径filePath名称指定的目录
                        try {
                            file_dir = new File(filePath);
                            if (!file_dir.exists()) {
                                file_dir.mkdir();//不能创建多级目录
                            }
                        }catch(Exception e){
                        }
                        f = new File(file_dir, filename + ".txt");//创建文件
                        out = new FileOutputStream(f, false);// 创建字节输出流
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // 结束存储，保存文件
        mStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(doWrite) {
                    try {
                        doWrite = false;
//                        vibrator.vibrate(500);
                        count_num_dig = 0;
                        count_num.setText(count_num_dig+"\n");// 计数器
                        Toast.makeText(context, "Shutdown", Toast.LENGTH_SHORT).show();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    //  new DrawThread().start();  //线程启动
                }
            }
        });

        // 清零计数器
        clear_count.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                count_num_dig = 0;
                count_num.setText(count_num_dig+"\n");
            }
        });
    }

    //用户选择允许或拒绝后，会回调onRequestPermissionsResult方法, 该方法类似于onActivityResult
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }

    //根据requestCode和grantResults(授权结果)做相应的后续处理
    private void doNext(int requestCode, int[] grantResults) {
        if(requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE){
            if(grantResults[0] == getPackageManager().PERMISSION_GRANTED){
                //Permission Granted
            }else{
                //Permission Denied
            }
        }
    }

    public void onPause() {
        /*
         * 很关键的部分：注意，说明文档中提到，即使activity不可见的时候，感应器依然会继续的工作，测试的时候可以发现，没有正常的刷新频率
         * 也会非常高，所以一定要在onPause方法中关闭触发器，否则讲耗费用户大量电量，很不负责。
         * */
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        magneticRate.setText("onAccuracyChanged被触发");
    }

    // 最重要的函数
    public void onSensorChanged(SensorEvent event) {
        String msg_total = new String();
        editRate = Integer.parseInt(editTextRate.getText().toString());// 用于控制传感器数据的显示刷新频率
        if (count++ == editRate) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magnetFieldValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accelerometerValues = event.values;
            }
            if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
                gravityValues = event.values;
            }
            // 通过加速度计与磁力计获取到方向
            msg_total = getOrientation(accelerometerValues,magnetFieldValues,gravityValues);

            if (doWrite) {
                try {
                    out.write(msg_total.getBytes());// 将str字符串里面包含的字符输出到指定输出流中
                    Log.v("!!",msg_total);
                    count_num_dig = count_num_dig+1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            count = 0;
            count_num.setText(count_num_dig+"\n");
        }
    }

    public String getOrientation(float[] accelerometerValues, float[] magnetFieldValues,float[] gravityValues){
        String message = new String();
        String msg = new String();
        String gra = new String();
        String msg_total = new String();
        if (accelerometerValues !=null && magnetFieldValues != null && gravityValues != null){

            float X = magnetFieldValues[0];
            float Y = magnetFieldValues[1];
            float Z = magnetFieldValues[2];
            DecimalFormat df = new DecimalFormat("#,##0.000");// 存储数据格式
            message += df.format(X) + "  ";
            message += df.format(Y) + "  ";
            message += df.format(Z) + "\n";
            magnetic.setText("地磁数据"+message + "\n");

            float[] values = new float[3];// 倾斜角度参数
            float[] R = new float[9];// 方向角度参数
            SensorManager.getRotationMatrix(R,null,accelerometerValues,magnetFieldValues);// 根据rotation matrix计算设备的方位
            SensorManager.getOrientation(R,values);
            //Math.toDegree将弧度转化为角度
            values[0] = (float) Math.toDegrees(values[0]);//正北转角Azimuth  z
            values[1] = (float) Math.toDegrees(values[1]);//顶尾翘角Pitch   x
            values[2] = (float) Math.toDegrees(values[2]);//左右转角Roll   y
            msg += df.format(values[0]) + "  ";
            msg += df.format(values[1]) + "  ";
            msg += df.format(values[2]) + "\n";
            fangxiang.setText("方位角"+msg + "\n");

            float graX = gravityValues[0];
            float graY = gravityValues[1];
            float graZ = gravityValues[2];
            gra += df.format(graX) + "  ";
            gra += df.format(graY) + "  ";
            gra += df.format(graZ) + "\n";
            graText.setText("重力"+gra + "\n");

            msg_total += message;// 地磁
            msg_total += msg;// 方向
            msg_total += gra;// 重力

        }
        return msg_total;
    }
}

