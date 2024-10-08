package com.sec.internal.constants.ims;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.SimUtil;

public class ImsConstants {
    public static String DOWNLOAD_CONFIG = "downloadconfig";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "ImsConstants";

    public static class CmcInfo {
        public static final String CMC_DUMMY_TEL_NUMBER = "99991111222";
    }

    public static class CscParserConstants {
        public static final String ENABLE_IMS = "EnableIMS";
        public static final String ENABLE_RCS = "EnableRCS";
        public static final String ENABLE_RCS_CHAT_SERVICE = "EnableRCSchat";
        public static final String ENABLE_VOLTE = "EnableVoLTE";
        public static final String SUPPORT_VOWIFI = "EnableVoiceoverWIFI";
    }

    public static class DeRegistrationCause {
        public static final int AUTOCONFIG_SMS_PUSH = 143;
        public static final int COUNTRY_CHANGED = 802;
        public static final int DATA_DEACTIVATED_DEFAULT = 3;
        public static final int DATA_DEACTIVATED_IMS = 4;
        public static final int DCN = 807;
        public static final int DEVICE_SHUT_DOWN = 13;
        public static final int ENTITLEMENT_FAILED = 144;
        public static final int EPDG_NOT_AVAILABLE = 124;
        public static final int FLIGHT_MODE_ON = 12;
        public static final int MOCK_LOCATION_UPDATED = 41;
        public static final int MULTI_USER_SWITCHED = 1000;
        public static final int NETWORK_MODE_CHANGED = 5;
        public static final int PHONE_CRASH = 6;
        public static final int SIM_NOT_AVAILABLE = 1;
        public static final int SIM_REFRESH_TIMEOUT = 42;
        public static final int TRIGGER_SIPDELEGATE = 145;
    }

    public static class EmergencyPdnPolicy {
        public static final int EPDN = 0;
        public static final int IMSPDN_IF_IPC_RAT_EPDG = 1;
    }

    public static class EmergencyRat {
        public static final String IWLAN = "VoWIFI";
        public static final String LTE = "VoLTE";
        public static final String NR = "VoNR";
    }

    public static class FtDlParams {
        public static final String FT_DL_CONV_ID = "ci";
        public static final String FT_DL_ID = "id";
        public static final String FT_DL_OTHER_PARTY = "op";
        public static final String FT_DL_URL = "url";
    }

