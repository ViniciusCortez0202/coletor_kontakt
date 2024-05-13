package com.example.coletor_kontack;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import io.flutter.embedding.android.FlutterActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    public static final int REQUEST_CODE_PERMISSIONS = 100;
    private ProximityManager proximityManager;
    private String CHANNEL = "samples.flutter.dev/beacons";

    private List<HashMap<String, Integer>> values = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
        KontaktSDK.initialize("OJWPPKwLEuahTooyXDKxRkuiYMwQTbVZ");

        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.configuration().deviceUpdateCallbackInterval(10)
                .scanPeriod(ScanPeriod.RANGING);
        proximityManager.setIBeaconListener(createIBeaconListener());

    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler (
            (call, result) -> {

                if (call.method.equals("startListener")) {
                    values.clear();
                    proximityManager.connect(new OnServiceReadyListener() {
                        @Override
                        public void onServiceReady() {
                            proximityManager.startScanning();
                        }
                    });
                    result.success("start");

                } else if(call.method.equals("stopListener")) {
                    proximityManager.stopScanning();
                    result.success(values);
                } else {
                    result.notImplemented();
                }
            }
        );

//        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
//                .setStreamHandler(new EventChannel.StreamHandler() {
//            @Override
//            public void onListen(Object arguments, EventChannel.EventSink events) {
//                events.success(null);
//            }
//
//            @Override
//            public void onCancel(Object arguments) {
//
//            }
//        });
    }


    private IBeaconListener createIBeaconListener() {
        return new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
                //Beacon discovered
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                //Log.i("Sample", "IBeacon discovered: " + iBeacons.toString());
                HashMap<String, Integer> map = new HashMap();
                for(IBeaconDevice device : iBeacons) {
                    map.put(device.getAddress(), device.getRssi());
                }
                values.add(map);
            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
                //Beacon lost

            }
        };
    }

    private void checkPermissions() {
        String[] requiredPermissions = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
                ? new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                : new String[]{ Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION };
        if(isAnyOfPermissionsNotGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS);
        }
    }

    private boolean isAnyOfPermissionsNotGranted(String[] requiredPermissions){
        for(String permission: requiredPermissions){
            int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, permission);
            if(PackageManager.PERMISSION_GRANTED != checkSelfPermissionResult){
                return true;
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (REQUEST_CODE_PERMISSIONS == requestCode) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            }
        } else {
            //disableButtons();
            Toast.makeText(this, "Location permissions are mandatory to use BLE features on Android 6.0 or higher", Toast.LENGTH_LONG).show();
        }
    }

}
