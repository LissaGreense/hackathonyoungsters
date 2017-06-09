package com.youngsters.myapplication2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    DownloadStops d;
    TextView lon;
    TextView lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        d = new DownloadStops();
        d.execute();

        DownloadStopDelays delays = new DownloadStopDelays(4);
        //delays.execute();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (d.getStatus() != AsyncTask.Status.FINISHED) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
        t.start();
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

    public double coordinatesToDistance(double latitiudeDevice, double latitiudeStop, double longtitiudeDevice, double longtitiudeStop) {

        final int RADIUS = 6371;
        double latDistance = Math.toRadians(latitiudeStop - latitiudeDevice);
        double lonDistance = Math.toRadians(longtitiudeStop - longtitiudeDevice);
        double tempDistance = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitiudeDevice)) * Math.cos(Math.toRadians(latitiudeStop))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        tempDistance = 2 * Math.atan2(Math.sqrt(tempDistance), Math.sqrt(1 - tempDistance));
        double distance = RADIUS * tempDistance * 1000;
        return Math.abs(distance);
    }

    public int getNearestStop(double latitiudeDevice, double longtitiudeDevice, JSONArray stopsList) throws JSONException {
        double maxWayToStop = 99999;
        int closestStop = 0;
        for (int i = 0; i < stopsList.length(); i++) {
            JSONObject stop = stopsList.getJSONObject(i);
            int id = stop.getInt("stopId");
            double latitiudeStop = stop.getDouble("stopLat");
            double longitiudeStop = stop.getDouble("stopLon");
            double distanceToThisStop = coordinatesToDistance(latitiudeDevice, latitiudeStop, longtitiudeDevice, longitiudeStop);
            if (distanceToThisStop < maxWayToStop) {
                maxWayToStop = distanceToThisStop;
                closestStop = id;
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
                lat.setText(location.getLatitude() + "");
                lon.setText(location.getLongitude() + "");
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
}
