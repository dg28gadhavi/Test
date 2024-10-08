package com.sec.internal.ims.core;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

class GeoLocationUtility {
    private static final float ATT_SCC_E911_MAX_LOCATIONFIX_ACCURACY = 150.0f;
    private static final long ATT_SCC_E911_MAX_LOCATIONFIX_AGE = 1800;
    private static final String LOG_TAG = "GeoLocationUtility";
    private static LocationInfo mLocationInfo;

    GeoLocationUtility() {
    }

    private static synchronized void updateLocationInfo(LocationInfo locationInfo) {
        synchronized (GeoLocationUtility.class) {
            mLocationInfo = locationInfo;
        }
    }

    static LocationInfo constructData(String str, String str2) {
        String str3 = LOG_TAG;
        IMSLog.s(str3, "constructData, countryIso : " + str);
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        LocationInfo locationInfo = mLocationInfo;
        if (locationInfo != null && str.equalsIgnoreCase(locationInfo.mCountry)) {
            return mLocationInfo;
        }
        LocationInfo locationInfo2 = new LocationInfo();
        long currentTimeMillis = System.currentTimeMillis();
        locationInfo2.mProviderType = str2;
        locationInfo2.mRetentionExpires = getInternetDateTimeFormat(currentTimeMillis);
        locationInfo2.mSRSName = "urn:ogc:def:crs:EPSG::4326";
        locationInfo2.mRadiusUOM = "urn:ogc:def:uom:EPSG::9001";
        locationInfo2.mOS = "Android " + Build.VERSION.RELEASE;
        locationInfo2.mLocationTime = String.valueOf(currentTimeMillis / 1000);
        locationInfo2.mDeviceId = "urn:uuid:" + UUID.randomUUID().toString();
        locationInfo2.mCountry = str.toUpperCase(Locale.US);
        updateLocationInfo(locationInfo2);
        return locationInfo2;
    }

    static LocationInfo constructData(Location location, String str, Context context, boolean z, int i) {
        List<Address> list;
        String str2;
        String str3;
        String str4 = str;
        double latitude = location.getLatitude();
        String str5 = Location.convert(latitude, 2) + (latitude > 0.0d ? "N" : "S");
        double longitude = location.getLongitude();
        String str6 = Location.convert(longitude, 2) + (longitude > 0.0d ? "E" : "W");
        float accuracy = location.getAccuracy();
        if (i == 90) {
            accuracy = (float) (((double) accuracy) * 1.65d);
        }
        long time = location.getTime() / 1000;
        float verticalAccuracyMeters = location.getVerticalAccuracyMeters();
        String str7 = LOG_TAG;
        IMSLog.s(str7, "constructData: providerType=" + str4 + " slatitude=" + str5 + " slongitude=" + str6 + " accuracy " + accuracy + " verticalAxis " + verticalAccuracyMeters + " orientation " + 0.0f + " locationtime " + time);
        NumberFormat instance = NumberFormat.getInstance();
        instance.setMinimumFractionDigits(5);
        instance.setMaximumFractionDigits(340);
        LocationInfo locationInfo = new LocationInfo();
        locationInfo.mLatitude = instance.format(latitude);
        locationInfo.mLongitude = instance.format(longitude);
        locationInfo.mAltitude = String.format("%.1f", new Object[]{Double.valueOf(location.getAltitude())});
        locationInfo.mAccuracy = String.valueOf(accuracy);
        locationInfo.mVerticalAxis = String.valueOf(verticalAccuracyMeters);
        locationInfo.mOrientation = String.valueOf(0.0f);
        locationInfo.mProviderType = str4;
        locationInfo.mRetentionExpires = getInternetDateTimeFormat(location.getTime());
        locationInfo.mSRSName = "urn:ogc:def:crs:EPSG::4326";
        locationInfo.mRadiusUOM = "urn:ogc:def:uom:EPSG::9001";
        locationInfo.mOS = "Android " + Build.VERSION.RELEASE;
        locationInfo.mLocationTime = String.valueOf(time);
        locationInfo.mDeviceId = "urn:uuid:" + UUID.randomUUID().toString();
        if (z) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            if (Geocoder.isPresent()) {
                list = getAddressUsingGeocoder(latitude, longitude, geocoder);
            } else {
                Log.e(str7, "geocoder is not created");
                list = null;
            }
            if (list != null && !list.isEmpty()) {
                Address address = list.get(0);
                locationInfo.mCountry = address.getCountryCode() != null ? address.getCountryCode().toUpperCase(Locale.US) : "";
                locationInfo.mA1 = address.getAdminArea();
                locationInfo.mA3 = address.getLocality();
                if (address.getThoroughfare() != null) {
                    str2 = address.getThoroughfare();
                } else {
                    str2 = address.getSubLocality();
                }
                locationInfo.mA6 = str2;
                if (address.getFeatureName() != null) {
                    str3 = address.getFeatureName();
                } else {
                    str3 = address.getPremises();
                }
                locationInfo.mHNO = str3;
                locationInfo.mPC = address.getPostalCode();
            }
        } else {
            IMSLog.d(str7, "Not fetching address from GeoCoder for VZW as its not required for 911 call");
        }
        IMSLog.s(str7, "constructData getAddressUsingGeocoder: mCountry=" + locationInfo.mCountry + " mA1=" + locationInfo.mA1 + " mA3=" + locationInfo.mA3 + " mA6=" + locationInfo.mA6 + " mHNO=" + locationInfo.mHNO + " mPC=" + locationInfo.mPC);
        String str8 = locationInfo.mLatitude;
        if (!(str8 == null || locationInfo.mLongitude == null)) {
            locationInfo.mLatitude = str8.replace(",", ".");
            locationInfo.mLongitude = locationInfo.mLongitude.replace(",", ".");
        }
        updateLocationInfo(locationInfo);
        return locationInfo;
    }

    static String getInternetDateTimeFormat(long j) {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (j != 0) {
            instance.setTimeInMillis(j);
        }
        instance.add(11, 24);
        int i = instance.get(1);
        int i2 = instance.get(5);
        int i3 = instance.get(11);
        int i4 = instance.get(12);
        int i5 = instance.get(13);
        return String.format("%2d-%02d-%02dT%02d%s%02d%s%02d.%02dZ", new Object[]{Integer.valueOf(i), Integer.valueOf(instance.get(2) + 1), Integer.valueOf(i2), Integer.valueOf(i3), ":", Integer.valueOf(i4), ":", Integer.valueOf(i5), Integer.valueOf(instance.get(14) / 100)});
    }

    static List<Address> getAddressUsingGeocoder(double d, double d2, Geocoder geocoder) {
        try {
            return geocoder.getFromLocation(d, d2, 1);
        } catch (IOException | IllegalArgumentException e) {
            String str = LOG_TAG;
            IMSLog.i(str, "getAddressUsingGeocoder: " + e.getMessage());
            return null;
        }
    }

    public static boolean isLocationValid(Location location) {
        if ((location.getTime() - System.currentTimeMillis()) / 1000 > ATT_SCC_E911_MAX_LOCATIONFIX_AGE) {
            String str = LOG_TAG;
            Log.d(str, "invalid location time expired location.time = " + location.getTime() + " current time = " + System.currentTimeMillis());
            return false;
        } else if (!location.hasAccuracy() || location.getAccuracy() <= ATT_SCC_E911_MAX_LOCATIONFIX_ACCURACY) {
            return true;
        } else {
            String str2 = LOG_TAG;
            Log.d(str2, "Location received is not valid, hence not notifying acc = " + location.getAccuracy());
            return false;
        }
    }
}
