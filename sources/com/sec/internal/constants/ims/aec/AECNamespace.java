package com.sec.internal.constants.ims.aec;

import android.os.Build;

public class AECNamespace {

    public static class AccessType {
        public static final int LTE_4G = 1;
        public static final int NG_RAN_5G = 2;
    }

    public static class Action {
        public static final String AKA_TOKEN_RETRIEVED = "com.samsung.nsds.action.AKA_TOKEN_RETRIEVED";
        public static final String COMPLETED_ENTITLEMENT = "com.sec.imsservice.aec.action.COMPLETED_ENTITLEMENT";
        public static final String POLL_INTERVAL_TIMEOUT = "com.sec.imsservice.aec.action.POLL_INTERVAL_TIMEOUT";
        public static final String RECEIVED_SMS_NOTIFICATION = "android.intent.action.DATA_SMS_RECEIVED";
        public static final String TOKEN_VALIDITY_TIMEOUT = "com.sec.imsservice.aec.action.TOKEN_VALIDITY_TIMEOUT";
        public static final String VERSION_VALIDITY_TIMEOUT = "com.sec.imsservice.aec.action.VERSION_VALIDITY_TIMEOUT";
    }

    public static class ActivationMode {
        public static final int CANNOT_BE_ACTIVATED = 0;
        public static final int CAN_BE_ACTIVATED = 3;
        public static final int IN_PROGRESS = 1;
        public static final int WEB_VIEW = 2;
    }

    public static final class AkaAuthResultType {
        public static final String AKA_IN_PROGRESS = "InProgress";
        public static final String AKA_NOT_PROCESS = "NotProcess";
    }

    public static final class ApplicationId {
        public static final String APP_ID_ALL = "ap2003,ap2004,ap2005";
        public static final String APP_ID_SMSOIP = "ap2005";
        public static final String APP_ID_VOLTE = "ap2003";
        public static final String APP_ID_VOWIFI = "ap2004";
    }

    public static class Build {
        public static final String ANDROID_OS_VERSION = Build.VERSION.RELEASE;
        public static final String TERMINAL_MODEL = android.os.Build.MODEL;
        public static final String TERMINAL_SW_VERSION = Build.VERSION.INCREMENTAL;
        public static final String TERMINAL_VENDOR = android.os.Build.MANUFACTURER;
    }

    public static class BundleData {
        public static final String APP_ID = "appId";
        public static final String HTTP_RESPONSE = "http_response";
        public static final String PHONE_ID = "phoneId";
        public static final String SMSOIP_ENTITLEMENT_STATUS = "smsoipEntitlementstatus";
        public static final String VERSION = "version";
        public static final String VOLTE_AUTO_ON = "volteAutoOn";
        public static final String VOLTE_ENTITLEMENT_STATUS = "volteEntitlementstatus";
        public static final String VOLTE_MESSAGE_FOR_INCOMPATIBLE = "volteMessageForIncompatible";
        public static final String VOWFIFI_SERVICEFLOW_URL = "serviceflow_url";
        public static final String VOWFIFI_SERVICEFLOW_USERDATA = "serviceflow_userdata";
        public static final String VOWIFI_ACTIVATION_MODE = "vowifiActivationMode";
        public static final String VOWIFI_ADDR_STATUS = "addr_status";
        public static final String VOWIFI_AUTO_ON = "vowifiAutoOn";
        public static final String VOWIFI_ENTITLEMENT_STATUS = "vowifiEntitlementstatus";
        public static final String VOWIFI_MESSAGE_FOR_INCOMPATIBLE = "vowifiMessageForIncompatible";
        public static final String VOWIFI_PROV_STATUS = "prov_status";
        public static final String VOWIFI_TC_STATUS = "tc_status";
    }

    public static class DefVal {
        public static final String ENTITLEMENT_STATUS = "incompatible";
        public static final String ENTITLEMENT_VERSION = "1.0";
        public static final String NOTIF_ACTION = "0";
        public static final String TERMINAL_VENDOR = "SEC";
        public static final String VERSION = "0";
    }

    public static class EntitlementStatus {
        public static final int DISABLED = 0;
        public static final int ENABLED = 1;
        public static final int INCOMPATIBLE = 2;
        public static final int PROVISIONING = 3;
    }

