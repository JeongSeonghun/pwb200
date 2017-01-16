package com.partron.wearable.pwb200.sdk.sample;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by jinkook on 2016. 9. 28..
 */

public class FileUtils {
    private String TAG = "";

    public String STRSAVEPATH;
    public String SAVEFILEPATH;
    public File dir;
    public File file;
    public String logStr;
    private int saveCase;

    ArrayList list= new ArrayList();
    public FileUtils(ArrayList list, String tag){
        this.list= list;
        TAG = tag;
        saveCase=0;

        STRSAVEPATH = Environment.getExternalStorageDirectory() + "/pwb200/";
        SAVEFILEPATH = todayDate(saveCase);
    }

    public FileUtils(ArrayList list){
        this.list= list;
        TAG = "";
        saveCase=1;

        STRSAVEPATH = Environment.getExternalStorageDirectory() + "/pwb200/";
        SAVEFILEPATH = todayDate(saveCase);
    }

    public FileUtils(String logStr, String tag){
        this.logStr= logStr;
        TAG = tag;
        saveCase=2;

        STRSAVEPATH = Environment.getExternalStorageDirectory() + "/pwb200/";
        SAVEFILEPATH = todayDate(saveCase);
    }

    private StringBuilder finalResultContents;
    public void saveFile(){
        //폴더 생성
        dir = makeDirectory(STRSAVEPATH);
        //파일 생성
        //SAVEFILEPATH = todayDate(1);
        file = makeFile(dir, (STRSAVEPATH + SAVEFILEPATH));

        //절대 경로
        Log.i(TAG, "" + getAbsolutePath(dir));
        Log.i(TAG, "" + getAbsolutePath(file));

        switch (saveCase){
            case 0:
                //파일 쓰기, 리눅스 : \n, 윈도우 : \r\n
                String content="all count : "+(list.size()-1);
                finalResultContents = new StringBuilder(content);

                finalResultContents.append("\r\nstart time: "+(((HashMap)list.get(0)).get("time"))
                        +", all count: "+ (((HashMap)list.get(0)).get("count")));
                for(int idx=1; idx<list.size(); idx++){
                    HashMap hashMap= (HashMap)list.get(idx);
                    finalResultContents.append("\r\nidx: "+idx+"time: "+hashMap.get("time")+", count: "+hashMap.get("count"));
                }
                finalResultContents.append("\r\n");
                writeFile(file, String.valueOf(finalResultContents));
                break;
            case 2:
                logStr.replace("\n", "\r\n");
                writeFile(file, logStr);
                break;
            case 1:
                content="all count : "+(list.size()-1);
                finalResultContents = new StringBuilder(content);

                finalResultContents.append("\r\nstart time: "+(((HashMap)list.get(0)).get("time"))
                        +", battery level: "+ (((HashMap)list.get(0)).get("bat_lv")));
                for(int idx=1; idx<list.size(); idx++){
                    HashMap hashMap= (HashMap)list.get(idx);
                    finalResultContents.append("\r\nidx: "+idx+"time: "+hashMap.get("time")+", battery lv: "+hashMap.get("bat_lv"));
                }
                finalResultContents.append("\r\n");
                writeFile(file, String.valueOf(finalResultContents));
                break;
        }
    }

    /**
     * 디렉토리 생성
     *
     * @return dir
     */
    private File makeDirectory(String dir_path) {
        File dir = new File(dir_path);
        if (!dir.exists()) {
            dir.mkdirs();
            Log.i(TAG, "!dir.exists");
        } else {
            Log.i(TAG, "dir.exists");
        }
        return dir;
    }

    /**
     * 파일 생성
     *
     * @param dir
     * @return file
     */
    private File makeFile(File dir, String file_path) {
        File file = null;
        boolean isSuccess = false;
        if (dir.isDirectory()) {
            file = new File(file_path);
            if (file != null && !file.exists()) {
                Log.i(TAG, "!file.exists");
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.i(TAG, "파일생성 여부 = " + isSuccess);
                }
            } else {
                Log.i(TAG, "file.exists");
            }
        }
        return file;
    }

    /**
     * (dir/file) 절대 경로 얻어오기
     *
     * @param file
     * @return String
     */
    private String getAbsolutePath(File file) {
        return "" + file.getAbsolutePath();
    }

    /*
        오늘날자 리턴
     */
    public String todayDate(int caseNum) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        sdf.setCalendar(Calendar.getInstance(Locale.getDefault()));
        String dateTime="";
        switch (caseNum){
            case 1:
                dateTime = sdf.format(new Date()) + "_test_pwb.txt";
                break;
            case 2:
                dateTime = sdf.format(new Date()) + "_test_pwb_log.csv";
                break;
        }


        return dateTime;
    }

    /**
     * 파일에 내용 쓰기
     *
     * @param file
     * @param file_content
     * @return
     */

    private boolean writeFile(File file, String file_content) {
        boolean result;
        FileOutputStream fos;
        if (file != null && file.exists() && file_content != null) {
            try {
                fos = new FileOutputStream(file);
                try {
                    Writer out = new OutputStreamWriter(fos, "UTF-8");
                    out.write(file_content);
                    out.flush();
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss:SSS");
    private String getStringTime(long time) {
        Date date = new Date(time);
        return mFormat.format(date);
    }
}
