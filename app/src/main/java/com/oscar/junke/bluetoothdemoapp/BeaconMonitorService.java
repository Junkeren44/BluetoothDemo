package com.oscar.junke.bluetoothdemoapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.exception.ScanError;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.ScanStatusListener;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeaconMonitorService extends Service {
    private static final String TAG = BeaconMonitorService.class.getSimpleName();
    private ProximityManager proximityManager;

    //BroadcastReceiver that listens to bluetooth-system calls and make sure the service is in a proper state
    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        proximityManager.disconnect();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        if (proximityManager != null && proximityManager.isConnected()) {
                            proximityManager.startScanning();
                        } else {
                            connectBeaconManager(); // Start over trying to connect
                        }
                        break;
                }
            }
        }
    };

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        IntentFilter filter = new IntentFilter((BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(bluetoothReceiver, filter); //Registers the local receiver to listen for bluetooth adapter changes



        KontaktSDK.initialize(BeaconMonitorService.this);
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            connectBeaconManager();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Connect the proximityManager and starts monitoring if bluetooth is on
     */
    private void connectBeaconManager() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {

            proximityManager = new ProximityManager(getApplicationContext());

            proximityManager.connect(new OnServiceReadyListener() {
                @Override
                public void onServiceReady() {
                    // Configuration
                        proximityManager.configuration()
                                .scanMode(ScanMode.LOW_POWER)
                                .scanPeriod(ScanPeriod.create(TimeUnit.SECONDS.toMillis(8), TimeUnit.SECONDS.toMillis(4)))
                                .activityCheckConfiguration(ActivityCheckConfiguration.create(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(4)))
                                .forceScanConfiguration(new ForceScanConfiguration(TimeUnit.SECONDS.toMillis(8), TimeUnit.SECONDS.toMillis(2)))
                                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(2))
                                .rssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(3))
                                .monitoringEnabled(true)
                                .monitoringSyncInterval(60);

                    proximityManager.setIBeaconListener(new IBeaconListener() {
                        @Override
                        public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
                            Log.i(TAG, "onIBeaconDiscovered: " + iBeacon.getUniqueId());

                        }

                        @Override
                        public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                            Log.i(TAG, "onIBeaconsUpdated, size: " + iBeacons.size());

                        }

                        @Override
                        public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
                            Log.i(TAG, "onIBeaconLost: " + iBeacon.getUniqueId());

                        }
                    });

                    proximityManager.startScanning();
                }
            });
        }

    }



    /**
     * Our custom Binder
     * Simple hands the service to the bound activity
     */

    }

