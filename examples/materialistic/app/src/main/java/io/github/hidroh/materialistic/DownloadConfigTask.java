package io.github.hidroh.materialistic;

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
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            try {
                if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }
                httpURLConnection.connect();
                try (InputStream stream = httpURLConnection.getInputStream()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    return sb.toString();
                }
            } finally {
                httpURLConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "Downloaded configuration: " + result);
        try {
            JSONObject config = new JSONObject(result);

            /*
            SET HERE THE NEW TRACKER INSTANCE WITH THE JSON
             */

        } catch (JSONException e) {
            Log.e(TAG, "Error: " + e.toString());
        }
    }
}