    public static class HANDLE_MSG {
        public static final int CLEAR_STORAGE = 8;
        public static final int COMPLETED_EAP_CHALLENGE_RESP = 1005;
        public static final int COMPLETED_WORKFLOW = 1003;
        public static final int CONNECTIVITY_CHANGE = 3;
        public static final int ENTITLEMENT_COMPLETE = 5;
        public static final int ENTITLEMENT_START = 4;
        public static final int ENTITLEMENT_STOP = 6;
        public static final int HANDLE_PUSH_NOTIFICATION = 1012;
        public static final int INIT_WORKFLOW = 1000;
        public static final int NETWORK_CALLBACK_AVAILABLE = 1008;
        public static final int POLL_INTERVAL_TIMEOUT = 1013;
        public static final int RECEIVED_FCM_NOTIFICATION = 1014;
        public static final int RECEIVED_SMS_NOTIFICATION = 7;
        public static final int REFRESH_FCM_TOKEN = 1015;
        public static final int REQUEST_FCM_TOKEN = 1006;
        public static final int REQUEST_NETWORK = 1007;
        public static final int SIM_READY = 1;
        public static final int SIM_REMOVED = 2;
        public static final int START_WORKFLOW = 1001;
        public static final int STOP_WORKFLOW = 1002;
        public static final int TOKEN_TIMEOUT = 1011;
        public static final int UNREGISTER_NETWORK_CALLBACK = 1009;
        public static final int VERSION_TIMEOUT = 1010;
        public static final int WAIT_EAP_CHALLENGE_RESP = 1004;
    }

    public static class HTTP_CONTENT_TYPE {
        public static final String JSON = "application/vnd.gsma.eap-relay.v1.0+json";
        public static final String XML = "text/vnd.wap.connectivity-xml";
    }

    public static class HomeRoamingNWType {
        public static final int ALL = 1;
        public static final int HOME = 2;
        public static final int ROAMING = 3;
    }

    public static final class HttpResponseCode {
        public static final int BAD_REQUEST = 400;
        public static final int FORBIDDEN = 403;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;
        public static final int NOT_FOUND = 404;
        public static final int OK = 200;
        public static final int SERVICE_UNAVAILABLE = 503;
        public static final int UNKNOWN = 0;
        public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    }

    public static class JsonKey {
        public static final String AP2003 = "ap2003";
        public static final String AP2004 = "ap2004";
        public static final String AP2005 = "ap2005";
        public static final String TOKEN = "token";
        public static final String VERS = "vers";
    }

    public static class NotifExtras {
        public static final String APP = "app";
        public static final String ERROR = "error";
        public static final String FROM = "from";
        public static final String PHONE_ID = "phoneId";
        public static final String SENDER_ID = "senderId";
        public static final String TIMESTAMP = "timestamp";
        public static final String TOKEN = "token";
    }

    public static final class Packages {
        public static final String EPDG = "com.sec.epdg";
        public static final String UNIFIED_WFC = "com.sec.unifiedwfc";
    }

    public static class Path {
        public static final String APPLICATION = "root/application/";
        public static final String APPLICATION_0 = "root/application/0";
        public static final String APPLICATION_0_APPID = "root/application/0/appid";
        public static final String APPLICATION_1 = "root/application/1";
        public static final String APPLICATION_1_APPID = "root/application/1/appid";
        public static final String APPLICATION_2 = "root/application/2";
        public static final String APPLICATION_2_APPID = "root/application/2/appid";
        public static final String EAP_RELAY_PACKET = "root/eap-relay-packet";
        public static final String IMSI = "root/imsi";
        public static final String PUSH_NOTIF_TOKEN = "root/push-notif-token";
        public static final String RESPONSE = "root/response";
        public static final String SMSOIP_ENTITLEMENT_STATUS = "root/application/smsoip/entitlementstatus";
        public static final String TIMESTAMP = "root/timestamp";
        public static final String TOKEN_TOKEN = "root/token/token";
        public static final String TOKEN_VALIDITY = "root/token/validity";
        public static final String VERS_VALIDITY = "root/vers/validity";
        public static final String VERS_VERSION = "root/vers/version";
        public static final String VOLTE_ENTITLEMENT_STATUS = "root/application/volte/entitlementstatus";
        public static final String VOLTE_HOME_ENTITLEMENT_STATUS = "root/application/*/home/entitlementstatus";
        public static final String VOLTE_HOME_MESSAGE_FOR_INCOMPATIBLE = "root/application/*/home/messageforincompatible";
        public static final String VOLTE_HOME_NETWORK_VOICE_IRAT_CAPABILITY = "root/application/*/home/networkvoiceiratcapability";
        public static final String VOLTE_MESSAGE_FOR_INCOMPATIBLE = "root/application/volte/messageforincompatible";
        public static final String VOLTE_ROAMING_ENTITLEMENT_STATUS = "root/application/*/roaming/entitlementstatus";
        public static final String VOLTE_ROAMING_MESSAGE_FOR_INCOMPATIBLE = "root/application/*/roaming/messageforincompatible";
        public static final String VOLTE_ROAMING_NETWORK_VOICE_IRAT_CAPABILITY = "root/application/*/roaming/networkvoiceiratcapability";
        public static final String VOWIFI_ADDR_STATUS = "root/application/vowifi/addrstatus";
        public static final String VOWIFI_ENTITLEMENT_STATUS = "root/application/vowifi/entitlementstatus";
        public static final String VOWIFI_MESSAGE_FOR_INCOMPATIBLE = "root/application/vowifi/messageforincompatible";
        public static final String VOWIFI_PROV_STATUS = "root/application/vowifi/provstatus";
        public static final String VOWIFI_SERVICEFLOW_URL = "root/application/vowifi/serviceflow_url";
        public static final String VOWIFI_SERVICEFLOW_USERDATA = "root/application/vowifi/serviceflow_userdata";
        public static final String VOWIFI_TC_STATUS = "root/application/vowifi/tc_status";
    }

