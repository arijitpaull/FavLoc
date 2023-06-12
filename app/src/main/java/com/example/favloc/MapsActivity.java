package com.example.favloc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.favloc.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        int locNumber = intent.getIntExtra("LocNumber", 0);

        if (locNumber == 0) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centeredLoc(location, "Your Location");
                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centeredLoc(lastLocation, "Your Location");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            Location placeLoc = new Location(LocationManager.GPS_PROVIDER);
            double latitude = MainActivity.locations.get(locNumber).latitude;
            placeLoc.setLatitude(latitude);
            double longitude = MainActivity.locations.get(locNumber).longitude;
            placeLoc.setLongitude(longitude);
            centeredLoc(placeLoc, MainActivity.loc.get(locNumber));
        }
    }

    public void centeredLoc(Location location, String title) {
        if (location != null) {
            LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLoc).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 12));
        }
    }

    private void saveLocation(LatLng latLng, String name) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the latitude and longitude of the location
        String key = "location_" + name;
        editor.putString(key, latLng.latitude + "," + latLng.longitude);
        editor.apply();
    }

    private LatLng loadLocation(String name) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Load the latitude and longitude of the location
        String key = "location_" + name;
        String locationStr = sharedPreferences.getString(key, null);
        if (locationStr != null) {
            String[] latLng = locationStr.split(",");
            double latitude = Double.parseDouble(latLng[0]);
            double longitude = Double.parseDouble(latLng[1]);
            return new LatLng(latitude, longitude);
        }
        return null;
    }


    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name...");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();

                if (!TextUtils.isEmpty(newName)) {
                    mMap.addMarker(new MarkerOptions().position(latLng).title(newName));
                    MainActivity.loc.add(newName);
                    MainActivity.locations.add(latLng);
                    MainActivity.arrayAdapter.notifyDataSetChanged();

                    saveLocation(latLng,newName);

                    Toast.makeText(MapsActivity.this, "Location Saved!", Toast.LENGTH_SHORT).show();
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    newName = sdf.format(new Date());

                    mMap.addMarker(new MarkerOptions().position(latLng).title(newName));
                    MainActivity.loc.add(newName);
                    MainActivity.locations.add(latLng);
                    MainActivity.arrayAdapter.notifyDataSetChanged();
                    saveLocation(latLng,newName);

                    Toast.makeText(MapsActivity.this, "Saved with Default Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
