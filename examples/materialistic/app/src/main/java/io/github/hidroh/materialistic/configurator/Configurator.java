package io.github.hidroh.materialistic.configurator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.util.Consumer;
import androidx.preference.PreferenceManager;

import com.snowplowanalytics.snowplow.Snowplow;
import com.snowplowanalytics.snowplow.configuration.RemoteConfiguration;
import com.snowplowanalytics.snowplow.network.HttpMethod;

import java.util.List;

import io.github.hidroh.materialistic.R;

public class Configurator {
    static private String TAG = Configurator.class.getSimpleName();

    private static String defaultUrl = "https://mobile-app-config-bucket.s3.us-east-2.amazonaws.com/materialistic-v2-config.json";
    private static String oldConfig = "";

    public synchronized static void init(Context context) {
        String key = context.getString(R.string.pref_snowplow_url);
        String configUrl = PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
        if (configUrl.isEmpty()) configUrl = defaultUrl;
        if (configUrl.equals(oldConfig)) {
            refresh(context, configUrl);
        } else {
            setup(context, configUrl);
        }
    }

    private static void setup(Context context, String configUrl) {
        RemoteConfiguration remoteConfig = new RemoteConfiguration(configUrl, HttpMethod.GET);
        Snowplow.setup(context, remoteConfig, null, new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) {
                Log.v(TAG, String.valueOf(strings));
                oldConfig = configUrl;
                showToast(context, "Tracker SETUP\r\n" + configUrl);
            }
        });
    }

    private static void refresh(Context context, String configUrl) {
        Snowplow.refresh(context, new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) {
                Log.v(TAG, String.valueOf(strings));
                showToast(context, "Tracker REFRESH\r\n" + configUrl);
            }
        });
    }

    private static void showToast(Context context, String msg) {
        if (context instanceof Activity) {
            Activity activity = (Activity)context;
            activity.runOnUiThread(() -> {
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            });
        }
    }
}
