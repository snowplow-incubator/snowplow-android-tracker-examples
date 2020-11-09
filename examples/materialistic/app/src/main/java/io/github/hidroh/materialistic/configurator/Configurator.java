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

    static private String defaultConfig = "{\n" +
            "  version: 0,\n" +
            "  endpoint: \"\",\n" +
            "  requestSecurity: \"http\",\n" +
            "  httpMethod: \"get\",\n" +
            "  namespace: \"MyNamespace\",\n" +
            "  appId: \"MyAppId\"\n" +
            "}";

    public Configurator(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Configuration", Context.MODE_PRIVATE);
    }

    /**
     * Setup the tracker with the cached version of the configuration.
     */
    public void setup() {
        try {
            String jsonConfig = sharedPreferences.getString("config", defaultConfig);
            JSONObject config = new JSONObject(jsonConfig);
            initAndroidTracker(config);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the tracker with the downloaded version of the configuration
     * when the configuration version is different by the cached one.
     */
    public void setup(String jsonConfig) {
        try {
            JSONObject config = new JSONObject(jsonConfig);
            int passedConfigVersion = config.getInt("version");
            int cachedConfigVersion = sharedPreferences.getInt("version", -1);
            if (passedConfigVersion == cachedConfigVersion) {
                return;
            }
            initAndroidTracker(config);
            sharedPreferences.edit()
                    .putInt("version", passedConfigVersion)
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
