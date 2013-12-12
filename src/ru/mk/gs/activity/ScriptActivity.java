package ru.mk.gs.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import ru.mk.gs.GSApplication;
import ru.mk.gs.R;
import ru.mk.gs.config.ConfigManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author mkasumov
 */
public class ScriptActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Uri data = intent.getData();
        if (data.getScheme().equals("file")) {
            try {
                processScript(data);
                toast("Script processed successfully");
            } catch (Exception e) {
                Log.e("GS", "Error running GS script: ", e);
                toast("Error running GS script: " + e.getMessage());
            }
        }
        finish();
    }

    private void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private void processScript(Uri scriptUri) {
        final String fileName = scriptUri.getLastPathSegment();
        if (fileName.equalsIgnoreCase(getString(R.string.configFileName))) {
            processConfig(scriptUri);
        }
    }

    private void processConfig(Uri configUri) {
        final Properties config = loadProperties(configUri.getPath());
        final ConfigManager configManager = ((GSApplication) getApplication()).getConfigManager();
        configManager.loadFromProperties(config);
    }

    private Properties loadProperties(String path) {
        try {
            final Properties config = new Properties();
            final FileInputStream in = new FileInputStream(path);
            try {
                config.load(in);
            } finally {
                in.close();
            }
            return config;
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file: " + path, e);
        }
    }
}
