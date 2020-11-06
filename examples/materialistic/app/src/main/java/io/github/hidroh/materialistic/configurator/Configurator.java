package io.github.hidroh.materialistic.configurator;

import android.content.Context;
import android.content.SharedPreferences;

import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;

import org.json.JSONException;
import org.json.JSONObject;

public class Configurator {
    private Context context;
    private SharedPreferences sharedPreferences;


    public Configurator(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Configuration", Context.MODE_PRIVATE);
    }

    public void process(String jsonConfig) {
        try {
            JSONObject config = new JSONObject(jsonConfig);
            int newVersion = config.getInt("version");
            int oldVersion = sharedPreferences.getInt("version", -1);
            if (newVersion == oldVersion) {
                return;
            }
            initAndroidTracker(config);
            sharedPreferences.edit()
                    .putInt("version", newVersion)
                    .putString("config", jsonConfig)
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initAndroidTracker(JSONObject config) throws JSONException {
        Tracker.close();

        String endpoint = config.getString("endpoint");
        HttpMethod method = config.getString("httpMethod") == "post" ? HttpMethod.POST : HttpMethod.GET;
        RequestSecurity security = config.getString("requestSecurity") == "https" ? RequestSecurity.HTTPS : RequestSecurity.HTTP;
        Emitter emitter = new Emitter.EmitterBuilder(endpoint, context)
                .method(method)
                .security(security)
                .build();

        Subject subject = new Subject.SubjectBuilder()
                .context(context)
                .build();

        String namespace = config.getString("namespace");
        String appId = config.getString("appId");
        Tracker.init(new Tracker.TrackerBuilder(emitter, namespace, appId, context)
                .base64(false)
                .subject(subject)
                .threadCount(20)
                .sessionContext(true)
                .mobileContext(true)
                .geoLocationContext(true)
                .applicationCrash(true)
                .trackerDiagnostic(true)
                .lifecycleEvents(true)
                .foregroundTimeout(60)
                .backgroundTimeout(30)
                .screenviewEvents(true)
                .screenContext(true)
                .installTracking(true)
                .applicationContext(false)
                .build());
    }
}
