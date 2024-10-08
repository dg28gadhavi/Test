package com.sec.internal.ims.util;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UriGeneratorImpl extends UriGenerator {
    private static final String LOG_TAG = "UriGenerator";
    protected String mCountryCode;
    protected String mDomain;
    protected String mOwnAreaCode;
    protected int mPhoneId;
    protected ImsProfile mProfile;
    protected int mRat;
    protected ImsUri.UriType mRcsUriType;
    protected int mSubscriptionId;
    protected ITelephonyManager mTelephonyManager;
    protected ImsUri.UriType mUriType;
    protected ImsUri.UriType mVoLTEUriType;

    public UriGeneratorImpl(ImsUri.UriType uriType, String str, String str2, ITelephonyManager iTelephonyManager, int i, int i2, ImsProfile imsProfile) {
        this.mRat = 0;
        this.mTelephonyManager = iTelephonyManager;
        this.mSubscriptionId = i;
        this.mUriType = uriType;
        this.mCountryCode = str;
        this.mDomain = str2;
        this.mPhoneId = i2;
        this.mProfile = imsProfile;
        Log.d(LOG_TAG, "mDomain " + this.mDomain);
    }

    public UriGeneratorImpl(ImsUri.UriType uriType, String str, String str2, ITelephonyManager iTelephonyManager, int i, int i2) {
        this(uriType, str, str2, iTelephonyManager, i, i2, (ImsProfile) null);
    }

    public ImsUri normalize(ImsUri imsUri) {
        IMSLog.s(LOG_TAG, "normalize " + imsUri);
        if (imsUri == null) {
            Log.d(LOG_TAG, "normalize: uri is null");
            return null;
        } else if (ChatbotUriUtil.hasUriBotPlatform(imsUri, this.mPhoneId)) {
            Log.d(LOG_TAG, "Do not normalize chatbot service ID");
            return imsUri;
        } else if (imsUri.getUriType() != ImsUri.UriType.SIP_URI || imsUri.toString().contains("user=phone") || !ChatbotUriUtil.isKnownBotServiceId(imsUri, this.mPhoneId)) {
            ImsUri convertToTelUri = convertToTelUri(imsUri, this.mCountryCode);
            if (convertToTelUri == null) {
                return imsUri;
            }
            convertToTelUri.setUserParam(PhoneConstants.PHONE_KEY);
            return convertToTelUri;
        } else {
            IMSLog.s(LOG_TAG, "Service Id exists in mBotServiceIdMap, so don't normalize it.");
            return imsUri;
        }
    }

    public ImsUri getNormalizedUri(String str) {
        return getNormalizedUri(str, false);
    }

    public ImsUri getNormalizedUri(String str, boolean z) {
        String str2 = this.mCountryCode;
        if (str == null) {
            return null;
        }
        if (str.contains("#") || str.contains("*") || str.contains(",") || str.contains("N")) {
            Log.d(LOG_TAG, "getNormalizedUri: invalid special character in number");
            return null;
        } else if (isRoaming() && !z) {
            return UriUtil.parseNumber(str, getLocalCountryCode());
        } else {
            if (str.startsWith("+")) {
                return ImsUri.parse("tel:" + str);
            }
            ImsProfile imsProfile = this.mProfile;
            if (imsProfile != null && imsProfile.getMnoName().equals(Mno.GOOGLEGC.getName())) {
                str2 = getLocalCountryCode();
            }
            if (str.length() == 7) {
                if (this.mOwnAreaCode == null) {
                    extractOwnAreaCode(this.mTelephonyManager.getMsisdn(this.mSubscriptionId));
                }
                if (this.mOwnAreaCode != null) {
                    str = this.mOwnAreaCode + str;
                    Log.d(LOG_TAG, "local number format, adding own area code " + IMSLog.checker(str));
                }
            }
            return UriUtil.parseNumber(str, str2);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRoaming() {
        return this.mTelephonyManager.isNetworkRoaming() && !this.mCountryCode.equals(getLocalCountryCode());
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    /* access modifiers changed from: protected */
    public String getLocalCountryCode() {
        return this.mTelephonyManager.getNetworkCountryIso();
    }

    public ImsUri swapUriType(ImsUri imsUri) {
        IMSLog.s(LOG_TAG, "swapUriType: [input: " + imsUri + " network preferred type: " + this.mUriType + "]");
        if (imsUri.getUriType() == ImsUri.UriType.SIP_URI) {
            return convertToTelUri(imsUri, this.mCountryCode);
        }
        return convertToSipUri(imsUri, this.mDomain);
    }

    public Set<ImsUri> swapUriType(List<ImsUri> list) {
        HashSet hashSet = new HashSet();
        for (ImsUri swapUriType : list) {
            hashSet.add(swapUriType(swapUriType));
        }
        return hashSet;
    }

    public ImsUri getNetworkPreferredUri(ImsUri imsUri) {
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: [input: " + imsUri + " network preferred type: " + this.mUriType + "]");
        if (imsUri == null) {
            Log.d(LOG_TAG, "uri is null");
            return null;
        } else if (this.mUriType == imsUri.getUriType()) {
            return imsUri;
        } else {
            return getNetworkPreferredUri(imsUri.getMsisdn(), (String) null);
        }
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType uRIServiceType, ImsUri imsUri) {
        return getNetworkPreferredUri(uRIServiceType, imsUri, this.mDomain);
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType uRIServiceType, ImsUri imsUri, String str) {
        ImsUri.UriType uriType;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: input URI: " + imsUri);
        if (imsUri == null) {
            Log.d(LOG_TAG, "uri is null");
            return null;
        } else if (ChatbotUriUtil.hasUriBotPlatform(imsUri, this.mPhoneId)) {
            Log.d(LOG_TAG, "Do not normalize chatbot service ID");
            return imsUri;
        } else {
            if (uRIServiceType == UriGenerator.URIServiceType.VOLTE_URI) {
                uriType = this.mVoLTEUriType;
            } else if (uRIServiceType == UriGenerator.URIServiceType.RCS_URI) {
                uriType = this.mRcsUriType;
            } else {
                uriType = this.mUriType;
            }
            ImsUri.UriType uriType2 = uriType;
            Log.d(LOG_TAG, "URI type: " + uriType2);
            if (uriType2 == imsUri.getUriType()) {
                return imsUri;
            }
            return getNetworkPreferredUriInternal(imsUri.getMsisdn(), (String) null, uriType2, str, uRIServiceType);
        }
    }

    public Set<ImsUri> getNetworkPreferredUri(Set<ImsUri> set) {
        HashSet hashSet = new HashSet();
        for (ImsUri networkPreferredUri : set) {
            hashSet.add(getNetworkPreferredUri(networkPreferredUri));
        }
        return hashSet;
    }

    public Set<ImsUri> getNetworkPreferredUri(UriGenerator.URIServiceType uRIServiceType, Set<ImsUri> set) {
        HashSet hashSet = new HashSet();
        for (ImsUri next : set) {
            if (ChatbotUriUtil.hasUriBotPlatform(next, this.mPhoneId)) {
                hashSet.add(next);
            } else {
                hashSet.add(getNetworkPreferredUri(uRIServiceType, next.getMsisdn(), (String) null));
            }
        }
        return hashSet;
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType uRIServiceType, String str, String str2) {
        ImsUri.UriType uriType;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: URIServiceType : " + uRIServiceType);
        if (uRIServiceType == UriGenerator.URIServiceType.VOLTE_URI) {
            uriType = this.mVoLTEUriType;
        } else if (uRIServiceType == UriGenerator.URIServiceType.RCS_URI) {
            uriType = this.mRcsUriType;
        } else {
            uriType = this.mUriType;
        }
        return getNetworkPreferredUriInternal(str, str2, uriType, uRIServiceType);
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType uRIServiceType, ImsUri.UriType uriType, String str, String str2) {
        return getNetworkPreferredUriInternal(str, str2, uriType, uRIServiceType);
    }

    public ImsUri getNetworkPreferredUri(String str, String str2) {
        return getNetworkPreferredUri(str, str2, this.mDomain);
    }

    public ImsUri getNetworkPreferredUri(String str, String str2, String str3) {
        return getNetworkPreferredUriInternal(str, str2, this.mUriType, str3);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String str, String str2, ImsUri.UriType uriType, UriGenerator.URIServiceType uRIServiceType) {
        return getNetworkPreferredUriInternal(str, str2, uriType, this.mDomain, uRIServiceType);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String str, String str2, ImsUri.UriType uriType, String str3) {
        return getNetworkPreferredUriInternal(str, str2, uriType, str3, (UriGenerator.URIServiceType) null);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String str, String str2, ImsUri.UriType uriType, String str3, UriGenerator.URIServiceType uRIServiceType) {
        ImsUri imsUri;
        if (TextUtils.isEmpty(str3)) {
            str3 = this.mDomain;
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mDomain : " + str3 + ", uriType : " + uriType);
        if (isLocalNumber(str) && !DeviceUtil.getGcfMode()) {
            String networkOperator = (this.mProfile == null || uRIServiceType != UriGenerator.URIServiceType.VOLTE_URI || !NetworkUtil.is3gppPsVoiceNetwork(this.mRat) || !isRoaming() || !"geo-local".equals(this.mProfile.getPolicyOnLocalNumbers())) ? null : this.mTelephonyManager.getNetworkOperator(this.mSubscriptionId);
            if (TextUtils.length(networkOperator) > 4) {
                str = str + ";phone-context=" + networkOperator.substring(0, 3) + "." + networkOperator.substring(3) + ".eps." + str3;
            } else {
                str = str + ";phone-context=" + str3;
            }
        }
        if (uriType == ImsUri.UriType.TEL_URI) {
            imsUri = ImsUri.parse("tel:" + str);
        } else {
            imsUri = getSipUri(str, str3, str2);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + imsUri);
        return imsUri;
    }

    /* access modifiers changed from: protected */
    public ImsUri getSipUri(String str, String str2, String str3) {
        ImsUri parse = ImsUri.parse("sip:" + str + "@" + str2);
        if (parse != null) {
            parse.setUserParam(PhoneConstants.PHONE_KEY);
            if (!TextUtils.isEmpty(str3)) {
                if (str3.startsWith("urn:")) {
                    parse.setParam("gr", str3);
                } else if (str3.length() == 15) {
                    String substring = str3.substring(0, 8);
                    String substring2 = str3.substring(8, 14);
                    String substring3 = str3.substring(14);
                    parse.setParam("gr", "urn:gsma:imei:" + substring + CmcConstants.E_NUM_SLOT_SPLIT + substring2 + CmcConstants.E_NUM_SLOT_SPLIT + substring3);
                } else {
                    parse.setParam("gr", "urn:gsma:imei:" + str3);
                }
            }
        }
        return parse;
    }

    public ImsUri getNetworkPreferredUri(String str) {
        ImsUri networkPreferredUri = getNetworkPreferredUri(this.mUriType, str);
        if (!(this.mUriType == ImsUri.UriType.TEL_URI || networkPreferredUri == null)) {
            networkPreferredUri.setUserParam(PhoneConstants.PHONE_KEY);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + networkPreferredUri);
        return networkPreferredUri;
    }

    public ImsUri getNetworkPreferredUri(ImsUri.UriType uriType, String str) {
        ImsUri imsUri;
        if (uriType == ImsUri.UriType.TEL_URI) {
            imsUri = ImsUri.parse("tel:" + str);
        } else {
            imsUri = ImsUri.parse("sip:" + str + "@" + this.mDomain);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri with URI type: " + imsUri);
        return imsUri;
    }

    public void extractOwnAreaCode(String str) {
        IMSLog.d(LOG_TAG, "Area code available for US operator only");
    }

    public ImsUri getUssdRuri(String str) {
        ImsUri imsUri;
        if (this.mVoLTEUriType == ImsUri.UriType.TEL_URI) {
            imsUri = ImsUri.parse("tel:" + str + ";phone-context=" + this.mDomain);
        } else {
            imsUri = ImsUri.parse("sip:" + str + ";phone-context=" + this.mDomain + "@" + this.mDomain);
        }
        if (imsUri != null) {
            imsUri.setUserParam("dialstring");
        }
        return imsUri;
    }

    public void updateNetworkPreferredUriType(UriGenerator.URIServiceType uRIServiceType, ImsUri.UriType uriType) {
        if (uRIServiceType == UriGenerator.URIServiceType.VOLTE_URI) {
            this.mVoLTEUriType = uriType;
        } else if (uRIServiceType == UriGenerator.URIServiceType.RCS_URI) {
            this.mRcsUriType = uriType;
        }
    }

    public void updateRat(int i) {
        this.mRat = i;
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToTelUri(ImsUri imsUri, String str) {
        IMSLog.s(LOG_TAG, "convert input: " + imsUri + " cc: " + str);
        if (imsUri == null) {
            return null;
        }
        if (imsUri.getUriType() == ImsUri.UriType.TEL_URI) {
            return imsUri;
        }
        if (UriUtil.hasMsisdnNumber(imsUri)) {
            return UriUtil.parseNumber(UriUtil.getMsisdnNumber(imsUri), str);
        }
        IMSLog.s(LOG_TAG, "non Tel-URI convertible uri " + imsUri);
        return null;
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToSipUri(ImsUri imsUri, String str) {
        Log.d(LOG_TAG, "convertToSipUri input: " + imsUri + " domain: " + str);
        if ("sip".equalsIgnoreCase(imsUri.getScheme())) {
            return imsUri;
        }
        String msisdn = imsUri.getMsisdn();
        if (msisdn == null) {
            return null;
        }
        if (isLocalNumber(msisdn)) {
            msisdn = msisdn + ";phone-context=" + str;
        }
        return ImsUri.parse("sip:" + msisdn + "@" + str + ";user=phone");
    }

    /* access modifiers changed from: protected */
    public boolean isLocalNumber(String str) {
        return str != null && !str.startsWith("+");
    }

    /* access modifiers changed from: protected */
    public boolean isSipNumber(String str) {
        return str.lastIndexOf("+") > 0;
    }
}
