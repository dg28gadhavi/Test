package com.sec.internal.ims.core;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.SemSystemProperties;
import android.telephony.CellInfo;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import com.sec.epdg.EpdgManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.core.PdnFailReason;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.NetworkState;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.RegistrationManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DmConfigModule;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class NetworkEventController {
    private static final int EPDG_EVENT_TIMER = 121000;
    static final String IMS_DM_START = "10";
    private static final String LOG_TAG = "RegiMgr-NetEvtCtr";
    protected ICmcAccountManager mCmcAccountManager;
    protected Context mContext;
    protected SimpleEventLog mEventLog;
    protected IImsFramework mImsFramework;
    protected List<String> mLastPcscfList;
    private int mNetType = 0;
    private boolean mNwChanged = false;
    protected PdnController mPdnController;
    protected IRcsPolicyManager mRcsPolicyManager;
    protected RegistrationManagerHandler mRegHandler;
    protected RegistrationManagerBase mRegMan;
    protected PendingIntent mRetryIntentOnPdnFail = null;
    protected List<ISimManager> mSimManagers;
    protected ITelephonyManager mTelephonyManager;
    protected IVolteServiceModule mVsm;
    private boolean mWiFi = false;

    private boolean isHandoverBetweenEpdgAndLeagacy(int i, int i2) {
        return (i == i2 || i2 == 0 || ((i != 18 || i2 == 13) && (i == 13 || i2 != 18))) ? false : true;
    }

    NetworkEventController(Context context) {
        this.mContext = context;
    }

    NetworkEventController(Context context, PdnController pdnController, ITelephonyManager iTelephonyManager, List<ISimManager> list, ICmcAccountManager iCmcAccountManager, IRcsPolicyManager iRcsPolicyManager, RegistrationManagerBase registrationManagerBase, IImsFramework iImsFramework) {
        this.mContext = context;
        this.mPdnController = pdnController;
        this.mTelephonyManager = iTelephonyManager;
        this.mSimManagers = list;
        this.mCmcAccountManager = iCmcAccountManager;
        this.mRcsPolicyManager = iRcsPolicyManager;
        this.mRegMan = registrationManagerBase;
        this.mImsFramework = iImsFramework;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 300);
    }

    public void setVolteServiceModule(IVolteServiceModule iVolteServiceModule) {
        this.mVsm = iVolteServiceModule;
    }

    public void setRegistrationHandler(RegistrationManagerHandler registrationManagerHandler) {
        this.mRegHandler = registrationManagerHandler;
    }

    public String getPcscfIpAddress(IRegisterTask iRegisterTask, String str) {
        if (iRegisterTask.getMno() == Mno.MTS_RUSSIA && iRegisterTask.isRcsOnly() && isNewPcscfListAvailable(iRegisterTask)) {
            iRegisterTask.getGovernor().resetPcscfList();
        }
        if (!iRegisterTask.getGovernor().hasValidPcscfIpList()) {
            List<String> retrievePcscfByProfileSettings = RegistrationUtils.retrievePcscfByProfileSettings(iRegisterTask, this.mPdnController, this.mRcsPolicyManager, this.mTelephonyManager.getIsimPcscf());
            this.mLastPcscfList = retrievePcscfByProfileSettings;
            if (CollectionUtils.isNullOrEmpty((Collection<?>) retrievePcscfByProfileSettings)) {
                return null;
            }
            List<String> checkValidPcscfIp = iRegisterTask.getGovernor().checkValidPcscfIp(lookupPcscfIfRequired(iRegisterTask, this.mLastPcscfList, str));
            if (CollectionUtils.isNullOrEmpty((Collection<?>) checkValidPcscfIp)) {
                return null;
            }
            iRegisterTask.getGovernor().updatePcscfIpList(checkValidPcscfIp);
            this.mRcsPolicyManager.updateDualRcsPcscfIp(iRegisterTask, checkValidPcscfIp);
            return iRegisterTask.getGovernor().getCurrentPcscfIp();
        }
        this.mRcsPolicyManager.updateDualRcsPcscfIp(iRegisterTask, (List<String>) null);
        return iRegisterTask.getGovernor().getCurrentPcscfIp();
    }

    /* access modifiers changed from: package-private */
    @SuppressLint({"NewApi"})
    public boolean isDomainPattern(String str) {
        return !TextUtils.isEmpty(str) && Patterns.DOMAIN_NAME.matcher(str).matches() && !InetAddresses.isNumericAddress(str);
    }

    private List<String> lookupPcscfIfRequired(IRegisterTask iRegisterTask, List<String> list, String str) {
        ArrayList arrayList = new ArrayList();
        Iterator<String> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String next = it.next();
            if (!NetworkUtil.isValidPcscfAddress(next)) {
                IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "getPcscfIpAddresses: pcscf is not valid... continue : " + next);
            } else if ((iRegisterTask.getMno().isKor() || iRegisterTask.getProfile().getPcscfPreference() == 0 || iRegisterTask.getProfile().getPcscfPreference() == 2) && !isDomainPattern(next)) {
                arrayList.add(next);
            } else {
                List<String> dnsServers = this.mPdnController.getDnsServers((PdnEventListener) iRegisterTask);
                iRegisterTask.setPcscfHostname(next);
                long networkHandle = iRegisterTask.getNetworkConnected() == null ? 0 : iRegisterTask.getNetworkConnected().getNetworkHandle();
                IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "netId: " + networkHandle);
                if (dnsServers != null && iRegisterTask.getProfile().getNeedNaptrDns() && (!ConfigUtil.isRcsChn(iRegisterTask.getMno()) || isDomainPattern(next))) {
                    String selectRcsTransportType = this.mRcsPolicyManager.selectRcsTransportType(iRegisterTask, "TLS");
                    if (!iRegisterTask.getProfile().getNeedIpv4Dns()) {
                        IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "not ipv4 dns");
                        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVING);
                        this.mRegMan.sendDnsQuery(iRegisterTask, str, next, dnsServers, "NAPTR", selectRcsTransportType, "", networkHandle);
                        break;
                    }
                    String selectRcsDnsType = this.mRcsPolicyManager.selectRcsDnsType(iRegisterTask, dnsServers);
                    if (selectRcsDnsType != null) {
                        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVING);
                        this.mRegMan.sendDnsQuery(iRegisterTask, str, next, dnsServers, "NAPTR", selectRcsTransportType, selectRcsDnsType, networkHandle);
                        break;
                    }
                }
                if (dnsServers == null || !SimUtil.isSoftphoneEnabled() || !this.mRegMan.getAresLookupRequired()) {
                    try {
                        int i = 0;
                        if (iRegisterTask.getProfile().getCmcType() != 0) {
                            InetAddress[] allByNameWithThread = RegistrationUtils.getAllByNameWithThread(iRegisterTask, next);
                            int length = allByNameWithThread.length;
                            while (i < length) {
                                arrayList.add(allByNameWithThread[i].getHostAddress());
                                i++;
                            }
                        } else if (!iRegisterTask.getMno().isKor()) {
                            iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVING);
                            RegistrationUtils.getHostAddressWithThread(this.mRegHandler, this.mRcsPolicyManager, iRegisterTask, next);
                        } else if (iRegisterTask.getNetworkConnected() != null) {
                            InetAddress[] allByName = iRegisterTask.getNetworkConnected().getAllByName(next);
                            int length2 = allByName.length;
                            while (i < length2) {
                                arrayList.add(allByName[i].getHostAddress());
                                i++;
                            }
                        } else {
                            InetAddress[] allByName2 = InetAddress.getAllByName(next);
                            int length3 = allByName2.length;
                            while (i < length3) {
                                arrayList.add(allByName2[i].getHostAddress());
                                i++;
                            }
                        }
                        iRegisterTask.setPcscfHostname(next);
                        if (iRegisterTask.getProfile().getCmcType() >= 3) {
                            iRegisterTask.setPcscfHostname(iRegisterTask.getProfile().getDomain());
                        }
                    } catch (UnknownHostException unused) {
                        IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "getPcscfIpAddresses: faild to resolve dns query... ");
                        this.mRegMan.setAresLookupRequired(true);
                        if (iRegisterTask.getMno() == Mno.KT) {
                            iRegisterTask.getGovernor().retryDNSQuery();
                        }
                        if (iRegisterTask.getProfile().getCmcType() != 0) {
                            IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "CMC dns query failed");
                            break;
                        }
                    }
                } else {
                    iRegisterTask.setPcscfHostname(next);
                    String str2 = "_sip._tls." + next;
                    iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVING);
                    ArrayList arrayList2 = new ArrayList();
                    for (String next2 : dnsServers) {
                        if (NetworkUtil.isIPv4Address(next2)) {
                            arrayList2.add(next2);
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "ATT SoftPhone : found ipv4 dns");
                        this.mRegMan.sendDnsQuery(iRegisterTask, str, str2, arrayList2, "SRV", "tcp", "IPV4", networkHandle);
                    } else {
                        this.mRegMan.sendDnsQuery(iRegisterTask, str, str2, dnsServers, "SRV", "tcp", "", networkHandle);
                    }
                }
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public void onEpdgConnected(int i) {
        this.mRegHandler.removeMessages(135);
        this.mRegMan.updatePani(i);
        IMSLog.i(LOG_TAG, i, "onEpdgConnected:");
        if (RegistrationUtils.getNetworkEvent(i) != null) {
            RegistrationUtils.getNetworkEvent(i).isEpdgConnected = true;
            SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
            if (pendingRegistrationInternal != null) {
                Iterator it = pendingRegistrationInternal.iterator();
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    if (registerTask.getProfile().getPdnType() == 11) {
                        IMSLog.i(LOG_TAG, i, "onEpdgConnected: " + registerTask.getState() + " mIsUpdateRegistering=" + registerTask.mIsUpdateRegistering + " task=" + registerTask.getProfile().getName() + " mno=" + registerTask.mMno);
                        registerTask.getGovernor().onEpdgConnected();
                        RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERED;
                        RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.REGISTERING;
                        if (registerTask.isOneOf(registerTaskState, registerTaskState2)) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("EPDG HO : L2");
                            sb.append(this.mPdnController.getEpdgPhysicalInterface(i) == 2 ? "C" : "W");
                            registerTask.setReason(sb.toString());
                            registerTask.setEpdgHandoverInProgress(true);
                            registerTask.setRegiRequestType(DiagnosisConstants.REGI_REQC.HAND_OVER);
                            this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.EPDG_CONNECTED, registerTask.getState() != registerTaskState2 && !registerTask.getGovernor().blockImmediatelyUpdateRegi());
                        } else {
                            this.mRegMan.tryRegister(i);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onEpdgDisconnected(int i) {
        this.mRegHandler.removeMessages(135);
        this.mRegMan.updatePani(i);
        IMSLog.i(LOG_TAG, i, "onEpdgDisconnected:");
        if (RegistrationUtils.getNetworkEvent(i) != null) {
            RegistrationUtils.getNetworkEvent(i).isEpdgConnected = false;
            SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
            if (pendingRegistrationInternal != null) {
                Iterator it = pendingRegistrationInternal.iterator();
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    if (registerTask.getProfile().getPdnType() == 11) {
                        IMSLog.i(LOG_TAG, i, "onEpdgDisconnected: " + registerTask.getState() + " mIsUpdateRegistering=" + registerTask.mIsUpdateRegistering + " task=" + registerTask.getProfile().getName() + " mno=" + registerTask.mMno);
                        IRegistrationGovernor governor = registerTask.getGovernor();
                        governor.onEpdgDisconnected();
                        RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERED;
                        RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.REGISTERING;
                        if (!registerTask.isOneOf(registerTaskState, registerTaskState2)) {
                            this.mRegMan.tryRegister(i);
                        } else if (!governor.checkEmergencyInProgress() || registerTask.getState() != registerTaskState) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("EPDG HO : ");
                            sb.append(this.mPdnController.getEpdgPhysicalInterface(i) == 2 ? "C" : "W");
                            sb.append("2L");
                            registerTask.setReason(sb.toString());
                            boolean z = true;
                            registerTask.setEpdgHandoverInProgress(true);
                            if (registerTask.getState() == registerTaskState2) {
                                z = false;
                            }
                            this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.EPDG_DISCONNECTED, z);
                        } else {
                            IMSLog.i(LOG_TAG, i, "onEpdgDisconnected: Skip re-registration due to Emergency registration");
                            RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(27, i, 0, (Object) null), 300);
                            return;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onEpdgDeregisterRequested(int i) {
        this.mRegMan.sendDeregister(124, i);
    }

    /* access modifiers changed from: protected */
    public void onPdnConnecting(int i, int i2) {
        SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new NetworkEventController$$ExternalSyntheticLambda0()).map(new NetworkEventController$$ExternalSyntheticLambda1()).forEach(new NetworkEventController$$ExternalSyntheticLambda2(i2));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onPdnConnecting$0(RegisterTask registerTask) {
        return registerTask.getPdnType() == 11;
    }

    /* access modifiers changed from: protected */
    public void onPdnConnected(RegisterTask registerTask) {
        boolean z;
        if (registerTask == null) {
            IMSLog.e(LOG_TAG, "task is null. Skip pdnConnected event");
            return;
        }
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        IMSLog.i(LOG_TAG, phoneId, "onPdnConnected: task=" + profile.getName() + " state=" + registerTask.getState());
        if (registerTask.getMno().isEur() && registerTask.getPdnType() == 11) {
            registerTask.getGovernor().releaseThrottle(6);
        }
        if (!registerTask.getGovernor().needPendingPdnConnected()) {
            if (registerTask.getMno().isChn() && profile.hasEmergencySupport()) {
                SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(phoneId);
                if (pendingRegistrationInternal != null) {
                    Iterator it = pendingRegistrationInternal.iterator();
                    while (true) {
                        if (it.hasNext()) {
                            if (((RegisterTask) it.next()).getProfile().hasEmergencySupport()) {
                                z = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                z = false;
                if (!z) {
                    IMSLog.i(LOG_TAG, phoneId, "onPdnConnected: Emergency task already removed");
                    return;
                }
            }
            if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING)) {
                registerTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
                registerTask.getGovernor().resetPdnFailureInfo();
                profile.setUicclessEmergency(false);
                if (profile.hasEmergencySupport()) {
                    boolean needEmergencyRegistration = needEmergencyRegistration(registerTask);
                    IMSLog.i(LOG_TAG, phoneId, "onPdnConnected: need emergency Registration: " + needEmergencyRegistration);
                    profile.setUicclessEmergency(needEmergencyRegistration ^ true);
                    this.mRegMan.removeE911RegiTimer();
                    if (this.mPdnController.isEmergencyEpdgConnected(phoneId)) {
                        registerTask.setRegistrationRat(18);
                    }
                }
                registerTask.getGovernor().onPdnConnected();
                this.mRegMan.tryRegister(registerTask);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isTaskHasSepecificPdnType(IRegisterTask iRegisterTask) {
        return (iRegisterTask.getProfile() == null || iRegisterTask.getProfile().getPdnType() == -1) ? false : true;
    }

    /* access modifiers changed from: protected */
    public void onPdnDisconnected(IRegisterTask iRegisterTask) {
        IMSLog.i(LOG_TAG, iRegisterTask.getPhoneId(), "onPdnDisconnected: " + iRegisterTask.getState());
        ISimManager iSimManager = this.mSimManagers.get(iRegisterTask.getPhoneId());
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(iRegisterTask.getPhoneId());
        if (pendingRegistrationInternal != null) {
            RegistrationConstants.RegisterTaskState state = iRegisterTask.getState();
            RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERED;
            if (state == registerTaskState) {
                iRegisterTask.setDeregiReason(2);
            }
            RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.REGISTERING;
            boolean z = true;
            if (iRegisterTask.isOneOf(registerTaskState2, registerTaskState)) {
                iRegisterTask.setReason("pdn disconnected - REGISTERED or REGISTERING");
                if (RegistrationUtils.isCmcProfile(iRegisterTask.getProfile()) && iRegisterTask.getState() == registerTaskState2) {
                    iRegisterTask.getGovernor().releaseThrottle(5);
                }
                if (!isTaskHasSepecificPdnType(iRegisterTask)) {
                    this.mRegMan.tryDeregisterInternal(iRegisterTask, true, false);
                    this.mRegMan.stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask);
                } else if ((iRegisterTask.getMno() != Mno.KDDI || !iRegisterTask.getProfile().hasEmergencySupport()) && (!iRegisterTask.getMno().isAus() || iSimManager == null || iSimManager.isSimLoaded())) {
                    this.mRegMan.tryDeregisterInternal(iRegisterTask, true, true);
                } else {
                    this.mRegMan.tryDeregisterInternal(iRegisterTask, true, false);
                    this.mRegMan.stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask);
                }
            } else if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY) {
                iRegisterTask.setReason("pdn disconnected - EMERGENCY");
                iRegisterTask.setDeregiReason(3);
                this.mRegMan.tryDeregisterInternal(iRegisterTask, true, false);
                this.mRegMan.stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask);
                pendingRegistrationInternal.remove(iRegisterTask);
                SlotBasedConfig.getInstance(iRegisterTask.getPhoneId()).removeExtendedProfile(iRegisterTask.getProfile().getId());
            } else if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                Log.i(LOG_TAG, "PDN disconnected received in DEREGISTERING state, send DEREGISTER_TIMEOUT event");
                this.mRegMan.stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask);
                if (this.mRegHandler.hasMessages(107, iRegisterTask)) {
                    this.mRegHandler.removeMessages(107, iRegisterTask);
                }
                RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(107, iRegisterTask));
            } else {
                if (isTaskHasSepecificPdnType(iRegisterTask)) {
                    if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                        z = false;
                    }
                    if ((iRegisterTask.getMno() == Mno.KDDI && iRegisterTask.getProfile().hasEmergencySupport()) || ((iRegisterTask.getMno().isChn() || iRegisterTask.getMno().isCanada()) && z && iRegisterTask.getPdnType() == 11)) {
                        this.mRegMan.stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask);
                        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    } else if (iRegisterTask.getState() != RegistrationConstants.RegisterTaskState.IDLE) {
                        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
                    }
                } else {
                    this.mRegMan.stopPdnConnectivity(iRegisterTask.getPdnType(), iRegisterTask);
                    iRegisterTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
                RegistrationManagerHandler registrationManagerHandler2 = this.mRegHandler;
                registrationManagerHandler2.sendMessageDelayed(registrationManagerHandler2.obtainMessage(2, Integer.valueOf(iRegisterTask.getPhoneId())), 1000);
                if (iRegisterTask.getMno() == Mno.VZW) {
                    iRegisterTask.getGovernor().releaseThrottle(5);
                }
            }
            if (iRegisterTask.getMno().isKor() && !iRegisterTask.isRcsOnly()) {
                iRegisterTask.getGovernor().releaseThrottle(5);
            }
            iRegisterTask.resetTaskOnPdnDisconnected();
            if (iRegisterTask.getMno().isKor()) {
                if (!iRegisterTask.isRcsOnly()) {
                    this.mRegMan.setOmadmState(iRegisterTask.getPhoneId(), RegistrationManager.OmadmConfigState.IDLE);
                    iRegisterTask.getGovernor().resetPcscfPreference();
                    iRegisterTask.getGovernor().resetIPSecAllow();
                }
                iRegisterTask.getGovernor().resetAllRetryFlow();
            }
            RegistrationManagerHandler registrationManagerHandler3 = this.mRegHandler;
            registrationManagerHandler3.sendMessage(registrationManagerHandler3.obtainMessage(136, iRegisterTask.getPhoneId(), 0, (Object) null));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needEmergencyRegistration(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        ISimManager iSimManager = this.mSimManagers.get(phoneId);
        if (iSimManager == null || iSimManager.hasNoSim() || iRegisterTask.getMno().isKor() || ImsUtil.needForceToUsePsE911(phoneId, iSimManager.hasNoSim())) {
            return false;
        }
        if (iRegisterTask.getMno() == Mno.USCC && !iSimManager.isISimDataValid()) {
            return false;
        }
        if (iRegisterTask.getMno().isAus() && NetworkUtil.is3gppPsVoiceNetwork(iRegisterTask.getRegistrationRat()) && iRegisterTask.getPdnType() == 15 && (this.mPdnController.getVoiceRegState(phoneId) == 2 || this.mPdnController.getVoiceRegState(phoneId) == 1)) {
            Log.i(LOG_TAG, "needEmergencyRegistration[AUS]: limited mode but has valid SIM. Try register.");
            return true;
        } else if (iRegisterTask.getMno().isCanada() && this.mPdnController.hasEmergencyServiceOnly(iRegisterTask.getPhoneId())) {
            Log.i(LOG_TAG, "needEmergencyRegistration: limited mode. Dont Skip for Canada.");
            return true;
        } else if (iRegisterTask.getMno() == Mno.DOCOMO || !this.mPdnController.hasEmergencyServiceOnly(iRegisterTask.getPhoneId())) {
            if (iRegisterTask.getMno() != Mno.VZW || (this.mTelephonyManager.validateMsisdn(SimUtil.getSubId(phoneId)) && !this.mRegMan.isSelfActivationRequired(phoneId))) {
                return true;
            }
            Log.i(LOG_TAG, "Get PCO 5. Skip emergency registration.");
            return false;
        } else if (iRegisterTask.getMno() == Mno.GCF && SlotBasedConfig.getInstance(phoneId).getEmcAttachAuth() == 1) {
            Log.i(LOG_TAG, "needEmergencyRegistration: limited mode. Dont Skip for GCF if EmcAttachAuth success");
            return true;
        } else {
            Log.i(LOG_TAG, "needEmergencyRegistration: limited mode. skip emergency registration.");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateRat(int i, int i2) {
        UriGeneratorFactory instance = UriGeneratorFactory.getInstance();
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i2);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                this.mRegMan.updateRat(registerTask, i);
                ImsRegistration imsRegistration = registerTask.mReg;
                if (imsRegistration != null) {
                    for (NameAddr uri : imsRegistration.getImpuList()) {
                        ImsUri uri2 = uri.getUri();
                        for (UriGenerator.URIServiceType uRIServiceType : UriGenerator.URIServiceType.values()) {
                            if (instance.contains(uri2, uRIServiceType)) {
                                instance.get(uri2, uRIServiceType).updateRat(i);
                            }
                        }
                    }
                }
            }
        }
    }

    public int getNetType() {
        return this.mNetType;
    }

    public boolean isWiFi() {
        return this.mWiFi;
    }

    public boolean isNwChanged() {
        return this.mNwChanged;
    }

    public void setNwChanged(boolean z) {
        this.mNwChanged = z;
    }

    /* access modifiers changed from: package-private */
    public void onNetworkChanged(int i, boolean z, int i2) {
        NetworkState networkState;
        boolean isSuspendedWhileIrat = SlotBasedConfig.getInstance(i2).isSuspendedWhileIrat();
        IMSLog.i(LOG_TAG, i2, "onNetworkChanged: suspendByIrat=" + isSuspendedWhileIrat);
        if (!isSuspendedWhileIrat) {
            if (this.mPdnController.isNeedCellLocationUpdate() && (networkState = this.mPdnController.getNetworkState(i2)) != null) {
                networkState.setAllCellInfo((List<CellInfo>) null);
            }
            this.mRegMan.updatePani(i2);
            updateRat(i, i2);
            notifyNetworkEvent(i, z, i2);
            return;
        }
        this.mNetType = i;
        this.mWiFi = z;
        this.mNwChanged = true;
    }

    /* access modifiers changed from: package-private */
    public void notifyNetworkEvent(int i, boolean z, int i2) {
        NetworkEvent networkEvent = RegistrationUtils.getNetworkEvent(i2);
        NetworkState networkState = this.mPdnController.getNetworkState(i2);
        if (networkEvent != null && networkState != null) {
            NetworkEvent buildNetworkEvent = NetworkEvent.buildNetworkEvent(i2, i, this.mTelephonyManager.getVoiceNetworkType(), this.mTelephonyManager.getCallState(), z, networkState.isEpdgConnected(), networkState.isEpdgAVailable(), networkEvent, networkState);
            if (!buildNetworkEvent.equalsIgnoreEpdg(networkEvent)) {
                onNetworkEventChanged(buildNetworkEvent, i2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reconnectPdn(RegisterTask registerTask) {
        if (!registerTask.getProfile().hasEmergencySupport()) {
            int findBestNetwork = this.mRegMan.findBestNetwork(registerTask.getPhoneId(), registerTask.getProfile(), registerTask.getGovernor());
            int selectPdnType = RegistrationUtils.selectPdnType(registerTask.getProfile(), findBestNetwork);
            int phoneId = registerTask.getPhoneId();
            NetworkEvent networkEvent = RegistrationUtils.getNetworkEvent(phoneId);
            if (networkEvent != null) {
                String networkTypeName = TelephonyManagerExt.getNetworkTypeName(networkEvent.network);
                if (findBestNetwork == 0 && !networkEvent.outOfService) {
                    IMSLog.i(LOG_TAG, phoneId, "Cancel ongoing PDN in " + networkTypeName);
                    this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                    if (registerTask.getMno() == Mno.VZW && NetworkUtil.is3gppLegacyNetwork(RegistrationUtils.getNetworkEvent(phoneId).network)) {
                        registerTask.getGovernor().releaseThrottle(5);
                    }
                    registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                } else if (selectPdnType != registerTask.getPdnType()) {
                    IMSLog.i(LOG_TAG, registerTask.getPhoneId(), "pdn type has been changed, reconnect.");
                    this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                    registerTask.setPdnType(selectPdnType);
                    registerTask.setState(RegistrationConstants.RegisterTaskState.CONNECTING);
                    if (registerTask.getGovernor().isMobilePreferredForRcs() && selectPdnType == 0) {
                        PdnController pdnController = this.mPdnController;
                        int translateNetworkBearer = pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer());
                        IMSLog.i(LOG_TAG, phoneId, "reconnectPdn startTimsTimer rcs pdn = " + selectPdnType);
                        if (translateNetworkBearer == 1) {
                            registerTask.mGovernor.stopTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
                            this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                        }
                        registerTask.mGovernor.startTimsTimer(RegistrationConstants.REASON_INTERNET_PDN_REQUEST);
                    }
                    this.mPdnController.startPdnConnectivity(selectPdnType, registerTask, RegistrationUtils.getPhoneIdForStartConnectivity(registerTask));
                    if (registerTask.getMno().isOneOf(Mno.VZW, Mno.CTCMO, Mno.CTC, Mno.ATT) || registerTask.getMno().isEmeasewaoce() || (registerTask.getMno().isKor() && !registerTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(registerTask.getProfile()))) {
                        registerTask.getGovernor().startTimsTimer(RegistrationConstants.REASON_IMS_PDN_REQUEST);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onRetryTimeExpired(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().getPdnType() == 11) {
                    PendingIntent pendingIntent = this.mRetryIntentOnPdnFail;
                    if (pendingIntent != null) {
                        AlarmTimer.stop(this.mContext, pendingIntent);
                        this.mRetryIntentOnPdnFail = null;
                    }
                    Log.i(LOG_TAG, "RetrySetupEventReceiver: release throttle pdn fail");
                    registerTask.getGovernor().releaseThrottle(4);
                    RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(2, Integer.valueOf(i)));
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0023, code lost:
        r3 = com.sec.internal.helper.SimUtil.getOppositeSimSlot(r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onPdnFailed(int r3, int r4, int r5) {
        /*
            r2 = this;
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r3)
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r0 = r0.getRegistrationTasks()
            java.util.stream.Stream r0 = r0.stream()
            com.sec.internal.ims.core.NetworkEventController$$ExternalSyntheticLambda5 r1 = new com.sec.internal.ims.core.NetworkEventController$$ExternalSyntheticLambda5
            r1.<init>()
            java.util.stream.Stream r0 = r0.filter(r1)
            com.sec.internal.ims.core.NetworkEventController$$ExternalSyntheticLambda6 r1 = new com.sec.internal.ims.core.NetworkEventController$$ExternalSyntheticLambda6
            r1.<init>(r2, r4, r3, r5)
            r0.forEach(r1)
            boolean r4 = com.sec.internal.helper.SimUtil.isDualIMS()
            if (r4 == 0) goto L_0x003c
            int r3 = com.sec.internal.helper.SimUtil.getOppositeSimSlot(r3)
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r4 = r2.mSimManagers
            java.lang.Object r4 = r4.get(r3)
            com.sec.internal.interfaces.ims.core.ISimManager r4 = (com.sec.internal.interfaces.ims.core.ISimManager) r4
            if (r4 == 0) goto L_0x003c
            boolean r4 = r4.isSimAvailable()
            if (r4 == 0) goto L_0x003c
            com.sec.internal.ims.core.RegistrationManagerHandler r2 = r2.mRegHandler
            r2.sendTryRegister(r3)
        L_0x003c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.NetworkEventController.onPdnFailed(int, int, int):void");
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onPdnFailed$2(RegisterTask registerTask) {
        return registerTask.getPdnType() == 11;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onPdnFailed$3(int i, int i2, int i3, RegisterTask registerTask) {
        PdnFailReason valueOf = PdnFailReason.valueOf(i);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i2, registerTask, "onPdnFailed: " + valueOf);
        IMSLog.c(LogClass.REGI_PDN_FAILED, i2 + ",PDN FAIL:" + valueOf);
        registerTask.getGovernor().onPdnRequestFailed(valueOf, i3);
        long retryTimeOnPdnFail = registerTask.getGovernor().getRetryTimeOnPdnFail();
        if (retryTimeOnPdnFail > 0) {
            PendingIntent pendingIntent = this.mRetryIntentOnPdnFail;
            if (pendingIntent != null) {
                AlarmTimer.stop(this.mContext, pendingIntent);
                this.mRetryIntentOnPdnFail = null;
            }
            Intent intent = new Intent(ImsConstants.Intents.ACTION_RETRYTIME_EXPIRED);
            intent.putExtra(ImsConstants.Intents.EXTRA_PHONE_ID, i2);
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
            this.mRetryIntentOnPdnFail = broadcast;
            AlarmTimer.start(this.mContext, broadcast, retryTimeOnPdnFail);
            registerTask.getGovernor().setRetryTimeOnPdnFail(-1);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasRetryIntentOnPdnFail() {
        return this.mRetryIntentOnPdnFail != null;
    }

    /* access modifiers changed from: package-private */
    public void onCheckUnprocessedOmadmConfig(int i) {
        if (this.mRegMan.getUnprocessedOmadmConfig(i)) {
            Log.i(LOG_TAG, "onCheckUnprocessedOmadmConfig<" + i + ">: triggerOmadmConfig");
            this.mRegMan.setOmadmState(i, RegistrationManager.OmadmConfigState.IDLE);
            triggerOmadmConfig(i);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDmConfigCompleted(int i, boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("onDmConfigCompleted<");
        sb.append(i);
        sb.append(">: ");
        sb.append(z ? "SUCCESS" : "TIMEOUT");
        Log.i(LOG_TAG, sb.toString());
        if (this.mRegMan.getUnprocessedOmadmConfig(i) && z) {
            this.mRegMan.setUnprocessedOmadmConfig(i, false);
        }
        this.mRegHandler.removeDmConfigTimeout(i);
        this.mRegMan.setOmadmState(i, RegistrationManager.OmadmConfigState.FINISHED);
        Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            ((RegisterTask) it.next()).getGovernor().checkProfileUpdateFromDM(true);
        }
        this.mRegMan.tryRegister();
    }

    /* access modifiers changed from: package-private */
    public void triggerOmadmConfig(int i) {
        Log.i(LOG_TAG, "triggerOmadmConfig<" + i + "> - mOmadmState : " + this.mRegMan.getOmadmState(i));
        RegistrationManager.OmadmConfigState omadmState = this.mRegMan.getOmadmState(i);
        RegistrationManager.OmadmConfigState omadmConfigState = RegistrationManager.OmadmConfigState.TRIGGERED;
        if (omadmState != omadmConfigState) {
            this.mRegMan.setUnprocessedOmadmConfig(i, true);
            this.mRegMan.setOmadmState(i, omadmConfigState);
            this.mRegHandler.sendDmConfigTimeout(i, getClass().getSimpleName());
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getMno() == Mno.SKT && !registerTask.isRcsOnly() && registerTask.getProfile().getCmcType() == 0) {
                    ImsProfile profile = registerTask.getProfile();
                    ArrayList arrayList = new ArrayList();
                    profile.setPcscfList(arrayList);
                    profile.setLboPcscfAddressList(arrayList);
                    profile.setLboPcscfPort(-1);
                    registerTask.setProfile(profile);
                    try {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_1/Address", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_2/Address", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_3/Address", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_4/Address", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_5/Address", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_6/Address", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_1/AddressType", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_2/AddressType", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_3/AddressType", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_4/AddressType", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_5/AddressType", "");
                        contentValues.put("./3GPP_IMS/LBO_P-CSCF_Address/LBO_P-CSCF_Address_6/AddressType", "");
                        contentValues.put(DmConfigModule.INTERNAL_KEY_PROCESS_NAME, "com.sec.imsservice");
                        this.mContext.getContentResolver().insert(UriUtil.buildUri(DmConfigModule.CONFIG_DM_PROVIDER, registerTask.getPhoneId()), contentValues);
                        break;
                    } catch (Exception e) {
                        SimpleEventLog simpleEventLog = this.mEventLog;
                        StringBuilder sb = new StringBuilder();
                        sb.append("triggerOmadmConfig : update failure - ");
                        sb.append(e.getMessage() != null ? e.getMessage() : "null");
                        simpleEventLog.logAndAdd(sb.toString());
                    }
                }
            }
            setOmaDmStateDB(i, this.mRegMan.getOmadmState(i));
        }
    }

    private void setOmaDmStateDB(int i, RegistrationManager.OmadmConfigState omadmConfigState) {
        if (SimUtil.getMno().isKor() && omadmConfigState == RegistrationManager.OmadmConfigState.TRIGGERED) {
            Log.i(LOG_TAG, "setOmaDmStateDB<" + i + ">: " + omadmConfigState);
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("dm_state", IMS_DM_START);
                contentValues.put("sim_slot_id", Integer.valueOf(i));
                this.mContext.getContentResolver().update(Uri.parse("content://com.ims.dm.ContentProvider/imsDmStart"), contentValues, (String) null, (String[]) null);
            } catch (Exception e) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                StringBuilder sb = new StringBuilder();
                sb.append("setOmaDmStateDB : update failure - ");
                sb.append(e.getMessage() != null ? e.getMessage() : "null");
                simpleEventLog.logAndAdd(sb.toString());
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0091, code lost:
        if (handleNetworkEventOnRegister(r15, r2, r14, r7) == false) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ac, code lost:
        if (handleNetworkEventBeforeRegister(r15, r2, r14, r7) == false) goto L_0x00ae;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onNetworkEventChanged(com.sec.internal.constants.ims.os.NetworkEvent r14, int r15) {
        /*
            r13 = this;
            com.sec.internal.constants.ims.os.NetworkEvent r7 = com.sec.internal.ims.core.RegistrationUtils.getNetworkEvent(r15)
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r0 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r15)
            if (r7 == 0) goto L_0x017c
            if (r0 != 0) goto L_0x000e
            goto L_0x017c
        L_0x000e:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onNetworkEventChanged:"
            r1.append(r2)
            java.lang.String r2 = r7.changedEvent(r14)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r8 = "RegiMgr-NetEvtCtr"
            com.sec.internal.log.IMSLog.i(r8, r15, r1)
            com.sec.internal.ims.core.SlotBasedConfig r1 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r15)
            r1.setNetworkEvent(r14)
            com.sec.internal.interfaces.ims.IImsFramework r1 = r13.mImsFramework
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r1 = r1.getServiceModuleManager()
            r1.notifyNetworkChanged(r14, r15)
            com.sec.internal.interfaces.ims.IImsFramework r1 = r13.mImsFramework
            com.sec.internal.interfaces.ims.aec.IAECModule r1 = r1.getAECModule()
            r1.onNetworkEventChanged(r7, r14, r15)
            java.util.Iterator r9 = r0.iterator()
            r10 = 0
            r0 = r10
            r1 = r0
        L_0x0048:
            boolean r2 = r9.hasNext()
            r3 = 1
            if (r2 == 0) goto L_0x00c0
            java.lang.Object r2 = r9.next()
            com.sec.internal.ims.core.RegisterTask r2 = (com.sec.internal.ims.core.RegisterTask) r2
            com.sec.internal.constants.Mno r4 = r2.getMno()
            boolean r4 = r4.isKor()
            if (r4 == 0) goto L_0x006a
            com.sec.internal.interfaces.ims.core.IRegistrationGovernor r0 = r2.getGovernor()
            boolean r0 = r0.isExistRetryTimer()
            r11 = r0
            r1 = r10
            goto L_0x006b
        L_0x006a:
            r11 = r0
        L_0x006b:
            r13.handleSsacOnNetworkEventChanged(r2, r15, r14, r7)
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r4 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[]{r0, r4}
            boolean r4 = r2.isOneOf(r4)
            if (r4 == 0) goto L_0x0094
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r2.getState()
            if (r4 != r0) goto L_0x008d
            com.sec.internal.constants.Mno r0 = r2.getMno()
            boolean r0 = r0.isKor()
            if (r0 == 0) goto L_0x008d
            r1 = r3
        L_0x008d:
            boolean r0 = r13.handleNetworkEventOnRegister(r15, r2, r14, r7)
            if (r0 != 0) goto L_0x00b3
            goto L_0x00ae
        L_0x0094:
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.IDLE
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r3 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURING
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONFIGURED
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTING
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r6 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.CONNECTED
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r0 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[]{r0, r3, r4, r5, r6}
            boolean r0 = r2.isOneOf(r0)
            if (r0 == 0) goto L_0x00b0
            boolean r0 = r13.handleNetworkEventBeforeRegister(r15, r2, r14, r7)
            if (r0 != 0) goto L_0x00b3
        L_0x00ae:
            r0 = r11
            goto L_0x0048
        L_0x00b0:
            r13.handleNetworkEventOnDeregistering(r15, r2, r14, r7)
        L_0x00b3:
            r12 = r1
            r0 = r13
            r1 = r15
            r3 = r14
            r4 = r7
            r5 = r11
            r6 = r12
            r0.handleNetworkEvent(r1, r2, r3, r4, r5, r6)
            r0 = r11
            r1 = r12
            goto L_0x0048
        L_0x00c0:
            r13.tryCmcRegisterOnNetworkEventChanged(r14, r7)
            boolean r0 = r14.outOfService
            if (r0 == 0) goto L_0x00dc
            java.lang.String r0 = "out of service."
            com.sec.internal.log.IMSLog.i(r8, r15, r0)
            r7.outOfService = r3
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r15)
            r0.setNetworkEvent(r7)
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r15)
            r0.setNotifiedImsNotAvailable(r10)
        L_0x00dc:
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r15)
            boolean r0 = r0.isNotifiedImsNotAvailable()
            if (r0 == 0) goto L_0x0122
            java.lang.String r0 = r14.operatorNumeric
            java.lang.String r1 = r7.operatorNumeric
            boolean r0 = android.text.TextUtils.equals(r0, r1)
            if (r0 == 0) goto L_0x00fa
            int r0 = r7.network
            int r1 = r14.network
            if (r0 == r1) goto L_0x0122
            r0 = 20
            if (r1 != r0) goto L_0x0122
        L_0x00fa:
            com.sec.internal.helper.SimpleEventLog r0 = r13.mEventLog
            java.util.Locale r1 = java.util.Locale.US
            java.lang.String r2 = r7.operatorNumeric
            java.lang.String r3 = r14.operatorNumeric
            int r4 = r7.network
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            int r5 = r14.network
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            java.lang.Object[] r2 = new java.lang.Object[]{r2, r3, r4, r5}
            java.lang.String r3 = "Reset notifiedImsNotAvailable: operator [%s] => [%s], network [%s] => [%s]"
            java.lang.String r1 = java.lang.String.format(r1, r3, r2)
            r0.logAndAdd(r15, r1)
            com.sec.internal.ims.core.SlotBasedConfig r0 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r15)
            r0.setNotifiedImsNotAvailable(r10)
        L_0x0122:
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r0 = r13.mSimManagers
            java.lang.Object r0 = r0.get(r15)
            com.sec.internal.interfaces.ims.core.ISimManager r0 = (com.sec.internal.interfaces.ims.core.ISimManager) r0
            if (r0 != 0) goto L_0x012d
            return
        L_0x012d:
            boolean r1 = r0.isSimAvailable()
            r13.updateUtOnNetworkEventChanged(r15, r1, r14, r7)
            com.sec.internal.constants.Mno r14 = r0.getSimMno()
            boolean r14 = r14.isKor()
            if (r14 != 0) goto L_0x0149
            java.lang.String r14 = "onNetworkEventChanged: tryRegister by phoneID"
            com.sec.internal.log.IMSLog.i(r8, r15, r14)
            com.sec.internal.ims.core.RegistrationManagerBase r14 = r13.mRegMan
            r14.tryRegister((int) r15)
            goto L_0x016d
        L_0x0149:
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r14 = r13.mCmcAccountManager
            com.sec.internal.interfaces.ims.core.IRegisterTask r14 = r14.getCmcRegisterTask(r15)
            com.sec.internal.ims.core.RegisterTask r14 = (com.sec.internal.ims.core.RegisterTask) r14
            if (r14 == 0) goto L_0x016d
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r0 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r1 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r2 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.DEREGISTERING
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState[] r0 = new com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState[]{r0, r1, r2}
            boolean r0 = r14.isOneOf(r0)
            if (r0 != 0) goto L_0x016d
            java.lang.String r0 = "onNetworkEventChanged: tryRegister"
            com.sec.internal.log.IMSLog.i(r8, r15, r0)
            com.sec.internal.ims.core.RegistrationManagerBase r15 = r13.mRegMan
            r15.tryRegister((com.sec.internal.ims.core.RegisterTask) r14)
        L_0x016d:
            com.sec.internal.ims.core.RegistrationManagerHandler r14 = r13.mRegHandler
            r15 = 32
            boolean r14 = r14.hasMessages(r15)
            if (r14 != 0) goto L_0x017c
            com.sec.internal.ims.core.RegistrationManagerHandler r13 = r13.mRegHandler
            r13.sendEmptyMessage(r15)
        L_0x017c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.NetworkEventController.onNetworkEventChanged(com.sec.internal.constants.ims.os.NetworkEvent, int):void");
    }

    /* access modifiers changed from: package-private */
    public boolean handleNetworkEventOnRegister(int i, RegisterTask registerTask, NetworkEvent networkEvent, NetworkEvent networkEvent2) {
        IVolteServiceModule iVolteServiceModule;
        if (networkEvent.outOfService) {
            IMSLog.i(LOG_TAG, i, "out of service.");
            handleRestrictionOnNetworkEventChanged(i, registerTask, networkEvent);
            handleOutOfServiceOnNetworkEvnentChanged(registerTask, i);
            return false;
        }
        if (registerTask.getMno().isKor() && !registerTask.isRcsOnly() && registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING && networkEvent.network != registerTask.getRegistrationRat()) {
            Log.i(LOG_TAG, "onNetworkEventChanged: setRegistrationRat");
            registerTask.setRegistrationRat(networkEvent.network);
        }
        if (updateEpdgStatusOnNetworkEventChanged(registerTask, networkEvent, networkEvent2)) {
            return false;
        }
        if (registerTask.getMno() == Mno.VZW) {
            if (NetworkUtil.is3gppLegacyNetwork(networkEvent.network) && NetworkUtil.is3gppPsVoiceNetwork(networkEvent2.network) && this.mRegHandler.hasMessages(121, Integer.valueOf(i))) {
                this.mRegHandler.removeMessages(121, Integer.valueOf(i));
                SlotBasedConfig.getInstance(i).enableSsac(true);
            }
            if (networkEvent.isVopsUpdated(networkEvent2) != NetworkEvent.VopsState.ENABLED || !networkEvent.operatorNumeric.equals(networkEvent2.operatorNumeric)) {
                if (registerTask.getRegistrationRat() == 14 && NetworkUtil.is3gppPsVoiceNetwork(networkEvent.network)) {
                    int intValue = DmConfigHelper.readInt(this.mContext, "t_delay", 5, i).intValue();
                    IMSLog.i(LOG_TAG, i, "onNetworkChanged: C2L, Tdelay=" + intValue);
                    if (intValue > 0) {
                        this.mRegMan.addPendingUpdateRegistration(registerTask, intValue);
                        return false;
                    }
                }
                if (this.mRegHandler.hasMessages(806)) {
                    IMSLog.i(LOG_TAG, i, "do not update registration due to HYS");
                    return false;
                }
            } else {
                int intValue2 = DmConfigHelper.readInt(this.mContext, "tvolte_hys_timer", 60, i).intValue();
                IMSLog.i(LOG_TAG, i, "Pending re-regi to T_VoLTE_hys[" + intValue2 + "] secs.");
                if (this.mRegHandler.hasMessages(806)) {
                    this.mRegHandler.removeMessages(806);
                }
                this.mRegHandler.sendEmptyMessageDelayed(806, ((long) intValue2) * 1000);
                this.mRegMan.addPendingUpdateRegistration(registerTask, intValue2);
                return false;
            }
        } else if (registerTask.getMno() == Mno.ATT) {
            if (this.mRegMan.getImsIconManager(registerTask.getPhoneId()) != null) {
                this.mRegMan.getImsIconManager(registerTask.getPhoneId()).updateRegistrationIcon();
            }
        } else if (registerTask.getMno() == Mno.SPRINT && !registerTask.isRcsOnly()) {
            if (!(networkEvent2.isDataRoaming == networkEvent.isDataRoaming && networkEvent2.isVoiceRoaming == networkEvent.isVoiceRoaming)) {
                Log.i(LOG_TAG, "onNetworkChanged: roaming event changed, check location cache");
                registerTask.getGovernor().onLocationCacheExpiry();
            }
            if (!(networkEvent2.isPsOnlyReg == networkEvent.isPsOnlyReg && networkEvent2.isVoiceRoaming == networkEvent.isVoiceRoaming)) {
                Log.i(LOG_TAG, "onNetworkEventChanged: roaming or ps-voice-only mode changed in registering/registered state");
                registerTask.getGovernor().onServiceStateDataChanged(networkEvent.isPsOnlyReg, networkEvent.isVoiceRoaming);
            }
        } else if (ConfigUtil.isRcsChn(registerTask.getMno()) && registerTask.isRcsOnly() && networkEvent.network == 16 && (iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.hasCsCall(i)) {
            Log.i(LOG_TAG, "RCS deregister during CS call - GSM : same as OOS");
            registerTask.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(registerTask, true, true);
            return false;
        }
        registerTask.setReason("by network event changed");
        this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.NETWORK_EVENT_CHANGED);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean handleNetworkEventBeforeRegister(int i, RegisterTask registerTask, NetworkEvent networkEvent, NetworkEvent networkEvent2) {
        int i2;
        int i3 = i;
        RegisterTask registerTask2 = registerTask;
        NetworkEvent networkEvent3 = networkEvent;
        NetworkEvent networkEvent4 = networkEvent2;
        IMSLog.i(LOG_TAG, i3, "handleNetworkEventBeforeRegister: new network event=" + networkEvent3 + ", old network event=" + networkEvent4);
        boolean z = !TextUtils.isEmpty(networkEvent4.operatorNumeric) && !TextUtils.isEmpty(networkEvent3.operatorNumeric) && !TextUtils.equals(networkEvent4.operatorNumeric, networkEvent3.operatorNumeric);
        boolean z2 = networkEvent.isVopsUpdated(networkEvent2) == NetworkEvent.VopsState.ENABLED;
        if (registerTask.getMno().isOneOf(Mno.TMOUS, Mno.DISH)) {
            if (networkEvent3.network == networkEvent4.network && !networkEvent3.isWifiConnected) {
                IMSLog.i(LOG_TAG, i3, "onNetworkEventChanged: WiFi has turned off. release throttle.");
                registerTask.getGovernor().releaseThrottle(2);
            }
            if (registerTask.getGovernor().isThrottled()) {
                if (z) {
                    registerTask.getGovernor().releaseThrottle(9);
                } else if (networkEvent3.network != networkEvent4.network) {
                    registerTask.getGovernor().releaseThrottle(6);
                } else if (!networkEvent3.outOfService && networkEvent4.outOfService) {
                    registerTask.getGovernor().releaseThrottle(14);
                }
            }
        }
        if (networkEvent3.voiceOverPs == VoPsIndication.NOT_SUPPORTED && registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING && registerTask.getPdnType() == 11 && NetworkUtil.is3gppPsVoiceNetwork(networkEvent3.network) && registerTask.getMno() != Mno.ATT && registerTask.getMno() != Mno.VZW && registerTask.getMno() != Mno.TRUEMOVE && registerTask.getMno() != Mno.AIS && registerTask.getMno() != Mno.SPRINT && !registerTask.getMno().isKor()) {
            this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask2);
            registerTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
        }
        if (registerTask.getMno().isOneOf(Mno.BOG, Mno.TELECOM_ITALY, Mno.RJIL, Mno.H3G, Mno.CU) && registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING && registerTask.getPdnType() == 11 && networkEvent3.outOfService) {
            this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask2);
            registerTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
        }
        if (RegistrationUtils.isCmcProfile(registerTask.getProfile()) && registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING && networkEvent3.isWifiConnected && !networkEvent4.isWifiConnected) {
            IMSLog.i(LOG_TAG, i3, "onNetworkEventChanged: Wifi connected in CMC profile. Stop the conneting PDN");
            this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask2);
            registerTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
        }
        if (registerTask.getMno().isOneOf(Mno.CMCC, Mno.CU) && registerTask.getPdnType() == 11) {
            if (registerTask.getGovernor().isDelayedDeregisterTimerRunning()) {
                if (NetworkUtil.is3gppPsVoiceNetwork(networkEvent3.network)) {
                    String networkTypeName = TelephonyManagerExt.getNetworkTypeName(networkEvent3.network);
                    IMSLog.i(LOG_TAG, i3, "onNetworkEventChanged: " + networkTypeName + "attached while DelayedDeregisterTimer running.");
                    this.mRegMan.onDelayedDeregister(registerTask2);
                } else {
                    IMSLog.i(LOG_TAG, i3, "onNetworkEventChanged: Do not stop IMS PDN on delayedDeregisterTimer running");
                }
                return false;
            }
            if (!NetworkUtil.is3gppPsVoiceNetwork(networkEvent3.network)) {
                registerTask.getGovernor().resetAllPcscfChecked();
            } else if (registerTask.getGovernor().isThrottled() && (i2 = networkEvent3.network) == 20 && i2 != networkEvent4.network) {
                registerTask.getGovernor().releaseThrottle(6);
            }
            if (this.mTelephonyManager.getCallState() != 0 && networkEvent3.network == 16) {
                return false;
            }
        }
        if (registerTask.getMno() == Mno.ROGERS) {
            boolean isHandoverBetweenEpdgAndLeagacy = isHandoverBetweenEpdgAndLeagacy(networkEvent3.network, registerTask.getRegistrationRat());
            boolean z3 = NetworkUtil.is3gppLegacyNetwork(networkEvent3.network) && "A4".equalsIgnoreCase(this.mTelephonyManager.getGroupIdLevel1(SimUtil.getSubId(i)));
            if (isHandoverBetweenEpdgAndLeagacy || z3) {
                if (isHandoverBetweenEpdgAndLeagacy) {
                    registerTask2.setReason("Handover Between VoWifi and 2G/3G");
                } else {
                    registerTask2.setReason("RWC 5G SIM doesn't support IMS on 2G/3G");
                }
                RegistrationConstants.RegisterTaskState state = registerTask.getState();
                RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.IDLE;
                if (state != registerTaskState) {
                    this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask2);
                    registerTask2.setState(registerTaskState);
                }
                if (registerTask.getGovernor().isThrottled()) {
                    registerTask.getGovernor().releaseThrottle(6);
                }
            }
        }
        if (registerTask.getGovernor().isThrottled() && registerTask.getMno().isOneOf(Mno.TELUS, Mno.KOODO, Mno.ROGERS, Mno.VTR, Mno.EASTLINK, Mno.ATT) && z) {
            registerTask.getGovernor().releaseThrottle(9);
        }
        if (registerTask.getGovernor().isThrottled() && registerTask.getMno().isOneOf(Mno.APT, Mno.YOIGO_SPAIN) && z) {
            registerTask.getGovernor().releaseThrottle(6);
        }
        if (!networkEvent3.isDataRoaming || registerTask.getGovernor().allowRoaming() || !this.mPdnController.isNetworkRequested(registerTask2)) {
            if (registerTask.getMno().isEmeasewaoce() && registerTask.getProfile().getPdnType() == 11 && registerTask.getGovernor().allowRoaming() && !networkEvent3.outOfService && networkEvent4.isDataRoaming != networkEvent3.isDataRoaming && registerTask.getGovernor().isThrottled()) {
                registerTask.getGovernor().releaseThrottle(11);
            }
            if (registerTask.getMno() == Mno.VZW || registerTask.getMno() == Mno.WIND) {
                if (!TextUtils.equals(networkEvent3.operatorNumeric, networkEvent4.operatorNumeric)) {
                    registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_PLMN_CHANGED);
                    if (registerTask.getGovernor().getThrottleState() == IRegistrationGovernor.ThrottleState.PERMANENTLY_STOPPED) {
                        Log.i(LOG_TAG, "PLMN changed but Permanent Stopped. Do nothing!");
                    } else {
                        if (networkEvent3.voiceOverPs == VoPsIndication.SUPPORTED) {
                            registerTask.getGovernor().startTimsTimer(RegistrationConstants.REASON_PLMN_CHANGED);
                        }
                        if (registerTask.getGovernor().isThrottled()) {
                            registerTask.getGovernor().releaseThrottle(9);
                        }
                    }
                }
                if ((registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING && !networkEvent3.outOfService && networkEvent3.voiceOverPs == VoPsIndication.SUPPORTED && networkEvent3.network == 13) || z2) {
                    registerTask.getGovernor().startTimsTimer(z2 ? RegistrationConstants.REASON_VOPS_CHANGED : RegistrationConstants.REASON_IMS_PDN_REQUEST);
                }
                if (networkEvent3.network != networkEvent4.network && registerTask.getGovernor().isThrottled()) {
                    registerTask.getGovernor().releaseThrottle(6);
                }
            }
            if (registerTask.getMno() == Mno.KDDI && networkEvent3.network != networkEvent4.network && registerTask.getGovernor().isThrottled()) {
                Log.i(LOG_TAG, "Kddi throttled, check Network Map for network=" + networkEvent3.network);
                int i4 = networkEvent3.network;
                if (i4 == 13) {
                    registerTask.getGovernor().releaseThrottle(12);
                } else if (i4 == 20) {
                    registerTask.getGovernor().releaseThrottle(13);
                }
            }
            if (registerTask.getMno() == Mno.SPRINT && ImsProfile.hasVolteService(registerTask.getProfile()) && !(networkEvent4.isPsOnlyReg == networkEvent3.isPsOnlyReg && networkEvent4.isVoiceRoaming == networkEvent3.isVoiceRoaming)) {
                Log.i(LOG_TAG, "onNetworkEventChanged: roaming or ps-voice-only mode changed in idle/connecting state");
                registerTask.getGovernor().onServiceStateDataChanged(RegistrationUtils.getNetworkEvent(i).isPsOnlyReg, RegistrationUtils.getNetworkEvent(i).isVoiceRoaming);
            }
            if (!registerTask2.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED) || networkEvent4.network == networkEvent3.network) {
                return true;
            }
            reconnectPdn(registerTask2);
            return true;
        }
        IMSLog.i(LOG_TAG, i3, "stopPdnConnectivity(), task " + registerTask2);
        this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask2);
        registerTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
        return false;
    }

    private void handleNetworkEventOnDeregistering(int i, RegisterTask registerTask, NetworkEvent networkEvent, NetworkEvent networkEvent2) {
        if (registerTask.getMno() == Mno.SPRINT && ImsProfile.hasVolteService(registerTask.getProfile()) && !(networkEvent2.isPsOnlyReg == networkEvent.isPsOnlyReg && networkEvent2.isVoiceRoaming == networkEvent.isVoiceRoaming)) {
            IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: roaming or ps-voice-only mode changed in de-registering state");
            registerTask.getGovernor().onServiceStateDataChanged(networkEvent.isPsOnlyReg, networkEvent.isVoiceRoaming);
        }
        if (registerTask.getMno() == Mno.CU && registerTask.isKeepPdn() && registerTask.getDeregiReason() == 2 && this.mRegMan.findBestNetwork(registerTask.getPhoneId(), registerTask.getProfile(), registerTask.getGovernor()) == 0) {
            IMSLog.i(LOG_TAG, i, "CU, if not in LTE,will stop pdn when in deregistering state caused by pdn lost");
            registerTask.setKeepPdn(false);
        }
    }

    private void handleNetworkEvent(int i, RegisterTask registerTask, NetworkEvent networkEvent, NetworkEvent networkEvent2, boolean z, boolean z2) {
        if (registerTask.getMno().isKor()) {
            handleRestrictionOnNetworkEventChanged(i, registerTask, networkEvent);
            boolean z3 = !TextUtils.isEmpty(networkEvent2.operatorNumeric) && !TextUtils.isEmpty(networkEvent.operatorNumeric) && !TextUtils.equals(networkEvent2.operatorNumeric, networkEvent.operatorNumeric);
            if (registerTask.getGovernor().isThrottled() && registerTask.getGovernor().isThrottledforImsNotAvailable() && (z3 || this.mPdnController.isEpsOnlyReg(i))) {
                registerTask.getGovernor().releaseThrottle(9);
            }
            if (registerTask.getGovernor().isMobilePreferredForRcs()) {
                IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: event.isDataStateConnected: " + networkEvent.isDataStateConnected + " old.isDataStateConnected: " + networkEvent2.isDataStateConnected + " event.outOfService: " + networkEvent.outOfService + " old.outOfService: " + networkEvent2.outOfService + " task.getPdnType() " + registerTask.getPdnType() + " rat: " + registerTask.getRegistrationRat() + " isWifiConnected: " + this.mPdnController.isWifiConnected() + " " + registerTask.getProfile().getName() + "(" + registerTask.getState() + ")");
                if (((networkEvent.isDataStateConnected && !networkEvent2.isDataStateConnected) || (networkEvent.outOfService && !networkEvent2.outOfService)) && this.mPdnController.isWifiConnected() && registerTask.getPdnType() == 1 && registerTask.isOneOf(RegistrationConstants.RegisterTaskState.RESOLVING, RegistrationConstants.RegisterTaskState.RESOLVED, RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED, RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    isPreferredPdnForRCSRegister(registerTask, i, true);
                }
            }
            if (z || z2) {
                IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: do not call sendTryRegister");
            } else {
                IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: sendTryRegister");
                this.mRegHandler.sendTryRegister(i);
            }
        }
        if (networkEvent.isVopsUpdated(networkEvent2) == NetworkEvent.VopsState.DISABLED) {
            handleVopsDisabledOnNetworkEventChanged(registerTask, networkEvent, i);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateEpdgStatusOnNetworkEventChanged(RegisterTask registerTask, NetworkEvent networkEvent, NetworkEvent networkEvent2) {
        if (registerTask.getProfile().isEpdgSupported()) {
            if (networkEvent.isEpdgHOEvent(networkEvent2)) {
                return true;
            }
            if (registerTask.getMno() == Mno.VZW) {
                if ((this.mPdnController.isEpdgAvailable(registerTask.getPhoneId()) && networkEvent.network == 18 && networkEvent2.network == 18 && networkEvent.isPsOnlyReg != networkEvent2.isPsOnlyReg) || (this.mPdnController.isEpdgConnected(registerTask.getPhoneId()) && networkEvent.network != networkEvent2.network)) {
                    RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                    registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(135, registerTask), 121000);
                    return true;
                }
            }
        }
        return false;
    }

    private void handleOutOfServiceOnNetworkEvnentChanged(RegisterTask registerTask, int i) {
        IVolteServiceModule iVolteServiceModule;
        IVolteServiceModule iVolteServiceModule2;
        IMSLog.i(LOG_TAG, i, "out of service.");
        Mno simMno = SimUtil.getSimMno(i);
        if (simMno == Mno.RJIL) {
            IMSLog.i(LOG_TAG, i, "Set OutOfService True for RJIL");
            this.mVsm.setOutOfService(true, i);
        }
        if (registerTask.getRegistrationRat() != 18 && ConfigUtil.isRcsEur(simMno) && registerTask.isRcsOnly()) {
            Log.i(LOG_TAG, "set EVENT_RCS_DELAYED_DEREGISTER");
            this.mRegHandler.removeMessages(142);
            RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(142), 30000);
        }
        if (ConfigUtil.isRcsChn(simMno) && registerTask.isRcsOnly() && (iVolteServiceModule2 = this.mVsm) != null && iVolteServiceModule2.hasCsCall(i)) {
            Log.i(LOG_TAG, "RCS deregister OOS during CS call");
            registerTask.setDeregiReason(4);
            this.mRegMan.tryDeregisterInternal(registerTask, true, true);
        }
        if (simMno == Mno.EE_ESN) {
            Log.i(LOG_TAG, "ESN send local deregi and PDN disconnect");
            this.mRegMan.tryDeregisterInternal(registerTask, true, false);
        }
        if (simMno.isOneOf(Mno.SMARTONE, Mno.ETISALAT_UAE) && (iVolteServiceModule = this.mVsm) != null && iVolteServiceModule.hasCsCall(i)) {
            Log.i(LOG_TAG, "Send local deregi and PDN disconnect after CSFB");
            this.mRegMan.tryDeregisterInternal(registerTask, true, false);
        }
        if (this.mImsFramework.getBoolean(registerTask.getPhoneId(), GlobalSettingsConstants.Registration.REMOVE_ICON_NOSVC, false) && this.mRegMan.getImsIconManager(registerTask.getPhoneId()) != null) {
            this.mRegMan.getImsIconManager(registerTask.getPhoneId()).updateRegistrationIcon();
        }
    }

    private void handleRestrictionOnNetworkEventChanged(int i, RegisterTask registerTask, NetworkEvent networkEvent) {
        if (registerTask.getMno().isKor() && !registerTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(registerTask.getProfile())) {
            if ((networkEvent.isDataRoaming && registerTask.getGovernor().allowRoaming() && (!NetworkUtil.is3gppPsVoiceNetwork(networkEvent.network) || networkEvent.voiceOverPs != VoPsIndication.SUPPORTED || networkEvent.outOfService)) || (!networkEvent.isDataRoaming && SimUtil.getPhoneCount() > 1 && i != SimUtil.getActiveDataPhoneId() && SemSystemProperties.get("ro.boot.hardware", "").contains("qcom") && !networkEvent.csOutOfService && networkEvent.voiceNetwork == 3)) {
                IMSLog.i(LOG_TAG, i, "handleRestrictionOnNetworkEventChanged: task:" + registerTask.getProfile().getName() + "(" + registerTask.getState() + ")");
                registerTask.setDeregiReason(2);
                registerTask.setReason("handleRestrictionOnNetworkEventChanged: VoLTE roaming disabled(not LTE/NR, not VoPS) or VoLTE disabled(qcom non DDS is cs only in 3G)");
                this.mRegMan.tryDeregisterInternal(registerTask, false, false);
                IMSLog.i(LOG_TAG, i, "handleRestrictionOnNetworkEventChanged: VoLTE roaming disabled(not LTE/NR, not VoPS) or VoLTE disabled(non DDS in 3G)");
                if (!registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                    this.mRegHandler.sendDisconnectPdnByVolteDisabled(registerTask, 0);
                }
            }
        }
    }

    private void handleSsacOnNetworkEventChanged(RegisterTask registerTask, int i, NetworkEvent networkEvent, NetworkEvent networkEvent2) {
        if (registerTask.getMno() == Mno.VZW && this.mRegHandler.hasMessages(121, Integer.valueOf(i)) && !TextUtils.equals(networkEvent.operatorNumeric, networkEvent2.operatorNumeric)) {
            if (registerTask.getImsRegistration() == null) {
                IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: remove SSAC re-regi");
                this.mRegHandler.removeMessages(121, Integer.valueOf(i));
            }
            IMSLog.i(LOG_TAG, i, "onNetworkEventChanged: set SSAC to default");
            SlotBasedConfig.getInstance(i).enableSsac(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleVopsDisabledOnNetworkEventChanged(RegisterTask registerTask, NetworkEvent networkEvent, int i) {
        if (registerTask.getMno() == Mno.VZW) {
            registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_VOPS_CHANGED);
            this.mRegHandler.removeMessages(132);
            if ((this.mTelephonyManager.isNetworkRoaming() || ImsUtil.isCdmalessEnabled(this.mContext, i)) && !this.mRegMan.getCsfbSupported(i)) {
                this.mRegMan.notifyImsNotAvailable(registerTask, true);
            }
        } else if (registerTask.getMno().isChn() && !this.mRegMan.getCsfbSupported(i)) {
            registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_VOPS_CHANGED);
            this.mRegHandler.removeMessages(132);
            if (registerTask.getMno().isOneOf(Mno.CTC, Mno.CTCMO) || (registerTask.getMno().isOneOf(Mno.CMCC, Mno.CU) && networkEvent.network == 20)) {
                this.mRegMan.notifyImsNotAvailable(registerTask, true);
            }
        }
    }

    private void tryCmcRegisterOnNetworkEventChanged(NetworkEvent networkEvent, NetworkEvent networkEvent2) {
        if (!this.mCmcAccountManager.isCmcProfileAdded() && !networkEvent.outOfService && networkEvent2.outOfService) {
            this.mCmcAccountManager.startCmcRegistration();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateUtOnNetworkEventChanged(int i, boolean z, NetworkEvent networkEvent, NetworkEvent networkEvent2) {
        boolean z2 = true;
        boolean z3 = !this.mImsFramework.getBoolean(i, GlobalSettingsConstants.SS.ENABLE_IN_ROAMING, true);
        if (z && !networkEvent.outOfService && z3) {
            boolean z4 = networkEvent2.isDataRoaming;
            boolean z5 = networkEvent.isDataRoaming;
            if (z4 == z5) {
                int i2 = networkEvent2.network;
                int i3 = networkEvent.network;
                if (i2 == i3) {
                    return;
                }
                if (!(i3 == 18 || i2 == 18)) {
                    return;
                }
            }
            boolean z6 = networkEvent.network == 18;
            if (z5 && !z6) {
                z2 = false;
            }
            IUtServiceModule utServiceModule = this.mImsFramework.getServiceModuleManager().getUtServiceModule();
            if (utServiceModule != null) {
                utServiceModule.enableUt(i, z2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPreferredPdnForRCSRegister(RegisterTask registerTask, int i, boolean z) {
        Mno mno = registerTask.getMno();
        boolean z2 = true;
        if (!mno.isKor() && mno != Mno.TMOBILE && mno != Mno.H3G) {
            return true;
        }
        int pdnType = registerTask.getPdnType();
        if (!(pdnType == 11 || pdnType == 15)) {
            if (mno.isKor()) {
                boolean isNeedDelayedDeregister = registerTask.getGovernor().isNeedDelayedDeregister();
                PdnController pdnController = this.mPdnController;
                int translateNetworkBearer = pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer());
                IMSLog.i(LOG_TAG, "isPreferredPdnForRCSRegister: isNeedDelayedDeregister [" + isNeedDelayedDeregister + "], preferred PDN [" + translateNetworkBearer + "], needDeregi [" + z + "]");
                if (!registerTask.getGovernor().isMobilePreferredForRcs() || z || !isNeedDelayedDeregister || translateNetworkBearer != 1) {
                    deregisterByDefaultNwChanged(registerTask, i, z);
                } else if (!this.mRegHandler.hasMessages(18)) {
                    this.mEventLog.logAndAdd(i, "isPreferredPdnForRCSRegister : Delay event");
                    RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
                    registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(18, registerTask), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                } else {
                    this.mEventLog.logAndAdd(i, "isPreferredPdnForRCSRegister : Now pending");
                }
                return true;
            } else if (this.mPdnController.isWifiConnected() && NetworkUtil.isMobileDataConnected(this.mContext)) {
                int translateNetworkBearer2 = this.mPdnController.translateNetworkBearer(this.mPdnController.getDefaultNetworkBearer());
                if (pdnType != translateNetworkBearer2) {
                    deregisterByDefaultNwChanged(registerTask, i, z);
                    z2 = false;
                }
                SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
                if (pendingRegistrationInternal != null) {
                    Iterator it = pendingRegistrationInternal.iterator();
                    while (it.hasNext()) {
                        RegisterTask registerTask2 = (RegisterTask) it.next();
                        if (!(registerTask2 == registerTask || !registerTask2.isRcsOnly() || registerTask2.getPdnType() == translateNetworkBearer2)) {
                            deregisterByDefaultNwChanged(registerTask2, i, z);
                        }
                    }
                }
            }
        }
        return z2;
    }

    private void deregisterByDefaultNwChanged(RegisterTask registerTask, int i, boolean z) {
        IMSLog.i(LOG_TAG, i, "deregisterByDefaultNwChanged: " + registerTask.getProfile().getName() + "(" + registerTask.getState() + ") needDeregi(" + z + ")");
        PdnController pdnController = this.mPdnController;
        boolean z2 = true;
        if (pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer()) != 1) {
            z2 = false;
        }
        Mno mno = registerTask.getMno();
        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            if (!mno.isKor() && mno != Mno.TMOBILE && mno != Mno.H3G) {
                return;
            }
            if (!registerTask.getGovernor().isMobilePreferredForRcs() || z2 || z) {
                registerTask.setDeregiReason(12);
                this.mRegMan.tryDeregisterInternal(registerTask, false, false);
            }
        } else if (mno.isKor()) {
            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            if (registerTask.getGovernor().isMobilePreferredForRcs() && z2) {
                IMSLog.i(LOG_TAG, i, "deregisterByDefaultNwChanged: stop pdn");
                this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDefaultNetworkStateChanged(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.isRcsOnly()) {
                    isPreferredPdnForRCSRegister(registerTask, i, false);
                    break;
                }
            }
        }
        RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(2, Integer.valueOf(i)), UtStateMachine.HTTP_READ_TIMEOUT_GCF);
    }

    /* access modifiers changed from: package-private */
    public void handOffEventTimeout(int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "handOffEventTimeout: mNetType = " + getNetType() + ", mWiFi = " + isWiFi());
        this.mRegMan.suspendRegister(false, i);
    }

    /* access modifiers changed from: protected */
    public void onEpdgIkeError(int i) {
        SlotBasedConfig.getInstance(i).getRegistrationTasks().stream().filter(new NetworkEventController$$ExternalSyntheticLambda3()).forEach(new NetworkEventController$$ExternalSyntheticLambda4());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$onEpdgIkeError$4(RegisterTask registerTask) {
        return registerTask.getPdnType() == 11;
    }

    /* access modifiers changed from: protected */
    public void onIpsecDisconnected(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal;
        IMSLog.i(LOG_TAG, i, "onIpsecDisconnected:");
        int voiceCallType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, i);
        boolean isEnabled = VowifiConfig.isEnabled(this.mContext, i);
        int prefMode = VowifiConfig.getPrefMode(this.mContext, 2, i);
        IMSLog.i(LOG_TAG, i, "onIpsecDisconnected: VoWiFi : " + isEnabled + ", pref: " + prefMode + ", callType : " + voiceCallType);
        if (isEnabled && prefMode == 2 && voiceCallType == 1 && (pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i)) != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getMno().isTw() && registerTask.getProfile().isEpdgSupported() && registerTask.getProfile().getPdnType() == 11) {
                    if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                        this.mRegMan.tryDeregisterInternal(registerTask, false, false);
                    } else if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTED, RegistrationConstants.RegisterTaskState.CONNECTING)) {
                        Log.i(LOG_TAG, "Stop pdn when ipsec disconnected.");
                        this.mRegMan.stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                        registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onVoicePreferredChanged(int i) {
        Log.i(LOG_TAG, "onVoicePreferredChanged:");
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        boolean z = false;
        if (iVolteServiceModule == null) {
            Log.e(LOG_TAG, "VolteServiceModule is not create yet so retry after 3 seconds");
            RegistrationManagerHandler registrationManagerHandler = this.mRegHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(123, i, 0, (Object) null), RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
            return;
        }
        iVolteServiceModule.onVoWiFiSwitched(i);
        if (this.mPdnController.isEpdgConnected(i)) {
            EpdgManager epdgManager = this.mVsm.getEpdgManager();
            int dataNetworkType = this.mTelephonyManager.getDataNetworkType();
            if (epdgManager != null) {
                z = epdgManager.isPossibleW2LHOAfterCallEndBySim(i);
                Log.i(LOG_TAG, "W2L available : " + z);
            }
            if (dataNetworkType != 13 || !z) {
                this.mRegMan.updateRegistration(i, RegistrationConstants.UpdateRegiReason.EPDG_VOICEPREFERENCE_CHANGED);
            } else {
                Log.i(LOG_TAG, "EpdgEventReceiver, waiting for W2L HO event w/o re-regi");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLocalIpChanged(RegisterTask registerTask) {
        registerTask.getGovernor().onLocalIpChanged();
        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
            registerTask.setReason("local IP changed");
            registerTask.setDeregiReason(5);
            this.mRegMan.tryDeregisterInternal(registerTask, true, true);
        }
        if (!registerTask.getMno().isKor() || !registerTask.isRcsOnly()) {
            this.mRegMan.setOmadmState(registerTask.mPhoneId, RegistrationManager.OmadmConfigState.IDLE);
            registerTask.getGovernor().resetPcscfPreference();
            registerTask.getGovernor().resetIPSecAllow();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onConfigUpdated(String str, int i) {
        IMSLog.i(LOG_TAG, i, "onConfigUpdated: " + str);
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager == null) {
            return false;
        }
        if (!TextUtils.isEmpty(str)) {
            if (TextUtils.indexOf(str, ':') != -1) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                simpleEventLog.logAndAdd(i, "Invalid DM item : " + str);
                IMSLog.c(LogClass.REGI_CONFIG_UPDATE, i + ",INVLD DM: " + str);
                return false;
            }
            this.mRegHandler.notifyDmValueChanged(str, i);
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal == null) {
            return true;
        }
        Iterator it = pendingRegistrationInternal.iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            registerTask.getGovernor().onConfigUpdated();
            if (iSimManager.getSimMno().isKor() && this.mRegMan.getOmadmState(i) == RegistrationManager.OmadmConfigState.FINISHED && !registerTask.isRcsOnly()) {
                Log.i(LOG_TAG, "onConfigUpdated:  mOmadmState is FINISHED");
            } else if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                registerTask.setReason("IMS configuration changed");
                this.mRegMan.updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.IMS_CONFIGURATION_CHANGED);
            } else if (registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTING) {
                reconnectPdn(registerTask);
            }
        }
        return true;
    }

    private boolean isNewPcscfListAvailable(IRegisterTask iRegisterTask) {
        List<String> retrievePcscfByProfileSettings = RegistrationUtils.retrievePcscfByProfileSettings(iRegisterTask, this.mPdnController, this.mRcsPolicyManager, this.mTelephonyManager.getIsimPcscf());
        return !CollectionUtils.isNullOrEmpty((Collection<?>) retrievePcscfByProfileSettings) && !retrievePcscfByProfileSettings.equals(this.mLastPcscfList);
    }
}
