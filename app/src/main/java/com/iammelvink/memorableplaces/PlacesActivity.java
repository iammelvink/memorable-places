package com.iammelvink.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlacesActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap mMap;
    /*Setting the app location
     * where to save the data*/
//    private SharedPreferences sharedPreferences = this.getSharedPreferences("com.iammelvink.memorableplaces", Context.MODE_PRIVATE);
//    private SharedPreferences sharedPreferences1 = this.getSharedPreferences("com.iammelvink.memorableplaces", Context.MODE_PRIVATE);

    public void centerMapOnLocation(Location location, String title) {
        if (location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*Get map ready*/
        mMap = googleMap;

        /*4=GoogleMap.MAP_TYPE_HYBRID*/
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        if (intent.getIntExtra("placeNumber", 0) == 0) {
            // Zoom in on user location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location, "Your Location");
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your Location");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber", 0)).longitude);

            centerMapOnLocation(placeLocation, MainActivity.places.get(intent.getIntExtra("placeNumber", 0)));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "";

        try {

            List<Address> listAdddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAdddresses != null && listAdddresses.size() > 0) {
                if (listAdddresses.get(0).getThoroughfare() != null) {
                    if (listAdddresses.get(0).getSubThoroughfare() != null) {
                        address += listAdddresses.get(0).getSubThoroughfare() + " ";
                    }
                    address += listAdddresses.get(0).getThoroughfare();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (address.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
            address += sdf.format(new Date());
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));


        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);

        MainActivity.aAdapter.notifyDataSetChanged();

        /*Calling savePlacesInStorage
        Save places to storage*/
        savePlacesInStorage();

        Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();
    }

    public void savePlacesInStorage() {
        /*Serializing ArrayList to save it as a String*/
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.iammelvink.memorableplaces", Context.MODE_PRIVATE);
        ArrayList<String> latitude = new ArrayList<String>();
        ArrayList<String> longitude = new ArrayList<String>();

        try {
            for (LatLng coord : MainActivity.locations) {
                latitude.add(Double.toString(coord.latitude));
                longitude.add(Double.toString(coord.longitude));
            }
            sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitude)).apply();
            sharedPreferences.edit().putString("lons", ObjectSerializer.serialize(longitude)).apply();

            Log.i("places", ObjectSerializer.serialize(MainActivity.places));
            Log.i("lats", ObjectSerializer.serialize(latitude));
            Log.i("lons", ObjectSerializer.serialize(longitude));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
