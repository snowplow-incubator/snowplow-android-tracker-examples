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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import java.util.HashMap;

import io.github.hidroh.materialistic.configurator.Configurator;
import io.github.hidroh.materialistic.configurator.DownloadConfigTask;

public class LauncherActivity extends Activity {
    static private String TAG = LauncherActivity.class.getSimpleName();

    private final Preferences.Observable mPreferenceObservable = new Preferences.Observable();

    // Default configuration file
    private String configUrl = "https://mobile-app-config-bucket.s3.us-east-2.amazonaws.com/materialistic-config.json";

    private Handler handler = new Handler();

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

        mPreferenceObservable.subscribe(this, this::onPreferenceChanged,
                R.string.pref_snowplow_url);

        // TODO: Check if this code has to go in Application class
        new Configurator(this).setup();

        String prefConfigUrl = Preferences.getSnowplowConfigURL(this);
        if (prefConfigUrl != null && !prefConfigUrl.isEmpty()) {
            configUrl = prefConfigUrl;
        }
        handler.post(downloadConfigScheduling);

        finish();
    }

    private Runnable downloadConfigScheduling = new Runnable() {
        @Override
        public void run() {
            Context context = getApplicationContext();
            new DownloadConfigTask(context, new Configurator(context))
                    .execute(configUrl);
            handler.postDelayed(downloadConfigScheduling, 10000);
        }
    };

    private void onPreferenceChanged(int key, boolean contextChanged) {
        switch (key) {
            case R.string.pref_snowplow_url:
                handler.removeCallbacks(downloadConfigScheduling);
                configUrl = Preferences.getSnowplowConfigURL(this);
                handler.post(downloadConfigScheduling);
                break;
        }
    }
}
