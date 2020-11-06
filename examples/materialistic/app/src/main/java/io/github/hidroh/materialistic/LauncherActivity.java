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
    static private String defaultConfig = "{\n" +
            "  version: 0,\n" +
            "  endpoint: \"\",\n" +
            "  requestSecurity: \"http\",\n" +
            "  httpMethod: \"get\",\n" +
            "  namespace: \"MyNamespace\",\n" +
            "  appId: \"MyAppId\"\n" +
            "}";

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

        // TODO: Check if this code has to go in Application class
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("Configuration", Context.MODE_PRIVATE);
        String config = sharedPreferences.getString("config", defaultConfig);
        new Configurator(context).process(config);

        handler.post(downloadConfigScheduling);

        finish();
    }

    private Runnable downloadConfigScheduling = new Runnable() {
        @Override
        public void run() {
            new DownloadConfigTask(new Configurator(getApplicationContext()))
                    .execute("https://gist.githubusercontent.com/AlexBenny/ec9c0f6b13fb8ca56188a53097583978/raw/config.json");
            handler.postDelayed(downloadConfigScheduling, 5000);
        }
    };
}
