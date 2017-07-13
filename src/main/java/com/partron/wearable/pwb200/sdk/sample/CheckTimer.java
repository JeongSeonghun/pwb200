package com.partron.wearable.pwb200.sdk.sample;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by EMGRAM on 2017-01-06.
 */

public class CheckTimer {
    private Timer timer;
    private String startTime;
    private ArrayList times;
    private ArrayList testArray;
    private int checkTimeNum;
    private int beforeCount;
    private long repeat;
    private int repeatCount;
    private TimerCallBack timerCallBack;
    private int timerMode;

    private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    public interface TimerCallBack{     //종료후 저장
        ArrayList onCheck();
        void onComplete(ArrayList testArray);
    }

    private TimerCallBack2 timerCallBack2;
    public interface TimerCallBack2{    //주기별 저장
        void onCheck(boolean addSpaceChk);
    }

    public CheckTimer(ArrayList times, TimerCallBack timerCallBack){
        this.times= times;
        this.timerCallBack= timerCallBack;
        repeat= 1000;
        repeatCount= 5*60;
        testArray= new ArrayList();
        timerMode=2;
    }

    public CheckTimer(ArrayList times, long repeat, int repeatCount, TimerCallBack timerCallBack){
        this.times= times;
        this.timerCallBack= timerCallBack;
        this.repeat= repeat;
        this.repeatCount= repeatCount;
        testArray= new ArrayList();
        timerMode=1;
    }
    public CheckTimer(TimerCallBack timerCallBack){
        this.timerCallBack= timerCallBack;
        timerMode=3;
    }
    public CheckTimer(TimerCallBack2 timerCallBack2){
        this.timerCallBack2= timerCallBack2;
        timerMode=4;
    }

    boolean isAddSpace=false;
    int taskNum=1;

    public void startTimer(){
        timer= new Timer();
        TimerTask timerTask;
        TimerTask timerTask2;

        switch (timerMode){
            case 1: //개수 확인용
                startTime= getCurrentTime();
                Log.d("pwb test", "Timer Start : "+startTime);
                timerTask= new TimerTask() {
                    @Override
                    public void run() {
                        checkCount();
                    }
                };

                timer.schedule(timerTask, repeat, repeat);
                break;
            case 2: //일정 시간별 확인용
                timerTask= new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("pwb test", "timer first");
                        timerCallBack.onCheck();
                    }
                };

                timerTask2= new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("pwb test", "timer second");
                        timerCallBack.onCheck();
                    }
                };

                TimerTask timerTask3= new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("pwb test", "timer third");
                        timerCallBack.onCheck();
                        saveLog();
                    }
                };
                Timer timer2, timer3;
                timer2= new Timer();
                timer3= new Timer();
                timer.schedule(timerTask,60*1000);
                timer2.schedule(timerTask2,10*60*1000);
                timer3.schedule(timerTask3,3*60*60*1000);
                break;
            case 3: //일정 주기별 확인
                timerTask= new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("pwb test", "timer first");
                        timerCallBack.onCheck();
                    }
                };

                timerTask2= new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("pwb test", "timer second");
                        timerCallBack.onCheck();

                    }
                };

                timer.schedule(timerTask, 10*60*1000);
                timer.schedule(timerTask2, 20*60*1000, 20*60*1000);
                //timer.schedule(timerTask2, 1000, 1000);
                break;
            case 4:
                timerTask= new TimerTask() {
                    @Override
                    public void run() {
                        if(isAddSpace){
                            timerCallBack2.onCheck(isAddSpace);
                            isAddSpace=false;
                        }else{
                            timerCallBack2.onCheck(isAddSpace);
                        }
                    }
                };
                timerTask2= new TimerTask() {
                    @Override
                    public void run() {
                        isAddSpace=true;
                    }
                };
                timer.schedule(timerTask, 1000, 1000);
                timer.schedule(timerTask2, 60*1000, 60*1000);
                break;
        }


    }

    public boolean addSpaceCheck(){
        return isAddSpace;
    }

    private void checkCount(){
        times=timerCallBack.onCheck();
        Log.d("pwb test", "checkCount");
        HashMap hashMap= new HashMap();
        int timesSize= times.size();

        if(times.isEmpty()){
            hashMap.put("time", getCurrentTime());
            hashMap.put("count", 0);
        }else{
            hashMap.put("time", getCurrentTime());
            hashMap.put("count", timesSize-beforeCount);
            beforeCount=timesSize;
        }
        testArray.add(hashMap);

        if(checkTimeNum>=repeatCount-1){
            HashMap lastHashMap= new HashMap();
            lastHashMap.put("time", startTime);
            lastHashMap.put("count", beforeCount);
            testArray.add(0, lastHashMap);
            saveLog();
        }

        checkTimeNum++;
    }

    private void saveLog(){
        timer.cancel();
        timerCallBack.onComplete(testArray);
    }

    private String getCurrentTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        return mFormat.format(date);
    }

    private String getStringTime(long time){
        return mFormat.format(new Date(time));
    }

}
