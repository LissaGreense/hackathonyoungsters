package com.youngsters.myapplication2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Michał 1984 on 2017-06-09.
 */

public class MainFragment extends Fragment {

    View view;

    Handler waiter;

    DownloadStops d;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
<<<<<<< HEAD
        view = inflater.inflate(R.layout.fragment_main, container, false);
        

=======
        view = inflater.inflate(R.layout.activity_main, container, false);

        d = new DownloadStops();
        d.execute();
>>>>>>> c71ab0b2ede197df881668d1a4b84b8e544b373d

        waiter = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        // calling to this function from other pleaces
                        // The notice call method of doing things
                        break;
                    default:
                        break;
                }
            }
        };


        return view;
    }



    class CheckDownloadStops extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            while(true){

            }
            return null;
        }


        protected void onPostExecute(Boolean result) {
            // The results of the above method
            // Processing the results here
            waiter.sendEmptyMessage(0);
        }

    }


    public static double coordinatesToDistance(double latitiudeDevice, double latitiudeStop, double longtitiudeDevice,double longtitiudeStop)
    {
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

    public static int getNearestStop(double latitiudeDevice, double longtitiudeDevice ,JSONArray stopsList) throws JSONException {
        double maxWayToStop = 99999;
        int closestStop = 0;
        for(int i =0;i<stopsList.length();i++)
        {
            JSONObject stop = stopsList.getJSONObject(i);
            int id = stop.getInt("stopId");
            double latitiudeStop = stop.getDouble("stopLat");
            double longitiudeStop = stop.getDouble("stopLon");
            double distanceToThisStop = coordinatesToDistance(latitiudeDevice,latitiudeStop,longtitiudeDevice,longitiudeStop);
            if(distanceToThisStop < maxWayToStop)
            {
                maxWayToStop = distanceToThisStop;
                closestStop = id;
            }
        }
        return closestStop;
    }
}
