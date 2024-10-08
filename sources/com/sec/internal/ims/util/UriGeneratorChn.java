package com.sec.internal.ims.util;

import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.log.IMSLog;

public class UriGeneratorChn extends UriGeneratorImpl {
    private static final String LOG_TAG = "UriGeneratorChn";

    public UriGeneratorChn(ImsUri.UriType uriType, String str, String str2, ITelephonyManager iTelephonyManager, int i, int i2, ImsProfile imsProfile) {
        super(uriType, str, str2, iTelephonyManager, i, i2, imsProfile);
    }

    public ImsUri getNormalizedUri(String str) {
        return getNormalizedUri(str, false);
    }

    public ImsUri getNormalizedUri(String str, boolean z) {
        if (str == null) {
            return null;
        }
        IMSLog.s(LOG_TAG, "getNormalizedUri for : number: " + str);
        if (str.startsWith("tel:") || str.startsWith("sip:")) {
            IMSLog.s(LOG_TAG, "CMCC number already parsed! number: " + str);
            return ImsUri.parse(str);
        }
        ImsUri normalizedUri = super.getNormalizedUri(str, z);
        if (normalizedUri == null || str.startsWith("12520") || str.startsWith("+8612520")) {
            ImsUri parse = ImsUri.parse("tel:" + str);
            IMSLog.s(LOG_TAG, "CMCC special number parsed result telUri:  " + parse + " number: " + str);
            return parse;
        }
        IMSLog.s(LOG_TAG, "normal number parsed result numberUri:  " + normalizedUri + " number: " + str);
        return normalizedUri;
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToTelUri(ImsUri imsUri, String str) {
        IMSLog.s(LOG_TAG, "convert input: " + imsUri + " cc: " + str);
        PublicAccountUri.setCountryCode(str);
        if (imsUri == null) {
            return null;
        }
        if (imsUri.getUriType() == ImsUri.UriType.TEL_URI) {
            if (!isLocalNumber(UriUtil.getMsisdnNumber(imsUri)) || str == null || !"cn".equalsIgnoreCase(str)) {
                return imsUri;
            }
            IMSLog.s(LOG_TAG, "Not Add country code for input: " + imsUri + " cc: " + str);
            return getNormalizedUri(UriUtil.getMsisdnNumber(imsUri));
        } else if (!UriUtil.hasMsisdnNumber(imsUri)) {
            IMSLog.s(LOG_TAG, "non Tel-URI convertible uri " + imsUri);
            return null;
        } else if (!PublicAccountUri.isPublicAccountUri(imsUri)) {
            return UriUtil.parseNumber(UriUtil.getMsisdnNumber(imsUri), str);
        } else {
            return ImsUri.parse("tel:" + UriUtil.getMsisdnNumber(imsUri));
        }
    }
}
