package ru.mk.gs.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;
import ru.mk.gs.http.HttpService;
import ru.mk.gs.http.HttpServicePool;

import java.io.IOException;
import java.util.List;

/**
 * @author mkasumov
 */
public abstract class ReceiveSubjectLocationTask extends AsyncTask<Object, Integer, GeocodedLocation>
        implements DialogInterface.OnCancelListener {

    private final String subject;

    private final Context context;
    private ProgressDialog waitDialog;
    private Throwable throwable;

    public ReceiveSubjectLocationTask(String subject, Context context) {
        this.subject = subject;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        waitDialog = ProgressDialog.show(context, "Wait", "Updating location...", true, true, this);
    }

    @Override
    protected GeocodedLocation doInBackground(Object... params) {
        final Location location = receiveLocation();
        if (location == null) {
            return null;
        }
        final Address address = geocodeLocation(location);
        return new GeocodedLocation(location, address);
    }

    private Location receiveLocation() {
        try {
            return HttpServicePool.withService(new HttpServicePool.ServiceAction<Location>() {
                @Override
                public Location doAction(HttpService service) {
                    return service.receiveLocation(subject);
                }
            });
        } catch (Throwable t) {
            throwable = t;
            return null;
        }
    }

    private Address geocodeLocation(Location l) {
        try {
            final Geocoder geocoder = new Geocoder(context);
            final List<Address> addresses = geocoder.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                return addresses.get(0);
            }
        } catch (IOException e) {
            throwable = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(GeocodedLocation l) {
        waitDialog.dismiss();
        if (l != null) {
            onResult(l);
        } else {
            Toast.makeText(context, "Error occurred: " + errorInfo(throwable), Toast.LENGTH_LONG).show();
        }
    }

    private String errorInfo(Throwable t) {
        String info = "";
        while (t != null) {
            if (!info.isEmpty()) {
                info += "; ";
            }
            info += t.getMessage();
            t = t.getCause();
        }
        return info;
    }

    protected abstract void onResult(GeocodedLocation location);

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
    }
}
