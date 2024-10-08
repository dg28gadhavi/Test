package com.sec.internal.helper.entitlement.softphone;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.util.Base64;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsProfileLoader;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.entitilement.softphone.ImsNetworkIdentity;
import com.sec.internal.constants.ims.entitilement.softphone.responses.AkaAuthenticationResponse;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SoftphoneAuthUtils {
    private static final String AUTH_NO_ERROR = "DB";
    private static final String LOG_TAG = "SoftphoneAuthUtils";
    private static final Map<String, String> mProdAppKeyMap;
    private static final Map<String, String> mProdAppSecretMap;

    private SoftphoneAuthUtils() {
    }

    static {
        HashMap hashMap = new HashMap();
        mProdAppKeyMap = hashMap;
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.S4, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S4);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.A8, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_A8);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.A4S, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_A4S);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.S7L, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S7L);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.S7PLITE, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S7PLITE);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.A7LITE, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_A7LITE);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.S4LV, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S4LV);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.S8P, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S8P);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.S9P, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S9P);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.S9FE, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_S9FE);
        hashMap.put(SoftphoneNamespaces.SoftphoneModels.A9P, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_KEY_A9P);
        HashMap hashMap2 = new HashMap();
        mProdAppSecretMap = hashMap2;
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.S4, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S4);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.A8, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_A8);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.A4S, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_A4S);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.S7L, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S7L);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.S7PLITE, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S7PLITE);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.A7LITE, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_A7LITE);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.S4LV, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S4LV);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.S8P, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S8P);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.S9P, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S9P);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.S9FE, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_S9FE);
        hashMap2.put(SoftphoneNamespaces.SoftphoneModels.A9P, SoftphoneNamespaces.SoftphoneSettings.PROD_APP_SECRET_A9P);
    }

    public static String setupAppKey(int i, String str) {
        return (1 != i || str == null) ? SoftphoneNamespaces.SoftphoneSettings.STAGE_APP_KEY : mProdAppKeyMap.get(str);
    }

    public static String setupAppSecret(int i, String str) {
        return (1 != i || str == null) ? SoftphoneNamespaces.SoftphoneSettings.STAGE_APP_SECRET : mProdAppSecretMap.get(str);
    }

    public static String[] splitRandAutn(String str) {
        String str2;
        String str3 = LOG_TAG;
        IMSLog.i(str3, "nonce: " + str + " length: " + str.length());
        String str4 = "";
        if (str.length() > 2) {
            int parseInt = Integer.parseInt(str.substring(0, 2), 16) * 2;
            IMSLog.i(str3, "rand length: " + parseInt);
            int i = parseInt + 2;
            String substring = str.substring(2, i);
            if (str.length() > parseInt + 4) {
                String substring2 = str.substring(i);
                int parseInt2 = Integer.parseInt(substring2.substring(0, 2), 16) * 2;
                String substring3 = substring2.substring(2, parseInt2 + 2);
                IMSLog.i(str3, "autn length: " + parseInt2);
                str4 = substring3;
            }
            IMSLog.i(str3, "rand: " + substring);
            IMSLog.i(str3, "autn: " + str4);
            str2 = str4;
            str4 = substring;
        } else {
            IMSLog.i(str3, "wrong nonce format");
            str2 = str4;
        }
        byte[] hexStringToBytes = StrUtil.hexStringToBytes(str4);
        byte[] hexStringToBytes2 = StrUtil.hexStringToBytes(str2);
        String encodeToString = Base64.encodeToString(hexStringToBytes, 2);
        String encodeToString2 = Base64.encodeToString(hexStringToBytes2, 2);
        IMSLog.i(str3, "base64 randStr: " + encodeToString);
        IMSLog.i(str3, "base64 autnStr: " + encodeToString2);
        return new String[]{encodeToString, encodeToString2};
    }

    public static String processAkaAuthenticationResponse(AkaAuthenticationResponse akaAuthenticationResponse) {
        IMSLog.i(LOG_TAG, "processAkaAuthenticationResponse()");
        if (akaAuthenticationResponse == null || akaAuthenticationResponse.mChallengeResponse == null) {
            return "";
        }
        return ((AUTH_NO_ERROR + StrUtil.convertByteToHexWithLength(Base64.decode(akaAuthenticationResponse.mChallengeResponse.mAuthenticationResponse, 0))) + StrUtil.convertByteToHexWithLength(Base64.decode(akaAuthenticationResponse.mChallengeResponse.mCipherKey, 0))) + StrUtil.convertByteToHexWithLength(Base64.decode(akaAuthenticationResponse.mChallengeResponse.mIntegrityKey, 0));
    }

    private static List<ImsProfile> getSoftphoneProfileList(Context context) {
        ArrayList arrayList = new ArrayList();
        Cursor query = context.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/profile"), (String[]) null, "mdmn_type=Softphone", (String[]) null, (String) null);
        if (query != null) {
            if (query.moveToFirst()) {
                do {
                    arrayList.add(ImsProfileLoader.getImsProfileFromRow(context, query));
                } while (query.moveToNext());
            }
            query.close();
        }
        return arrayList;
    }

    public static String getDeviceType(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 3 ? "Tablet" : "Phone";
    }

    private static String getSccLabel(Context context, String str, int i) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "getSccLabel()");
        Cursor query = context.getContentResolver().query(SoftphoneContract.SoftphoneAccount.buildAccountLabelUri(str, (long) i), (String[]) null, (String) null, (String[]) null, (String) null);
        String str3 = "";
        if (query != null) {
            IMSLog.i(str2, "getSccLabel: found " + query.getCount() + " records");
            if (query.moveToFirst()) {
                String string = query.getString(query.getColumnIndex("label"));
                if (string != null && string.length() > 40) {
                    str3 = string.substring(0, 40);
                } else if (string != null) {
                    str3 = string;
                }
            }
            query.close();
        }
        return str3;
    }

    public static ImsProfile createProfileFromTemplate(Context context, ImsNetworkIdentity imsNetworkIdentity, String str, int i) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "createProfileFromTemplate:");
        if (!SimUtil.isSoftphoneEnabled()) {
            return null;
        }
        List<ImsProfile> softphoneProfileList = getSoftphoneProfileList(context);
        if (!softphoneProfileList.isEmpty()) {
            ImsProfile imsProfile = softphoneProfileList.get(0);
            IMSLog.s(str2, "profileTemplate=" + imsProfile);
            if (!imsNetworkIdentity.impiEmpty()) {
                IMSLog.s(str2, "identity: " + imsNetworkIdentity.toString());
                Parcel obtain = Parcel.obtain();
                imsProfile.writeToParcel(obtain, 0);
                obtain.setDataPosition(0);
                ImsProfile imsProfile2 = (ImsProfile) ImsProfile.CREATOR.createFromParcel(obtain);
                imsProfile2.setImpi(imsNetworkIdentity.getImpi());
                imsProfile2.setImpuList(imsNetworkIdentity.getImpu());
                List<String> addressList = imsNetworkIdentity.getAddressList();
                if (addressList != null && addressList.size() > 0) {
                    imsProfile2.setPcscfList(addressList);
                    imsProfile2.setPcscfPreference(2);
                }
                imsProfile2.setAppId(imsNetworkIdentity.getAppId());
                imsProfile2.setDisplayName(getSccLabel(context, str, i));
                obtain.recycle();
                IMSLog.s(str2, "inject profile=" + imsProfile2);
                return imsProfile2;
            }
        }
        return null;
    }
}