    public static class Intents {
        public static final String ACTION_AIRPLANE_MODE = "android.intent.action.AIRPLANE_MODE";
        public static final String ACTION_AKA_CHALLENGE_COMPLETE = "com.sec.imsservice.AKA_CHALLENGE_COMPLETE";
        public static final String ACTION_AKA_CHALLENGE_FAILED = "com.sec.imsservice.AKA_CHALLENGE_FAILED";
        public static final String ACTION_CALL_STATE_CHANGED = "com.samsung.rcs.CALL_STATE_CHANGED";
        public static final String ACTION_CHECK_REGISTRATION_DAILY = "com.samsung.intent.ACTION_CHECK_REGISTRATION_DAILY";
        public static final String ACTION_DATAUSAGE_REACH_TO_LIMIT = "com.android.intent.action.DATAUSAGE_REACH_TO_LIMIT";
        public static final String ACTION_DCN_TRIGGERED = "com.samsung.intent.action.UPDATE_IMS_REGISTRATION";
        public static final String ACTION_DEVICE_STORAGE_FULL = "android.intent.action.DEVICE_STORAGE_FULL";
        public static final String ACTION_DEVICE_STORAGE_NOT_FULL = "android.intent.action.DEVICE_STORAGE_NOT_FULL";
        public static final String ACTION_DM_CHANGED = "com.samsung.ims.dm.DM_CHANGED";
        public static final String ACTION_DSAC_MODE_SWITCH = "android.ims.hvolte.MODE_SWITCH";
        public static final String ACTION_EMERGENCY_CALLBACK_MODE_CHANGED = "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED";
        public static final String ACTION_EMM_ERROR = "com.samsung.intent.action.EMM_ERROR";
        public static final String ACTION_FACTORY_RESET = "android.intent.action.FACTORY_RESET";
        public static final String ACTION_FLIGHT_MODE = "com.sec.android.internal.ims.FLIGHT_MODE";
        public static final String ACTION_FLIGHT_MODE_BY_POWEROFF = "powerofftriggered";
        public static final String ACTION_IMS_ON_SIMLOADED = "com.samsung.ims.action.onsimloaded";
        public static final String ACTION_IMS_PROFILE_LOADED = "com.sec.imsservice.action.IMS_PROFILE_LOADED";
        public static final String ACTION_IMS_STATE = "com.samsung.ims.action.IMS_REGISTRATION";
        public static final String ACTION_PERIODIC_POLLING_TIMEOUT = "com.sec.internal.ims.imsservice.dm_polling_timeout";
        public static final String ACTION_REQUEST_AKA_CHALLENGE = "com.sec.imsservice.REQUEST_AKA_CHALLENGE";
        public static final String ACTION_RESET_NETWORK_SETTINGS = "com.samsung.intent.action.SETTINGS_NETWORK_RESET";
        public static final String ACTION_RETRYTIME_EXPIRED = "android.intent.action.retryTimeExpired";
        public static final String ACTION_SERVICE_DOWN = "com.android.ims.IMS_SERVICE_DOWN";
        public static final String ACTION_SERVICE_UP = "com.android.ims.IMS_SERVICE_UP";
        public static final String ACTION_SIM_ICCID_CHANGED = "com.samsung.action.SIM_ICCID_CHANGED";
        public static final String ACTION_SIM_ISIM_LOADED = "android.intent.action.ISIM_LOADED";
        public static final String ACTION_SIM_REFRESH = "com.android.intent.isim_refresh";
        public static final String ACTION_SIM_REFRESH_FAIL_RECOVERY = "com.samsung.intent.action.isim_refresh_fail_recovery";
        public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
        public static final String ACTION_SMS_CALLBACK_MODE_CHANGED_INTERNAL = "com.samsung.intent.action.SMS_CALLBACK_MODE_CHANGED_INTERNAL";
        public static final String ACTION_SOFT_RESET = "com.samsung.intent.action.SETTINGS_SOFT_RESET";
        public static final String ACTION_T3396_EXPIRED = "android.intent.action.retrySetupData";
        public static final String ACTION_UPDATE_PCSCF_RESTORATION = "com.samsung.intent.action.UPDATE_PCSCF_RESTORATION";
        public static final String ACTION_WFC_SWITCH_PROFILE = "action_wfc_switch_profile_broadcast";
        public static final String EXTRA_AIRPLANE_KEY = "state";
        public static final String EXTRA_ANDORID_PHONE_ID = "android:phone_id";
        public static final String EXTRA_CALL_CALLREASON = "EXTRA_CALL_CALLREASON";
        public static final String EXTRA_CALL_EVENT = "EXTRA_CALL_EVENT";
        public static final String EXTRA_CALL_IMAGE = "EXTRA_CALL_IMAGE";
        public static final String EXTRA_CALL_IMPORTANCE = "EXTRA_CALL_IMPORTANCE";
        public static final String EXTRA_CALL_LATITUDE = "EXTRA_CALL_LATITUDE";
        public static final String EXTRA_CALL_LONGITUDE = "EXTRA_CALL_LONGITUDE";
        public static final String EXTRA_CALL_RADIUS = "EXTRA_CALL_RADIUS";
        public static final String EXTRA_CALL_SUBJECT = "EXTRA_CALL_SUBJECT";
        public static final String EXTRA_CAUSE_KEY = "CAUSE";
        public static final String EXTRA_DCN_PHONE_ID = "phoneId";
        public static final String EXTRA_DSAC_MODE = "modeType";
        public static final String EXTRA_IS_CMC_CALL = "EXTRA_IS_CMC_CALL";
        public static final String EXTRA_IS_CMC_CONNECTED = "EXTRA_IS_CMC_CONNECTED";
        public static final String EXTRA_IS_INCOMING = "EXTRA_IS_INCOMING";
        public static final String EXTRA_LIMIT_POLICY = "policyData";
        public static final String EXTRA_PCSCF_RESTORATION_PHONEID = "PhoneId";
        public static final String EXTRA_PCSCF_RESTORATION_V4 = "V4";
        public static final String EXTRA_PCSCF_RESTORATION_V6 = "V6";
        public static final String EXTRA_PHONE_ID = "EXTRA_PHONE_ID";
        public static final String EXTRA_POWEROFF_TRIGGERED = "powerofftriggered";
        public static final String EXTRA_REGISTERED = "REGISTERED";
        public static final String EXTRA_REGISTERED_SERVICES = "SERVICE";
        public static final String EXTRA_REGI_PHONE_ID = "PHONE_ID";
        public static final String EXTRA_RESET_NETWORK_SUBID = "subId";
        public static final String EXTRA_SIMMOBILITY = "SIMMO";
        public static final String EXTRA_SIP_ERROR_CODE = "SIP_ERROR";
        public static final String EXTRA_SIP_ERROR_REASON = "ERROR_REASON";
        public static final String EXTRA_TEL_NUMBER = "EXTRA_TEL_NUMBER";
        public static final String EXTRA_UICC_MOBILITY_SPEC_VER = "IMS_CONFIG_UICC_MOBILITY_SPEC_VER";
        public static final String EXTRA_UPDATED_ITEM = "item";
        public static final String EXTRA_UPDATED_PHONE_ID = "phoneId";
        public static final String EXTRA_UPDATED_VALUE = "value";
        public static final String EXTRA_VOWIFI = "VOWIFI";
        public static final String EXTRA_WFC_REQUEST = "oem_request";
        public static final String INTENT_ACTION_LTE_REJECT = "com.samsung.intent.action.LTE_REJECT";
        public static final String INTENT_ACTION_REGIST_REJECT = "com.samsung.intent.action.regist_reject";
    }

