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
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Thread.sleep;

/**
 * Created by Michał 1984 on 2017-06-09.
 */

public class MainFragment extends Fragment {

    View view;
    boolean istram;

    Thread threadDownloadStops, threadStopDelays;

    boolean wait;

    JSONArray stopsList;
    JSONArray stopDelaysList;

    ImageView image;

    TextView stopname, line;

    int nearestStop;

    double longitude;
    double latitude;
    TextToSpeech t1;
    CharSequence toSpeak = "";
    List<Integer> closeststops = new ArrayList<Integer>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.kadzetan, container, false);

        stopsList = null;
        stopDelaysList = null;

        longitude = -1;
        latitude = -1;

        istram = false;

        stopname = (TextView) view.findViewById(R.id.stop_name);
        stopname.setText("Stop_name");

        line = (TextView) view.findViewById(R.id.vehicle_number);
        line.setText("Vehicle_num");

        ImageView bus = (ImageView) view.findViewById(R.id.bus_image); //tram
        ImageView tram = (ImageView) view.findViewById(R.id.tram_image); //tram

        configureThreads();

        if (!MainActivity.isBus)
        {
            tram.setVisibility(View.VISIBLE);
            bus.setVisibility(View.INVISIBLE);
        }
        else
        {
            tram.setVisibility(View.INVISIBLE);
            bus.setVisibility(View.VISIBLE);
        }

        if (isThereSavedStops()) {
            stopsList = loadStops();
            threadStopDelays.start();

        } else {
            threadDownloadStops.start();
        }
        
        return view;
    }

    private void configureThreads(){
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
                if(getActivity() == null) return;
                else {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            stopname.setText("works");
                            saveStops();
                            threadStopDelays.start();
                        }
                    });
                }
            }
        });

        threadStopDelays = new Thread(new Runnable() {
            @Override
            public void run() {
                if(getActivity() == null) return;
                else {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            stopname.setText("gps");
                            configureGPS();
                        }
                    });
                }


                while (longitude == -1 || latitude == -1) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //gps position got


                wait = true;
                if(getActivity() == null) return;
                else {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            nearestStop = -2013;
                            stopname.setText("nearestStop");
                            try {
                                nearestStop = getNearestStop(latitude, longitude, stopsList);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }stopname.setText("nearestStop");
                            wait = false;
                        }
                    });
                }
                while (wait) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                for(int i =0;i < closeststops.size();i++)
                {
                    final DownloadStopDelays stopDelays = new DownloadStopDelays(closeststops.get(i));
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
                        if(getActivity() == null) return;
                        else {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                stopname.setText("speak");
                                try {
                                    JSONObject vehicle = (JSONObject) stopDelaysList.get(0);
                                    String time = vehicle.getString("estimatedTime");
                                    String ID = vehicle.getString("routeId");
                                    String headsign = vehicle.getString("headsign");

                                    if((ID.length() > 2 && MainActivity.isBus )|| (ID.length() < 3 && !MainActivity.isBus )) {
                                        char temp = time.charAt(0);
                                        if (temp == '0') temp = ' ';
                                        stopname.setText(ID);
                                        line.setText(headsign);
                                        toSpeak = "Attention Please,in " + temp + time.charAt(1) + " minutes, bus number " + ID + " to " + headsign + " will arrive";
                                        t1 = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
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
                    }}}
            }
        });
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
                getActivity().getSystemService(Context.LOCATION_SERVICE);

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
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void saveStops(){
        SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = stopsList.toString();
        prefsEditor.putString("StopsList", json);
        prefsEditor.apply();
    }

    private boolean isThereSavedStops(){
        SharedPreferences  mPrefs = getActivity().getPreferences(MODE_PRIVATE);
        String json = mPrefs.getString("StopsList", "");
        if(json.equals("")) return false;
        else return true;
    }

    private JSONArray loadStops(){
        SharedPreferences  mPrefs = getActivity().getPreferences(MODE_PRIVATE);
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