    public static class Permission {
        public static final String RECEIVE_AKA_TOKEN = "com.sec.imsservice.permission.RECEIVE_AKA_TOKEN";
    }

    public static class PramsName {
        public static final String APP = "app";
        public static final String EAP_ID = "EAP_ID";
        public static final String EAP_RELAY_PACKET = "eap-relay-packet";
        public static final String ENTITLEMENT_VERSION = "entitlement_version";
        public static final String IMSI = "IMSI";
        public static final String NOTIF_ACTION = "notif_action";
        public static final String NOTIF_TOKEN = "notif_token";
        public static final String TERMINAL_ID = "terminal_id";
        public static final String TERMINAL_MODEL = "terminal_model";
        public static final String TERMINAL_SW_VERSION = "terminal_sw_version";
        public static final String TERMINAL_VENDOR = "terminal_vendor";
        public static final String TOKEN = "token";
        public static final String VERSION = "vers";
    }

    public static class ProviderSettings {
        public static final String DEFAULT_ENTITLEMENT_STATUS = "default_entitlement_status";
        public static final String ENTITLEMENT_DOMAIN = "entitlement_domain";
        public static final String ENTITLEMENT_DOMAIN_FROM_IMPI = "entitlement_domain_from_impi";
        public static final String ENTITLEMENT_FOR_SMSOIP = "entitlement_for_smsoip";
        public static final String ENTITLEMENT_FOR_VOLTE = "entitlement_for_volte";
        public static final String ENTITLEMENT_FOR_VOWIFI = "entitlement_for_vowifi";
        public static final String ENTITLEMENT_INIT_FROM_APP = "entitlement_init_from_app";
        public static final String ENTITLEMENT_PATH = "entitlement_path";
        public static final String ENTITLEMENT_PORT = "entitlement_port";
        public static final String ENTITLEMENT_VERSION = "entitlement_version";
        public static final String NOTIF_ACTION = "notif_action";
        public static final String NOTIF_IGNORE_TIMESTAMP = "notif_ignore_timestamp";
        public static final String NOTIF_SENDER_ID = "notif_senderid";
        public static final String PS_DATA_OFF_EXEMPT = "ps_data_off_exempt";
        public static final String SERVER_VENDOR = "server_vendor";
        public static final String SNI_IN_HEADER = "sni_in_header";
        public static final String VOLTE_AUTO_ON = "volte_auto_on";
        public static final String VOWIFI_AUTO_ON = "vowifi_auto_on";
    }

    public static final class ServiceId {
        public static final String SERVICE_ID_SMSOIP = "smsoip";
        public static final String SERVICE_ID_VOLTE = "volte";
        public static final String SERVICE_ID_VOWIFI = "vowifi";
    }

    public static class Template {
        public static final String AEC_RESULT = "aec_result_%s";
        public static final String DOMAIN = "aes.mnc%s.mcc%s.pub.3gppnetwork.org";
        public static final String USER_AGENT = "PRD-TS43/%s %s/%s IMS-Entitlement/6 OS-Android/%s";
    }
}