    public static class NrInterworking {
        public static int DISABLE_SA_DURING_WFC = 1;
        public static int FULL_SUPPORT = 2;
        public static int NOT_SUPPORT;
    }

    public static class NrSaMode {
        public static int DEPRIORITIZE = 2;
        public static int DISABLE = 1;
        public static int ENABLE;
    }

    public static class Packages {
        public static final String CLASS_SIMMOBILITY_KIT_UPDATE = "com.samsung.ims.smk.DeviceUpdateService";
        public static final String PACKAGE_BIKE_MODE = "com.samsung.android.app.bikemode";
        public static final String PACKAGE_DM_CLIENT = "com.ims.dm";
        public static final String PACKAGE_EPDG = "com.sec.epdg";
        public static final String PACKAGE_IDC_WEBVIEW = "com.sec.datachannelwebview";
        public static final String PACKAGE_QUALITY_DATALOG = "com.vzw.qualitydatalog";
        public static final String PACKAGE_SDM = "com.samsung.sdm";
        public static final String PACKAGE_SEC_MSG = "com.samsung.android.messaging";
        public static final String PACKAGE_SEC_VVM = "com.samsung.vvm";
        public static final String PACKAGE_SIMMOBILITY_KIT = "com.samsung.ims.smk";
        public static final String SMK_PRELOADED_VERSION = "1.3.31";
    }

    public static class Phone {
        public static int SLOT_1 = 0;
        public static int SLOT_2 = 1;
    }

    public static class RCS_AS {
        public static final String CARRIER = "";
        public static final String INTEROP = "interop";
        public static final String JIBE = "jibe";
        public static final String SEC = "sec";
    }

    public static class ServiceType {
        public static final int RCS = 2;
        public static final int VIDEO = 1;
        public static final int VOICE = 0;
        public static final int VOWIFI = 3;
    }

    public static class SimMobilityKitTimer {
        public static final long BASIC_INTERVAL = 604800000;
        public static final String RETRY_INTERVAL = "com.samsung.ims.smk.retry_interval";
        public static final String START_TIMER = "com.samsung.ims.smk.smk_timer_start";
        public static final String TIMER_EXIST = "smk_timer_exist";
    }

    public static class SystemPath {
        public static final String EFS = "/efs/sec_efs/";
    }

    public static class SystemProperties {
        public static final String CARRIERFEATURE_FORCE_USE = "persist.ims.carrierfeature_force_use";
        public static final String CURRENT_PLMN = "ril.currentplmn";
        public static final String FIRST_API_VERSION = "ro.product.first_api_level";
        public static final String FIRST_API_VERSION_VENDOR = "ro.vendor.api_level";
        public static final String GCF_MODE_PROPERTY = "persist.ims.gcfmode";
        public static final String GCF_MODE_PROPERTY_P_OS = "persist.radio.gcfmode";
        public static final String IMSSETUP_MODE = "debug.test.imssetup.restart";
        public static final String IMSSWITCH_POLICY = "persist.ims.imsswitch";
        public static final String IMS_TEST_MODE_PROP = "persist.sys.ims_test_mode";
        public static final String LTE_VOICE_STATUS = "ril.lte.voice.status";
        public static final String PS_INDICATOR = "ril.ims.ltevoicesupport";
        public static final String SIMMOBILITY_ENABLE = "persist.ims.simmobility";
        public static final String SIM_STATE = "gsm.sim.state";
    }

    public static class UpdateChatServiceReason {
        public static final int DISABLED_BY_SIP_FORBIDDEN = 1;
        public static final int UPDATED_BY_DMA_CHANGE = 2;
    }

    public static class Uris {
        public static final String AUTHORITY = "com.sec.ims.settings";
        public static final String CONFIG_URI = "content://com.sec.ims.settings";
        public static final String FRAGMENT_SIM_SLOT = "simslot";
        public static final Uri LINES_CONTENT_URI = Uri.parse("content://com.samsung.ims.nsds.provider/lines");
        public static final Uri MMS_PREFERENCE_PROVIDER_DATASAVER_URI = Uri.parse("content://com.android.mms.csc.PreferenceProvider/data_saver");
        public static final Uri MMS_PREFERENCE_PROVIDER_KEY_URI = Uri.parse("content://com.android.mms.csc.PreferenceProvider/key");
        public static final Uri RCS_PREFERENCE_PROVIDER_SUPPORT_DUAL_RCS = Uri.parse("content://com.sec.ims.android.rcs/support_dual_rcs");
        public static final Uri SETTINGS_PROVIDER_DYNAMIC_IMS_UPDATE_URI = Uri.parse("content://com.sec.ims.settings/dynamic_ims_updated");
        public static final Uri SETTINGS_PROVIDER_PROFILE_URI = Uri.parse("content://com.sec.ims.settings/profile");
        public static final Uri SETTINGS_PROVIDER_SIMMOBILITY_URI = Uri.parse("content://com.sec.ims.settings/simmobility");
        public static final Uri SETTINGS_PROVIDER_SMK_CONFIG_RESET_URI = Uri.parse("content://com.sec.ims.settings/resetconfig");
        public static final Uri SETTINGS_PROVIDER_SMK_CONFIG_URI = Uri.parse("content://com.sec.ims.settings/downloadconfig");
        public static final Uri SMS_SETTING = Uri.parse("content://com.sec.ims.settings/sms_setting");
        public static final Uri SPECIFIC_BOT_EXPIRES = Uri.parse("content://com.android.mms.csc.PreferenceProvider/specific_bot_expires");
        public static final Uri SPECIFIC_BOT_URI = Uri.parse("content://com.android.mms.csc.PreferenceProvider/specific_bot");
    }

