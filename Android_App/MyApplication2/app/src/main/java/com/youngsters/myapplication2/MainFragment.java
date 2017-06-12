package com.youngsters.myapplication2;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Thread.sleep;

public class MainFragment extends Fragment {

    View view;

    Thread threadDownloadStops, threadStopDelays;

    boolean wait;

    Speech speech;

    JSONArray stopsList;
    JSONArray stopDelaysList;

    TextView stopname, line, to;

    Typeface font;

    final int MAX_RADIUS = 250;

    int nearestStop;

    double longitude;
    double latitude;

    List<Integer> closeststops = new ArrayList<Integer>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.kadzetan, container, false);

        speech = new Speech(getContext());

        stopsList = null;
        stopDelaysList = null;

        font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/jaapokki-regular.ttf");

        longitude = -1;
        latitude = -1;


        stopname = (TextView) view.findViewById(R.id.stop_name);
        stopname.setTypeface(font);

        line = (TextView) view.findViewById(R.id.vehicle_number);
        line.setTypeface(font);

        to = (TextView) view.findViewById(R.id.TO);
        to.setTypeface(font);

        ImageView bus = (ImageView) view.findViewById(R.id.bus_image); //bus
        ImageView tram = (ImageView) view.findViewById(R.id.tram_image); //tram

        configureThreads();

        if (!MainActivity.isBus) {
            tram.setVisibility(View.VISIBLE);
            bus.setVisibility(View.INVISIBLE);
            line.setText("TRAM NUMBER");
        } else {
            tram.setVisibility(View.INVISIBLE);
            bus.setVisibility(View.VISIBLE);
            line.setText("BUS NUMBER");
        }

        /*if (isThereSavedStops()) {
            stopsList = loadStops();
            threadStopDelays.start();

        } else {
            threadDownloadStops.start();
        }*/

        threadDownloadStops.start();

        return view;
    }

    private void configureThreads() {
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

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        saveStops();
                        Toast.makeText(getContext(), "Downloaded stops", Toast.LENGTH_SHORT).show();
                        threadStopDelays.start();
                    }
                });
            }
        });

        threadStopDelays = new Thread(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        configureGPS();
                        Toast.makeText(getContext(), "Getting gps position", Toast.LENGTH_SHORT).show();
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
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        nearestStop = -2;
                        Toast.makeText(getContext(), "Searching for nearest stop", Toast.LENGTH_SHORT).show();
                        try {
                            nearestStop = getNearestStop(latitude, longitude, stopsList);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        wait = false;
                    }
                });

                while (wait) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < closeststops.size(); i++) {
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

                    if (stopDelaysList.length() > 0) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    JSONObject vehicle = (JSONObject) stopDelaysList.get(0);
                                    String time = vehicle.getString("estimatedTime");
                                    String ID = vehicle.getString("routeId");
                                    String headsign = vehicle.getString("headsign");

                                    char min1 = time.charAt(0);
                                    if (min1 == '0') min1 = ' ';
                                    String minutes = "" + min1 + time.charAt(1);

                                    //bus
                                    if (ID.length() > 2 && MainActivity.isBus) {
                                        String text = "Uwaga, za " + minutes + " minuty, autobus numerze " + ID + " do " + headsign + " odjedzie z przystanku";
                                        //String text = "Attention Please, in " + minutes + " minutes, tram number " + ID + " to " + headsign + " will arrive";
                                        speech.speak(text);
                                        stopname.setText(ID);
                                        line.setText(headsign);
                                    }
                                    //tram
                                    else if (ID.length() < 3 && !MainActivity.isBus) {
                                        String text = "Uwaga, za " + minutes + " minuty, tramwaj o numerze " + ID + " do " + headsign + " odjedzie z przystanku";
                                        //String text = "Attention Please, in " + minutes + " minutes, tram number " + ID + " to " + headsign + " will arrive";
                                        speech.speak(text);
                                        stopname.setText(ID);
                                        line.setText(headsign);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        //no buses
                        //no trams
                    }
                }
                if (closeststops.size() == 0);
                    //speech.speak("W promieniu" + MAX_RADIUS + " meterów, nie ma żadnego przystanku");
                    //speech.speak("There are no stops in radius of" + MAX_RADIUS + " meters");
            }
        });
    }

    public int getNearestStop(double latitiudeDevice, double longtitiudeDevice, JSONArray stopsList) throws JSONException {
        double maxWayToStop = MAX_RADIUS;
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
            double distanceToThisStop = device.distanceTo(stop_location);
            if (distanceToThisStop < maxWayToStop) {
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

    private void saveStops() {
        SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = stopsList.toString();
        prefsEditor.putString("StopsList", json);
        prefsEditor.apply();
    }

    private boolean isThereSavedStops() {
        SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
        String json = mPrefs.getString("StopsList", "");
        if (json.equals("")) return false;
        else return true;
    }

    private JSONArray loadStops() {
        SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
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
