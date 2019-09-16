package com.telie.hw1;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.StringReader;
import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private final String gMapsAPIURL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private final String gMapsAPIKey = "&key=AIzaSyDf6GzYHZBb-g70qJD2VNBmacQnYhymkMg";
    MapFragment mv;
    GoogleMap map;
    double lat = 0, lon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mv = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mv.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.addMarker((new MarkerOptions()
                .position(new LatLng(lat, lon))));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMyLocationEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 10));
    }

    public void submitLocation(View v) {
        String json = "";
        double precipProb = 0, temp = 0, humid = 0, windSpd = 0;
        String precipType = "";
        EditText locationET = (EditText) findViewById(R.id.editText);
        String locationStr = locationET.getText().toString();
        locationStr = locationStr.replace(' ', '+');
        StringBuilder sb = new StringBuilder();
        sb.append(gMapsAPIURL);
        sb.append(locationStr);
        sb.append(gMapsAPIKey);
        try {
            NetworkingAsync net = new NetworkingAsync();
            net.execute(new URL(sb.toString()));
            json = net.get();
        } catch (MalformedURLException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        JsonReader jsrdr = new JsonReader(new StringReader(json));
        try {
            jsrdr.beginObject();
            while(jsrdr.hasNext()) {
                if(jsrdr.nextName().equals("results")) {
                    jsrdr.beginArray();
                    jsrdr.beginObject();
                    while (jsrdr.hasNext()) {
                        if (!jsrdr.nextName().equals("geometry")) {
                            jsrdr.skipValue();
                            continue;
                        }
                        jsrdr.beginObject();
                        while (jsrdr.hasNext()) {
                            if(jsrdr.nextName().equals("location")) {
                                jsrdr.beginObject();
                                if(jsrdr.nextName().equals("lat")) {
                                    lat = jsrdr.nextDouble();
                                    jsrdr.nextName();
                                    lon = jsrdr.nextDouble();
                                } else if(jsrdr.nextName().equals("lng")) {
                                    lon = jsrdr.nextDouble();
                                    jsrdr.nextName();
                                    lat = jsrdr.nextDouble();
                                }
                                int b = 0;
                            }
                        }
                    }
                }
                else {
                    jsrdr.skipValue();
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        try {
            try {
                @SuppressLint("DefaultLocale") URL darkReq = new URL(String.format("https://api.darksky.net/forecast/%s/%f,%f", "ade59c6e9c04e17097bbb7c3106d444e", lat, lon));
                NetworkingAsync net = new NetworkingAsync();
                net.execute(darkReq);
                json = net.get();
            } catch (MalformedURLException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            JsonReader darkSkyReader = new JsonReader(new StringReader(json));
            //temperature, humidity, windSpeed, precipProbability, precipType
            darkSkyReader.beginObject();
            while(darkSkyReader.hasNext()) {
                if(darkSkyReader.nextName().equals("currently")) {
                    darkSkyReader.beginObject();
                    while(darkSkyReader.hasNext()){
                        String name = darkSkyReader.nextName();
                        if(name.equals("temperature")){
                            temp = darkSkyReader.nextDouble();
                        }
                        else if(name.equals("humidity")) {
                            humid = darkSkyReader.nextDouble();
                        }
                        else if(name.equals("windSpeed")) {
                            windSpd = darkSkyReader.nextDouble();
                        }
                        else if(name.equals("precipProbability")) {
                            precipProb = darkSkyReader.nextDouble();
                        }
                        else if(name.equals("precipType")) {
                            precipType = darkSkyReader.nextString();
                        }
                        else {
                            darkSkyReader.skipValue();
                        }
                    }
                } else {
                    darkSkyReader.skipValue();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(precipType.equals("")){
            ((TextView) findViewById(R.id.textViewPrecipType)).setText(getString(R.string.noChangePrecip));
        } else {
            ((TextView) findViewById(R.id.textViewPrecipType)).setText(getString(R.string.precip, precipProb * 100, precipType));
        }
        ((TextView) findViewById(R.id.textViewHumid)).setText(getString(R.string.humidity, humid * 100));
        ((TextView) findViewById(R.id.textViewTemp)).setText(getString(R.string.tempString, temp));
        ((TextView) findViewById(R.id.textViewWindSpd)).setText(getString(R.string.wind_speed, windSpd));
        mv.getMapAsync(this);
    }
}