    public static class VcrtPost {
        public static int NO_VCRT = 0;
        public static int VCRT_AVAILABLE = 1;
    }

    public static class VoiceDomainPrefEutran {
        public static final int CS_VOICE_ONLY = 1;
        public static final int CS_VOICE_PREFERRED = 2;
        public static final int IMS_PS_VOICE_ONLY = 4;
        public static final int IMS_PS_VOICE_PREFERRED = 3;
    }

    public static class SystemSettings {
        public static final SettingsItem AIRPLANE_MODE = new SettingsItem("settings", GLOBAL, "airplane_mode_on");
        public static int AIRPLANE_MODE_ON = 1;
        public static final String AUTHOTIRY_RCS_DM_CONFIG = "com.samsung.rcs.dmconfigurationprovider";
        public static final SettingsItem DATA_ROAMING = new SettingsItem("settings", GLOBAL, "data_roaming");
        public static int DATA_ROAMING_UNKNOWN = -1;
        public static final SettingsItem DEFAULT_SMS_APP = new SettingsItem("settings", SECURE, "sms_default_application");
        public static final SettingsItem DOWNLOAD_SMK_CONFIG = new SettingsItem(Uris.AUTHORITY, "", "downloadconfig");
        public static final SettingsItem DYNAMIC_IMS_UPDATED = new SettingsItem(Uris.AUTHORITY, "", "dynamic_ims_updated");
        public static final SettingsItem ENRICHED_CALL_VBC = new SettingsItem("settings", SYSTEM, "enriched_call_vbc");
        /* access modifiers changed from: private */
        public static String GLOBAL = "global";
        public static final SettingsItem IMS_DM_CONFIG = new SettingsItem(AUTHOTIRY_RCS_DM_CONFIG, IMS_OMADM, "*");
        public static final SettingsItem IMS_GLOBAL = new SettingsItem(Uris.AUTHORITY, GLOBAL, "");
        private static String IMS_NV = "nvstorage";
        public static final SettingsItem IMS_NV_STORAGE = new SettingsItem(Uris.AUTHORITY, IMS_NV, "*");
        private static String IMS_OMADM = "omadm/./3GPP_IMS";
        private static String IMS_PROFILE = "profile";
        public static final SettingsItem IMS_PROFILES = new SettingsItem(Uris.AUTHORITY, IMS_PROFILE, "*");
        public static final SettingsItem IMS_SIM_MOBILITY = new SettingsItem(Uris.AUTHORITY, SIM_MOBILITY, "");
        private static String IMS_SWITCH = "imsswitch";
        public static final SettingsItem IMS_SWITCHES = new SettingsItem(Uris.AUTHORITY, IMS_SWITCH, "");
        private static String IMS_USER = "userconfig";
        public static int LTE_DATA_NETWORK_MODE_ENABLED = 1;
        public static int LTE_DATA_ROAMING_DISABLED = 0;
        public static final SettingsItem MNOMAP_UPDATED = new SettingsItem(Uris.AUTHORITY, "", "mnomap_updated");
        public static final SettingsItem MOBILE_DATA = new SettingsItem("settings", GLOBAL, "mobile_data");
        public static final SettingsItem MOBILE_DATA_PRESSED = new SettingsItem("settings", GLOBAL, "mobile_data_pressed");
        public static final SettingsItem PREFFERED_NETWORK_MODE = new SettingsItem("settings", GLOBAL, "preferred_network_mode");
        public static final SettingsItem PREFFERED_VOICE_CALL = new SettingsItem("settings", SYSTEM, "prefered_voice_call");
        public static final SettingsItem RCS_ALLOWED_URI = new SettingsItem("com.sec.knox.provider2", "PhoneRestrictionPolicy", "isRCSEnabled");
        public static final int RCS_DISABLED = 0;
        public static final int RCS_DISABLED_BY_NETWORK = -2;
        public static final int RCS_ENABLED = 1;
        public static final int RCS_ENABLED_BY_NETWORK = 3;
        public static final int RCS_NOTSET = -1;
        public static final SettingsItem RCS_ROAMING_PREF = new SettingsItem(Uris.AUTHORITY, IMS_USER, "");
        public static final int RCS_SETTING_NOT_FOUND = -3;
        public static final int RCS_TURNING_OFF = 2;
        public static final SettingsItem RCS_USER_SETTING1 = new SettingsItem("settings", SYSTEM, "rcs_user_setting");
        public static final SettingsItem RCS_USER_SETTING2 = new SettingsItem("settings", SYSTEM, "rcs_user_setting2");
        public static final SettingsItem RESET_SMK_CONFIG = new SettingsItem(Uris.AUTHORITY, "", "resetconfig");
        public static int ROAMING_DATA_ENABLED = 1;
        /* access modifiers changed from: private */
        public static String SECURE = "secure";
        public static final SettingsItem SETUP_WIZARD = new SettingsItem("settings", SECURE, "user_setup_complete");
        private static String SIM_MOBILITY = "simmobility";
        /* access modifiers changed from: private */
        public static String SYSTEM = "system";
        public static final SettingsItem USER_TOGGLED_VOLTE_SLOT1 = new SettingsItem("settings", SYSTEM, "voicecall_type_user_action");
        public static final SettingsItem USER_TOGGLED_VOLTE_SLOT2 = new SettingsItem("settings", SYSTEM, "voicecall_type_user_action2");
        public static final int VIDEO_DISABLED = 1;
        public static final int VIDEO_ENABLED = 0;
        public static final int VIDEO_UNKNOWN = -1;
        public static final SettingsItem VILTE_SLOT1 = new SettingsItem("settings", SYSTEM, "videocall_type");
        public static final SettingsItem VILTE_SLOT2 = new SettingsItem("settings", SYSTEM, "videocall_type2");
        public static final int VOICE_CS = 1;
        public static final int VOICE_UNKNOWN = -1;
        public static final int VOICE_VOLTE = 0;
        public static final SettingsItem VOLTE_PROVISIONING = new SettingsItem("settings", SYSTEM, "allow_volte_provisioning");
        public static final int VOLTE_PROVISIONING_DISABLED = 0;
        public static final int VOLTE_PROVISIONING_ENABLED = 1;
        public static final SettingsItem VOLTE_ROAMING = new SettingsItem("settings", GLOBAL, "hd_voice_roaming_enabled");
        public static int VOLTE_ROAMING_ENABLED = 1;
        public static int VOLTE_ROAMING_UNKNOWN = -1;
        public static final SettingsItem VOLTE_SLOT1 = new SettingsItem("settings", "system", "voicecall_type");
        public static final SettingsItem VOLTE_SLOT2 = new SettingsItem("settings", SYSTEM, "voicecall_type2");
        public static final SettingsItem WIFI_CALL_ENABLE1 = new SettingsItem("settings", SYSTEM, "wifi_call_enable1");
        public static final SettingsItem WIFI_CALL_ENABLE2 = new SettingsItem("settings", SYSTEM, "wifi_call_enable2");
        public static final SettingsItem WIFI_CALL_PREFERRED1 = new SettingsItem("settings", SYSTEM, "wifi_call_preferred1");
        public static final SettingsItem WIFI_CALL_PREFERRED2 = new SettingsItem("settings", SYSTEM, "wifi_call_preferred2");
        public static final SettingsItem WIFI_CALL_WHEN_ROAMING1 = new SettingsItem("settings", SYSTEM, "wifi_call_when_roaming1");
        public static final SettingsItem WIFI_CALL_WHEN_ROAMING2 = new SettingsItem("settings", SYSTEM, "wifi_call_when_roaming2");
        public static final SettingsItem WIFI_SETTING = new SettingsItem("settings", GLOBAL, "wifi_on");

