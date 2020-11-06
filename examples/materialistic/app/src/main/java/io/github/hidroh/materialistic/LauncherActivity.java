/*
 * Copyright (c) 2016 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hidroh.materialistic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.snowplowanalytics.snowplow.tracker.DevicePlatforms;
import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Gdpr;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
// import com.snowplowanalytics.snowplow.tracker.utils.LogLevel;
import com.snowplowanalytics.snowplow.tracker.constants.Parameters;
import com.snowplowanalytics.snowplow.tracker.constants.TrackerConstants;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

import java.util.HashMap;
import java.util.Map;


import static com.snowplowanalytics.snowplow.tracker.utils.Util.addToMap;


public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HashMap<String, Class<? extends Activity>> map = new HashMap<>();
        map.put(getString(R.string.pref_launch_screen_value_top), ListActivity.class);
        map.put(getString(R.string.pref_launch_screen_value_best), BestActivity.class);
        map.put(getString(R.string.pref_launch_screen_value_hot), PopularActivity.class);
        map.put(getString(R.string.pref_launch_screen_value_new), NewActivity.class);
        map.put(getString(R.string.pref_launch_screen_value_ask), AskActivity.class);
        map.put(getString(R.string.pref_launch_screen_value_show), ShowActivity.class);
        map.put(getString(R.string.pref_launch_screen_value_jobs), JobsActivity.class);
        map.put(getString(R.string.pref_launch_screen_value_saved), FavoriteActivity.class);
        String launchScreen = Preferences.getLaunchScreen(this);
        startActivity(new Intent(this, map.containsKey(launchScreen) ?
                map.get(launchScreen) : ListActivity.class));

        initAndroidTracker();

        finish();
    }

    // --- Tracker

    private static final String namespace = "SnowplowAndroidTrackerExample";
    private static final String appId = "SnowplowAndroidDemoID";

    /**
     * Builds a Tracker
     */
    private void initAndroidTracker() {
        Tracker.close();

        Emitter emitter = new Emitter.EmitterBuilder("4e6c7fcd35ac.ngrok.io", this.getApplicationContext())
                .tick(1)
                .build();

        Subject subject = new Subject.SubjectBuilder()
                .context(this.getApplicationContext())
                .build();

        Tracker.init(new Tracker.TrackerBuilder(emitter, namespace, appId, this.getApplicationContext())
                .base64(false)
                .platform(DevicePlatforms.Mobile)
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
                .build()
        );

        Map<String, Object> pairs = new HashMap<>();
        addToMap(Parameters.APP_VERSION, "0.1.0", pairs);
        addToMap(Parameters.APP_BUILD, "1", pairs);
        Tracker.instance().enableGdprContext(Gdpr.Basis.CONSENT, "someDocumentId", "0.1.0", "demo document description for gdpr context");
    }


}
