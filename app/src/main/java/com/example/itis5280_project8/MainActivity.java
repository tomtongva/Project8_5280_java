package com.example.itis5280_project8;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.icu.text.LocaleDisplayNames;
import android.os.Build;
import android.os.Bundle;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BlueCatsSDK;
import com.example.itis5280_project8.util.Globals;
import com.example.itis5280_project8.apicalls.Item;
import com.example.itis5280_project8.apicalls.ItemResponse;
import com.example.itis5280_project8.apicalls.RetrofitInterface;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.itis5280_project8.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "Project8";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;

    ArrayList<Item> items = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(Globals.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        requestBlePermissions(this, 001);

        BlueCatsSDK.startPurringWithAppToken(getApplicationContext(), Globals.BlueCatsToken);

        final BCBeaconManager beaconManager = new BCBeaconManager();
        beaconManager.registerCallback(mBeaconManagerCallback);

    }

    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    String previousBeaconName = "";
    int previousRegionProximity = 10;
    private final BCBeaconManagerCallback mBeaconManagerCallback = new BCBeaconManagerCallback() {

        private int findIndexOfBeacon(String name, List<BCBeacon> beacons) {
            int retIndex = -1;

            for (int i = 0; i < beacons.size(); ++i) {
                if (name.equals(beacons.get(i).getName())) {
                    retIndex = i;
                }
            }

            return retIndex;
        }

        private List<Integer> findAllIndices(List<BCBeacon> beacons, int proximityValue) {
            List<Integer> retList = new ArrayList<>();

            for (int i = 0; i < beacons.size(); ++i) {
                if (proximityValue == beacons.get(i).getProximity().getValue())
                    retList.add(i);
            }

            return retList;
        }

        @Override
        public void didRangeBlueCatsBeacons(List<BCBeacon> beacons) {

            int prevBeaconIndex = findIndexOfBeacon(previousBeaconName, beacons);
            int prevBeaconCurrentProximityVal = -1;
            if (prevBeaconIndex > -1)
                prevBeaconCurrentProximityVal = beacons.get(prevBeaconIndex).getProximity().getValue();

            List<Integer> removeIndices = findAllIndices(beacons, prevBeaconCurrentProximityVal);
            Collections.reverse(removeIndices);

            for (int index : removeIndices) {
                Log.d(TAG, "beacon " + beacons.get(index).getName() + " " + beacons.get(index).getProximity().getValue());
                if (beacons.get(index).getProximity().getValue() >= prevBeaconCurrentProximityVal
                        && beacons.get(index).getProximity().getValue() > previousRegionProximity)
                    beacons.remove(index);
            }

            for (BCBeacon currentBeacon : beacons) {
                Log.d(TAG, currentBeacon.getName() + " proximity:" + currentBeacon.getProximity().getValue()
                        + " total array size:" + beacons.size());

                if (currentBeacon.getProximity().getValue() < previousRegionProximity || currentBeacon.getProximity().getValue() < prevBeaconCurrentProximityVal) {
                    previousRegionProximity = currentBeacon.getProximity().getValue();
                    previousBeaconName = currentBeacon.getName();
                }

                Log.d(TAG, "current beacon " + previousBeaconName + " at proximity " + previousRegionProximity);
            }

            getItems(Globals.blueCatsToRegionMap.get(previousBeaconName));
            for (Item item : items) {
                Log.d(TAG, item.getName() + " " + item.getPhoto());
            }
            super.didRangeBlueCatsBeacons(beacons);
        }
    };

    private void getItems(String region) {
        Log.d(TAG, "get items for " + region);
        Call<ItemResponse> call = retrofitInterface.getItems(region);
        call.enqueue(new Callback<ItemResponse>() {
            @Override
            public void onResponse(Call<ItemResponse> call, Response<ItemResponse> response) {
                ItemResponse itemResponse = response.body();
                itemResponse.g
//                items = new ArrayList<>(Arrays.asList(itemResponse.getItemsArray()));
//                PutDataIntoRecyclerView(items);
            }

            @Override
            public void onFailure(Call<ItemResponse> call, Throwable t) {
                Log.d(TAG, "Failed " + t.getMessage());
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}