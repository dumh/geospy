package ru.mk.gs;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import ru.mk.gs.config.ConfigManager;
import ru.mk.gs.http.HttpServicePool;
import ru.mk.gs.receiver.ScheduleGSReceiver;

import java.util.Collections;
import java.util.List;

/**
 * @author mkasumov
 */
public class GSApplication extends Application {

    private ConfigManager configManager;

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        configManager = new ConfigManager(this);
        try {
            configManager.loadFromPreferences();
        } catch (Exception e) {
            Log.e("GS", "Error loading config from preferences", e);
        }
    }

    public void onConfigLoaded() {
        HttpServicePool.init(configManager);
        sendBroadcast(new Intent(this, ScheduleGSReceiver.class));
    }

    public static boolean isEmulation() {
        return Build.PRODUCT != null && Build.PRODUCT.contains("sdk");
    }

    public List<String> getSubjects() {
        if (!configManager.isLoaded()) {
            return Collections.emptyList();
        }
        return configManager.getConfig().getTrackedSubjects();
    }

    public String getMySubject() {
        if (!configManager.isLoaded()) {
            return null;
        }
        if (isEmulation()) {
            return "emu";
        }
        return configManager.getConfig().getMySubject();
    }

    public static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences("ru.mk.gs.PREV_LOCATION", Context.MODE_PRIVATE);
    }
}