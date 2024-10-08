package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class ImIconData {
    private String mIconLocation;
    private final IconType mIconType;
    private String mIconUri;
    private final ImsUri mParticipant;
    private final Date mTimestamp;

    public enum IconType {
        ICON_TYPE_NONE,
        ICON_TYPE_FILE,
        ICON_TYPE_URI
    }

    public ImIconData(IconType iconType, ImsUri imsUri, Date date, String str, String str2) {
        this.mIconType = iconType;
        this.mParticipant = imsUri;
        this.mTimestamp = date;
        this.mIconLocation = str;
        this.mIconUri = str2;
    }

    public IconType getIconType() {
        return this.mIconType;
    }

    public String getIconLocation() {
        return this.mIconLocation;
    }

    public void setIconLocation(String str) {
        this.mIconLocation = str;
    }

    public String getIconUri() {
        return this.mIconUri;
    }

    public ImsUri getParticipant() {
        return this.mParticipant;
    }

    public Date getTimestamp() {
        return this.mTimestamp;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImIconData imIconData = (ImIconData) obj;
        if (!this.mIconLocation.equals(imIconData.mIconLocation) || !this.mParticipant.equals(imIconData.mParticipant) || !this.mTimestamp.equals(imIconData.mTimestamp)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "ImIconData [mIconType=" + this.mIconType + ", mParticipant=" + IMSLog.checker(this.mParticipant) + ", mTimestamp=" + this.mTimestamp + ", mIconLocation=" + this.mIconLocation + ", mIconUri=" + this.mIconUri + ']';
    }
}
