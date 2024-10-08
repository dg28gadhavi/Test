package com.sec.internal.ims.rcs;

import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.config.RcsConfig;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsServiceConnector;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.RegiConfig;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.core.RcsRegistration;
import com.sec.internal.ims.core.handler.secims.ResipRegistrationManager$$ExternalSyntheticLambda0;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.strategy.DefaultRCSMnoStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.MnoStrategyCreator;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RcsPolicyManager extends Handler implements IRcsPolicyManager {
    private static final int EVENT_IMS_GLOBAL_SETTINGS_CHANGED = 10;
    private static final int EVENT_IMS_SWITCHES_CHANGED = 11;
    private static final int EVENT_RCS_ALLOWED_CHANGED = 9;
    private static final int EVENT_RCS_ROAMING_PREF = 8;
    private static final int EVT_SIM_READY = 0;
    private static final int EVT_SIM_REFRESH = 3;
    private static Map<Integer, IMnoStrategy> mRcsStrategy = new ConcurrentHashMap();
    private static PhoneIdKeyMap<RegiConfig> mRegiConfig;
    /* access modifiers changed from: private */
    public static UriMatcher sUriMatcher;
    protected final Context context;
    private ContentObserver mRcsContentObserver = new ContentObserver(this) {
        public void onChange(boolean z, Uri uri) {
            int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
            if (uri.getFragment() != null) {
                activeDataPhoneId = UriUtil.getSimSlotFromUri(uri);
            }
            int match = RcsPolicyManager.sUriMatcher.match(uri);
            IMSLog.i(IRcsPolicyManager.LOG_TAG, activeDataPhoneId, "onChange: match: " + match);
            switch (match) {
                case 8:
                    RcsPolicyManager.this.onRcsRoamingPrefChanged(activeDataPhoneId);
                    return;
                case 9:
                    RcsPolicyManager.this.onRCSAllowedChangedbyMDM();
                    return;
                case 10:
                case 11:
                    RcsPolicyManager.this.updateRcsStrategy(activeDataPhoneId);
                    return;
                default:
                    return;
            }
        }
    };
    private IRegistrationManager mRegMgr;
    private List<ISimManager> mSimManagers;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        ImsConstants.SystemSettings.addUri(uriMatcher, ImsConstants.SystemSettings.RCS_ROAMING_PREF, 8);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.RCS_ALLOWED_URI, 9);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_GLOBAL, 10);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_SWITCHES, 11);
    }

    public RcsPolicyManager(Looper looper, Context context2, List list) {
        super(looper);
        this.context = context2;
        this.mSimManagers = list;
        mRegiConfig = new PhoneIdKeyMap<>(SimUtil.getPhoneCount(), null);
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            mRegiConfig.put(i, new RegiConfig(i, context2));
        }
    }

    public void initSequentially() {
        for (ISimManager next : this.mSimManagers) {
            next.registerForSimReady(this, 0, (Object) null);
            next.registerForSimRefresh(this, 3, (Object) null);
        }
        this.context.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.RCS_ROAMING_PREF.getUri(), false, this.mRcsContentObserver);
        this.context.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.IMS_GLOBAL.getUri(), true, this.mRcsContentObserver);
        this.context.getContentResolver().registerContentObserver(ImsConstants.SystemSettings.IMS_SWITCHES.getUri(), true, this.mRcsContentObserver);
        mRcsStrategy.clear();
        for (ISimManager next2 : this.mSimManagers) {
            if (next2 != null) {
                mRcsStrategy.put(Integer.valueOf(next2.getSimSlotIndex()), new DefaultRCSMnoStrategy(this.context, next2.getSimSlotIndex()));
            }
        }
    }

    public void setRegistrationManager(IRegistrationManager iRegistrationManager) {
        this.mRegMgr = iRegistrationManager;
    }

    public void handleMessage(Message message) {
        Log.i(IRcsPolicyManager.LOG_TAG, "handleMessage:" + message.what);
        int i = message.what;
        if (i == 0 || i == 3) {
            updateRcsStrategy(((Integer) ((AsyncResult) message.obj).result).intValue());
        }
    }

    /* access modifiers changed from: private */
    public void updateRcsStrategy(int i) {
        IMSLog.i(IRcsPolicyManager.LOG_TAG, i, "updateRcsStrategy");
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager != null) {
            mRcsStrategy.put(Integer.valueOf(i), MnoStrategyCreator.makeInstance(iSimManager.getSimMno(), i, this.context));
        }
    }

    public static IMnoStrategy getRcsStrategy(int i) {
        return mRcsStrategy.get(Integer.valueOf(i));
    }

    public static boolean loadRcsSettings(int i, boolean z) {
        IMnoStrategy iMnoStrategy = mRcsStrategy.get(Integer.valueOf(i));
        if (iMnoStrategy == null) {
            return false;
        }
        return iMnoStrategy.loadRcsSettings(z);
    }

    public boolean pendingRcsRegister(IRegisterTask iRegisterTask, List<IRegisterTask> list, int i) {
        IMSLog.i(IRcsPolicyManager.LOG_TAG, i, "pendingRcsRegister: mActiveDataPhoneId = " + SimUtil.getActiveDataPhoneId());
        if ((ConfigUtil.isRcsEur(iRegisterTask.getMno()) || ConfigUtil.isRcsChn(iRegisterTask.getMno())) && iRegisterTask.isRcsOnly() && !RcsUtils.DualRcs.isDualRcsReg() && i != SimUtil.getActiveDataPhoneId()) {
            return true;
        }
        if ((ConfigUtil.isRcsChn(iRegisterTask.getMno()) || iRegisterTask.getMno().isKor()) && iRegisterTask.isRcsOnly() && isWaitingRcsDeregister(iRegisterTask, list, iRegisterTask.getPhoneId())) {
            return true;
        }
        return false;
    }

    private boolean isWaitingRcsDeregister(IRegisterTask iRegisterTask, List<IRegisterTask> list, int i) {
        for (IRegisterTask next : list) {
            if (next != iRegisterTask && next.isRcsOnly() && next.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                Log.i(IRcsPolicyManager.LOG_TAG, "isWaitingRcsDeregister: " + next.getProfile().getName() + "(" + next.getState() + ")");
                return true;
            }
        }
        return false;
    }

    public String getRcsPcscfAddress(ImsProfile imsProfile, int i) {
        String substring;
        IMSLog.d(IRcsPolicyManager.LOG_TAG, i, "getRcsPcscfAddress:");
        Bundle lboPcscfAddressAndIpType = mRegiConfig.get(i).getLboPcscfAddressAndIpType();
        String string = lboPcscfAddressAndIpType.getString("address");
        String string2 = lboPcscfAddressAndIpType.getString(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE);
        if (string != null) {
            int indexOf = string.indexOf(58);
            if (("ipv4".equalsIgnoreCase(string2) || "IP Address".equalsIgnoreCase(string2) || "FQDN".equalsIgnoreCase(string2)) && indexOf > 0) {
                imsProfile.setSipPort(Integer.parseInt(string.substring(indexOf + 1)));
                substring = string.substring(0, indexOf);
            } else {
                if ("ipv6".equalsIgnoreCase(string2)) {
                    int indexOf2 = string.indexOf(91);
                    int indexOf3 = string.indexOf(93);
                    int indexOf4 = string.indexOf("]:");
                    if (indexOf4 > 0) {
                        imsProfile.setSipPort(Integer.parseInt(string.substring(indexOf4 + 2)));
                    }
                    if (indexOf2 == 0 && indexOf3 > 0) {
                        substring = string.substring(indexOf2 + 1, indexOf3);
                    }
                }
                IMSLog.i(IRcsPolicyManager.LOG_TAG, i, "getPcscfAddresses: LBO-PCSCF=" + string + " port=" + imsProfile.getSipPort());
            }
            string = substring;
            IMSLog.i(IRcsPolicyManager.LOG_TAG, i, "getPcscfAddresses: LBO-PCSCF=" + string + " port=" + imsProfile.getSipPort());
        }
        return string;
    }

    public String selectRcsDnsType(IRegisterTask iRegisterTask, List<String> list) {
        ImsProfile profile = iRegisterTask.getProfile();
        String acsServerType = ConfigUtil.getAcsServerType(iRegisterTask.getPhoneId());
        if ((profile.getNeedIpv4Dns() || ImsConstants.RCS_AS.JIBE.equals(acsServerType)) && list != null) {
            for (String isIPv4Address : list) {
                if (NetworkUtil.isIPv4Address(isIPv4Address)) {
                    return "IPV4";
                }
            }
        }
        return "";
    }

    public String selectRcsTransportType(IRegisterTask iRegisterTask, String str) {
        if (!iRegisterTask.isRcsOnly()) {
            return str;
        }
        String rcsTransport = getRcsTransport(this.context, iRegisterTask.getPdnType(), iRegisterTask.getProfile(), iRegisterTask.getPhoneId());
        if ("udp-preferred".equals(rcsTransport)) {
            rcsTransport = "udp";
        }
        return rcsTransport.toUpperCase();
    }

    public String getRcsPrivateUserIdentity(String str, ImsProfile imsProfile, int i) {
        if (Mno.fromName(imsProfile.getMnoName()).isKor()) {
            return str;
        }
        IMSLog.i(IRcsPolicyManager.LOG_TAG, i, "RCS only");
        String privateUserIdentity = mRegiConfig.get(i).getPrivateUserIdentity();
        if (privateUserIdentity == null) {
            return str;
        }
        IMSLog.s(IRcsPolicyManager.LOG_TAG, i, "impi: " + privateUserIdentity);
        return privateUserIdentity;
    }

    public String getRcsPublicUserIdentity(int i) {
        return mRegiConfig.get(i).getPublicUserIdentity();
    }

    public String getRcsHomeNetworkDomain(ImsProfile imsProfile, int i) {
        NetworkEvent networkEvent;
        Mno fromName = Mno.fromName(imsProfile.getMnoName());
        IConfigModule configModule = ImsRegistry.getConfigModule();
        String acsServerType = ConfigUtil.getAcsServerType(i);
        boolean z = (fromName == Mno.ATT && !ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(acsServerType)) || (ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(acsServerType) && !ImsProfile.hasRcsService(imsProfile));
        if (fromName == Mno.RJIL && (networkEvent = this.mRegMgr.getNetworkEvent(i)) != null) {
            z = ImsProfile.hasVolteService(imsProfile, networkEvent.network);
        }
        if (z || !imsProfile.getNeedAutoconfig() || !configModule.isValidAcsVersion(i)) {
            return "";
        }
        String homeNetworkDomain = mRegiConfig.get(i).getHomeNetworkDomain();
        IMSLog.d(IRcsPolicyManager.LOG_TAG, i, "Config Domain():" + homeNetworkDomain);
        return homeNetworkDomain;
    }

    public boolean isRcsRoamingPref(IRegisterTask iRegisterTask, boolean z) {
        int userConfig = UserConfiguration.getUserConfig(this.context, iRegisterTask.getPhoneId(), "rcs_roaming_pref", 2);
        Mno mno = iRegisterTask.getMno();
        NetworkEvent networkEvent = this.mRegMgr.getNetworkEvent(iRegisterTask.getPhoneId());
        if (networkEvent == null || ImsProfile.hasVolteService(iRegisterTask.getProfile(), networkEvent.network) || !ConfigUtil.isRcsEur(mno) || !z || userConfig != 0) {
            return true;
        }
        IMSLog.i(IRcsPolicyManager.LOG_TAG, iRegisterTask.getPhoneId(), "not allowed as per RCS preference");
        return false;
    }

    public void updateDualRcsPcscfIp(IRegisterTask iRegisterTask, List<String> list) {
        if (iRegisterTask.isRcsOnly() && RcsUtils.DualRcs.isDualRcsReg()) {
            String currentPcscfIp = iRegisterTask.getGovernor().getCurrentPcscfIp();
            boolean checkDualRcsPcscfIp = checkDualRcsPcscfIp(iRegisterTask);
            int phoneId = iRegisterTask.getPhoneId();
            IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "checkDualRcsPcscf: " + checkDualRcsPcscfIp + ", curPcscfIp: " + currentPcscfIp);
            if (checkDualRcsPcscfIp) {
                iRegisterTask.getGovernor().increasePcscfIdx();
                if (list != null) {
                    iRegisterTask.getGovernor().updatePcscfIpList(list);
                }
            }
        }
    }

    public ImsUri.UriType getRcsNetworkUriType(int i, String str, boolean z) {
        return RcsConfigurationHelper.getNetworkUriType(this.context, str, z, i);
    }

    private boolean checkDualRcsPcscfIp(IRegisterTask iRegisterTask) {
        String currentPcscfIp = iRegisterTask.getGovernor().getCurrentPcscfIp();
        int oppositeSimSlot = SimUtil.getOppositeSimSlot(iRegisterTask.getPhoneId());
        List<IRegisterTask> pendingRegistration = this.mRegMgr.getPendingRegistration(oppositeSimSlot);
        if (pendingRegistration == null) {
            return false;
        }
        for (IRegisterTask next : pendingRegistration) {
            if (next.isRcsOnly() && ((next.getState() == RegistrationConstants.RegisterTaskState.REGISTERED || next.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) && RcsUtils.UiUtils.isSameRcsOperator(iRegisterTask.getProfile(), next.getProfile()))) {
                String currentPcscfIp2 = next.getGovernor().getCurrentPcscfIp();
                IMSLog.i(IRcsPolicyManager.LOG_TAG, oppositeSimSlot, "checkDualRcsPcscfIp: pcscf: " + currentPcscfIp2);
                if (currentPcscfIp.equals(currentPcscfIp2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String changeRcsIfacename(IRegisterTask iRegisterTask, IPdnController iPdnController, String str) {
        NetworkInterface byInetAddress;
        try {
            if (NetworkUtil.isIPv4Address(str)) {
                LinkPropertiesWrapper linkProperties = iPdnController.getLinkProperties(iRegisterTask);
                if (linkProperties == null) {
                    Log.i(IRcsPolicyManager.LOG_TAG, "changeIfacename: LinkPropertiesWrapper null");
                    return null;
                }
                List<InetAddress> allAddresses = linkProperties.getAllAddresses();
                if (allAddresses != null && !allAddresses.isEmpty()) {
                    for (InetAddress next : allAddresses) {
                        if (NetworkUtil.isIPv4Address(next.getHostAddress()) && (byInetAddress = NetworkInterface.getByInetAddress(next)) != null) {
                            String name = byInetAddress.getName();
                            Log.i(IRcsPolicyManager.LOG_TAG, "register: Change iface = " + name);
                            return name;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.d(IRcsPolicyManager.LOG_TAG, e.getMessage());
        }
        String interfaceName = iPdnController.getInterfaceName(iRegisterTask);
        Log.i(IRcsPolicyManager.LOG_TAG, "register: changeIfacename : no change - " + interfaceName);
        return interfaceName;
    }

    public Bundle getRcsConfigForUserAgent(ImsProfile imsProfile, Mno mno, int i, int i2, ImConfig imConfig, RcsRegistration.Builder builder) {
        String str;
        String str2;
        String str3;
        String str4;
        boolean z;
        ImsProfile imsProfile2 = imsProfile;
        int i3 = i;
        int i4 = i2;
        Bundle bundle = new Bundle();
        String str5 = "";
        String transportName = imsProfile.getTransportName();
        synchronized (IRcsPolicyManager.class) {
            String str6 = ((ImConstants.ImMsgTech) Optional.ofNullable(imConfig).map(new ResipRegistrationManager$$ExternalSyntheticLambda0()).orElse(ImConstants.ImMsgTech.SIMPLE_IM)).toString();
            Integer valueOf = Integer.valueOf(imsProfile.getQValue());
            String acsServerType = ConfigUtil.getAcsServerType(i2);
            RegiConfig regiConfig = mRegiConfig.get(i4);
            if (TextUtils.isEmpty(imsProfile.getPassword())) {
                str = regiConfig.getAppUserPwd(imsProfile.getPassword());
                builder.setPassword(str);
                if (ImsConstants.RCS_AS.JIBE.equals(acsServerType) || ImsConstants.RCS_AS.SEC.equals(acsServerType) || ImsConstants.RCS_AS.INTEROP.equals(acsServerType)) {
                    str = ConfigUtil.decryptParam(str, imsProfile.getPassword());
                }
                IMSLog.s(IRcsPolicyManager.LOG_TAG, i4, "getRcsConfigForUserAgent: Rcs Config password=" + str);
            } else {
                str = imsProfile.getPassword();
                IMSLog.s(IRcsPolicyManager.LOG_TAG, i4, "getRcsConfigForUserAgent: profile password=" + str);
            }
            if (mno != Mno.RJIL || ConfigUtil.isRcsOnly(imsProfile)) {
                str5 = regiConfig.getAppRealm();
            }
            if (i3 == 1) {
                str3 = regiConfig.getTransportWifiMedia();
                str2 = transportName;
            } else {
                str2 = transportName;
                str3 = (!ImsProfile.isRcsUpProfile(ConfigUtil.getRcsProfileWithFeature(this.context, i4, imsProfile2)) || !((TelephonyManager) this.context.getSystemService(PhoneConstants.PHONE_KEY)).isNetworkRoaming() || ImsConstants.RCS_AS.JIBE.equals(acsServerType)) ? regiConfig.getTransportPsMedia() : regiConfig.getTransportPsMediaRoaming();
            }
            IMSLog.s(IRcsPolicyManager.LOG_TAG, i4, "msrpTransType=(" + str3 + ")");
            if (!ImsProfile.hasVolteService(imsProfile)) {
                str4 = getRcsTransport(this.context, i3, imsProfile2, i4);
                z = regiConfig.getKeepAlive();
            } else {
                z = false;
                str4 = str2;
            }
            if (ConfigUtil.getAutoconfigSourceWithFeature(i4, 0) == 0 && ConfigUtil.isRcsChn(mno)) {
                str6 = "CPM";
            }
            int timer1 = imsProfile.getTimer1();
            int timer2 = imsProfile.getTimer2();
            int timer4 = imsProfile.getTimer4();
            if (ConfigUtil.isRcsOnly(imsProfile)) {
                String qValue = regiConfig.getQValue();
                if (!TextUtils.isEmpty(qValue)) {
                    try {
                        valueOf = Integer.valueOf(Float.valueOf(Float.parseFloat(qValue) * 1000.0f).intValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                timer1 = regiConfig.getTimer1(imsProfile.getTimer1());
                timer2 = regiConfig.getTimer2(imsProfile.getTimer2());
                timer4 = regiConfig.getTimer4(imsProfile.getTimer4());
            }
            int regRetryBaseTime = regiConfig.getRegRetryBaseTime(imsProfile.getRegRetryBaseTime());
            int regRetryMaxTime = regiConfig.getRegRetryMaxTime(imsProfile.getRegRetryMaxTime());
            bundle.putString("password", str);
            bundle.putString("realm", str5);
            bundle.putString("msrpTransType", str3);
            bundle.putString("transport", str4);
            bundle.putString(ConfigConstants.ConfigTable.IM_IM_MSG_TECH, str6);
            bundle.putBoolean("useKeepAlive", z);
            bundle.putInt("qVal", valueOf.intValue());
            bundle.putInt(ConfigConstants.ConfigTable.TIMER_T1, timer1);
            bundle.putInt(ConfigConstants.ConfigTable.TIMER_T2, timer2);
            bundle.putInt(ConfigConstants.ConfigTable.TIMER_T4, timer4);
            bundle.putInt(ConfigConstants.ConfigTable.REG_RETRY_BASE_TIME, regRetryBaseTime);
            bundle.putInt(ConfigConstants.ConfigTable.REG_RETRY_MAX_TIME, regRetryMaxTime);
        }
        return bundle;
    }

    /* access modifiers changed from: private */
    public void onRcsRoamingPrefChanged(int i) {
        int userConfig = UserConfiguration.getUserConfig(this.context, i, "rcs_roaming_pref", 2);
        Log.i(IRcsPolicyManager.LOG_TAG, "onRcsRoamingPrefChanged: now [" + userConfig + "]");
        this.mRegMgr.notifyRomaingSettingsChanged(userConfig, i);
    }

    /* access modifiers changed from: private */
    public void onRCSAllowedChangedbyMDM() {
        this.mRegMgr.notifyRCSAllowedChangedbyMDM();
    }

    public boolean tryRcsConfig(IRegisterTask iRegisterTask) {
        IConfigModule configModule = ImsRegistry.getConfigModule();
        if (!configModule.tryAutoconfiguration(iRegisterTask)) {
            return false;
        }
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "tryRcsConfig for task : " + iRegisterTask.getProfile().getName());
        if (configModule.getAcsTryReason(iRegisterTask.getPhoneId()) != DiagnosisConstants.RCSA_ATRE.INIT) {
            return true;
        }
        configModule.setAcsTryReason(iRegisterTask.getPhoneId(), DiagnosisConstants.RCSA_ATRE.FROM_REGI);
        return true;
    }

    public boolean doRcsConfig(IRegisterTask iRegisterTask, List<IRegisterTask> list) {
        IConfigModule configModule = ImsRegistry.getConfigModule();
        if (!configModule.isWaitAutoconfig(iRegisterTask)) {
            return false;
        }
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(IRcsPolicyManager.LOG_TAG, phoneId, "doRcsConfig for task : " + iRegisterTask.getProfile().getName());
        if (configModule.getAcsTryReason(iRegisterTask.getPhoneId()) == DiagnosisConstants.RCSA_ATRE.INIT) {
            configModule.setAcsTryReason(iRegisterTask.getPhoneId(), DiagnosisConstants.RCSA_ATRE.FROM_REGI);
        }
        configModule.triggerAutoConfig(false, iRegisterTask.getPhoneId(), list);
        return true;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007b, code lost:
        if (r1.equals("SIPoTLS") == false) goto L_0x0068;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getRcsTransport(android.content.Context r5, int r6, com.sec.ims.settings.ImsProfile r7, int r8) {
        /*
            r4 = this;
            com.sec.internal.helper.PhoneIdKeyMap<com.sec.internal.helper.RegiConfig> r4 = mRegiConfig
            java.lang.Object r4 = r4.get(r8)
            com.sec.internal.helper.RegiConfig r4 = (com.sec.internal.helper.RegiConfig) r4
            r0 = 1
            if (r6 == r0) goto L_0x0059
            boolean r6 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isDualRcsReg()
            if (r6 == 0) goto L_0x0018
            boolean r6 = com.sec.internal.helper.SimUtil.isDdsSimSlot(r8)
            if (r6 != 0) goto L_0x0018
            goto L_0x0059
        L_0x0018:
            java.lang.String r6 = "phone"
            java.lang.Object r6 = r5.getSystemService(r6)
            android.telephony.TelephonyManager r6 = (android.telephony.TelephonyManager) r6
            java.lang.String r1 = r4.getTransProtoPsSignaling()
            com.sec.internal.constants.Mno r2 = com.sec.internal.helper.SimUtil.getSimMno(r8)
            java.lang.String r3 = com.sec.internal.ims.util.ConfigUtil.getAcsServerType(r8)
            java.lang.String r5 = com.sec.internal.ims.util.ConfigUtil.getRcsProfileWithFeature(r5, r8, r7)
            boolean r5 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r5)
            if (r5 == 0) goto L_0x005d
            boolean r5 = r6.isNetworkRoaming()
            if (r5 == 0) goto L_0x005d
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.SPRINT
            if (r2 == r5) goto L_0x005d
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.VZW
            if (r2 == r5) goto L_0x005d
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.TCE
            if (r2 == r5) goto L_0x005d
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.ROGERS
            if (r2 == r5) goto L_0x005d
            java.lang.String r5 = "jibe"
            boolean r5 = r5.equals(r3)
            if (r5 != 0) goto L_0x005d
            java.lang.String r1 = r4.getTransProtoPsRoamSignaling()
            goto L_0x005d
        L_0x0059:
            java.lang.String r1 = r4.getTransProtoWifiSignaling()
        L_0x005d:
            r1.hashCode()
            int r4 = r1.hashCode()
            r5 = -1
            switch(r4) {
                case -1479406420: goto L_0x007e;
                case -1479406138: goto L_0x0075;
                case -1479405428: goto L_0x006a;
                default: goto L_0x0068;
            }
        L_0x0068:
            r0 = r5
            goto L_0x0088
        L_0x006a:
            java.lang.String r4 = "SIPoUDP"
            boolean r4 = r1.equals(r4)
            if (r4 != 0) goto L_0x0073
            goto L_0x0068
        L_0x0073:
            r0 = 2
            goto L_0x0088
        L_0x0075:
            java.lang.String r4 = "SIPoTLS"
            boolean r4 = r1.equals(r4)
            if (r4 != 0) goto L_0x0088
            goto L_0x0068
        L_0x007e:
            java.lang.String r4 = "SIPoTCP"
            boolean r4 = r1.equals(r4)
            if (r4 != 0) goto L_0x0087
            goto L_0x0068
        L_0x0087:
            r0 = 0
        L_0x0088:
            switch(r0) {
                case 0: goto L_0x0098;
                case 1: goto L_0x0094;
                case 2: goto L_0x0090;
                default: goto L_0x008b;
            }
        L_0x008b:
            java.lang.String r4 = r7.getTransportName()
            return r4
        L_0x0090:
            java.lang.String r4 = "udp"
            return r4
        L_0x0094:
            java.lang.String r4 = "tls"
            return r4
        L_0x0098:
            java.lang.String r4 = "tcp"
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.rcs.RcsPolicyManager.getRcsTransport(android.content.Context, int, com.sec.ims.settings.ImsProfile, int):java.lang.String");
    }

    public static RcsConfig getRcsConfig(Context context2, ImsProfile imsProfile, int i, ImConfig imConfig) {
        String str;
        String str2;
        String str3;
        Context context3 = context2;
        int i2 = i;
        if (!RcsUtils.DualRcs.isRegAllowed(context3, i2) || imConfig == null) {
            return null;
        }
        int i3 = ImsRegistry.getInt(i2, GlobalSettingsConstants.RCS.FT_CHUNK_SIZE, 0);
        int i4 = ImsRegistry.getInt(i2, GlobalSettingsConstants.RCS.ISH_CHUNK_SIZE, 0);
        boolean z = ImsRegistry.getBoolean(i2, GlobalSettingsConstants.RCS.MSRP_CEMA, false);
        boolean z2 = ImsRegistry.getBoolean(i2, GlobalSettingsConstants.RCS.CONF_SUBSCRIBE_ENABLED, true);
        int i5 = ImsRegistry.getInt(i2, GlobalSettingsConstants.RCS.SUPPORT_CANCEL_MESSAGE, 0);
        int pagerModeLimit = imConfig.getPagerModeLimit();
        boolean z3 = ImsRegistry.getBoolean(i2, GlobalSettingsConstants.RCS.AGGR_IMDN_SUPPORTED, false);
        boolean realtimeUserAliasAuth = imConfig.getRealtimeUserAliasAuth();
        ImsUri confFactoryUri = imConfig.getConfFactoryUri();
        if (confFactoryUri != null) {
            str = confFactoryUri.toString();
        } else {
            str = "";
        }
        String fileDownloadPath = FilePathGenerator.getFileDownloadPath(context3, true);
        if (fileDownloadPath == null) {
            fileDownloadPath = "";
        }
        ImsUri exploderUri = imConfig.getExploderUri();
        if (exploderUri == null || "sip:foo@bar".equals(exploderUri.toString())) {
            str2 = "";
        } else {
            str2 = exploderUri.toString();
        }
        boolean z4 = ImsRegistry.getBoolean(i2, GlobalSettingsConstants.RCS.MSRP_DISCARD_PORT, false);
        boolean botPrivacyDisable = imsProfile.getRcsProfileType() >= ImsProfile.RCS_PROFILE.UP_2_2.ordinal() ? imConfig.getBotPrivacyDisable() : true;
        int ordinal = imConfig.getChatbotMsgTech() != null ? imConfig.getChatbotMsgTech().ordinal() : 1;
        String endUserConfReqId = mRegiConfig.get(i2).getEndUserConfReqId();
        String string = ImsRegistry.getString(i2, GlobalSettingsConstants.RCS.SUPPORTED_BOT_VERSIONS, "");
        if (ImsUtil.isSingleRegiAppConnected(i)) {
            String str4 = (String) new SecImsServiceConnector(context3).getSipTransportImpl(i2).getAllocatedFeatureTags().stream().filter(new RcsPolicyManager$$ExternalSyntheticLambda0()).findFirst().map(new RcsPolicyManager$$ExternalSyntheticLambda1()).map(new RcsPolicyManager$$ExternalSyntheticLambda2()).orElse("");
            if (!TextUtils.isEmpty(str4)) {
                IMSLog.i(IRcsPolicyManager.LOG_TAG, i2, "Replace botversion retrieving from SipDelegate: " + str4);
                string = str4;
            }
        }
        if (TextUtils.isEmpty(string)) {
            str3 = imsProfile.getRcsProfileType() >= ImsProfile.RCS_PROFILE.UP_2_4.ordinal() ? "#=1,#=2" : "#=1";
        } else {
            str3 = string;
        }
        return new RcsConfig(i3, i4, str, z, fileDownloadPath, z2, str2, pagerModeLimit, z4, z3, botPrivacyDisable, ordinal, endUserConfReqId, str3, i5, realtimeUserAliasAuth);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$getRcsConfig$1(String str) {
        return str.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR, 2)[1];
    }

    public void updateRegiConfig(int i) {
        mRegiConfig.get(i).load();
    }

    public RegiConfig getRegiConfig(int i) {
        return mRegiConfig.get(i);
    }
}
