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

    StringBuffer buffer;
    String json = "";

    @Override
    protected Void doInBackground(Void... params) {
        String address = "http://91.244.248.19/dataset/c24aa637-3619-4dc2-a171-a23eec8f2172/resource/cd4c08b5-460e-40db-b920-ab9fc93c1a92/download/stops.json";

        HttpURLConnection connection = null;
        BufferedReader reader = null;

            try {
                URL url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                //buffer = new StringBuffer();

                String line;
            while ((line =reader.readLine()) != null) {
                //buffer.append(line).append("\n");
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
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
            JSONArray stopsArray = jsonObject.getJSONObject("2017-06-15").getJSONArray("stops");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
