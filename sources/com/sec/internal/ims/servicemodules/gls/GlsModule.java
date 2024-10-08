package com.sec.internal.ims.servicemodules.gls;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.ChnStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.omanetapi.nms.data.GeoLocation;
import com.sec.sve.generalevent.VcidEvent;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class GlsModule extends ServiceModuleBase implements IGlsModule {
    private static final int AUTO_ACCEPT_FT_GLS = 0;
    private static final int AUTO_SEND_FT_GLS = 1;
    private static final String LOG_TAG = GlsModule.class.getSimpleName();
    private final PhoneIdKeyMap<ImConfig> mConfigs;
    private final Context mContext;
    private final IImModule mImModule;
    private boolean[] mPushEnabled = {false, false};
    private final PhoneIdKeyMap<Integer> mRegistrationIds;
    private final GlsTranslation mTranslation;
    private int phoneCount = 0;

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 0) {
            FtMessage ftMessage = (FtMessage) message.obj;
            acceptLocationShare(ftMessage.getImdnId(), ftMessage.getChatId(), (Uri) null);
        } else if (i != 1) {
            super.handleMessage(message);
        } else {
            startLocationShareInCall((String) message.obj);
        }
    }

    public GlsModule(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        int size = SimManagerFactory.getAllSimManagers().size();
        this.phoneCount = size;
        this.mConfigs = new PhoneIdKeyMap<>(size, null);
        this.mRegistrationIds = new PhoneIdKeyMap<>(this.phoneCount, null);
        this.mImModule = getServiceModuleManager().getImModule();
        this.mTranslation = new GlsTranslation(context, this);
        for (int i = 0; i < this.phoneCount; i++) {
            this.mConfigs.put(i, this.mImModule.getImConfig(i));
        }
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        String str = LOG_TAG;
        Log.i(str, "onServiceSwitched: " + i);
        updateFeatures(i);
    }

    public void start() {
        super.start();
        Log.i(LOG_TAG, VcidEvent.BUNDLE_VALUE_ACTION_START);
    }

    public void stop() {
        super.stop();
        Log.i(LOG_TAG, VcidEvent.BUNDLE_VALUE_ACTION_STOP);
    }

    public String[] getServicesRequiring() {
        return new String[]{"im", "gls"};
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        if (imsRegistration != null && imsRegistration.hasRcsService()) {
            String str = LOG_TAG;
            Log.i(str, "onRegistered() phoneId = " + imsRegistration.getPhoneId() + ", services : " + imsRegistration.getServices());
            this.mRegistrationIds.put(imsRegistration.getPhoneId(), Integer.valueOf(getRegistrationInfoId(imsRegistration)));
        }
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        Log.i(LOG_TAG, "onDeregistered");
        super.onDeregistered(imsRegistration, i);
        if (imsRegistration != null && imsRegistration.hasRcsService()) {
            this.mRegistrationIds.remove(imsRegistration.getPhoneId());
        }
    }

    public void onConfigured(int i) {
        String str = LOG_TAG;
        Log.i(str, "onConfigured : phoneId = " + i);
        updateFeatures(i);
    }

    public void handleIntent(Intent intent) {
        this.mTranslation.handleIntent(intent);
    }

    public void registerMessageEventListener(ImConstants.Type type, IMessageEventListener iMessageEventListener) {
        this.mImModule.registerMessageEventListener(type, iMessageEventListener);
    }

    public void registerFtEventListener(ImConstants.Type type, IFtEventListener iFtEventListener) {
        this.mImModule.registerFtEventListener(type, iFtEventListener);
    }

    public ImsRegistration getImsRegistration() {
        return getImsRegistration(SimUtil.getActiveDataPhoneId());
    }

    public ImsRegistration getImsRegistration(int i) {
        if (this.mRegistrationIds.get(i) != null) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationIds.get(i).intValue());
        }
        return null;
    }

    public Future<ImMessage> shareLocationInChat(String str, Set<NotificationStatus> set, Location location, String str2, String str3, String str4, ImsUri imsUri, boolean z, String str5) {
        return shareLocationInChat(SimUtil.getActiveDataPhoneId(), str, set, location, str2, str3, str4, imsUri, z, str5);
    }

    public Future<ImMessage> shareLocationInChat(int i, String str, Set<NotificationStatus> set, Location location, String str2, String str3, String str4, ImsUri imsUri, boolean z, String str5) {
        String str6 = str;
        Location location2 = location;
        String str7 = str2;
        Log.i(LOG_TAG, "shareLocationInChat()");
        int i2 = i;
        int activeDataPhoneId = i2 == -1 ? SimUtil.getActiveDataPhoneId() : i2;
        if (!isPushServiceAvailable(activeDataPhoneId)) {
            this.mTranslation.onShareLocationInChatResponse(str6, str3, (String) null, false);
            return null;
        }
        String str8 = str3;
        boolean isImCapAlwaysOn = this.mConfigs.get(activeDataPhoneId).isImCapAlwaysOn();
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(activeDataPhoneId);
        if (!z) {
            Capabilities capabilities = getServiceModuleManager().getCapabilityDiscoveryModule().getCapabilities(imsUri, CapabilityRefreshType.DISABLED, activeDataPhoneId);
            if (simManagerFromSimSlot != null && ConfigUtil.isRcsChn(simManagerFromSimSlot.getSimMno())) {
                String str9 = str;
                return this.mImModule.sendMessage(str9, generateGeoSms(str9, ImsUri.parse("sip:anonymous@anonymous.invalid"), location, str2, activeDataPhoneId), set, MIMEContentType.PLAIN_TEXT, str3, -1, false, false, true, (List<ImsUri>) null, false, str5, (String) null, (String) null, (String) null);
            } else if ((capabilities != null && capabilities.hasFeature(Capabilities.FEATURE_GEOLOCATION_PUSH)) || isImCapAlwaysOn) {
                return this.mImModule.sendMessage(str, generateXML(str6, ImsUri.parse("sip:anonymous@anonymous.invalid"), location2, str7), set, MIMEContentType.LOCATION_PUSH, str3, -1, false, false, true, (List<ImsUri>) null, false, str5, (String) null, (String) null, (String) null);
            } else if ((simManagerFromSimSlot == null || !simManagerFromSimSlot.getSimMno().isOneOf(Mno.TMOUS)) && (capabilities == null || (!capabilities.hasFeature(Capabilities.FEATURE_CHAT_CPM) && !capabilities.hasFeature(Capabilities.FEATURE_CHAT_SIMPLE_IM)))) {
                this.mTranslation.onReceiveShareLocationInChatResponse(str, str3, (String) null, false, (IMnoStrategy.StrategyResponse) null, RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()), (Result) null);
                return null;
            } else {
                return this.mImModule.sendMessage(str, str7 + " " + str4, set, MIMEContentType.PLAIN_TEXT, str3, -1, false, false, true, (List<ImsUri>) null, false, str5, (String) null, (String) null, (String) null);
            }
        } else if (simManagerFromSimSlot == null || !ConfigUtil.isRcsChn(simManagerFromSimSlot.getSimMno())) {
            return this.mImModule.sendMessage(str, generateXML(str6, ImsUri.parse("sip:anonymous@anonymous.invalid"), location2, str7), set, MIMEContentType.LOCATION_PUSH, str3, -1, false, false, true, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null);
        } else {
            String str10 = str;
            return this.mImModule.sendMessage(str10, generateGeoSms(str10, ImsUri.parse("sip:anonymous@anonymous.invalid"), location, str2, activeDataPhoneId), set, MIMEContentType.PLAIN_TEXT, str3, -1, false, false, true, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null);
        }
    }

    public Future<FtMessage> createInCallLocationShare(String str, ImsUri imsUri, Set<NotificationStatus> set, Location location, String str2, String str3, boolean z, boolean z2) {
        Log.i(LOG_TAG, "createInCallLocationShare()");
        if (!isPushServiceAvailable()) {
            this.mTranslation.onCreateInCallLocationShareResponse((String) null, (String) null, str3, false);
            return null;
        }
        String str4 = str3;
        String generateXML = generateXML("0", imsUri, location, str2);
        String str5 = "gls" + System.currentTimeMillis() + ".xml";
        Uri save2FileSystem = save2FileSystem(str5, generateXML);
        String glsExtInfo = new GlsXmlParser().getGlsExtInfo(generateXML);
        if (save2FileSystem == null) {
            return null;
        }
        if (!z2) {
            return this.mImModule.attachFileToSingleChat(SimUtil.getActiveDataPhoneId(), str5, save2FileSystem, imsUri, set, str3, MIMEContentType.LOCATION_PUSH, z, false, false, false, glsExtInfo, FileDisposition.ATTACH);
        }
        return this.mImModule.attachFileToGroupChat(str, str5, save2FileSystem, set, str3, MIMEContentType.LOCATION_PUSH, false, false, false, false, glsExtInfo, FileDisposition.ATTACH);
    }

    public void startLocationShareInCall(String str) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onStartLocationShareInCallResponse(str, false);
        } else {
            this.mImModule.sendFile(str);
        }
    }

    public void acceptLocationShare(String str, String str2, Uri uri) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onAcceptLocationShareInCallResponse(str, str2, false);
        } else {
            this.mImModule.acceptFileTransfer(str, ImDirection.INCOMING, str2, uri);
        }
    }

    public void cancelLocationShare(String str, ImDirection imDirection, String str2) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onCancelLocationShareInCallResponse(str, imDirection, str2, false);
        } else {
            this.mImModule.cancelFileTransfer(str, imDirection, str2);
        }
    }

    public void deleteGeolocSharings(List<String> list) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onDeleteAllLocationShareResponse(false, list);
        } else {
            this.mImModule.deleteMessages(list, false);
        }
    }

    public void rejectLocationShare(String str, String str2) {
        if (!isPushServiceAvailable()) {
            this.mTranslation.onRejectLocationShareInCallResponse(str, str2, false);
        } else {
            this.mImModule.rejectFileTransfer(str, ImDirection.INCOMING, str2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0054 A[SYNTHETIC, Splitter:B:21:0x0054] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0064 A[SYNTHETIC, Splitter:B:31:0x0064] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0071 A[SYNTHETIC, Splitter:B:39:0x0071] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:18:0x004f=Splitter:B:18:0x004f, B:28:0x005f=Splitter:B:28:0x005f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.net.Uri save2FileSystem(java.lang.String r4, java.lang.String r5) {
        /*
            r3 = this;
            android.content.Context r0 = r3.mContext
            java.io.File r0 = r0.getExternalCacheDir()
            r1 = 0
            if (r0 == 0) goto L_0x007a
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            android.content.Context r2 = r3.mContext
            java.io.File r2 = r2.getExternalCacheDir()
            java.lang.String r2 = r2.getAbsolutePath()
            r0.append(r2)
            java.lang.String r2 = "/"
            r0.append(r2)
            r0.append(r4)
            java.lang.String r4 = r0.toString()
            java.io.File r0 = new java.io.File
            r0.<init>(r4)
            java.io.FileOutputStream r4 = new java.io.FileOutputStream     // Catch:{ FileNotFoundException -> 0x005d, IOException -> 0x004d, all -> 0x004b }
            r4.<init>(r0)     // Catch:{ FileNotFoundException -> 0x005d, IOException -> 0x004d, all -> 0x004b }
            byte[] r5 = r5.getBytes()     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0047 }
            r4.write(r5)     // Catch:{ FileNotFoundException -> 0x0049, IOException -> 0x0047 }
            r4.close()     // Catch:{ IOException -> 0x003c }
            goto L_0x0040
        L_0x003c:
            r4 = move-exception
            r4.printStackTrace()
        L_0x0040:
            android.content.Context r3 = r3.mContext
            android.net.Uri r3 = com.sec.internal.helper.FileUtils.getUriForFile(r3, r0)
            return r3
        L_0x0047:
            r3 = move-exception
            goto L_0x004f
        L_0x0049:
            r3 = move-exception
            goto L_0x005f
        L_0x004b:
            r3 = move-exception
            goto L_0x006f
        L_0x004d:
            r3 = move-exception
            r4 = r1
        L_0x004f:
            r3.printStackTrace()     // Catch:{ all -> 0x006d }
            if (r4 == 0) goto L_0x005c
            r4.close()     // Catch:{ IOException -> 0x0058 }
            goto L_0x005c
        L_0x0058:
            r3 = move-exception
            r3.printStackTrace()
        L_0x005c:
            return r1
        L_0x005d:
            r3 = move-exception
            r4 = r1
        L_0x005f:
            r3.printStackTrace()     // Catch:{ all -> 0x006d }
            if (r4 == 0) goto L_0x006c
            r4.close()     // Catch:{ IOException -> 0x0068 }
            goto L_0x006c
        L_0x0068:
            r3 = move-exception
            r3.printStackTrace()
        L_0x006c:
            return r1
        L_0x006d:
            r3 = move-exception
            r1 = r4
        L_0x006f:
            if (r1 == 0) goto L_0x0079
            r1.close()     // Catch:{ IOException -> 0x0075 }
            goto L_0x0079
        L_0x0075:
            r4 = move-exception
            r4.printStackTrace()
        L_0x0079:
            throw r3
        L_0x007a:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.gls.GlsModule.save2FileSystem(java.lang.String, java.lang.String):android.net.Uri");
    }

    private static GlsData makeGlsData(String str, ImsUri imsUri, Location location, String str2, LocationType locationType) {
        Date date = new Date();
        return new GlsData(str, imsUri, location, locationType, date, str2, new GlsValidityTime(date));
    }

    public static String generateXML(String str, GeoLocation geoLocation) {
        Location location = new Location("gps");
        location.setLatitude(geoLocation.mCircle.mLatitude.doubleValue());
        location.setLongitude(geoLocation.mCircle.mLongitude.doubleValue());
        location.setAccuracy(geoLocation.mCircle.mRadius);
        return generateXML(str, ImsUri.parse("sip:anonymous@anonymous.invalid"), location, geoLocation.mLabel);
    }

    private static String generateXML(String str, ImsUri imsUri, Location location, String str2) {
        LocationType locationType;
        String replaceAll = str2.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll(CmcConstants.E_NUM_STR_QUOTE, "&quot;").replaceAll("'", "&apos;");
        if (replaceAll == null) {
            locationType = LocationType.OWN_LOCATION;
        } else {
            locationType = LocationType.OTHER_LOCATION;
        }
        return new GlsXmlComposer().compose(makeGlsData(str, imsUri, location, replaceAll, locationType));
    }

    private String generateGeoSms(String str, ImsUri imsUri, Location location, String str2, int i) {
        LocationType locationType;
        String replaceAll = str2.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll(CmcConstants.E_NUM_STR_QUOTE, "&quot;").replaceAll("'", "&apos;");
        if (replaceAll == null) {
            locationType = LocationType.OWN_LOCATION;
        } else {
            locationType = LocationType.OTHER_LOCATION;
        }
        String compose = new GlsGeoSmsComposer().compose(makeGlsData(str, imsUri, location, replaceAll, locationType), this.mConfigs.get(i).getPagerModeLimit());
        String str3 = LOG_TAG;
        Log.d(str3, "generateGeoSms: " + compose + " by limit: " + this.mConfigs.get(i).getPagerModeLimit());
        return compose;
    }

    private boolean isPushServiceAvailable() {
        return isPushServiceAvailable(SimUtil.getActiveDataPhoneId());
    }

    private boolean isPushServiceAvailable(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null || !imsRegistration.hasRcsService() || imsRegistration.getPhoneId() != i || !this.mPushEnabled[imsRegistration.getPhoneId()]) {
            Mno simMno = SimUtil.getSimMno(i);
            if (imsRegistration != null && simMno == Mno.ATT && imsRegistration.hasService(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
                return true;
            }
            Log.i(LOG_TAG, "geolocation push is disabled.");
            return false;
        }
        String str = LOG_TAG;
        Log.i(str, "imsRegistration:" + imsRegistration + ", mPushEnabled: true");
        return true;
    }

    public void onTransferCompleted(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "onTransferCompleted: " + ftMessage.getStateId());
        updateExtInfo(ftMessage);
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) instanceof ChnStrategy) {
            this.mTranslation.onLocationShareInCallCompleted(ftMessage, true);
        } else {
            this.mTranslation.onLocationShareInCallCompleted(ftMessage.getImdnId(), ftMessage.getDirection(), ftMessage.getChatId(), true);
        }
    }

    public void onTransferCanceled(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "onTransferCanceled: " + ftMessage.getStateId());
        updateExtInfo(ftMessage);
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) instanceof ChnStrategy) {
            this.mTranslation.onLocationShareInCallCompleted(ftMessage, false);
        } else {
            this.mTranslation.onLocationShareInCallCompleted(ftMessage.getImdnId(), ftMessage.getDirection(), ftMessage.getChatId(), false);
        }
    }

    public void onOutgoingTransferAttached(FtMessage ftMessage) {
        this.mTranslation.onCreateInCallLocationShareResponse(ftMessage.getChatId(), ftMessage.getImdnId(), ftMessage.getRequestMessageId(), true);
        obtainMessage(1, ftMessage.getImdnId()).sendToTarget();
    }

    public void onIncomingTransferUndecided(FtMessage ftMessage) {
        this.mTranslation.onIncomingLoactionShareInCall(ftMessage);
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority());
        if (rcsStrategy != null && rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.AUTO_ACCEPT_GLS)) {
            obtainMessage(0, ftMessage).sendToTarget();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0059, code lost:
        if (r0 != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0063, code lost:
        if (r0 != null) goto L_0x005b;
     */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x006d A[SYNTHETIC, Splitter:B:38:0x006d] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:32:0x0060=Splitter:B:32:0x0060, B:26:0x0056=Splitter:B:26:0x0056} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateExtInfo(com.sec.internal.ims.servicemodules.im.MessageBase r6) {
        /*
            r5 = this;
            java.lang.String r5 = r6.getExtInfo()
            if (r5 == 0) goto L_0x000e
            java.lang.String r5 = LOG_TAG
            java.lang.String r6 = "Already has ext info, no need update!!!"
            android.util.Log.v(r5, r6)
            return
        L_0x000e:
            boolean r5 = r6 instanceof com.sec.internal.ims.servicemodules.im.ImMessage
            if (r5 == 0) goto L_0x001a
            r5 = r6
            com.sec.internal.ims.servicemodules.im.ImMessage r5 = (com.sec.internal.ims.servicemodules.im.ImMessage) r5
            java.lang.String r5 = r5.getBody()
            goto L_0x0072
        L_0x001a:
            boolean r5 = r6 instanceof com.sec.internal.ims.servicemodules.im.FtMessage
            r0 = 0
            if (r5 == 0) goto L_0x0071
            r5 = r6
            com.sec.internal.ims.servicemodules.im.FtMessage r5 = (com.sec.internal.ims.servicemodules.im.FtMessage) r5
            java.lang.String r5 = r5.getFilePath()
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch:{ FileNotFoundException -> 0x005f, IOException -> 0x0055 }
            java.io.InputStreamReader r3 = new java.io.InputStreamReader     // Catch:{ FileNotFoundException -> 0x005f, IOException -> 0x0055 }
            java.io.FileInputStream r4 = new java.io.FileInputStream     // Catch:{ FileNotFoundException -> 0x005f, IOException -> 0x0055 }
            r4.<init>(r5)     // Catch:{ FileNotFoundException -> 0x005f, IOException -> 0x0055 }
            java.lang.String r5 = "UTF-8"
            r3.<init>(r4, r5)     // Catch:{ FileNotFoundException -> 0x005f, IOException -> 0x0055 }
            r2.<init>(r3)     // Catch:{ FileNotFoundException -> 0x005f, IOException -> 0x0055 }
        L_0x003c:
            java.lang.String r5 = r2.readLine()     // Catch:{ FileNotFoundException -> 0x0050, IOException -> 0x004d, all -> 0x004a }
            if (r5 == 0) goto L_0x0046
            r1.append(r5)     // Catch:{ FileNotFoundException -> 0x0050, IOException -> 0x004d, all -> 0x004a }
            goto L_0x003c
        L_0x0046:
            r2.close()     // Catch:{ IOException -> 0x0066 }
            goto L_0x0066
        L_0x004a:
            r5 = move-exception
            r0 = r2
            goto L_0x006b
        L_0x004d:
            r5 = move-exception
            r0 = r2
            goto L_0x0056
        L_0x0050:
            r5 = move-exception
            r0 = r2
            goto L_0x0060
        L_0x0053:
            r5 = move-exception
            goto L_0x006b
        L_0x0055:
            r5 = move-exception
        L_0x0056:
            r5.printStackTrace()     // Catch:{ all -> 0x0053 }
            if (r0 == 0) goto L_0x0066
        L_0x005b:
            r0.close()     // Catch:{ IOException -> 0x0066 }
            goto L_0x0066
        L_0x005f:
            r5 = move-exception
        L_0x0060:
            r5.printStackTrace()     // Catch:{ all -> 0x0053 }
            if (r0 == 0) goto L_0x0066
            goto L_0x005b
        L_0x0066:
            java.lang.String r5 = r1.toString()
            goto L_0x0072
        L_0x006b:
            if (r0 == 0) goto L_0x0070
            r0.close()     // Catch:{ IOException -> 0x0070 }
        L_0x0070:
            throw r5
        L_0x0071:
            r5 = r0
        L_0x0072:
            boolean r0 = android.text.TextUtils.isEmpty(r5)
            if (r0 == 0) goto L_0x0080
            java.lang.String r5 = LOG_TAG
            java.lang.String r6 = "Error!!! no gls data in message"
            android.util.Log.e(r5, r6)
            return
        L_0x0080:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "XML BODY IS "
            r1.append(r2)
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r5)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.v(r0, r1)
            java.lang.String r1 = r5.toLowerCase()
            java.lang.String r2 = "geo"
            boolean r1 = r1.startsWith(r2)
            if (r1 == 0) goto L_0x00b0
            com.sec.internal.ims.servicemodules.gls.GlsGeoSmsParser r1 = new com.sec.internal.ims.servicemodules.gls.GlsGeoSmsParser
            r1.<init>()
            java.lang.String r5 = r1.getGlsExtInfo(r5)
            goto L_0x00b9
        L_0x00b0:
            com.sec.internal.ims.servicemodules.gls.GlsXmlParser r1 = new com.sec.internal.ims.servicemodules.gls.GlsXmlParser
            r1.<init>()
            java.lang.String r5 = r1.getGlsExtInfo(r5)
        L_0x00b9:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "THE EXTINFO IS "
            r1.append(r2)
            java.lang.String r2 = com.sec.internal.log.IMSLog.checker(r5)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            r6.updateExtInfo(r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.gls.GlsModule.updateExtInfo(com.sec.internal.ims.servicemodules.im.MessageBase):void");
    }

    private void updateFeatures(int i) {
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, i) != 1) {
            z = false;
        }
        if (!z) {
            Log.i(LOG_TAG, "updateFeatures: RCS is disabled");
            this.mPushEnabled[i] = false;
            return;
        }
        this.mPushEnabled[i] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, i), Boolean.FALSE).booleanValue();
        Log.i(LOG_TAG, "updateFeatures mPushEnabled: " + this.mPushEnabled[i]);
        for (int i2 = 0; i2 < this.phoneCount; i2++) {
            this.mConfigs.put(i2, this.mImModule.getImConfig(i2));
        }
    }

    public int getPhoneIdByChatId(String str) {
        return this.mImModule.getPhoneIdByChatId(str);
    }

    public int getPhoneIdByImdnId(String str, ImDirection imDirection) {
        return this.mImModule.getPhoneIdByImdnId(str, imDirection);
    }

    public int getPhoneIdByMessageId(int i) {
        return this.mImModule.getPhoneIdByMessageId(i);
    }
}