        public static class SecWfcModeFeatureValueConstants {
            public static final int SEC_WFC_CS_PREF = 2;
            public static final int SEC_WFC_WIFI_ONLY = 3;
            public static final int SEC_WFC_WIFI_PREF = 1;
        }

        public static class SecWfcRoamModeFeatureValueConstants {
            public static final int SEC_ROAM_WFC_CS_PREF = 0;
            public static final int SEC_ROAM_WFC_WIFI_ONLY = 2;
            public static final int SEC_ROAM_WFC_WIFI_PREF = 1;
        }

        public static class WfcModeFeatureValueConstants {
            public static final int CELLULAR_PREFERRED = 1;
            public static final int WIFI_ONLY = 0;
            public static final int WIFI_PREFERRED = 2;
        }

        private static int convertToGoogleWfcMode(int i, boolean z) {
            if (z) {
                if (i != 0) {
                    return i != 2 ? 2 : 0;
                }
                return 1;
            } else if (i != 1) {
                return i != 3 ? 1 : 0;
            } else {
                return 2;
            }
        }

        public static void addUri(UriMatcher uriMatcher, SettingsItem settingsItem, int i) {
            uriMatcher.addURI(settingsItem.getAuthority(), settingsItem.getPath(), i);
        }

        public static int getVoiceCallType(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                return VOLTE_SLOT1.get(context, i);
            }
            return VOLTE_SLOT2.get(context, i);
        }

