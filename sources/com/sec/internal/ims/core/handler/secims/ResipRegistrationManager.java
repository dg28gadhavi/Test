package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.RcsConfig;
import com.sec.internal.constants.ims.core.PaniConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.RegisterTask;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.handler.RegistrationHandler;
import com.sec.internal.ims.core.handler.secims.CallProfile;
import com.sec.internal.ims.core.handler.secims.UaProfile;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.Cert;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.X509CertVerifyRequest;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.IUserAgent;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IMSLogTimer;
import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ResipRegistrationManager extends RegistrationHandler {
    private static final String LOG_TAG = "ResipRegiMgr";
    private static final boolean SHIP_BUILD = IMSLog.isShipBuild();
    protected Context mContext;
    protected SimpleEventLog mEventLog;
    protected IImsFramework mImsFramework;
    protected PaniGenerator mPaniGenerator;
    protected IPdnController mPdnController;
    protected IRegistrationHandlerNotifiable mRegistrationHandler;
    protected List<ISimManager> mSimManagers;
    protected ITelephonyManager mTelephonyManager;
    protected Handler mUaHandler = null;
    protected HandlerThread mUaHandlerThread = null;
    protected final Map<Integer, UserAgent> mUaList = new ConcurrentHashMap();

    public ResipRegistrationManager(Looper looper, Context context, IImsFramework iImsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = iImsFramework;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
    }

    public void setEventLog(SimpleEventLog simpleEventLog) {
        this.mEventLog = simpleEventLog;
    }

    public void setRegistrationHandler(IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable) {
        this.mRegistrationHandler = iRegistrationHandlerNotifiable;
    }

    public void setSimManagers(List<ISimManager> list) {
        this.mSimManagers = list;
    }

    public void setPdnController(IPdnController iPdnController) {
        this.mPdnController = iPdnController;
    }

    public void init() {
        HandlerThread handlerThread = new HandlerThread("UaHandler");
        this.mUaHandlerThread = handlerThread;
        handlerThread.start();
        this.mUaHandler = new Handler(this.mUaHandlerThread.getLooper());
        this.mPaniGenerator = new PaniGenerator(this.mContext, this.mPdnController);
        StackIF.getInstance().registerUaListener(0, new StackEventListener() {
            public void onX509CertVerifyRequested(X509CertVerifyRequest x509CertVerifyRequest) {
                Log.i(ResipRegistrationManager.LOG_TAG, "onX509CertVerifyRequested");
                ArrayList arrayList = new ArrayList();
                try {
                    CertificateFactory instance = CertificateFactory.getInstance("X.509");
                    for (int i = 0; i < x509CertVerifyRequest.certLength(); i++) {
                        Cert cert = x509CertVerifyRequest.cert(i);
                        int certDataLength = cert != null ? cert.certDataLength() : 0;
                        byte[] bArr = new byte[certDataLength];
                        for (int i2 = 0; i2 < certDataLength; i2++) {
                            bArr[i2] = (byte) cert.certData(i2);
                        }
                        X509Certificate x509Certificate = (X509Certificate) instance.generateCertificate(new ByteArrayInputStream(bArr));
                        arrayList.add(x509Certificate);
                        IMSLog.s(ResipRegistrationManager.LOG_TAG, "Subject: " + x509Certificate.getSubjectDN().toString() + ", Issuer: " + x509Certificate.getIssuerDN().toString());
                    }
                } catch (CertificateException e) {
                    Log.i(ResipRegistrationManager.LOG_TAG, "something wrong with certificate", e);
                }
                ResipRegistrationManager.this.mRegistrationHandler.notifyX509CertVerificationRequested((X509Certificate[]) arrayList.toArray(new X509Certificate[0]));
            }

            public void onDnsResponse(String str, List<String> list, int i, int i2) {
                IMSLog.s(ResipRegistrationManager.LOG_TAG, "onDnsResponse: hostname " + str + " ipAddrList " + list + " port " + i + ", handle " + i2);
                ResipRegistrationManager.this.mRegistrationHandler.notifyDnsResponse(list, i, i2);
            }
        });
    }

    public boolean registerInternal(IRegisterTask iRegisterTask, String str, String str2, Set<String> set, Capabilities capabilities, String str3, String str4, String str5, String str6, String str7, List<String> list, Bundle bundle, Bundle bundle2, boolean z) {
        IRegisterTask iRegisterTask2 = iRegisterTask;
        String str8 = str2;
        Set<String> set2 = set;
        Capabilities capabilities2 = capabilities;
        int phoneId = iRegisterTask.getPhoneId();
        StringBuilder sb = new StringBuilder();
        sb.append("registerInternal: task=");
        sb.append(iRegisterTask2);
        sb.append(" pcscf=");
        sb.append(!SHIP_BUILD ? str8 : "");
        sb.append(" services=");
        sb.append(set2);
        sb.append(" reason=");
        sb.append(iRegisterTask.getReason());
        IMSLog.i(LOG_TAG, phoneId, sb.toString());
        ImsProfile profile = iRegisterTask.getProfile();
        configureRCS(iRegisterTask);
        int registrationInfoId = IRegistrationManager.getRegistrationInfoId(profile.getId(), phoneId);
        UserAgent userAgent = this.mUaList.get(Integer.valueOf(registrationInfoId));
        if (userAgent == null) {
            IMSLog.i(LOG_TAG, phoneId, "register: creating UserAgent.");
            IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable = this.mRegistrationHandler;
            String str9 = LOG_TAG;
            int i = phoneId;
            UserAgent createUserAgent = createUserAgent(iRegisterTask, str, str2, set, capabilities, str3, str4, str5, str6, str7, bundle, bundle2, iRegistrationHandlerNotifiable);
            if (createUserAgent != null) {
                this.mUaList.put(Integer.valueOf(registrationInfoId), createUserAgent);
                iRegisterTask2.setUserAgent(createUserAgent);
                iRegisterTask2.setState(RegistrationConstants.RegisterTaskState.REGISTERING);
                iRegisterTask2.setRegiRequestType(DiagnosisConstants.REGI_REQC.INITIAL);
                if (profile.getPdnType() == 11) {
                    IMSLogTimer.setVolteRegisterStartTime(i);
                    IMSLog.lazer(iRegisterTask2, "SEND SIP REGISTER <+" + (((double) (IMSLogTimer.getVolteRegisterStartTime(i) - IMSLogTimer.getPdnEndTime(i))) / 1000.0d) + "s>");
                } else {
                    IMSLog.lazer(iRegisterTask2, "SEND SIP REGISTER");
                }
                if (iRegisterTask.getProfile().hasEmergencySupport()) {
                    return true;
                }
                this.mRegistrationHandler.notifyTriggeringRecoveryAction(iRegisterTask2, ((long) iRegisterTask.getProfile().getTimerF()) * 2);
                return true;
            }
            IMSLog.e(str9, i, "register: fail creating UserAgent.");
            return false;
        }
        String str10 = LOG_TAG;
        int i2 = phoneId;
        UaProfile uaProfile = userAgent.getUaProfile();
        if (str8 != null) {
            uaProfile.setPcscfIp(str8);
        }
        Log.i(str10, "register: Re-Register with new services=" + set2);
        iRegisterTask2.setRegiRequestType(DiagnosisConstants.REGI_REQC.RE_REGI);
        if (profile.hasEmergencySupport()) {
            if (set2.contains("mmtel-video")) {
                capabilities2.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL_VIDEO));
                capabilities2.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
                this.mEventLog.logAndAdd(i2, iRegisterTask2, "createUserAgent: add mmtel, mmtel-video to Capabilities for E-REGI");
            } else {
                capabilities2.addFeature(Capabilities.getTagFeature(Capabilities.FEATURE_TAG_MMTEL));
                this.mEventLog.logAndAdd(i2, iRegisterTask2, "createUserAgent : add mmtel to Capabilities for E-REGI");
            }
        }
        uaProfile.setOwnCapabilities(capabilities2);
        uaProfile.setServiceList(new HashSet(set2));
        uaProfile.setLinkedImpuList(profile.getExtImpuList());
        uaProfile.setImpu(str4);
        uaProfile.setSingleRegiEnabled(ImsUtil.isSingleRegiAppConnected(i2));
        if (iRegisterTask.getMno() == Mno.TMOUS) {
            uaProfile.setAddMmtelCallComposerTag(ImsUtil.isAddMmtelCallComposerTag(i2, this.mContext));
        }
        uaProfile.setImMsgTech(((ImConstants.ImMsgTech) Optional.ofNullable(this.mImsFramework.getServiceModuleManager().getImModule() != null ? this.mImsFramework.getServiceModuleManager().getImModule().getImConfig(i2) : null).map(new ResipRegistrationManager$$ExternalSyntheticLambda0()).orElse(ImConstants.ImMsgTech.SIMPLE_IM)).toString());
        if (!profile.hasEmergencySupport()) {
            userAgent.setThirdPartyFeatureTags(list);
        }
        this.mImsFramework.getServiceModuleManager().notifyReRegistering(i2, set2);
        userAgent.register();
        return true;
    }

    public void deregisterInternal(IRegisterTask iRegisterTask, boolean z) {
        int phoneId = iRegisterTask.getPhoneId();
        UserAgent userAgent = (UserAgent) iRegisterTask.getUserAgent();
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, iRegisterTask, "deregisterInternal : " + iRegisterTask.getReason() + "(" + iRegisterTask.getDeregiReason() + ")");
        IMSLog.c(LogClass.REGI_DEREGISTER_INTERNAL, iRegisterTask.getPhoneId() + ",DEREGI:" + iRegisterTask.getReason() + ":" + iRegisterTask.getDeregiReason());
        ImsRegistration imsRegistration = iRegisterTask.getImsRegistration();
        boolean hasService = imsRegistration != null ? imsRegistration.hasService(SipMsg.EVENT_PRESENCE) : false;
        if (userAgent == null) {
            IMSLog.e(LOG_TAG, phoneId, "deregisterInternal: UserAgent is null, can't deregister");
            return;
        }
        userAgent.deregister(z, hasService);
        if ((iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY && !iRegisterTask.needKeepEmergencyTask()) || (iRegisterTask.getMno() != Mno.KDDI && iRegisterTask.getProfile().hasEmergencySupport() && iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING)) {
            removeUserAgent(iRegisterTask);
        }
    }

    private boolean needRemoveEmergencyUserAgent(IRegisterTask iRegisterTask, SipError sipError) {
        if (iRegisterTask.getMno() == Mno.VZW && SipErrorBase.SIP_TIMEOUT.equals(sipError) && iRegisterTask.getGovernor().getFailureCount() < 2) {
            return true;
        }
        if (iRegisterTask.getProfile().getE911RegiTime() <= 0 || !SipErrorBase.SIP_TIMEOUT.equals(sipError) || iRegisterTask.getGovernor().getFailureCount() >= iRegisterTask.getGovernor().getNumOfEmerPcscfIp()) {
            return false;
        }
        IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "pending Emergency Register msg, Remove Emergency UserAgent");
        return true;
    }

    public void onRegisterError(IRegisterTask iRegisterTask, int i, SipError sipError, long j) {
        if (!iRegisterTask.getProfile().hasEmergencySupport() || iRegisterTask.getMno() == Mno.KDDI) {
            if (iRegisterTask.getMno() == Mno.TMOUS && SipErrorBase.MISSING_P_ASSOCIATED_URI.equals(sipError)) {
                return;
            }
            if (!iRegisterTask.isRefreshReg() || (iRegisterTask.getMno() != Mno.KDDI && !iRegisterTask.getGovernor().needImsNotAvailable())) {
                UserAgent userAgent = (UserAgent) iRegisterTask.getUserAgent();
                if (userAgent != null && userAgent.getHandle() == i) {
                    removeUserAgent(iRegisterTask);
                } else if (getUserAgent(i) != null) {
                    int phoneId = iRegisterTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId, "remove user agent not in the IRegisterTask: " + i);
                    UserAgent userAgent2 = getUserAgent(i);
                    userAgent2.terminate();
                    userAgent2.unRegisterListener();
                }
            } else {
                IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "Dont Remove the user Agent for Refresh Reg ,Re-register to be triggered.");
            }
        } else if (needRemoveEmergencyUserAgent(iRegisterTask, sipError)) {
            removeUserAgent(iRegisterTask);
        }
    }

    public void onDeregistered(IRegisterTask iRegisterTask, SipError sipError, long j, boolean z) {
        removeUserAgent(iRegisterTask);
    }

    public boolean suspended(IRegisterTask iRegisterTask, boolean z) {
        if (iRegisterTask.getUserAgent() == null) {
            return false;
        }
        UserAgent userAgent = (UserAgent) iRegisterTask.getUserAgent();
        if (userAgent.getSuspendState() == z) {
            return false;
        }
        NetworkEvent networkEvent = SlotBasedConfig.getInstance(iRegisterTask.getPhoneId()).getNetworkEvent();
        SimpleEventLog simpleEventLog = this.mEventLog;
        int phoneId = iRegisterTask.getPhoneId();
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "Suspend : " : "Resume : ");
        sb.append(iRegisterTask.getProfile().getName());
        sb.append(" ");
        sb.append(networkEvent);
        simpleEventLog.logAndAdd(phoneId, sb.toString());
        userAgent.suspended(z);
        if (z) {
            this.mRegistrationHandler.removeRecoveryAction();
            return true;
        } else if (iRegisterTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERING) {
            return true;
        } else {
            this.mRegistrationHandler.notifyTriggeringRecoveryAction(iRegisterTask, ((long) iRegisterTask.getProfile().getTimerF()) * 2);
            return true;
        }
    }

    private UaProfile.Builder configureTimerTS(ImsProfile imsProfile, UaProfile.Builder builder) {
        int i;
        Mno fromName = Mno.fromName(imsProfile.getMnoName());
        if (fromName == Mno.SPRINT) {
            i = 1000;
        } else {
            i = (fromName == Mno.KDDI || fromName == Mno.RAKUTEN_JAPAN) ? 200000 : 32000;
        }
        Log.i(LOG_TAG, "timerTS=%" + i);
        builder.setTimerTS(i);
        return builder;
    }

    private CallProfile configureMedia(ImsProfile imsProfile) {
        String str;
        Log.i(LOG_TAG, "configureMedia:");
        boolean z = false;
        try {
            str = (String) Class.forName("com.sec.internal.ims.core.handler.secims.ResipMediaHandler").getMethod("getHwSupportedVideoCodecs", new Class[]{String.class}).invoke(this.mImsFramework.getHandlerFactory().getMediaHandler(), new Object[]{imsProfile.getVideoCodec()});
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            str = "";
        }
        CallProfile.Builder dtmfMode = CallProfile.Builder.newBuilder().setAudioCodec(imsProfile.getAudioCodec()).setAudioPort(imsProfile.getAudioPortStart()).setAudioDscp(imsProfile.getAudioDscp()).setAmrPayloadType(imsProfile.getAmrnbbePayload()).setAmrOaPayloadType(imsProfile.getAmrnboaPayload()).setAmrWbPayloadType(imsProfile.getAmrwbbePayload()).setAmrWbOaPayloadType(imsProfile.getAmrwboaPayload()).setAmrOpenPayloadType(imsProfile.getAmropenPayload()).setDtmfPayloadType(imsProfile.getDtmfNbPayload()).setDtmfWbPayloadType(imsProfile.getDtmfWbPayload()).setAmrOaMaxRed(imsProfile.getAmrnboaMaxRed()).setAmrBeMaxRed(imsProfile.getAmrnbbeMaxRed()).setAmrOaWbMaxRed(imsProfile.getAmrwboaMaxRed()).setAmrBeWbMaxRed(imsProfile.getAmrwbbeMaxRed()).setEvsMaxRed(imsProfile.getEvsMaxRed()).setAmrMode(imsProfile.getAmrnbMode()).setAmrWbMode(imsProfile.getAmrwbMode()).setAudioAs(imsProfile.getAudioAS()).setAudioRs(imsProfile.getAudioRS()).setAudioRr(imsProfile.getAudioRR()).setPTime(imsProfile.getPTime()).setMaxPTime(imsProfile.getMaxPTime()).setVideoCodec(str).setVideoPort(imsProfile.getVideoPortStart()).setFrameRate(imsProfile.getFramerate()).setDisplayFormat(imsProfile.getDisplayFormat()).setDisplayFormatHevc(imsProfile.getDisplayFormatHevc()).setPacketizationMode(imsProfile.getPacketizationMode()).setH265QvgaPayloadType(imsProfile.getH265QvgaPayload()).setH265QvgaLPayloadType(imsProfile.getH265QvgalPayload()).setH265VgaPayloadType(imsProfile.getH265VgaPayload()).setH265VgaLPayloadType(imsProfile.getH265VgalPayload()).setH265Hd720pPayloadType(imsProfile.getH265Hd720pPayload()).setH265Hd720pLPayloadType(imsProfile.getH265Hd720plPayload()).setH264720pPayloadType(imsProfile.getH264720pPayload()).setH264720pLPayloadType(imsProfile.getH264720plPayload()).setH264VgaPayloadType(imsProfile.getH264VgaPayload()).setH264VgaLPayloadType(imsProfile.getH264VgalPayload()).setH264QvgaPayloadType(imsProfile.getH264QvgaPayload()).setH264QvgaLPayloadType(imsProfile.getH264QvgalPayload()).setH264CifPayloadType(imsProfile.getH264CifPayload()).setH264CifLPayloadType(imsProfile.getH264CiflPayload()).setH263QcifPayloadType(imsProfile.getH263QcifPayload()).setUseSpsForH264Hd(imsProfile.getUseSpsForH264Hd()).setVideoAs(imsProfile.getVideoAS()).setVideoRs(imsProfile.getVideoRS()).setVideoRr(imsProfile.getVideoRR()).setTextAs(imsProfile.getTextAS()).setTextRs(imsProfile.getTextRS()).setTextRr(imsProfile.getTextRR()).setTextPort(imsProfile.getTextPort()).setAudioAvpf(imsProfile.getAudioAvpf() == 1).setAudioSrtp(imsProfile.getAudioSrtp() == 1).setVideoAvpf(imsProfile.getVideoAvpf() == 1).setVideoSrtp(imsProfile.getVideoSrtp() == 1).setTextAvpf(imsProfile.getTextAvpf() == 1).setTextSrtp(imsProfile.getTextSrtp() == 1).setVideoCapabilities(imsProfile.isSupportVideoCapabilities()).setRtpTimeout(imsProfile.getRTPTimeout()).setRtcpTimeout(imsProfile.getRTCPTimeout()).setIgnoreRtcpTimeoutOnHoldCall(imsProfile.getIgnoreRtcpTimeoutOnHoldCall()).setEnableRtcpOnActiveCall(imsProfile.getEnableRtcpOnActiveCall()).setEnableAvSync(imsProfile.getEnableAvSync()).setEnableScr(imsProfile.getEnableScr()).setAudioRtcpXr(imsProfile.getAudioRtcpXr() == 1).setVideoRtcpXr(imsProfile.getVideoRtcpXr() == 1).setDtmfMode(imsProfile.getDtmfMode());
        if (imsProfile.getEnableEvsCodec() == 1) {
            z = true;
        }
        return dtmfMode.setEnableEvsCodec(z).setEvsDiscontinuousTransmission(imsProfile.getEvsDiscontinuousTransmission()).setEvsDtxRecv(imsProfile.getEvsDtxRecv()).setEvsHeaderFull(imsProfile.getEvsHeaderFull()).setEvsModeSwitch(imsProfile.getEvsModeSwitch()).setEvsChannelSend(imsProfile.getEvsChannelSend()).setEvsChannelRecv(imsProfile.getEvsChannelRecv()).setEvsChannelAwareReceive(imsProfile.getEvsChannelAwareReceive()).setEvsCodecModeRequest(imsProfile.getEvsCodecModeRequest()).setEvsBitRateSend(imsProfile.getEvsBitRateSend()).setEvsBitRateReceive(imsProfile.getEvsBitRateReceive()).setEvsBandwidthSend(imsProfile.getEvsBandwidthSend()).setEvsBandwidthReceive(imsProfile.getEvsBandwidthReceive()).setEvsPayload(imsProfile.getEvsPayload()).setEvs2ndPayload(imsProfile.getEvs2ndPayload()).setEvsDefaultBandwidth(imsProfile.getEvsDefaultBandwidth()).setEvsDefaultBitrate(imsProfile.getEvsDefaultBitrate()).setEvsPayloadExt(imsProfile.getEvsPayloadExt()).setEvsBitRateSendExt(imsProfile.getEvsBitRateSendExt()).setEvsBitRateReceiveExt(imsProfile.getEvsBitRateReceiveExt()).setEvsBandwidthSendExt(imsProfile.getEvsBandwidthSendExt()).setEvsBandwidthReceiveExt(imsProfile.getEvsBandwidthReceiveExt()).setEvsLimitedCodec(imsProfile.getEvsLimitedCodec()).setEvsUseDefaultRtcpBw(imsProfile.getEvsUseDefaultRtcpBw()).build();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:67:0x03eb, code lost:
        if (com.sec.internal.ims.util.ImsUtil.needForceToUsePsE911(r3, r6.hasNoSim()) != false) goto L_0x03f0;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0534  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0593  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0595  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x093a  */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x09b1  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0a09  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0a2c  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x0a88  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0a99  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x0526  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sec.internal.ims.core.handler.secims.UserAgent createUserAgent(com.sec.internal.interfaces.ims.core.IRegisterTask r64, java.lang.String r65, java.lang.String r66, java.util.Set<java.lang.String> r67, com.sec.ims.options.Capabilities r68, java.lang.String r69, java.lang.String r70, java.lang.String r71, java.lang.String r72, java.lang.String r73, android.os.Bundle r74, android.os.Bundle r75, com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable r76) {
        /*
            r63 = this;
            r0 = r63
            r1 = r64
            r2 = r65
            r3 = r66
            r4 = r67
            r5 = r68
            r6 = r69
            r7 = r70
            r8 = r71
            r9 = r73
            r10 = r74
            r11 = r75
            com.sec.ims.settings.ImsProfile r12 = r64.getProfile()
            java.lang.String r13 = r64.getPcscfHostname()
            int r14 = r64.getPdnType()
            android.net.Network r15 = r64.getNetworkConnected()
            int r1 = r64.getRegistrationRat()
            int r3 = r64.getPhoneId()
            r16 = 0
            r17 = r13
            java.lang.String r13 = "ResipRegiMgr"
            if (r2 != 0) goto L_0x003e
            java.lang.String r0 = "createUserAgent: ifacename is null"
            com.sec.internal.log.IMSLog.e(r13, r3, r0)
            return r16
        L_0x003e:
            java.lang.String r18 = r12.getTransportName()
            boolean r11 = r12.hasEmergencySupport()
            int r19 = r12.getUsePrecondition()
            r20 = r1
            if (r19 == 0) goto L_0x0051
            r22 = 1
            goto L_0x0053
        L_0x0051:
            r22 = 0
        L_0x0053:
            boolean r1 = r12.getPrecondtionInitialSendrecv()
            r23 = r1
            int r1 = r12.getSessionExpire()
            r24 = r1
            int r1 = r12.getMinSe()
            r25 = r1
            java.lang.String r1 = r12.getSessionRefresher()
            r26 = r1
            int r1 = r12.getRegExpire()
            r27 = r1
            int r1 = r12.getMssSize()
            r28 = r1
            java.lang.String r1 = r0.getPdnName(r14)
            boolean r7 = com.sec.internal.helper.os.DeviceUtil.getGcfMode()
            r29 = r7
            int r7 = r12.getSipMobility()
            r30 = r7
            boolean r7 = r12.isEnableGruu()
            r31 = r7
            boolean r7 = r12.isEnableVcid()
            r32 = r7
            boolean r7 = r12.isEnableSessionId()
            r33 = r7
            int r7 = r63.getAudioEngineType()
            int r34 = r12.getSubscribeForReg()
            if (r34 == 0) goto L_0x00a8
            r34 = r7
            r35 = 1
            goto L_0x00ac
        L_0x00a8:
            r34 = r7
            r35 = 0
        L_0x00ac:
            int r7 = r12.getTtyType()
            r36 = r7
            java.lang.String r7 = "support_upgrade_precondition"
            java.lang.Boolean r7 = r12.getAsBoolean(r7)
            boolean r7 = r7.booleanValue()
            boolean r37 = r12.getSimMobility()
            boolean r6 = r12.getEncrNullRoaming()
            r38 = r6
            java.util.List r6 = r12.getAcb()
            r39 = r6
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r8 = "isSupportUpgradePrecondition "
            r6.append(r8)
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r6)
            if (r11 == 0) goto L_0x0129
            java.lang.String r6 = "mmtel-video"
            boolean r6 = r4.contains(r6)
            if (r6 == 0) goto L_0x0106
            java.lang.String r6 = com.sec.ims.options.Capabilities.FEATURE_TAG_MMTEL_VIDEO
            r8 = r7
            long r6 = com.sec.ims.options.Capabilities.getTagFeature(r6)
            r5.addFeature(r6)
            java.lang.String r6 = com.sec.ims.options.Capabilities.FEATURE_TAG_MMTEL
            long r6 = com.sec.ims.options.Capabilities.getTagFeature(r6)
            r5.addFeature(r6)
            com.sec.internal.helper.SimpleEventLog r6 = r0.mEventLog
            java.lang.String r7 = "createUserAgent: add mmtel, mmtel-video to Capabilities for E-REGI"
            r6.logAndAdd(r3, r7)
            goto L_0x0117
        L_0x0106:
            r8 = r7
            java.lang.String r6 = com.sec.ims.options.Capabilities.FEATURE_TAG_MMTEL
            long r6 = com.sec.ims.options.Capabilities.getTagFeature(r6)
            r5.addFeature(r6)
            com.sec.internal.helper.SimpleEventLog r6 = r0.mEventLog
            java.lang.String r7 = "createUserAgent: add mmtel to Capabilities for E-REGI"
            r6.logAndAdd(r3, r7)
        L_0x0117:
            com.sec.internal.ims.core.SlotBasedConfig r6 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r3)
            android.content.Context r7 = r0.mContext
            boolean r7 = com.sec.internal.ims.util.ImsUtil.isRttModeOnFromCallSettings(r7, r3)
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r7)
            r6.setRTTMode(r7)
            goto L_0x012a
        L_0x0129:
            r8 = r7
        L_0x012a:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "createUserAgent: ownCap= "
            r6.append(r7)
            r6.append(r5)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.s(r13, r3, r6)
            long r6 = r68.getFeature()
            r40 = r8
            int r8 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            r41 = r11
            long r10 = (long) r8
            int r6 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1))
            if (r6 != 0) goto L_0x0169
            int r6 = r67.size()
            r7 = 1
            if (r6 != r7) goto L_0x0163
            java.lang.String r6 = "smsip"
            boolean r6 = r4.contains(r6)
            if (r6 == 0) goto L_0x0163
            java.lang.String r6 = "createUserAgent: empty capabilities. smsip only registration"
            com.sec.internal.log.IMSLog.e(r13, r3, r6)
            goto L_0x0169
        L_0x0163:
            java.lang.String r0 = "createUserAgent: empty capabilities. fail to create"
            com.sec.internal.log.IMSLog.e(r13, r3, r0)
            return r16
        L_0x0169:
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r6 = r0.mSimManagers
            java.lang.Object r6 = r6.get(r3)
            com.sec.internal.interfaces.ims.core.ISimManager r6 = (com.sec.internal.interfaces.ims.core.ISimManager) r6
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = new com.sec.internal.ims.core.handler.secims.UserAgent
            android.content.Context r8 = r0.mContext
            android.os.Handler r10 = r0.mUaHandler
            com.sec.internal.ims.core.handler.secims.StackIF r45 = com.sec.internal.ims.core.handler.secims.StackIF.getInstance()
            com.sec.internal.helper.os.ITelephonyManager r11 = r0.mTelephonyManager
            com.sec.internal.interfaces.ims.core.IPdnController r5 = r0.mPdnController
            com.sec.internal.interfaces.ims.IImsFramework r4 = r0.mImsFramework
            r42 = r7
            r43 = r8
            r44 = r10
            r46 = r11
            r47 = r5
            r48 = r6
            r49 = r4
            r42.<init>(r43, r44, r45, r46, r47, r48, r49)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "createUserAgent: pdn "
            r4.append(r5)
            r4.append(r14)
            java.lang.String r5 = "("
            r4.append(r5)
            r4.append(r1)
            java.lang.String r5 = ") services "
            r4.append(r5)
            java.lang.String r5 = r67.toString()
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "createUserAgent: uuid "
            r4.append(r5)
            r4.append(r9)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r4)
            int r4 = r12.getCmcType()
            r5 = 7
            if (r4 == r5) goto L_0x01e7
            r5 = 8
            if (r4 == r5) goto L_0x01e7
            r5 = 5
            if (r4 != r5) goto L_0x01dd
            goto L_0x01e7
        L_0x01dd:
            if (r15 != 0) goto L_0x01e2
            r4 = 0
            goto L_0x01eb
        L_0x01e2:
            long r4 = r15.getNetworkHandle()
            goto L_0x01eb
        L_0x01e7:
            long r4 = r0.getP2pNetworkHandle(r4)
        L_0x01eb:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r10 = "createUserAgent: profile="
            r8.append(r10)
            java.lang.String r10 = r12.getName()
            r8.append(r10)
            java.lang.String r10 = " iface="
            r8.append(r10)
            r8.append(r2)
            java.lang.String r10 = " NetId="
            r8.append(r10)
            r8.append(r4)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r8)
            java.lang.String r8 = r12.getRegistrationAlgorithm()
            if (r8 != 0) goto L_0x021b
            java.lang.String r8 = "md5"
        L_0x021b:
            java.lang.String r10 = r12.getAuthAlgorithm()
            if (r10 != 0) goto L_0x0223
            java.lang.String r10 = "hmac-md5-96,hmac-sha-1-96,hmac-sha-2-256-128,hmac-sha-2-512-256"
        L_0x0223:
            java.lang.String r11 = r12.getEncAlgorithm()
            if (r11 != 0) goto L_0x022b
            java.lang.String r11 = "null,des-ede3-cbc,aes-cbc,aes-gcm-16"
        L_0x022b:
            r42 = r15
            android.content.Context r15 = r0.mContext
            java.lang.String r15 = com.sec.internal.ims.util.ConfigUtil.getRcsProfileWithFeature(r15, r3, r12)
            int r15 = com.sec.ims.settings.ImsProfile.getRcsProfileType(r15)
            int r43 = r12.getTimer1()
            int r44 = r12.getTimer2()
            int r45 = r12.getTimer4()
            int r46 = r12.getRegRetryBaseTime()
            int r47 = r12.getRegRetryMaxTime()
            int r48 = r12.getQValue()
            java.lang.Integer r48 = java.lang.Integer.valueOf(r48)
            com.sec.internal.constants.Mno r49 = r6.getSimMno()
            java.lang.String r50 = r6.getSimMnoName()
            java.lang.String r50 = com.sec.internal.helper.SimUtil.getMvnoName(r50)
            r51 = r7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r52 = r14
            java.lang.String r14 = "createUserAgent: task.getMno()="
            r7.append(r14)
            com.sec.internal.constants.Mno r14 = r64.getMno()
            r7.append(r14)
            java.lang.String r14 = " sm.getSimMno()="
            r7.append(r14)
            com.sec.internal.constants.Mno r14 = r6.getSimMno()
            r7.append(r14)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r7)
            com.sec.internal.constants.Mno r7 = r64.getMno()
            com.sec.internal.constants.Mno r14 = r6.getSimMno()
            if (r7 == r14) goto L_0x02bc
            com.sec.internal.constants.Mno r7 = r64.getMno()
            java.lang.String r14 = r12.getMnoName()
            java.lang.String r14 = com.sec.internal.helper.SimUtil.getMvnoName(r14)
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            r53 = r15
            java.lang.String r15 = "createUserAgent: Updated mno: "
            r9.append(r15)
            r9.append(r7)
            java.lang.String r15 = " mvno: "
            r9.append(r15)
            r9.append(r14)
            java.lang.String r9 = r9.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r9)
            goto L_0x02c2
        L_0x02bc:
            r53 = r15
            r7 = r49
            r14 = r50
        L_0x02c2:
            java.util.Set r9 = r12.getAllServiceSetFromAllNetwork()
            boolean r9 = com.sec.internal.ims.rcs.util.RcsUtils.isAutoConfigNeeded(r9)
            java.lang.String r15 = ""
            if (r9 != 0) goto L_0x0304
            java.lang.String r9 = r12.getPassword()
            r49 = r14
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r50 = r11
            java.lang.String r11 = "createUserAgent: AUTOCONFIG_NOT_NEEDED password="
            r14.append(r11)
            r14.append(r9)
            java.lang.String r11 = r14.toString()
            com.sec.internal.log.IMSLog.s(r13, r3, r11)
            java.lang.String r11 = "CPM"
            r14 = r16
            r57 = r43
            r58 = r44
            r59 = r45
            r60 = r46
            r61 = r47
            r47 = r1
            r44 = r8
            r43 = r10
            r10 = r15
            r1 = r18
            r8 = 0
            goto L_0x03a5
        L_0x0304:
            r50 = r11
            r49 = r14
            java.lang.String r9 = "password"
            r11 = r74
            java.lang.String r9 = r11.getString(r9)
            java.lang.String r14 = "realm"
            java.lang.String r14 = r11.getString(r14)
            r18 = r14
            java.lang.String r14 = "imMsgTech"
            java.lang.String r14 = r11.getString(r14)
            r43 = r10
            java.lang.String r10 = "msrpTransType"
            java.lang.String r10 = r11.getString(r10)
            r44 = r8
            java.lang.String r8 = "transport"
            java.lang.String r8 = r11.getString(r8)
            r45 = r8
            java.lang.String r8 = "useKeepAlive"
            boolean r8 = r11.getBoolean(r8)
            r46 = r8
            java.lang.String r8 = "qVal"
            int r8 = r11.getInt(r8)
            java.lang.Integer r48 = java.lang.Integer.valueOf(r8)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r47 = r1
            java.lang.String r1 = "getRcsConfig - password : "
            r8.append(r1)
            java.lang.String r1 = com.sec.internal.log.IMSLog.checker(r9)
            r8.append(r1)
            java.lang.String r1 = ", imMsgTech : "
            r8.append(r1)
            r8.append(r14)
            java.lang.String r1 = ", msrpTransType : "
            r8.append(r1)
            r8.append(r10)
            java.lang.String r1 = r8.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r1)
            java.lang.String r1 = "Timer_T1"
            int r1 = r11.getInt(r1)
            java.lang.String r8 = "Timer_T2"
            int r8 = r11.getInt(r8)
            r54 = r1
            java.lang.String r1 = "Timer_T4"
            int r1 = r11.getInt(r1)
            r55 = r1
            java.lang.String r1 = "RegRetryBaseTime"
            int r1 = r11.getInt(r1)
            r56 = r1
            java.lang.String r1 = "RegRetryMaxTime"
            int r1 = r11.getInt(r1)
            r61 = r1
            r58 = r8
            r11 = r14
            r14 = r18
            r1 = r45
            r8 = r46
            r57 = r54
            r59 = r55
            r60 = r56
        L_0x03a5:
            java.lang.String r18 = r12.getMnoName()
            com.sec.internal.constants.Mno r18 = com.sec.internal.constants.Mno.fromName(r18)
            boolean r18 = r18.isKor()
            if (r18 == 0) goto L_0x03cf
            boolean r18 = com.sec.internal.ims.util.ConfigUtil.isRcsOnly(r12)
            if (r18 != 0) goto L_0x03cf
            r18 = r10
            android.content.Context r10 = r0.mContext
            com.sec.ims.settings.ImsProfile r10 = com.sec.internal.ims.settings.DmProfileLoader.getProfile(r10, r12, r3)
            com.sec.internal.ims.core.handler.secims.CallProfile r10 = r0.configureMedia(r10)
            r74 = r10
            java.lang.String r10 = "createUserAgent: imsDmProfile from DmProfileLoader"
            com.sec.internal.log.IMSLog.i(r13, r3, r10)
            r10 = r74
            goto L_0x03d5
        L_0x03cf:
            r18 = r10
            com.sec.internal.ims.core.handler.secims.CallProfile r10 = r0.configureMedia(r12)
        L_0x03d5:
            r45 = r8
            java.lang.String r8 = "is_server_header_enabled"
            if (r41 == 0) goto L_0x043d
            boolean r46 = r6.hasNoSim()
            if (r46 != 0) goto L_0x03ee
            r46 = r10
            boolean r10 = r6.hasNoSim()
            boolean r10 = com.sec.internal.ims.util.ImsUtil.needForceToUsePsE911(r3, r10)
            if (r10 == 0) goto L_0x043f
            goto L_0x03f0
        L_0x03ee:
            r46 = r10
        L_0x03f0:
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = com.sec.internal.ims.core.handler.secims.UaProfile.Builder.newBuilder()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setImpi(r15)
            java.lang.String r10 = "sip:anonymous@anonymous.invalid"
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setImpu(r10)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setImsiBasedImpu(r15)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setPreferredId(r10)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setDomain(r15)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setPassword(r15)
            r10 = 0
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setIsIpSec(r10)
            boolean r15 = r12.isWifiPreConditionEnabled()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setWifiPreConditionEnabled(r15)
            boolean r15 = r12.shouldUseCompactHeader()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setUseCompactHeader(r15)
            r15 = 1
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r9 = r9.setEmergencyProfile(r15)
            com.sec.internal.interfaces.ims.IImsFramework r15 = r0.mImsFramework
            boolean r8 = r15.getBoolean(r3, r8, r10)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r8 = r9.setIsServerHeaderEnabled(r8)
            r54 = r29
            r29 = r38
            r9 = r41
            r38 = r6
            goto L_0x0508
        L_0x043d:
            r46 = r10
        L_0x043f:
            boolean r10 = android.text.TextUtils.isEmpty(r71)
            java.lang.String r15 = "null"
            java.lang.String r54 = "empty"
            if (r10 == 0) goto L_0x0465
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "createUserAgent: impi is "
            r0.append(r1)
            r10 = r71
            if (r10 != 0) goto L_0x0458
            goto L_0x045a
        L_0x0458:
            r15 = r54
        L_0x045a:
            r0.append(r15)
            java.lang.String r0 = r0.toString()
            com.sec.internal.log.IMSLog.e(r13, r3, r0)
            return r16
        L_0x0465:
            r10 = r71
            boolean r55 = android.text.TextUtils.isEmpty(r69)
            if (r55 == 0) goto L_0x0489
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "createUserAgent: domain is "
            r0.append(r1)
            r1 = r69
            if (r1 != 0) goto L_0x047c
            goto L_0x047e
        L_0x047c:
            r15 = r54
        L_0x047e:
            r0.append(r15)
            java.lang.String r0 = r0.toString()
            com.sec.internal.log.IMSLog.e(r13, r3, r0)
            return r16
        L_0x0489:
            r15 = r69
            r62 = r38
            r38 = r6
            r6 = r62
            boolean r54 = android.text.TextUtils.isEmpty(r14)
            if (r54 == 0) goto L_0x049a
            r74 = r15
            goto L_0x049c
        L_0x049a:
            r74 = r14
        L_0x049c:
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r14 = com.sec.internal.ims.core.handler.secims.UaProfile.Builder.newBuilder()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r10 = r14.setImpi(r10)
            r14 = r70
            r62 = r29
            r29 = r6
            r6 = r62
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r10 = r10.setImpu(r14)
            r54 = r6
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r6 = r0.mSimManagers
            java.lang.Object r6 = r6.get(r3)
            com.sec.internal.interfaces.ims.core.ISimManager r6 = (com.sec.internal.interfaces.ims.core.ISimManager) r6
            java.lang.String r6 = r6.getDerivedImpu()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r10.setImsiBasedImpu(r6)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r6.setPreferredId(r14)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r6.setDomain(r15)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r6.setPassword(r9)
            boolean r9 = r12.isIpSecEnabled()
            if (r9 == 0) goto L_0x04e0
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r9 = r64.getGovernor()
            boolean r9 = r9.isIPSecAllow()
            if (r9 == 0) goto L_0x04e0
            r9 = 1
            goto L_0x04e1
        L_0x04e0:
            r9 = 0
        L_0x04e1:
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r6.setIsIpSec(r9)
            boolean r9 = r12.isWifiPreConditionEnabled()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r6.setWifiPreConditionEnabled(r9)
            boolean r9 = r12.shouldUseCompactHeader()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r6.setUseCompactHeader(r9)
            r9 = r41
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r6 = r6.setEmergencyProfile(r9)
            com.sec.internal.interfaces.ims.IImsFramework r10 = r0.mImsFramework
            r14 = 0
            boolean r8 = r10.getBoolean(r3, r8, r14)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r8 = r6.setIsServerHeaderEnabled(r8)
            r14 = r74
        L_0x0508:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r10 = "###set profile id, id = "
            r6.append(r10)
            int r10 = r12.getId()
            r6.append(r10)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r6)
            r6 = 4
            r10 = 3
            r15 = r36
            if (r15 != r6) goto L_0x0534
            com.sec.internal.ims.core.SlotBasedConfig r6 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r3)
            boolean r6 = r6.getRTTMode()
            if (r6 == 0) goto L_0x0531
            goto L_0x0544
        L_0x0531:
            r6 = 2
            r10 = r6
            goto L_0x0544
        L_0x0534:
            if (r15 != r10) goto L_0x0543
            com.sec.internal.ims.core.SlotBasedConfig r6 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r3)
            boolean r6 = r6.getRTTMode()
            if (r6 == 0) goto L_0x0541
            goto L_0x0544
        L_0x0541:
            r10 = 0
            goto L_0x0544
        L_0x0543:
            r10 = r15
        L_0x0544:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            r36 = r11
            java.lang.String r11 = "TTY Type "
            r6.append(r11)
            r6.append(r15)
            java.lang.String r11 = " convert to TextMode "
            r6.append(r11)
            r6.append(r10)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r13, r3, r6)
            com.sec.internal.interfaces.ims.IImsFramework r6 = r0.mImsFramework
            java.lang.String r11 = "srvcc_version"
            r15 = 0
            int r6 = r6.getInt(r3, r11, r15)
            com.sec.internal.interfaces.ims.IImsFramework r11 = r0.mImsFramework
            r41 = r13
            java.lang.String r13 = "ignore_display_name"
            boolean r11 = r11.getBoolean(r3, r13, r15)
            com.sec.internal.interfaces.ims.IImsFramework r13 = r0.mImsFramework
            r69 = r11
            java.lang.String r11 = "keep_alive_factor"
            int r11 = r13.getInt(r3, r11, r15)
            com.sec.internal.interfaces.ims.IImsFramework r13 = r0.mImsFramework
            r70 = r11
            java.lang.String r11 = "support_uac"
            boolean r11 = r13.getBoolean(r3, r11, r15)
            if (r11 == 0) goto L_0x0595
            boolean r11 = r0.isCpSupportUac(r3)
            if (r11 == 0) goto L_0x0595
            r11 = 1
            goto L_0x0596
        L_0x0595:
            r11 = 0
        L_0x0596:
            java.util.List r13 = r12.getUacList()
            r0.filterUacSipListByCpUacType(r3, r13)
            int r15 = r0.getVowifi5gsaMode(r3, r7, r12)
            r21 = r7
            com.sec.internal.interfaces.ims.IImsFramework r7 = r0.mImsFramework
            r71 = r13
            java.lang.String r13 = "is_support_update_sa_mode_on_start"
            r55 = r11
            r11 = 0
            boolean r7 = r7.getBoolean(r3, r13, r11)
            int r9 = r0.getTimerF(r9, r12)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r8.setIface(r2)
            int r11 = r12.getId()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setProfileId(r11)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setNetId(r4)
            r4 = r47
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setPdn(r4)
            r4 = r67
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setServiceList(r4)
            r4 = r68
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setOwnCapabilities(r4)
            int r4 = r48.intValue()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setQparam(r4)
            android.content.Context r4 = r0.mContext
            java.lang.String r5 = r12.getRemoteUriType()
            boolean r11 = r12.getNeedAutoconfig()
            com.sec.ims.util.ImsUri$UriType r4 = com.sec.internal.helper.RcsConfigurationHelper.getNetworkUriType(r4, r5, r11, r3)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRemoteUriType(r4)
            int r4 = r12.getControlDscp()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setControlDscp(r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTransportType(r1)
            int r4 = r12.getSipPort()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setPcscfPort(r4)
            r4 = r44
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRegiAlgorithm(r4)
            r4 = r43
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setAuthAlg(r4)
            r11 = r50
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setEncrAlg(r11)
            r4 = r22
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setPrecondEnabled(r4)
            r4 = r23
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setPrecondInitialSendrecv(r4)
            r4 = r24
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSessionExpires(r4)
            r4 = r27
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRegExpires(r4)
            r4 = r25
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setMinSe(r4)
            java.lang.String r4 = r12.getSipUserAgent()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUserAgent(r4)
            java.lang.String r4 = r12.getDisplayName()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setDisplayName(r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRealm(r14)
            r11 = r36
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setImMsgTech(r11)
            int r4 = r12.getRingbackTimer()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRingbackTimer(r4)
            int r4 = r12.getRingingTimer()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRingingTimer(r4)
            r4 = r46
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setCallProfile(r4)
            boolean r4 = r12.isSoftphoneEnabled()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsSoftphoneEnabled(r4)
            android.content.Context r4 = r0.mContext
            boolean r4 = com.sec.internal.ims.util.ImsUtil.isCdmalessEnabled(r4, r3)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsCdmalessEnabled(r4)
            r4 = r28
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setMssSize(r4)
            r4 = r30
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSipMobility(r4)
            r4 = r31
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsEnableGruu(r4)
            r4 = r32
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsEnableVcid(r4)
            r4 = r33
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsEnableSessionId(r4)
            r4 = r34
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setAudioEngineType(r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTextMode(r10)
            boolean r4 = r12.isVceConfigEnabled()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setVceConfigEnabled(r4)
            r4 = r54
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setGcfConfigEnabled(r4)
            r4 = 0
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setNsdsServiceEnabled(r4)
            boolean r4 = r12.isMsrpBearerUsed()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setMsrpBearerUsed(r4)
            int r4 = r12.getSubscriberTimer()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSubscriberTimer(r4)
            r4 = r35
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSubscribeReg(r4)
            r4 = r45
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUseKeepAlive(r4)
            int r4 = r12.getSelfPort()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSelfPort(r4)
            int r4 = r12.getScmVersion()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setScmVersion(r4)
            int r4 = com.sec.internal.helper.SimUtil.getActiveDataPhoneId()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setActiveDataPhoneId(r4)
            r10 = r18
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setMsrpTransType(r10)
            boolean r4 = r12.getFullCodecOfferRequired()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsFullCodecOfferRequired(r4)
            boolean r4 = r12.getRcsTelephonyFeatureTagRequired()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsRcsTelephonyFeatureTagRequired(r4)
            r4 = r53
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRcsProfile(r4)
            boolean r4 = r12.getIsTransportNeeded()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsTransportNeeded(r4)
            r4 = r20
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRat(r4)
            int r4 = r12.getDbrTimer()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setDbrTimer(r4)
            boolean r4 = r12.isTcpGracefulShutdownEnabled()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsTcpGracefulShutdownEnabled(r4)
            int r4 = r12.getTcpRstUacErrorcode()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTcpRstUacErrorcode(r4)
            int r4 = r12.getTcpRstUasErrorcode()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTcpRstUasErrorcode(r4)
            java.lang.String r4 = r12.getPrivacyHeaderRestricted()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setPrivacyHeaderRestricted(r4)
            boolean r4 = r12.getUsePemHeader()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUsePemHeader(r4)
            boolean r4 = r12.getSupportEct()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportEct(r4)
            int r4 = r12.getEarlyMediaRtpTimeoutTimer()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setEarlyMediaRtpTimeoutTimer(r4)
            boolean r4 = r12.getAddHistinfo()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setAddHistinfo(r4)
            int r4 = r12.getSupportedGeolocationPhase()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportedGeolocationPhase(r4)
            int r4 = r12.getNeedPidfSipMsg()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setNeedPidfSipMsg(r4)
            int r4 = r12.getNeedPidfRat()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setNeedPidfRat(r4)
            boolean r4 = r12.getUseSubcontactWhenResub()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUseSubcontactWhenResub(r4)
            boolean r4 = r12.getUseProvisionalResponse100rel()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUseProvisionalResponse100rel(r4)
            boolean r4 = r12.getUse183OnProgressIncoming()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUse183OnProgressIncoming(r4)
            boolean r4 = r12.getUseQ850causeOn480()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUseQ850causeOn480(r4)
            boolean r4 = r12.getSupport183ForIr92v9Precondition()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupport183ForIr92v9Precondition(r4)
            boolean r4 = r12.getSupportImsNotAvailable()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportImsNotAvailable(r4)
            boolean r4 = r12.getSupportLtePreferred()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportLtePreferred(r4)
            r4 = r40
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportUpgradePrecondition(r4)
            boolean r4 = r12.getSupportReplaceMerge()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportReplaceMerge(r4)
            boolean r4 = r12.getSupportAccessType()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportAccessType(r4)
            java.lang.String r4 = r12.getLastPaniHeader()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setLastPaniHeader(r4)
            java.lang.String r4 = r12.getOipFromPreferred()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setOipFromPreferred(r4)
            java.lang.String r4 = r12.getSelectTransportAfterTcpReset()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSelectTransportAfterTcpReset(r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSrvccVersion(r6)
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r37)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsSimMobility(r4)
            int r4 = r12.getCmcType()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setCmcType(r4)
            int r4 = r12.getVideoCrbtSupportType()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setVideoCrbtSupportType(r4)
            boolean r4 = r12.getRetryInviteOnTcpReset()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRetryInviteOnTcpReset(r4)
            boolean r4 = r12.getEnableVerstat()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setEanbleVerstat(r4)
            r4 = r57
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimer1(r4)
            r4 = r58
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimer2(r4)
            r4 = r59
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimer4(r4)
            int r4 = r12.getTimerA()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerA(r4)
            int r4 = r12.getTimerB()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerB(r4)
            int r4 = r12.getTimerC()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerC(r4)
            int r4 = r12.getTimerD()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerD(r4)
            int r4 = r12.getTimerE()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerE(r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerF(r9)
            int r4 = r12.getTimerG()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerG(r4)
            int r4 = r12.getTimerH()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerH(r4)
            int r4 = r12.getTimerI()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerI(r4)
            int r4 = r12.getTimerJ()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerJ(r4)
            int r4 = r12.getTimerK()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTimerK(r4)
            r4 = r60
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRegRetryBaseTime(r4)
            r4 = r61
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setRegRetryMaxTime(r4)
            boolean r4 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isDualRcsReg()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportDualRcs(r4)
            boolean r4 = com.sec.internal.ims.util.ImsUtil.isPttSupported()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setPttSupported(r4)
            boolean r4 = r12.getTryReregisterFromKeepalive()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setTryReregisterFromKeepalive(r4)
            int r4 = r12.getSslType()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSslType(r4)
            boolean r4 = r12.getSupport199ProvisionalResponse()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupport199ProvisionalResponse(r4)
            boolean r4 = r12.getSend18xReliably()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSend18xReliably(r4)
            r4 = r39
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setAcb(r4)
            r4 = r69
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIgnoreDisplayName(r4)
            boolean r4 = r12.getSupportNetworkInitUssi()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportNetworkInitUssi(r4)
            boolean r4 = r12.getSendByeForUssi()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSendByeForUssi(r4)
            boolean r4 = r12.getSupportRfc6337ForDelayedOffer()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportRfc6337ForDelayedOffer(r4)
            boolean r4 = r12.getUse200offerWhenRemoteNotSupport100rel()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUse200offerWhenRemoteNotSupport100rel(r4)
            int r4 = r12.getHashAlgoType()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setHashAlgoType(r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setVowifi5gsaMode(r15)
            boolean r4 = r12.getExcludePaniVowifiInitialRegi()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setExcludePaniVowifiInitialRegi(r4)
            boolean r4 = com.sec.internal.ims.util.ImsUtil.isSingleRegiAppConnected(r3)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSingleRegiEnabled(r4)
            boolean r4 = r12.getNeedCheckAllowedMethodForRefresh()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setNeedCheckAllowedMethodForRefresh(r4)
            r4 = r70
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setKeepAliveFactor(r4)
            r4 = r29
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setEncrNullRoaming(r4)
            android.content.Context r4 = r0.mContext
            boolean r4 = com.sec.internal.ims.util.ImsUtil.isAddMmtelCallComposerTag(r3, r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setAddMmtelCallComposerTag(r4)
            r4 = r55
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setSupportUac(r4)
            r4 = r71
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setUacSipList(r4)
            boolean r4 = r12.getNeedVoLteRetryInNr()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setNeedVolteRetryInNr(r4)
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r4 = r64.getGovernor()
            int r4 = r4.getNextImpuType()
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setImpuPreference(r4)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r2 = r2.setIsUpdateSaOnStartSupported(r7)
            int r4 = r12.getSupportB2cCallcomposerWithoutFeaturetag()
            r2.setSupportB2cCallcomposerWithoutFeaturetag(r4)
            boolean r2 = r12.isSamsungMdmnEnabled()
            if (r2 == 0) goto L_0x09b1
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.MDMN
            r8.setMno(r2)
            java.lang.String r2 = r12.getAccessToken()
            r8.setAccessToken(r2)
            java.lang.String r2 = "saServerUrl"
            r4 = r75
            java.lang.String r2 = r4.getString(r2)
            java.lang.String r5 = "relayType"
            java.lang.String r5 = r4.getString(r5)
            java.lang.String r6 = "eCallNum"
            java.lang.String r4 = r4.getString(r6)
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "SA url: "
            r6.append(r7)
            java.lang.String r7 = com.sec.internal.log.IMSLog.checker(r2)
            r6.append(r7)
            java.lang.String r7 = ", relayType: "
            r6.append(r7)
            r6.append(r5)
            java.lang.String r7 = ", cmcEmergencyNumbers: "
            r6.append(r7)
            java.lang.String r7 = com.sec.internal.log.IMSLog.checker(r4)
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            r7 = r41
            com.sec.internal.log.IMSLog.i(r7, r6)
            boolean r6 = android.text.TextUtils.isEmpty(r2)
            if (r6 != 0) goto L_0x0993
            r8.setAuthServerUrl(r2)
        L_0x0993:
            boolean r2 = android.text.TextUtils.isEmpty(r5)
            if (r2 != 0) goto L_0x099c
            r8.setCmcRelayType(r5)
        L_0x099c:
            boolean r2 = android.text.TextUtils.isEmpty(r4)
            if (r2 != 0) goto L_0x09a5
            r8.setCmcEmergencyNumbers(r4)
        L_0x09a5:
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r2 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()
            boolean r2 = r2.isSupportDualSimCMC()
            r8.setSupportDualSimCmc(r2)
            goto L_0x09d4
        L_0x09b1:
            r7 = r41
            com.sec.internal.constants.Mno r2 = r38.getDevMno()
            boolean r2 = r2.isAus()
            if (r2 == 0) goto L_0x09cf
            boolean r2 = r12.hasEmergencySupport()
            if (r2 == 0) goto L_0x09cf
            java.lang.String r2 = r12.getMnoName()
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.fromName(r2)
            r8.setMno(r2)
            goto L_0x09d4
        L_0x09cf:
            r2 = r21
            r8.setMno(r2)
        L_0x09d4:
            r14 = r49
            r8.setMvnoName(r14)
            r2 = r72
            r8.setInstanceId(r2)
            r2 = r73
            r8.setUuid(r2)
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "createUserAgent: TransportType="
            r2.append(r4)
            r2.append(r1)
            java.lang.String r1 = " port="
            r2.append(r1)
            int r1 = r12.getSipPort()
            r2.append(r1)
            java.lang.String r1 = r2.toString()
            com.sec.internal.log.IMSLog.i(r7, r3, r1)
            boolean r1 = android.text.TextUtils.isEmpty(r26)
            if (r1 != 0) goto L_0x0a22
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "createUserAgent: sessionRefresher="
            r1.append(r2)
            r2 = r26
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r7, r3, r1)
            r8.setSessionRefresher(r2)
        L_0x0a22:
            java.util.List r1 = r12.getExtImpuList()
            int r1 = r1.size()
            if (r1 <= 0) goto L_0x0a33
            java.util.List r1 = r12.getExtImpuList()
            r8.setLinkedImpuList(r1)
        L_0x0a33:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "createUserAgent: hostname="
            r1.append(r2)
            r2 = r17
            r1.append(r2)
            java.lang.String r4 = ", P-CSCF="
            r1.append(r4)
            r4 = r3
            r3 = r66
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r7, r4, r1)
            com.sec.internal.ims.core.handler.secims.UaProfile$Builder r1 = r8.setHostname(r2)
            r1.setPcscfIp(r3)
            java.lang.String r1 = r12.getAppId()
            r8.setContactDisplayName(r1)
            r8.setPhoneId(r4)
            r0.configureTimerTS(r12, r8)
            r2 = r51
            r1 = r52
            r2.setPdn(r1)
            r1 = r42
            r2.setNetwork(r1)
            com.sec.internal.ims.core.handler.secims.PaniGenerator r1 = r0.mPaniGenerator
            int r3 = r2.getPdn()
            java.lang.String r5 = r12.getOperator()
            java.lang.String r1 = r1.generate(r3, r5, r4, r12)
            boolean r3 = android.text.TextUtils.isEmpty(r1)
            if (r3 == 0) goto L_0x0a99
            com.sec.internal.constants.ims.DiagnosisConstants$REGI_FRSN r0 = com.sec.internal.constants.ims.DiagnosisConstants.REGI_FRSN.NO_PANI_NO_USER_AGENT
            int r0 = r0.getCode()
            r3 = r64
            r3.setRegiFailReason(r0)
            java.lang.String r0 = "createUserAgent: pani is null"
            com.sec.internal.log.IMSLog.e(r7, r4, r0)
            return r16
        L_0x0a99:
            r3 = r64
            r8.setCurPani(r1)
            android.content.Context r1 = r0.mContext
            boolean r1 = com.sec.internal.ims.xq.att.ImsXqReporter.isXqEnabled(r1, r4)
            r8.setIsXqEnabled(r1)
            boolean r1 = r12.isVceConfigEnabled()
            if (r1 == 0) goto L_0x0ab7
            java.lang.String r1 = "enable subscribe dialog"
            com.sec.internal.log.IMSLog.e(r7, r4, r1)
            java.lang.Boolean r1 = java.lang.Boolean.TRUE
            r8.setSubscribeDialogEvent(r1)
        L_0x0ab7:
            r2.setImsProfile(r12)
            com.sec.internal.ims.core.handler.secims.UaProfile r1 = r8.build()
            r2.setUaProfile(r1)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r5 = "createUserAgent:mno="
            r1.append(r5)
            com.sec.internal.ims.core.handler.secims.UaProfile r5 = r8.build()
            com.sec.internal.constants.Mno r5 = r5.getMno()
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r7, r4, r1)
            com.sec.internal.ims.core.handler.secims.ResipRegistrationManager$2 r1 = new com.sec.internal.ims.core.handler.secims.ResipRegistrationManager$2
            r5 = r76
            r1.<init>(r4, r3, r5)
            r2.registerListener(r1)
            r2.create()
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipRegistrationManager.createUserAgent(com.sec.internal.interfaces.ims.core.IRegisterTask, java.lang.String, java.lang.String, java.util.Set, com.sec.ims.options.Capabilities, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, android.os.Bundle, android.os.Bundle, com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable):com.sec.internal.ims.core.handler.secims.UserAgent");
    }

    private long getP2pNetworkHandle(int i) {
        IMSLog.i(LOG_TAG, "getP2pNetworkHandle, cmcType: " + i);
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        long j = 0;
        for (Network network : connectivityManager.getAllNetworks()) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            long networkHandle = network.getNetworkHandle();
            IMSLog.i(LOG_TAG, "netId (NetworkHandle): " + networkHandle);
            if (i != 7 && i != 8) {
                j = networkHandle;
            } else if (networkCapabilities == null || !networkCapabilities.hasTransport(1)) {
                IMSLog.i(LOG_TAG, "not found netId for wifi-direct");
                j = 0;
            } else {
                IMSLog.i(LOG_TAG, "Found netId for cmcType: " + i + ", netId: " + networkHandle);
                return networkHandle;
            }
        }
        return j;
    }

    private String getPdnName(int i) {
        if (i == -1) {
            return "default";
        }
        if (i == 0) {
            return "internet";
        }
        if (i == 1) {
            return "wifi";
        }
        if (i == 5) {
            return "internet";
        }
        if (i == 11) {
            return DeviceConfigManager.IMS;
        }
        if (i == 15) {
            return "emergency";
        }
        return "unknown(" + i + ")";
    }

    private void configureRCS(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "configureRCS:");
        RcsConfig rcsConfig = RcsPolicyManager.getRcsConfig(this.mContext, iRegisterTask.getProfile(), phoneId, (ImConfig) Optional.ofNullable(this.mImsFramework.getServiceModuleManager().getImModule()).map(new ResipRegistrationManager$$ExternalSyntheticLambda3(phoneId)).orElse((Object) null));
        if (rcsConfig != null) {
            StackIF.getInstance().configRCS(phoneId, rcsConfig);
        }
    }

    public void configure(int i) {
        IMSLog.i(LOG_TAG, i, "configure:");
        StackIF instance = StackIF.getInstance();
        String imei = this.mTelephonyManager.getImei(i);
        if (!TextUtils.isEmpty(imei)) {
            imei = DeviceUtil.getFormattedDeviceId(imei, SimUtil.getSimMno(i) == Mno.TMOUS ? this.mTelephonyManager.getDeviceSoftwareVersion(SimUtil.getSubId(i)) : "");
        }
        instance.configRegistration(i, imei);
        instance.configSrvcc(i, this.mImsFramework.getInt(i, GlobalSettingsConstants.Call.SRVCC_VERSION, 0));
    }

    public void sendDmState(int i, boolean z) {
        IMSLog.i(LOG_TAG, i, "sendDmState:" + z);
        StackIF.getInstance().sendDmState(i, z);
    }

    public void setSilentLogEnabled(boolean z) {
        Log.i(LOG_TAG, "setSilentLogEnabled:");
        StackIF.getInstance().setSilentLogEnabled(z);
    }

    public boolean isUserAgentInRegistered(IRegisterTask iRegisterTask) {
        return ((Boolean) Optional.ofNullable(this.mUaList.get(Integer.valueOf(IRegistrationManager.getRegistrationInfoId(iRegisterTask.getProfile().getId(), iRegisterTask.getPhoneId())))).map(new ResipRegistrationManager$$ExternalSyntheticLambda2()).orElse(Boolean.FALSE)).booleanValue();
    }

    public void sendDnsQuery(int i, String str, String str2, List<String> list, String str3, String str4, String str5, long j) {
        StringBuilder sb = new StringBuilder();
        sb.append("sendDnsQuery: handle ");
        int i2 = i;
        sb.append(i);
        Log.i(LOG_TAG, sb.toString());
        StackIF.getInstance().sendDnsQuery(i, str, str2, list, str3, str4, str5, j);
    }

    public void updatePani(IRegisterTask iRegisterTask) {
        String str;
        int phoneId = iRegisterTask.getPhoneId();
        ImsProfile profile = iRegisterTask.getProfile();
        PaniGenerator paniGenerator = this.mPaniGenerator;
        int pdnType = iRegisterTask.getPdnType();
        String generate = paniGenerator.generate(pdnType, profile.getMcc() + profile.getMnc(), phoneId, profile);
        if (!TextUtils.isEmpty(generate)) {
            this.mPaniGenerator.setLkcForLastPani(phoneId, generate, profile, new Date());
            if (iRegisterTask.getUserAgent() != null) {
                UserAgent userAgent = (UserAgent) iRegisterTask.getUserAgent();
                if (generate.contains(PaniConstants.IWLAN_PANI_PREFIX)) {
                    str = this.mPaniGenerator.getLastPani(phoneId, profile, new Date());
                    if (this.mPaniGenerator.needCellInfoAge(profile) || this.mPaniGenerator.needCellInfoAgeInactive(profile)) {
                        userAgent.updateTimeInPlani(this.mPaniGenerator.getTimeInPlani(phoneId));
                    }
                } else {
                    str = "";
                }
                userAgent.getUaProfile().setCurPani(generate);
                userAgent.updatePani(generate, str);
                iRegisterTask.setPaniSet(generate, str);
            }
        }
    }

    public void updateTimeInPlani(int i, ImsProfile imsProfile) {
        String lastPani = this.mPaniGenerator.getLastPani(i, imsProfile, new Date());
        if (this.mPaniGenerator.needCellInfoAge(imsProfile) && !TextUtils.isEmpty(lastPani)) {
            long currentTimeMillis = System.currentTimeMillis() / 1000;
            long cid = this.mPaniGenerator.getCid(i);
            boolean isChangedPlani = this.mPaniGenerator.isChangedPlani(i, lastPani);
            IMSLog.s(LOG_TAG, i, "updateTimeInPlani: plani " + lastPani + ", time " + currentTimeMillis);
            if (this.mPaniGenerator.getTimeInPlani(i) == 0) {
                this.mPaniGenerator.setTimeInPlani(i, currentTimeMillis);
            }
            if (cid != 0 && isChangedPlani) {
                this.mPaniGenerator.setTimeInPlani(i, currentTimeMillis);
                Log.i(LOG_TAG, "updateTimeInPlani: plani " + lastPani + ", time " + currentTimeMillis);
            }
        }
    }

    public void handleInactiveCiaOnMobileConnected(int i, RegisterTask registerTask) {
        boolean needCellInfoAgeInactive = this.mPaniGenerator.needCellInfoAgeInactive(registerTask.getProfile());
        Log.i(LOG_TAG, "[" + i + "] handleInactiveCiaOnMobileConnected() need CIA_Inactive = " + needCellInfoAgeInactive + " for " + registerTask.getProfile().getName());
        if (needCellInfoAgeInactive) {
            this.mPaniGenerator.setTimeInPlani(i, 0);
            UserAgent userAgent = (UserAgent) registerTask.getUserAgent();
            if (userAgent == null) {
                Log.i(LOG_TAG, "[" + i + "] handleInactiveCiaOnMobileConnected() task.getUserAgent() returned null");
                return;
            }
            userAgent.updateTimeInPlani(0);
        }
    }

    public void handleInactiveCiaOnMobileDisconnected(int i, RegisterTask registerTask) {
        boolean needCellInfoAgeInactive = this.mPaniGenerator.needCellInfoAgeInactive(registerTask.getProfile());
        Log.i(LOG_TAG, "[" + i + "] handleInactiveCiaOnMobileDisconnected() need CIA_Inactive = " + needCellInfoAgeInactive + " for " + registerTask.getProfile().getName());
        if (needCellInfoAgeInactive) {
            long currentTimeMillis = System.currentTimeMillis() / 1000;
            this.mPaniGenerator.setTimeInPlani(i, currentTimeMillis);
            UserAgent userAgent = (UserAgent) registerTask.getUserAgent();
            if (userAgent == null) {
                Log.i(LOG_TAG, "[" + i + "] handleInactiveCiaOnMobileDisconnected() task.getUserAgent() returned null");
                return;
            }
            userAgent.updateTimeInPlani(currentTimeMillis);
        }
    }

    public void removePreviousLastPani(int i) {
        this.mPaniGenerator.removePreviousPlani(i);
    }

    public void updateRat(IRegisterTask iRegisterTask, int i) {
        if (iRegisterTask.getUserAgent() != null) {
            iRegisterTask.getUserAgent().updateRat(i);
        }
    }

    public void updateVceConfig(IRegisterTask iRegisterTask, boolean z) {
        if (iRegisterTask.getUserAgent() == null) {
            Log.i(LOG_TAG, "updateVceConfig: no pending task, simply return");
        } else {
            iRegisterTask.getUserAgent().updateVceConfig(z);
        }
    }

    public void updateGeolocation(IRegisterTask iRegisterTask, LocationInfo locationInfo) {
        if (iRegisterTask.getUserAgent() == null) {
            Log.i(LOG_TAG, "updateGeolocation: ua is null. return");
        } else {
            iRegisterTask.getUserAgent().updateGeolocation(locationInfo);
        }
    }

    public void dump() {
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Dump of UserAgents:");
        for (UserAgent next : this.mUaList.values()) {
            ImsProfile imsProfile = next.getImsProfile();
            IMSLog.dump(LOG_TAG, "UserAgent [" + next.getHandle() + "] State: [" + next.getStateName() + "], Profile: [" + imsProfile.getName() + "(#" + imsProfile.getId() + ")]");
        }
        StackIF.getInstance().dump();
        this.mPaniGenerator.dump();
    }

    /* access modifiers changed from: protected */
    public int getAudioEngineType() {
        if (DeviceUtil.getModemBoardName().startsWith("SHANNON")) {
            return 1;
        }
        return DeviceUtil.getChipName().startsWith("unisoc") ? 3 : 0;
    }

    public void removeUserAgent(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "removeUserAgent: task " + iRegisterTask.getProfile().getName());
        int registrationInfoId = IRegistrationManager.getRegistrationInfoId(iRegisterTask.getProfile().getId(), iRegisterTask.getPhoneId());
        UserAgent userAgent = this.mUaList.get(Integer.valueOf(registrationInfoId));
        if (userAgent == null) {
            IMSLog.e(LOG_TAG, iRegisterTask.getPhoneId(), "removeUserAgent: UserAgent null");
            iRegisterTask.clearUserAgent();
            return;
        }
        int phoneId2 = iRegisterTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId2, "removeUserAgent: UserAgent handle " + userAgent.getHandle());
        userAgent.terminate();
        this.mUaList.remove(Integer.valueOf(registrationInfoId));
        iRegisterTask.clearUserAgent();
    }

    public UserAgent getUserAgentByRegId(int i) {
        for (UserAgent next : this.mUaList.values()) {
            ImsRegistration imsRegistration = next.getImsRegistration();
            if (imsRegistration != null && imsRegistration.getHandle() == i) {
                return next;
            }
        }
        return null;
    }

    public String getImsiByUserAgent(IUserAgent iUserAgent) {
        if (iUserAgent != null) {
            return (String) Optional.ofNullable(this.mSimManagers.get(iUserAgent.getPhoneId())).map(new ResipRegistrationManager$$ExternalSyntheticLambda1()).orElse((Object) null);
        }
        IMSLog.e(LOG_TAG, "getImsiByUserAgent: ua is null!");
        return null;
    }

    public String getImsiByUserAgentHandle(int i) {
        UserAgent userAgent = getUserAgent(i);
        if (userAgent != null) {
            return getImsiByUserAgent(userAgent);
        }
        return null;
    }

    public IUserAgent getUserAgentByImsi(String str, String str2) {
        if (str2 == null || str2.equals("")) {
            return getUserAgent(str, (ImsUri) null);
        }
        IMSLog.s(LOG_TAG, "getUserAgentByImsi : Argument imsi = " + str2);
        for (UserAgent next : this.mUaList.values()) {
            ImsRegistration imsRegistration = next.getImsRegistration();
            ImsProfile imsProfile = next.getImsProfile();
            if (imsProfile != null && !imsProfile.hasEmergencySupport() && imsRegistration != null && imsRegistration.hasService(str)) {
                int phoneId = imsRegistration.getPhoneId();
                int subId = SimUtil.getSubId(phoneId);
                Log.i(LOG_TAG, "getUserAgentByImsi, phoneId=" + phoneId + ",subId=" + subId);
                String subscriberId = this.mTelephonyManager.getSubscriberId(subId);
                if (subscriberId != null) {
                    IMSLog.s(LOG_TAG, phoneId, "getUserAgentByImsi imsi = " + subscriberId);
                    if (!subscriberId.equals("") && subscriberId.equals(str2)) {
                        return next;
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    public IUserAgent getUserAgent(String str) {
        return getUserAgent(str, (ImsUri) null);
    }

    public IUserAgent getUserAgent(String str, int i) {
        Log.i(LOG_TAG, "getUserAgent, phoneId=" + i);
        for (UserAgent next : this.mUaList.values()) {
            ImsRegistration imsRegistration = next.getImsRegistration();
            ImsProfile imsProfile = next.getImsProfile();
            if (imsProfile != null && !imsProfile.hasEmergencySupport() && imsRegistration != null && imsRegistration.hasService(str)) {
                Log.i(LOG_TAG, "getUserAgent, reg.getPhoneId()=" + imsRegistration.getPhoneId());
                if (imsRegistration.getPhoneId() == i) {
                    return next;
                }
            }
        }
        return null;
    }

    private UserAgent getUserAgent(String str, ImsUri imsUri) {
        for (UserAgent next : this.mUaList.values()) {
            ImsRegistration imsRegistration = next.getImsRegistration();
            ImsProfile imsProfile = next.getImsProfile();
            if (imsProfile != null && !imsProfile.hasEmergencySupport() && imsRegistration != null && imsRegistration.hasService(str)) {
                if (imsUri != null) {
                    for (NameAddr uri : imsRegistration.getImpuList()) {
                        if (imsUri.equals(uri.getUri())) {
                        }
                    }
                    continue;
                }
                return next;
            }
        }
        if (imsUri != null) {
            return getUserAgent(str, (ImsUri) null);
        }
        return null;
    }

    public UserAgent getUserAgent(int i) {
        for (UserAgent next : this.mUaList.values()) {
            if (next.getHandle() == i) {
                return next;
            }
        }
        return null;
    }

    public UserAgent getUserAgentOnPdn(int i, int i2) {
        for (UserAgent next : this.mUaList.values()) {
            if (next.getPdn() == i && next.getPhoneId() == i2) {
                return next;
            }
        }
        return null;
    }

    public UserAgent[] getUserAgentByPhoneId(int i, String str) {
        ArrayList arrayList = new ArrayList();
        for (UserAgent next : this.mUaList.values()) {
            if (next.getPhoneId() == i) {
                ImsRegistration imsRegistration = next.getImsRegistration();
                ImsProfile imsProfile = next.getImsProfile();
                if (imsProfile != null && !imsProfile.hasEmergencySupport() && imsRegistration != null && imsRegistration.hasService(str)) {
                    arrayList.add(next);
                }
            }
        }
        return (UserAgent[]) arrayList.toArray(new UserAgent[0]);
    }

    private int getTimerF(boolean z, ImsProfile imsProfile) {
        if (!z || imsProfile.getE911RegiTime() <= 0) {
            return imsProfile.getTimerF();
        }
        return Math.min(imsProfile.getTimerF(), imsProfile.getE911RegiTime() * 1000);
    }

    private int getVowifi5gsaMode(int i, Mno mno, ImsProfile imsProfile) {
        if (imsProfile.getCmcType() != 0) {
            return ImsConstants.NrSaMode.ENABLE;
        }
        String string = this.mImsFramework.getString(i, GlobalSettingsConstants.Call.VOWIFI_5GSA_MODE, "ENABLE");
        if ("ENABLE".equals(string)) {
            boolean z = this.mImsFramework.getBoolean(i, GlobalSettingsConstants.Call.SUPPORT_VOWIFI_DEPRIORITIZE_NR5G, false);
            boolean z2 = this.mImsFramework.getBoolean(i, GlobalSettingsConstants.Call.SUPPORT_DISABLE_VOWIFI_5GSA, false);
            if (z) {
                string = "DEPRIORITIZE";
            } else if (!z2) {
                return ImsConstants.NrSaMode.ENABLE;
            } else {
                string = "DISABLE";
            }
        }
        boolean z3 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "EPDGHANDOVERENABLE", false);
        boolean z4 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "ONLYEPSFALLBACK", false);
        boolean z5 = ImsProfile.hasVolteService(imsProfile, 20) && ImsProfile.hasVolteService(imsProfile, 18) && ImsRegistry.getWfcEpdgManager().getNrInterworkingMode(i) != ImsConstants.NrInterworking.FULL_SUPPORT;
        boolean z6 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "NRPREFERREDMODE", false);
        boolean z7 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "NRSAMODE", true);
        IMSLog.i(LOG_TAG, i, "vowifi5gsaMode: " + string + ", hoEnable : " + z3 + ", onlyEpsFallback : " + z4 + ", needDisable5gsa : " + z5 + ", isNrPreferredMode: " + z6 + ", isNrSaMode : " + z7);
        if ("DEPRIORITIZE".equals(string)) {
            if (((z3 && z4) || z5) && (mno != Mno.ATT || (z6 && z7))) {
                return ImsConstants.NrSaMode.DEPRIORITIZE;
            }
        } else if (mno == Mno.TMOUS) {
            if (SemSystemProperties.get("ro.boot.hardware", "").contains("qcom")) {
                return ImsConstants.NrSaMode.ENABLE;
            }
            try {
                boolean booleanValue = ((Boolean) Class.forName("com.sec.internal.ims.core.handler.secims.ResipMediaHandler").getMethod("getSupportVowifiDisable5gsa", new Class[0]).invoke(this.mImsFramework.getHandlerFactory().getMediaHandler(), new Object[0])).booleanValue();
                IMSLog.i(LOG_TAG, i, "getSupportVowifiDisable5gsa : " + booleanValue);
                if (booleanValue) {
                    return ImsConstants.NrSaMode.DISABLE;
                }
            } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else if (((z3 && z4) || z5) && z6 && z7) {
            return ImsConstants.NrSaMode.DISABLE;
        }
        return ImsConstants.NrSaMode.ENABLE;
    }

    private boolean isCpSupportUac(int i) {
        SemTelephonyAdapter.CpUacType supportUacType = SemTelephonyAdapter.getSupportUacType(i);
        return supportUacType == SemTelephonyAdapter.CpUacType.CP_UAC_SUPPORT_REL15 || supportUacType == SemTelephonyAdapter.CpUacType.CP_UAC_SUPPORT_REL16;
    }

    private void filterUacSipListByCpUacType(int i, List<String> list) {
        SemTelephonyAdapter.CpUacType supportUacType = SemTelephonyAdapter.getSupportUacType(i);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "filterUacSipListByCpUacType : modem support type " + supportUacType.toString());
        if (supportUacType == SemTelephonyAdapter.CpUacType.CP_UAC_NOT_SUPPORT) {
            list.clear();
        } else if (supportUacType == SemTelephonyAdapter.CpUacType.CP_UAC_SUPPORT_REL15) {
            list.remove("REGISTER");
        }
    }
}
