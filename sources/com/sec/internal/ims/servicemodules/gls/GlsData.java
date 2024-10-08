package com.sec.internal.ims.servicemodules.gls;

import android.location.Location;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import java.util.Date;

public class GlsData {
    private final Date mDate;
    private final String mId;
    private final String mLabel;
    private final Location mLocation;
    private final LocationType mLocationType;
    private final ImsUri mUri;
    private final GlsValidityTime mValidityDate;

    public GlsData(String str, ImsUri imsUri, Location location, LocationType locationType, Date date, String str2, GlsValidityTime glsValidityTime) {
        this.mId = str;
        this.mUri = imsUri;
        this.mLocation = location;
        this.mLocationType = locationType;
        this.mDate = date;
        this.mLabel = str2;
        this.mValidityDate = glsValidityTime;
    }

    public String getId() {
        return this.mId;
    }

    public ImsUri getSender() {
        return this.mUri;
    }

    public Location getLocation() {
        return this.mLocation;
    }

    public LocationType getLocationType() {
        return this.mLocationType;
    }

    public Date getDate() {
        return this.mDate;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public GlsValidityTime getValidityDate() {
        return this.mValidityDate;
    }

    public String toString() {
        return "GlsData [" + "sender=" + this.mUri + ", id=" + this.mId + ", " + this.mLocation.toString() + ", location type=" + this.mLocationType + ", shared date=" + this.mDate.toString() + ", label=" + this.mLabel.toString() + ", validity date=" + this.mValidityDate.toString() + ']';
    }
}
