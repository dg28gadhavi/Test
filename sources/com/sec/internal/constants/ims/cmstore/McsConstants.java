package com.sec.internal.constants.ims.cmstore;

import android.net.Uri;
import android.os.Build;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.os.DeviceUtil;

public class McsConstants {
    public static final String CONNECT_HTTPS = "https://";

    public static final class AccountStatus {
        public static final int ACTIVE_ALL = 1000;
        public static final int AUTH = 3;
        public static final int DEACTIVE = 10;
        public static final int DELETE = 9999;
        public static final int DORMANT = 11;
        public static final int MAX = 10000;
        public static final int NONE = 0;
        public static final int OTP = 1;
        public static final int REGI = 2;
    }

    public static final class Auth {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String APPLICATIONS = "APPLICATIONS";
        public static final String AUTHENTICATION_CODE = "auth_code";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String AUTO_CONFIG = "auto_config";
        public static final String CMS_DATA_TTL = "cms_data_ttl";
        public static final String CODE = "code";
        public static final String CONSENT_CONTEXT = "consent_context";
        public static final String DEVICE_INFO = "device_info";
        public static final String FCM_SENDER_ID = "fcm_sender_id";
        public static final String GRANT_TYPE = "grant_type";
        public static final String ID = "id";
        public static final String IS_CHANGED_ALIAS = "is_changed_alias";
        public static final String IS_CHANGED_CONSENT = "is_changed_consent";
        public static final String MAX_SMALL_FILE_SIZE = "max_small_file_size";
        public static final String MAX_UPLOAD_FILE_SIZE = "max_upload_file_size";
        public static final String MCS_ACCOUNT = "account";
        public static final String MCS_ACCOUNT_ID = "account_id";
        public static final String MCS_ACCOUNT_STATUS = "account_status";
        public static final String MCS_ALIAS = "alias";
        public static final String MDN = "mdn";
        public static final String MMS_REVOKE_TTL_SECS = "mms_revoke_ttl_secs";
        public static final String OASIS_CONFIG = "oasis_config";
        public static final String OASIS_LARGE_FILE_SERVER_ROOT = "oasis_large_file_server_root";
        public static final String OASIS_SERVER_ROOT = "oasis_server_root";
        public static final String OASIS_SERVER_VERSION = "server_version";
        public static final String OASIS_SMALL_FILE_SERVER_ROOT = "oasis_small_file_server_root";
        public static final String PRIMARY = "primary";
        public static final String PROVISION_EVENT = "provision_event";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String REGISTRATION_CODE = "registration_code";
        public static final String ROOT = "rapi.rcsoasis.kr";
        public static final String ROOT_CLIENT_ID = "root_client_id";
        public static final String ROOT_URL = "https://rapi.rcsoasis.kr";
        public static final String SMS_REVOKE_TTL_SECS = "sms_revoke_ttl_secs";
        public static final String TARGET_INFO = "TARGET_INFO";
        public static final String TYPE = "type";
        public static final String TYPE_MOBILE_IP = "mobile_ip";
        public static final String TYPE_OTP = "otp";
        public static final String TYPE_PASSWORD = "password";
        public static final String XMS_MESSAGE = "XMS_MESSAGE";
    }

    public static final class BundleData {
        public static final String GET_ALL = "getAll";
        public static final String INFO = "info";
        public static final String KEY = "key";
        public static final String PUSH_TYPE = "pushType";
        public static final String VALUE = "value";
    }

    public static class ChannelDeleteReason {
        public static final String NONDMA = "NONDMA";
        public static final String NORMAL = "NORMAL";
    }

    public static final class ClientEvent {
        public static final int HANDLE_MCS_PROVISION_COMPLETED = 8;
        public static final int HANDLE_MCS_PROVISION_DEREGISTER = 7;
        public static final int HANDLE_MCS_PROVISION_GET_ACCOUNT = 6;
        public static final int HANDLE_MCS_PROVISION_GET_SD = 3;
        public static final int HANDLE_MCS_PROVISION_INIT = 0;
        public static final int HANDLE_MCS_PROVISION_MANAGE_SD = 2;
        public static final int HANDLE_MCS_PROVISION_RE_AUTHENTICATION = 4;
        public static final int HANDLE_MCS_PROVISION_START = 1;
        public static final int HANDLE_MCS_PROVISION_UPDATE_ACCOUNT = 5;
    }

