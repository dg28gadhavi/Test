package com.sec.internal.constants.ims;

import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.core.PaniConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.settings.DeviceConfigManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DiagnosisConstants {
    public static final String CALL_METHOD_LOGANDADD = "logAndAdd";
    public static final String COMMON_KEY_MNO_NAME = "MNON";
    public static final String COMMON_KEY_OMC_NW_CODE = "OMNW";
    public static final String COMMON_KEY_PLMN = "PLMN";
    public static final String COMMON_KEY_SIM_SLOT = "SLOT";
    public static final String COMMON_KEY_SPEC_REVISION = "SREV";
    public static final String COMMON_KEY_VIDEO_SETTINGS = "VILS";
    public static final String COMMON_KEY_VOLTE_SETTINGS = "VLTS";
    public static final String COMPONENT_ID = "Telephony";
    public static final int CS_CALL_EMERGENCY = 3;
    public static final int CS_CALL_VOICE = 1;
    public static final int CS_STATE_INCOMING = 2;
    public static final int CS_STATE_OUTGOING = 1;
    public static final int DIMS_FEATURE_ACTIVE = 2;
    public static final int DIMS_FEATURE_AVAILABLE = 1;
    public static final int DIMS_FEATURE_DISABLED = 0;
    public static final String DMUI_KEY_CALLER_INFO = "USRC";
    public static final String DMUI_KEY_SETTING_TYPE = "DMST";
    public static final int DOWNGRADE_BY_CAMERAFAIL = 3;
    public static final int DOWNGRADE_BY_RTPTIMEOUT = 2;
    public static final int DOWNGRADE_BY_USER = 1;
    public static final String DRCS_KEY_MAAP_FT_MO_SUCCESS = "MFOS";
    public static final String DRCS_KEY_MAAP_GLS_MO_SUCCESS = "MGOS";
    public static final String DRCS_KEY_MAAP_IM_MO_SUCCESS = "MIOS";
    public static final String DRCS_KEY_MAAP_MO_FAIL = "MPOF";
    public static final String DRCS_KEY_MAAP_MO_FAIL_NETWORK = "MOFN";
    public static final String DRCS_KEY_MAAP_MO_FAIL_TERMINAL = "MOFT";
    public static final String DRCS_KEY_MAAP_MO_SUCCESS = "MPOS";
    public static final String DRCS_KEY_MAAP_MT = "MPMT";
    public static final String DRCS_KEY_MAAP_SLM_MO_SUCCESS = "MSOS";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_ADVERTISEMENT = "MPAD";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_NONE = "MPNO";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_PAYMENT = "MPPA";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_PREMIUM = "MPPR";
    public static final String DRCS_KEY_MAAP_TRAFFIC_TYPE_SUBSCRIPTION = "MPSU";
    public static final String DRCS_KEY_RACC = "RACC";
    public static final String DRCS_KEY_RACF = "RACF";
    public static final String DRCS_KEY_RACV = "RACV";
    public static final String DRCS_KEY_RCPC = "RCPC";
    public static final String DRCS_KEY_RCPF = "RCPF";
    public static final String DRCS_KEY_RCSC = "RCSC";
    public static final String DRCS_KEY_RCSF = "RCSF";
    public static final String DRCS_KEY_RCS_CANCEL = "RCCC";
    public static final String DRCS_KEY_RCS_EC_MO_SUCCESS = "REOS";
    public static final String DRCS_KEY_RCS_FT_MO_SUCCESS = "RFOS";
    public static final String DRCS_KEY_RCS_GLS_MO_SUCCESS = "RGOS";
    public static final String DRCS_KEY_RCS_IM_MO_SUCCESS = "RIOS";
    public static final String DRCS_KEY_RCS_MO_FAIL = "RCOF";
    public static final String DRCS_KEY_RCS_MO_FAIL_NETWORK = "ROFN";
    public static final String DRCS_KEY_RCS_MO_FAIL_TERMINAL = "ROFT";
    public static final String DRCS_KEY_RCS_MO_SUCCESS = "RCOS";
    public static final String DRCS_KEY_RCS_MT = "RCMT";
    public static final String DRCS_KEY_RCS_REACTION = "RCRA";
    public static final String DRCS_KEY_RCS_REGI_STATUS = "RCRS";
    public static final String DRCS_KEY_RCS_REPLY = "RCRP";
    public static final String DRCS_KEY_RCS_SLM_MO_SUCCESS = "RSOS";
    public static final String DRCS_KEY_SMS_FALLBACK = "SMFB";
    public static final String DRCS_KEY_SMS_MO_IMS_FAIL = "SOIF";
    public static final String DRCS_KEY_SMS_MO_IMS_SUCCESS = "SOIS";
    public static final String DRCS_KEY_SMS_MT_IMS = "SMTI";
    public static final String DRPT_KEY_CMC_END_FAIL_COUNT = "CMCF";
    public static final String DRPT_KEY_CMC_END_TOTAL_COUNT = "CMCE";
    public static final String DRPT_KEY_CMC_INCOMING_FAIL = "CMMT";
    public static final String DRPT_KEY_CMC_OUTGOING_FAIL = "CMMO";
    public static final String DRPT_KEY_CMC_START_TOTAL_COUNT = "CMCS";
    public static final String DRPT_KEY_CROSS_SIM_ENABLE_SETTINGS = "CSES";
    public static final String DRPT_KEY_CSCALL_END_FAIL_COUNT = "CEFC";
    public static final String DRPT_KEY_CSCALL_END_TOTAL_COUNT = "CETC";
    public static final String DRPT_KEY_CSCALL_INCOMING_FAIL = "CSMT";
    public static final String DRPT_KEY_CSCALL_OUTGOING_FAIL = "CSMO";
    public static final String DRPT_KEY_CSFB_COUNT = "CFCT";
    public static final String DRPT_KEY_DOWNGRADE_TO_VOICE_COUNT = "DWCT";
    public static final String DRPT_KEY_DUAL_IMS_ACTIVE = "DIMS";
    public static final String DRPT_KEY_EPS_FALLBACK_CALL_COUNT = "EFCT";
    public static final String DRPT_KEY_EXPERIENCE_AUDIO_CONFERENCE_COUNT = "EXAC";
    public static final String DRPT_KEY_EXPERIENCE_EMERGENCY_COUNT = "EXEM";
    public static final String DRPT_KEY_EXPERIENCE_RTT_COUNT = "EXRT";
    public static final String DRPT_KEY_EXPERIENCE_TOTAL_COUNT = "EXTC";
    public static final String DRPT_KEY_EXPERIENCE_TTY_COUNT = "EXTY";
    public static final String DRPT_KEY_EXPERIENCE_VIDEO_CONFERENCE_COUNT = "EXVC";
    public static final String DRPT_KEY_EXPERIENCE_VIDEO_COUNT = "EXVI";
    public static final String DRPT_KEY_EXPERIENCE_VOICE_COUNT = "EXVO";
    public static final String DRPT_KEY_FORWARDED_COUNT = "FWCT";
    public static final String DRPT_KEY_MULTIDEVICE_MEP_COUNT = "MDMP";
    public static final String DRPT_KEY_MULTIDEVICE_SOFTPHONE_COUNT = "MDSF";
    public static final String DRPT_KEY_MULTIDEVICE_TOTAL_COUNT = "MDTC";
    public static final String DRPT_KEY_SIM_MOBILITY_ENABLED = "SMMO";
    public static final String DRPT_KEY_SMK_VERSION = "SMKV";
    public static final String DRPT_KEY_SRVCC_COUNT = "SRCT";
    public static final String DRPT_KEY_UPGRADE_TO_VIDEO_COUNT = "UPCT";
    public static final String DRPT_KEY_VOLTE_END_EMERGENCY_COUNT = "VEEM";
    public static final String DRPT_KEY_VOLTE_END_FAIL_COUNT = "VEFC";
    public static final String DRPT_KEY_VOLTE_END_TOTAL_COUNT = "VETC";
    public static final String DRPT_KEY_VOLTE_END_VIDEO_COUNT = "VEVI";
    public static final String DRPT_KEY_VOLTE_END_VOICE_COUNT = "VEVO";
    public static final String DRPT_KEY_VOLTE_INCOMING_FAIL = "PSMT";
    public static final String DRPT_KEY_VOLTE_OUTGOING_FAIL = "PSMO";
    public static final String DRPT_KEY_VONR_END_EMERGENCY_COUNT = "NEEM";
    public static final String DRPT_KEY_VONR_END_FAIL_COUNT = "NEFC";
    public static final String DRPT_KEY_VONR_END_TOTAL_COUNT = "NETC";
    public static final String DRPT_KEY_VONR_END_VIDEO_COUNT = "NEVI";
    public static final String DRPT_KEY_VONR_END_VOICE_COUNT = "NEVO";
    public static final String DRPT_KEY_VONR_START_TOTAL_COUNT = "NSTC";
    public static final String DRPT_KEY_VOWIFI_ENABLE_SETTINGS = "VWES";
    public static final String DRPT_KEY_VOWIFI_END_EMERGENCY_COUNT = "WEEM";
    public static final String DRPT_KEY_VOWIFI_END_FAIL_COUNT = "WEFC";
    public static final String DRPT_KEY_VOWIFI_END_TOTAL_COUNT = "WETC";
    public static final String DRPT_KEY_VOWIFI_END_VIDEO_COUNT = "WEVI";
    public static final String DRPT_KEY_VOWIFI_END_VOICE_COUNT = "WEVO";
    public static final String DRPT_KEY_VOWIFI_INCOMING_FAIL = "VWMT";
    public static final String DRPT_KEY_VOWIFI_OUTGOING_FAIL = "VWMO";
    public static final String DRPT_KEY_VOWIFI_PREF_SETTINGS = "VWPS";
    public static final int EXTERNAL_FEATURE_CEND = 0;
    public static final int EXTERNAL_FEATURE_DROP = 1;
    public static final String FEATURE_DMUI = "DMUI";
    public static final String FEATURE_DRCS = "DRCS";
    public static final String FEATURE_DRPT = "DRPT";
    public static final String FEATURE_ISMO = "ISMO";
    public static final String FEATURE_PSCI = "PSCI";
    public static final String FEATURE_RCSA = "RCSA";
    public static final String FEATURE_RCSC = "RCSC";
    public static final String FEATURE_RCSL = "RCSL";
    public static final String FEATURE_RCSM = "RCSM";
    public static final String FEATURE_RCSP = "RCSP";
    public static final String FEATURE_REGI = "REGI";
    public static final String FEATURE_SIMI = "SIMI";
    public static final String FEATURE_SMOT = "SMOT";
    public static final String FEATURE_UNKNOWN = "UNKNOWN";
    public static final String ISMO_KEY_ORPC = "ORPC";
    public static final String ISMO_KEY_ORST = "ORST";
    public static final String ISMO_KEY_OSIP = "OSIP";
    public static final String KEY_FEATURE = "feature";
    public static final String KEY_NEXT_DRPT_SCHEDULE = "next_drpt_schedule";
    public static final String KEY_OVERWRITE_MODE = "overwrite_mode";
    public static final String KEY_SEND_MODE = "send_mode";
    public static final int MAX_INT = 999999;
    public static final int OVERWRITE_MODE_ADD = 1;
    public static final int OVERWRITE_MODE_REPLACE = 0;
    public static final int OVERWRITE_MODE_REPLACE_IF_BIGGER = 2;
    public static final String PSCI_KEY_CALL_BEARER = "PSCS";
    public static final String PSCI_KEY_CALL_DOWNGRADE = "DWGD";
    public static final String PSCI_KEY_CALL_END_TIME = "CETE";
    public static final String PSCI_KEY_CALL_SETUP_TIME = "CSTE";
    public static final String PSCI_KEY_CALL_STATE = "STAT";
    public static final String PSCI_KEY_CALL_TIME = "CTME";
    public static final String PSCI_KEY_CALL_TYPE = "TYPE";
    public static final String PSCI_KEY_DATA_ROAMING = "ROAM";
    public static final String PSCI_KEY_EPDG_STATUS = "EPDG";
    public static final String PSCI_KEY_FAIL_CODE = "FLCD";
    public static final String PSCI_KEY_LTE_BAND = "BAND";
    public static final String PSCI_KEY_MO_MT = "MOMT";
    public static final String PSCI_KEY_NETWORK_TYPE = "NWTP";
    public static final String PSCI_KEY_PARTICIPANT_NUMBER = "PARN";
    public static final String PSCI_KEY_RAT_CHANGED = "RTCH";
    public static final String PSCI_KEY_RSRP = "RSRP";
    public static final String PSCI_KEY_RSRQ = "RSRQ";
    public static final String PSCI_KEY_SIP_FLOW = "SPFW";
    public static final String RCSA_KEY_ARST = "ARST";
    public static final String RCSA_KEY_ATRE = "ATRE";
    public static final String RCSA_KEY_AVER = "AVER";
    public static final String RCSA_KEY_ERRC = "ERRC";
    public static final String RCSA_KEY_PROF = "PROF";
    public static final String RCSA_KEY_TDRE = "TDRE";
    public static final String RCSC_KEY_NCAP = "NCAP";
    public static final String RCSC_KEY_NRCS = "NRCS";
    public static final String RCSL_KEY_LTCH = "LTCH";
    public static final String RCSM_KEY_FTRC = "FTRC";
    public static final String RCSM_KEY_FTYP = "FTYP";
    public static final String RCSM_KEY_HTTP = "HTTP";
    public static final String RCSM_KEY_ITER = "ITER";
    public static final String RCSM_KEY_MCID = "MCID";
    public static final String RCSM_KEY_MDIR = "MDIR";
    public static final String RCSM_KEY_MGRP = "MGRP";
    public static final String RCSM_KEY_MIID = "MIID";
    public static final String RCSM_KEY_MRAT = "MRAT";
    public static final String RCSM_KEY_MRTY = "MRTY";
    public static final String RCSM_KEY_MRVA = "MRVA";
    public static final String RCSM_KEY_MSIZ = "MSIZ";
    public static final String RCSM_KEY_MSRP = "MSRP";
    public static final String RCSM_KEY_MTYP = "MTYP";
    public static final String RCSM_KEY_ORST = "ORST";
    public static final String RCSM_KEY_PTCN = "PTCN";
    public static final String RCSM_KEY_SIPR = "SIPR";
    public static final String RCSM_KEY_SRSC = "SRSC";
    public static final String RCSM_MDIR_MO = "0";
    public static final String RCSM_MGRP_1_TO_1 = "0";
    public static final String RCSM_MGRP_GROUP = "1";
    public static final String RCSM_MRAT_WIFI_POSTFIX = "_WIFI";
    public static final String RCSM_MTYP_CHATBOT_POSTFIX = "_CHATBOT";
    public static final String RCSM_MTYP_EC = "EC";
    public static final String RCSM_MTYP_FT = "FT";
    public static final String RCSM_MTYP_GLS = "GLS";
    public static final String RCSM_MTYP_IM = "IM";
    public static final String RCSM_MTYP_SLM = "SLM";
    public static final String RCSM_ORST_HTTP = "4";
    public static final String RCSM_ORST_ITER = "5";
    public static final String RCSM_ORST_MSRP = "2";
    public static final String RCSM_ORST_PASS = "0";
    public static final String RCSM_ORST_REGI = "3";
    public static final String RCSM_ORST_SIP = "1";
    public static final String RCSP_KEY_ERES = "ERES";
    public static final String RCSP_KEY_ERRC = "ERRC";
    public static final String RCSP_KEY_SERR = "SERR";
    public static final List<String> REGI_COUNT_KEYS = new ArrayList<String>() {
        {
            add("RGSN");
            add("RGS4");
            add("RGSL");
            add("RGSW");
            add("RGSC");
            add("RGFN");
            add("RGF4");
            add("RGFL");
            add("RGFW");
            add("RGFC");
            add("RRSN");
            add("RRS4");
            add("RRSL");
            add("RRSW");
            add("RRFN");
            add("RRF4");
            add("RRFL");
            add("RRFW");
        }
    };
    public static final String REGI_KEY_DATA_RAT_TYPE = "DRAT";
    public static final String REGI_KEY_DATA_ROAMING = "ROAM";
    public static final String REGI_KEY_FAIL_COUNT = "FALC";
    public static final String REGI_KEY_FAIL_REASON = "FRSN";
    public static final String REGI_KEY_PANI_PREFIX = "PNPR";
    public static final String REGI_KEY_PCSCF_ORDINAL = "PCOD";
    public static final String REGI_KEY_PDN_TYPE = "PDTY";
    public static final String REGI_KEY_REQUEST_CODE = "REQC";
    public static final String REGI_KEY_SERVICE_SET_ALL = "SVCA";
    public static final String REGI_KEY_SERVICE_SET_REGISTERED = "SVCR";
    public static final String REGI_KEY_SIGNAL_STRENGTH = "SIGS";
    public static final int SEND_MODE_INSTANT = 0;
    public static final int SEND_MODE_PENDING = 1;
    public static final String SIMI_KEY_EVENT_TYPE = "EVTT";
    public static final String SIMI_KEY_GID1 = "GID1";
    public static final String SIMI_KEY_ISIM_EXISTS = "ISIM";
    public static final String SIMI_KEY_ISIM_VALIDITY = "ISVL";
    public static final String SIMI_KEY_SIM_VALIDITY = "SMVL";
    public static final String SIMI_KEY_SUBSCRIPTION_ID = "SCID";
    public static final int SMMO_FEATURE_DISABLED = 0;
    public static final int SMMO_FEATURE_ENABLED = 1;
    public static final int SMMO_MOBILITY_AVAILABLE = 4;
    public static final int SMMO_MOBILITY_STATUS = 2;
    public static final String SMOT_KEY_ITER = "ITER";
    public static final String SMOT_KEY_MOMT = "MOMT";
    public static final String SMS_COMMON_KEY_CSDA = "CSDA";
    public static final String SMS_COMMON_KEY_RGST = "RgSt";
    public static final int SPEC_VERSION = 28;

    public enum RCSA_ATRE {
        INIT,
        FROM_REGI,
        FROM_APP,
        SIM_SWAP,
        CHANGE_MSG_APP,
        REJECT_LTE,
        VERSION_ZERO,
        CHANGE_SWVERSION,
        CHANGE_AIRPLANE,
        PUSH_SMS,
        EXPIRE_VALIDITY
    }

    public enum RCSA_TDRE {
        INIT,
        INVALID_TOKEN,
        INVALID_IIDTOKEN,
        UPDATE_REMOTE_CONFIG,
        UPDATE_LOCAL_CONFIG,
        UPDATE_TOKEN,
        DISABLE_RCS,
        FORCE_ACS,
        TOKEN_EXPIRED,
        CHANGE_SWVERSION,
        SIPERROR_UNAUTHORIZED,
        FORBIDDEN_ERROR,
        SIM_REFRESH,
        SIM_CHANGED,
        GCPOLICY_CHANGE
    }

    /* renamed from: com.sec.internal.constants.ims.DiagnosisConstants$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.sec.internal.constants.ims.core.SimConstants$SIM_STATE[] r0 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE = r0
                com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r1 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.core.SimConstants$SIM_STATE r1 = com.sec.internal.constants.ims.core.SimConstants.SIM_STATE.ABSENT     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.constants.ims.DiagnosisConstants.AnonymousClass2.<clinit>():void");
        }
    }

    public static int getEventType(SimConstants.SIM_STATE sim_state, boolean z, boolean z2) {
        int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$core$SimConstants$SIM_STATE[sim_state.ordinal()];
        if (i == 1) {
            return z ? 4 : 1;
        }
        if (i != 2) {
            return 0;
        }
        return z2 ? 2 : 3;
    }

    public enum REGI_REQC {
        UNKNOWN(0),
        INITIAL(1),
        REFRESH(2),
        HAND_OVER(3),
        RE_REGI(4),
        DE_REGI(9);
        
        private int mCode;

        private REGI_REQC(int i) {
            this.mCode = i;
        }

        public int getCode() {
            return this.mCode;
        }
    }

    public enum REGI_FRSN {
        UNKNOWN(0),
        OK(200),
        OK_AFTER_FAIL(201),
        CSC_DISABLED(1000),
        VOPS_OFF(1001),
        USER_SETTINGS_OFF(1002),
        MAIN_SWITCHES_OFF(1003),
        CS_TTY(1004),
        ROAMING_NOT_SUPPORTED(1005),
        LOCATION_NOT_LOADED(1006),
        PDN_ESTABLISH_TIMEOUT(1007),
        ONGOING_NW_MODE_CHANGE(1008),
        EMPTY_PCSCF(1009),
        REGI_THROTTLED(1010),
        FLIGHT_MODE_ON(1011),
        PENDING_RCS_REGI(1012),
        HIGHER_PRIORITY(1013),
        GVN_NOT_READY(1014),
        SIMMANAGER_NULL(1015),
        ENTITLEMENT_NOT_READY(1016),
        RCS_ROAMING(1017),
        TRY_RCS_CONFIG(1018),
        DM_TRIGGERED(1019),
        KDDI_EMERGENCY(1020),
        NETWORK_UNKNOWN(1021),
        NETWORK_SUSPENDED(1022),
        IP4ADDR_NOT_EXIST(1023),
        RCS_ONLY_NEEDED(1024),
        ALREADY_REGISTERING(1025),
        RCS_NOT_DDS(SoftphoneNamespaces.SoftphoneEvents.EVENT_USER_SWITCH_BACK),
        RCS_MUM_DISALLOW_SMS(SoftphoneNamespaces.SoftphoneEvents.EVENT_RETRY_OBTAIN_ACCESS_TOKEN),
        DATA_RAT_IS_NOT_PS_VOICE(1100),
        NW_MODE_CHANGE(Id.REQUEST_VSH_START_SESSION),
        ONGOING_RCS_SESSION(Id.REQUEST_VSH_ACCEPT_SESSION),
        PS_ONLY_OR_CS_ROAMING(Id.REQUEST_VSH_STOP_SESSION),
        DM_EUTRAN_OFF(1104),
        ONGOING_OTA(1105),
        ROAMING_ON_NET_CUSTOM(1106),
        NO_MMTEL_IMS_SWITCH_OFF(Id.REQUEST_SIP_DIALOG_SEND_SIP),
        NO_MMTEL_DM_OFF(Id.REQUEST_SIP_DIALOG_OPEN),
        NO_MMTEL_VOPS_OFF(1202),
        NO_MMTEL_SSAC_BARRING(1203),
        NO_MMTEL_USER_SETTINGS_OFF(1204),
        NO_MMTEL_DSAC(1205),
        NO_MMTEL_INVITE_403(1206),
        NO_MMTEL_VOWIFI_CELLULAR_PREF(1207),
        NO_MMTEL_LIMITED_MODE(1208),
        NO_MMTEL_MPS_DISABLED(1209),
        NO_MMTEL_3G_PREFERRED_MODE(1210),
        NO_MMTEL_EPS_ONLY(1211),
        NO_MMTEL_CS_TTY(1212),
        NON_DDS_CS_ONLY_IN_3G(1213),
        NO_PANI_NO_USER_AGENT(1300),
        RECOVERY_UA_CREATION_FAIL(ImSessionEvent.ADD_PARTICIPANTS),
        RECOVERY_UA_MISMATCH(ImSessionEvent.ADD_PARTICIPANTS_DONE),
        RECOVERY_UA_MISSING(ImSessionEvent.EXTEND_TO_GROUP_CHAT),
        OFFSET_DEREGI_REASON(3000);
        
        private int mCode;

        private REGI_FRSN(int i) {
            this.mCode = i;
        }

        public int getCode() {
            return this.mCode;
        }

        public boolean isOneOf(REGI_FRSN... regi_frsnArr) {
            for (REGI_FRSN regi_frsn : regi_frsnArr) {
                if (this == regi_frsn) {
                    return true;
                }
            }
            return false;
        }

        public static REGI_FRSN valueOf(int i) {
            for (REGI_FRSN regi_frsn : values()) {
                if (regi_frsn.getCode() == i) {
                    return regi_frsn;
                }
            }
            return UNKNOWN;
        }
    }

    public enum PDN {
        DEFAULT(1),
        PDN_IMS(2),
        PDN_WIFI(3),
        PDN_INTERNET(4),
        PDN_EMERGENCY(5);
        
        private final int value;

        private PDN(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static int getPdnType(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("default", 1);
        hashMap.put(DeviceConfigManager.IMS, 2);
        hashMap.put("wifi", 3);
        hashMap.put("internet", 4);
        hashMap.put("emergency", 5);
        return ((Integer) hashMap.entrySet().stream().filter(new DiagnosisConstants$$ExternalSyntheticLambda0(str)).findFirst().map(new DiagnosisConstants$$ExternalSyntheticLambda1()).orElse(0)).intValue();
    }

    public static int getPaniPrefix(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        HashMap hashMap = new HashMap();
        hashMap.put(PaniConstants.LTE_PANI_PREFIX, 4);
        hashMap.put(PaniConstants.IWLAN_PANI_PREFIX, 6);
        hashMap.put(PaniConstants.NR_PANI_PREFIX, 7);
        hashMap.put(PaniConstants.NR_PANI_PREFIX_FDD, 8);
        hashMap.put(PaniConstants.NR_PANI_PREFIX_TDD, 9);
        hashMap.put(PaniConstants.UMTS_PANI_PREFIX, 3);
        hashMap.put(PaniConstants.TDLTE_PANI_PREFIX, 5);
        hashMap.put(PaniConstants.EDGE_PANI_PREFIX, 1);
        hashMap.put(PaniConstants.EHRPD_PANI_PREFIX, 2);
        return ((Integer) hashMap.entrySet().stream().filter(new DiagnosisConstants$$ExternalSyntheticLambda2(str)).findFirst().map(new DiagnosisConstants$$ExternalSyntheticLambda1()).orElse(0)).intValue();
    }

    public enum REGI_PROFILE {
        UNKNOWN(0),
        SMSIP(1),
        MMTEL(2),
        MMTEL_VIDEO(4),
        PRESENCE(8),
        IM(16),
        FT(32),
        FT_HTTP(64),
        OPTIONS(128),
        IS(256),
        VS(512),
        EC(1024),
        GLS(2048),
        SLM(4096),
        EUC(8192),
        PROFILE(16384),
        CDPN(32768),
        LASTSEEN(65536),
        CHATBOT_COMMUNICATION(131072),
        MMTEL_CALL_COMPOSER(262144),
        DATACHANNEL(524288);
        
        private int mValue;

        private REGI_PROFILE(int i) {
            this.mValue = i;
        }

        public int getValue() {
            return this.mValue;
        }

        public boolean compare(String str) {
            return !TextUtils.isEmpty(str) && normalizeSvcName(str).equalsIgnoreCase(normalizeSvcName(toString()));
        }

        public static REGI_PROFILE fromService(String str) {
            for (REGI_PROFILE regi_profile : values()) {
                if (regi_profile.compare(str)) {
                    return regi_profile;
                }
            }
            return UNKNOWN;
        }

        private String normalizeSvcName(String str) {
            return str.replaceAll("[\\W_]", "");
        }
    }

    public static String convertServiceSetToHex(Set<String> set) {
        if (CollectionUtils.isNullOrEmpty((Collection<?>) set)) {
            return intToHexStr(0);
        }
        int i = 0;
        for (String str : ImsProfile.getVoLteServiceList()) {
            if (set.contains(str)) {
                i |= REGI_PROFILE.fromService(str).getValue();
            }
        }
        for (String str2 : ImsProfile.getRcsServiceList()) {
            if (set.contains(str2)) {
                i = REGI_PROFILE.fromService(str2).getValue() | i;
            }
        }
        return intToHexStr(i);
    }

    public enum EPDG_STATUS {
        UNAVAILABLE(0),
        AVAILABLE(1),
        AVAILABLE_MOBILE_DATA_PHYSICAL_INTERFACE(2);
        
        private final int value;

        private EPDG_STATUS(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum CALL_BEARER {
        CS(0),
        LTE(1),
        WLAN(2),
        NR(3);
        
        private final int value;

        private CALL_BEARER(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static String intToHexStr(int i) {
        return String.format("0x%8s", new Object[]{Integer.toHexString(i).toUpperCase()}).replace(' ', '0');
    }
}
