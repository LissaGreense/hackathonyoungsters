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

public class DownloadStops extends AsyncTask<Void, Void, Void> {

    private JSONArray Stops;
    private String json;
    private boolean running;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Stops = null;
        json = "";
        running = true;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String address = "http://91.244.248.19/dataset/c24aa637-3619-4dc2-a171-a23eec8f2172/resource/cd4c08b5-460e-40db-b920-ab9fc93c1a92/download/stops.json";

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
            Stops = jsonObject.getJSONObject("2017-06-15").getJSONArray("stops");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        running = false;
    }

    public boolean isRunning(){
        if(running) return true;
        else return false;
    }

    public JSONArray getStops() {
        return Stops;
    }
}
