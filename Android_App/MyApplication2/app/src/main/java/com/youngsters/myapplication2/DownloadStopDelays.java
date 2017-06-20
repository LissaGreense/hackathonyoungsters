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


public class DownloadStopDelays extends AsyncTask<Void, Void, Void> {

    String downloadedJsonData = "";
    private JSONArray closestStopDelays;
    private int id;

    DownloadStopDelays(int id){
        this.id = id;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String temporaryStringID = Integer.toString(id);
        String stopDownloadUrl = "http://87.98.237.99:88/delays?stopId=" +temporaryStringID;

        HttpURLConnection connection;
        BufferedReader reader;

        try {
            URL url = new URL(stopDownloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            String tempDownloadedLine;
            while ((tempDownloadedLine =reader.readLine()) != null) {
                downloadedJsonData += tempDownloadedLine;
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
        JSONObject stopDelaysResult;
        try {
            stopDelaysResult = new JSONObject(downloadedJsonData);
            closestStopDelays = stopDelaysResult.getJSONArray("delay");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getDelays() {
        return closestStopDelays;
    }
}