    public static final class ClosedReason {
        public static final int ALL_PARTICIPANTS_LEFT = 1;
        public static final int LEFT_BY_SERVER = 2;
        public static final int REASON_DEFAULT = -1;
        public static final int SD_LEAVE_CHAT = 0;
    }

    public static final class CommonHttpHeaders {
        public static final String CLIENT_VERSION = "x-mcs-clientVersion";
        public static final String DEVICE_ID = "x-mcs-deviceId";
        public static final String DEVICE_NAME = "x-mcs-deviceName";
        public static final String DEVICE_TYPE = "x-mcs-deviceType";
        public static final String FIRMWARE_VERSION = "x-mcs-firmwareVersion";
        public static final String OASIS_ENCRYPT = "Oasis-Encrypt";
        public static final String OS_VERSION = "x-mcs-osVersion";
    }

    public static final class Decryption {
        public static final String ENCRYPTED_DATA = "encrypted_data";
    }

    public static final class Details {
        public static final int BOOT_UP = 3;
        public static final int BOOT_UP_TOKEN_UPDATE = 4;
        public static final int INITIAL_REGISTRATION = 2;
        public static final int TOKEN_UPDATE_WHILE_IN_USE = 5;
        public static final int UNKNOWN = 1;
        public static final int UNKNOWN_QR_CODE = 10;
    }

    public static final class DeviceInfo {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_IP = "client_ip";
        public static final String CLIENT_VENDOR = "client_vendor";
        public static final String CLIENT_VERSION = "client_version";
        public static final String DEVICE_ID = "device_id";
        public static final String DEVICE_KIND = "device_kind";
        public static final String DEVICE_NAME = "device_name";
        public static final String FIRMWARE_VERSION = "firmware_version";
        public static final String MNO = "mno";
        public static final String NATIVE_INFO = "native_info";
        public static final String OS_TYPE = "os_type";
        public static final String OS_VERSION = "os_version";
        public static final String SERVICE_VERSION = "service_version";
    }

    public static final class DeviceInfoValue {
        public static final String CLIENT_VENDOR = "Samsung";
        public static final String DEVICE_KIND = (DeviceUtil.isTablet() ? "tablet" : PhoneConstants.PHONE_KEY);
        public static final String DEVICE_NAME = Build.MODEL;
        public static final String FIRMWARE_VERSION = Build.VERSION.INCREMENTAL;
        public static final String MNO = "mno";
        public static final String NATIVE_INFO = "native_info";
        public static final String OS_TYPE = "aos";
        public static final String OS_VERSION = String.valueOf(Build.VERSION.SDK_INT);
        public static final String SERVICE_VERSION = "1.0";
        public static final int SMS_PORT = 16793;
    }

    public static class DispositionStatus {
        public static final String DELIVERED = "delivered";
        public static final String DISPLAYED = "displayed";
    }

    public static final class ExtendedErrorCode {
        public static final int EXCEPTION_CONNECT = 802;
        public static final int EXCEPTION_SOCKET = 803;
        public static final int EXCEPTION_SOCKET_TIMEOUT = 804;
        public static final int EXCEPTION_SSL = 801;
        public static final int EXCEPTION_SSL_HANDSHAKE = 800;
        public static final int EXCEPTION_UNKNOWN_HOST = 805;
        public static final int NO_USER_INFO = 900;
    }

    public static final class Functions {
        public static final int STARRED = 0;
        public static final int UNSTARRED = 1;
    }

    public static final class HiddenStatus {
        public static final int HIDDEN = 1;
        public static final int NON_HIDDEN = 0;
    }

