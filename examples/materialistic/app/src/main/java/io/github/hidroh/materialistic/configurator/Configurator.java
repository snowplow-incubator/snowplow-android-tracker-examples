package io.github.hidroh.materialistic.configurator;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Consumer;

import com.snowplowanalytics.snowplow.Snowplow;
import com.snowplowanalytics.snowplow.configuration.RemoteConfiguration;
import com.snowplowanalytics.snowplow.network.HttpMethod;

import java.util.List;

public class Configurator {
    static private String TAG = Configurator.class.getSimpleName();

    private Context context;

    public Configurator(Context context) {
        this.context = context;
    }

    public void setup() {
        String configUrl = "https://mobile-app-config-bucket.s3.us-east-2.amazonaws.com/materialistic-v2-config.json";
        initAndroidTracker(configUrl);
    }

    private void initAndroidTracker(String configUrl) {
        RemoteConfiguration remoteConfig = new RemoteConfiguration(configUrl, HttpMethod.GET);
        Snowplow.setup(context, remoteConfig, null, new Consumer<List<String>>() {
            @Override
            public void accept(List<String> strings) {
                Log.v(TAG, String.valueOf(strings));
            }
        });
    }
}
