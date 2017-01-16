package com.partron.wearable.pwb200.sdk.sample;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.partron.wearable.pwb200.sdk.core.interfaces.BandScanCallback;
import com.partron.wearable.pwb200.sdk.core.interfaces.PWB_200_Client;

import java.util.ArrayList;

/**
 * Created by user on 2015-11-25.
 */
public class DeviceScanActivity extends Activity implements BandScanCallback,ListView.OnItemClickListener{
    public static final String TAG = "DeviceScanActivity";

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private PWB_200_Client mClient;
    private ListView mLisView;

    @Override
    public void onBandScanCallback(int state, final BluetoothDevice bluetoothDevice, int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bluetoothDevice == null){
                    return;
                }
                mLeDeviceListAdapter.addDevice(bluetoothDevice);
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_divice_scan);
        Log.d("CHOONG","onCreate");

        mLisView = (ListView)findViewById(R.id.bt_list);
        mClient = MainActivity.mClient;
        mClient.registerBandScanCallback(this);

        /**
         * BAND 디바이스 검색 시작
         */
        mClient.bandScan().start(1000 * 10);

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mLisView.setAdapter(mLeDeviceListAdapter);
        mLisView.setOnItemClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mClient.unRegisterBandScanCallback();
        /**
         * BAND 디바이스 검색 중지
         */
        mClient.bandScan().stop();
        mLeDeviceListAdapter.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        Intent result = new Intent();
        result.putExtra(BluetoothDevice.EXTRA_DEVICE, device.getAddress());
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        public void clear() {
            mLeDevices.clear();
        }
        @Override
        public int getCount() {
            return mLeDevices.size();
        }
        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            final String deviceAddress = device.getAddress();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            }

            if (deviceAddress != null && deviceAddress.length() > 0) {
                viewHolder.deviceAddress.setText(deviceAddress);
            }
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

}
