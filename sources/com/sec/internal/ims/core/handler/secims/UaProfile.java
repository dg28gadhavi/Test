package com.sec.internal.ims.core.handler.secims;

import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UaProfile {
    public static final int TEXT_MODE_CS_TTY = 1;
    public static final int TEXT_MODE_NONE = 0;
    public static final int TEXT_MODE_PS_TTY = 2;
    public static final int TEXT_MODE_RTT = 3;
    List<String> acb;
    String accessToken;
    int activeDataPhoneId;
    boolean addHistinfo;
    boolean addMmtelCallComposerTag;
    int audioEngineType;
    String authServerUrl;
    String authalg;
    CallProfile callProfile;
    Capabilities capabilities;
    String cmcEmergencyNumbers;
    String cmcRelayType;
    int cmcType;
    String contactDisplayName;
    int controlDscp;
    String curPani;
    int dbrTimer;
    String displayName;
    String domain;
    int earlyMediaRtpTimeoutTimer;
    boolean enableVerstat;
    boolean encrNullRoaming;
    String encralg;
    boolean excludePaniVowifiInitialRegi;
    int hashAlgoType;
    String hostname;
    String iface;
    boolean ignoreDisplayName;
    String imMsgTech;
    String impi;
    String impu;
    int impuPreference;
    String imsiBasedImpu;
    String instanceId;
    boolean isCdmalessEnabled;
    boolean isEmergencyProfile;
    boolean isEnableGruu;
    boolean isEnableSessionId;
    boolean isEnableVcid;
    boolean isFullCodecOfferRequired;
    boolean isGcfConfigEnabled;
    boolean isMsrpBearerUsed;
    boolean isNsdsServiceEnabled;
    boolean isPrecondEnabled;
    boolean isPrecondInitialSendrecv;
    boolean isPttSupported;
    boolean isRcsTelephonyFeatureTagRequired;
    boolean isSimMobility;
    boolean isSoftphoneEnabled;
    boolean isSubscribeReg;
    boolean isTcpGracefulShutdownEnabled;
    boolean isTlsEnabled;
    boolean isTransportNeeded;
    boolean isUpdateSaOnStartSupported;
    boolean isVceConfigEnabled;
    boolean isXqEnabled;
    boolean isipsec;
    boolean issipoutbound;
    int keepAliveFactor;
    String lastPaniHeader;
    List<String> linkedImpuList;
    boolean mIsServerHeaderEnabled;
    boolean mIsWifiPreConditionEnabled;
    boolean mUseCompactHeader;
    int minSe;
    Mno mno;
    String msrpTransType;
    int mssSize;
    String mvno;
    boolean needCheckAllowedMethodForRefresh;
    int needPidfRat;
    int needPidfSipMsg;
    boolean needVolteRetryInNr;
    long netId;
    String oipFromPreferred;
    String password;
    String pcscfIp;
    int pcscfPort;
    String pdn;
    int phoneId;
    String preferredId;
    String privacyHeaderRestricted;
    int profileId;
    int qparam;
    int rat;
    int rcsProfile;
    String realm;
    int regExpires;
    int regRetryBaseTime;
    int regRetryMaxTime;
    String registeralgo;
    ImsUri.UriType remoteuritype;
    boolean retryInviteOnTcpReset;
    int ringbackTimer;
    int ringingTimer;
    int scmVersion;
    String selectTransportAfterTcpReset;
    int selfPort;
    boolean send18xReliably;
    boolean sendByeForUssi;
    Set<String> serviceList;
    int sessionExpires;
    String sessionRefresher;
    boolean singleRegiEnabled;
    int sipMobility;
    int srvccVersion;
    int sslType;
    int subscriberTimer;
    boolean support183ForIr92v9Precondition;
    boolean support199ProvisionalResponse;
    boolean supportAccessType;
    int supportB2cCallcomposerWithoutFeaturetag;
    boolean supportDualRcs;
    boolean supportDualSimCmc;
    boolean supportEct;
    boolean supportImsNotAvailable;
    boolean supportLtePreferred;
    boolean supportNetworkInitUssi;
    boolean supportReplaceMerge;
    boolean supportRfc6337ForDelayedOffer;
    boolean supportSubscribeDialogEvent;
    boolean supportUac;
    boolean supportUpgradePrecondition;
    int supportedGeolocationPhase;
    int tcpRstUacErrorcode;
    int tcpRstUasErrorcode;
    int textMode;
    int timer1;
    int timer2;
    int timer4;
    int timerA;
    int timerB;
    int timerC;
    int timerD;
    int timerE;
    int timerF;
    int timerG;
    int timerH;
    int timerI;
    int timerJ;
    int timerK;
    int timerTS;
    String transtype;
    boolean tryReregisterFromKeepalive;
    List<String> uacSipList;
    boolean use183OnProgressIncoming;
    boolean use200offerWhenRemoteNotSupport100rel;
    boolean useKeepAlive;
    boolean usePemHeader;
    boolean useProvisionalResponse100rel;
    boolean useQ850causeOn480;
    boolean useSubcontactWhenResub;
    String userAgent;
    String uuid;
    int videoCrbtSupportType;
    int vowifi5gsaMode;

    public int getProfileId() {
        return this.profileId;
    }

    public String getIface() {
        return this.iface;
    }

    public long getNetId() {
        return this.netId;
    }

    public String getPdn() {
        return this.pdn;
    }

    public String getImpi() {
        return this.impi;
    }

    public String getImpu() {
        return this.impu;
    }

    public void setImpu(String str) {
        this.impu = str;
    }

    public String getImsiBasedImpu() {
        return this.imsiBasedImpu;
    }

    public void setImsiBasedImpu(String str) {
        this.imsiBasedImpu = str;
    }

    public List<String> getLinkedImpuList() {
        return this.linkedImpuList;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean isIsSipOutbound() {
        return this.issipoutbound;
    }

    public int getQparam() {
        return this.qparam;
    }

    public int getControlDscp() {
        return this.controlDscp;
    }

    public String getTranstype() {
        return this.transtype;
    }

    public boolean isIsEmergencyProfile() {
        return this.isEmergencyProfile;
    }

    public boolean isIsipsec() {
        return this.isipsec;
    }

    public boolean isWifiPreConditionEnabled() {
        return this.mIsWifiPreConditionEnabled;
    }

    public boolean isServerHeaderEnabled() {
        return this.mIsServerHeaderEnabled;
    }

    public boolean shouldUseCompactHeader() {
        return this.mUseCompactHeader;
    }

    public String getEncralg() {
        return this.encralg;
    }

    public String getAuthalg() {
        return this.authalg;
    }

    public boolean isTlsEnabled() {
        return this.isTlsEnabled;
    }

    public String getRegisteralgo() {
        return this.registeralgo;
    }

    public String getpreferredId() {
        return this.preferredId;
    }

    public ImsUri.UriType getRemoteuritype() {
        return this.remoteuritype;
    }

    public String getPcscfIp() {
        return this.pcscfIp;
    }

    public void setPcscfIp(String str) {
        this.pcscfIp = str;
    }

    public int getPcscfPort() {
        return this.pcscfPort;
    }

    public int getRegExpires() {
        return this.regExpires;
    }

    public Set<String> getServiceList() {
        return this.serviceList;
    }

    public void setServiceList(Set<String> set) {
        this.serviceList = set;
    }

    public void setLinkedImpuList(List<String> list) {
        this.linkedImpuList = list;
    }

    public Capabilities getOwnCapabilities() {
        return this.capabilities;
    }

    public void setOwnCapabilities(Capabilities capabilities2) {
        try {
            this.capabilities = capabilities2.clone();
        } catch (CloneNotSupportedException unused) {
            this.capabilities = null;
        }
    }

    public Mno getMno() {
        return this.mno;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public int getSipMobility() {
        return this.sipMobility;
    }

    public void setCallProfile(CallProfile callProfile2) {
        this.callProfile = callProfile2;
    }

    public CallProfile getCallProfile() {
        return this.callProfile;
    }

    public int getTimer1() {
        return this.timer1;
    }

    public int getTimer2() {
        return this.timer2;
    }

    public int getTimer4() {
        return this.timer4;
    }

    public int getTimerA() {
        return this.timerA;
    }

    public int getTimerB() {
        return this.timerB;
    }

    public int getTimerC() {
        return this.timerC;
    }

    public int getTimerD() {
        return this.timerD;
    }

    public int getTimerE() {
        return this.timerE;
    }

    public int getTimerF() {
        return this.timerF;
    }

    public int getTimerG() {
        return this.timerG;
    }

    public int getTimerH() {
        return this.timerH;
    }

    public int getTimerI() {
        return this.timerI;
    }

    public int getTimerJ() {
        return this.timerJ;
    }

    public int getTimerK() {
        return this.timerK;
    }

    public int getTimerTS() {
        return this.timerTS;
    }

    public int getMssSize() {
        return this.mssSize;
    }

    public int getRingbackTimer() {
        return this.ringbackTimer;
    }

    public int getRingingTimer() {
        return this.ringingTimer;
    }

    public boolean getIsEnableGruu() {
        return this.isEnableGruu;
    }

    public boolean getIsEnableVcid() {
        return this.isEnableVcid;
    }

    public boolean getIsEnableSessionId() {
        return this.isEnableSessionId;
    }

    public int getAudioEngineType() {
        return this.audioEngineType;
    }

    public int getTextMode() {
        return this.textMode;
    }

    public String getCurPani() {
        return this.curPani;
    }

    public void setCurPani(String str) {
        this.curPani = str;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public String getUuid() {
        return this.uuid;
    }

    public boolean getisSubscribeReg() {
        return this.isSubscribeReg;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getAuthServerUrl() {
        return this.authServerUrl;
    }

    public boolean getIsXqEnabled() {
        return this.isXqEnabled;
    }

    public int getRcsProfile() {
        return this.rcsProfile;
    }

    public boolean getIsTransportNeeded() {
        return this.isTransportNeeded;
    }

    public int getRat() {
        return this.rat;
    }

    public int getDbrTimer() {
        return this.dbrTimer;
    }

    public boolean getIsTcpGracefulShutdownEnabled() {
        return this.isTcpGracefulShutdownEnabled;
    }

    public int getTcpRstUacErrorcode() {
        return this.tcpRstUacErrorcode;
    }

    public int getTcpRstUasErrorcode() {
        return this.tcpRstUasErrorcode;
    }

    public String getPrivacyHeaderRestricted() {
        return this.privacyHeaderRestricted;
    }

    public boolean getUsePemHeader() {
        return this.usePemHeader;
    }

    public int getPhoneId() {
        return this.phoneId;
    }

    public boolean getSupportEct() {
        return this.supportEct;
    }

    public boolean getAddHistinfo() {
        return this.addHistinfo;
    }

    public int getEarlyMediaRtpTimeoutTimer() {
        return this.earlyMediaRtpTimeoutTimer;
    }

    public int getSupportedGeolocationPhase() {
        return this.supportedGeolocationPhase;
    }

    public int getNeedPidfSipMsg() {
        return this.needPidfSipMsg;
    }

    public int getNeedPidfRat() {
        return this.needPidfRat;
    }

    public boolean getUseProvisionalResponse100rel() {
        return this.useProvisionalResponse100rel;
    }

    public boolean getUse183OnProgressIncoming() {
        return this.use183OnProgressIncoming;
    }

    public boolean getUseQ850causeOn480() {
        return this.useQ850causeOn480;
    }

    public boolean getSupport183ForIr92v9Precondition() {
        return this.support183ForIr92v9Precondition;
    }

    public boolean getSupportImsNotAvailable() {
        return this.supportImsNotAvailable;
    }

    public boolean getSupportLtePreferred() {
        return this.supportLtePreferred;
    }

    public boolean getUseSubcontactWhenResub() {
        return this.useSubcontactWhenResub;
    }

    public boolean getSupportUpgradePrecondition() {
        return this.supportUpgradePrecondition;
    }

    public boolean getSupportReplaceMerge() {
        return this.supportReplaceMerge;
    }

    public boolean getSupportAccessType() {
        return this.supportAccessType;
    }

    public String getLastPaniHeader() {
        return this.lastPaniHeader;
    }

    public String getOipFromPreferred() {
        return this.oipFromPreferred;
    }

    public String getSelectTransportAfterTcpReset() {
        return this.selectTransportAfterTcpReset;
    }

    public int getSrvccVersion() {
        return this.srvccVersion;
    }

    public boolean getIsSimMobility() {
        return this.isSimMobility;
    }

    public int getCmcType() {
        return this.cmcType;
    }

    public String getCmcRelayType() {
        return this.cmcRelayType;
    }

    public String getCmcEmergencyNumbers() {
        return this.cmcEmergencyNumbers;
    }

    public boolean getSupportDualSimCmc() {
        return this.supportDualSimCmc;
    }

    public int getVideoCrbtSupportType() {
        return this.videoCrbtSupportType;
    }

    public boolean getRetryInviteOnTcpReset() {
        return this.retryInviteOnTcpReset;
    }

    public int getRegRetryBaseTime() {
        return this.regRetryBaseTime;
    }

    public int getRegRetryMaxTime() {
        return this.regRetryMaxTime;
    }

    public boolean getEnableVerstat() {
        return this.enableVerstat;
    }

    public boolean getSupportDualRcs() {
        return this.supportDualRcs;
    }

    public boolean getTryReregisterFromKeepalive() {
        return this.tryReregisterFromKeepalive;
    }

    public boolean getIsPttSupported() {
        return this.isPttSupported;
    }

    public int getSslType() {
        return this.sslType;
    }

    public boolean getSupport199ProvisionalResponse() {
        return this.support199ProvisionalResponse;
    }

    public boolean getSend18xReliably() {
        return this.send18xReliably;
    }

    public List<String> getAcb() {
        return this.acb;
    }

    public boolean isDisplayNameIgnored() {
        return this.ignoreDisplayName;
    }

    public boolean getSupportNetworkInitUssi() {
        return this.supportNetworkInitUssi;
    }

    public boolean getSendByeForUssi() {
        return this.sendByeForUssi;
    }

    public boolean getSupportRfc6337ForDelayedOffer() {
        return this.supportRfc6337ForDelayedOffer;
    }

    public boolean getUse200offerWhenRemoteNotSupport100rel() {
        return this.use200offerWhenRemoteNotSupport100rel;
    }

    public int getVowifi5gsaMode() {
        return this.vowifi5gsaMode;
    }

    public boolean getExcludePaniVowifiInitialRegi() {
        return this.excludePaniVowifiInitialRegi;
    }

    public int getHashAlgoTypeType() {
        return this.hashAlgoType;
    }

    public void setSingleRegiEnabled(boolean z) {
        this.singleRegiEnabled = z;
    }

    public void setAddMmtelCallComposerTag(boolean z) {
        this.addMmtelCallComposerTag = z;
    }

    public boolean getSingleRegiEnabled() {
        return this.singleRegiEnabled;
    }

    public void setNeedCheckAllowedMethodForRefresh(boolean z) {
        this.needCheckAllowedMethodForRefresh = z;
    }

    public boolean getNeedCheckAllowedMethodForRefresh() {
        return this.needCheckAllowedMethodForRefresh;
    }

    public void setKeepAliveFactor(int i) {
        this.keepAliveFactor = i;
    }

    public int getKeepAliveFactor() {
        return this.keepAliveFactor;
    }

    public void setSupportUac(boolean z) {
        this.supportUac = z;
    }

    public boolean getSupportUac() {
        return this.supportUac;
    }

    public void setUacSipList(List<String> list) {
        this.uacSipList = list;
    }

    public List<String> getUacSipList() {
        return this.uacSipList;
    }

    public void setImMsgTech(String str) {
        this.imMsgTech = str;
    }

    public String getImMsgTech() {
        return this.imMsgTech;
    }

    public boolean getEncrNullRoaming() {
        return this.encrNullRoaming;
    }

    public boolean getIsAddMmtelCallComposerTag() {
        return this.addMmtelCallComposerTag;
    }

    public boolean getNeedVolteRetryInNr() {
        return this.needVolteRetryInNr;
    }

    public int getImpuPreference() {
        return this.impuPreference;
    }

    public boolean isUpdateSaOnStartSupported() {
        return this.isUpdateSaOnStartSupported;
    }

    public int getSupportB2cCallcomposerWithoutFeaturetag() {
        return this.supportB2cCallcomposerWithoutFeaturetag;
    }

    public UaProfile(Builder builder) {
        this.profileId = builder.profileId;
        this.iface = builder.iface;
        this.netId = builder.netId;
        this.pdn = builder.pdn;
        this.impi = builder.impi;
        this.impu = builder.impu;
        this.imsiBasedImpu = builder.imsiBasedImpu;
        this.linkedImpuList = builder.impuList;
        this.domain = builder.domain;
        this.password = builder.password;
        this.issipoutbound = builder.issipoutbound;
        this.qparam = builder.qparam;
        this.controlDscp = builder.controlDscp;
        this.transtype = builder.transtype;
        this.isEmergencyProfile = builder.isemergencysupport;
        this.isipsec = builder.isipsec;
        this.mIsWifiPreConditionEnabled = builder.mIsWifiPreConditionEnabled;
        this.mIsServerHeaderEnabled = builder.mIsServerHeaderEnabled;
        this.mUseCompactHeader = builder.mUseCompactHeader;
        this.encralg = builder.encralg;
        this.authalg = builder.authalg;
        this.isTlsEnabled = builder.isenabletlsforsip;
        this.registeralgo = builder.registeralgo;
        this.preferredId = builder.preferredId;
        this.remoteuritype = builder.remoteuritype;
        this.mno = builder.mno;
        this.mvno = builder.mvno;
        this.hostname = builder.hostname;
        this.pcscfIp = builder.pcscfIp;
        this.pcscfPort = builder.pcscfPort;
        this.serviceList = builder.serviceList;
        try {
            this.capabilities = builder.capabilities.clone();
        } catch (CloneNotSupportedException | NullPointerException unused) {
            this.capabilities = null;
        }
        this.isPrecondEnabled = builder.isprecondenabled;
        this.isPrecondInitialSendrecv = builder.ispreconinitialsendrecv;
        this.isRcsTelephonyFeatureTagRequired = builder.isRcsTelephonyFeatureTagRequired;
        this.isFullCodecOfferRequired = builder.isFullCodecOfferRequired;
        this.sessionExpires = builder.sessionexpires;
        this.minSe = builder.minSe;
        this.sessionRefresher = builder.sessionrefresher;
        this.regExpires = builder.regExpires;
        this.userAgent = builder.userAgent;
        this.displayName = builder.displayName;
        this.contactDisplayName = builder.contactDisplayName;
        this.uuid = builder.uuid;
        this.instanceId = builder.instanceId;
        this.realm = builder.realm;
        this.imMsgTech = builder.imMsgTech;
        this.callProfile = builder.callProfile;
        this.mssSize = builder.mssSize;
        this.sipMobility = builder.sipMobility;
        this.timer1 = builder.timer1;
        this.timer2 = builder.timer2;
        this.timer4 = builder.timer4;
        this.timerA = builder.timerA;
        this.timerB = builder.timerB;
        this.timerC = builder.timerC;
        this.timerD = builder.timerD;
        this.timerE = builder.timerE;
        this.timerF = builder.timerF;
        this.timerG = builder.timerG;
        this.timerH = builder.timerH;
        this.timerI = builder.timerI;
        this.timerJ = builder.timerJ;
        this.timerK = builder.timerK;
        this.timerTS = builder.timerTS;
        this.isSoftphoneEnabled = builder.isSoftphoneEnabled;
        this.isCdmalessEnabled = builder.isCdmalessEnabled;
        this.ringbackTimer = builder.ringbackTimer;
        this.ringingTimer = builder.ringingTimer;
        this.isEnableGruu = builder.isEnableGruu;
        this.isEnableVcid = builder.isEnableVcid;
        this.isEnableSessionId = builder.isEnableSessionId;
        this.audioEngineType = builder.audioEngineType;
        this.curPani = builder.curPani;
        this.isVceConfigEnabled = builder.isVceConfigEnabled;
        this.isGcfConfigEnabled = builder.isGcfConfigEnabled;
        this.isNsdsServiceEnabled = builder.isNsdsServiceEnabled;
        this.isMsrpBearerUsed = builder.isMsrpBearerUsed;
        this.subscriberTimer = builder.subscriberTimer;
        this.isSubscribeReg = builder.isSubscribeReg;
        this.accessToken = builder.accessToken;
        this.authServerUrl = builder.authServerUrl;
        this.useKeepAlive = builder.useKeepAlive;
        this.selfPort = builder.selfPort;
        this.scmVersion = builder.scmVersion;
        this.activeDataPhoneId = builder.activeDataPhoneId;
        this.msrpTransType = builder.msrpTransType;
        this.isXqEnabled = builder.isXqEnabled;
        this.textMode = builder.textMode;
        this.rcsProfile = builder.rcsProfile;
        this.isTransportNeeded = builder.isTransportNeeded;
        this.rat = builder.rat;
        this.dbrTimer = builder.dbrTimer;
        this.isTcpGracefulShutdownEnabled = builder.isTcpGracefulShutdownEnabled;
        this.tcpRstUacErrorcode = builder.tcpRstUacErrorcode;
        this.tcpRstUasErrorcode = builder.tcpRstUasErrorcode;
        this.privacyHeaderRestricted = builder.privacyHeaderRestricted;
        this.usePemHeader = builder.usePemHeader;
        this.phoneId = builder.phoneId;
        this.supportEct = builder.supportEct;
        this.earlyMediaRtpTimeoutTimer = builder.earlyMediaRtpTimeoutTimer;
        this.addHistinfo = builder.addHistinfo;
        this.supportedGeolocationPhase = builder.supportedGeolocationPhase;
        this.needPidfSipMsg = builder.needPidfSipMsg;
        this.needPidfRat = builder.needPidfRat;
        this.useProvisionalResponse100rel = builder.useProvisionalResponse100rel;
        this.use183OnProgressIncoming = builder.use183OnProgressIncoming;
        this.useQ850causeOn480 = builder.useQ850causeOn480;
        this.support183ForIr92v9Precondition = builder.support183ForIr92v9Precondition;
        this.supportImsNotAvailable = builder.supportImsNotAvailable;
        this.supportLtePreferred = builder.supportLtePreferred;
        this.useSubcontactWhenResub = builder.useSubcontactWhenResub;
        this.supportUpgradePrecondition = builder.supportUpgradePrecondition;
        this.supportReplaceMerge = builder.supportReplaceMerge;
        this.supportAccessType = builder.supportAccessType;
        this.lastPaniHeader = builder.lastPaniHeader;
        this.oipFromPreferred = builder.oipFromPreferred;
        this.selectTransportAfterTcpReset = builder.selectTransportAfterTcpReset;
        this.srvccVersion = builder.srvccVersion;
        this.supportSubscribeDialogEvent = builder.supportScribeDialogEvent;
        this.isSimMobility = builder.isSimMobility;
        this.cmcType = builder.cmcType;
        this.cmcRelayType = builder.cmcRelayType;
        this.cmcEmergencyNumbers = builder.cmcEmergencyNumbers;
        this.supportDualSimCmc = builder.supportDualSimCmc;
        this.videoCrbtSupportType = builder.videoCrbtSupportType;
        this.retryInviteOnTcpReset = builder.retryInviteOnTcpReset;
        this.enableVerstat = builder.enableVerstat;
        this.regRetryBaseTime = builder.regRetryBaseTime;
        this.regRetryMaxTime = builder.regRetryMaxTime;
        this.supportDualRcs = builder.supportDualRcs;
        this.tryReregisterFromKeepalive = builder.tryReregisterFromKeepalive;
        this.isPttSupported = builder.isPttSupported;
        this.sslType = builder.sslType;
        this.support199ProvisionalResponse = builder.support199ProvisionalResponse;
        this.send18xReliably = builder.send18xReliably;
        this.acb = builder.acb;
        this.ignoreDisplayName = builder.ignoreDisplayName;
        this.supportNetworkInitUssi = builder.supportNetworkInitUssi;
        this.sendByeForUssi = builder.sendByeForUssi;
        this.supportRfc6337ForDelayedOffer = builder.supportRfc6337ForDelayedOffer;
        this.use200offerWhenRemoteNotSupport100rel = builder.use200offerWhenRemoteNotSupport100rel;
        this.hashAlgoType = builder.hashAlgoType;
        this.vowifi5gsaMode = builder.vowifi5gsaMode;
        this.excludePaniVowifiInitialRegi = builder.excludePaniVowifiInitialRegi;
        this.singleRegiEnabled = builder.singleRegiEnabled;
        this.needCheckAllowedMethodForRefresh = builder.needCheckAllowedMethodForRefresh;
        this.addMmtelCallComposerTag = builder.addMmtelCallComposerTag;
        this.keepAliveFactor = builder.keepAliveFactor;
        this.encrNullRoaming = builder.encrNullRoaming;
        this.supportUac = builder.supportUac;
        this.uacSipList = builder.uacSipList;
        this.needVolteRetryInNr = builder.needVolteRetryInNr;
        this.impuPreference = builder.impuPreference;
        this.isUpdateSaOnStartSupported = builder.isUpdateSaOnStartSupported;
        this.supportB2cCallcomposerWithoutFeaturetag = builder.supportB2cCallcomposerWithoutFeaturetag;
    }

    public static class Builder {
        List<String> acb;
        String accessToken;
        int activeDataPhoneId;
        boolean addHistinfo;
        boolean addMmtelCallComposerTag;
        int audioEngineType;
        String authServerUrl;
        String authalg;
        CallProfile callProfile;
        Capabilities capabilities;
        String cmcEmergencyNumbers;
        String cmcRelayType;
        int cmcType;
        String contactDisplayName;
        int controlDscp;
        String curPani;
        int dbrTimer;
        String displayName;
        String domain;
        int earlyMediaRtpTimeoutTimer;
        boolean enableVerstat;
        boolean encrNullRoaming;
        String encralg;
        boolean excludePaniVowifiInitialRegi;
        int hashAlgoType;
        String hostname;
        String iface;
        boolean ignoreDisplayName;
        String imMsgTech;
        String impi;
        String impu;
        List<String> impuList;
        int impuPreference;
        String imsiBasedImpu;
        String instanceId;
        boolean isCdmalessEnabled;
        boolean isEnableGruu;
        boolean isEnableSessionId;
        boolean isEnableVcid;
        boolean isFullCodecOfferRequired;
        boolean isGcfConfigEnabled;
        boolean isMsrpBearerUsed;
        boolean isNsdsServiceEnabled;
        boolean isPttSupported;
        boolean isRcsTelephonyFeatureTagRequired;
        boolean isSimMobility;
        boolean isSoftphoneEnabled;
        boolean isSubscribeReg;
        boolean isTcpGracefulShutdownEnabled;
        boolean isTransportNeeded;
        boolean isUpdateSaOnStartSupported;
        boolean isVceConfigEnabled;
        boolean isXqEnabled;
        boolean isemergencysupport;
        boolean isenabletlsforsip;
        boolean isipsec;
        boolean isprecondenabled;
        boolean ispreconinitialsendrecv;
        boolean issipoutbound;
        int keepAliveFactor;
        String lastPaniHeader;
        boolean mIsServerHeaderEnabled;
        boolean mIsWifiPreConditionEnabled;
        boolean mUseCompactHeader;
        int minSe;
        Mno mno;
        String msrpTransType;
        int mssSize;
        String mvno;
        boolean needCheckAllowedMethodForRefresh;
        int needPidfRat;
        int needPidfSipMsg;
        boolean needVolteRetryInNr;
        long netId;
        String oipFromPreferred;
        String password;
        String pcscfIp;
        int pcscfPort;
        String pdn;
        int phoneId;
        String preferredId;
        String privacyHeaderRestricted;
        int profileId;
        int qparam;
        int rat;
        int rcsProfile;
        String realm;
        int regExpires;
        int regRetryBaseTime;
        int regRetryMaxTime;
        String registeralgo;
        ImsUri.UriType remoteuritype;
        boolean retryInviteOnTcpReset;
        int ringbackTimer;
        int ringingTimer;
        int scmVersion;
        String selectTransportAfterTcpReset;
        int selfPort;
        boolean send18xReliably;
        boolean sendByeForUssi;
        Set<String> serviceList;
        int sessionexpires;
        String sessionrefresher;
        boolean singleRegiEnabled;
        int sipMobility;
        int srvccVersion;
        int sslType;
        int subscriberTimer;
        boolean support183ForIr92v9Precondition;
        boolean support199ProvisionalResponse;
        boolean supportAccessType;
        int supportB2cCallcomposerWithoutFeaturetag;
        boolean supportDualRcs;
        boolean supportDualSimCmc;
        boolean supportEct;
        boolean supportImsNotAvailable;
        boolean supportLtePreferred;
        boolean supportNetworkInitUssi;
        boolean supportReplaceMerge;
        boolean supportRfc6337ForDelayedOffer;
        boolean supportScribeDialogEvent;
        boolean supportUac;
        boolean supportUpgradePrecondition;
        int supportedGeolocationPhase;
        int tcpRstUacErrorcode;
        int tcpRstUasErrorcode;
        int textMode;
        int timer1;
        int timer2;
        int timer4;
        int timerA;
        int timerB;
        int timerC;
        int timerD;
        int timerE;
        int timerF;
        int timerG;
        int timerH;
        int timerI;
        int timerJ;
        int timerK;
        int timerTS;
        String transtype;
        boolean tryReregisterFromKeepalive;
        List<String> uacSipList;
        boolean use183OnProgressIncoming;
        boolean use200offerWhenRemoteNotSupport100rel;
        boolean useKeepAlive;
        boolean usePemHeader;
        boolean useProvisionalResponse100rel;
        boolean useQ850causeOn480;
        boolean useSubcontactWhenResub;
        String userAgent;
        String uuid;
        int videoCrbtSupportType;
        int vowifi5gsaMode;

        public static Builder newBuilder() {
            return new Builder();
        }

        public UaProfile build() {
            return new UaProfile(this);
        }

        public Builder setProfileId(int i) {
            this.profileId = i;
            return this;
        }

        public Builder setIface(String str) {
            this.iface = str;
            return this;
        }

        public Builder setNetId(long j) {
            this.netId = j;
            return this;
        }

        public Builder setPdn(String str) {
            this.pdn = str;
            return this;
        }

        public Builder setImpi(String str) {
            this.impi = str;
            return this;
        }

        public Builder setImpu(String str) {
            this.impu = str;
            return this;
        }

        public Builder setImsiBasedImpu(String str) {
            this.imsiBasedImpu = str;
            return this;
        }

        public Builder setLinkedImpuList(List<String> list) {
            this.impuList = list;
            return this;
        }

        public Builder setPreferredId(String str) {
            this.preferredId = str;
            return this;
        }

        public Builder setRemoteUriType(ImsUri.UriType uriType) {
            this.remoteuritype = uriType;
            return this;
        }

        public Builder setDomain(String str) {
            this.domain = str;
            return this;
        }

        public Builder setRegiAlgorithm(String str) {
            this.registeralgo = str;
            return this;
        }

        public Builder setPassword(String str) {
            this.password = str;
            return this;
        }

        public Builder setOutboundSip(boolean z) {
            this.issipoutbound = z;
            return this;
        }

        public Builder setQparam(int i) {
            this.qparam = i;
            return this;
        }

        public Builder setControlDscp(int i) {
            this.controlDscp = i;
            return this;
        }

        public Builder setTransportType(String str) {
            this.transtype = str;
            return this;
        }

        public Builder setUseTls(boolean z) {
            this.isenabletlsforsip = z;
            return this;
        }

        public Builder setIsIpSec(boolean z) {
            this.isipsec = z;
            return this;
        }

        public Builder setWifiPreConditionEnabled(boolean z) {
            this.mIsWifiPreConditionEnabled = z;
            return this;
        }

        public Builder setIsServerHeaderEnabled(boolean z) {
            this.mIsServerHeaderEnabled = z;
            return this;
        }

        public Builder setUseCompactHeader(boolean z) {
            this.mUseCompactHeader = z;
            return this;
        }

        public Builder setAuthAlg(String str) {
            this.authalg = str;
            return this;
        }

        public Builder setEncrAlg(String str) {
            this.encralg = str;
            return this;
        }

        public Builder setHostname(String str) {
            this.hostname = str;
            return this;
        }

        public Builder setPcscfIp(String str) {
            this.pcscfIp = str;
            return this;
        }

        public Builder setPcscfPort(int i) {
            this.pcscfPort = i;
            return this;
        }

        public Builder setEmergencyProfile(boolean z) {
            this.isemergencysupport = z;
            return this;
        }

        public Builder setServiceList(Set<String> set) {
            this.serviceList = set;
            return this;
        }

        public Builder addService(String str) {
            if (this.serviceList == null) {
                this.serviceList = new HashSet();
            }
            this.serviceList.add(str);
            return this;
        }

        public Builder setOwnCapabilities(Capabilities capabilities2) {
            this.capabilities = capabilities2;
            return this;
        }

        public Builder setMno(Mno mno2) {
            this.mno = mno2;
            return this;
        }

        public Builder setMvnoName(String str) {
            this.mvno = str;
            return this;
        }

        public Builder setPrecondEnabled(boolean z) {
            this.isprecondenabled = z;
            return this;
        }

        public Builder setPrecondInitialSendrecv(boolean z) {
            this.ispreconinitialsendrecv = z;
            return this;
        }

        public Builder setIsRcsTelephonyFeatureTagRequired(boolean z) {
            this.isRcsTelephonyFeatureTagRequired = z;
            return this;
        }

        public Builder setIsFullCodecOfferRequired(boolean z) {
            this.isFullCodecOfferRequired = z;
            return this;
        }

        public Builder setSessionExpires(int i) {
            this.sessionexpires = i;
            return this;
        }

        public Builder setMinSe(int i) {
            this.minSe = i;
            return this;
        }

        public Builder setSessionRefresher(String str) {
            this.sessionrefresher = str;
            return this;
        }

        public Builder setRegExpires(int i) {
            this.regExpires = i;
            return this;
        }

        public Builder setUserAgent(String str) {
            this.userAgent = str;
            return this;
        }

        public Builder setDisplayName(String str) {
            this.displayName = str;
            return this;
        }

        public Builder setContactDisplayName(String str) {
            this.contactDisplayName = str;
            return this;
        }

        public Builder setUuid(String str) {
            this.uuid = str;
            return this;
        }

        public Builder setInstanceId(String str) {
            this.instanceId = str;
            return this;
        }

        public Builder setRealm(String str) {
            this.realm = str;
            return this;
        }

        public Builder setImMsgTech(String str) {
            this.imMsgTech = str;
            return this;
        }

        public Builder setCallProfile(CallProfile callProfile2) {
            this.callProfile = callProfile2;
            return this;
        }

        public Builder setTimer1(int i) {
            this.timer1 = i;
            return this;
        }

        public Builder setTimer2(int i) {
            this.timer2 = i;
            return this;
        }

        public Builder setTimer4(int i) {
            this.timer4 = i;
            return this;
        }

        public Builder setTimerA(int i) {
            this.timerA = i;
            return this;
        }

        public Builder setTimerB(int i) {
            this.timerB = i;
            return this;
        }

        public Builder setTimerC(int i) {
            this.timerC = i;
            return this;
        }

        public Builder setTimerD(int i) {
            this.timerD = i;
            return this;
        }

        public Builder setTimerE(int i) {
            this.timerE = i;
            return this;
        }

        public Builder setTimerF(int i) {
            this.timerF = i;
            return this;
        }

        public Builder setTimerG(int i) {
            this.timerG = i;
            return this;
        }

        public Builder setTimerH(int i) {
            this.timerH = i;
            return this;
        }

        public Builder setTimerI(int i) {
            this.timerI = i;
            return this;
        }

        public Builder setTimerJ(int i) {
            this.timerJ = i;
            return this;
        }

        public Builder setTimerK(int i) {
            this.timerK = i;
            return this;
        }

        public Builder setTimerTS(int i) {
            this.timerTS = i;
            return this;
        }

        public Builder setIsSoftphoneEnabled(boolean z) {
            this.isSoftphoneEnabled = z;
            return this;
        }

        public Builder setIsCdmalessEnabled(boolean z) {
            this.isCdmalessEnabled = z;
            return this;
        }

        public Builder setMssSize(int i) {
            this.mssSize = i;
            return this;
        }

        public Builder setRingbackTimer(int i) {
            this.ringbackTimer = i;
            return this;
        }

        public Builder setRingingTimer(int i) {
            this.ringingTimer = i;
            return this;
        }

        public Builder setSipMobility(int i) {
            this.sipMobility = i;
            return this;
        }

        public Builder setIsEnableGruu(boolean z) {
            this.isEnableGruu = z;
            return this;
        }

        public Builder setIsEnableVcid(boolean z) {
            this.isEnableVcid = z;
            return this;
        }

        public Builder setIsEnableSessionId(boolean z) {
            this.isEnableSessionId = z;
            return this;
        }

        public Builder setAudioEngineType(int i) {
            this.audioEngineType = i;
            return this;
        }

        public Builder setCurPani(String str) {
            this.curPani = str;
            return this;
        }

        public Builder setVceConfigEnabled(boolean z) {
            this.isVceConfigEnabled = z;
            return this;
        }

        public Builder setGcfConfigEnabled(boolean z) {
            this.isGcfConfigEnabled = z;
            return this;
        }

        public Builder setNsdsServiceEnabled(boolean z) {
            this.isNsdsServiceEnabled = z;
            return this;
        }

        public Builder setMsrpBearerUsed(boolean z) {
            this.isMsrpBearerUsed = z;
            return this;
        }

        public Builder setSubscriberTimer(int i) {
            this.subscriberTimer = i;
            return this;
        }

        public Builder setSubscribeReg(boolean z) {
            this.isSubscribeReg = z;
            return this;
        }

        public Builder setAccessToken(String str) {
            this.accessToken = str;
            return this;
        }

        public Builder setAuthServerUrl(String str) {
            this.authServerUrl = str;
            return this;
        }

        public Builder setUseKeepAlive(boolean z) {
            this.useKeepAlive = z;
            return this;
        }

        public Builder setSelfPort(int i) {
            this.selfPort = i;
            return this;
        }

        public Builder setScmVersion(int i) {
            this.scmVersion = i;
            return this;
        }

        public Builder setActiveDataPhoneId(int i) {
            this.activeDataPhoneId = i;
            return this;
        }

        public Builder setMsrpTransType(String str) {
            this.msrpTransType = str;
            return this;
        }

        public Builder setIsXqEnabled(boolean z) {
            this.isXqEnabled = z;
            return this;
        }

        public Builder setTextMode(int i) {
            this.textMode = i;
            return this;
        }

        public Builder setRcsProfile(int i) {
            this.rcsProfile = i;
            return this;
        }

        public Builder setIsTransportNeeded(boolean z) {
            this.isTransportNeeded = z;
            return this;
        }

        public Builder setRat(int i) {
            this.rat = i;
            return this;
        }

        public Builder setDbrTimer(int i) {
            this.dbrTimer = i;
            return this;
        }

        public Builder setIsTcpGracefulShutdownEnabled(boolean z) {
            this.isTcpGracefulShutdownEnabled = z;
            return this;
        }

        public Builder setTcpRstUacErrorcode(int i) {
            this.tcpRstUacErrorcode = i;
            return this;
        }

        public Builder setTcpRstUasErrorcode(int i) {
            this.tcpRstUasErrorcode = i;
            return this;
        }

        public Builder setPrivacyHeaderRestricted(String str) {
            this.privacyHeaderRestricted = str;
            return this;
        }

        public Builder setUsePemHeader(boolean z) {
            this.usePemHeader = z;
            return this;
        }

        public Builder setPhoneId(int i) {
            this.phoneId = i;
            return this;
        }

        public Builder setSupportEct(boolean z) {
            this.supportEct = z;
            return this;
        }

        public Builder setEarlyMediaRtpTimeoutTimer(int i) {
            this.earlyMediaRtpTimeoutTimer = i;
            return this;
        }

        public Builder setAddHistinfo(boolean z) {
            this.addHistinfo = z;
            return this;
        }

        public Builder setSupportedGeolocationPhase(int i) {
            this.supportedGeolocationPhase = i;
            return this;
        }

        public Builder setNeedPidfSipMsg(int i) {
            this.needPidfSipMsg = i;
            return this;
        }

        public Builder setNeedPidfRat(int i) {
            this.needPidfRat = i;
            return this;
        }

        public Builder setUseSubcontactWhenResub(boolean z) {
            this.useSubcontactWhenResub = z;
            return this;
        }

        public Builder setUseProvisionalResponse100rel(boolean z) {
            this.useProvisionalResponse100rel = z;
            return this;
        }

        public Builder setUse183OnProgressIncoming(boolean z) {
            this.use183OnProgressIncoming = z;
            return this;
        }

        public Builder setUseQ850causeOn480(boolean z) {
            this.useQ850causeOn480 = z;
            return this;
        }

        public Builder setSupport183ForIr92v9Precondition(boolean z) {
            this.support183ForIr92v9Precondition = z;
            return this;
        }

        public Builder setSupportImsNotAvailable(boolean z) {
            this.supportImsNotAvailable = z;
            return this;
        }

        public Builder setSupportLtePreferred(boolean z) {
            this.supportLtePreferred = z;
            return this;
        }

        public Builder setSupportUpgradePrecondition(boolean z) {
            this.supportUpgradePrecondition = z;
            return this;
        }

        public Builder setSupportReplaceMerge(boolean z) {
            this.supportReplaceMerge = z;
            return this;
        }

        public Builder setSupportAccessType(boolean z) {
            this.supportAccessType = z;
            return this;
        }

        public Builder setLastPaniHeader(String str) {
            this.lastPaniHeader = str;
            return this;
        }

        public Builder setOipFromPreferred(String str) {
            this.oipFromPreferred = str;
            return this;
        }

        public Builder setSelectTransportAfterTcpReset(String str) {
            this.selectTransportAfterTcpReset = str;
            return this;
        }

        public Builder setSrvccVersion(int i) {
            this.srvccVersion = i;
            return this;
        }

        public Builder setSubscribeDialogEvent(Boolean bool) {
            this.supportScribeDialogEvent = bool.booleanValue();
            return this;
        }

        public Builder setIsSimMobility(Boolean bool) {
            this.isSimMobility = bool.booleanValue();
            return this;
        }

        public Builder setCmcType(int i) {
            this.cmcType = i;
            return this;
        }

        public Builder setCmcRelayType(String str) {
            this.cmcRelayType = str;
            return this;
        }

        public Builder setCmcEmergencyNumbers(String str) {
            this.cmcEmergencyNumbers = str;
            return this;
        }

        public Builder setSupportDualSimCmc(boolean z) {
            this.supportDualSimCmc = z;
            return this;
        }

        public Builder setVideoCrbtSupportType(int i) {
            this.videoCrbtSupportType = i;
            return this;
        }

        public Builder setRetryInviteOnTcpReset(boolean z) {
            this.retryInviteOnTcpReset = z;
            return this;
        }

        public Builder setEanbleVerstat(boolean z) {
            this.enableVerstat = z;
            return this;
        }

        public Builder setRegRetryBaseTime(int i) {
            this.regRetryBaseTime = i;
            return this;
        }

        public Builder setRegRetryMaxTime(int i) {
            this.regRetryMaxTime = i;
            return this;
        }

        public Builder setSupportDualRcs(boolean z) {
            this.supportDualRcs = z;
            return this;
        }

        public Builder setTryReregisterFromKeepalive(boolean z) {
            this.tryReregisterFromKeepalive = z;
            return this;
        }

        public Builder setPttSupported(boolean z) {
            this.isPttSupported = z;
            return this;
        }

        public Builder setSslType(int i) {
            this.sslType = i;
            return this;
        }

        public Builder setSupport199ProvisionalResponse(boolean z) {
            this.support199ProvisionalResponse = z;
            return this;
        }

        public Builder setSend18xReliably(boolean z) {
            this.send18xReliably = z;
            return this;
        }

        public Builder setAcb(List<String> list) {
            this.acb = list;
            return this;
        }

        public Builder setIgnoreDisplayName(boolean z) {
            this.ignoreDisplayName = z;
            return this;
        }

        public Builder setSupportNetworkInitUssi(boolean z) {
            this.supportNetworkInitUssi = z;
            return this;
        }

        public Builder setSendByeForUssi(boolean z) {
            this.sendByeForUssi = z;
            return this;
        }

        public Builder setSupportRfc6337ForDelayedOffer(boolean z) {
            this.supportRfc6337ForDelayedOffer = z;
            return this;
        }

        public Builder setUse200offerWhenRemoteNotSupport100rel(boolean z) {
            this.use200offerWhenRemoteNotSupport100rel = z;
            return this;
        }

        public Builder setExcludePaniVowifiInitialRegi(boolean z) {
            this.excludePaniVowifiInitialRegi = z;
            return this;
        }

        public Builder setHashAlgoType(int i) {
            this.hashAlgoType = i;
            return this;
        }

        public Builder setVowifi5gsaMode(int i) {
            this.vowifi5gsaMode = i;
            return this;
        }

        public Builder setSingleRegiEnabled(boolean z) {
            this.singleRegiEnabled = z;
            return this;
        }

        public Builder setNeedCheckAllowedMethodForRefresh(boolean z) {
            this.needCheckAllowedMethodForRefresh = z;
            return this;
        }

        public Builder setAddMmtelCallComposerTag(boolean z) {
            this.addMmtelCallComposerTag = z;
            return this;
        }

        public Builder setKeepAliveFactor(int i) {
            this.keepAliveFactor = i;
            return this;
        }

        public Builder setEncrNullRoaming(boolean z) {
            this.encrNullRoaming = z;
            return this;
        }

        public Builder setSupportUac(boolean z) {
            this.supportUac = z;
            return this;
        }

        public Builder setUacSipList(List<String> list) {
            this.uacSipList = list;
            return this;
        }

        public Builder setNeedVolteRetryInNr(boolean z) {
            this.needVolteRetryInNr = z;
            return this;
        }

        public Builder setImpuPreference(int i) {
            this.impuPreference = i;
            return this;
        }

        public Builder setIsUpdateSaOnStartSupported(boolean z) {
            this.isUpdateSaOnStartSupported = z;
            return this;
        }

        public Builder setSupportB2cCallcomposerWithoutFeaturetag(int i) {
            this.supportB2cCallcomposerWithoutFeaturetag = i;
            return this;
        }
    }
}
