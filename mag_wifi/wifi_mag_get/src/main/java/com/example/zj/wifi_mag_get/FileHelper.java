package com.example.zj.wifi_mag_get;

/**
 * Created by LY on 2017/7/20.
 */

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.content.Context;
import android.os.Environment;

public class FileHelper {
    private Context context;
    /** SD卡是否存在 **/
    private boolean hasSD = false;
    /** SD卡的路径 **/
    private String SDPATH;
    /** 当前程序包的路径 **/
    private String FILESPATH;
    private String FILESAVE;

    public FileHelper(Context context) {
        this.context = context;
        hasSD = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        SDPATH = Environment.getExternalStorageDirectory().getPath();
        FILESPATH = this.context.getFilesDir().getPath();
        FILESAVE = SDPATH+"/dataGet";
    }

    /**
     * 在SD卡上创建文件
     *
     * @throws IOException
     */
    public File createSDFile(String fileName) throws IOException {
        File file_dir = null;
        try {
            file_dir = new File(FILESAVE);
            if (!file_dir.exists()) {
                file_dir.mkdir();
            }
        }catch(Exception e){
        }
        File file = new File(FILESAVE + "//" + fileName);
        if(file.exists())
        {
            file.delete();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * 删除SD卡上的文件
     *
     * @param fileName
     */
    public boolean deleteSDFile(String fileName) {
        File file = new File(FILESAVE + "//" + fileName);
        if (file == null || !file.exists() || file.isDirectory())
            return false;
        return file.delete();
    }

    /**
     * 写入内容到SD卡中的txt文本中 str为内容
     */
    public void writeSDFile(String str, String fileName) {
        try {
            File f = new File(FILESAVE, fileName);
            FileOutputStream out = new FileOutputStream(f, false);
            out.write(str.getBytes());
        } catch (Exception e) {
        }
    }

    /**
     * 读取SD卡中文本文件
     *
     * @param fileName
     * @return
     */
    public String readSDFile(String fileName) {
        StringBuffer sb = new StringBuffer();
        File file = new File(FILESAVE + "//" + fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            int c;
            while ((c = fis.read()) != -1) {
                sb.append((char) c);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public String getFILESPATH() {
        return FILESPATH;
    }

    public String getSDPATH() {
        return FILESAVE;
    }

    public boolean hasSD() {
        return hasSD;
    }
}

