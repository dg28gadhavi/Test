package com.sec.internal.ims.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegistrationEvents {
    protected static final int DATAUSAGE_REACH_TO_LIMIT = 712;
    protected static final int EVENT_ADS_CHANGED = 702;
    public static final int EVENT_BLOCK_REGISTRATION_HYS_TIMER = 806;
    public static final int EVENT_BLOCK_REGISTRATION_ROAMING_TIMER = 144;
    public static final int EVENT_BOOT_COMPLETED = 150;
    protected static final int EVENT_CELL_INFO_CHANGED = 24;
    public static final int EVENT_CHATBOT_AGREEMENT_CHANGED = 56;
    public static final int EVENT_CHECK_UNPROCESSED_OMADM_CONFIG = 407;
    public static final int EVENT_CHECK_UNPROCESSED_VOLTE_STATE = 157;
    public static final int EVENT_CONFIG_UPDATED = 35;
    public static final int EVENT_CONTACT_ACTIVATED = 809;
    public static final int EVENT_DEFAULT_NETWORK_CHANGED = 706;
    public static final int EVENT_DELAYED_DEREGISTER = 128;
    public static final int EVENT_DELAYED_DEREGISTERINTERNAL = 145;
    public static final int EVENT_DELAYED_STOP_PDN = 133;
    public static final int EVENT_DEREGISTERED = 101;
    public static final int EVENT_DEREGISTER_BY_PENDED_DEFAULT_NET_CHANGED = 18;
    public static final int EVENT_DEREGISTER_FOR_DCN = 807;
    public static final int EVENT_DEREGISTER_TIMEOUT = 107;
    public static final int EVENT_DEREGISTRATION_REQUESTED = 120;
    public static final int EVENT_DISCONNECT_PDN_BY_TIMEOUT = 404;
    public static final int EVENT_DISCONNECT_PDN_BY_VOLTE_DISABLED = 406;
    public static final int EVENT_DM_CONFIG_COMPLETE = 29;
    public static final int EVENT_DM_CONFIG_TIMEOUT = 43;
    public static final int EVENT_DNS_RESPONSE = 57;
    public static final int EVENT_DO_PENDING_UPDATE_REGISTRATION = 32;
    public static final int EVENT_DO_RECOVERY_ACTION = 134;
    public static final int EVENT_DSAC_MODE_CHANGED = 146;
    public static final int EVENT_DYNAMIC_IMS_UPDATED = 408;
    public static final int EVENT_E911_REGI_TIMEOUT = 155;
    protected static final int EVENT_EC_VBC_SETTING_CHANGED = 156;
    public static final int EVENT_EMERGENCY_READY = 119;
    protected static final int EVENT_EPDG_CONNECTED = 26;
    public static final int EVENT_EPDG_DEREGISTER_REQUESTED = 124;
    public static final int EVENT_EPDG_DISCONNECTED = 27;
    public static final int EVENT_EPDG_EVENT_TIMEOUT = 135;
    public static final int EVENT_EPDG_HANDOVER_ENABLE_CHANGED = 154;
    protected static final int EVENT_EPDG_IKEERROR = 52;
    protected static final int EVENT_EPDG_IPSECDISCONNECTED = 54;
    public static final int EVENT_EPDG_VOICE_PREFERENCE_CHANGED = 123;
    public static final int EVENT_FINISH_OMADM_PROVISIONING_UPDATE = 39;
    public static final int EVENT_FINISH_THREAD_FOR_GETTING_HOST_ADDRESS = 60;
    public static final int EVENT_FLIGHT_MODE_CHANGED = 12;
    public static final int EVENT_FORCED_UPDATE_REGISTRATION_REQUESTED = 140;
    public static final int EVENT_FORCE_SMS_PUSH = 143;
    public static final int EVENT_GEO_LOCATION_UPDATED = 40;
    public static final int EVENT_HANDOFF_EVENT_TIMEOUT = 136;
    public static final int EVENT_IMS_PDN_CONNECTING = 401;
    public static final int EVENT_IMS_PROFILE_UPDATED = 15;
    public static final int EVENT_IMS_SWITCH_UPDATED = 17;
    public static final int EVENT_LOCAL_IP_CHANGED = 5;
    public static final int EVENT_LOCATION_CACHE_EXPIRY = 803;
    public static final int EVENT_LOCATION_TIMEOUT = 800;
    public static final int EVENT_LTE_DATA_NETWORK_MODE_CHAGED = 50;
    public static final int EVENT_MANUAL_DEREGISTER = 10;
    public static final int EVENT_MANUAL_REGISTER = 9;
    public static final int EVENT_MNOMAP_UPDATED = 148;
    public static final int EVENT_MOBILE_DATA_CHANGED = 34;
    public static final int EVENT_MOBILE_DATA_PRESSED_CHANGED = 153;
    public static final int EVENT_MOBILE_RADIO_CONNECTED = 61;
    public static final int EVENT_MOBILE_RADIO_DISCONNECTED = 62;
    public static final int EVENT_NETWORK_EVENT_CHANGED = 701;
    public static final int EVENT_NETWORK_MODE_CHANGE_TIMEOUT = 49;
    public static final int EVENT_NETWORK_SUSPENDED = 151;
    public static final int EVENT_NETWORK_TYPE = 3;
    public static final int EVENT_OWN_CAPABILITIES_CHANGED = 31;
    public static final int EVENT_PCO_INFO = 703;
    public static final int EVENT_PDN_CONNECTED = 22;
    public static final int EVENT_PDN_DISCONNECTED = 23;
    protected static final int EVENT_PDN_FAILED = 129;
    public static final int EVENT_POSTPONE_UPDATE_REGISTRATION_BY_DMA_CHANGE = 139;
    public static final int EVENT_RCS_ALLOWED_CHANGED = 53;
    public static final int EVENT_RCS_DELAYED_DEREGISTER = 142;
    public static final int EVENT_RCS_USER_SETTING_CHANGED = 147;
    public static final int EVENT_REFRESH_REGISTRATION = 141;
    public static final int EVENT_REGEVENT_CONTACT_URI_NOTIFIED = 810;
    public static final int EVENT_REGISTERED = 100;
    public static final int EVENT_REGISTER_ERROR = 104;
    public static final int EVENT_REMOVE_CHAT_FEATURES_BY_SIP_FORBIDDEN = 137;
    public static final int EVENT_REQUEST_DM_CONFIG = 28;
    public static final int EVENT_REQUEST_FULL_NETWORK_REGISTRATION = 63;
    public static final int EVENT_REQUEST_LOCATION = 801;
    public static final int EVENT_REQUEST_NOTIFY_VOLTE_SETTINGS_OFF = 131;
    public static final int EVENT_REQUEST_X509_CERT_VERIFY = 30;
    public static final int EVENT_ROAMING_DATA_CHANGED = 44;
    public static final int EVENT_ROAMING_SETTINGS_CHANGED = 46;
    public static final int EVENT_RTTMODE_UPDATED = 705;
    public static final int EVENT_SETUP_WIZARD_COMPLETED = 811;
    public static final int EVENT_SET_THIRDPARTY_FEATURE_TAGS = 126;
    public static final int EVENT_SHUTDOWN = 130;
    public static final int EVENT_SIM_READY = 20;
    public static final int EVENT_SIM_REFRESH = 36;
    public static final int EVENT_SIM_REFRESH_TIMEOUT = 42;
    protected static final int EVENT_SIM_SUBSCRIBE_ID_CHANGED = 707;
    public static final int EVENT_SSAC_REREGISTER = 121;
    public static final int EVENT_START_GEO_LOCATION_UPDATE = 51;
    public static final int EVENT_START_OMADM_PROVISIONING_UPDATE = 38;
    public static final int EVENT_SUBSCRIBE_ERROR = 108;
    public static final int EVENT_TELEPHONY_CALL_STATUS_CHANGED = 33;
    public static final int EVENT_TIMS_ESTABLISHMENT_TIMEOUT = 132;
    public static final int EVENT_TIMS_ESTABLISHMENT_TIMEOUT_RCS = 152;
    public static final int EVENT_TRY_EMERGENCY_REGISTER = 118;
    public static final int EVENT_TRY_REGISTER = 2;
    public static final int EVENT_TRY_REGISTER_TIMER = 4;
    public static final int EVENT_TTYMODE_UPDATED = 37;
    protected static final int EVENT_UICC_CHANGED = 21;
    public static final int EVENT_UPDATE_CHAT_SERVICE_BY_DMA_CHANGE = 138;
    public static final int EVENT_UPDATE_REGISTRATION = 25;
    public static final int EVENT_UPDATE_REGI_CONFIG = 409;
    public static final int EVENT_UPDATE_SIP_DELEGATE_REGISTRATION = 58;
    public static final int EVENT_UPDATE_SIP_DELEGATE_REGI_TIMEOUT = 59;
    public static final int EVENT_USER_SWITCHED = 1000;
    public static final int EVENT_VIDEO_SETTING_CHANGED = 127;
    public static final int EVENT_VOLTE_SETTING_CHANGED = 125;
    public static final int EVENT_VOWIFI_SETTING_CHANGED = 122;
    public static final int EVENT_WFC_SWITCH_PROFILE = 704;
    private static final String TAG = "RegistrationEvents";
    private static final Map<Integer, String> msgToStringMap = new HashMap();

    private RegistrationEvents() {
    }

    static {
        Arrays.stream(RegistrationEvents.class.getDeclaredFields()).filter(new RegistrationEvents$$ExternalSyntheticLambda0()).filter(new RegistrationEvents$$ExternalSyntheticLambda1()).filter(new RegistrationEvents$$ExternalSyntheticLambda2()).forEach(new RegistrationEvents$$ExternalSyntheticLambda3());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$static$3(Field field) {
        try {
            msgToStringMap.put(Integer.valueOf(field.getInt((Object) null)), field.getName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String msgToString(int i) {
        Map<Integer, String> map = msgToStringMap;
        Integer valueOf = Integer.valueOf(i);
        return map.getOrDefault(valueOf, "UNKNOWN(" + i + ")");
    }

    /* JADX WARNING: Removed duplicated region for block: B:163:0x03f4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean handleEvent(android.os.Message r5, com.sec.internal.ims.core.RegistrationManagerHandler r6, com.sec.internal.ims.core.RegistrationManagerBase r7, com.sec.internal.ims.core.NetworkEventController r8, com.sec.internal.ims.core.UserEventController r9) {
        /*
            int r0 = r5.what
            r1 = 2
            r2 = 1
            if (r0 == r1) goto L_0x050e
            r1 = 3
            java.lang.String r3 = "phoneId"
            r4 = 0
            if (r0 == r1) goto L_0x04f3
            r1 = 4
            if (r0 == r1) goto L_0x04e7
            r1 = 5
            if (r0 == r1) goto L_0x04df
            r1 = 9
            if (r0 == r1) goto L_0x04d5
            r1 = 10
            if (r0 == r1) goto L_0x04bd
            r1 = 17
            if (r0 == r1) goto L_0x04b1
            r1 = 18
            if (r0 == r1) goto L_0x049a
            r1 = 100
            if (r0 == r1) goto L_0x0491
            r1 = 101(0x65, float:1.42E-43)
            if (r0 == r1) goto L_0x048a
            r1 = 107(0x6b, float:1.5E-43)
            if (r0 == r1) goto L_0x0481
            r1 = 108(0x6c, float:1.51E-43)
            if (r0 == r1) goto L_0x047a
            java.lang.String r1 = "mode"
            switch(r0) {
                case 12: goto L_0x0470;
                case 15: goto L_0x0469;
                case 20: goto L_0x0458;
                case 21: goto L_0x0447;
                case 22: goto L_0x043e;
                case 23: goto L_0x0435;
                case 24: goto L_0x0425;
                case 25: goto L_0x041a;
                case 26: goto L_0x0413;
                case 27: goto L_0x040c;
                case 28: goto L_0x03fa;
                case 29: goto L_0x03ee;
                case 30: goto L_0x03e5;
                case 31: goto L_0x03da;
                case 32: goto L_0x03d5;
                case 33: goto L_0x03cc;
                case 34: goto L_0x03c3;
                case 35: goto L_0x03b8;
                case 36: goto L_0x03a7;
                case 37: goto L_0x0396;
                case 38: goto L_0x0389;
                case 39: goto L_0x037c;
                case 40: goto L_0x0377;
                case 46: goto L_0x036e;
                case 104: goto L_0x0367;
                case 131: goto L_0x035e;
                case 132: goto L_0x0355;
                case 133: goto L_0x034c;
                case 134: goto L_0x0343;
                case 135: goto L_0x032d;
                case 136: goto L_0x0326;
                case 137: goto L_0x0319;
                case 138: goto L_0x030c;
                case 139: goto L_0x02ff;
                case 140: goto L_0x02f6;
                case 141: goto L_0x02e9;
                case 142: goto L_0x02e4;
                case 144: goto L_0x02db;
                case 145: goto L_0x02cd;
                case 146: goto L_0x02c8;
                case 147: goto L_0x02b9;
                case 148: goto L_0x02b2;
                case 150: goto L_0x02ad;
                case 151: goto L_0x029d;
                case 152: goto L_0x0355;
                case 153: goto L_0x0294;
                case 154: goto L_0x0288;
                case 155: goto L_0x027f;
                case 156: goto L_0x0278;
                case 157: goto L_0x0271;
                case 401: goto L_0x0258;
                case 404: goto L_0x024f;
                case 406: goto L_0x0246;
                case 407: goto L_0x023b;
                case 408: goto L_0x0234;
                case 409: goto L_0x0227;
                case 701: goto L_0x021c;
                case 702: goto L_0x0217;
                case 703: goto L_0x0206;
                case 704: goto L_0x01fb;
                case 705: goto L_0x01ea;
                case 706: goto L_0x01dd;
                case 707: goto L_0x01d0;
                case 712: goto L_0x01c4;
                case 800: goto L_0x01bb;
                case 801: goto L_0x01b6;
                case 803: goto L_0x01ad;
                case 807: goto L_0x01a4;
                case 809: goto L_0x018e;
                case 810: goto L_0x0187;
                case 811: goto L_0x050e;
                case 1000: goto L_0x0182;
                default: goto L_0x0037;
            }
        L_0x0037:
            switch(r0) {
                case 42: goto L_0x0173;
                case 43: goto L_0x03ee;
                case 44: goto L_0x0167;
                default: goto L_0x003a;
            }
        L_0x003a:
            switch(r0) {
                case 49: goto L_0x0162;
                case 50: goto L_0x0156;
                case 51: goto L_0x0148;
                case 52: goto L_0x0141;
                case 53: goto L_0x013c;
                case 54: goto L_0x0135;
                default: goto L_0x003d;
            }
        L_0x003d:
            switch(r0) {
                case 56: goto L_0x012e;
                case 57: goto L_0x0121;
                case 58: goto L_0x0114;
                case 59: goto L_0x0107;
                case 60: goto L_0x00fc;
                case 61: goto L_0x00f5;
                case 62: goto L_0x00ee;
                case 63: goto L_0x00df;
                default: goto L_0x0040;
            }
        L_0x0040:
            switch(r0) {
                case 118: goto L_0x00d6;
                case 119: goto L_0x00cf;
                case 120: goto L_0x00ba;
                case 121: goto L_0x00a8;
                case 122: goto L_0x00a1;
                case 123: goto L_0x008a;
                case 124: goto L_0x0083;
                case 125: goto L_0x0074;
                case 126: goto L_0x006d;
                case 127: goto L_0x005e;
                case 128: goto L_0x0055;
                case 129: goto L_0x0044;
                default: goto L_0x0043;
            }
        L_0x0043:
            return r4
        L_0x0044:
            int r6 = r5.arg1
            int r7 = r5.arg2
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r8.onPdnFailed(r6, r7, r5)
            goto L_0x0519
        L_0x0055:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r7.onDelayedDeregister(r5)
            goto L_0x0519
        L_0x005e:
            java.lang.Object r6 = r5.obj
            java.lang.Boolean r6 = (java.lang.Boolean) r6
            boolean r6 = r6.booleanValue()
            int r5 = r5.arg1
            r9.onVideoCallServiceSettingChanged(r6, r5)
            goto L_0x0519
        L_0x006d:
            int r5 = r5.arg1
            r6.onThirdParyFeatureTagsUpdated(r5)
            goto L_0x0519
        L_0x0074:
            java.lang.Object r6 = r5.obj
            java.lang.Boolean r6 = (java.lang.Boolean) r6
            boolean r6 = r6.booleanValue()
            int r5 = r5.arg1
            r9.onVolteServiceSettingChanged(r6, r5)
            goto L_0x0519
        L_0x0083:
            int r5 = r5.arg1
            r8.onEpdgDeregisterRequested(r5)
            goto L_0x0519
        L_0x008a:
            int r6 = r5.arg1
            int r9 = r5.arg2
            if (r9 != r2) goto L_0x0091
            r4 = r2
        L_0x0091:
            boolean r9 = r7.isCdmaAvailableForVoice(r6)
            if (r9 == r4) goto L_0x0519
            r7.setCdmaAvailableForVoice(r6, r4)
            int r5 = r5.arg1
            r8.onVoicePreferredChanged(r5)
            goto L_0x0519
        L_0x00a1:
            int r5 = r5.arg1
            r9.onVowifiServiceSettingChanged(r5, r6)
            goto L_0x0519
        L_0x00a8:
            java.lang.Object r7 = r5.obj
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r7 = r7.intValue()
            int r5 = r5.arg1
            if (r5 != r2) goto L_0x00b5
            r4 = r2
        L_0x00b5:
            r6.onSSACRegiRequested(r7, r4)
            goto L_0x0519
        L_0x00ba:
            java.lang.Object r7 = r5.obj
            com.sec.internal.ims.core.RegisterTask r7 = (com.sec.internal.ims.core.RegisterTask) r7
            int r8 = r5.arg1
            if (r8 != r2) goto L_0x00c4
            r8 = r2
            goto L_0x00c5
        L_0x00c4:
            r8 = r4
        L_0x00c5:
            int r5 = r5.arg2
            if (r5 != r2) goto L_0x00ca
            r4 = r2
        L_0x00ca:
            r6.onDeregistrationRequest(r7, r8, r4)
            goto L_0x0519
        L_0x00cf:
            int r5 = r5.arg1
            r7.onEmergencyReady(r5)
            goto L_0x0519
        L_0x00d6:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r7.tryEmergencyRegister(r5)
            goto L_0x0519
        L_0x00df:
            java.lang.Object r6 = r5.obj
            java.lang.Integer r6 = (java.lang.Integer) r6
            int r6 = r6.intValue()
            int r5 = r5.arg1
            r7.triggerFullNetworkRegistration(r6, r5)
            goto L_0x0519
        L_0x00ee:
            int r5 = r5.arg1
            r7.handleInactiveCiaOnMobileDisconnected(r5)
            goto L_0x0519
        L_0x00f5:
            int r5 = r5.arg1
            r7.handleInactiveCiaOnMobileConnected(r5)
            goto L_0x0519
        L_0x00fc:
            java.lang.Object r6 = r5.obj
            java.lang.Thread r6 = (java.lang.Thread) r6
            int r5 = r5.arg1
            r7.finishThreadForGettingHostAddress(r6, r5)
            goto L_0x0519
        L_0x0107:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r7.onUpdateSipDelegateRegistrationTimeOut(r5)
            goto L_0x0519
        L_0x0114:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r7.onUpdateSipDelegateRegistration(r5)
            goto L_0x0519
        L_0x0121:
            java.lang.Object r6 = r5.obj
            java.util.List r6 = (java.util.List) r6
            int r8 = r5.arg1
            int r5 = r5.arg2
            r7.onDnsResponse(r6, r8, r5)
            goto L_0x0519
        L_0x012e:
            int r5 = r5.arg1
            r9.onChatbotAgreementChanged(r5)
            goto L_0x0519
        L_0x0135:
            int r5 = r5.arg1
            r8.onIpsecDisconnected(r5)
            goto L_0x0519
        L_0x013c:
            r6.onRCSAllowedChangedbyMDM()
            goto L_0x0519
        L_0x0141:
            int r5 = r5.arg1
            r8.onEpdgIkeError(r5)
            goto L_0x0519
        L_0x0148:
            java.lang.Object r6 = r5.obj
            com.sec.internal.constants.ims.gls.LocationInfo r6 = (com.sec.internal.constants.ims.gls.LocationInfo) r6
            int r5 = r5.arg1
            if (r5 != r2) goto L_0x0151
            r4 = r2
        L_0x0151:
            r7.updateGeolocation(r6, r4)
            goto L_0x0519
        L_0x0156:
            int r6 = r5.arg1
            if (r6 != r2) goto L_0x015b
            r4 = r2
        L_0x015b:
            int r5 = r5.arg2
            r9.onLteDataNetworkModeSettingChanged(r4, r5)
            goto L_0x0519
        L_0x0162:
            r7.tryRegister()
            goto L_0x0519
        L_0x0167:
            int r6 = r5.arg1
            if (r6 != r2) goto L_0x016c
            r4 = r2
        L_0x016c:
            int r5 = r5.arg2
            r9.onRoamingDataChanged(r4, r5)
            goto L_0x0519
        L_0x0173:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r6 = 42
            r7.sendDeregister((int) r6, (int) r5)
            goto L_0x0519
        L_0x0182:
            r9.onUserSwitched()
            goto L_0x0519
        L_0x0187:
            java.lang.Object r5 = r5.obj
            r6.onRegEventContactUriNotified(r5)
            goto L_0x0519
        L_0x018e:
            int r6 = r5.arg2
            int r5 = r5.arg1
            com.sec.internal.ims.core.RegisterTask r5 = r7.getRegisterTaskByProfileId(r6, r5)
            java.util.Optional r5 = java.util.Optional.ofNullable(r5)
            com.sec.internal.ims.core.RegistrationEvents$$ExternalSyntheticLambda4 r6 = new com.sec.internal.ims.core.RegistrationEvents$$ExternalSyntheticLambda4
            r6.<init>()
            r5.ifPresent(r6)
            goto L_0x0519
        L_0x01a4:
            r6 = 807(0x327, float:1.131E-42)
            int r5 = r5.arg1
            r7.sendDeregister((int) r6, (int) r5)
            goto L_0x0519
        L_0x01ad:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.onLocationCacheExpired(r5)
            goto L_0x0519
        L_0x01b6:
            r6.onRequestLocation()
            goto L_0x0519
        L_0x01bb:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.onLocationTimerExpired(r5)
            goto L_0x0519
        L_0x01c4:
            int r6 = r5.arg1
            if (r6 != r2) goto L_0x01c9
            r4 = r2
        L_0x01c9:
            int r5 = r5.arg2
            r9.onDataUsageLimitReached(r4, r5)
            goto L_0x0519
        L_0x01d0:
            java.lang.Object r5 = r5.obj
            com.sec.internal.helper.AsyncResult r5 = (com.sec.internal.helper.AsyncResult) r5
            java.lang.Object r5 = r5.result
            android.telephony.SubscriptionInfo r5 = (android.telephony.SubscriptionInfo) r5
            r6.onSimSubscribeIdChanged(r5)
            goto L_0x0519
        L_0x01dd:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r8.onDefaultNetworkStateChanged(r5)
            goto L_0x0519
        L_0x01ea:
            java.lang.Object r5 = r5.obj
            android.os.Bundle r5 = (android.os.Bundle) r5
            int r6 = r5.getInt(r3)
            boolean r5 = r5.getBoolean(r1)
            r9.onRTTmodeUpdated(r6, r5)
            goto L_0x0519
        L_0x01fb:
            java.lang.Object r7 = r5.obj
            byte[] r7 = (byte[]) r7
            int r5 = r5.arg1
            r6.onWfcSwitchProfile(r7, r5)
            goto L_0x0519
        L_0x0206:
            java.lang.Object r7 = r5.obj
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r7 = r7.intValue()
            int r8 = r5.arg1
            int r5 = r5.arg2
            r6.onPcoInfo(r7, r8, r5)
            goto L_0x0519
        L_0x0217:
            r7.onActiveDataSubscriptionChanged()
            goto L_0x0519
        L_0x021c:
            java.lang.Object r6 = r5.obj
            com.sec.internal.constants.ims.os.NetworkEvent r6 = (com.sec.internal.constants.ims.os.NetworkEvent) r6
            int r5 = r5.arg1
            r8.onNetworkEventChanged(r6, r5)
            goto L_0x0519
        L_0x0227:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r7.updateRegiConfig(r5)
            goto L_0x0519
        L_0x0234:
            int r5 = r5.arg1
            r6.handleDynamicImsUpdated(r5)
            goto L_0x0519
        L_0x023b:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            int r5 = r5.mPhoneId
            r8.onCheckUnprocessedOmadmConfig(r5)
            goto L_0x0519
        L_0x0246:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.onDisconnectPdnByVolteDisabled(r5)
            goto L_0x0519
        L_0x024f:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.onDisconnectPdnByTimeout(r5)
            goto L_0x0519
        L_0x0258:
            java.lang.Object r6 = r5.obj
            if (r6 != 0) goto L_0x0264
            java.lang.String r5 = "RegistrationEvents"
            java.lang.String r6 = "handleEvent: Ignore EVENT_IMS_PDN_CONNECTING with null obj!"
            com.sec.internal.log.IMSLog.assertUnreachable(r5, r6)
            return r4
        L_0x0264:
            java.lang.Integer r6 = (java.lang.Integer) r6
            int r6 = r6.intValue()
            int r5 = r5.arg1
            r8.onPdnConnecting(r6, r5)
            goto L_0x0519
        L_0x0271:
            int r5 = r5.arg1
            r7.checkUnProcessedVoLTEState(r5)
            goto L_0x0519
        L_0x0278:
            int r5 = r5.arg1
            r9.onEcVbcSettingChanged(r5)
            goto L_0x0519
        L_0x027f:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r7.handleE911RegiTimeOut(r5)
            goto L_0x0519
        L_0x0288:
            int r6 = r5.arg1
            int r5 = r5.arg2
            if (r5 != r2) goto L_0x028f
            r4 = r2
        L_0x028f:
            r7.updateEpdgHandoverEnableChanged(r6, r4)
            goto L_0x0519
        L_0x0294:
            int r6 = r5.arg1
            int r5 = r5.arg2
            r9.onMobileDataPressedChanged(r6, r5, r8)
            goto L_0x0519
        L_0x029d:
            java.lang.Object r6 = r5.obj
            com.sec.internal.ims.core.RegisterTask r6 = (com.sec.internal.ims.core.RegisterTask) r6
            int r8 = r5.arg1
            if (r8 != r2) goto L_0x02a6
            r4 = r2
        L_0x02a6:
            int r5 = r5.arg2
            r7.suspended(r6, r4, r5)
            goto L_0x0519
        L_0x02ad:
            r6.onBootCompleted()
            goto L_0x0519
        L_0x02b2:
            int r5 = r5.arg1
            r6.handleMnoMapUpdated(r5)
            goto L_0x0519
        L_0x02b9:
            java.lang.Object r6 = r5.obj
            java.lang.Integer r6 = (java.lang.Integer) r6
            int r6 = r6.intValue()
            int r5 = r5.arg1
            r9.onRcsUserSettingChanged(r6, r5)
            goto L_0x0519
        L_0x02c8:
            r6.onDsacModeChanged()
            goto L_0x0519
        L_0x02cd:
            java.lang.Object r7 = r5.obj
            com.sec.internal.ims.core.RegisterTask r7 = (com.sec.internal.ims.core.RegisterTask) r7
            int r5 = r5.arg1
            if (r5 != r2) goto L_0x02d6
            r4 = r2
        L_0x02d6:
            r6.onDelayedDeregisterInternal(r7, r4)
            goto L_0x0519
        L_0x02db:
            int r7 = r5.arg1
            int r5 = r5.arg2
            r6.onBlockRegistrationRoamingTimer(r7, r5)
            goto L_0x0519
        L_0x02e4:
            r6.onRcsDelayedDeregister()
            goto L_0x0519
        L_0x02e9:
            int r5 = r5.arg1
            com.sec.internal.ims.core.RegisterTask r6 = r7.getRegisterTaskByRegHandle(r5)
            if (r6 == 0) goto L_0x0519
            r7.onRefreshRegistration(r6, r5)
            goto L_0x0519
        L_0x02f6:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r7.onForcedUpdateRegistrationRequested(r5)
            goto L_0x0519
        L_0x02ff:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r6.onUpdateChatServiceByDmaChange(r5, r2)
            goto L_0x0519
        L_0x030c:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r6.onUpdateChatServiceByDmaChange(r5, r4)
            goto L_0x0519
        L_0x0319:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r6.onDisableChatFeatureBySipForbidden(r5)
            goto L_0x0519
        L_0x0326:
            int r5 = r5.arg1
            r8.handOffEventTimeout(r5)
            goto L_0x0519
        L_0x032d:
            java.lang.Object r6 = r5.obj
            com.sec.internal.ims.core.RegisterTask r6 = (com.sec.internal.ims.core.RegisterTask) r6
            if (r6 == 0) goto L_0x0519
            java.lang.String r8 = "ePDG timeout"
            r6.setReason(r8)
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            com.sec.internal.constants.ims.core.RegistrationConstants$UpdateRegiReason r6 = com.sec.internal.constants.ims.core.RegistrationConstants.UpdateRegiReason.EPDG_TIMEOUT
            r7.updateRegistration((com.sec.internal.ims.core.RegisterTask) r5, (com.sec.internal.constants.ims.core.RegistrationConstants.UpdateRegiReason) r6)
            goto L_0x0519
        L_0x0343:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.doRecoveryAction(r5)
            goto L_0x0519
        L_0x034c:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.handleDelayedStopPdn(r5)
            goto L_0x0519
        L_0x0355:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.onTimsTimerExpired(r5)
            goto L_0x0519
        L_0x035e:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.onRequestNotifyVolteSettingsOff(r5)
            goto L_0x0519
        L_0x0367:
            java.lang.Object r5 = r5.obj
            r6.onRegisterError(r5)
            goto L_0x0519
        L_0x036e:
            int r6 = r5.arg1
            int r5 = r5.arg2
            r9.onRoamingSettingsChanged(r6, r5)
            goto L_0x0519
        L_0x0377:
            r6.onGeoLocationUpdated()
            goto L_0x0519
        L_0x037c:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r5 = r5.getGovernor()
            r5.finishOmadmProvisioningUpdate()
            goto L_0x0519
        L_0x0389:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r5 = r5.getGovernor()
            r5.startOmadmProvisioningUpdate()
            goto L_0x0519
        L_0x0396:
            java.lang.Object r5 = r5.obj
            android.os.Bundle r5 = (android.os.Bundle) r5
            int r6 = r5.getInt(r3)
            boolean r5 = r5.getBoolean(r1)
            r9.onTTYmodeUpdated(r6, r5)
            goto L_0x0519
        L_0x03a7:
            java.lang.Object r5 = r5.obj
            com.sec.internal.helper.AsyncResult r5 = (com.sec.internal.helper.AsyncResult) r5
            java.lang.Object r5 = r5.result
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r7.onSimRefresh(r5)
            goto L_0x0519
        L_0x03b8:
            java.lang.Object r7 = r5.obj
            java.lang.String r7 = (java.lang.String) r7
            int r5 = r5.arg1
            r6.onConfigUpdated(r7, r5)
            goto L_0x0519
        L_0x03c3:
            int r6 = r5.arg1
            int r5 = r5.arg2
            r9.onMobileDataChanged(r6, r5, r8)
            goto L_0x0519
        L_0x03cc:
            int r7 = r5.arg1
            int r5 = r5.arg2
            r6.onTelephonyCallStatusChanged(r7, r5)
            goto L_0x0519
        L_0x03d5:
            r7.onPendingUpdateRegistration()
            goto L_0x0519
        L_0x03da:
            int r6 = r5.arg1
            java.lang.Object r5 = r5.obj
            com.sec.ims.options.Capabilities r5 = (com.sec.ims.options.Capabilities) r5
            r7.onOwnCapabilitiesChanged(r6, r5)
            goto L_0x0519
        L_0x03e5:
            java.lang.Object r5 = r5.obj
            java.security.cert.X509Certificate[] r5 = (java.security.cert.X509Certificate[]) r5
            r6.verifyX509Certificate(r5)
            goto L_0x0519
        L_0x03ee:
            int r5 = r5.arg1
            r6 = 29
            if (r0 != r6) goto L_0x03f5
            r4 = r2
        L_0x03f5:
            r8.onDmConfigCompleted(r5, r4)
            goto L_0x0519
        L_0x03fa:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            int r6 = r5.mPhoneId
            com.sec.internal.ims.core.RegistrationManager$OmadmConfigState r9 = com.sec.internal.ims.core.RegistrationManager.OmadmConfigState.IDLE
            r7.setOmadmState(r6, r9)
            int r5 = r5.mPhoneId
            r8.triggerOmadmConfig(r5)
            goto L_0x0519
        L_0x040c:
            int r5 = r5.arg1
            r8.onEpdgDisconnected(r5)
            goto L_0x0519
        L_0x0413:
            int r5 = r5.arg1
            r8.onEpdgConnected(r5)
            goto L_0x0519
        L_0x041a:
            java.lang.Object r7 = r5.obj
            com.sec.ims.settings.ImsProfile r7 = (com.sec.ims.settings.ImsProfile) r7
            int r5 = r5.arg1
            r6.onUpdateRegistration(r7, r5)
            goto L_0x0519
        L_0x0425:
            int r5 = r5.arg1
            r7.updatePani((int) r5)
            r7.updateTimeInPlani(r5, r4)
            r7.tryRegister((int) r5)
            r7.onPendingUpdateRegistration()
            goto L_0x0519
        L_0x0435:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r8.onPdnDisconnected(r5)
            goto L_0x0519
        L_0x043e:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r8.onPdnConnected(r5)
            goto L_0x0519
        L_0x0447:
            java.lang.Object r5 = r5.obj
            com.sec.internal.helper.AsyncResult r5 = (com.sec.internal.helper.AsyncResult) r5
            java.lang.Object r5 = r5.userObj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r6.handleUiccChanged(r5)
            goto L_0x0519
        L_0x0458:
            java.lang.Object r5 = r5.obj
            com.sec.internal.helper.AsyncResult r5 = (com.sec.internal.helper.AsyncResult) r5
            java.lang.Object r7 = r5.result
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r7 = r7.intValue()
            r6.handleSimReady(r7, r5)
            goto L_0x0519
        L_0x0469:
            int r5 = r5.arg1
            r7.onImsProfileUpdated(r5)
            goto L_0x0519
        L_0x0470:
            int r5 = r5.arg1
            if (r5 != r2) goto L_0x0475
            r4 = r2
        L_0x0475:
            r6.onFlightModeChanged(r4)
            goto L_0x0519
        L_0x047a:
            java.lang.Object r5 = r5.obj
            r6.onSubscribeError(r5)
            goto L_0x0519
        L_0x0481:
            java.lang.Object r5 = r5.obj
            com.sec.internal.interfaces.ims.core.IRegisterTask r5 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r5
            r6.onDeregisterTimeout(r5)
            goto L_0x0519
        L_0x048a:
            java.lang.Object r5 = r5.obj
            r6.onDeregistered(r5)
            goto L_0x0519
        L_0x0491:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r6.onRegistered(r5)
            goto L_0x0519
        L_0x049a:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            int r6 = r5.getPhoneId()
            r8.isPreferredPdnForRCSRegister(r5, r6, r4)
            com.sec.internal.ims.core.RegistrationManagerHandler r6 = r7.mHandler
            int r5 = r5.getPhoneId()
            r7 = 2000(0x7d0, double:9.88E-321)
            r6.sendTryRegister(r5, r7)
            goto L_0x0519
        L_0x04b1:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r7.onImsSwitchUpdated(r5)
            goto L_0x0519
        L_0x04bd:
            java.lang.Object r5 = r5.obj
            android.os.Bundle r5 = (android.os.Bundle) r5
            java.lang.String r6 = "id"
            int r6 = r5.getInt(r6)
            java.lang.String r8 = "explicitDeregi"
            boolean r8 = r5.getBoolean(r8)
            int r5 = r5.getInt(r3)
            r7.onManualDeregister(r6, r8, r5)
            goto L_0x0519
        L_0x04d5:
            java.lang.Object r6 = r5.obj
            com.sec.ims.settings.ImsProfile r6 = (com.sec.ims.settings.ImsProfile) r6
            int r5 = r5.arg1
            r7.onManualRegister(r6, r5)
            goto L_0x0519
        L_0x04df:
            java.lang.Object r5 = r5.obj
            com.sec.internal.ims.core.RegisterTask r5 = (com.sec.internal.ims.core.RegisterTask) r5
            r8.onLocalIpChanged(r5)
            goto L_0x0519
        L_0x04e7:
            java.lang.Object r5 = r5.obj
            com.sec.internal.interfaces.ims.core.IRegisterTask r5 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r5
            int r5 = r5.getPhoneId()
            r7.tryRegister((int) r5)
            goto L_0x0519
        L_0x04f3:
            java.lang.Object r5 = r5.obj
            android.os.Bundle r5 = (android.os.Bundle) r5
            java.lang.String r6 = "networkType"
            int r6 = r5.getInt(r6)
            java.lang.String r7 = "isWifiConnected"
            int r7 = r5.getInt(r7)
            if (r7 != r2) goto L_0x0506
            r4 = r2
        L_0x0506:
            int r5 = r5.getInt(r3)
            r8.onNetworkChanged(r6, r4, r5)
            goto L_0x0519
        L_0x050e:
            java.lang.Object r5 = r5.obj
            java.lang.Integer r5 = (java.lang.Integer) r5
            int r5 = r5.intValue()
            r7.tryRegister((int) r5)
        L_0x0519:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationEvents.handleEvent(android.os.Message, com.sec.internal.ims.core.RegistrationManagerHandler, com.sec.internal.ims.core.RegistrationManagerBase, com.sec.internal.ims.core.NetworkEventController, com.sec.internal.ims.core.UserEventController):boolean");
    }
}
