package com.sec.internal.ims.servicemodules.ss;

import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;

public class UtFeatureData {
    String cbbaic;
    String cbbaoc;
    String cbbicwr;
    String cbboic;
    String cbboic_exhc;
    String cfUriType;
    String cfb;
    String cfni;
    String cfnr;
    String cfnrc;
    String cfu;
    String[] csfbErrorCodeList;
    int delay_disconnect_pdn;
    boolean insertNewRule;
    int ip_version;
    boolean isCBSingleElement;
    boolean isCFSingleElement;
    boolean isDisconnectXcapPdn;
    boolean isErrorMsgDisplay;
    boolean isNeedFirstGet;
    boolean isNeedInternationalNumber;
    boolean isNeedSeparateCFA;
    boolean isNeedSeparateCFNL;
    boolean isNeedSeparateCFNRY;
    boolean isReuseConnection;
    boolean noMediaForCB;
    boolean setAllMediaCF;
    int ssXcapConfigExempt;
    boolean supportAlternativeMediaForCb;
    boolean supportAlternativeMediaForCf;
    boolean supportSimservsRetry;
    boolean support_media;
    boolean support_ss_namespace;
    boolean support_tls;
    int timerFor403;
    int ussdExempt;

    public void setTurnOffGcfCondition() {
        this.isNeedSeparateCFNRY = false;
        this.isNeedSeparateCFNL = false;
    }

