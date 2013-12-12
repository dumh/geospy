package ru.mk.gs.config;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.util.Log;
import ru.mk.gs.GSApplication;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author mkasumov
 */
public class ConfigManager extends ContextWrapper {

    public static final String URL = "url";
    public static final String CREDENTIALS = "credentials";
    public static final String MY_SUBJECT = "mySubject";
    public static final String TRACKED_SUBJECTS = "trackedSubjects";

    private GSApplication app;
    private Config config;

    public ConfigManager(GSApplication app) {
        super(app);
        this.app = app;
    }

    public Config getConfig() {
        return config.clone();
    }

    public boolean isLoaded() {
        return config != null;
    }

    // ----------------------------------------------------------------------------------------------------------------

    private void setNewConfig(Config newConfig) {
        if (newConfig.getUrl() == null) {
            throw new RuntimeException("Missing URL");
        }
        if (newConfig.getMySubject() == null) {
            throw new RuntimeException("My subject not specified");
        }
        this.config = newConfig;
    }

    public void loadFromProperties(Properties config) {
        final Config newConfig = new Config();
        try {
            newConfig.setUrl(new URL(config.getProperty(URL)));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid or missing URL", e);
        }
        newConfig.setCredentials(config.getProperty(CREDENTIALS));
        newConfig.setMySubject(config.getProperty(MY_SUBJECT));
        newConfig.setTrackedSubjects(explode(config.getProperty(TRACKED_SUBJECTS)));

        setNewConfig(newConfig);
        afterLoaded(true);
    }

    private List<String> explode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    private String implode(List<String> list) {
        if (list == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(list.size() * 5);
        for (String value : list) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(value);
        }
        return sb.toString();
    }

    public void loadFromPreferences() {
        final SharedPreferences pref = GSApplication.getSharedPreferences(this);
        final Config newConfig = new Config();
        try {
            newConfig.setUrl(new URL(pref.getString(URL, null)));
        } catch (MalformedURLException e) {
            Log.e("GS", "URL in preferences is not found or invalid", e);
        }
        newConfig.setCredentials(pref.getString(CREDENTIALS, null));
        newConfig.setMySubject(pref.getString(MY_SUBJECT, null));
        newConfig.setTrackedSubjects(explode(pref.getString(TRACKED_SUBJECTS, null)));

        setNewConfig(newConfig);
        afterLoaded(false);
    }

    public void saveToPreferences() {
        if (config == null) {
            return;
        }
        final SharedPreferences pref = GSApplication.getSharedPreferences(this);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(URL, config.getUrl().toString());
        editor.putString(CREDENTIALS, config.getCredentials());
        editor.putString(MY_SUBJECT, config.getMySubject());
        editor.putString(TRACKED_SUBJECTS, implode(config.getTrackedSubjects()));
        editor.commit();
    }

    private void afterLoaded(boolean saveToPreferences) {
        if (saveToPreferences) {
            saveToPreferences();
        }
        app.onConfigLoaded();
    }

}
