package com.youngsters.myapplication2;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadStops extends AsyncTask<Void, Void, Void> {

    private String json;
    private JSONArray stopsArray;
    private String date;

    public DownloadStops(){
        json = "";
        stopsArray = new JSONArray();
        date = "1900-01-01";
    }

    @Override
    protected Void doInBackground(Void... params) {
        json = "";

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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        try {
            jsonObject = new JSONObject(json);
            stopsArray = jsonObject.getJSONObject(dateFormat.format(currentDate)).getJSONArray("stops");
            date = dateFormat.format(currentDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getStopsArray() {
        return stopsArray;
    }

    public String getDataDate(){
        return date;
    }
}
