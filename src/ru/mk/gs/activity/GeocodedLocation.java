package ru.mk.gs.activity;

import android.location.Address;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author mkasumov
 */
public class GeocodedLocation implements Parcelable {
    private Location location;
    private Address address;

    public GeocodedLocation(Location location, Address address) {
        this.location = location;
        this.address = address;
    }

    public Location getLocation() {
        return location;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(location);
        dest.writeValue(address);
    }

    private GeocodedLocation(Parcel parcel) {
        location = Location.CREATOR.createFromParcel(parcel);
        address = Address.CREATOR.createFromParcel(parcel);
    }

    public static final Creator<GeocodedLocation> CREATOR = new Creator<GeocodedLocation>() {
        @Override
        public GeocodedLocation createFromParcel(Parcel source) {
            return new GeocodedLocation(source);
        }

        @Override
        public GeocodedLocation[] newArray(int size) {
            return new GeocodedLocation[size];
        }
    };
}