    public UtFeatureData(Builder builder) {
        this.support_tls = builder.support_tls;
        this.isCFSingleElement = builder.isCFSingleElement;
        this.isCBSingleElement = builder.isCBSingleElement;
        this.support_media = builder.support_media;
        this.support_ss_namespace = builder.support_ss_namespace;
        this.supportSimservsRetry = builder.supportSimservsRetry;
        this.cfb = builder.cfb;
        this.cfu = builder.cfu;
        this.cfnr = builder.cfnr;
        this.cfnrc = builder.cfnrc;
        this.cfni = builder.cfni;
        this.cbbaic = builder.cbbaic;
        this.cbbicwr = builder.cbbicwr;
        this.cbbaoc = builder.cbbaoc;
        this.cbboic = builder.cbboic;
        this.cbboic_exhc = builder.cbboic_exhc;
        this.timerFor403 = builder.timerFor403;
        this.isNeedSeparateCFNL = builder.isNeedSeparateCFNL;
        this.isNeedSeparateCFNRY = builder.isNeedSeparateCFNRY;
        this.isNeedSeparateCFA = builder.isNeedSeparateCFA;
        this.isNeedFirstGet = builder.isNeedFirstGet;
        this.isErrorMsgDisplay = builder.isErrorMsgDisplay;
        this.isDisconnectXcapPdn = builder.isDisconnectXcapPdn;
        this.isReuseConnection = builder.isReuseConnection;
        this.delay_disconnect_pdn = builder.delay_disconnect_pdn;
        this.csfbErrorCodeList = builder.csfbErrorCodeList;
        this.ip_version = builder.ip_version;
        this.insertNewRule = builder.insertNewRule;
        this.noMediaForCB = builder.noMediaForCB;
        this.setAllMediaCF = builder.setAllMediaCF;
        this.cfUriType = builder.cfUriType;
        this.supportAlternativeMediaForCf = builder.supportAlternativeMediaForCf;
        this.supportAlternativeMediaForCb = builder.supportAlternativeMediaForCb;
        this.isNeedInternationalNumber = builder.isNeedInternationalNumber;
        this.ssXcapConfigExempt = builder.ssXcapConfigExempt;
        this.ussdExempt = builder.ussdExempt;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        protected String cbbaic;
        protected String cbbaoc;
        protected String cbbicwr;
        protected String cbboic;
        protected String cbboic_exhc;
        protected String cfUriType;
        protected String cfb;
        protected String cfni;
        protected String cfnr;
        protected String cfnrc;
        protected String cfu;
        protected String[] csfbErrorCodeList;
        protected int delay_disconnect_pdn;
        protected boolean insertNewRule;
        protected int ip_version;
        protected boolean isCBSingleElement;
        protected boolean isCFSingleElement;
        protected boolean isDisconnectXcapPdn;
        protected boolean isErrorMsgDisplay;
        protected boolean isNeedFirstGet;
        protected boolean isNeedInternationalNumber;
        protected boolean isNeedSeparateCFA;
        protected boolean isNeedSeparateCFNL;
        protected boolean isNeedSeparateCFNRY;
        protected boolean isReuseConnection;
        private int mPhoneId;
        protected boolean noMediaForCB;
        protected boolean setAllMediaCF;
        protected int ssXcapConfigExempt;
        protected boolean supportAlternativeMediaForCb;
        protected boolean supportAlternativeMediaForCf;
        protected boolean supportSimservsRetry;
        protected boolean support_media;
        protected boolean support_ss_namespace;
        protected boolean support_tls;
        protected int timerFor403;
        protected int ussdExempt;

        public UtFeatureData build() {
            this.support_tls = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.SUPPORT_TLS, false);
            this.isCFSingleElement = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.SELECT_MODE, true);
            this.isCBSingleElement = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CB_SELECT_MODE, false);
            this.support_media = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.MEDIA, false);
            this.support_ss_namespace = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.SUPPORT_SS_NAMESPACE, false);
            this.supportSimservsRetry = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.SUPPORT_SIMSERVS_RETRY, true);
            this.cfb = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CF_BUSY_RULEID, "call-diversion-busy-audio");
            this.cfu = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CF_UNCONDITIONAL_RULEID, "call-diversion-unconditional");
            this.cfnr = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CF_NO_ANSWER_RULEID, "call-diversion-no-reply");
            this.cfnrc = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CF_NOT_REACHABLE_RULEID, "call-diversion-not-reachable-audio");
            this.cfni = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CF_NOT_LOGGED_IN_RULEID, "call-diversion-not-logged-in");
            this.cbbaic = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.ICB_UNCONDITIONAL_RULEID, "");
            this.cbbicwr = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.ICB_ROAMING_RULEID, "");
            this.cbbaoc = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.OCB_UNCONDITIONAL_RULEID, "");
            this.cbboic = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.OCB_INTERNATIONAL_RULEID, "");
            this.cbboic_exhc = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.OCB_INTERNATIONAL_EX_HOME_RULEID, "");
            this.timerFor403 = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.TIMER_FOR_403, -1);
            this.isNeedSeparateCFNL = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.NEED_SEPARATE_CFNL, true);
            this.isNeedSeparateCFNRY = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.NEED_SEPARATE_CFNRY, true);
            this.isNeedSeparateCFA = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.NEED_SEPARATE_CFA, false);
            this.isNeedFirstGet = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.IS_NEED_GET_FIRST, true);
            this.isErrorMsgDisplay = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.ERROR_MSG_DISPLAY, false);
            this.isDisconnectXcapPdn = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.DISCONNECT_XCAP_PDN, true);
            this.isReuseConnection = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.REUSE_CONNECTION, false);
            this.delay_disconnect_pdn = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.DELAY_DISCONNECT_XCAP_PDN, 5) * 1000;
            this.csfbErrorCodeList = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CSFB_ERROR_CODE_LIST, new String[]{"all"});
            this.ip_version = UtUtils.doConvertIpVersion(UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.SELECT_IP_VERSION, "default"));
            this.insertNewRule = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.INSERT_NEW_RULE, true);
            this.noMediaForCB = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.NO_MEDIA_FOR_CB, false);
            this.setAllMediaCF = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CF_SET_ALL_MEDIA, false);
            this.cfUriType = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.CF_URI_TYPE, "SIP");
            this.supportAlternativeMediaForCf = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.SUPPORT_ALTERNATIVE_MEDIA_FOR_CF, false);
            this.supportAlternativeMediaForCb = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.SUPPORT_ALTERNATIVE_MEDIA_FOR_CB, false);
            this.isNeedInternationalNumber = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.IS_NEED_INTERNATIONAL_NUMBER, false);
            this.ssXcapConfigExempt = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.SS.XCAP_CONFIG_EXEMPT, 1);
            this.ussdExempt = UtUtils.getSetting(this.mPhoneId, GlobalSettingsConstants.Call.USSD_EXEMPT, 1);
            return new UtFeatureData(this);
        }

        public Builder setPhoneId(int i) {
            this.mPhoneId = i;
            return this;
        }
    }
}
