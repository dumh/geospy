package ru.mk.gs.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import ru.mk.gs.GSApplication;
import ru.mk.gs.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author mkasumov
 */
public class MainActivity extends Activity {

    private String subject;
    private HashMap<String, GeocodedLocation> locationsCache = new HashMap<String, GeocodedLocation>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        if (state == null) {
            subject = ((GSApplication) getApplication()).getMySubject();
        } else {
            subject = state.getString("subject");
            locationsCache = (HashMap<String, GeocodedLocation>) state.getSerializable("locations");
        }

        if (subject == null || locationsCache == null) {
            finish();
        }

        setContentView(R.layout.main);
        if (!buildTabs()) {
            finish();
            return;
        }

        if (state == null) {
            update(null);
        } else {
            updateGeocodedLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean buildTabs() {
        final List<String> subjects = ((GSApplication)getApplication()).getSubjects();
        if (subjects == null || subjects.isEmpty()) {
            return false;
        }

        final TabHost tabHost = getTabHost();
        tabHost.setup();

        for (String subject : subjects) {
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(subject);
            tabSpec.setIndicator(subject);
            tabSpec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return getLayoutInflater().inflate(R.layout.subject_tab, null);
                }
            });
            tabHost.addTab(tabSpec);
        }

        tabHost.setCurrentTabByTag(subject);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabTag) {
                onSwitchTab(tabTag);
            }
        });

        return true;
    }

    private TabHost getTabHost() {
        return (TabHost) findViewById(R.id.tabHost);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("subject", subject);
        state.putSerializable("locations", locationsCache);
    }

    public void onSwitchTab(String tabTag) {
        subject = tabTag;
        if (!locationsCache.containsKey(subject)) {
            update(null);
        } else {
            updateGeocodedLocation();
        }
    }

    public void update(View view) {
        new ReceiveSubjectLocationTask(subject, this) {
            protected void onResult(GeocodedLocation gl) {
                locationsCache.put(subject, gl);
                updateGeocodedLocation();
            }
        }.execute();
    }

    private void updateGeocodedLocation() {
        final GeocodedLocation gl = locationsCache.get(subject);
        final Location location = gl.getLocation();
        final Address address = gl.getAddress();
        final View tabView = getTabHost().getCurrentView();

        if (address != null) {
            setViewText(tabView, R.id.locationText, addressToOneLine(address));
        } else {
            setViewText(tabView, R.id.locationText, location.getLatitude() + ", " + location.getLongitude());
        }
        setViewText(tabView, R.id.trackedOnText, locationTimeToString(location));
        setViewText(tabView, R.id.accuracyText, location.getAccuracy()+" m (" + location.getProvider() + ")");
        setViewText(tabView, R.id.speedText, (location.getSpeed()*60*60/1000f)+" km/h");
        tabView.invalidate();

        findViewById(R.id.showMap).setEnabled(true);
    }

    private void setViewText(View root, int textViewId, String text) {
        ((TextView) root.findViewById(textViewId)).setText(text);
    }

    private String addressToOneLine(Address address) {
        String addrOneLine = "";
        for (int i = 0; ; i++) {
            String line = address.getAddressLine(i);
            if (line == null) {
                break;
            }
            if (i > 0) {
                addrOneLine += ", ";
            }
            addrOneLine += line;
        }
        return addrOneLine;
    }

    public void openMap(View view) {
        final GeocodedLocation gl = locationsCache.get(subject);
        if (gl == null) {
            return;
        }
        final Location l = gl.getLocation();
        String label = subject + " at " + locationTimeToString(l) + " by " + l.getProvider();
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=(%s)",
                l.getLatitude(), l.getLongitude(), label);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No Map application found", Toast.LENGTH_SHORT).show();
        }
    }

    private String locationTimeToString(Location l) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());
        return format.format(new Date(l.getTime()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_close:
                finish();
                return true;
            case R.id.action_refresh:
                update(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
