package com.youngsters.myapplication2;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    public static boolean isBus;

    boolean wait;

    JSONArray stopsList;
    JSONArray stopDelaysList;

    Thread threadDownloadStops, threadStopDelays;

    int nearestStop;

    double longitude;
    double latitude;
    TextToSpeech t1;
    CharSequence toSpeak = "";
    List<Integer> closeststops = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopsList = null;
        stopDelaysList = null;

        longitude = -1;
        latitude = -1;

        threadDownloadStops = new Thread(new Runnable() {
            @Override
            public void run() {
                DownloadStops stops = new DownloadStops();
                stops.execute();

                while (stops.getStatus() != AsyncTask.Status.FINISHED) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //download stops finished
                stopsList = stops.getStopsArray();
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        saveStops();
                        threadStopDelays.start();
                    }
                });
            }
        });


        threadStopDelays = new Thread(new Runnable() {
            @Override
            public void run() {

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        configureGPS();
                    }
                });


                while (longitude == -1 || latitude == -1) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //gps position got



                wait = true;
                nearestStop = -2013;
                try {
                    nearestStop = getNearestStop(latitude, longitude, stopsList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                wait = false;

                while (wait) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                for(int i =0;i<closeststops.size();i++)
                {final DownloadStopDelays stopDelays = new DownloadStopDelays(nearestStop);
                    stopDelays.execute();

                    while (stopDelays.getStatus() != AsyncTask.Status.FINISHED) {
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //download stop delays completed
                    stopDelaysList = stopDelays.getDelays();
                    if(stopDelaysList.length()>0)
                    {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try {

                                JSONObject vehicle = (JSONObject) stopDelaysList.get(0);
                                String time = vehicle.getString("estimatedTime");
                                String ID = vehicle.getString("routeId");
                                String headsign = vehicle.getString("headsign");
                                if((headsign.length() > 2 && isBus == true )|| (headsign.length() < 3 && isBus == false )) {
                                    char temp = time.charAt(0);
                                    if (temp == '0') temp = ' ';

                                    toSpeak = "Attention Please,in " + temp + time.charAt(1) + " minutes, buss number " + ID + " to " + headsign + " will arrive";
                                    t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                        @Override
                                        public void onInit(int status) {
                                            if (status != TextToSpeech.ERROR) {
                                                t1.setLanguage(Locale.UK);
                                                t1.speak(toSpeak, 1, null, null);
                                            }
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }}
            }
        });

        if (isThereSavedStops()) {
            stopsList = loadStops();
            threadStopDelays.start();

        } else {
            threadDownloadStops.start();
        }

        threadDownloadStops.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int getNearestStop(double latitiudeDevice, double longtitiudeDevice, JSONArray stopsList) throws JSONException {
        double maxWayToStop = 160;
        int closestStop = -1;
        Location device = new Location("device");
        device.setLatitude(latitiudeDevice);
        device.setLongitude(longtitiudeDevice);
        for (int i = 0; i < stopsList.length(); i++) {
            JSONObject stop = stopsList.getJSONObject(i);
            int id = stop.getInt("stopId");
            double latitiudeStop = stop.getDouble("stopLat");
            double longitiudeStop = stop.getDouble("stopLon");
            Location stop_location = new Location("stop");
            stop_location.setLatitude(latitiudeStop);
            stop_location.setLongitude(longitiudeStop);
            double distanceToThisStop =device.distanceTo(stop_location);
            if (distanceToThisStop < maxWayToStop) {
                maxWayToStop = distanceToThisStop;
                closestStop = id;
                closeststops.add(id);
            }
        }
        return closestStop;
    }

    private void configureGPS() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                //lat.setText(""+latitude);
                //lon.setText(""+longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void saveStops(){
        SharedPreferences  mPrefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = stopsList.toString();
        prefsEditor.putString("StopsList", json);
        prefsEditor.apply();
    }

    private boolean isThereSavedStops(){
        SharedPreferences  mPrefs = getPreferences(MODE_PRIVATE);
        String json = mPrefs.getString("StopsList", "");
        if(json.equals("")) return false;
        else return true;
    }

    private JSONArray loadStops(){
        SharedPreferences  mPrefs = getPreferences(MODE_PRIVATE);
        String json = mPrefs.getString("StopsList", "");
        JSONArray array = null;
        try {
            array = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }
}