    public static final class McsActions {
        public static final String INTENT_ACCESS_TOKEN_VALIDITY_TIMEOUT = "com.sec.imsservice.cmstore.mcs.action.ACCESS_TOKEN_VALIDITY_TIMEOUT";
        public static final String INTENT_OTP_RESPONSE_TIMEOUT = "com.sec.imsservice.cmstore.mcs.action.OTP_RESPONSE_TIMEOUT";
        public static final String INTENT_REFRESH_TOKEN_VALIDITY_TIMEOUT = "com.sec.imsservice.cmstore.mcs.action.REFRESH_TOKEN_VALIDITY_TIMEOUT";
        public static final String INTENT_REGISTRATION_CODE_VALIDITY_TIMEOUT = "com.sec.imsservice.cmstore.mcs.action.REGISTRATION_CODE_VALIDITY_TIMEOUT";
    }

    public static class McsMessageContextValues extends MessageContextValues {
        public static final String botMessage = "bot-message";
        public static final String chatMessage = "chat-message";
        public static final String conferenceMessage = "conference-message";
        public static final String fileMessage = "file-message";
        public static final String geolocationMessage = "geolocation-message";
        public static final String imdnMessage = "imdn-message";
        public static final String multiMediaMessage = "multimedia-message";
        public static final String responseMessage = "response-message";
        public static final String standaloneMessage = "standalone-message";
    }

    public static final class McsSharedPref {
        public static final String PREF_ACCESS_TOKEN = "access_token";
        public static final String PREF_ACCESS_TOKEN_EXPIRE_TIME = "access_token_expire_time";
        public static final String PREF_AUTHENTICATION_CODE = "authentication_code";
        public static final String PREF_CMS_DATA_TTL = "cms_data_ttl";
        public static final String PREF_FCM_REGISTRATION_TOKEN = "fcm_registration_token";
        public static final String PREF_FCM_SENDER_ID = "fcm_sender_id";
        public static final String PREF_MAX_SMALL_FILE_SIZE = "max_small_file_size";
        public static final String PREF_MAX_UPLOAD_FILE_SIZE = "max_upload_file_size";
        public static final String PREF_MCS_ACCOUNT_ID = "account_id";
        public static final String PREF_MCS_ALIAS = "alias";
        public static final String PREF_MCS_USER = "is_mcs_user";
        public static final String PREF_MMS_REVOKE_TTL_SECS = "mms_revoke_ttl_secs";
        public static final String PREF_OASIS_LARGE_FILE_SERVER_ROOT = "oasis_large_file_server_root";
        public static final String PREF_OASIS_SERVER_ROOT = "oasis_server_root";
        public static final String PREF_OASIS_SERVER_VERSION = "oasis_server_version";
        public static final String PREF_OASIS_SMALL_FILE_SERVER_ROOT = "oasis_small_file_server_root";
        public static final String PREF_REFRESH_TOKEN = "refresh_token";
        public static final String PREF_REFRESH_TOKEN_EXPIRE_TIME = "refresh_token_expire_time";
        public static final String PREF_REGISTRATION_CODE = "registration_code";
        public static final String PREF_SMS_REVOKE_TTL_SECS = "sms_revoke_ttl_secs";
    }

    public static final class McsUserRegistration {
        public static final int MCS_NOT_USER = 0;
        public static final int MCS_UNKNOWN = -1;
        public static final int MCS_USER = 1;
    }

    public static final class NativeInfo {
        public static final String DEFAULT_SMS_APP = "default_sms_app";
        public static final String IMEI = "imei";
        public static final String IMSI = "imsi";
        public static final String SMS_PORT = "sms_port";
    }

    public static final class Operations {
        public static final int DELETE = 1;
        public static final int GET = 2;
        public static final int POST = 0;
        public static final int PUT = 3;
    }

    public static final class Protocol {
        public static final String SENDER_PD = "standard";
        public static final String SENDER_SD = "oasis";
    }

    public static final class ProvisionNotificationType {
        public static final int NOTIFY_ACCOUNT_INFO = 7;
        public static final int NOTIFY_DE_REGISTRATION_COMPLETED = 2;
        public static final int NOTIFY_PUSH_MESSAGE_RECEIVED = 8;
        public static final int NOTIFY_REGISTRATION_COMPLETED = 1;
        public static final int NOTIFY_SD_APPROVED = 3;
        public static final int NOTIFY_SD_LIST = 5;
        public static final int NOTIFY_SD_LIST_ALL = 6;
        public static final int NOTIFY_SD_REMOVED = 4;
    }

