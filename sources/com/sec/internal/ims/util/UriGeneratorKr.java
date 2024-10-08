package com.sec.internal.ims.util;

import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;

public class UriGeneratorKr extends UriGeneratorImpl {
    private static final String LOG_TAG = "UriGeneratorKr";
    private String mMnoName = Mno.DEFAULT.getName();

    public UriGeneratorKr(ImsUri.UriType uriType, String str, String str2, ITelephonyManager iTelephonyManager, int i, int i2, ImsProfile imsProfile) {
        super(uriType, str, str2, iTelephonyManager, i, i2, imsProfile);
        Log.d(LOG_TAG, "mDomain " + this.mDomain);
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToTelUri(ImsUri imsUri, String str) {
        IMSLog.s(LOG_TAG, "kr convert input: " + imsUri + " cc: " + str);
        if (imsUri == null) {
            return null;
        }
        if (imsUri.getUriType() == ImsUri.UriType.TEL_URI) {
            return UriUtil.parseNumber(UriUtil.getMsisdnNumber(imsUri), str);
        }
        if (UriUtil.hasMsisdnNumber(imsUri)) {
            return UriUtil.parseNumber(UriUtil.getMsisdnNumber(imsUri), str);
        }
        IMSLog.s(LOG_TAG, "non Tel-URI convertible uri " + imsUri);
        return null;
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType uRIServiceType, String str, String str2) {
        ImsUri.UriType uriType;
        Mno fromName = Mno.fromName(this.mMnoName);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mDomain " + this.mDomain);
        if ((fromName != Mno.KT || !isRoaming() || uRIServiceType == UriGenerator.URIServiceType.RCS_URI) && (fromName != Mno.SKT || !isSipNumber(str))) {
            if (uRIServiceType == UriGenerator.URIServiceType.VOLTE_URI) {
                uriType = this.mVoLTEUriType;
            } else if (uRIServiceType == UriGenerator.URIServiceType.RCS_URI) {
                uriType = this.mRcsUriType;
            } else {
                uriType = this.mUriType;
            }
            return super.getNetworkPreferredUriInternal(str, str2, uriType, uRIServiceType);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: KOR SIP URI");
        if (fromName == Mno.KT && isLocalNumber(str)) {
            str = str + ";phone-context=geo-local." + this.mDomain;
        }
        ImsUri sipUri = getSipUri(str, this.mDomain + ";user=phone", str2);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + sipUri);
        return sipUri;
    }

    public ImsUri getNetworkPreferredUri(String str, String str2) {
        Mno fromName = Mno.fromName(this.mMnoName);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mDomain " + this.mDomain);
        if ((fromName != Mno.KT || !isRoaming()) && (fromName != Mno.SKT || !isSipNumber(str))) {
            return super.getNetworkPreferredUri(str, str2);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: KOR SIP URI");
        if (fromName == Mno.KT && isLocalNumber(str)) {
            str = str + ";phone-context=geo-local." + this.mDomain;
        }
        ImsUri sipUri = getSipUri(str, this.mDomain + ";user=phone", str2);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + sipUri);
        return sipUri;
    }

    public void extractOwnAreaCode(String str) {
        IMSLog.d(LOG_TAG, "extractOwnAreaCode: KOR operator not use OwnAreaCode");
        this.mOwnAreaCode = "";
    }

    /* access modifiers changed from: protected */
    public void setMnoName(String str) {
        this.mMnoName = str;
    }
}
