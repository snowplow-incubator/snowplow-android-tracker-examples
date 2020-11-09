package io.github.hidroh.materialistic.configurator;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
            url = new URL(params[0] + "?unused=" + System.currentTimeMillis());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == null) {
            return;
        }
        Log.d(TAG, "Downloaded configuration: " + result);
        try {
            configurator.setup(result);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
        }
    }
}
