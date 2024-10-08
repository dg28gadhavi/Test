package com.sec.internal.ims.aec.persist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.util.Map;

public class AECStorage {
    private final Context mContext;
    private boolean mIsPushMsgStatus = false;
    private final int mPhoneId;
    protected Map<String, String> mProviderSettings;
    protected final String mSharedPreference;

    public AECStorage(Context context, int i, String str) {
        this.mContext = context;
        this.mPhoneId = i;
        this.mSharedPreference = String.format(AECNamespace.Template.AEC_RESULT, new Object[]{Integer.valueOf(i)});
        this.mProviderSettings = ProviderSettings.getSettingMap(context, i, str);
    }

    private SharedPreferences getSharedPreferences() {
        return this.mContext.getSharedPreferences(this.mSharedPreference, 0);
    }

    private synchronized int getIntValue(String str) {
        try {
        } catch (NumberFormatException unused) {
            return 0;
        }
        return Integer.parseInt(getSharedPreferences().getString(str, "0"));
    }

    private synchronized String getStringValue(String str) {
        return getSharedPreferences().getString(str, "");
    }

    private synchronized void setStringValue(String str, String str2) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putString(str, str2);
        edit.commit();
    }

    private synchronized void setMap(Map<String, String> map) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        for (Map.Entry next : map.entrySet()) {
            edit.putString((String) next.getKey(), (String) next.getValue());
        }
        edit.commit();
    }

    public void setConfiguration(Map<String, String> map) {
        setMap(map);
    }

    public void setDefaultValues(String str) {
        String str2;
        boolean equalsIgnoreCase = getDefaultEntitlement().equalsIgnoreCase("enabled");
        setStringValue("root/vers/version", str);
        String str3 = "0";
        setStringValue("root/vers/validity", str3);
        setStringValue("root/token/token", "");
        setStringValue(AECNamespace.Path.TOKEN_VALIDITY, str3);
        setStringValue(AECNamespace.Path.PUSH_NOTIF_TOKEN, "");
        setStringValue(AECNamespace.Path.VOLTE_ENTITLEMENT_STATUS, equalsIgnoreCase ? "1" : str3);
        if (equalsIgnoreCase) {
            str2 = "1";
        } else {
            str2 = str3;
        }
        setStringValue(AECNamespace.Path.VOWIFI_ENTITLEMENT_STATUS, str2);
        if (equalsIgnoreCase) {
            setStringValue(AECNamespace.Path.VOWIFI_PROV_STATUS, "2");
            setStringValue(AECNamespace.Path.VOWIFI_TC_STATUS, "2");
            setStringValue(AECNamespace.Path.VOWIFI_ADDR_STATUS, "2");
        }
        if (equalsIgnoreCase) {
            str3 = "1";
        }
        setStringValue(AECNamespace.Path.SMSOIP_ENTITLEMENT_STATUS, str3);
    }

    public void setImsi(String str) {
        setStringValue(AECNamespace.Path.IMSI, str);
    }

    public void setAkaToken(String str) {
        setStringValue("root/token/token", str);
    }

    public void setHttpResponse(int i) {
        setStringValue(AECNamespace.Path.RESPONSE, Integer.toString(i));
    }

    public void setVersion(String str) {
        setStringValue("root/vers/version", str);
    }

    public void setNotifToken(String str) {
        setStringValue(AECNamespace.Path.PUSH_NOTIF_TOKEN, str);
    }

    public void setVoLteEntitlementStatus(boolean z) {
        setStringValue(AECNamespace.Path.VOLTE_ENTITLEMENT_STATUS, z ? "1" : "0");
    }

    public void setVoWiFiEntitlementStatus(boolean z) {
        setStringValue(AECNamespace.Path.VOWIFI_ENTITLEMENT_STATUS, z ? "1" : "0");
    }

    public void setSMSoIPEntitlementStatus(boolean z) {
        setStringValue(AECNamespace.Path.SMSOIP_ENTITLEMENT_STATUS, z ? "1" : "0");
    }

    public void setPushMsgStatus(boolean z) {
        this.mIsPushMsgStatus = z;
    }

    public Bundle getStoredConfiguration() {
        Bundle bundle = new Bundle();
        bundle.putInt("phoneId", this.mPhoneId);
        bundle.putInt("version", getVersion());
        bundle.putInt(AECNamespace.BundleData.HTTP_RESPONSE, getHttpResponse());
        bundle.putInt(AECNamespace.BundleData.VOLTE_ENTITLEMENT_STATUS, getVoLTEEntitlementStatus());
        bundle.putString(AECNamespace.BundleData.VOLTE_MESSAGE_FOR_INCOMPATIBLE, getVoLTEMessageForIncompatible());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_ACTIVATION_MODE, getVoWiFiActivationMode());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_ENTITLEMENT_STATUS, getVoWiFiEntitlementStatus());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_PROV_STATUS, getVoWiFiProvStatus());
        bundle.putInt("tc_status", getVoWiFiTcStatus());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_ADDR_STATUS, getVoWiFiAddrStatus());
        bundle.putString(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_URL, getServiceFlowURL());
        bundle.putString(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_USERDATA, getServiceFlowUserData());
        bundle.putString(AECNamespace.BundleData.VOWIFI_MESSAGE_FOR_INCOMPATIBLE, getVoWiFiMessageForIncompatible());
        bundle.putInt(AECNamespace.BundleData.SMSOIP_ENTITLEMENT_STATUS, getSMSoIPEntitlementStatus());
        bundle.putInt(AECNamespace.BundleData.VOLTE_AUTO_ON, getVoLTEAutoOn() ? 1 : 0);
        bundle.putInt(AECNamespace.BundleData.VOWIFI_AUTO_ON, getVoWiFiAutoOn() ? 1 : 0);
        return bundle;
    }

    public String getImsi() {
        return getStringValue(AECNamespace.Path.IMSI);
    }

    public String getAkaToken() {
        return getStringValue("root/token/token");
    }

    public int getHttpResponse() {
        String stringValue = getStringValue(AECNamespace.Path.RESPONSE);
        if (TextUtils.isEmpty(stringValue)) {
            return 0;
        }
        return Integer.parseInt(stringValue);
    }

    public int getVersion() {
        String stringValue = getStringValue("root/vers/version");
        if (TextUtils.isEmpty(stringValue)) {
            stringValue = "0";
        }
        return Integer.parseInt(stringValue);
    }

    public String getNotifToken() {
        return getStringValue(AECNamespace.Path.PUSH_NOTIF_TOKEN);
    }

    public String getTimeStamp() {
        return getStringValue(AECNamespace.Path.TIMESTAMP);
    }

    public int getVersionValidity() {
        return getIntValue("root/vers/validity");
    }

    public int getTokenValidity() {
        return getIntValue(AECNamespace.Path.TOKEN_VALIDITY);
    }

    public int getVoLTEEntitlementStatus() {
        return getIntValue(AECNamespace.Path.VOLTE_ENTITLEMENT_STATUS);
    }

    private String getVoLTEMessageForIncompatible() {
        return getStringValue(AECNamespace.Path.VOLTE_MESSAGE_FOR_INCOMPATIBLE);
    }

    public int getSMSoIPEntitlementStatus() {
        return getIntValue(AECNamespace.Path.SMSOIP_ENTITLEMENT_STATUS);
    }

    public int getVoWiFiActivationMode() {
        return getVoWiFiActivationMode(getVoWiFiEntitlementStatus(), getVoWiFiProvStatus(), getVoWiFiTcStatus(), getVoWiFiAddrStatus());
    }

    public int getVoWiFiEntitlementStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_ENTITLEMENT_STATUS);
    }

    /* access modifiers changed from: protected */
    public String getVoWiFiMessageForIncompatible() {
        return getStringValue(AECNamespace.Path.VOWIFI_MESSAGE_FOR_INCOMPATIBLE);
    }

    /* access modifiers changed from: protected */
    public int getVoWiFiProvStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_PROV_STATUS);
    }

    /* access modifiers changed from: protected */
    public int getVoWiFiTcStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_TC_STATUS);
    }

    /* access modifiers changed from: protected */
    public int getVoWiFiAddrStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_ADDR_STATUS);
    }

    /* access modifiers changed from: protected */
    public int getVoWiFiActivationMode(int i, int i2, int i3, int i4) {
        if (getVersion() >= 0 && i != 2) {
            if (i == 0) {
                if (getEntitlementInitFromApp() && this.mIsPushMsgStatus) {
                    setPushMsgStatus(false);
                    return 0;
                } else if (i3 == 0 || i4 == 0) {
                    return 2;
                } else {
                    if (i3 == 3 || i4 == 3 || i2 == 3) {
                        return 1;
                    }
                }
            }
            return (i == 1 && (i2 == 1 || i2 == 2) && ((i3 == 1 || i3 == 2) && (i4 == 1 || i4 == 2))) ? 3 : 0;
        }
    }

    private String getServiceFlowURL() {
        return getStringValue(AECNamespace.Path.VOWIFI_SERVICEFLOW_URL);
    }

    private String getServiceFlowUserData() {
        return getStringValue(AECNamespace.Path.VOWIFI_SERVICEFLOW_USERDATA);
    }

    public boolean getDomainFromImpi() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_DOMAIN_FROM_IMPI));
    }

    public boolean getSNIInHeader() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.SNI_IN_HEADER));
    }

    public String getEntitlementVersion() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "1.0";
        }
        return map.getOrDefault("entitlement_version", "1.0");
    }

    public String getEntitlementDomain() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        return map.getOrDefault(AECNamespace.ProviderSettings.ENTITLEMENT_DOMAIN, "");
    }

    public String getEntitlementPort() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        return map.getOrDefault(AECNamespace.ProviderSettings.ENTITLEMENT_PORT, "");
    }

    public String getEntitlementPath() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        return map.getOrDefault(AECNamespace.ProviderSettings.ENTITLEMENT_PATH, "");
    }

    public String getAppId() {
        StringBuilder sb = new StringBuilder();
        if (getEntitlementForVoLte()) {
            sb.append("ap2003");
        }
        if (getEntitlementForVoWiFi()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("ap2004");
        }
        if (getEntitlementForSMSoIp()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("ap2005");
        }
        return sb.toString();
    }

    public String getNotifSenderId() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        return map.getOrDefault(AECNamespace.ProviderSettings.NOTIF_SENDER_ID, "");
    }

    public String getNotifAction() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        String orDefault = map.getOrDefault("notif_action", "");
        if ("0".equals(orDefault)) {
            return "";
        }
        return orDefault;
    }

    public boolean getNotifIgnoreTimestamp() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.NOTIF_IGNORE_TIMESTAMP));
    }

    public boolean getPsDataOffExempt() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.PS_DATA_OFF_EXEMPT));
    }

    public boolean getEntitlementInitFromApp() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_INIT_FROM_APP));
    }

    public boolean getEntitlementForVoLte() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_FOR_VOLTE));
    }

    public boolean getEntitlementForVoWiFi() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_FOR_VOWIFI));
    }

    public boolean getEntitlementForSMSoIp() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_FOR_SMSOIP));
    }

    public boolean isSupportOnlyVoWiFibyUserAction() {
        if (this.mProviderSettings != null && getEntitlementForVoWiFi() && !getEntitlementForVoLte() && getEntitlementInitFromApp() && !getVoWiFiAutoOn()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String getDefaultEntitlement() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return AECNamespace.DefVal.ENTITLEMENT_STATUS;
        }
        return map.getOrDefault(AECNamespace.ProviderSettings.DEFAULT_ENTITLEMENT_STATUS, AECNamespace.DefVal.ENTITLEMENT_STATUS);
    }

    /* access modifiers changed from: protected */
    public boolean getVoLTEAutoOn() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.VOLTE_AUTO_ON));
    }

    /* access modifiers changed from: protected */
    public boolean getVoWiFiAutoOn() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.VOWIFI_AUTO_ON));
    }

    /* access modifiers changed from: protected */
    public String getServerVendor() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        return map.getOrDefault(AECNamespace.ProviderSettings.SERVER_VENDOR, "");
    }
}
