package com.example.itis5280_project8;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluecats.sdk.BCBeacon;
import com.bluecats.sdk.BCBeaconManager;
import com.bluecats.sdk.BCBeaconManagerCallback;
import com.bluecats.sdk.BlueCatsSDK;
import com.example.itis5280_project8.apicalls.Item;
import com.example.itis5280_project8.apicalls.ItemResponse;
import com.example.itis5280_project8.apicalls.RetrofitInterface;
import com.example.itis5280_project8.databinding.FragmentFirstBinding;
import com.example.itis5280_project8.util.Globals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FirstFragment extends Fragment {
    public static String TAG = "Project8";

    private FragmentFirstBinding binding;

    RetrofitInterface retrofitInterface;
    Retrofit retrofit;

    ArrayList<Item> items = new ArrayList<>();
    LinearLayoutManager layoutManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        getActivity().setTitle("Aisle Items");
        layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        retrofit = new Retrofit.Builder()
                .baseUrl(Globals.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        requestBlePermissions(getActivity(), 001);

        BlueCatsSDK.startPurringWithAppToken(getContext(), Globals.BlueCatsToken);

        final BCBeaconManager beaconManager = new BCBeaconManager();
        beaconManager.registerCallback(mBeaconManagerCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                items = new ArrayList<>(Arrays.asList(itemResponse.getItemsArray()));
                Log.d(TAG, "items " + items.toString());
//                PutDataIntoRecyclerView(items);
            }

            @Override
            public void onFailure(Call<ItemResponse> call, Throwable t) {
                Log.d(TAG, "Failed " + t.getMessage());
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public class ItemsRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}