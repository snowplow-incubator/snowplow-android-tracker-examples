package io.github.hidroh.materialistic.configurator;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadConfigTask extends AsyncTask<String, String, String> {
    static private String TAG = DownloadConfigTask.class.getSimpleName();

    private Configurator configurator;
    private Context context;

    public DownloadConfigTask(Context context, Configurator configurator) {
        this.context = context;
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
            if (!response.isSuccessful()) {
                Log.d(TAG, "Downloader response code: " + response.code() + " - url: " + url);
                return null;
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                Log.d(TAG, "Response body is null");
                return null;
            }
            String result = responseBody.string();
            responseBody.close();
            return result;
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
            boolean configIsAccepted = configurator.setup(result);
            if (configIsAccepted) {
                showDownloadedConfig(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
        }
    }

    private void showDownloadedConfig(String jsonConfig) {
        Toast.makeText(context, jsonConfig, Toast.LENGTH_LONG).show();
    }
}
