package io.github.hidroh.materialistic.configurator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;
import com.snowplowanalytics.snowplow.tracker.utils.LogLevel;

import org.json.JSONException;
import org.json.JSONObject;

public class Configurator {
    static private String TAG = Configurator.class.getSimpleName();

    private Context context;
    private SharedPreferences sharedPreferences;

    final static private String DEFAULT_CONFIG = "{\n" +
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
            String jsonConfig = sharedPreferences.getString("config", DEFAULT_CONFIG);
            JSONObject config = new JSONObject(jsonConfig);
            initAndroidTracker(config);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setup the tracker with the downloaded version of the configuration
     * when the configuration version is different by the cached one.
     * @param jsonConfig the configuration settings
     * @return whether the configuration has been accepted
     */
    public boolean setup(String jsonConfig) {
        try {
            JSONObject config = new JSONObject(jsonConfig);
            int passedConfigVersion = config.getInt("version");
            int cachedConfigVersion = sharedPreferences.getInt("version", -1);
            if (passedConfigVersion == cachedConfigVersion) {
                Log.d(TAG, "Same config version: " + passedConfigVersion);
                return false;
            }
            initAndroidTracker(config);
            sharedPreferences.edit()
                    .putInt("version", passedConfigVersion)
                    .putString("config", jsonConfig)
                    .apply();
            Log.d(TAG, "Config version saved: " + passedConfigVersion);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void initAndroidTracker(JSONObject config) throws JSONException {
        Tracker.close();

        String endpoint = config.getString("endpoint");
        HttpMethod method = "get".equals(config.getString("httpMethod")) ? HttpMethod.GET : HttpMethod.POST;
        RequestSecurity security = "http".equals(config.getString("requestSecurity")) ? RequestSecurity.HTTP : RequestSecurity.HTTPS;
        String namespace = config.getString("namespace");
        String appId = config.getString("appId");
        String userId = config.optString("userId");

        boolean base64 = config.optBoolean("base64", false);
        boolean sessionContext = config.optBoolean("sessionContext", false);
        boolean mobileContext = config.optBoolean("mobileContext", false);
        boolean geoLocationContext = config.optBoolean("geoLocationContext", false);
        boolean applicationCrash = config.optBoolean("applicationCrash", false);
        boolean trackerDiagnostic = config.optBoolean("trackerDiagnostic", false);
        boolean lifecycleEvents = config.optBoolean("lifecycleEvents", false);
        boolean screenviewEvents = config.optBoolean("screenviewEvents", false);
        boolean screenContext = config.optBoolean("screenContext", false);
        boolean installTracking = config.optBoolean("installTracking", false);
        boolean applicationContext = config.optBoolean("applicationContext", false);

        int foregroundTimeout = config.optInt("foregroundTimeout", 30);
        int backgroundTimeout = config.optInt("backgroundTimeout", 30);

        Emitter emitter = new Emitter.EmitterBuilder(endpoint, context)
                .method(method)
                .security(security)
                .build();

        Subject subject = new Subject.SubjectBuilder()
                .context(context)
                .build();

        if (userId != null) {
            subject.identifyUser(userId);
        }

        Tracker.init(new Tracker.TrackerBuilder(emitter, namespace, appId, context)
                .level(LogLevel.DEBUG)
                .base64(base64)
                .subject(subject)
                .threadCount(20)
                .sessionContext(sessionContext)
                .mobileContext(mobileContext)
                .geoLocationContext(geoLocationContext)
                .applicationCrash(applicationCrash)
                .trackerDiagnostic(trackerDiagnostic)
                .lifecycleEvents(lifecycleEvents)
                .foregroundTimeout(foregroundTimeout)
                .backgroundTimeout(backgroundTimeout)
                .screenviewEvents(screenviewEvents)
                .screenContext(screenContext)
                .installTracking(installTracking)
                .applicationContext(applicationContext)
                .build());
    }
}
