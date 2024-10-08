package com.sec.internal.ims.core;

import android.annotation.SuppressLint;
import android.net.DnsResolver;
import android.net.Network;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Message;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.gls.LocationInfo;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NaptrDnsResolver;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.SrvDnsResolver;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.entitlement.nsds.NSDSSimEventManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IGeolocationController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IMSLogTimer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrationManagerBase extends RegistrationManagerInternal {
    public static final long DELAY = 10000;
    public static final int DNS_QUERY_RETRY_COUNT = 5;

    public /* bridge */ /* synthetic */ void initSequentially() {
        super.initSequentially();
    }

    public /* bridge */ /* synthetic */ void suspended(RegisterTask registerTask, boolean z, int i) {
        super.suspended(registerTask, z, i);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public RegistrationManagerBase(android.os.Looper r15, com.sec.internal.interfaces.ims.IImsFramework r16, android.content.Context r17, com.sec.internal.ims.core.PdnController r18, java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r19, com.sec.internal.helper.os.ITelephonyManager r20, com.sec.internal.interfaces.ims.core.ICmcAccountManager r21, com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager r22) {
        /*
            r14 = this;
            r12 = r14
            r0 = r14
            r1 = r16
            r2 = r17
            r3 = r18
            r4 = r19
            r5 = r20
            r6 = r21
            r7 = r22
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            com.sec.internal.ims.core.NetworkEventController r9 = new com.sec.internal.ims.core.NetworkEventController
            com.sec.internal.interfaces.ims.IImsFramework r8 = r12.mImsFramework
            r0 = r9
            r1 = r17
            r2 = r18
            r3 = r20
            r5 = r21
            r6 = r22
            r7 = r14
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8)
            r12.mNetEvtCtr = r9
            com.sec.internal.ims.core.UserEventController r7 = new com.sec.internal.ims.core.UserEventController
            com.sec.internal.helper.SimpleEventLog r6 = r12.mEventLog
            r0 = r7
            r2 = r14
            r3 = r18
            r5 = r20
            r0.<init>(r1, r2, r3, r4, r5, r6)
            r12.mUserEvtCtr = r7
            com.sec.internal.ims.core.RegistrationManagerHandler r13 = new com.sec.internal.ims.core.RegistrationManagerHandler
            com.sec.internal.ims.core.NetworkEventController r9 = r12.mNetEvtCtr
            com.sec.internal.ims.core.UserEventController r10 = r12.mUserEvtCtr
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r11 = r12.mVsm
            r0 = r13
            r1 = r15
            r2 = r17
            r3 = r14
            r4 = r16
            r5 = r18
            r6 = r19
            r7 = r20
            r8 = r21
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
            r12.mHandler = r13
            com.sec.internal.ims.core.UserEventController r0 = r12.mUserEvtCtr
            r0.mRegHandler = r13
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerBase.<init>(android.os.Looper, com.sec.internal.interfaces.ims.IImsFramework, android.content.Context, com.sec.internal.ims.core.PdnController, java.util.List, com.sec.internal.helper.os.ITelephonyManager, com.sec.internal.interfaces.ims.core.ICmcAccountManager, com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager):void");
    }

    public void setThirdPartyFeatureTags(String[] strArr) {
        this.mThirdPartyFeatureTags = Arrays.asList(strArr);
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(126, SimUtil.getActiveDataPhoneId(), 0, (Object) null));
    }

    public void registerProfile(List<Integer> list, int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "registerProfile: " + list);
        this.mHandler.notifyManualRegisterRequested(list, i);
    }

    public void deregisterProfile(List<Integer> list, boolean z, int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "deregisterProfile: " + list + " disconnectPdn=" + z);
        this.mHandler.notifyManualDeRegisterRequested(list, z, i);
    }

    public int registerProfile(ImsProfile imsProfile, int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "registerProfile: profile=" + imsProfile.toString());
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager == null) {
            return -1;
        }
        if (iSimManager.hasVsim() && SlotBasedConfig.getInstance(i).getIconManager() == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "registerProfile: added iconmanager.");
            SlotBasedConfig.getInstance(i).createIconManager(this.mContext, this, this.mPdnController, iSimManager.getSimMno(), i);
        }
        return this.mHandler.notifyManualRegisterRequested(imsProfile, iSimManager.hasVsim(), i);
    }

    public void deregisterProfile(int i, int i2) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "deregisterProfile: handle:" + i);
        this.mHandler.notifyManualDeRegisterRequested(i, i2);
    }

    public void deregisterProfile(int i, int i2, boolean z) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "deregisterProfile: handle:" + i + ", disconnectPdn: " + z);
        this.mHandler.notifyManualDeRegisterRequested(i, i2, z);
    }

    public int updateRegistration(ImsProfile imsProfile, int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateRegistration: profile=" + imsProfile);
        return this.mHandler.notifyUpdateRegisterRequested(imsProfile, i);
    }

    public int forcedUpdateRegistration(ImsProfile imsProfile, int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal;
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "forcedUpdateRegistration: profile=" + imsProfile);
        if (!(imsProfile == null || (pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i)) == null)) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().getId() == imsProfile.getId()) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, registerTask, "start updateRegistration");
                    this.mHandler.requestForcedUpdateRegistration(registerTask, 0);
                    return 0;
                }
            }
        }
        return -1;
    }

    public void doPendingUpdateRegistration() {
        this.mHandler.removeMessages(32);
        this.mHandler.sendEmptyMessage(32);
    }

    public void bootCompleted() {
        this.mHandler.removeMessages(150);
        this.mHandler.sendEmptyMessage(150);
    }

    public void deregister(IRegisterTask iRegisterTask, boolean z, boolean z2, String str) {
        Preconditions.checkNotNull(iRegisterTask);
        deregister(iRegisterTask, z, z2, 0, str);
    }

    public void deregister(IRegisterTask iRegisterTask, boolean z, boolean z2, int i, String str) {
        Preconditions.checkNotNull(iRegisterTask);
        iRegisterTask.setReason(str);
        Log.i(IRegistrationManager.LOG_TAG, "deregister: task=" + iRegisterTask + " local=" + z + " keepPdn=" + z2 + " delay=" + i + " reason=" + str);
        this.mHandler.requestPendingDeregistration(iRegisterTask, z, z2, (long) i);
    }

    public void sendDeregister(int i) {
        for (ISimManager simSlotIndex : this.mSimManagers) {
            sendDeregister(i, simSlotIndex.getSimSlotIndex());
        }
    }

    public void sendDeregister(int i, int i2) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i2, "sendDeregister: reason=" + i);
        this.mHandler.notifySendDeRegisterRequested(SimUtil.getMno(), i, i2);
    }

    public void sendDeregister(IRegisterTask iRegisterTask, long j) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(128, iRegisterTask), j);
    }

    public boolean isSuspended(int i) {
        RegisterTask registerTaskByRegHandle = getRegisterTaskByRegHandle(i);
        if (registerTaskByRegHandle != null) {
            return registerTaskByRegHandle.isSuspended();
        }
        Log.e(IRegistrationManager.LOG_TAG, "isSuspended: unknown handle " + i);
        return false;
    }

    public int getCurrentNetworkByPhoneId(int i) {
        if (getNetworkEvent(i) == null) {
            return 0;
        }
        return getNetworkEvent(i).network;
    }

    public int getCurrentNetwork(int i) {
        RegisterTask registerTaskByRegHandle = getRegisterTaskByRegHandle(i);
        if (registerTaskByRegHandle != null) {
            return registerTaskByRegHandle.getRegistrationRat();
        }
        Log.i(IRegistrationManager.LOG_TAG, "getCurrentNetwork: unknown handle " + i);
        return 0;
    }

    public String[] getCurrentPcscf(int i) {
        String[] strArr = new String[2];
        RegisterTask registerTaskByRegHandle = getRegisterTaskByRegHandle(i);
        if (registerTaskByRegHandle == null) {
            Log.i(IRegistrationManager.LOG_TAG, "getCurrentPcscf: unknown handle " + i);
            return null;
        }
        strArr[0] = registerTaskByRegHandle.getGovernor().getCurrentPcscfIp();
        strArr[1] = Integer.toString(registerTaskByRegHandle.getProfile().getSipPort());
        return strArr;
    }

    public void setTtyMode(int i, int i2) {
        boolean z = (i2 == Extensions.TelecomManager.TTY_MODE_OFF || i2 == Extensions.TelecomManager.RTT_MODE) ? false : true;
        if (SlotBasedConfig.getInstance(i).getTTYMode() != z) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "setTtyMode [" + z + "]");
            Bundle bundle = new Bundle();
            bundle.putInt("phoneId", i);
            bundle.putBoolean("mode", z);
            RegistrationManagerHandler registrationManagerHandler = this.mHandler;
            registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(37, bundle));
        }
    }

    public void setRttMode(int i, boolean z) {
        if (SlotBasedConfig.getInstance(i).getRTTMode() != z) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "setRttMode [" + z + "]");
            Bundle bundle = new Bundle();
            bundle.putInt("phoneId", i);
            bundle.putBoolean("mode", z);
            RegistrationManagerHandler registrationManagerHandler = this.mHandler;
            registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(RegistrationEvents.EVENT_RTTMODE_UPDATED, bundle));
        }
    }

    public void sendReRegister(int i, int i2) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "sendReRegister : pdnType:" + i2);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getPdnType() == i2) {
                    sendReRegister(registerTask);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"WrongConstant"})
    public void sendDnsQuery(IRegisterTask iRegisterTask, String str, String str2, List<String> list, String str3, String str4, String str5, long j) {
        Network network;
        int i;
        String str6 = str2;
        String str7 = str3;
        ArrayList arrayList = new ArrayList();
        try {
            network = iRegisterTask.getNetworkConnected().getPrivateDnsBypassingCopy();
        } catch (NullPointerException unused) {
            network = null;
        }
        Network network2 = network;
        if (network2 == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, "null task");
            return;
        }
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "sendDnsQuery: hostname " + str6 + " dnses " + list);
        str3.hashCode();
        char c = 65535;
        switch (str3.hashCode()) {
            case 65:
                if (str7.equals("A")) {
                    c = 0;
                    break;
                }
                break;
            case 82391:
                if (str7.equals("SRV")) {
                    c = 1;
                    break;
                }
                break;
            case 2000960:
                if (str7.equals("AAAA")) {
                    c = 2;
                    break;
                }
                break;
            case 74050619:
                if (str7.equals("NAPTR")) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                i = 1;
                break;
            case 1:
                i = 33;
                break;
            case 2:
                i = 28;
                break;
            case 3:
                i = 35;
                break;
            default:
                i = 0;
                break;
        }
        final IRegisterTask iRegisterTask2 = iRegisterTask;
        final String str8 = str;
        final List<String> list2 = list;
        final String str9 = str4;
        final String str10 = str5;
        int i2 = i;
        final long j2 = j;
        final ArrayList arrayList2 = arrayList;
        final int i3 = phoneId;
        int i4 = phoneId;
        final String str11 = str2;
        Network network3 = network2;
        final String str12 = str3;
        AnonymousClass1 r0 = new DnsResolver.Callback<List<NaptrDnsResolver.NaptrTarget>>() {
            public void onAnswer(List<NaptrDnsResolver.NaptrTarget> list, int i) {
                Log.d(IRegistrationManager.LOG_TAG, "DnsResponse: NaptrTargets size : " + list.size());
                boolean z = false;
                for (NaptrDnsResolver.NaptrTarget next : list) {
                    if (next.mType == 1) {
                        RegistrationManagerBase.this.sendDnsQuery(iRegisterTask2, str8, next.mName, list2, "SRV", str9, str10, j2);
                        z = true;
                    }
                }
                if (!z) {
                    RegistrationManagerBase.this.onDnsResponse(arrayList2, -1, i3);
                }
            }

            public void onError(DnsResolver.DnsException dnsException) {
                RegistrationManagerBase.this.mRegStackIf.sendDnsQuery(i3, str8, str11, list2, str12, str9, str10, j2);
            }
        };
        final ArrayList arrayList3 = arrayList;
        final int i5 = i4;
        final String str13 = str;
        final String str14 = str2;
        final List<String> list3 = list;
        final String str15 = str3;
        final String str16 = str4;
        final String str17 = str5;
        final long j3 = j;
        AnonymousClass2 r02 = new DnsResolver.Callback<List<SrvDnsResolver.SrvRecordInetAddress>>() {
            public void onAnswer(List<SrvDnsResolver.SrvRecordInetAddress> list, int i) {
                int i2 = -1;
                for (SrvDnsResolver.SrvRecordInetAddress next : list) {
                    arrayList3.add(next.mInetAddress.getHostAddress());
                    int i3 = next.mPort;
                    if (i2 != i3) {
                        i2 = i3;
                    }
                }
                RegistrationManagerBase.this.onDnsResponse(arrayList3, i2, i5);
            }

            public void onError(DnsResolver.DnsException dnsException) {
                RegistrationManagerBase.this.mRegStackIf.sendDnsQuery(i5, str13, str14, list3, str15, str16, str17, j3);
            }
        };
        final int i6 = i4;
        final ArrayList arrayList4 = arrayList;
        AnonymousClass3 r03 = new DnsResolver.Callback<List<InetAddress>>() {
            public void onAnswer(List<InetAddress> list, int i) {
                for (InetAddress next : list) {
                    IMSLog.d(IRegistrationManager.LOG_TAG, i6, next.toString());
                    arrayList4.add(next.getHostAddress());
                }
                RegistrationManagerBase.this.onDnsResponse(arrayList4, 0, i6);
            }

            public void onError(DnsResolver.DnsException dnsException) {
                RegistrationManagerBase.this.mRegStackIf.sendDnsQuery(i6, str13, str14, list3, str15, str16, str17, j3);
            }
        };
        CancellationSignal cancellationSignal = new CancellationSignal();
        DnsResolver instance = DnsResolver.getInstance();
        ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
        if (i2 == 35) {
            NaptrDnsResolver.query(network3, str2, newSingleThreadExecutor, cancellationSignal, r0, str4);
        } else if (i2 == 33) {
            SrvDnsResolver.query(network3, str2, newSingleThreadExecutor, cancellationSignal, r02);
        } else {
            instance.query(network3, str2, i2, 1, newSingleThreadExecutor, cancellationSignal, r03);
        }
    }

    public void sendDummyDnsQuery() {
        Log.i(IRegistrationManager.LOG_TAG, "sendDummyDnsQuery");
        List<String> dnsServersByNetType = this.mPdnController.getDnsServersByNetType();
        String str = "ipv4";
        if (dnsServersByNetType != null) {
            String str2 = dnsServersByNetType.get(0);
            Log.i(IRegistrationManager.LOG_TAG, "dns : " + str2);
            if (NetworkUtil.isIPv6Address(str2)) {
                str = "ipv6";
            }
        }
        String str3 = str;
        String intfNameByNetType = this.mPdnController.getIntfNameByNetType();
        Log.i(IRegistrationManager.LOG_TAG, "iface : " + intfNameByNetType + ",ipver:" + str3);
        if (dnsServersByNetType != null && intfNameByNetType != null) {
            this.mRegStackIf.sendDnsQuery(10, intfNameByNetType, "www.ims_rrc_refresh_dns.net", dnsServersByNetType, "HOST", "UDP", str3, 0);
        }
    }

    /* access modifiers changed from: package-private */
    public void tryRegister() {
        if (SimUtil.isDualIMS()) {
            for (ISimManager simSlotIndex : this.mSimManagers) {
                tryRegister(simSlotIndex.getSimSlotIndex());
            }
            return;
        }
        tryRegister(SimUtil.getActiveDataPhoneId());
    }

    public int findBestNetwork(int i, ImsProfile imsProfile, IRegistrationGovernor iRegistrationGovernor) {
        boolean isPdnConnected = isPdnConnected(imsProfile, i);
        PdnController pdnController = this.mPdnController;
        return RegistrationUtils.findBestNetwork(i, imsProfile, iRegistrationGovernor, isPdnConnected, pdnController, this.mVsm, pdnController.getNetworkState(i).getMobileDataNetworkType(), this.mContext);
    }

    /* access modifiers changed from: protected */
    public boolean onSimReady(boolean z, int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal;
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal2;
        Integer rcsConfVersion;
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onSimReady: absent=" + z);
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (!z) {
            NSDSSimEventManager.startIMSDeviceConfigService(this.mContext, iSimManager);
        }
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", i) == 1 || DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS_SWITCH, i) == 1) {
            if (!z) {
                IServiceModuleManager serviceModuleManager = this.mImsFramework.getServiceModuleManager();
                if (!loadImsProfile(i) || !serviceModuleManager.isLooperExist()) {
                    AsyncResult asyncResult = new AsyncResult((Object) null, Integer.valueOf(i), (Throwable) null);
                    RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                    registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(20, asyncResult), 1000);
                } else {
                    serviceModuleManager.serviceStartDeterminer(SlotBasedConfig.getInstance(i).getProfiles(), i);
                    serviceModuleManager.notifyImsSwitchUpdateToApp();
                    if (ConfigUtil.isRcsChn(SimUtil.getSimMno(i)) && (rcsConfVersion = this.mConfigModule.getRcsConfVersion(i)) != null && rcsConfVersion.intValue() == -2) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onSimReady: disableRcsByAcs for ConfigDBVer == -2");
                        this.mConfigModule.getAcsConfig(i).disableRcsByAcs(true);
                    }
                }
            } else {
                SlotBasedConfig.RegisterTaskList pendingRegistrationInternal3 = RegistrationUtils.getPendingRegistrationInternal(i);
                if (pendingRegistrationInternal3 != null) {
                    Iterator it = pendingRegistrationInternal3.iterator();
                    while (it.hasNext()) {
                        ((RegisterTask) it.next()).getGovernor().releaseThrottle(4);
                    }
                }
            }
            RegistrationUtils.setVoLTESupportProperty(z, i);
            ImsUtil.updateEmergencyCallDomain(this.mContext, i, getEmergencyProfile(i), iSimManager, this.mImsFramework.getString(i, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS"));
            if (!iSimManager.hasVsim()) {
                clearTask(i);
            }
            buildTask(i);
            RegistrationUtils.initRttMode(this.mContext);
            if (!ConfigUtil.hasAcsProfile(this.mContext, i, iSimManager)) {
                this.mImsFramework.getServiceModuleManager().notifyConfigured(false, i);
            }
            updateImsIconManagerStatus(i);
            Mno simMno = iSimManager.getSimMno();
            if (this.mlegacyPhoneCount == 0 && SlotBasedConfig.getInstance(i).getIconManager() == null && this.mCmcAccountManager.isSecondaryDevice()) {
                SlotBasedConfig.getInstance(i).createIconManager(this.mContext, this, this.mPdnController, simMno, i);
            }
            if (simMno.isOneOf(Mno.TMOUS, Mno.DISH) && this.mVsm != null) {
                this.mVsm.setRttMode(Settings.Secure.getInt(this.mContext.getContentResolver(), "preferred_rtt_mode", 0));
            }
            if (simMno == Mno.CMCC && iSimManager.isLabSimCard() && !z) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "Change SS domain to PS_ONLY_VOLTEREGIED");
                ImsUtil.updateSsDomain(this.mContext, i, "PS_ONLY_VOLTEREGIED");
            }
            if (!z) {
                notifySimMobilityStatusChanged(i, iSimManager);
                if (DeviceUtil.dimVolteMenuBySaMode(i) && this.mTelephonyManager.getPreferredNetworkType(SimUtil.getSubId(i)) >= 23) {
                    ImsConstants.SystemSettings.setVoiceCallType(this.mContext, 0, i);
                }
            }
            if (!z && OmcCode.isDCMOmcCode() && simMno == Mno.DOCOMO && SemSystemProperties.getInt("ro.telephony.default_network", 26) >= 23 && (pendingRegistrationInternal2 = RegistrationUtils.getPendingRegistrationInternal(i)) != null) {
                Iterator it2 = pendingRegistrationInternal2.iterator();
                while (it2.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it2.next();
                    if (registerTask != null && registerTask.getProfile().getPdnType() == 11) {
                        registerTask.getGovernor().notifyVoLteOnOffToRil(ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, i) == 0);
                    }
                }
            }
            if (!z && simMno == Mno.SKT && OmcCode.isKOROmcCode() && (pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i)) != null) {
                Iterator it3 = pendingRegistrationInternal.iterator();
                while (true) {
                    if (!it3.hasNext()) {
                        break;
                    }
                    RegisterTask registerTask2 = (RegisterTask) it3.next();
                    if (!registerTask2.isRcsOnly() && registerTask2.getProfile().getCmcType() == 0) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onSimReady: registerAllowedNetworkTypesListener");
                        registerTask2.getGovernor().registerAllowedNetworkTypesListener();
                        break;
                    }
                }
            }
            this.mRegStackIf.configure(i);
            tryRegister(i);
            return true;
        } else if (Mno.fromSalesCode(OmcCode.get()).isAus()) {
            this.mEventLog.logAndAdd(i, "Aus device, keep IMS Service Up for Emergency Call.");
            ImsUtil.updateEmergencyCallDomain(this.mContext, i, getEmergencyProfile(i), iSimManager, "PS");
            return true;
        } else {
            this.mEventLog.logAndAdd(i, "IMS is disabled. Do not load profiles");
            IMSLog.c(LogClass.REGI_IMS_OFF, i + ",IMS OFF");
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void updateImsIconManagerStatus(int i) {
        initImsIconManagerOrCreate(i);
        if (SimUtil.isSimActive(this.mContext, SimUtil.getOppositeSimSlot(i))) {
            initImsIconManagerOrCreate(SimUtil.getOppositeSimSlot(i));
        }
        updateImsIconManagerOrCreate(i);
        if (SimUtil.isSimActive(this.mContext, SimUtil.getOppositeSimSlot(i))) {
            updateImsIconManagerOrCreate(SimUtil.getOppositeSimSlot(i));
        }
    }

    private void initImsIconManagerOrCreate(int i) {
        SlotBasedConfig instance = SlotBasedConfig.getInstance(i);
        ImsIconManager iconManager = instance.getIconManager();
        if (RegistrationUtils.hasLoadedProfile(i)) {
            Mno mno = SimUtil.getMno(i);
            if (iconManager == null) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "initImsIconManagerOrCreate: added iconmanager.");
                instance.createIconManager(this.mContext, this, this.mPdnController, mno, i);
                return;
            }
            iconManager.initConfiguration(mno, i);
        }
    }

    private void updateImsIconManagerOrCreate(int i) {
        SlotBasedConfig instance = SlotBasedConfig.getInstance(i);
        ImsIconManager iconManager = instance.getIconManager();
        if (RegistrationUtils.hasLoadedProfile(i)) {
            Mno mno = SimUtil.getMno(i);
            if (iconManager == null) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateImsIconManagerOrCreate: added iconmanager.");
                instance.createIconManager(this.mContext, this, this.mPdnController, mno, i);
                return;
            }
            iconManager.updateRegistrationIcon();
        }
    }

    public void stopPdnConnectivity(int i, IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        iRegisterTask.getGovernor().resetPcscfList();
        iRegisterTask.getGovernor().resetPcoType();
        iRegisterTask.getGovernor().resetPdnFailureInfo();
        iRegisterTask.clearSuspended();
        iRegisterTask.clearSuspendedBySnapshot();
        iRegisterTask.setKeepPdn(false);
        this.mPdnController.stopPdnConnectivity(i, phoneId, iRegisterTask);
    }

    public void moveNextPcscf(int i, Message message) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "moveNextPcscf");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().hasEmergencySupport()) {
                    IRegistrationGovernor governor = registerTask.getGovernor();
                    int phoneId = registerTask.getPhoneId();
                    IMSLog.i(IRegistrationManager.LOG_TAG, phoneId, "moveNextPcscf: current [" + governor.getPcscfOrdinal() + "]");
                    registerTask.setResultMessage(message);
                    this.mMoveNextPcscf = true;
                    registerTask.setDeregiReason(11);
                    if (registerTask.getState() == RegistrationConstants.RegisterTaskState.EMERGENCY) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, registerTask.getPhoneId(), "moveNextPcscf: EMERGENCY state, try UA delete");
                        onDeregistered(registerTask, SipErrorBase.OK, 0, true, false);
                    } else if (registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, registerTask.getPhoneId(), "moveNextPcscf: REGISTERED state, local deregister");
                        tryDeregisterInternal(registerTask, true, true);
                    } else {
                        IMSLog.i(IRegistrationManager.LOG_TAG, registerTask.getPhoneId(), "It should not occur. ImsEmergencySession Issue!");
                        this.mMoveNextPcscf = false;
                        registerTask.getProfile().setUicclessEmergency(true);
                        registerTask.getGovernor().increasePcscfIdx();
                        this.mHandler.sendTryRegister(i);
                    }
                }
            }
        }
    }

    public void suspendRegister(boolean z, int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "suspendRegister:");
        if (z != SlotBasedConfig.getInstance(i).isSuspendedWhileIrat()) {
            SlotBasedConfig.getInstance(i).setSuspendWhileIrat(z);
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd(i, "suspendedByIrat : " + z);
            if (!z) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "Resume reRegister: mNetType = " + this.mNetEvtCtr.getNetType() + ", mWiFi = " + this.mNetEvtCtr.isWiFi());
                this.mHandler.removeMessages(136);
                if (this.mNetEvtCtr.isNwChanged()) {
                    this.mNetEvtCtr.setNwChanged(false);
                    Bundle bundle = new Bundle();
                    bundle.putInt("networkType", this.mNetEvtCtr.getNetType());
                    bundle.putInt("isWifiConnected", this.mNetEvtCtr.isWiFi() ? 1 : 0);
                    bundle.putInt("phoneId", i);
                    RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                    registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(3, bundle));
                }
            } else {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "Suspend reRegister");
                RegistrationManagerHandler registrationManagerHandler2 = this.mHandler;
                registrationManagerHandler2.sendMessageDelayed(registrationManagerHandler2.obtainMessage(136, i, 0, (Object) null), 300000);
            }
            SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
            if (pendingRegistrationInternal != null) {
                Iterator it = pendingRegistrationInternal.iterator();
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    if (z) {
                        registerTask.suspendByIrat();
                    } else {
                        registerTask.resumeByIrat();
                    }
                }
            }
        }
    }

    public boolean getCsfbSupported(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "getCsfbSupported:");
        NetworkEvent networkEvent = getNetworkEvent(i);
        if (networkEvent == null) {
            return false;
        }
        if (NetworkUtil.is3gppPsVoiceNetwork(networkEvent.network)) {
            boolean z = networkEvent.csOutOfService;
            boolean isPsOnlyReg = this.mPdnController.isPsOnlyReg(i);
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "isPsOnlyReg : " + isPsOnlyReg + " mEmmCause = " + getEmmCause());
            if (this.mEmmCause == 22) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "Support for EMM Cause 22");
                return true;
            } else if (z || isPsOnlyReg) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "CS OOS or CSFB not supported.");
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onEmergencyReady(int i) {
        Log.i(IRegistrationManager.LOG_TAG, "onEmergencyReady:");
        RegisterTask registerTask = getRegisterTask(i);
        if (registerTask != null) {
            registerTask.setState(RegistrationConstants.RegisterTaskState.EMERGENCY);
            if (registerTask.getResultMessage() != null) {
                registerTask.getResultMessage().sendToTarget();
                registerTask.setResultMessage((Message) null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean loadImsProfile(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "loadImsProfile:");
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager == null) {
            IMSLog.e(IRegistrationManager.LOG_TAG, i, "loadImsProfile: no SIM loaded");
            return false;
        }
        String simMnoName = iSimManager.getSimMnoName();
        IMSLog.e(IRegistrationManager.LOG_TAG, i, "loadImsProfile : " + simMnoName);
        if (TextUtils.isEmpty(simMnoName)) {
            IMSLog.e(IRegistrationManager.LOG_TAG, i, "loadImsProfile: no SIM detected.");
            return false;
        }
        SlotBasedConfig.getInstance(i).clearProfiles();
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "loadImsProfile: mno: " + simMnoName);
        for (ImsProfile next : ImsProfileLoaderInternal.getProfileListWithMnoName(this.mContext, simMnoName, i)) {
            if (loademergencyprofileinvalidimpu(next, i, iSimManager.isISimDataValid())) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "loadImsProfile: Add profile: " + next.getName() + " - profile id[" + next.getId() + "]");
                SlotBasedConfig.getInstance(i).addProfile(next);
            }
        }
        String rcsProfileLoaderInternalWithFeature = ConfigUtil.getRcsProfileLoaderInternalWithFeature(this.mContext, simMnoName, i);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "loadImsProfile: mRcsProfile: " + rcsProfileLoaderInternalWithFeature);
        RcsUtils.DualRcs.refreshDualRcsReg(this.mContext);
        ImsUtil.notifyImsProfileLoaded(this.mContext, i);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean loademergencyprofileinvalidimpu(ImsProfile imsProfile, int i, boolean z) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "loademergencyprofileinvalidimpu:");
        if (Mno.fromName(imsProfile.getMnoName()) != Mno.BELL || z || imsProfile.hasEmergencySupport()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onImsProfileUpdated(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onImsProfileUpdated:");
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager == null) {
            this.mHandler.removeMessages(15);
            RegistrationManagerHandler registrationManagerHandler = this.mHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(15, i, 0, (Object) null), 100);
            return;
        }
        loadImsProfile(i);
        RegistrationUtils.setVoLTESupportProperty(iSimManager.hasNoSim(), i);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    registerTask.setReason("profile updated");
                    registerTask.setDeregiReason(29);
                    if (this.mTelephonyManager.getCallState(i) != 0) {
                        registerTask.setHasPendingDeregister(true);
                    } else {
                        tryDeregisterInternal(registerTask, false, false);
                    }
                }
                RegistrationUtils.replaceProfilesOnTask(registerTask);
            }
        }
        buildTask(i);
        this.mImsFramework.notifyImsReady(true, i);
        notifySimMobilityStatusChanged(i, iSimManager);
        RegistrationManagerHandler registrationManagerHandler2 = this.mHandler;
        registrationManagerHandler2.sendMessageDelayed(registrationManagerHandler2.obtainMessage(2, Integer.valueOf(i)), 500);
    }

    /* access modifiers changed from: package-private */
    public void onImsSwitchUpdated(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onImsSwitchUpdated:");
        this.mImsFramework.getServiceModuleManager().onImsSwitchUpdated(i);
        this.mRegStackIf.configure(i);
        this.mHandler.onConfigUpdated((String) null, i);
    }

    /* access modifiers changed from: package-private */
    public void onOwnCapabilitiesChanged(int i, Capabilities capabilities) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onOwnCapabilitiesChanged: capabilities=" + capabilities);
        Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.getPhoneId() == i && (!registerTask.getMno().isKor() || registerTask.isRcsOnly())) {
                registerTask.getGovernor().checkAcsPcscfListChange();
                if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED)) {
                    if (this.mHandler.hasMessages(139, Integer.valueOf(i))) {
                        if (!RcsUtils.isImsSingleRegiRequired(this.mContext, i) || this.mConfigModule.getRcsConfVersion(i).intValue() != 0) {
                            this.mHandler.removeMessages(139, Integer.valueOf(i));
                        } else {
                            IMSLog.i(IRegistrationManager.LOG_TAG, i, "onOwnCapabilitiesChanged: Postpone the update registration till next ACS complete event");
                            return;
                        }
                    }
                    registerTask.setReason("own capability changed : " + capabilities);
                    if (registerTask.getMno() != Mno.TMOUS || !ImsUtil.needForceRegiOrPublishForMmtelCallComposer(this.mContext, registerTask.getProfile(), i)) {
                        updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.CAPABILITY_CHANGED);
                    } else {
                        updateRegistration(registerTask, RegistrationConstants.UpdateRegiReason.CAPABILITY_CHANGED_FORCED);
                    }
                } else {
                    tryRegister(i);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x008a, code lost:
        r1 = r0.verify(r4, r8[0]);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean verifyCmcCertificate(java.security.cert.X509Certificate[] r8) {
        /*
            r7 = this;
            okhttp3.internal.tls.OkHostnameVerifier r0 = okhttp3.internal.tls.OkHostnameVerifier.INSTANCE
            int r1 = r7.getCmcLineSlotIndex()
            r2 = -1
            r3 = 0
            if (r1 == r2) goto L_0x0093
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r1 = r7.mCmcAccountManager
            boolean r1 = r1.isCmcActivated()
            if (r1 == 0) goto L_0x0093
            int r1 = r7.getCmcLineSlotIndex()
            int r2 = com.sec.internal.helper.SimUtil.getActiveDataPhoneId()
            if (r1 == r2) goto L_0x0093
            int r1 = com.sec.internal.helper.SimUtil.getPhoneCount()
            r2 = 1
            if (r1 <= r2) goto L_0x0093
            int r1 = r7.getCmcLineSlotIndex()
            java.util.List r7 = r7.getPendingRegistration(r1)
            if (r7 != 0) goto L_0x002e
            return r3
        L_0x002e:
            java.util.Iterator r7 = r7.iterator()
            r1 = r3
        L_0x0033:
            boolean r2 = r7.hasNext()
            if (r2 == 0) goto L_0x0092
            java.lang.Object r2 = r7.next()
            com.sec.internal.interfaces.ims.core.IRegisterTask r2 = (com.sec.internal.interfaces.ims.core.IRegisterTask) r2
            com.sec.ims.settings.ImsProfile r4 = r2.getProfile()
            boolean r4 = com.sec.internal.ims.core.RegistrationUtils.isCmcProfile(r4)
            if (r4 == 0) goto L_0x0033
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r2.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERING
            if (r4 == r5) goto L_0x0059
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r4 = r2.getState()
            com.sec.internal.constants.ims.core.RegistrationConstants$RegisterTaskState r5 = com.sec.internal.constants.ims.core.RegistrationConstants.RegisterTaskState.REGISTERED
            if (r4 != r5) goto L_0x0033
        L_0x0059:
            java.lang.String r4 = r2.getPcscfHostname()
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Checking task: "
            r5.append(r6)
            com.sec.ims.settings.ImsProfile r2 = r2.getProfile()
            java.lang.String r2 = r2.getName()
            r5.append(r2)
            java.lang.String r2 = " / "
            r5.append(r2)
            r5.append(r4)
            java.lang.String r2 = r5.toString()
            java.lang.String r5 = "RegiMgr"
            android.util.Log.i(r5, r2)
            boolean r2 = android.text.TextUtils.isEmpty(r4)
            if (r2 == 0) goto L_0x008a
            goto L_0x0033
        L_0x008a:
            r1 = r8[r3]
            boolean r1 = r0.verify((java.lang.String) r4, (java.security.cert.X509Certificate) r1)
            if (r1 == 0) goto L_0x0033
        L_0x0092:
            r3 = r1
        L_0x0093:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerBase.verifyCmcCertificate(java.security.cert.X509Certificate[]):boolean");
    }

    /* access modifiers changed from: package-private */
    public void onSimRefresh(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onSimRefresh:");
        logTask();
        RcsUtils.DualRcs.refreshDualRcsReg(this.mContext);
        if (this.mTelephonyManager.getSimState(i) == 1) {
            updateImsIconManagerStatus(i);
        }
        if (!this.mHandler.hasMessages(42)) {
            RegistrationManagerHandler registrationManagerHandler = this.mHandler;
            registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(42, Integer.valueOf(i)), 10000);
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                registerTask.getGovernor().releaseThrottle(0);
                RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERING;
                RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.REGISTERED;
                if (registerTask.isOneOf(registerTaskState, registerTaskState2, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, "De-Register would be called by RIL(or timeout).");
                    AsyncResult asyncResult = new AsyncResult((Object) null, Integer.valueOf(registerTask.getPhoneId()), (Throwable) null);
                    RegistrationManagerHandler registrationManagerHandler2 = this.mHandler;
                    registrationManagerHandler2.sendMessageDelayed(registrationManagerHandler2.obtainMessage(36, asyncResult), 600);
                    if (RegistrationUtils.isCmcProfile(registerTask.getProfile()) && registerTask.getRegistrationRat() == 18 && registerTask.isOneOf(registerTaskState, registerTaskState2)) {
                        registerTask.setDeregiReason(25);
                        IMSLog.i(IRegistrationManager.LOG_TAG, i, "CMC deregister explicitly on WiFi");
                        tryDeregisterInternal(registerTask, false, false);
                    }
                    if (registerTask.getMno().isKor() && !registerTask.isRcsOnly() && !RegistrationUtils.isCmcProfile(registerTask.getProfile()) && TelephonyManager.getDefault().getSimState() == 1 && registerTask.getState() == registerTaskState2) {
                        registerTask.setDeregiReason(25);
                        IMSLog.i(IRegistrationManager.LOG_TAG, i, "De-Register is called right away to send SIP explicitly by sim absent event.");
                        tryDeregisterInternal(registerTask, false, false);
                        return;
                    }
                    return;
                } else if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.CONFIGURED)) {
                    if (registerTask.getMno() == Mno.RJIL) {
                        IMSLog.e(IRegistrationManager.LOG_TAG, i, "stop auto configuration using config module");
                        registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    }
                } else if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, "connecting task Stop PDN by sim refresh event.");
                    stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                    registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
            }
        }
        this.mEventLog.logAndAdd(i, "onSimRefresh: Reset SIM-related configuration.");
        clearTask(i);
        SlotBasedConfig.getInstance(i).clear();
        UriGeneratorFactory instance = UriGeneratorFactory.getInstance();
        for (UriGenerator.URIServiceType removeByPhoneId : UriGenerator.URIServiceType.values()) {
            instance.removeByPhoneId(i, removeByPhoneId);
        }
        if (this.mHandler.hasMessages(42)) {
            this.mHandler.removeMessages(42);
        }
        ImsUtil.updateEmergencyCallDomain(this.mContext, i, getEmergencyProfile(i), this.mSimManagers.get(i), this.mImsFramework.getString(i, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS"));
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "onSimRefresh: reset NetworkEvent");
        this.mPdnController.resetNetworkState(i);
        this.mPdnController.registerTelephonyCallback(i);
        ICmcAccountManager iCmcAccountManager = this.mCmcAccountManager;
        if (iCmcAccountManager != null) {
            iCmcAccountManager.onSimRefresh(i);
        }
    }

    /* access modifiers changed from: package-private */
    public void onActiveDataSubscriptionChanged() {
        RcsUtils.DualRcs.refreshDualRcsReg(this.mContext);
        if (SimUtil.isDualIMS()) {
            handleAdsChangeOnDualIms();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleAdsChangeOnDualIms() {
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        updateRegistration(activeDataPhoneId, RegistrationConstants.UpdateRegiReason.ADS_CHANGED);
        Iterator it = SlotBasedConfig.getInstance(activeDataPhoneId).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            RegisterTask registerTask = (RegisterTask) it.next();
            if (registerTask.getPdnType() == 0 && registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING)) {
                IMSLog.i(IRegistrationManager.LOG_TAG, activeDataPhoneId, "onActiveDataSubscriptionChanged: stopPdnConnectivity");
                stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                tryRegister(activeDataPhoneId);
            }
        }
        int oppositeSimSlot = SimUtil.getOppositeSimSlot(activeDataPhoneId);
        Iterator it2 = SlotBasedConfig.getInstance(oppositeSimSlot).getRegistrationTasks().iterator();
        while (it2.hasNext()) {
            RegisterTask registerTask2 = (RegisterTask) it2.next();
            if (RegistrationUtils.isCmcProfile(registerTask2.getProfile()) && registerTask2.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && (registerTask2.getProfile().getCmcType() == 2 || registerTask2.getRegistrationRat() != 18)) {
                registerTask2.setReason("ADS change");
                registerTask2.setDeregiReason(35);
                tryDeregisterInternal(registerTask2, true, false);
                IMSLog.i(IRegistrationManager.LOG_TAG, oppositeSimSlot, "onActiveDataSubscriptionChanged: Cmc deregister");
            } else if (registerTask2.getPdnType() == 0 && registerTask2.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING)) {
                IMSLog.i(IRegistrationManager.LOG_TAG, oppositeSimSlot, "onActiveDataSubscriptionChanged: stopPdnConnectivity");
                stopPdnConnectivity(registerTask2.getPdnType(), registerTask2);
                registerTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
                tryRegister(oppositeSimSlot);
            }
            if (registerTask2.getPdnType() == 11) {
                registerTask2.getGovernor().onAdsChanged(activeDataPhoneId);
            }
        }
        if (!RcsUtils.DualRcs.isRegAllowed(this.mContext, oppositeSimSlot)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, oppositeSimSlot, "ADS set to other SIM, dual rcs not supported, dereg previous ads rcs.");
            Iterator it3 = SlotBasedConfig.getInstance(oppositeSimSlot).getRegistrationTasks().iterator();
            while (it3.hasNext()) {
                RegisterTask registerTask3 = (RegisterTask) it3.next();
                if (registerTask3.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED) && registerTask3.isRcsOnly()) {
                    registerTask3.setReason("ADS change");
                    updateRegistration(registerTask3, RegistrationConstants.UpdateRegiReason.ADS_CHANGED);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDelayedDeregister(RegisterTask registerTask) {
        this.mHandler.removeMessages(128);
        registerTask.getGovernor().runDelayedDeregister();
    }

    /* access modifiers changed from: protected */
    public void notifyImsNotAvailable(RegisterTask registerTask, boolean z, boolean z2) {
        ImsRegistrationError imsRegistrationError;
        if (this.mCallState != 0) {
            Log.i(IRegistrationManager.LOG_TAG, "ignore notifyImsNotAvailable in call");
        } else if ((!registerTask.getMno().isKor() && registerTask.getMno() != Mno.DOCOMO) || registerTask.mGovernor.needImsNotAvailable()) {
            boolean z3 = false;
            if (z2) {
                this.mEventLog.logAndAdd("notifyImsNotAvailable: Permanent blocked");
                imsRegistrationError = new ImsRegistrationError(0, "", 81, 33);
            } else {
                imsRegistrationError = new ImsRegistrationError(0, "", 72, 32);
            }
            if (!SlotBasedConfig.getInstance(registerTask.getPhoneId()).isNotifiedImsNotAvailable() || z) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                StringBuilder sb = new StringBuilder();
                sb.append("notifyImsNotAvailable: UserAgent: ");
                sb.append(registerTask.mObject == null ? "null" : "exist");
                sb.append(", force: ");
                sb.append(z);
                sb.append(", task: ");
                sb.append(registerTask.getState());
                sb.append(", reason: ");
                sb.append(registerTask.getNotAvailableReason());
                simpleEventLog.logAndAdd(sb.toString());
                registerTask.getGovernor().stopTimsTimer(RegistrationConstants.REASON_IMS_NOT_AVAILABLE);
                ImsRegistration build = ImsRegistration.getBuilder().setHandle(-1).setImsProfile(new ImsProfile(registerTask.getProfile())).setServices(registerTask.getProfile().getAllServiceSetFromAllNetwork()).setEpdgStatus(this.mPdnController.isEpdgConnected(registerTask.getPhoneId())).setPdnType(registerTask.getPdnType()).setUuid(getUuid(registerTask.getPhoneId(), registerTask.getProfile())).setInstanceId(getInstanceId(registerTask.getPhoneId(), registerTask.getPdnType(), registerTask.getProfile())).setNetwork(registerTask.getNetworkConnected()).setRegiRat(SlotBasedConfig.getInstance(registerTask.getPhoneId()).getNetworkEvent().network).setPhoneId(registerTask.getPhoneId()).build();
                if (registerTask.getUserAgent() == null || registerTask.getNotAvailableReason() == 1) {
                    notifyImsRegistration(build, false, registerTask, imsRegistrationError);
                    makeThrottleforImsNotAvailable(registerTask);
                }
                if (registerTask.getUserAgent() != null) {
                    if (registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED) {
                        registerTask.setDeregiReason(72);
                        makeThrottleforImsNotAvailable(registerTask);
                        if (registerTask.getNotAvailableReason() == 1) {
                            z3 = true;
                        }
                        tryDeregisterInternal(registerTask, true, z3);
                    } else if (registerTask.getNotAvailableReason() != 1) {
                        ImsRegistration imsRegistration = registerTask.getImsRegistration();
                        if (imsRegistration != null) {
                            build = imsRegistration;
                        }
                        notifyImsRegistration(build, false, registerTask, imsRegistrationError);
                        makeThrottleforImsNotAvailable(registerTask);
                    }
                }
                SlotBasedConfig.getInstance(registerTask.getPhoneId()).setNotifiedImsNotAvailable(true);
                registerTask.clearNotAvailableReason();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyImsNotAvailable(RegisterTask registerTask, boolean z) {
        notifyImsNotAvailable(registerTask, z, false);
    }

    /* access modifiers changed from: package-private */
    public void makeThrottleforImsNotAvailable(RegisterTask registerTask) {
        if (registerTask.getGovernor().needImsNotAvailable() && !this.mPdnController.isEpsOnlyReg(registerTask.getPhoneId())) {
            int i = SemSystemProperties.getInt(ImsConstants.SystemProperties.LTE_VOICE_STATUS, -1);
            IMSLog.i(IRegistrationManager.LOG_TAG, "makeThrottleforImsNotAvailable: lteVoiceStatus = " + i);
            if (i == 1) {
                this.mEventLog.logAndAdd("makeThrottleforImsNotAvailable, combined with csfb supported");
                registerTask.getGovernor().makeThrottle();
                registerTask.getGovernor().throttleforImsNotAvailable();
            }
        }
    }

    public void updateRegistrationBySSAC(int i, boolean z) {
        int i2;
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateRegistrationBySSAC:[" + i + "]");
        ISimManager iSimManager = this.mSimManagers.get(i);
        if (iSimManager != null && iSimManager.getSimMno() == Mno.VZW && !Boolean.parseBoolean(SemSystemProperties.get("ro.ril.svlte1x"))) {
            boolean isSsacEnabled = SlotBasedConfig.getInstance(i).isSsacEnabled();
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateRegistrationBySSAC : " + isSsacEnabled + " -> " + z);
            if (!z) {
                this.mHandler.removeMessages(121, Integer.valueOf(i));
            }
            if (isSsacEnabled != z) {
                this.mHandler.removeMessages(121, Integer.valueOf(i));
                if (z) {
                    Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            i2 = 0;
                            break;
                        }
                        RegisterTask registerTask = (RegisterTask) it.next();
                        if (ImsProfile.hasVolteService(registerTask.getProfile()) && registerTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERED && NetworkUtil.is3gppPsVoiceNetwork(registerTask.getRegistrationRat())) {
                            i2 = DmConfigHelper.readInt(this.mContext, "tvolte_hys_timer", 60, i).intValue() * 1000;
                            break;
                        }
                    }
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateRegistrationBySSAC : registration will be started after " + i2 + "ms.");
                    RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                    registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(121, 1, 0, Integer.valueOf(i)), (long) i2);
                    return;
                }
                RegistrationManagerHandler registrationManagerHandler2 = this.mHandler;
                registrationManagerHandler2.sendMessage(registrationManagerHandler2.obtainMessage(121, 0, 0, Integer.valueOf(i)));
            }
        }
    }

    public void updateTelephonyCallStatus(int i, int i2) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateTelephonyCallStatus: " + i2);
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(33, i, i2, (Object) null));
    }

    public boolean isSelfActivationRequired(int i) {
        boolean z;
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (((RegisterTask) it.next()).getGovernor().getPcoType() == RegistrationGovernor.PcoType.PCO_SELF_ACTIVATION) {
                        z = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        z = false;
        Log.d(IRegistrationManager.LOG_TAG, "isSelfActivationRequired = " + z);
        return z;
    }

    public void startEmergencyRegistration(int i, Message message, int i2) {
        this.mPdnController.setEmergencyQualifiedNetowrk(i, i2);
        startEmergencyRegistrationInternal(i, message);
    }

    public void startEmergencyRegistration(int i, Message message) {
        startEmergencyRegistrationInternal(i, message);
    }

    private void startEmergencyRegistrationInternal(int i, Message message) {
        ImsProfile imsProfile;
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "startEmergencyRegistration:");
        ISimManager iSimManager = this.mSimManagers.get(i);
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (iSimManager != null && pendingRegistrationInternal != null) {
            if (this.mHandler.hasMessages(10)) {
                this.mHasSilentE911 = message;
                this.mPhoneIdForSilentE911 = i;
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "startEmergencyRegistration: retry after previous stopEmergencyRegistration");
                return;
            }
            if (SlotBasedConfig.getInstance(i).getIconManager() != null) {
                SlotBasedConfig.getInstance(i).getIconManager().setDuringEmergencyCall(true);
            }
            Iterator it = pendingRegistrationInternal.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().hasEmergencySupport()) {
                    IMSLog.i(IRegistrationManager.LOG_TAG, i, "startEmergencyRegistration: EmergencyRegistration state=" + registerTask.mState);
                    if (registerTask.getMno().isAus() && (imsProfile = this.mAuEmergencyProfile.get(i)) != null && imsProfile.getId() != registerTask.getProfile().getId()) {
                        IMSLog.i(IRegistrationManager.LOG_TAG, i, "Aus Emergency case, remove emergency task if old and new profile ID are different.");
                        stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                        this.mRegStackIf.removeUserAgent(registerTask);
                        pendingRegistrationInternal.remove(registerTask);
                    } else if (registerTask.getMno() != Mno.KDDI || !registerTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                        RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.REGISTERED;
                        RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.EMERGENCY;
                        if (registerTask.isOneOf(registerTaskState, registerTaskState2)) {
                            if (registerTask.mState == registerTaskState2 && registerTask.mMno == Mno.GENERIC_IR92 && registerTask.getUserAgent() == null) {
                                Log.d(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: Trigger New Register with same task");
                                registerTask.setState(RegistrationConstants.RegisterTaskState.CONNECTED);
                                registerTask.setResultMessage(message);
                                pendingRegistrationInternal.remove(registerTask);
                                this.mHandler.requestTryEmergencyRegister(registerTask);
                                return;
                            }
                            Log.d(IRegistrationManager.LOG_TAG, "startEmergencyRegistration: already registered.");
                            message.sendToTarget();
                            return;
                        } else if (registerTask.getState() == RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                            IMSLog.i(IRegistrationManager.LOG_TAG, i, "startEmergencyRegistration: DeRegistering Mode. Deregister current and start new registration.");
                            if (this.mHandler.hasMessages(107, registerTask)) {
                                this.mHandler.removeMessages(107, registerTask);
                                RegistrationManagerHandler registrationManagerHandler = this.mHandler;
                                registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(107, registerTask));
                            } else {
                                return;
                            }
                        } else {
                            if (registerTask.getResultMessage() != null) {
                                registerTask.getResultMessage().sendToTarget();
                            }
                            registerTask.setResultMessage(message);
                            return;
                        }
                    } else {
                        IMSLog.i(IRegistrationManager.LOG_TAG, i, "remove emergency pending RegiTask.");
                        pendingRegistrationInternal.remove(registerTask);
                    }
                }
            }
            if (iSimManager.getDevMno().isAus()) {
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "startEmergencyRegistration: refresh Emergency profile...");
                refreshAuEmergencyProfile(i);
            }
            ImsProfile emergencyProfile = getEmergencyProfile(i);
            if (emergencyProfile != null) {
                tryEmergencyRegister(i, emergencyProfile, message, iSimManager.hasNoSim());
            }
        }
    }

    public void refreshAuEmergencyProfile(int i) {
        this.mAuEmergencyProfile.delete(i);
    }

    public void stopEmergencyRegistration(int i) {
        ImsProfile imsProfile;
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "stopEmergencyRegistration:");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().hasEmergencySupport()) {
                    imsProfile = registerTask.mProfile;
                    break;
                }
            }
        }
        imsProfile = null;
        if (imsProfile != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("id", imsProfile.getId());
            bundle.putBoolean("explicitDeregi", true);
            bundle.putInt("phoneId", i);
            RegistrationManagerHandler registrationManagerHandler = this.mHandler;
            registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(10, bundle));
            if (!this.mSimManagers.get(i).hasNoSim()) {
                imsProfile.setUicclessEmergency(false);
            }
        }
        if (SlotBasedConfig.getInstance(i).getIconManager() != null) {
            SlotBasedConfig.getInstance(i).getIconManager().setDuringEmergencyCall(false);
        }
        if (imsProfile == null) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "stopEmergencyRegistration: profile not found.");
            startSilentEmergency();
        }
    }

    public void stopEmergencyPdnOnly(int i) {
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "stopEmergencyPdnOnly:");
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (registerTask.getProfile().hasEmergencySupport()) {
                    stopPdnConnectivity(registerTask.getPdnType(), registerTask);
                    registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
            }
        }
    }

    public void setOwnCapabilities(int i, Capabilities capabilities) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(31, i, 0, capabilities));
    }

    /* access modifiers changed from: protected */
    public void updateGeolocation(LocationInfo locationInfo, boolean z) {
        ISmsServiceModule smsServiceModule;
        IGeolocationController iGeolocationController;
        for (int i = 0; i < this.mSimManagers.size(); i++) {
            Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                int supportedGeolocationPhase = registerTask.getProfile().getSupportedGeolocationPhase();
                IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateGeolocation: " + registerTask.getProfile().getName() + ", geoLocPhase: " + supportedGeolocationPhase);
                if (getNetworkEvent(i) != null) {
                    if (supportedGeolocationPhase >= 2) {
                        this.mRegStackIf.updateGeolocation(registerTask, locationInfo);
                    }
                    if (registerTask.getGovernor().onUpdateGeolocation(locationInfo) && (iGeolocationController = this.mGeolocationCon) != null) {
                        iGeolocationController.stopPeriodicLocationUpdate(i);
                    }
                }
            }
        }
        Log.i(IRegistrationManager.LOG_TAG, "updateGeolocation: CountryCode : " + locationInfo.mCountry + ", silentUpdate : " + z);
        IVolteServiceModule iVolteServiceModule = this.mVsm;
        if (iVolteServiceModule != null && !z) {
            iVolteServiceModule.onUpdateGeolocation();
        }
        IServiceModuleManager serviceModuleManager = this.mImsFramework.getServiceModuleManager();
        if (serviceModuleManager != null && (smsServiceModule = serviceModuleManager.getSmsServiceModule()) != null) {
            smsServiceModule.onUpdateGeolocation();
        }
    }

    /* access modifiers changed from: protected */
    public void updateRat(RegisterTask registerTask, int i) {
        this.mRegStackIf.updateRat(registerTask, i);
    }

    /* access modifiers changed from: protected */
    public void updateTimeInPlani(int i, boolean z) {
        if (z) {
            this.mRegStackIf.removePreviousLastPani(i);
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                this.mRegStackIf.updateTimeInPlani(i, ((RegisterTask) it.next()).getProfile());
            }
        }
    }

    public void handleInactiveCiaOnMobileConnected(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (!registerTask.isRcsOnly()) {
                    this.mRegStackIf.handleInactiveCiaOnMobileConnected(i, registerTask);
                }
            }
        }
    }

    public void handleInactiveCiaOnMobileDisconnected(int i) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = RegistrationUtils.getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (!registerTask.isRcsOnly()) {
                    this.mRegStackIf.handleInactiveCiaOnMobileDisconnected(i, registerTask);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyGeolocationUpdate(LocationInfo locationInfo, boolean z) {
        IMSLog.i(IRegistrationManager.LOG_TAG, "notifyGeolocationUpdate, silentUpdate = " + z);
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(51, z ? 1 : 0, 0, locationInfo));
        if (!z) {
            this.mHandler.sendEmptyMessage(40);
        }
    }

    /* access modifiers changed from: protected */
    public void sendNrDisableDuringEpdgCall(int i) {
        IVolteServiceModule volteServiceModule = this.mImsFramework.getServiceModuleManager().getVolteServiceModule();
        if (volteServiceModule != null && volteServiceModule.getEpdgCallCount(i) > 0 && DeviceUtil.isSupportNrMode(this.mTelephonyManager, i)) {
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "sendNrDisableDuringEpdgCall, set semSetNrMode: 4");
            this.mTelephonyManager.semSetNrMode(i, 4);
        }
    }

    /* access modifiers changed from: protected */
    public void onFlightModeChanged(boolean z) {
        if (z) {
            for (int i = 0; i < this.mSimManagers.size(); i++) {
                Iterator it = SlotBasedConfig.getInstance(i).getRegistrationTasks().iterator();
                while (it.hasNext()) {
                    IRegisterTask iRegisterTask = (IRegisterTask) it.next();
                    if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.REGISTERING) {
                        this.mRegStackIf.removeUserAgent(iRegisterTask);
                        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    }
                    iRegisterTask.clearSuspended();
                }
                suspendRegister(false, i);
            }
            return;
        }
        this.mSimManagers.forEach(new RegistrationManagerBase$$ExternalSyntheticLambda0(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onFlightModeChanged$0(ISimManager iSimManager) {
        int simSlotIndex = iSimManager.getSimSlotIndex();
        if (iSimManager.getSimMno() == Mno.VODAFONE_UK || iSimManager.getSimMno() == Mno.ORANGE_SPAIN) {
            sendNrDisableDuringEpdgCall(simSlotIndex);
        }
        SlotBasedConfig.getInstance(simSlotIndex).setNotifiedImsNotAvailable(false);
        if (iSimManager.getSimMno().isOneOf(Mno.VELCOM_BY, Mno.SBERBANK_RUSSIA, Mno.MTS_RUSSIA, Mno.MEGAFON_RUSSIA, Mno.BEELINE_RUSSIA, Mno.TMOBILE)) {
            updateTimeInPlani(simSlotIndex, true);
        }
        if (iSimManager.getSimMno().isKor()) {
            Iterator it = SlotBasedConfig.getInstance(simSlotIndex).getRegistrationTasks().iterator();
            while (it.hasNext()) {
                IRegisterTask iRegisterTask = (IRegisterTask) it.next();
                if (iRegisterTask.getGovernor().isMobilePreferredForRcs() && NetworkUtil.isMobileDataOn(this.mContext) && NetworkUtil.isMobileDataPressed(this.mContext) && this.mPdnController.isWifiConnected()) {
                    this.mNetEvtCtr.isPreferredPdnForRCSRegister((RegisterTask) iRegisterTask, simSlotIndex, true);
                }
            }
        }
        IMSLogTimer.setLatchStartTime(simSlotIndex);
        tryRegister(iSimManager.getSimSlotIndex());
    }

    public void setSilentLogEnabled(boolean z) {
        this.mRegStackIf.setSilentLogEnabled(z);
    }

    public void onDnsResponse(List<String> list, int i, int i2) {
        IMSLog.d(IRegistrationManager.LOG_TAG, i2, "onDnsResponse, ipAddr size " + list.size() + ", port " + i);
        Iterator it = SlotBasedConfig.getInstance(i2).getRegistrationTasks().iterator();
        while (it.hasNext()) {
            IRegisterTask iRegisterTask = (IRegisterTask) it.next();
            if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.RESOLVING) {
                if (!iRegisterTask.getMno().isKor() || iRegisterTask.isRcsOnly()) {
                    iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVED);
                    list = iRegisterTask.getGovernor().checkValidPcscfIp(list);
                    if (list.isEmpty() || i > 65535) {
                        this.mAresLookupRequired = false;
                    } else {
                        iRegisterTask.getGovernor().updatePcscfIpList(list);
                        iRegisterTask.getProfile().setSipPort(i);
                        iRegisterTask.setDnsQueryRetryCount(0);
                    }
                    if (iRegisterTask.isRcsOnly() && list.isEmpty()) {
                        String acsServerType = ConfigUtil.getAcsServerType(iRegisterTask.getPhoneId());
                        if (((ConfigUtil.isRcsEur(iRegisterTask.getMno()) || iRegisterTask.getMno().isKor() || ConfigUtil.isRcsChn(iRegisterTask.getMno())) && iRegisterTask.getRegistrationRat() == 18) || ImsConstants.RCS_AS.JIBE.equals(acsServerType)) {
                            int dnsQueryRetryCount = iRegisterTask.getDnsQueryRetryCount();
                            IMSLog.s(IRegistrationManager.LOG_TAG, "onDnsResponse: retrycount=" + dnsQueryRetryCount);
                            if (dnsQueryRetryCount <= 5) {
                                iRegisterTask.setDnsQueryRetryCount(dnsQueryRetryCount + 1);
                                this.mHandler.sendTryRegister(iRegisterTask.getPhoneId(), 10000);
                            }
                        }
                    }
                    if (!list.isEmpty()) {
                        this.mHandler.sendTryRegister(iRegisterTask.getPhoneId());
                    }
                } else {
                    IMSLog.i(IRegistrationManager.LOG_TAG, i2, "onDnsResponse: profile not match!! " + iRegisterTask.getProfile().getName() + " port: " + i);
                }
            }
        }
    }

    public void finishThreadForGettingHostAddress(Thread thread, int i) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, "getHostAddress time out or failed");
        RegisterTask registerTask = getRegisterTask(i);
        if (registerTask != null && registerTask.getState() == RegistrationConstants.RegisterTaskState.RESOLVING) {
            registerTask.setState(RegistrationConstants.RegisterTaskState.IDLE);
            int dnsQueryRetryCount = registerTask.getDnsQueryRetryCount();
            IMSLog.s(IRegistrationManager.LOG_TAG, "onDnsResponse: retrycount=" + dnsQueryRetryCount);
            if (dnsQueryRetryCount <= 5) {
                registerTask.setDnsQueryRetryCount(dnsQueryRetryCount + 1);
                this.mHandler.sendTryRegister(registerTask.getPhoneId(), 10000);
            }
        }
    }

    public void registerDmListener(IImsDmConfigListener iImsDmConfigListener) {
        this.mHandler.registerDmListener(iImsDmConfigListener);
    }

    public void unregisterDmListener(IImsDmConfigListener iImsDmConfigListener) {
        this.mHandler.unregisterDmListener(iImsDmConfigListener);
    }

    public void setRegiConfig(int i) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessage(registrationManagerHandler.obtainMessage(RegistrationEvents.EVENT_UPDATE_REGI_CONFIG, Integer.valueOf(i)));
    }

    public void updateRegiConfig(int i) {
        this.mRcsPolicyManager.updateRegiConfig(i);
    }

    private boolean isHoEnable(int i) {
        int hoEnable = SlotBasedConfig.getInstance(i).getHoEnable();
        boolean z = hoEnable;
        if (hoEnable == -1) {
            boolean z2 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "EPDGHANDOVERENABLE", false);
            setHoEnable(i, z2);
            z = z2;
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "isHoEnable: " + ((int) z));
        if (z == 1) {
            return true;
        }
        return false;
    }

    private void setHoEnable(int i, boolean z) {
        SlotBasedConfig.getInstance(i).setHoEnable(z);
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "EPDGHANDOVERENABLE", z);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "setHoEnable: " + z);
    }

    private boolean isOnlyEpsFallback(int i) {
        int onlyEpsFallback = SlotBasedConfig.getInstance(i).getOnlyEpsFallback();
        boolean z = onlyEpsFallback;
        if (onlyEpsFallback == -1) {
            boolean z2 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "ONLYEPSFALLBACK", false);
            setOnlyEpsFallback(i, z2);
            z = z2;
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "isOnlyEpsFallback: " + ((int) z));
        if (z == 1) {
            return true;
        }
        return false;
    }

    private void setOnlyEpsFallback(int i, boolean z) {
        SlotBasedConfig.getInstance(i).setOnlyEpsFallback(z);
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "ONLYEPSFALLBACK", z);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "setOnlyEpsFallback: " + z);
    }

    public boolean isNrPreferredMode(int i) {
        int nrPreferredMode = SlotBasedConfig.getInstance(i).getNrPreferredMode();
        boolean z = nrPreferredMode;
        if (nrPreferredMode == -1) {
            boolean z2 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "NRPREFERREDMODE", false);
            setNrPreferredMode(i, z2);
            z = z2;
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "nrPreferredMode: " + ((int) z));
        if (z == 1) {
            return true;
        }
        return false;
    }

    private void setNrPreferredMode(int i, boolean z) {
        SlotBasedConfig.getInstance(i).setNrPreferredMode(z);
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "NRPREFERREDMODE", z);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "setNrPreferredMode: " + z);
    }

    public boolean isNrSaMode(int i) {
        int nrSaMode = SlotBasedConfig.getInstance(i).getNrSaMode();
        boolean z = nrSaMode;
        if (nrSaMode == -1) {
            boolean z2 = ImsSharedPrefHelper.getBoolean(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "NRSAMODE", true);
            setNrSaMode(i, z2);
            z = z2;
        }
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "nrSaMode : " + ((int) z));
        if (z == 1) {
            return true;
        }
        return false;
    }

    private void setNrSaMode(int i, boolean z) {
        SlotBasedConfig.getInstance(i).setNrSaMode(z);
        ImsSharedPrefHelper.save(i, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "NRSAMODE", z);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "setNrSaMode : " + z);
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0095  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ab  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c3 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00c4 A[ADDED_TO_REGION] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void supportVoWiFiDisable5GSA(int r10, boolean r11, boolean r12, boolean r13, boolean r14) {
        /*
            r9 = this;
            java.lang.String r0 = "mmtel"
            java.lang.String r1 = "RegiMgr"
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r2 = r9.mSimManagers
            java.lang.Object r2 = r2.get(r10)
            com.sec.internal.interfaces.ims.core.ISimManager r2 = (com.sec.internal.interfaces.ims.core.ISimManager) r2
            com.sec.internal.constants.Mno r2 = r2.getSimMno()
            com.sec.internal.constants.Mno r3 = com.sec.internal.constants.Mno.TMOUS
            if (r2 != r3) goto L_0x0015
            return
        L_0x0015:
            com.sec.internal.interfaces.ims.IImsFramework r2 = r9.mImsFramework
            java.lang.String r3 = "vowifi_5gsa_mode"
            java.lang.String r4 = "ENABLE"
            java.lang.String r2 = r2.getString(r10, r3, r4)
            boolean r3 = r4.equals(r2)
            java.lang.String r4 = "DEPRIORITIZE"
            r5 = 0
            if (r3 == 0) goto L_0x0045
            com.sec.internal.interfaces.ims.IImsFramework r2 = r9.mImsFramework
            java.lang.String r3 = "support_vowifi_deprioritize_nr5g"
            boolean r2 = r2.getBoolean(r10, r3, r5)
            com.sec.internal.interfaces.ims.IImsFramework r3 = r9.mImsFramework
            java.lang.String r6 = "support_disable_vowifi_5gsa"
            boolean r3 = r3.getBoolean(r10, r6, r5)
            if (r2 == 0) goto L_0x003f
            r2 = r4
            goto L_0x0045
        L_0x003f:
            if (r3 == 0) goto L_0x0044
            java.lang.String r2 = "DISABLE"
            goto L_0x0045
        L_0x0044:
            return
        L_0x0045:
            r3 = 1
            com.sec.internal.interfaces.ims.IImsFramework r6 = r9.mImsFramework     // Catch:{ RemoteException -> 0x006a }
            r7 = 20
            boolean r6 = r6.isServiceAvailable(r0, r7, r10)     // Catch:{ RemoteException -> 0x006a }
            if (r6 == 0) goto L_0x006f
            com.sec.internal.interfaces.ims.IImsFramework r6 = r9.mImsFramework     // Catch:{ RemoteException -> 0x006a }
            r7 = 18
            boolean r0 = r6.isServiceAvailable(r0, r7, r10)     // Catch:{ RemoteException -> 0x006a }
            if (r0 == 0) goto L_0x006f
            com.sec.internal.interfaces.ims.IImsFramework r0 = r9.mImsFramework     // Catch:{ RemoteException -> 0x006a }
            com.sec.internal.interfaces.ims.core.IWfcEpdgManager r0 = r0.getWfcEpdgManager()     // Catch:{ RemoteException -> 0x006a }
            int r0 = r0.getNrInterworkingMode(r10)     // Catch:{ RemoteException -> 0x006a }
            int r6 = com.sec.internal.constants.ims.ImsConstants.NrInterworking.FULL_SUPPORT     // Catch:{ RemoteException -> 0x006a }
            if (r0 == r6) goto L_0x006f
            r0 = r3
            goto L_0x0070
        L_0x006a:
            java.lang.String r0 = "isServiceAvailable RemoteException do nothing"
            android.util.Log.e(r1, r0)
        L_0x006f:
            r0 = r5
        L_0x0070:
            boolean r6 = r9.isOnlyEpsFallback(r10)
            boolean r7 = r9.isHoEnable(r10)
            boolean r8 = r9.isNrPreferredMode(r10)
            if (r0 == 0) goto L_0x0095
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "supportVoWiFiDisable5GSA: requires alwaysDisable5gsa(Nr interworking FULL_SUPPORT) "
            r6.append(r7)
            r6.append(r0)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r1, r10, r6)
            r6 = r5
            goto L_0x00a3
        L_0x0095:
            if (r6 == r11) goto L_0x009c
            r9.setOnlyEpsFallback(r10, r11)
            r6 = r3
            goto L_0x009d
        L_0x009c:
            r6 = r5
        L_0x009d:
            if (r7 == r12) goto L_0x00a3
            r9.setHoEnable(r10, r12)
            r6 = r3
        L_0x00a3:
            if (r8 == r13) goto L_0x00a9
            r9.setNrPreferredMode(r10, r13)
            r6 = r3
        L_0x00a9:
            if (r14 == 0) goto L_0x00ac
            r6 = r3
        L_0x00ac:
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r7 = "supportVoWiFiDisable5GSA: needToSendIpc : "
            r14.append(r7)
            r14.append(r6)
            java.lang.String r14 = r14.toString()
            com.sec.internal.log.IMSLog.i(r1, r10, r14)
            if (r6 != 0) goto L_0x00c4
            return
        L_0x00c4:
            if (r12 == 0) goto L_0x00c8
            if (r11 != 0) goto L_0x00ca
        L_0x00c8:
            if (r0 == 0) goto L_0x00d3
        L_0x00ca:
            if (r13 == 0) goto L_0x00d3
            boolean r9 = r9.isNrSaMode(r10)
            if (r9 == 0) goto L_0x00d3
            r5 = r3
        L_0x00d3:
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r9 = com.sec.internal.ims.core.RegistrationUtils.getPendingRegistrationInternal(r10)
            if (r9 == 0) goto L_0x010e
            java.util.Iterator r9 = r9.iterator()
        L_0x00dd:
            boolean r10 = r9.hasNext()
            if (r10 == 0) goto L_0x010e
            java.lang.Object r10 = r9.next()
            com.sec.internal.ims.core.RegisterTask r10 = (com.sec.internal.ims.core.RegisterTask) r10
            com.sec.internal.interfaces.ims.core.IUserAgent r11 = r10.getUserAgent()
            if (r11 == 0) goto L_0x00dd
            if (r5 == 0) goto L_0x0104
            com.sec.internal.interfaces.ims.core.IUserAgent r10 = r10.getUserAgent()
            boolean r11 = r4.equals(r2)
            if (r11 == 0) goto L_0x00fe
            int r11 = com.sec.internal.constants.ims.ImsConstants.NrSaMode.DEPRIORITIZE
            goto L_0x0100
        L_0x00fe:
            int r11 = com.sec.internal.constants.ims.ImsConstants.NrSaMode.DISABLE
        L_0x0100:
            r10.setVowifi5gsaMode(r11)
            goto L_0x00dd
        L_0x0104:
            com.sec.internal.interfaces.ims.core.IUserAgent r10 = r10.getUserAgent()
            int r11 = com.sec.internal.constants.ims.ImsConstants.NrSaMode.ENABLE
            r10.setVowifi5gsaMode(r11)
            goto L_0x00dd
        L_0x010e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationManagerBase.supportVoWiFiDisable5GSA(int, boolean, boolean, boolean, boolean):void");
    }

    public boolean isSupportVoWiFiDisable5GSA(int i) {
        if (!this.mSimManagers.get(i).getSimMno().isEmeasewaoce()) {
            return false;
        }
        if ("ENABLE".equals(this.mImsFramework.getString(i, GlobalSettingsConstants.Call.VOWIFI_5GSA_MODE, "ENABLE"))) {
            boolean z = this.mImsFramework.getBoolean(i, GlobalSettingsConstants.Call.SUPPORT_VOWIFI_DEPRIORITIZE_NR5G, false);
            boolean z2 = this.mImsFramework.getBoolean(i, GlobalSettingsConstants.Call.SUPPORT_DISABLE_VOWIFI_5GSA, false);
            if (!z && !z2) {
                return false;
            }
        }
        if (!isHoEnable(i) || !isOnlyEpsFallback(i) || !isNrPreferredMode(i)) {
            return false;
        }
        return true;
    }

    public void updateEpsFbInImsCall(int i) {
        boolean isHoEnable = isHoEnable(i);
        boolean z = getNetworkEvent(i).network != 20;
        boolean isNrPreferredMode = isNrPreferredMode(i);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateEpsFbInImsCall: onlyEpsFb = " + z);
        supportVoWiFiDisable5GSA(i, z, isHoEnable, isNrPreferredMode, false);
    }

    public void updateEpdgHandoverEnableChanged(int i, boolean z) {
        boolean isOnlyEpsFallback = isOnlyEpsFallback(i);
        boolean isNrPreferredMode = isNrPreferredMode(i);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateEpdgHandoverEnableChanged: onlyEpsFb = " + isOnlyEpsFallback + " , nrPreferredMode = " + isNrPreferredMode + " , hoEnable = " + z);
        supportVoWiFiDisable5GSA(i, isOnlyEpsFallback, z, isNrPreferredMode, false);
    }

    public void updateNrPreferredMode(int i, boolean z) {
        boolean isHoEnable = isHoEnable(i);
        boolean isOnlyEpsFallback = isOnlyEpsFallback(i);
        IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateNrPreferredMode: onlyEpsFb = " + isOnlyEpsFallback + " , nrPreferredMode = " + z + " , hoEnable = " + isHoEnable);
        supportVoWiFiDisable5GSA(i, isOnlyEpsFallback, isHoEnable, z, false);
    }

    public void updateNrSaMode(int i, boolean z) {
        boolean isHoEnable = isHoEnable(i);
        boolean isOnlyEpsFallback = isOnlyEpsFallback(i);
        boolean isNrPreferredMode = isNrPreferredMode(i);
        if (isNrSaMode(i) != z) {
            setNrSaMode(i, z);
            IMSLog.i(IRegistrationManager.LOG_TAG, i, "updateNrSaMode: onlyEpsFb = " + isOnlyEpsFallback + " , nrPreferredMode = " + isNrPreferredMode + " , hoEnable = " + isHoEnable + " , nrSaMode = " + z);
            supportVoWiFiDisable5GSA(i, isOnlyEpsFallback, isHoEnable, isNrPreferredMode, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void postponeUpdateRegistrationByDmaChange(int i) {
        RegistrationManagerHandler registrationManagerHandler = this.mHandler;
        registrationManagerHandler.sendMessageDelayed(registrationManagerHandler.obtainMessage(139, Integer.valueOf(i)), 30000);
    }

    public void updateEmcAttachAuth(int i, int i2) {
        SlotBasedConfig.getInstance(i).setEmcAttachAuth(i2);
    }

    public void updateVo5gIconStatus(int i, int i2) {
        if (SlotBasedConfig.getInstance(i).getIconManager() != null) {
            SlotBasedConfig.getInstance(i).getIconManager().setVo5gIcon(i2);
        }
    }

    public void handleE911RegiTimeOut(IRegisterTask iRegisterTask) {
        Log.i(IRegistrationManager.LOG_TAG, "handleE911RegiTimeOut");
        iRegisterTask.getGovernor().onRegistrationError(SipErrorBase.SIP_TIMEOUT, 1000, true);
    }

    public void checkUnProcessedVoLTEState(int i) {
        Optional.ofNullable(RegistrationUtils.getPendingRegistrationInternal(i)).ifPresent(new RegistrationManagerBase$$ExternalSyntheticLambda2());
    }

    public boolean isSupportVoWiFiDisable5GSAFromConfiguration(int i) {
        if (!"ENABLE".equals(this.mImsFramework.getString(i, GlobalSettingsConstants.Call.VOWIFI_5GSA_MODE, "ENABLE"))) {
            return true;
        }
        boolean z = this.mImsFramework.getBoolean(i, GlobalSettingsConstants.Call.SUPPORT_VOWIFI_DEPRIORITIZE_NR5G, false);
        boolean z2 = this.mImsFramework.getBoolean(i, GlobalSettingsConstants.Call.SUPPORT_DISABLE_VOWIFI_5GSA, false);
        if (z || z2) {
            return true;
        }
        return false;
    }
}
