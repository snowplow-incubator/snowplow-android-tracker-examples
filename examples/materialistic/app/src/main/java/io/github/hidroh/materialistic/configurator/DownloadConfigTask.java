package io.github.hidroh.materialistic.configurator;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadConfigTask extends AsyncTask<String, String, String> {
    static private String TAG = DownloadConfigTask.class.getSimpleName();
    private Configurator configurator;

    public DownloadConfigTask(Configurator configurator) {
        this.configurator = configurator;
    }

    @Override
    protected String doInBackground(String... params) {
        URL url = null;
        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setDefaultUseCaches(false);
            connection.setUseCaches(false);
            try {
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }
                connection.connect();
                try (InputStream stream = connection.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    return sb.toString();
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) {
            return;
        }
        Log.d(TAG, "Downloaded configuration: " + result);
        try {
            configurator.process(result);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
        }
    }
}
