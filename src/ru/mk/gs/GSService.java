package ru.mk.gs;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;
import ru.mk.gs.http.HttpService;
import ru.mk.gs.http.HttpServicePool;

import static ru.mk.gs.GSApplication.isEmulation;

/**
 * @author mkasumov
 */
public class GSService extends IntentService {

    private String mySubject;
    private Handler handler;

    public GSService() {
        super("GS");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mySubject = ((GSApplication)getApplication()).getMySubject();
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final Location location = getMyLocation();
            if (location != null) {
                sendMyLocation(location);
            }
        } catch (Exception e) {
            Log.e("GS", "Error in GSService", e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    private Location getMyLocation() {
        return isEmulation() ? getMockLocation() : getMyRealLocation();
    }

    private Location getMyRealLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
    }

    private Location getMockLocation() {
        final Location l = newLocation("gps", Math.random() * 50, Math.random() * 40);
        l.setAccuracy((float)Math.random()*50);
        l.setSpeed((float)Math.random()*10);
        return l;
    }

    private Location newLocation(String provider, double latitude, double longitude) {
        final Location l = new Location(provider);
        l.setLatitude(latitude);
        l.setLongitude(longitude);
        return l;
    }

    // ----------------------------------------------------------------------------------------------------------------

    private static final String HASH = "hash";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    private int locationHashCode(Location l) {
        int result;
        long temp;
        result = l.getProvider().hashCode();
        temp = Double.doubleToLongBits(l.getLatitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(l.getLongitude());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (l.getAccuracy() != +0.0f ? Float.floatToIntBits(l.getAccuracy()) : 0);
        return result;
    }

    private void setPrevLocation(Location l) {
        final SharedPreferences pref = GSApplication.getSharedPreferences(this);
        final SharedPreferences.Editor editor = pref.edit();
        editor.putInt(HASH, locationHashCode(l));
        editor.putFloat(LATITUDE, (float)l.getLatitude());
        editor.putFloat(LONGITUDE, (float)l.getLongitude());
        editor.commit();
    }

    private static final float DISTANCE_THRESHOLD = 20;

    private boolean hasLocationChanged(Location l) {
        final SharedPreferences pref = GSApplication.getSharedPreferences(this);
        if (!pref.contains(HASH)) {
            return true;
        }
        if (pref.getInt(HASH, 0) == locationHashCode(l)) {
            return false;
        }
        final Location prevLocation = newLocation(null, pref.getFloat(LATITUDE, 0), pref.getFloat(LONGITUDE, 0));
        return l.distanceTo(prevLocation) > DISTANCE_THRESHOLD;
    }

    // ----------------------------------------------------------------------------------------------------------------

    private void sendMyLocation(final Location location) {
        HttpServicePool.withService(new HttpServicePool.ServiceAction<Object>() {
            @Override
            public Object doAction(HttpService service) {
                if (hasLocationChanged(location)) {
                    if (service.sendLocation(mySubject, location)) {
                        setPrevLocation(location);
                    }
                }
                return null;
            }
        });
    }

}

