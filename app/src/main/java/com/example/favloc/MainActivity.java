package com.example.favloc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> loc = new ArrayList<>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);

        loadLocations();

        if(loc.isEmpty()) {
            loc.add("ADD FAVOURITE LOCATION");
            locations.add(new LatLng(0, 0));
        }

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,loc);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("LocNumber",i);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(i!=0) {
                int x = i;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("Saved location will be deleted.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loc.remove(x);
                                arrayAdapter.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

                return true;
            }
        });
    }

    private void saveLocations() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the location names
        Set<String> locationNames = new HashSet<>(loc);
        editor.putStringSet("locationNames", locationNames);

        // Save the latitude and longitude of each location
        for (int i = 0; i < locations.size(); i++) {
            String key = "location_" + i;
            LatLng location = locations.get(i);
            editor.putString(key, location.latitude + "," + location.longitude);
        }

        editor.apply();
    }

    private void loadLocations() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        Set<String> locationNames = sharedPreferences.getStringSet("locationNames", new HashSet<>());
        loc.clear();
        loc.addAll(locationNames);


        if (loc.contains("ADD FAVOURITE LOCATION")) {
            loc.remove("ADD FAVOURITE LOCATION");
            loc.add(0, "ADD FAVOURITE LOCATION");
        } else {
            loc.add(0, "ADD FAVOURITE LOCATION");
        }


        locations.clear();
        for (int i = 0; i < locationNames.size(); i++) {
            String key = "location_" + i;
            String locationStr = sharedPreferences.getString(key, null);
            if (locationStr != null) {
                String[] latLng = locationStr.split(",");
                double latitude = Double.parseDouble(latLng[0]);
                double longitude = Double.parseDouble(latLng[1]);
                locations.add(new LatLng(latitude, longitude));
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveLocations();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            int locNumber = data.getIntExtra("LocNumber", 0);
            String newName = data.getStringExtra("NewName");

            if (!TextUtils.isEmpty(newName)) {
                loc.set(locNumber, newName);
                arrayAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid Name!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}