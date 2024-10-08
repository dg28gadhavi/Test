package com.sec.internal.ims.servicemodules.gls;

import android.location.Location;
import android.net.Uri;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.HashMap;

public class GlsGeoSmsParser {
    private static final String LOG_TAG = "GlsGeoSmsParser";

    public String getGlsExtInfo(String str) {
        long j;
        String str2 = LOG_TAG;
        IMSLog.s(str2, "body=" + str);
        try {
            GlsData parse = parse(str);
            if (parse == null) {
                Log.e(str2, "getGlsExtInfo, data is null.");
                return null;
            }
            GlsValidityTime validityDate = parse.getValidityDate();
            Location location = parse.getLocation();
            LocationType locationType = parse.getLocationType();
            String label = locationType == LocationType.OWN_LOCATION ? "" : parse.getLabel();
            if (validityDate != null) {
                if (validityDate.getValidityDate() != null) {
                    j = validityDate.getValidityDate().getTime();
                    return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + j + "," + label + "," + locationType.toString();
                }
            }
            j = parse.getDate() != null ? parse.getDate().getTime() : 0;
            return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + j + "," + label + "," + locationType.toString();
        } catch (Exception e) {
            IMSLog.s(LOG_TAG, e.toString());
            return null;
        }
    }

    public GlsData parse(String str) throws Exception {
        double d;
        LocationType locationType;
        Log.d(LOG_TAG, "parse enter: geoSms = " + str);
        try {
            Uri parse = Uri.parse(str);
            if (!parse.getScheme().equals("geo")) {
                return null;
            }
            String[] split = parse.getSchemeSpecificPart().split(";");
            String[] split2 = split[0].split(",");
            HashMap hashMap = new HashMap();
            hashMap.put("crs", (Object) null);
            hashMap.put("u", (Object) null);
            hashMap.put("rcs-l", (Object) null);
            for (int i = 1; i < split.length; i++) {
                String[] split3 = split[i].split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                if (hashMap.containsKey(split3[0])) {
                    hashMap.put(split3[0], split3[1]);
                }
            }
            String str2 = (String) hashMap.get("u");
            if (str2 == null) {
                d = 0.0d;
            } else {
                d = Double.valueOf(str2).doubleValue();
            }
            double doubleValue = Double.valueOf(split2[0]).doubleValue();
            double doubleValue2 = Double.valueOf(split2[1]).doubleValue();
            Location location = new Location("passive");
            location.setLatitude(doubleValue);
            location.setLongitude(doubleValue2);
            location.setAccuracy((float) d);
            if (!"gcj02".equals(hashMap.get("crs"))) {
                Log.d(LOG_TAG, "parse fail: crs is not gcj02.");
                return null;
            }
            String str3 = (String) hashMap.get("rcs-l");
            if (str3 != null) {
                locationType = LocationType.OTHER_LOCATION;
            } else {
                locationType = LocationType.OWN_LOCATION;
            }
            LocationType locationType2 = locationType;
            Log.d(LOG_TAG, "parse success: location = " + location + " label = " + str3);
            return new GlsData((String) null, (ImsUri) null, location, locationType2, (Date) null, str3, (GlsValidityTime) null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