        public static void setVoiceCallType(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                SettingsItem settingsItem = VOLTE_SLOT1;
                if (settingsItem.get(context, -1) != i || i == -1) {
                    settingsItem.set(context, i);
                }
            } else {
                SettingsItem settingsItem2 = VOLTE_SLOT2;
                if (settingsItem2.get(context, -1) != i || i == -1) {
                    settingsItem2.set(context, i);
                }
            }
            int subId = SimUtil.getSubId(i2);
            if (subId != -1 && i != -1) {
                SubscriptionManager.setSubscriptionProperty(subId, "volte_vt_enabled", i == 0 ? "1" : "0");
            }
        }

        public static void setVoiceCallTypeUserAction(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                SettingsItem settingsItem = USER_TOGGLED_VOLTE_SLOT1;
                if (settingsItem.get(context, 0) != i) {
                    settingsItem.set(context, i);
                    return;
                }
                return;
            }
            SettingsItem settingsItem2 = USER_TOGGLED_VOLTE_SLOT2;
            if (settingsItem2.get(context, 0) != i) {
                settingsItem2.set(context, i);
            }
        }

        public static boolean isUserToggledVoiceCallType(Context context, int i) {
            int i2;
            if (i == Phone.SLOT_1) {
                i2 = USER_TOGGLED_VOLTE_SLOT1.get(context, 0);
            } else {
                i2 = USER_TOGGLED_VOLTE_SLOT2.get(context, 0);
            }
            if (i2 == 1) {
                return true;
            }
            return false;
        }

        public static int getRcsUserSetting(Context context, int i, int i2) {
            return getSettingsItemByPhoneId(2, i2).get(context, i);
        }

        public static void setRcsUserSetting(Context context, int i, int i2) {
            SettingsItem settingsItemByPhoneId = getSettingsItemByPhoneId(2, i2);
            if (settingsItemByPhoneId.get(context, -1) != i || i == -1) {
                settingsItemByPhoneId.set(context, i);
            }
        }

        public static int getVideoCallType(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                return VILTE_SLOT1.get(context, i);
            }
            return VILTE_SLOT2.get(context, i);
        }

        public static void setVideoCallType(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                SettingsItem settingsItem = VILTE_SLOT1;
                if (settingsItem.get(context, -1) != i || i == -1) {
                    settingsItem.set(context, i);
                    return;
                }
                return;
            }
            SettingsItem settingsItem2 = VILTE_SLOT2;
            if (settingsItem2.get(context, -1) != i || i == -1) {
                settingsItem2.set(context, i);
            }
        }

        public static boolean isWiFiCallEnabled(Context context) {
            int wiFiCallEnabled = getWiFiCallEnabled(context, -1, 0);
            int wiFiCallEnabled2 = getWiFiCallEnabled(context, -1, 1);
            String r0 = ImsConstants.LOG_TAG;
            Log.d(r0, "isWiFiCallEnabled: wifi_call_enable [" + wiFiCallEnabled + "], wifi_call_enable2 [" + wiFiCallEnabled2 + "]");
            if (wiFiCallEnabled == 1 || wiFiCallEnabled2 == 1) {
                return true;
            }
            return false;
        }

        public static int getWiFiCallEnabled(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                return WIFI_CALL_ENABLE1.get(context, i);
            }
            return WIFI_CALL_ENABLE2.get(context, i);
        }

        public static int getWiFiCallPreferred(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                return WIFI_CALL_PREFERRED1.get(context, i);
            }
            return WIFI_CALL_PREFERRED2.get(context, i);
        }

        public static int getWiFiCallWhenRoaming(Context context, int i, int i2) {
            if (i2 == Phone.SLOT_1) {
                return WIFI_CALL_WHEN_ROAMING1.get(context, i);
            }
            return WIFI_CALL_WHEN_ROAMING2.get(context, i);
        }

        public static void setWiFiCallEnabled(Context context, int i, int i2) {
            if (i == Phone.SLOT_1) {
                WIFI_CALL_ENABLE1.set(context, i2);
            } else if (i == Phone.SLOT_2) {
                WIFI_CALL_ENABLE2.set(context, i2);
            }
            int subId = SimUtil.getSubId(i);
            if (subId >= 0) {
                SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_enabled", String.valueOf(i2));
            }
        }

        public static void setWiFiCallPreferred(Context context, int i, int i2) {
            int subId;
            if (i == Phone.SLOT_1) {
                WIFI_CALL_PREFERRED1.set(context, i2);
            } else if (i == Phone.SLOT_2) {
                WIFI_CALL_PREFERRED2.set(context, i2);
            }
            if (i2 <= 3 && (subId = SimUtil.getSubId(i)) >= 0) {
                int convertToGoogleWfcMode = convertToGoogleWfcMode(i2, false);
                Log.i(ImsConstants.LOG_TAG, "setWiFiCallPreferred: Set telephony DB");
                SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_mode", String.valueOf(convertToGoogleWfcMode));
            }
        }

        public static void setWiFiCallWhenRoaming(Context context, int i, int i2) {
            if (i == Phone.SLOT_1) {
                WIFI_CALL_WHEN_ROAMING1.set(context, i2);
            } else if (i == Phone.SLOT_2) {
                WIFI_CALL_WHEN_ROAMING2.set(context, i2);
            }
            int subId = SimUtil.getSubId(i);
            if (subId >= 0) {
                int convertToGoogleWfcMode = convertToGoogleWfcMode(i2, true);
                Log.i(ImsConstants.LOG_TAG, "setWiFiCallWhenRoaming: Set telephony DB");
                SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_roaming_mode", String.valueOf(convertToGoogleWfcMode));
            }
        }

        public static SettingsItem getSettingsItemByPhoneId(int i, int i2) {
            if (i != 0) {
                if (i != 1) {
                    if (i != 2) {
                        if (i != 3) {
                            return null;
                        }
                        if (i2 == 0) {
                            return WIFI_CALL_ENABLE1;
                        }
                        return WIFI_CALL_ENABLE2;
                    } else if (i2 == 0) {
                        return RCS_USER_SETTING1;
                    } else {
                        return RCS_USER_SETTING2;
                    }
                } else if (i2 == 0) {
                    return VILTE_SLOT1;
                } else {
                    return VILTE_SLOT2;
                }
            } else if (i2 == 0) {
                return VOLTE_SLOT1;
            } else {
                return VOLTE_SLOT2;
            }
        }

        public static class SettingsItem {
            private String mAuthority;
            private String mCategory;
            private String mName;
            private String mPkg = "";

            public SettingsItem(String str, String str2, String str3) {
                this.mAuthority = str;
                this.mCategory = str2;
                this.mName = str3;
            }

            public String getAuthority() {
                return this.mAuthority;
            }

            public String getName() {
                return this.mName;
            }

            public String getPath() {
                if (TextUtils.isEmpty(this.mName)) {
                    return this.mCategory;
                }
                return this.mCategory + "/" + this.mName;
            }

            public String getPackage() {
                return this.mPkg;
            }

            public Uri getUri() {
                return Uri.parse("content://" + this.mAuthority + "/" + getPath());
            }

            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: android.database.Cursor} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v1, resolved type: android.database.Cursor} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: android.net.Uri} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v2, resolved type: android.database.Cursor} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v3, resolved type: android.database.Cursor} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: android.net.Uri} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v9, resolved type: android.net.Uri} */
            /* JADX WARNING: Code restructure failed: missing block: B:21:0x009f, code lost:
                if (r7 == null) goto L_0x00cd;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:31:0x00ca, code lost:
                if (r7 == null) goto L_0x00cd;
             */
            /* JADX WARNING: Multi-variable type inference failed */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public int get(android.content.Context r9, int r10) {
                /*
                    r8 = this;
                    java.lang.String r0 = "package"
                    java.lang.String[] r3 = new java.lang.String[]{r0}
                    r7 = 0
                    java.lang.String r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.SYSTEM     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r2 = r8.mCategory     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    boolean r1 = r1.equalsIgnoreCase(r2)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    if (r1 == 0) goto L_0x0025
                    android.content.ContentResolver r1 = r9.getContentResolver()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r2 = r8.mName     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    int r10 = android.provider.Settings.System.getInt(r1, r2)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r1 = r8.mName     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    android.net.Uri r1 = android.provider.Settings.System.getUriFor(r1)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                L_0x0023:
                    r2 = r1
                    goto L_0x007a
                L_0x0025:
                    java.lang.String r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.GLOBAL     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r2 = r8.mCategory     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    boolean r1 = r1.equalsIgnoreCase(r2)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    if (r1 == 0) goto L_0x0042
                    android.content.ContentResolver r1 = r9.getContentResolver()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r2 = r8.mName     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    int r10 = android.provider.Settings.Global.getInt(r1, r2)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r1 = r8.mName     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    android.net.Uri r1 = android.provider.Settings.Global.getUriFor(r1)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    goto L_0x0023
                L_0x0042:
                    java.lang.String r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.SECURE     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r2 = r8.mCategory     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    boolean r1 = r1.equalsIgnoreCase(r2)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    if (r1 == 0) goto L_0x005f
                    android.content.ContentResolver r1 = r9.getContentResolver()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r2 = r8.mName     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    int r10 = android.provider.Settings.Secure.getInt(r1, r2)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r1 = r8.mName     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    android.net.Uri r1 = android.provider.Settings.Secure.getUriFor(r1)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    goto L_0x0023
                L_0x005f:
                    java.lang.String r1 = com.sec.internal.constants.ims.ImsConstants.LOG_TAG     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    r2.<init>()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r4 = "Unknown Category : "
                    r2.append(r4)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r4 = r8.mCategory     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    r2.append(r4)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r2 = r2.toString()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    android.util.Log.e(r1, r2)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    r2 = r7
                L_0x007a:
                    if (r2 == 0) goto L_0x009f
                    android.content.ContentResolver r1 = r9.getContentResolver()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    r4 = 0
                    r5 = 0
                    r6 = 0
                    android.database.Cursor r7 = r1.query(r2, r3, r4, r5, r6)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    if (r7 == 0) goto L_0x009f
                    int r9 = r7.getCount()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    if (r9 == 0) goto L_0x009f
                    boolean r9 = r7.moveToFirst()     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    if (r9 == 0) goto L_0x009f
                    int r9 = r7.getColumnIndex(r0)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    java.lang.String r9 = r7.getString(r9)     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                    r8.mPkg = r9     // Catch:{ SettingNotFoundException -> 0x00ae, IllegalArgumentException | IllegalStateException -> 0x00a7 }
                L_0x009f:
                    if (r7 == 0) goto L_0x00cd
                L_0x00a1:
                    r7.close()
                    goto L_0x00cd
                L_0x00a5:
                    r8 = move-exception
                    goto L_0x00fc
                L_0x00a7:
                    r9 = move-exception
                    r9.printStackTrace()     // Catch:{ all -> 0x00a5 }
                    if (r7 == 0) goto L_0x00cd
                    goto L_0x00a1
                L_0x00ae:
                    java.lang.String r9 = com.sec.internal.constants.ims.ImsConstants.LOG_TAG     // Catch:{ all -> 0x00a5 }
                    java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a5 }
                    r0.<init>()     // Catch:{ all -> 0x00a5 }
                    java.lang.String r1 = "SettingNotFound : "
                    r0.append(r1)     // Catch:{ all -> 0x00a5 }
                    java.lang.String r1 = r8.getPath()     // Catch:{ all -> 0x00a5 }
                    r0.append(r1)     // Catch:{ all -> 0x00a5 }
                    java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x00a5 }
                    android.util.Log.d(r9, r0)     // Catch:{ all -> 0x00a5 }
                    if (r7 == 0) goto L_0x00cd
                    goto L_0x00a1
                L_0x00cd:
                    java.lang.String r9 = com.sec.internal.constants.ims.ImsConstants.LOG_TAG
                    java.lang.StringBuilder r0 = new java.lang.StringBuilder
                    r0.<init>()
                    java.lang.String r1 = "getInt("
                    r0.append(r1)
                    java.lang.String r1 = r8.getPath()
                    r0.append(r1)
                    java.lang.String r1 = ") : "
                    r0.append(r1)
                    r0.append(r10)
                    java.lang.String r1 = ", package : "
                    r0.append(r1)
                    java.lang.String r8 = r8.mPkg
                    r0.append(r8)
                    java.lang.String r8 = r0.toString()
                    android.util.Log.d(r9, r8)
                    return r10
                L_0x00fc:
                    if (r7 == 0) goto L_0x0101
                    r7.close()
                L_0x0101:
                    throw r8
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.constants.ims.ImsConstants.SystemSettings.SettingsItem.get(android.content.Context, int):int");
            }

            public int getbySubId(Context context, int i, int i2) {
                int i3;
                try {
                    if (SystemSettings.GLOBAL.equalsIgnoreCase(this.mCategory)) {
                        ContentResolver contentResolver = context.getContentResolver();
                        i3 = Settings.Global.getInt(contentResolver, this.mName + i2);
                    } else if (SystemSettings.SYSTEM.equalsIgnoreCase(this.mCategory)) {
                        ContentResolver contentResolver2 = context.getContentResolver();
                        i3 = Settings.System.getInt(contentResolver2, this.mName + i2);
                    } else if (SystemSettings.SECURE.equalsIgnoreCase(this.mCategory)) {
                        ContentResolver contentResolver3 = context.getContentResolver();
                        i3 = Settings.Secure.getInt(contentResolver3, this.mName + i2);
                    } else {
                        String r3 = ImsConstants.LOG_TAG;
                        Log.e(r3, "Unknown Category : " + this.mCategory);
                        String r32 = ImsConstants.LOG_TAG;
                        Log.d(r32, "getIntbySubId(" + getPath() + i2 + ") : " + i);
                        return i;
                    }
                    i = i3;
                } catch (Settings.SettingNotFoundException unused) {
                    String r33 = ImsConstants.LOG_TAG;
                    Log.d(r33, "SettingNotFound : " + getPath() + ",subId " + i2);
                }
                String r322 = ImsConstants.LOG_TAG;
                Log.d(r322, "getIntbySubId(" + getPath() + i2 + ") : " + i);
                return i;
            }

            public void set(Context context, int i) {
                String r0 = ImsConstants.LOG_TAG;
                Log.d(r0, "setInt(" + getPath() + ") : " + i);
                if (SystemSettings.SYSTEM.equalsIgnoreCase(this.mCategory)) {
                    Settings.System.putInt(context.getContentResolver(), this.mName, i);
                } else if (SystemSettings.GLOBAL.equalsIgnoreCase(this.mCategory)) {
                    Settings.Global.putInt(context.getContentResolver(), this.mName, i);
                } else if (SystemSettings.SECURE.equalsIgnoreCase(this.mCategory)) {
                    Settings.Secure.putInt(context.getContentResolver(), this.mName, i);
                } else {
                    String r4 = ImsConstants.LOG_TAG;
                    Log.e(r4, "Unknown Category : " + this.mCategory);
                }
            }
        }
    }

    public static class OmaVersion {
        public static final String OMA_2_0 = "OMA2.0";
        public static final String OMA_2_1 = "OMA2.1";
        public static final String OMA_2_2 = "OMA2.2";

        private OmaVersion() {
        }
    }
}