    public static final class PushMessages {
        public static final String KEY_CONFIG_TYPE = "configType";
        public static final String KEY_STATUS = "status";
        public static final String KEY_SYNC_TYPE = "syncType";
        public static final String TYPE_SYNC_BLOCKFILTER = "syncBlockfilter";
        public static final String TYPE_SYNC_CONFIG = "syncConfig";
        public static final String TYPE_SYNC_STATUS = "syncStatus";
        public static final String VALUE_DISABLE_MCS = "disableMcs";
        public static final String VALUE_ENABLE_MCS = "enableMcs";
    }

    public static final class Result {
        public static final int FAILURE = 200;
        public static final int SUCCESS = 100;
    }

    public static final class SdInfo {
        public static final String DEVICE_INFO = "device_info";
        public static final String USER_CODE = "user_code";
    }

    public static final class SdManagementType {
        public static final int APPROVE_SD = 1;
        public static final int REMOVE_SD = 2;
    }

    public static final class ServerConfig {
        public static final int DEFAULT_CMS_TTL_VALUE = 2592000;
        public static final int DEFAULT_XMS_TTL_VALUE = 86400;
        public static final String OASIS_VERSION = "0.0.0";
    }

    public static class Uris {
        public static final String AUTHORITY = "com.sec.ims.android.rcs";
        public static final String FRAGMENT_SIM_SLOT = "simslot";
        public static final Uri RCS_REGISTRATION_STATUS_URI = Uri.parse("content://com.sec.ims.android.rcs/registration");
        public static final Uri RCS_USER_ALIAS_URI = Uri.parse("content://com.sec.ims.android.rcs/preferences/5");
        public static final String URI = "content://com.sec.ims.android.rcs";
    }

    public enum RCSMessageType {
        MULTIMEDIA(0),
        TEXT(1),
        LOCATION(2),
        SYSTEM(3),
        SYSTEM_USER_LEFT(4),
        SYSTEM_USER_INVITED(5),
        SYSTEM_USER_JOINED(6),
        SYSTEM_CONTINUE_ON_ANOTHER_DEVICE(7),
        SYSTEM_LEADER_CHANGED(8),
        SYSTEM_GROUP_INVITE(10),
        SYSTEM_GROUP_INVITE_FAIL(11),
        SYSTEM_GROUP_REINVITE(12),
        SYSTEM_LEADER_INFORMED(13),
        SYSTEM_DISMISS_CHAT(14),
        SYSTEM_KICKED_OUT_BY_LEADER(15),
        SYSTEM_RENAME_BY_LEADER(16),
        SYSTEM_LEFT_CHAT(17),
        SYSTEM_ALL_LEFT_CHAT(18),
        SYSTEM_GROUPCHAT_CLOSED(19),
        SYSTEM_IS_INVITED(20),
        SYSTEM_ALL_LEFT_CHAT_NO_ADD(21),
        SINGLE(30),
        GROUP(40);
        
        private final int mId;

        private RCSMessageType(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public enum TP_MessageType {
        MULTIMEDIA(0),
        SYSTEM_USER_LEFT(1),
        SYSTEM_USER_INVITED(2),
        SYSTEM_USER_JOINED(3),
        SYSTEM_CONTINUE_ON_ANOTHER_DEVICE(4),
        TEXT(5),
        LOCATION(6),
        SYSTEM(7),
        SYSTEM_LEADER_CHANGED(8),
        SYSTEM_GROUP_INVITE(10),
        SYSTEM_GROUP_INVITE_FAIL(11),
        SYSTEM_GROUP_REINVITE(12),
        SYSTEM_LEADER_INFORMED(13),
        SYSTEM_DISMISS_CHAT(14),
        SYSTEM_KICKED_OUT_BY_LEADER(15),
        SYSTEM_RENAME_BY_LEADER(16),
        SYSTEM_LEFT_CHAT(17),
        SYSTEM_ALL_LEFT_CHAT(18),
        SYSTEM_GROUPCHAT_CLOSED(19),
        SYSTEM_IS_INVITED(20),
        SYSTEM_ALL_LEFT_CHAT_NO_ADD(21),
        SINGLE(30),
        GROUP(40);
        
        private final int mId;

        private TP_MessageType(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }
}
