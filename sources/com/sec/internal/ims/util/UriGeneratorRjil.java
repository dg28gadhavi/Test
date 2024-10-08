package com.sec.internal.ims.util;

import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;

public class UriGeneratorRjil extends UriGeneratorImpl {
    private static final String LOG_TAG = "UriGeneratorRjil";

    public UriGeneratorRjil(ImsUri.UriType uriType, String str, String str2, ITelephonyManager iTelephonyManager, int i, int i2, ImsProfile imsProfile) {
        super(uriType, str, str2, iTelephonyManager, i, i2, imsProfile);
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType uRIServiceType, String str, String str2) {
        ImsUri.UriType uriType;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: URIServiceType : " + uRIServiceType);
        ImsUri.UriType uriType2 = this.mUriType;
        if (uRIServiceType == UriGenerator.URIServiceType.VOLTE_URI) {
            uriType = ImsUri.UriType.TEL_URI;
        } else {
            uriType = uRIServiceType == UriGenerator.URIServiceType.RCS_URI ? this.mRcsUriType : uriType2;
        }
        if (uriType == ImsUri.UriType.SIP_URI || uriType == ImsUri.UriType.TEL_URI) {
            uriType2 = uriType;
        }
        return getNetworkPreferredUriInternal(str, str2, uriType2, uRIServiceType);
    }

    public ImsUri getNetworkPreferredUri(ImsUri.UriType uriType, String str) {
        ImsUri imsUri;
        if (uriType == ImsUri.UriType.SIP_URI) {
            imsUri = ImsUri.parse("sip:" + str + "@" + this.mDomain);
        } else {
            imsUri = ImsUri.parse("tel:" + str);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri with URI type: " + imsUri);
        return imsUri;
    }

    public ImsUri getNormalizedUri(String str, boolean z) {
        if (str == null) {
            return null;
        }
        String substring = str.contains(";phone-context") ? str.substring(0, str.indexOf(";phone-context")) : str;
        if (!this.mCountryCode.equalsIgnoreCase("in") || substring.length() != 12 || substring.startsWith("+") || !substring.startsWith("91")) {
            return super.getNormalizedUri(str, z);
        }
        return ImsUri.parse("tel:" + substring + ";phone-context=" + this.mDomain);
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToTelUri(ImsUri imsUri, String str) {
        IMSLog.s(LOG_TAG, "convert input: " + imsUri + " cc: " + str);
        if (imsUri.getUriType() != ImsUri.UriType.TEL_URI && UriUtil.hasMsisdnNumber(imsUri)) {
            String msisdnNumber = UriUtil.getMsisdnNumber(imsUri);
            if (msisdnNumber != null && msisdnNumber.contains("phone-context")) {
                msisdnNumber = msisdnNumber.substring(0, msisdnNumber.indexOf(";phone-context"));
            }
            if (msisdnNumber == null) {
                return null;
            }
            if (this.mCountryCode.equalsIgnoreCase("in") && msisdnNumber.length() == 12 && !msisdnNumber.startsWith("+") && msisdnNumber.startsWith("91")) {
                return ImsUri.parse("tel:" + msisdnNumber + ";phone-context=" + this.mDomain);
            }
        }
        return super.convertToTelUri(imsUri, str);
    }
}
