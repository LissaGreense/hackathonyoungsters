package com.youngsters.myapplication2;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Michał 1984 on 2017-06-09.
 */

public class DownloadStopDelays extends AsyncTask<Void, Void, Void> {

    String json = "";
    private JSONArray delays;
    private int id;

    DownloadStopDelays(int id){
        this.id = id;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String address = "http://87.98.237.99:88/delays?stopId=" +id;

        HttpURLConnection connection;
        BufferedReader reader;

        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while ((line =reader.readLine()) != null) {
                json += line;
            }

            connection.disconnect();



        }catch( Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
            delays = jsonObject.getJSONArray("delay");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getDelays() {
        return delays;
    }
}