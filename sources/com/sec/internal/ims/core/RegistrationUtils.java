package com.sec.internal.ims.core;

import android.content.ContentValues;
import android.content.Context;
import android.net.Network;
import android.net.Uri;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.aec.IAECModule;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class RegistrationUtils {
    private static final String LOG_TAG = "RegiMgr-Utils";

    static boolean isCmcPrimaryType(int i) {
        return i == 1 || i == 3 || i == 5 || i == 7;
    }

    static boolean isCmcSecondaryType(int i) {
        return i == 2 || i == 4 || i == 8;
    }

    private RegistrationUtils() {
    }

    static ImsProfile[] getProfileList(int i) {
        List<ImsProfile> profiles = SlotBasedConfig.getInstance(i).getProfiles();
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) profiles)) {
            ImsProfile[] imsProfileArr = new ImsProfile[profiles.size()];
            profiles.toArray(imsProfileArr);
            return imsProfileArr;
        }
        Map<Integer, ImsProfile> extendedProfiles = SlotBasedConfig.getInstance(i).getExtendedProfiles();
        int i2 = 0;
        if (CollectionUtils.isNullOrEmpty((Map<?, ?>) extendedProfiles)) {
            return new ImsProfile[0];
        }
        ImsProfile[] imsProfileArr2 = new ImsProfile[extendedProfiles.size()];
        for (Map.Entry<Integer, ImsProfile> value : extendedProfiles.entrySet()) {
            imsProfileArr2[i2] = (ImsProfile) value.getValue();
            i2++;
        }
        return imsProfileArr2;
    }

    static ImsRegistration[] getRegistrationInfoByPhoneId(int i, ImsRegistration[] imsRegistrationArr) {
        ArrayList arrayList = new ArrayList();
        for (ImsRegistration imsRegistration : imsRegistrationArr) {
            if (imsRegistration.getPhoneId() == i) {
                arrayList.add(imsRegistration);
            }
        }
        if (CollectionUtils.isNullOrEmpty((Collection<?>) arrayList)) {
            return null;
        }
        return (ImsRegistration[]) arrayList.toArray(new ImsRegistration[0]);
    }

    static NetworkEvent getNetworkEvent(int i) {
        NetworkEvent networkEvent = SlotBasedConfig.getInstance(i).getNetworkEvent();
        if (networkEvent == null) {
            IMSLog.i(LOG_TAG, i, "getNetworkEvent is not exist. Return null..");
        }
        return networkEvent;
    }

    static ImsRegistration getRegistrationInfo(int i, int i2) {
        if (i2 >= 0) {
            return SlotBasedConfig.getInstance(i).getImsRegistrations().get(Integer.valueOf(i2));
        }
        Log.i(LOG_TAG, "invalid profileId : " + i2);
        return null;
    }

    static boolean hasVolteService(int i, ImsProfile imsProfile) {
        NetworkEvent networkEvent = getNetworkEvent(i);
        if (networkEvent == null) {
            return false;
        }
        return ImsProfile.hasVolteService(imsProfile, networkEvent.network);
    }

    static boolean hasRcsService(int i, ImsProfile imsProfile) {
        NetworkEvent networkEvent = SlotBasedConfig.getInstance(i).getNetworkEvent();
        if (networkEvent == null) {
            return false;
        }
        if ((imsProfile.getPdnType() == -1 || imsProfile.getPdnType() == 1) && networkEvent.isWifiConnected) {
            return ImsProfile.hasRcsService(imsProfile, ImsProfile.NETWORK_TYPE.WIFI);
        }
        return ImsProfile.hasRcsService(imsProfile, networkEvent.network);
    }

    static boolean hasRcsService(int i, ImsProfile imsProfile, boolean z) {
        if ((imsProfile.getPdnType() == -1 || imsProfile.getPdnType() == 1) && z) {
            return ImsProfile.hasRcsService(imsProfile, ImsProfile.NETWORK_TYPE.WIFI);
        }
        return hasRcsService(i, imsProfile);
    }

    static boolean supportCsTty(IRegisterTask iRegisterTask) {
        int ttyType = iRegisterTask.getProfile().getTtyType();
        return ttyType == 1 || ttyType == 3;
    }

    static String getPublicUserIdentity(ImsProfile imsProfile, int i, String str, ISimManager iSimManager) {
        String str2;
        if (imsProfile != null && imsProfile.getImpuList().size() > 0 && (str2 = (String) imsProfile.getImpuList().get(0)) != null) {
            IMSLog.s(LOG_TAG, i, "getPublicUserIdentity: impu from ImsProfile - " + str2);
            return str2;
        } else if (imsProfile == null || ImsProfile.hasVolteService(imsProfile) || str == null) {
            String impuFromSim = iSimManager.getImpuFromSim();
            IMSLog.s(LOG_TAG, i, "getPublicUserIdentity: impu from sim - " + impuFromSim);
            return (imsProfile == null || !Mno.fromName(imsProfile.getMnoName()).isOneOf(Mno.CABLE_PANAMA, Mno.ORANGE_DOMINICANA, Mno.ALE_ECUADOR, Mno.CABLE_BARBADOS, Mno.CABLE_JAMAICA, Mno.VODAFONEPNG_NEWZEALAND)) ? impuFromSim : iSimManager.getDerivedImpu();
        } else {
            IMSLog.s(LOG_TAG, i, "getPublicUserIdentity: impu from autoconfig - " + str);
            return str;
        }
    }

    static String getPrivateUserIdentityfromIsim(int i, ITelephonyManager iTelephonyManager, ISimManager iSimManager, Mno mno) {
        String str;
        int subId = SimUtil.getSubId(i);
        if (subId < 0) {
            return "";
        }
        String isimImpi = iTelephonyManager.getIsimImpi(subId);
        if (TextUtils.isEmpty(isimImpi)) {
            isimImpi = iSimManager.getDerivedImpi();
        }
        if (mno.isOneOf(Mno.EE, Mno.EE_ESN) || mno.isKor()) {
            String[] isimImpu = iTelephonyManager.getIsimImpu(subId);
            String isimDomain = iTelephonyManager.getIsimDomain(subId);
            boolean z = false;
            if (isimImpu != null) {
                int length = isimImpu.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (!TextUtils.isEmpty(isimImpu[i2])) {
                        z = true;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            IMSLog.i(LOG_TAG, i, "getPrivateUserIdentity: MNO=" + mno + ", found impu=" + z + ", domain=" + isimDomain + ", impi=" + IMSLog.checker(isimImpi));
            if (mno.isKor()) {
                if (z) {
                    return isimImpi;
                }
                str = iSimManager.getDerivedImpi();
            } else if (z && !TextUtils.isEmpty(isimDomain) && !TextUtils.isEmpty(isimImpi)) {
                return isimImpi;
            } else {
                str = iSimManager.getDerivedImpi();
            }
            return str;
        } else if (mno.isOneOf(Mno.CABLE_PANAMA, Mno.ORANGE_DOMINICANA, Mno.ALE_ECUADOR, Mno.CABLE_BARBADOS, Mno.CABLE_JAMAICA)) {
            return iSimManager.getDerivedImpi();
        } else {
            return isimImpi;
        }
    }

    static String getPrivateUserIdentity(Context context, ImsProfile imsProfile, int i, ITelephonyManager iTelephonyManager, IRcsPolicyManager iRcsPolicyManager, ISimManager iSimManager) {
        String str;
        int indexOf;
        String impi = imsProfile.getImpi();
        if (!TextUtils.isEmpty(impi)) {
            IMSLog.s(LOG_TAG, i, "impi=" + impi);
            return impi;
        }
        Mno fromName = Mno.fromName(imsProfile.getMnoName());
        if (fromName == Mno.VZW && !SimUtil.isCCT(i) && !ConfigUtil.isRcsOnly(imsProfile)) {
            int subId = SimUtil.getSubId(i);
            if (subId < 0) {
                return "";
            }
            String subscriberId = iTelephonyManager.getSubscriberId(subId);
            if (IsNonDirectRoamingCase(context, iSimManager, iTelephonyManager)) {
                String isimImpi = iTelephonyManager.getIsimImpi(subId);
                if (!TextUtils.isEmpty(isimImpi) && (indexOf = isimImpi.indexOf("@")) > 0) {
                    subscriberId = isimImpi.substring(0, indexOf);
                }
                IMSLog.e(LOG_TAG, i, "IMPI from ISIM is empty");
            }
            String str2 = subscriberId + "@" + getHomeNetworkDomain(context, imsProfile, i, iTelephonyManager, iRcsPolicyManager, iSimManager);
            IMSLog.s(LOG_TAG, i, "imsiBasedImpi=" + str2);
            return str2;
        } else if (iSimManager == null) {
            return "";
        } else {
            if (iSimManager.hasIsim()) {
                str = getPrivateUserIdentityfromIsim(i, iTelephonyManager, iSimManager, fromName);
            } else {
                str = iSimManager.getDerivedImpi();
            }
            if (!ImsProfile.hasVolteService(imsProfile)) {
                str = iRcsPolicyManager.getRcsPrivateUserIdentity(str, imsProfile, i);
            }
            IMSLog.s(LOG_TAG, i, "impi=" + str);
            return str;
        }
    }

    static boolean IsNonDirectRoamingCase(Context context, ISimManager iSimManager, ITelephonyManager iTelephonyManager) {
        String str;
        String str2;
        if (iSimManager == null) {
            IMSLog.i(LOG_TAG, 0, "IsNonDirectRoamingCase, get operator from TelephonyManager.");
            str2 = ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).getSimOperator();
            str = iTelephonyManager.getGroupIdLevel1();
        } else {
            IMSLog.i(LOG_TAG, 0, "IsNonDirectRoamingCase, get operator from SimManager.");
            str2 = iSimManager.getSimOperator();
            int subscriptionId = iSimManager.getSubscriptionId();
            String groupIdLevel1 = iTelephonyManager.getGroupIdLevel1(subscriptionId);
            IMSLog.i(LOG_TAG, subscriptionId + "," + str2 + "," + groupIdLevel1);
            str = groupIdLevel1;
        }
        if (!TextUtils.equals(str2, "20404") || !"BAE0000000000000".equalsIgnoreCase(str)) {
            return false;
        }
        return true;
    }

    static String getHomeNetworkDomain(Context context, ImsProfile imsProfile, int i, ITelephonyManager iTelephonyManager, IRcsPolicyManager iRcsPolicyManager, ISimManager iSimManager) {
        String isimDomain = iTelephonyManager.getIsimDomain(SimUtil.getSubId(i));
        Mno fromName = Mno.fromName(imsProfile.getMnoName());
        IMSLog.i(LOG_TAG, i, "getHomeNetworkDomain: mno=" + fromName.getName() + " EFDOMAIN=" + isimDomain + " domain from profile=" + imsProfile.getDomain());
        boolean z = true;
        if (fromName == Mno.VZW && !ConfigUtil.isRcsOnly(imsProfile)) {
            if (imsProfile.getPcscfPreference() != 2) {
                z = false;
            }
            if (TextUtils.isEmpty(isimDomain) || z) {
                isimDomain = imsProfile.getDomain();
            }
        } else if (imsProfile.isSoftphoneEnabled() || imsProfile.isSamsungMdmnEnabled()) {
            Iterator it = imsProfile.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    isimDomain = null;
                    break;
                }
                String str = (String) it.next();
                if (!TextUtils.isEmpty(str) && str.indexOf("@") > 0 && str.contains("sip")) {
                    isimDomain = str.substring(str.indexOf("@") + 1);
                    break;
                }
            }
        } else if (fromName == Mno.GCF && !TextUtils.isEmpty(isimDomain)) {
            return isimDomain;
        } else {
            String rcsHomeNetworkDomain = TextUtils.isEmpty(imsProfile.getDomain()) ? iRcsPolicyManager.getRcsHomeNetworkDomain(imsProfile, i) : imsProfile.getDomain();
            if (!TextUtils.isEmpty(rcsHomeNetworkDomain)) {
                isimDomain = rcsHomeNetworkDomain;
            }
        }
        String isDerivedDomainFromImsiRequired = isDerivedDomainFromImsiRequired(fromName, imsProfile, iSimManager, i, isimDomain);
        IMSLog.i(LOG_TAG, i, "getHomeNetworkDomain: domain=" + isDerivedDomainFromImsiRequired);
        return isDerivedDomainFromImsiRequired.replaceAll("[^\\x20-\\x7E]", "");
    }

    private static String isDerivedDomainFromImsiRequired(Mno mno, ImsProfile imsProfile, ISimManager iSimManager, int i, String str) {
        String str2;
        boolean z = true;
        boolean z2 = (!ConfigUtil.isRcsEur(i) || !ConfigUtil.isRcsOnly(imsProfile)) && !imsProfile.isSamsungMdmnEnabled() && ((mno.isChn() && !ImsProfile.isRcsUpProfile(imsProfile.getRcsProfile())) || mno.isOneOf(Mno.H3G_DK, Mno.H3G_SE, Mno.METEOR_IRELAND, Mno.EE, Mno.EE_ESN)) && !ImsConstants.RCS_AS.JIBE.equalsIgnoreCase(ConfigUtil.getAcsServerType(i)) && iSimManager != null && (!iSimManager.hasIsim() || !iSimManager.isISimDataValid());
        if (!TextUtils.isEmpty(str) && ((!mno.isLatin() || !imsProfile.getPdn().equals(DeviceConfigManager.IMS) || iSimManager == null || (iSimManager.hasIsim() && iSimManager.isISimDataValid())) && mno != Mno.ALE_ECUADOR && mno != Mno.CABLE_BARBADOS && mno != Mno.CABLE_JAMAICA && mno != Mno.VINAPHONE && mno != Mno.MASCOM_BOTSWANA)) {
            z = z2;
        } else if (mno == Mno.TWM) {
            return String.format(Locale.US, "%s", new Object[]{"ims.taiwanmobile.com"});
        }
        if (!z) {
            return str;
        }
        if (iSimManager != null) {
            str2 = mno == Mno.TMOUS ? iSimManager.getHighestPriorityEhplmn() : iSimManager.getSimOperator();
        } else {
            str2 = null;
        }
        if (TextUtils.isEmpty(str2)) {
            return "";
        }
        String format = String.format(Locale.US, "ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{Integer.valueOf(Integer.parseInt(str2.substring(3))), Integer.valueOf(Integer.parseInt(str2.substring(0, 3)))});
        Log.i(LOG_TAG, "getHomeNetworkDomain: Use derived domain - operator " + str2);
        return format;
    }

    static void saveRegisteredImpu(Context context, ImsRegistration imsRegistration, ISimManager iSimManager) {
        IMSLog.i(LOG_TAG, imsRegistration.getPhoneId(), "saveRegisteredImpu:");
        if (iSimManager != null) {
            if (!iSimManager.isSimLoaded()) {
                Log.i(LOG_TAG, "SIM not Loaded");
                return;
            }
            Uri parse = Uri.parse("content://com.sec.ims.settings/impu");
            String imsi = iSimManager.getImsi();
            ContentValues contentValues = new ContentValues();
            contentValues.put("imsi", imsi);
            contentValues.put("impu", imsRegistration.getPreferredImpu().getUri().toString());
            contentValues.put("timestamp", Long.valueOf(new Date().getTime()));
            context.getContentResolver().insert(parse, contentValues);
        }
    }

    static boolean hasVoLteSim(int i, ISimManager iSimManager, ITelephonyManager iTelephonyManager, SlotBasedConfig.RegisterTaskList registerTaskList) {
        Mno simMno = iSimManager.getSimMno();
        if (simMno == null || registerTaskList == null) {
            IMSLog.i(LOG_TAG, i, "hasVoLteSim - no mno or no task");
            return false;
        } else if (CollectionUtils.isNullOrEmpty((Object[]) getProfileList(i))) {
            IMSLog.i(LOG_TAG, i, "hasVoLteSim - no matched profile with SIM");
            return false;
        } else {
            if (!Mno.fromSalesCode(OmcCode.get()).isKor() && !isNotNeededCheckPdnFail(iSimManager, iTelephonyManager, simMno)) {
                Iterator it = registerTaskList.iterator();
                while (it.hasNext()) {
                    RegisterTask registerTask = (RegisterTask) it.next();
                    if (registerTask.getGovernor().isNonVoLteSimByPdnFail()) {
                        IMSLog.i(LOG_TAG, registerTask.getPhoneId(), "hasVoLteSim - Pdn rejected by network");
                        return false;
                    }
                }
                if (simMno.isOneOf(Mno.TELEFONICA_UK, Mno.TELEFONICA_UK_LAB)) {
                    IAECModule aECModule = ImsRegistry.getAECModule();
                    if (aECModule.getEntitlementForVoLte(i) && !aECModule.getVoLteEntitlementStatus(i)) {
                        IMSLog.i(LOG_TAG, i, "hasVoLteSim - Entitlement is not ready");
                        return false;
                    }
                }
                NetworkEvent networkEvent = getNetworkEvent(i);
                if (ImsRegistry.getBoolean(i, GlobalSettingsConstants.Registration.VOLTE_SETTING_DIM_BY_VOPS, false) && networkEvent != null && networkEvent.voiceOverPs == VoPsIndication.NOT_SUPPORTED && NetworkUtil.is3gppPsVoiceNetwork(networkEvent.network)) {
                    String networkTypeName = TelephonyManagerExt.getNetworkTypeName(networkEvent.network);
                    IMSLog.i(LOG_TAG, i, "hasVoLteSim - VoPS not supported in " + networkTypeName);
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isNotNeededCheckPdnFail(ISimManager iSimManager, ITelephonyManager iTelephonyManager, Mno mno) {
        if (mno != Mno.ORANGE_SPAIN) {
            return false;
        }
        String simOperatorName = iTelephonyManager.getSimOperatorName(iSimManager.getSubscriptionId());
        return "simyo".equalsIgnoreCase(TextUtils.isEmpty(simOperatorName) ? "" : simOperatorName.trim());
    }

    static boolean hasLoadedProfile(int i) {
        IMSLog.i(LOG_TAG, i, "hasLoadedProfile:");
        return !CollectionUtils.isNullOrEmpty((Collection<?>) SlotBasedConfig.getInstance(i).getProfiles()) || !CollectionUtils.isNullOrEmpty((Map<?, ?>) SlotBasedConfig.getInstance(i).getExtendedProfiles());
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x003d A[EDGE_INSN: B:20:0x003d->B:14:0x003d ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x001b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void setVoLTESupportProperty(boolean r2, int r3) {
        /*
            boolean r0 = com.sec.internal.helper.SimUtil.isMultiSimSupported()
            if (r0 != 0) goto L_0x0007
            return
        L_0x0007:
            if (r2 != 0) goto L_0x003d
            com.sec.internal.ims.core.SlotBasedConfig r2 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r3)
            java.util.List r2 = r2.getProfiles()
            java.util.Iterator r2 = r2.iterator()
        L_0x0015:
            boolean r0 = r2.hasNext()
            if (r0 == 0) goto L_0x003d
            java.lang.Object r0 = r2.next()
            com.sec.ims.settings.ImsProfile r0 = (com.sec.ims.settings.ImsProfile) r0
            java.lang.String r1 = "smsip"
            boolean r1 = r0.hasService(r1)
            if (r1 != 0) goto L_0x003a
            java.lang.String r1 = "mmtel"
            boolean r1 = r0.hasService(r1)
            if (r1 != 0) goto L_0x003a
            java.lang.String r1 = "mmtel-video"
            boolean r0 = r0.hasService(r1)
            if (r0 == 0) goto L_0x0015
        L_0x003a:
            java.lang.String r2 = "1"
            goto L_0x003f
        L_0x003d:
            java.lang.String r2 = "0"
        L_0x003f:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "setVoLTESupportProperty: volteSupported ["
            r0.append(r1)
            r0.append(r2)
            java.lang.String r1 = "]"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "RegiMgr-Utils"
            com.sec.internal.log.IMSLog.i(r1, r3, r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "persist.sys.ims.supportmmtel"
            r0.append(r1)
            int r3 = r3 + 1
            r0.append(r3)
            java.lang.String r3 = r0.toString()
            android.os.SemSystemProperties.set(r3, r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.setVoLTESupportProperty(boolean, int):void");
    }

    static boolean hasSimMobilityProfile(int i) {
        for (ImsProfile simMobility : SlotBasedConfig.getInstance(i).getProfiles()) {
            if (simMobility.getSimMobility()) {
                return true;
            }
        }
        return false;
    }

    static boolean pendingHasEmergencyTask(int i, Mno mno) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal == null || mno != Mno.VZW) {
            return false;
        }
        Iterator it = pendingRegistrationInternal.iterator();
        while (it.hasNext()) {
            if (((RegisterTask) it.next()).getProfile().hasEmergencySupport()) {
                return true;
            }
        }
        return false;
    }

    protected static SlotBasedConfig.RegisterTaskList getPendingRegistrationInternal(int i) {
        if (i >= 0 && i < SimUtil.getPhoneCount()) {
            return SlotBasedConfig.getInstance(i).getRegistrationTasks();
        }
        IMSLog.e(LOG_TAG, "getPendingRegistrationInternal : Invalid phoneId : " + i);
        return null;
    }

    static int selectPdnType(ImsProfile imsProfile, int i) {
        int pdnType = imsProfile.getPdnType();
        if (pdnType == -1) {
            pdnType = i == 18 ? 1 : 0;
        }
        if (SimUtil.isSoftphoneEnabled() && pdnType == 0) {
            pdnType = 5;
        }
        Log.i(LOG_TAG, "selectPdnType: rat=" + i + "pdn=" + pdnType);
        return pdnType;
    }

    static boolean checkAusEmergencyCall(Mno mno, int i, ISimManager iSimManager) {
        if (!mno.isAus()) {
            return false;
        }
        if (iSimManager.getSimMno().isAus() || ImsUtil.getSystemProperty("gsm.operator.numeric", i, "00101").startsWith("505")) {
            return true;
        }
        return false;
    }

    static int getPhoneIdForStartConnectivity(IRegisterTask iRegisterTask) {
        int phoneId = iRegisterTask.getPhoneId();
        if (iRegisterTask.getPdnType() == 0) {
            phoneId = SimUtil.getActiveDataPhoneId();
        }
        IMSLog.i(LOG_TAG, phoneId, "getPhoneIdForStartConnectivity: task: " + iRegisterTask + " phoneId: " + phoneId);
        return phoneId;
    }

    static void sendEmergencyRegistrationFailed(IRegisterTask iRegisterTask) {
        Log.i(LOG_TAG, "sendEmergencyRegistrationFailed");
        iRegisterTask.setState(RegistrationConstants.RegisterTaskState.EMERGENCY);
        if (iRegisterTask.getResultMessage() != null) {
            iRegisterTask.getResultMessage().sendToTarget();
            iRegisterTask.setResultMessage((Message) null);
            return;
        }
        Log.i(LOG_TAG, "sendEmergencyRegistrationFailed, mResult is NULL");
    }

    static boolean isCmcProfile(ImsProfile imsProfile) {
        return imsProfile.getCmcType() != 0;
    }

    static List<RegisterTask> getPriorityRegiedTask(boolean z, IRegisterTask iRegisterTask) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal;
        int phoneId = iRegisterTask.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "getPriorityRegiedTask : isPriority  High? " + z);
        ArrayList arrayList = new ArrayList();
        if (!isCmcProfile(iRegisterTask.getProfile()) && (pendingRegistrationInternal = getPendingRegistrationInternal(iRegisterTask.getPhoneId())) != null) {
            Iterator it = pendingRegistrationInternal.iterator();
            while (it.hasNext()) {
                RegisterTask registerTask = (RegisterTask) it.next();
                if (!isCmcProfile(registerTask.getProfile())) {
                    RegistrationConstants.RegisterTaskState registerTaskState = RegistrationConstants.RegisterTaskState.CONNECTING;
                    RegistrationConstants.RegisterTaskState registerTaskState2 = RegistrationConstants.RegisterTaskState.REGISTERING;
                    RegistrationConstants.RegisterTaskState registerTaskState3 = RegistrationConstants.RegisterTaskState.REGISTERED;
                    if (registerTask.isOneOf(registerTaskState, registerTaskState2, registerTaskState3, RegistrationConstants.RegisterTaskState.DEREGISTERING)) {
                        Set allServiceSetFromAllNetwork = registerTask.getProfile().getAllServiceSetFromAllNetwork();
                        allServiceSetFromAllNetwork.retainAll(iRegisterTask.getProfile().getAllServiceSetFromAllNetwork());
                        if (!allServiceSetFromAllNetwork.isEmpty()) {
                            if (z) {
                                if (registerTask.getProfile().getPriority() > iRegisterTask.getProfile().getPriority()) {
                                    arrayList.add(registerTask);
                                }
                            } else if (!registerTask.getProfile().hasEmergencySupport() && registerTask.getProfile().getPriority() < iRegisterTask.getProfile().getPriority()) {
                                arrayList.add(registerTask);
                            }
                        }
                    }
                    if (iRegisterTask.getMno() == Mno.RJIL && z) {
                        int phoneId2 = registerTask.getPhoneId();
                        IMSLog.i(LOG_TAG, phoneId2, "Profile is in = " + registerTask.getState());
                        if (!registerTask.getProfile().hasEmergencySupport() && registerTask.getState() != registerTaskState3 && registerTask.getProfile().getPriority() > iRegisterTask.getProfile().getPriority()) {
                            IMSLog.i(LOG_TAG, registerTask.getPhoneId(), "Priority task is pending");
                            arrayList.add(registerTask);
                        }
                    }
                }
            }
        }
        return arrayList;
    }

    static String handleExceptionalMnoName(Mno mno, int i, ISimManager iSimManager) {
        IMSLog.i(LOG_TAG, i, "handleExceptionalMnoName:");
        String name = mno.getName();
        if (iSimManager == null) {
            return "";
        }
        if (mno == Mno.ATT && iSimManager.hasVsim()) {
            return name + ":softphone";
        } else if (!checkAusEmergencyCall(mno, i, iSimManager)) {
            return name;
        } else {
            String systemProperty = ImsUtil.getSystemProperty("gsm.operator.numeric", i, "00101");
            IMSLog.i(LOG_TAG, i, "handleExceptionalMnoName: nwOperator: " + systemProperty);
            if ("50502".equals(systemProperty)) {
                return Mno.OPTUS.getName();
            }
            if ("50501".equals(systemProperty) || "50571".equals(systemProperty) || "50572".equals(systemProperty)) {
                return Mno.TELSTRA.getName();
            }
            if ("50503".equals(systemProperty) || "50506".equals(systemProperty)) {
                return Mno.VODAFONE_AUSTRALIA.getName();
            }
            "50514".equals(systemProperty);
            return name;
        }
    }

    static void replaceProfilesOnTask(RegisterTask registerTask) {
        IMSLog.i(LOG_TAG, registerTask.getPhoneId(), "ReplaceProfilesOnTask:");
        List<ImsProfile> profiles = SlotBasedConfig.getInstance(registerTask.getPhoneId()).getProfiles();
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) profiles)) {
            for (ImsProfile next : profiles) {
                if (registerTask.getProfile().getId() == next.getId()) {
                    registerTask.setProfile(next);
                }
            }
        }
    }

    static boolean needToNotifyImsReady(ImsProfile imsProfile, int i) {
        if ((imsProfile.isSoftphoneEnabled() || (imsProfile.isSamsungMdmnEnabled() && imsProfile.getCmcType() == 0)) && !imsProfile.hasEmergencySupport()) {
            return true;
        }
        if (!isCmcSecondaryType(imsProfile.getCmcType())) {
            return false;
        }
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal = getPendingRegistrationInternal(i);
        if (pendingRegistrationInternal == null || pendingRegistrationInternal.size() != 1) {
            return false;
        }
        return true;
    }

    static boolean isDelayDeRegForNonADSOnFlightModeChanged(RegisterTask registerTask) {
        SlotBasedConfig.RegisterTaskList pendingRegistrationInternal;
        boolean z;
        if (!SimUtil.isDualIMS() || !SemSystemProperties.get("ro.boot.hardware", "").contains("qcom") || (pendingRegistrationInternal = getPendingRegistrationInternal(SimUtil.getOppositeSimSlot(registerTask.getPhoneId()))) == null) {
            return false;
        }
        Iterator it = pendingRegistrationInternal.iterator();
        while (true) {
            if (!it.hasNext()) {
                z = false;
                break;
            }
            RegisterTask registerTask2 = (RegisterTask) it.next();
            if (registerTask2.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING) && !registerTask2.isRcsOnly()) {
                z = true;
                break;
            }
        }
        if (!z || registerTask.getPhoneId() == SimUtil.getActiveDataPhoneId()) {
            return false;
        }
        IMSLog.i(LOG_TAG, registerTask.getPhoneId(), "isDelayDeRegForNonADSOnFlightModeChanged : true");
        return true;
    }

    static Set<String> filterserviceFbe(Context context, Set<String> set, ImsProfile imsProfile) {
        if (set == null) {
            return new HashSet();
        }
        HashSet hashSet = new HashSet(set);
        if (!DeviceUtil.isUserUnlocked(context)) {
            Log.i(LOG_TAG, "filterserviceFbe: rcsonly=" + ConfigUtil.isRcsOnly(imsProfile));
            if (ConfigUtil.isRcsOnly(imsProfile)) {
                return new HashSet();
            }
            for (String remove : ImsProfile.getChatServiceList()) {
                hashSet.remove(remove);
            }
        }
        return hashSet;
    }

    static void updateImsIcon(IRegisterTask iRegisterTask) {
        Optional.ofNullable(SlotBasedConfig.getInstance(iRegisterTask.getPhoneId()).getIconManager()).ifPresent(new RegistrationUtils$$ExternalSyntheticLambda0(iRegisterTask));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$updateImsIcon$0(IRegisterTask iRegisterTask, ImsIconManager imsIconManager) {
        if (!iRegisterTask.getProfile().hasEmergencySupport()) {
            imsIconManager.updateRegistrationIcon();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0075 A[EDGE_INSN: B:18:0x0075->B:13:0x0075 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x001e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void initRttMode(android.content.Context r6) {
        /*
            java.lang.String r0 = "initRttMode"
            java.lang.String r1 = "RegiMgr-Utils"
            android.util.Log.i(r1, r0)
            r0 = 0
        L_0x0008:
            int r2 = com.sec.internal.helper.SimUtil.getPhoneCount()
            if (r0 >= r2) goto L_0x0078
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r2 = getPendingRegistrationInternal(r0)
            if (r2 == 0) goto L_0x0075
            java.util.Iterator r2 = r2.iterator()
        L_0x0018:
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L_0x0075
            java.lang.Object r3 = r2.next()
            com.sec.internal.ims.core.RegisterTask r3 = (com.sec.internal.ims.core.RegisterTask) r3
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            int r4 = r4.getTtyType()
            r5 = 3
            if (r4 == r5) goto L_0x003a
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            int r4 = r4.getTtyType()
            r5 = 4
            if (r4 != r5) goto L_0x0018
        L_0x003a:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "initRttMode : "
            r2.append(r4)
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            java.lang.String r4 = r4.getName()
            r2.append(r4)
            java.lang.String r4 = " : "
            r2.append(r4)
            com.sec.ims.settings.ImsProfile r3 = r3.getProfile()
            int r3 = r3.getTtyType()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r1, r0, r2)
            com.sec.internal.ims.core.SlotBasedConfig r2 = com.sec.internal.ims.core.SlotBasedConfig.getInstance(r0)
            boolean r3 = com.sec.internal.ims.util.ImsUtil.isRttModeOnFromCallSettings(r6, r0)
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r3)
            r2.setRTTMode(r3)
        L_0x0075:
            int r0 = r0 + 1
            goto L_0x0008
        L_0x0078:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.initRttMode(android.content.Context):void");
    }

    public static int findBestNetwork(int i, ImsProfile imsProfile, IRegistrationGovernor iRegistrationGovernor, boolean z, PdnController pdnController, IVolteServiceModule iVolteServiceModule, int i2, Context context) {
        int i3 = i;
        ImsProfile imsProfile2 = imsProfile;
        NetworkEvent networkEvent = getNetworkEvent(i);
        if (networkEvent == null) {
            return 0;
        }
        Mno fromName = Mno.fromName(imsProfile.getMnoName());
        int pdnType = imsProfile.getPdnType();
        Set networkSet = imsProfile.getNetworkSet();
        if (imsProfile.hasEmergencySupport()) {
            return findBestEmergencyNetwork(i, fromName, pdnController, iVolteServiceModule, networkEvent, imsProfile, context);
        }
        if (determineWifi(i, fromName, pdnType, networkSet, pdnController, imsProfile, iRegistrationGovernor, networkEvent, context)) {
            return 18;
        }
        int availableMobileNetwork = getAvailableMobileNetwork(context, i, networkEvent, imsProfile2, i2);
        String networkTypeName = TelephonyManagerExt.getNetworkTypeName(availableMobileNetwork);
        if (!networkSet.contains(Integer.valueOf(availableMobileNetwork)) || imsProfile2.getServiceSet(Integer.valueOf(availableMobileNetwork)).isEmpty()) {
            PdnController pdnController2 = pdnController;
        } else if (pdnController.isNetworkAvailable(availableMobileNetwork, pdnType, i) && !networkEvent.outOfService && (!networkEvent.isDataRoaming || iRegistrationGovernor.allowRoaming() || networkEvent.network == 18)) {
            IMSLog.i(LOG_TAG, i, "findBestNetwork: " + networkTypeName);
            return availableMobileNetwork;
        }
        printFailReason(i, imsProfile, availableMobileNetwork, pdnType, networkEvent, pdnController, iRegistrationGovernor.allowRoaming(), networkTypeName);
        return 0;
    }

    static int findBestEmergencyNetwork(int i, Mno mno, PdnController pdnController, IVolteServiceModule iVolteServiceModule, NetworkEvent networkEvent, ImsProfile imsProfile, Context context) {
        int voWIFIEmergencyCallRat;
        if (mno == Mno.VZW || mno.isCanada()) {
            if (pdnController.isEpdgConnected(i)) {
                return 18;
            }
            return 13;
        } else if (mno.isTw()) {
            int i2 = networkEvent.network;
            Set networkSet = imsProfile.getNetworkSet();
            IMSLog.i(LOG_TAG, i, "current RAT : " + i2 + " contains network in profile: " + networkSet.contains(Integer.valueOf(i2)) + ", hasEmergnecy option: " + imsProfile.hasEmergencySupport());
            if (networkSet.contains(Integer.valueOf(i2))) {
                return i2;
            }
            return 13;
        } else if (iVolteServiceModule != null && (voWIFIEmergencyCallRat = iVolteServiceModule.getVoWIFIEmergencyCallRat(i)) != -1) {
            return voWIFIEmergencyCallRat;
        } else {
            if (networkEvent.network == 20) {
                return 20;
            }
            if (!imsProfile.isUicclessEmergency() || networkEvent.network != 0) {
                return 13;
            }
            return ImsUtil.getRatInNoSimCase(i, context);
        }
    }

    static boolean determineWifi(int i, Mno mno, int i2, Set<Integer> set, PdnController pdnController, ImsProfile imsProfile, IRegistrationGovernor iRegistrationGovernor, NetworkEvent networkEvent, Context context) {
        int translateNetworkBearer = pdnController.translateNetworkBearer(pdnController.getDefaultNetworkBearer());
        boolean z = !mno.isKor() || (!iRegistrationGovernor.isMobilePreferredForRcs() ? translateNetworkBearer == 1 : !NetworkUtil.isMobileDataOn(context) || !NetworkUtil.isMobileDataPressed(context) || ImsConstants.SystemSettings.AIRPLANE_MODE.get(context, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON || networkEvent.outOfService || iRegistrationGovernor.hasNetworkFailure());
        if ((i2 == -1 || i2 == 1) && set.contains(18) && !imsProfile.getServiceSet(18).isEmpty() && pdnController.isWifiConnected() && z && (!ConfigUtil.isRcsOnly(imsProfile) || (!(mno == Mno.TMOBILE || mno == Mno.H3G) || translateNetworkBearer == 1))) {
            IMSLog.i(LOG_TAG, i, "findBestNetwork: WIFI needWifiNetwork = " + z);
            return true;
        } else if (i2 == ConnectivityManagerExt.TYPE_WIFI_P2P) {
            IMSLog.i(LOG_TAG, i, "findBestNetwork: WIFI-P2P (Wifi-Direct or Mobile-HotSpot connected)");
            return true;
        } else if (!pdnController.isEpdgConnected(i) || !pdnController.isWifiConnected()) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, i, "findBestNetwork: WIFI (ePDG connected)");
            return true;
        }
    }

    static int getAvailableMobileNetwork(Context context, int i, NetworkEvent networkEvent, ImsProfile imsProfile, int i2) {
        int blurNetworkType;
        int i3 = networkEvent.network;
        boolean z = false;
        if (i3 == 18 && (!imsProfile.isEpdgSupported() || (SimUtil.isCCT(i) && (!VowifiConfig.isEnabled(context, i) || (VowifiConfig.getRoamPrefMode(context, 0, i) == 0 && networkEvent.isDataRoaming))))) {
            z = true;
        }
        if (z && (blurNetworkType = NetworkEvent.blurNetworkType(i2)) != 0) {
            i3 = blurNetworkType;
        }
        IMSLog.i(LOG_TAG, i, "getAvailableMobileNetwork: network=" + networkEvent.network + " mobileDataType=" + i2 + " => rat=" + i3);
        return i3;
    }

    private static void printFailReason(int i, ImsProfile imsProfile, int i2, int i3, NetworkEvent networkEvent, PdnController pdnController, boolean z, String str) {
        String str2;
        if (imsProfile.getServiceSet(Integer.valueOf(i2)).isEmpty()) {
            str2 = "" + " - serviceSet empty";
        } else {
            str2 = "";
        }
        if (!pdnController.isNetworkAvailable(i2, i3, i)) {
            str2 = str2 + " - NetworkAvailable: false";
        }
        if (networkEvent.outOfService) {
            str2 = str2 + " - OOS: true";
        }
        if (networkEvent.isDataRoaming && !z && networkEvent.network != 18) {
            str2 = str2 + "- Roaming not allowed";
        }
        if ("".equals(str2)) {
            str2 = str2 + " - networkSet empty";
        }
        IMSLog.i(LOG_TAG, i, "Not found best network in " + str + str2);
    }

    public static boolean ignoreSendDeregister(int i, Mno mno, RegisterTask registerTask, int i2) {
        if ((i2 == 3 && registerTask.getPdnType() == 11) || (i2 == 4 && registerTask.getPdnType() != 11)) {
            Log.i(LOG_TAG, "Not matched pdn type. reason: " + i2 + ",pdnType: " + registerTask.getPdnType());
            return true;
        } else if (i2 == 124 && !registerTask.getProfile().isEpdgSupported()) {
            Log.i(LOG_TAG, "Ignore Epdg deregister due to not support epdg profile : " + registerTask.getProfile().getName());
            return true;
        } else if (!ConfigUtil.isRcsChn(mno) || !registerTask.isRcsOnly() || !(i2 == 4 || i2 == 3 || i2 == 1)) {
            int i3 = getNetworkEvent(i).network;
            if (mno == Mno.DOCOMO && !NetworkUtil.is3gppPsVoiceNetwork(i3) && (i2 == 4 || i2 == 3)) {
                Log.i(LOG_TAG, "sendDeregister : DCM doesn't need to handle this on 3G");
                return true;
            } else if (i2 != 143) {
                return false;
            } else {
                if (!registerTask.isRcsOnly() || !registerTask.getProfile().getNeedAutoconfig()) {
                    return true;
                }
                registerTask.setReason("FORCE SMS PUSH");
                return false;
            }
        } else {
            Log.i(LOG_TAG, "sendDeregister : 4 or 1: RCS not needed");
            return true;
        }
    }

    public static List<String> retrievePcscfByProfileSettings(IRegisterTask iRegisterTask, PdnController pdnController, IRcsPolicyManager iRcsPolicyManager, String[] strArr) {
        int phoneId = iRegisterTask.getPhoneId();
        ImsProfile profile = iRegisterTask.getProfile();
        int pcscfPreference = profile.getPcscfPreference();
        List<String> arrayList = new ArrayList<>();
        if (pcscfPreference == 0 || (pcscfPreference == 4 && !iRegisterTask.isRcsOnly())) {
            arrayList = pdnController.readPcscfFromLinkProperties(pdnController.getLinkProperties(iRegisterTask));
            if ((iRegisterTask.getMno() == Mno.KT || iRegisterTask.getMno() == Mno.SKT) && CollectionUtils.isNullOrEmpty((Collection<?>) arrayList)) {
                IMSLog.i(LOG_TAG, phoneId, "getPcscfAddresses: pcscfList invalid call retryDNSQuery");
                iRegisterTask.getGovernor().retryDNSQuery();
                if (getNetworkEvent(phoneId).isDataRoaming) {
                    arrayList = profile.getPcscfList();
                }
            }
        } else if (pcscfPreference == 3 || pcscfPreference == 4) {
            arrayList.add(iRcsPolicyManager.getRcsPcscfAddress(iRegisterTask.getProfile(), iRegisterTask.getPhoneId()));
        } else if (pcscfPreference == 5) {
            arrayList = retrievePcscfViaOmadm(iRegisterTask, pdnController);
        } else if (pcscfPreference == 2) {
            arrayList = profile.getPcscfList();
            if (arrayList.size() == 0) {
                IMSLog.e(LOG_TAG, phoneId, "getPcscfAddress: No P-CSCF address found in profile " + profile.getName());
                return null;
            }
        } else if (pcscfPreference == 1) {
            arrayList = new ArrayList<>(Arrays.asList(strArr));
        }
        IMSLog.i(LOG_TAG, phoneId, "getPcscfAddress: " + arrayList);
        return arrayList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0054  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0090 A[EDGE_INSN: B:22:0x0090->B:16:0x0090 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static java.util.List<java.lang.String> retrievePcscfViaOmadm(com.sec.internal.interfaces.ims.core.IRegisterTask r6, com.sec.internal.ims.core.PdnController r7) {
        /*
            int r0 = r6.getPhoneId()
            com.sec.ims.settings.ImsProfile r1 = r6.getProfile()
            java.lang.String r2 = r1.getMnoName()
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.fromName(r2)
            boolean r2 = r2.isKor()
            if (r2 == 0) goto L_0x009b
            boolean r2 = r1.hasEmergencySupport()
            if (r2 == 0) goto L_0x009b
            com.sec.internal.helper.os.LinkPropertiesWrapper r6 = r7.getLinkProperties(r6)
            java.util.List r6 = r7.readPcscfFromLinkProperties(r6)
            int r7 = r6.size()
            if (r7 != 0) goto L_0x009f
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r2 = "getPcscfAddress: No P-CSCF address found in PCO "
            r7.append(r2)
            java.lang.String r2 = r1.getName()
            r7.append(r2)
            java.lang.String r7 = r7.toString()
            java.lang.String r2 = "RegiMgr-Utils"
            com.sec.internal.log.IMSLog.e(r2, r0, r7)
            com.sec.internal.ims.core.SlotBasedConfig$RegisterTaskList r7 = getPendingRegistrationInternal(r0)
            if (r7 == 0) goto L_0x0090
            java.util.Iterator r7 = r7.iterator()
        L_0x004e:
            boolean r3 = r7.hasNext()
            if (r3 == 0) goto L_0x0090
            java.lang.Object r3 = r7.next()
            com.sec.internal.ims.core.RegisterTask r3 = (com.sec.internal.ims.core.RegisterTask) r3
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            boolean r4 = r4.hasEmergencySupport()
            if (r4 == 0) goto L_0x0074
            com.sec.ims.settings.ImsProfile r4 = r3.getProfile()
            java.lang.String r4 = r4.getName()
            java.lang.String r5 = "VoLTE"
            boolean r4 = r4.contains(r5)
            if (r4 == 0) goto L_0x004e
        L_0x0074:
            com.sec.ims.settings.ImsProfile r6 = r3.getProfile()
            java.util.List r6 = r6.getPcscfList()
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r3 = "getPcscfAddress: P-CSCF address found in VoLTE profile "
            r7.append(r3)
            r7.append(r6)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.e(r2, r0, r7)
        L_0x0090:
            int r7 = r6.size()
            if (r7 != 0) goto L_0x009f
            java.util.List r6 = r1.getPcscfList()
            goto L_0x009f
        L_0x009b:
            java.util.List r6 = r1.getPcscfList()
        L_0x009f:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationUtils.retrievePcscfViaOmadm(com.sec.internal.interfaces.ims.core.IRegisterTask, com.sec.internal.ims.core.PdnController):java.util.List");
    }

    static boolean isSatisfiedCarrierRequirement(int i, ImsProfile imsProfile, Mno mno, int i2, boolean z) {
        boolean z2;
        if (mno == Mno.TMOUS && i2 == 1 && !z) {
            return false;
        }
        if (mno.isKor() && !ImsProfile.hasVolteService(imsProfile) && !imsProfile.getSupportRcsAcrossSalesCode()) {
            if (mno != Mno.SKT ? mno != Mno.KT ? mno != Mno.LGU || OmcCode.isLGTOmcCode() || OmcCode.isKorOpenOmcCode() : OmcCode.isKTTOmcCode() || OmcCode.isKorOpenOmcCode() : OmcCode.isSKTOmcCode() || OmcCode.isKorOpenOmcCode()) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (!z2) {
                IMSLog.i(LOG_TAG, i, "buildTask: Not support device. skip RCS Profile.");
                return false;
            }
        }
        return true;
    }

    static boolean isCdmConfigured(IImsFramework iImsFramework, int i) {
        ICapabilityDiscoveryModule capabilityDiscoveryModule = iImsFramework.getServiceModuleManager().getCapabilityDiscoveryModule();
        return capabilityDiscoveryModule == null || !capabilityDiscoveryModule.isRunning() || capabilityDiscoveryModule.isConfigured(i);
    }

    static boolean determineUpdateRegistration(RegisterTask registerTask, int i, int i2, Set<String> set, Set<String> set2, boolean z) {
        int phoneId = registerTask.getPhoneId();
        if (z) {
            IMSLog.i(LOG_TAG, phoneId, "determineUpdateRegistration: Force to do Re-register.");
            if (!"".equals(registerTask.getReason())) {
                return true;
            }
            registerTask.setReason("service changed by user");
            return true;
        }
        if (skipReRegi(registerTask, i, i2)) {
            IMSLog.i(LOG_TAG, phoneId, "determineUpdateRegistration: no need to re-register due to the policy. previousRat=" + i + ", rat=" + i2);
        } else if (registerTask.getProfile().getReregiOnRatChange() == 2 && ((i == 20 && i2 != 20) || (i != 20 && i2 == 20))) {
            IMSLog.i(LOG_TAG, phoneId, "determineUpdateRegistration: Need to re-register due to re-registration on NR policy.");
            registerTask.setReason("RAT has changed from/to NR");
            return true;
        } else if (set2.equals(set)) {
            IMSLog.i(LOG_TAG, phoneId, "determineUpdateRegistration: Same services. No need to re-register.");
        } else if (!"mobile data changed : 0".equals(registerTask.getReason()) || !registerTask.isRcsOnly() || !ConfigUtil.isRcsChn(registerTask.getMno())) {
            IMSLog.i(LOG_TAG, phoneId, "determineUpdateRegistration: service has changed. Re-register.");
            registerTask.setReason("service has changed");
            return true;
        }
        return false;
    }

    static boolean skipReRegi(RegisterTask registerTask, int i, int i2) {
        if (registerTask.getProfile().getReregiOnRatChange() == 0) {
            return true;
        }
        if (registerTask.getProfile().getReregiOnRatChange() != 1 || i2 == i) {
            return false;
        }
        return true;
    }

    protected static void getHostAddressWithThread(RegistrationManagerHandler registrationManagerHandler, IRcsPolicyManager iRcsPolicyManager, IRegisterTask iRegisterTask, String str) throws UnknownHostException {
        int phoneId = iRegisterTask.getPhoneId();
        try {
            Thread thread = new Thread(new RegistrationUtils$$ExternalSyntheticLambda2(phoneId, iRegisterTask.getNetworkConnected(), str, new ArrayList(), registrationManagerHandler, iRcsPolicyManager, iRegisterTask));
            thread.start();
            Message obtainMessage = registrationManagerHandler.obtainMessage(60);
            obtainMessage.obj = thread;
            obtainMessage.arg1 = iRegisterTask.getProfile().getId();
            registrationManagerHandler.sendMessageDelayed(obtainMessage, 10000);
        } catch (Throwable unused) {
            IMSLog.i(LOG_TAG, phoneId, "getHostAddressWithThread: Unknown Host");
            throw new UnknownHostException("cannot resolve host " + str);
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$getHostAddressWithThread$1(int i, Network network, String str, List list, RegistrationManagerHandler registrationManagerHandler, IRcsPolicyManager iRcsPolicyManager, IRegisterTask iRegisterTask) {
        InetAddress[] inetAddressArr;
        try {
            IMSLog.i(LOG_TAG, i, "getHostAddressWithThread: start runnable");
            if (network != null) {
                inetAddressArr = network.getAllByName(str);
            } else {
                inetAddressArr = InetAddress.getAllByName(str);
            }
            if (inetAddressArr != null && inetAddressArr.length > 0) {
                synchronized (list) {
                    for (InetAddress hostAddress : inetAddressArr) {
                        list.add(hostAddress.getHostAddress());
                    }
                    if (list.size() > 0) {
                        handleHostAddressResponse(registrationManagerHandler, iRcsPolicyManager, iRegisterTask, i, list);
                    }
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private static void handleHostAddressResponse(RegistrationManagerHandler registrationManagerHandler, IRcsPolicyManager iRcsPolicyManager, IRegisterTask iRegisterTask, int i, List<String> list) {
        IMSLog.s(LOG_TAG, i, "getHostAddressWithThread: ret " + list);
        List<String> checkValidPcscfIp = iRegisterTask.getGovernor().checkValidPcscfIp(list);
        if (iRegisterTask.getState() == RegistrationConstants.RegisterTaskState.RESOLVING) {
            iRegisterTask.setState(RegistrationConstants.RegisterTaskState.RESOLVED);
        }
        if (registrationManagerHandler.hasMessages(60)) {
            registrationManagerHandler.removeMessages(60);
        }
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) checkValidPcscfIp)) {
            iRegisterTask.getGovernor().updatePcscfIpList(checkValidPcscfIp);
            iRcsPolicyManager.updateDualRcsPcscfIp(iRegisterTask, checkValidPcscfIp);
            registrationManagerHandler.sendTryRegister(i);
        }
    }

    public static InetAddress[] getAllByNameWithThread(IRegisterTask iRegisterTask, String str) throws UnknownHostException {
        int phoneId = iRegisterTask.getPhoneId();
        Network networkConnected = iRegisterTask.getNetworkConnected();
        try {
            long currentTimeMillis = System.currentTimeMillis() + 5000;
            LinkedList linkedList = new LinkedList();
            AtomicBoolean atomicBoolean = new AtomicBoolean(false);
            Thread thread = new Thread(new RegistrationUtils$$ExternalSyntheticLambda1(phoneId, networkConnected, str, linkedList, atomicBoolean));
            thread.start();
            while (true) {
                if (System.currentTimeMillis() < currentTimeMillis) {
                    if (atomicBoolean.get()) {
                        IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName: query failed");
                        break;
                    }
                    synchronized (linkedList) {
                        if (linkedList.size() > 0) {
                            IMSLog.s(LOG_TAG, phoneId, "getAllAddressByName: current result is " + linkedList);
                            InetAddress[] inetAddressArr = (InetAddress[]) linkedList.toArray(new InetAddress[linkedList.size()]);
                            return inetAddressArr;
                        }
                        try {
                            linkedList.wait(300);
                        } catch (Throwable unused) {
                            IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName: wait failed");
                        }
                    }
                } else {
                    break;
                }
            }
            thread.interrupt();
            IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName time out or failed");
            throw new UnknownHostException("cannot resolve host " + str);
        } catch (Throwable unused2) {
            IMSLog.i(LOG_TAG, phoneId, "getAllAddressByName: Unknown Host");
            throw new UnknownHostException("cannot resolve host " + str);
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$getAllByNameWithThread$2(int i, Network network, String str, LinkedList linkedList, AtomicBoolean atomicBoolean) {
        InetAddress[] inetAddressArr;
        try {
            IMSLog.i(LOG_TAG, i, "getAllAddressByName: start runnable");
            if (network != null) {
                inetAddressArr = network.getAllByName(str);
            } else {
                inetAddressArr = InetAddress.getAllByName(str);
            }
            if (inetAddressArr != null && inetAddressArr.length > 0) {
                synchronized (linkedList) {
                    if (linkedList.size() <= 0) {
                        for (int i2 = 0; i2 < inetAddressArr.length; i2++) {
                            linkedList.add(inetAddressArr[i2]);
                            IMSLog.s(LOG_TAG, i, "getAllAddressByName: getAllByName " + inetAddressArr[i2]);
                        }
                        linkedList.notifyAll();
                    }
                }
            }
        } catch (Throwable th) {
            atomicBoolean.set(true);
            th.printStackTrace();
        }
    }

    protected static boolean checkInitialRegistrationIsReady(RegisterTask registerTask, List<IRegisterTask> list, boolean z, boolean z2, boolean z3, IRcsPolicyManager iRcsPolicyManager, RegistrationManagerHandler registrationManagerHandler) {
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        if (z && (!profile.isNetworkEnabled(18) || (registerTask.getMno().isKor() && registerTask.getRegistrationRat() != 18))) {
            int phoneId2 = registerTask.getPhoneId();
            IMSLog.i(LOG_TAG, phoneId2, registerTask.getProfile().getName() + " tryRegister: Airplane mode is on");
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.FLIGHT_MODE_ON.getCode());
            return false;
        } else if (registerTask.getGovernor().isThrottled() && !registerTask.getGovernor().isReadyToGetReattach()) {
            int phoneId3 = registerTask.getPhoneId();
            IMSLog.i(LOG_TAG, phoneId3, "tryRegister: task " + profile.getName() + " is throttled.");
            if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.IDLE, RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                long nextRetryMillis = registerTask.getGovernor().getNextRetryMillis();
                if (nextRetryMillis > 0) {
                    int phoneId4 = registerTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId4, "tryRegister: retry in " + nextRetryMillis + "ms.");
                    registrationManagerHandler.sendTryRegister(phoneId, nextRetryMillis);
                    IMSLog.lazer((IRegisterTask) registerTask, "NOT_TRIGGERED : THROTTLED : " + nextRetryMillis + "ms");
                }
            }
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.REGI_THROTTLED.getCode());
            return false;
        } else if (iRcsPolicyManager.pendingRcsRegister(registerTask, list, registerTask.getPhoneId())) {
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.PENDING_RCS_REGI.getCode());
            return false;
        } else {
            if (!profile.hasEmergencySupport()) {
                if (supportCsTty(registerTask) && SlotBasedConfig.getInstance(registerTask.getPhoneId()).getTTYMode() && registerTask.getMno() != Mno.VZW && !registerTask.getMno().isKor() && !registerTask.getMno().isOneOf(Mno.TMOUS, Mno.DISH)) {
                    int phoneId5 = registerTask.getPhoneId();
                    IMSLog.i(LOG_TAG, phoneId5, "RegisterTask : TtyType=" + profile.getTtyType() + " mTTYMode=" + SlotBasedConfig.getInstance(registerTask.getPhoneId()).getTTYMode());
                    registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.CS_TTY.getCode());
                    return false;
                } else if (!getPriorityRegiedTask(true, registerTask).isEmpty()) {
                    IMSLog.i(LOG_TAG, registerTask.getPhoneId(), "checkHigherPriorityRegiedTask != null");
                    registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.HIGHER_PRIORITY.getCode());
                    return false;
                } else if (!iRcsPolicyManager.isRcsRoamingPref(registerTask, z2)) {
                    registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.RCS_ROAMING.getCode());
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean checkConfigForInitialRegistration(Context context, RegisterTask registerTask, boolean z, boolean z2, boolean z3, boolean z4, IRcsPolicyManager iRcsPolicyManager, RegistrationManagerHandler registrationManagerHandler, NetworkEventController networkEventController) {
        int phoneId = registerTask.getPhoneId();
        ImsProfile profile = registerTask.getProfile();
        boolean z5 = ImsConstants.SystemSettings.AIRPLANE_MODE.get(context, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON;
        if (((registerTask.getMno().isKor() && registerTask.isRcsOnly() && !z5) || !registerTask.getMno().isKor()) && iRcsPolicyManager.tryRcsConfig(registerTask)) {
            IMSLog.i(LOG_TAG, phoneId, "try RCS autoconfiguration");
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.TRY_RCS_CONFIG.getCode());
            return false;
        } else if (z && profile.getNeedAutoconfig() && !z2 && (!registerTask.getMno().isKor() || registerTask.getState() == RegistrationConstants.RegisterTaskState.CONNECTED)) {
            IMSLog.i(LOG_TAG, phoneId, "capability is not configured");
            registrationManagerHandler.sendTryRegister(phoneId, 500);
            return false;
        } else if (!registerTask.isNeedOmadmConfig() || !registerTask.getGovernor().isOmadmConfigAvailable() || !z3 || (registerTask.getMno().isKor() && registerTask.getState() != RegistrationConstants.RegisterTaskState.CONNECTED)) {
            if (!profile.hasEmergencySupport()) {
                registerTask.getGovernor().checkProfileUpdateFromDM(false);
            }
            if (registerTask.getMno().isKor() && ConfigUtil.isRcsOnly(profile)) {
                registerTask.getGovernor().checkAcsPcscfListChange();
            }
            if (registerTask.getMno() != Mno.KDDI || !profile.hasEmergencySupport() || z4) {
                return true;
            }
            IMSLog.e(LOG_TAG, phoneId, "No Emergency Call is made,so dont try for Emergency Register");
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.KDDI_EMERGENCY.getCode());
            return false;
        } else {
            networkEventController.triggerOmadmConfig(phoneId);
            registerTask.setRegiFailReason(DiagnosisConstants.REGI_FRSN.DM_TRIGGERED.getCode());
            return false;
        }
    }

    static boolean needToSkipTryRegister(RegisterTask registerTask, boolean z, boolean z2, boolean z3, ITelephonyManager iTelephonyManager, PdnController pdnController, boolean z4) {
        int phoneId = registerTask.getPhoneId();
        if (registerTask.isOneOf(RegistrationConstants.RegisterTaskState.CONFIGURING, RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.DEREGISTERING, RegistrationConstants.RegisterTaskState.EMERGENCY) || z) {
            return true;
        }
        if (!registerTask.getProfile().hasEmergencySupport() && z3 && !SimUtil.isDualIMS()) {
            IMSLog.e(LOG_TAG, phoneId, "Deregistering is not completed");
            return true;
        } else if (registerTask.getProfile().getEnableStatus() == 0) {
            IMSLog.i(LOG_TAG, phoneId, "tryRegister: profile is disabled. " + registerTask.getProfile());
            return true;
        } else if (registerTask.isSuspended()) {
            IMSLog.i(LOG_TAG, phoneId, "tryRegister: suspened");
            return true;
        } else if (registerTask.getMno() == Mno.KDDI && SimUtil.isDualIMS() && registerTask.getProfile().hasEmergencySupport() && z4 && iTelephonyManager.getCallState(SimUtil.getOppositeSimSlot(phoneId)) != 0) {
            IMSLog.i(LOG_TAG, phoneId, "tryRegister: emergency call is ongoing on other slot");
            return true;
        } else if (!registerTask.isRcsOnly() || !ConfigUtil.isRcsEurNonRjil(registerTask.getMno()) || iTelephonyManager.getCallState(SimUtil.getOppositeSimSlot(phoneId)) == 0 || pdnController.getDataState(phoneId) != 3) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "tryRegister: suspended because other slot is on calling ");
            return true;
        }
    }

    static boolean isRcsRegistered(int i, ImsRegistration[] imsRegistrationArr) {
        ImsRegistration[] registrationInfoByPhoneId = getRegistrationInfoByPhoneId(i, imsRegistrationArr);
        if (registrationInfoByPhoneId != null) {
            for (ImsRegistration hasRcsService : registrationInfoByPhoneId) {
                if (hasRcsService.hasRcsService()) {
                    return true;
                }
            }
        }
        return false;
    }

    static String replaceEnablerPlaceholderWithEnablerVersion(Context context, String str, String str2, int i, ImConfig imConfig) {
        if (TextUtils.isEmpty(str) || !str2.contains("[ENABLER]")) {
            return str2;
        }
        String upOmaEnablerVersion = getUpOmaEnablerVersion(str, ImsRegistry.getString(i, GlobalSettingsConstants.RCS.UP_PROFILE, ""));
        if (!upOmaEnablerVersion.isEmpty()) {
            return str2.replace("[ENABLER]", getImMsgTech(imConfig) + "-client/" + upOmaEnablerVersion);
        }
        String replace = str2.replace("[ENABLER]", "IM-client/OMA1.0");
        Log.e(LOG_TAG, "replaceEnablerPlaceholderWithEnablerVersion: Cannot specify omaEnablerVersion for given rcs_profile and rcs_up_profile. Set enabler to IM-client/OMA1.0 as a default.");
        return replace;
    }

    private static String getImMsgTech(ImConfig imConfig) {
        if (imConfig == null) {
            return "IM";
        }
        String str = imConfig.getImMsgTech().toString();
        if (ImConstants.ImMsgTech.SIMPLE_IM.toString().equals(str)) {
            return "IM";
        }
        return str;
    }

    private static String getUpOmaEnablerVersion(String str, String str2) {
        if (ImsProfile.isRcsUpTransitionProfile(str2)) {
            return ImsConstants.OmaVersion.OMA_2_0;
        }
        if (ImsProfile.isRcsUp10Profile(str)) {
            return ImsConstants.OmaVersion.OMA_2_1;
        }
        return ImsProfile.isRcsUp2Profile(str) ? ImsConstants.OmaVersion.OMA_2_2 : "";
    }
}
