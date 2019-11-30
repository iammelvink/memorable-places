package com.iammelvink.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listItems;
    /*protected= can be used anywhere by the app
     * static= can be accessed anywhere in the app*/
    protected static ArrayList<String> places = new ArrayList<String>();
    protected static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    protected static ArrayAdapter aAdapter;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Calling getaSavedPlacesFromStorage
         * from storage*/
        getaSavedPlacesFromStorage();

        listItems = (ListView) findViewById(R.id.listOfMemorablePlaces);

        /*
         * Adapter to put ArrayList into ListView*/
        aAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        listItems.setAdapter(aAdapter);

        /*
         * ListView OnItemClickListener*/
        listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                /*Move to next Activity
                 * using Intent*/
                intent = new Intent(getApplicationContext(), PlacesActivity.class);

                /*Send info to next Activity*/
                intent.putExtra("placeNumber", position);

                /*Make it happen*/
                startActivity(intent);
            }
        });
    }

    public void getaSavedPlacesFromStorage() {
        /*Deserializing String to get it as an ArrayList*/
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.iammelvink.memorableplaces", Context.MODE_PRIVATE);

        ArrayList<String> latitudes = new ArrayList<String>();
        ArrayList<String> longitudes = new ArrayList<String>();

        places.clear();
        latitudes.clear();
        longitudes.clear();

        try {
            /*Default value must be an empty ArrayList*/
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",
                    ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats",
                    ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lons",
                    ObjectSerializer.serialize(new ArrayList<String>())));

            Log.i("places", places.toString());
            Log.i("lats", latitudes.toString());
            Log.i("lons", longitudes.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (places.size() > 0 && latitudes.size() > 0 && longitudes.size() > 0) {
            if (places.size() == latitudes.size() && places.size() == longitudes.size()) {
                for (int i = 0; i < latitudes.size(); i++) {
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)), Double.parseDouble(longitudes.get(i))));
                }
            }
        } else {
            /*
             * Adding placeholder places ArrayList*/
            places.add("Add a new place...");
            locations.add(new LatLng(0, 0));
        }
    }
}
