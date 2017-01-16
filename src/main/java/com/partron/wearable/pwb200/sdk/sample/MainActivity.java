package com.partron.wearable.pwb200.sdk.sample;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.partron.wearable.pwb200.sdk.core.BandResultCode;
import com.partron.wearable.pwb200.sdk.core.ConnectionState;
import com.partron.wearable.pwb200.sdk.core.ExerciseModeState;
import com.partron.wearable.pwb200.sdk.core.PWB_200_ClientManager;
import com.partron.wearable.pwb200.sdk.core.UserProfile;
import com.partron.wearable.pwb200.sdk.core.interfaces.BandConnectStateCallback;
import com.partron.wearable.pwb200.sdk.core.interfaces.BandExerciseListener;
import com.partron.wearable.pwb200.sdk.core.interfaces.OnCompleteListener;
import com.partron.wearable.pwb200.sdk.core.interfaces.PWB_200_Client;
import com.partron.wearable.pwb200.sdk.core.item.ExerciseGoalItem;
import com.partron.wearable.pwb200.sdk.core.item.ExerciseInfoItem;
import com.partron.wearable.pwb200.sdk.core.item.SleepSyncItem;
import com.partron.wearable.pwb200.sdk.core.item.UrbanInfoItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = "Sample_SDK";
    public static final int REQEUST_BLUETOOTH_DEVICE_SCAN = 0;

    public static PWB_200_Client mClient;
    private TextView mLogTextView;
    private IntentFilter mFilter;
    private ScrollView mLogScrollView;
    private Menu mMenu;

    private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss:SSS");


    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    private BandConnectStateCallback mBandConnectStateCallback = new BandConnectStateCallback() {
        @Override
        public void onBandConnectState(int state, ConnectionState connectionState) {
            Log.d(TAG, "state : " + state + ", onBandConnectState : " + connectionState.toString());
            String text = null;
            switch (connectionState){
                case CONNECTED:
                    text = "CONNECTED";
                    break;
                case CONNECTING:
                    text = "CONNECTING";
                    break;
                case DISCONNECTED:
                    text = "DISCONNECTED";
                    break;
            }

            final String finalText = text;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mMenu.getItem(0).setTitle(finalText);
                    Toast.makeText(getApplicationContext(), finalText, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mLogTextView = (TextView)findViewById(R.id.log_tv);
        mLogScrollView = (ScrollView)findViewById(R.id.log_scroll);

        mLogScrollView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    mLogScrollView.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mLogScrollView.smoothScrollBy(0, 800);
                        }
                    }, 100);
                }
            }
        });

        checkPermission();
        Button exerciseBtn = (Button)findViewById(R.id.exercise_btn);
        Button exerciseStopBtn = (Button)findViewById(R.id.exercise_st_btn);
        exerciseStopBtn.setOnClickListener(this);
        exerciseBtn.setOnClickListener(this);

        init();

        mFilter = new IntentFilter();
        mFilter.addAction("com.partron.wearable.pwb200.sdk.action_ble_send_data_test");
        mFilter.addAction("com.partron.wearable.pwb200.sdk.action_ble_receive_data_test");

        registerReceiver(mReceiver, mFilter);
    }

    private void init() {
        /**
         * SDK Client 를 이용하여 Api를 사용할 수 있다.
         */
        UserProfile item = new UserProfile();
        item.setAge(31);
        item.setHeight(170);
        item.setWeight(70);
        item.setGender(0);

        PWB_200_ClientManager clientManager = PWB_200_ClientManager.getInstance();
        mClient = clientManager.create(getApplicationContext(),item);

        Log.d(TAG, "ConnectionState : " + mClient.getConnectionState());
        Log.d(TAG, "connected : " + mClient.isBandConnected());
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            if (permissions.size() > 0) {
                String[] array = new String[permissions.size()];
                permissions.toArray(array);
                requestPermissions(array, 0x0001);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.exercise_btn:
                exerciseStart();
                break;
            case R.id.exercise_st_btn:
                exerciseStop();
                break;
        }
    }

    boolean isFirst= true;
    ArrayList times = new ArrayList();  //전체 기록
    long currentTime;
    long startTime;
    ArrayList batterys = new ArrayList();

    private void exerciseStart(){
        startTime= System.currentTimeMillis();
        //운동 모드 시작 시간
        batteryCheck();
        if(mClient!=null){
            Log.d("pwb test", "exercise Start()");
            //example
            ExerciseGoalItem item = new ExerciseGoalItem();
            item.setMode(ExerciseModeState.WALKING);
            item.setGoalStep(10000);
            item.setGoalDistance(10);
            item.setGoalTime(60);
            item.setGoalAltitude(500);
            //base pressure
            float base_prssure = 1013; //example
            mClient.getExerciseMode().startExercise(item, base_prssure, new OnCompleteListener() {
                @Override
                public void onResult(int result, Object item) {
                    //Handle BandResultCode, Oject : null
                    if (result == BandResultCode.SUCCESS) { //Your code...
                        Log.d("pwb test", "exercise Mode Start");

                    }else {

                    }
                }
            });
            exerciseInfo();
        }
    }

    /*ch.h.nam 2017.01.09 {{*/
    private void batteryCheck() {
        Log.d("pwb-200", "call batteryCheck()");
        if (mClient != null) {
            mClient.getNotification().batteryInfoReq(new OnCompleteListener() {
                @Override
                public void onResult(int i, Object o) {
                    HashMap hashMap= new HashMap();
                    hashMap.put("time", getCurrentTime());
                    hashMap.put("bat_lv", o);
                    batterys.add(hashMap);
                    Log.d("battery Check :", "value i :" + String.valueOf(i) + "value o : " + String.valueOf(o));
                    if(isFinish){
                        isFinish=false;
                        new FileUtils(batterys).saveFile();
                    }
                }
            });

        }

    }

    private void exerciseInfo(){
        Log.d("pwb test", "exerciseInfoStart");
        //register
        if (mClient != null) {
            mClient.getExerciseMode().registerBandExerciseListener(mBandExerciseListener);
        }
        //unRegister
        /*
        if (mClient != null) {
            mClient.getExerciseMode().unRegisterBandExerciseListener();
        }
        */


    }

    boolean isFinish= false;
    StringBuilder stringBuilder= new StringBuilder();
    // BandExerciseListener Callback 운동 중 정보 확인
    private BandExerciseListener mBandExerciseListener = new BandExerciseListener() {
        @Override
        public void exerciseDBInfo(int i, int i1, int i2) {
            Log.d("pwb test", "exerciseDBInfo: "+i+"/"+i1+"/"+i2);
        }

        @Override
        public ExerciseGoalItem[] startExerciseFromBand() {
            Log.d("pwb test", "startExerciseFromBand");
            //example
            ExerciseGoalItem[] item = new ExerciseGoalItem[1];
            item[0].setMode(ExerciseModeState.WALKING);
            item[0].setGoalStep(10000);
            item[0].setGoalDistance(10);
            item[0].setGoalTime(60);
            item[0].setGoalAltitude(500);
            return item;
        }

        @Override
        public void stopExerciseFromBand(ExerciseInfoItem exerciseInfoItem) {
            //your code...
            Log.d("pwb test", "stopExerciseFromBand");
        }

        @Override
        public void exerciseInfo(ExerciseInfoItem exerciseInfoItem) {
            //your code...
            Log.d("pwb test", "exerciseInfo");
        }

        @Override
        public void exerciseRealTimeInfo(int step, int hrm, int altitude) {
            //your code...
            if(isFirst){
                isFirst=false;

                /*
                final CheckTimer checkTimer= new CheckTimer(times, 1000, 30, new CheckTimer.TimerCallBack() {
                    @Override
                    public void onComplete(ArrayList testArray) {
                        Log.d("pwb test", "saveLog()");
                        new FileUtils(testArray, "").saveFile();
                    }

                    @Override
                    public ArrayList onCheck() {
                        return times;
                    }
                });
                */
                CheckTimer checkTimer= new CheckTimer(batterys, new CheckTimer.TimerCallBack() {
                    @Override
                    public ArrayList onCheck() {
                        batteryCheck();
                        return null;
                    }

                    @Override
                    public void onComplete(ArrayList testArray) {
                        isFinish= true;
                    }
                });
                checkTimer.startTimer();
                batteryCheck();
            }

            currentTime= System.currentTimeMillis();
            times.add(currentTime);

            Log.d("pwb test", getStringTime(currentTime)
                    +", exerciseRealTimeInfo ppg : "+hrm);
            stringBuilder.append("time : "+getStringTime(currentTime)
                    +", hrm : "+hrm+"\n");
            mLogTextView.post(new Runnable() {
                @Override
                public void run() {
                    mLogTextView.setText(stringBuilder);
                }
            });



        }

        @Override
        public void exercisePPGInfo(int hrm) {
            //your code...
            Log.d("pwb test", "exercisePPGInfo ppg : "+hrm);
        }

        @Override
        public void exerciseBaroInfo(int altitude) {
            //your code...
            Log.d("pwb test", "exerciseBaroInfo");
        }
    };

    private void exerciseStop(){
        if(mClient!=null){
            mClient.getExerciseMode().stopExercise(new OnCompleteListener() {
                @Override
                public void onResult(int result, Object o) {
                    if(result==BandResultCode.SUCCESS){
                        Log.d("pwb test", "exercise Stop()");
                    }else{

                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 밴드 연결 상태 리스너 등록
         */
        mClient.registerBandConnectStateCallback(mBandConnectStateCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if(mClient != null) {
            /**
             * 밴드 연결 상태 리스너 해제
             */
            mClient.unRegisterBandConnectStateCallback();
        }

        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.main_activity_actions, menu);

        mMenu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_connect:
                connectBT();
                return true;
            case R.id.action_disconnect:
                disconnectBT();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void connectBT() {
        Log.d(TAG, "connectBT" );
        Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivityForResult(intent, REQEUST_BLUETOOTH_DEVICE_SCAN);
    }

    public void disconnectBT() {
        Log.d(TAG, "disconnectBT");
        if (mClient != null) mClient.bandDisconnect();
    }

    /**
     * Api 사용 예시
     */
    public void urbanInfoBtn(View v) {
        if (mClient != null) {
            mClient.getUrbanMode().getUrbanInfo(new OnCompleteListener() {
                @Override
                public void onResult(int result, Object item) {
                    if(result == BandResultCode.SUCCESS) {
                        UrbanInfoItem infoItem = (UrbanInfoItem) item;
                        int totalstep = infoItem.getStep();
                        float distance = infoItem.getDistance();
                        int calories = infoItem.getCalories();
                        Log.d(TAG, "totalstep : " + totalstep + ",distance : " + distance + ",calories : " + calories);
                    }else{
                        //BandResultCode code
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQEUST_BLUETOOTH_DEVICE_SCAN && resultCode == AppCompatActivity.RESULT_OK) {
            String address = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(TAG, "address : " + address);
            if(mClient != null) {
                mClient.bandConnect(address);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void ClearLog(View v) {
        builder.setLength(0);
        mLogTextView.setText("");
    }

    /**
     * SDK RX/TX TEST SOURCE
     */
    StringBuilder builder = new StringBuilder();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ForegroundColorSpan foregroundColorSpan= null;

            builder.append("[");
            builder.append(getCurrentTime());
            builder.append(" ");

            String action = intent.getAction();
            if(action.equals("com.partron.wearable.pwb200.sdk.action_ble_send_data_test")){
                foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);
                builder.append("TX");
                builder.append("] => ");
                builder.append(intent.getStringExtra("send_data"));
            }else if(action.equals("com.partron.wearable.pwb200.sdk.action_ble_receive_data_test")){
                foregroundColorSpan = new ForegroundColorSpan(Color.RED);
                builder.append("RX");
                builder.append("] =>");
                builder.append(intent.getStringExtra("receive_data"));
            }
            builder.append("\n");

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(builder.toString());
            spannableStringBuilder.setSpan(foregroundColorSpan, 0, builder.toString().length()-5,0);

            autoScrollView(spannableStringBuilder.toString());
        }
    };

    private void autoScrollView(String text) {
        if (!text.isEmpty()) {
            mLogTextView.setText(text, TextView.BufferType.SPANNABLE);
        }

        mLogScrollView.post(new Runnable() {
            @Override
            public void run() {
                mLogScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
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
