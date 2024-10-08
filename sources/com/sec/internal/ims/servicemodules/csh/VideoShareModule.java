package com.sec.internal.ims.servicemodules.csh;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.ims.servicemodules.csh.event.CshErrorReason;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshSuccessCallback;
import com.sec.internal.ims.servicemodules.csh.event.IvshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.OpenApiTranslationFilter;
import com.sec.internal.ims.servicemodules.csh.event.VshIncomingSessionEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshIntents;
import com.sec.internal.ims.servicemodules.csh.event.VshOrientation;
import com.sec.internal.ims.servicemodules.csh.event.VshSessionEstablishedEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshSessionTerminatedEvent;
import com.sec.internal.ims.servicemodules.csh.event.VshSwitchCameraParams;
import com.sec.internal.ims.servicemodules.csh.event.VshVideoDisplayParams;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.DeviceOrientationStatus;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class VideoShareModule extends CshModuleBase implements IVideoShareModule {
    private static final int DISABLED_ALL_COVERAGES = 0;
    private static final int ENABLED_3G_COVERAGES = 4;
    private static final int ENABLED_ALL_COVERAGES = 1;
    private static final int ENABLED_HSPA_COVERAGES = 8;
    private static final int ENABLED_LTE_COVERAGES = 16;
    private static final int ENABLED_WLAN_COVERAGES = 2;
    private static final int EVENT_CANCEL_SHARE = 5;
    private static final int EVENT_INCOMING_SESSION = 2;
    private static final int EVENT_SESSION_ESTABLISHED = 3;
    private static final int EVENT_SESSION_TEMINATED = 4;
    /* access modifiers changed from: private */
    public static final String EXTRA_SESSIONID = (VideoShareModule.class.getName() + "SessionID");
    /* access modifiers changed from: private */
    public static final String INTENT_MAX_DURATION_TIME = (VideoShareModule.class.getName() + ".max_duration_time");
    /* access modifiers changed from: private */
    public static String LOG_TAG = VideoShareModule.class.getSimpleName();
    public static final String NAME = VideoShareModule.class.getSimpleName();
    private boolean[] mHasVideoShareSupport = {false, false};
    private final IvshServiceInterface mImsService;
    /* access modifiers changed from: private */
    public int mInComingTerminateId = -1;
    private long[] mInitialFeatures = {0, 0};
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(VideoShareModule.INTENT_MAX_DURATION_TIME)) {
                int intExtra = intent.getIntExtra(VideoShareModule.EXTRA_SESSIONID, -1);
                VideoShare session = VideoShareModule.this.getSession(intExtra);
                if (session != null) {
                    String r1 = VideoShareModule.LOG_TAG;
                    Log.d(r1, "Session #" + intExtra + " duration is approaching/longer than the VS MAX DURATION :" + VideoShareModule.this.maxDurationTime[VideoShareModule.this.mActiveCallPhoneId] + "s");
                    session.maxDurationTime();
                    return;
                }
                String r3 = VideoShareModule.LOG_TAG;
                Log.w(r3, "Session #" + intExtra + " is not found");
            }
        }
    };
    private int[] mNetworkType = {0, 0};
    private int mRegistrationId = -1;
    private UriGenerator mUriGenerator;
    private int[] mVsAuth = {0, 0};
    /* access modifiers changed from: private */
    public boolean mVshInComingEntered = false;
    /* access modifiers changed from: private */
    public final VshTranslation mVshTranslation;
    /* access modifiers changed from: private */
    public int[] maxDurationTime = {0, 0};

    private int blurNetworkType(int i) {
        if (!(i == 1 || i == 2)) {
            if (i == 15) {
                return 10;
            }
            if (i != 16) {
                switch (i) {
                    case 8:
                    case 9:
                    case 10:
                        return 10;
                    default:
                        return i;
                }
            }
        }
        return 16;
    }

    public VideoShareModule(Looper looper, Context context, IvshServiceInterface ivshServiceInterface) {
        super(looper, context);
        this.mImsService = ivshServiceInterface;
        this.mCache = CshCache.getInstance(ivshServiceInterface);
        this.mVshTranslation = new VshTranslation(this.mContext, this);
        this.mUriGenerator = UriGeneratorFactory.getInstance().get(UriGenerator.URIServiceType.RCS_URI);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_MAX_DURATION_TIME);
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter, (String) null, this);
    }

    public String getName() {
        return NAME;
    }

    public String[] getServicesRequiring() {
        return new String[]{"vs"};
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        updateFeatures(i);
    }

    public void start() {
        if (!isRunning()) {
            super.start();
            this.mImsService.registerForVshIncomingSession(this, 2, (Object) null);
            this.mImsService.registerForVshSessionEstablished(this, 3, (Object) null);
            this.mImsService.registerForVshSessionTerminated(this, 4, (Object) null);
        }
    }

    public void stop() {
        for (int i = 0; i < this.mCache.getSize(); i++) {
            if (this.mCache.getSessionAt(i).getContent().shareType == 2) {
                ((VideoShare) this.mCache.getSessionAt(i)).sessionFailed();
            }
        }
        super.stop();
        disableVshFeature();
        this.mImsService.unregisterForVshIncomingSession(this);
        this.mImsService.unregisterForVshSessionEstablished(this);
        this.mImsService.unregisterForVshSessionTerminated(this);
    }

    public void onConfigured(int i) {
        String str = LOG_TAG;
        Log.d(str, "onConfigured: phoneId = " + i);
        updateFeatures(i);
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        String str = LOG_TAG;
        Log.i(str, "onRegistered() phoneId = " + imsRegistration.getPhoneId() + ", services : " + imsRegistration.getServices());
        if (imsRegistration.getImsProfile() != null) {
            this.mRegistrationId = getRegistrationInfoId(imsRegistration);
        }
        updateServiceStatus(imsRegistration.getPhoneId());
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (telephonyManager != null) {
            int networkType = telephonyManager.getNetworkType();
            if (this.mNetworkType[imsRegistration.getPhoneId()] != 18) {
                this.mNetworkType[imsRegistration.getPhoneId()] = blurNetworkType(networkType);
            }
        }
        this.mHasVideoShareSupport[imsRegistration.getPhoneId()] = isVsEnabled(this.mNetworkType[imsRegistration.getPhoneId()], imsRegistration.getPhoneId());
        if (this.mHasVideoShareSupport[imsRegistration.getPhoneId()]) {
            Log.i(LOG_TAG, "enable VSH");
            this.mEnabledFeatures[imsRegistration.getPhoneId()] = (long) Capabilities.FEATURE_VSH;
        } else {
            Log.i(LOG_TAG, "disable VSH");
            this.mEnabledFeatures[imsRegistration.getPhoneId()] = 0;
        }
        ICapabilityDiscoveryModule capabilityDiscoveryModule = getServiceModuleManager().getCapabilityDiscoveryModule();
        capabilityDiscoveryModule.updateOwnCapabilities(imsRegistration.getPhoneId());
        capabilityDiscoveryModule.exchangeCapabilitiesForVSHOnRegi(this.mHasVideoShareSupport[imsRegistration.getPhoneId()], imsRegistration.getPhoneId());
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        Log.i(LOG_TAG, "onDeregistered");
        super.onDeregistered(imsRegistration, i);
        if (getImsRegistration() == null) {
            this.mEnabledFeatures[imsRegistration.getPhoneId()] = this.mInitialFeatures[imsRegistration.getPhoneId()];
            return;
        }
        this.mRegistrationId = -1;
        updateServiceStatus(imsRegistration.getPhoneId());
        this.mEnabledFeatures[imsRegistration.getPhoneId()] = this.mInitialFeatures[imsRegistration.getPhoneId()];
    }

    public void onNetworkChanged(NetworkEvent networkEvent, int i) {
        int i2;
        if (networkEvent.isWifiConnected) {
            i2 = 18;
        } else {
            i2 = networkEvent.network;
        }
        if (getImsRegistration() != null) {
            Log.i(LOG_TAG, "onNetworkChanged: " + networkEvent + " network: " + i2);
            if (i2 != this.mNetworkType[i] && i2 == 3) {
                for (int i3 = 0; i3 < this.mCache.getSize(); i3++) {
                    IContentShare sessionAt = this.mCache.getSessionAt(i3);
                    if (sessionAt != null && sessionAt.getContent().shareType == 2 && sessionAt.getContent().shareDirection == 1) {
                        sendMessage(obtainMessage(5, Long.valueOf(sessionAt.getContent().shareId)));
                    }
                }
            }
            if (i2 != this.mNetworkType[i]) {
                boolean isVsEnabled = isVsEnabled(i2, i);
                if (this.mHasVideoShareSupport[i] != isVsEnabled) {
                    if (isVsEnabled) {
                        Log.i(LOG_TAG, "enable VSH");
                        this.mEnabledFeatures[i] = (long) Capabilities.FEATURE_VSH;
                    } else {
                        Log.i(LOG_TAG, "disable VSH");
                        this.mEnabledFeatures[i] = 0;
                    }
                    ICapabilityDiscoveryModule capabilityDiscoveryModule = getServiceModuleManager().getCapabilityDiscoveryModule();
                    capabilityDiscoveryModule.updateOwnCapabilities(i);
                    if (!(i2 == 18 || this.mNetworkType[i] == 18)) {
                        capabilityDiscoveryModule.exchangeCapabilitiesForVSHOnRegi(isVsEnabled, i);
                    }
                }
                this.mHasVideoShareSupport[i] = isVsEnabled;
            }
        }
        this.mNetworkType[i] = i2;
    }

    public void onCallStateChanged(int i, List<ICall> list) {
        processCallStateChanged(i, new CopyOnWriteArrayList(list));
    }

    private void processCallStateChanged(int i, CopyOnWriteArrayList<ICall> copyOnWriteArrayList) {
        super.onCallStateChanged(i, copyOnWriteArrayList);
        if (this.mNPrevConnectedCalls == 0 || this.mIsDuringMultipartyCall) {
            for (int i2 = 0; i2 < this.mCache.getSize(); i2++) {
                if (this.mCache.getSessionAt(i2).getContent().shareType == 2) {
                    Log.i(LOG_TAG, "processCallStateChanged: call cancelByUserSession");
                    ((VideoShare) this.mCache.getSessionAt(i2)).cancelByUserSession();
                }
            }
        }
    }

    public int getMaxDurationTime() {
        return this.maxDurationTime[this.mActiveCallPhoneId];
    }

    public Context getContext() {
        return this.mContext;
    }

    public Future<VideoShare> createShare(final ImsUri imsUri, final String str) {
        FutureTask futureTask = new FutureTask(new Callable<VideoShare>() {
            public VideoShare call() throws Exception {
                Log.i(VideoShareModule.LOG_TAG, "createShare");
                if (VideoShareModule.this.getImsRegistration() == null) {
                    VideoShareModule.this.mVshTranslation.broadcastCommunicationError();
                    return null;
                }
                VideoShareModule videoShareModule = VideoShareModule.this;
                VideoShare newOutgoingVideoShare = videoShareModule.mCache.newOutgoingVideoShare(videoShareModule, imsUri, str);
                if (newOutgoingVideoShare != null) {
                    newOutgoingVideoShare.startQutgoingSession();
                }
                return newOutgoingVideoShare;
            }
        });
        post(futureTask);
        return futureTask;
    }

    public void acceptShare(long j) {
        String str = LOG_TAG;
        Log.i(str, "acceptShare sharedId " + j);
        VideoShare session = getSession(j);
        if (session != null) {
            session.acceptIncomingSession();
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + j);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public void cancelShare(long j) {
        String str = LOG_TAG;
        Log.i(str, "cancelShare sharedId " + j);
        VideoShare session = getSession(j);
        if (session != null) {
            session.cancelByUserSession();
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + j);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public void toggleCamera(long j) {
        String str = LOG_TAG;
        Log.i(str, "toggleCamera sharedId " + j);
        if (getSession(j) != null) {
            this.mImsService.switchCamera(new VshSwitchCameraParams(new ICshSuccessCallback() {
                public void onSuccess() {
                }

                public void onFailure() {
                    Log.d(VideoShareModule.LOG_TAG, "IToggleCamera onFailure");
                }
            }));
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + j);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public void changeSurfaceOrientation(long j, int i) {
        VideoShare session = getSession(j);
        if (session != null) {
            session.setPhoneOrientation(DeviceOrientationStatus.translate(i));
            String str = LOG_TAG;
            Log.i(str, "changeSurfaceOrientation sharedId : " + j + " onSuccess");
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + j);
        this.mVshTranslation.broadcastCommunicationError();
    }

    public VideoShare getSession(long j) {
        try {
            return (VideoShare) super.getSession(j);
        } catch (ClassCastException unused) {
            String str = LOG_TAG;
            Log.w(str, j + " is not Video Share");
            return null;
        }
    }

    public VideoShare getSession(int i) {
        try {
            return (VideoShare) this.mCache.getSession(i);
        } catch (ClassCastException unused) {
            String str = LOG_TAG;
            Log.w(str, i + " is not Video Share");
            return null;
        }
    }

    private void disableVshFeature() {
        Log.d(LOG_TAG, "disableVshFeature");
        ModuleChannel.createChannel(ModuleChannel.CAPDISCOVERY, this).disableFeature((long) Capabilities.FEATURE_VSH);
    }

    private void vshIncomingSessionEvent(VshIncomingSessionEvent vshIncomingSessionEvent) {
        final int i = vshIncomingSessionEvent.mSessionId;
        final ImsUri imsUri = vshIncomingSessionEvent.mRemoteUri;
        String str = vshIncomingSessionEvent.mContentType;
        final int i2 = vshIncomingSessionEvent.mSource;
        final String str2 = vshIncomingSessionEvent.mFilePath;
        String str3 = LOG_TAG;
        Log.i(str3, "vshIncomingSessionEvent #" + i + ", " + IMSLog.checker(imsUri));
        this.mVshInComingEntered = true;
        this.mInComingTerminateId = -1;
        if (str == null || !str.startsWith(OpenApiTranslationFilter.SOS_CONTENT_TYPE_PREFIX)) {
            post(new Runnable() {
                public void run() {
                    String str;
                    if (i == VideoShareModule.this.mInComingTerminateId) {
                        Log.d(VideoShareModule.LOG_TAG, "InComing Video Share is already cancelled by stack");
                        VideoShareModule.this.mVshInComingEntered = false;
                        VideoShareModule.this.mInComingTerminateId = -1;
                        return;
                    }
                    if (i2 == 1) {
                        str = VshIntents.LIVE_VIDEO_CONTENTPATH;
                    } else {
                        str = str2;
                    }
                    VideoShareModule videoShareModule = VideoShareModule.this;
                    VideoShare newIncommingVideoShare = videoShareModule.mCache.newIncommingVideoShare(videoShareModule, i, imsUri, str);
                    if (newIncommingVideoShare != null) {
                        Log.i(VideoShareModule.LOG_TAG, "created incoming session");
                        newIncommingVideoShare.incomingSessionDone();
                    }
                    VideoShareModule.this.mVshInComingEntered = false;
                    VideoShareModule.this.mInComingTerminateId = -1;
                }
            });
        } else {
            Log.v(LOG_TAG, "Skipping OpenAPI incoming session message");
        }
    }

    public void notityIncommingSession(long j, ImsUri imsUri, String str) {
        ImsUri normalizedUri;
        boolean z = false;
        for (Integer num : this.mActiveCallLists.keySet()) {
            Iterator it = this.mActiveCallLists.get(num).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ICall iCall = (ICall) it.next();
                if (iCall.isConnected() && (normalizedUri = this.mUriGenerator.getNormalizedUri(iCall.getNumber(), true)) != null && normalizedUri.equals(imsUri)) {
                    z = true;
                    break;
                }
            }
        }
        if (!z) {
            sendMessage(obtainMessage(5, Long.valueOf(j)));
        } else {
            this.mVshTranslation.broadcastIncomming(j, imsUri, str);
        }
    }

    public void notifyApprochingVsMaxDuration(long j, int i) {
        this.mVshTranslation.broadcastApproachingVsMaxDuration(j, i);
    }

    private void vshSessionEstablishedEvent(VshSessionEstablishedEvent vshSessionEstablishedEvent) {
        PendingIntent pendingIntent;
        String str = LOG_TAG;
        Log.i(str, "vshSessionEstablishedEvent session #" + vshSessionEstablishedEvent.mSessionId);
        VideoShare session = getSession(vshSessionEstablishedEvent.mSessionId);
        if (session != null) {
            if (this.maxDurationTime[this.mActiveCallPhoneId] != 0) {
                Intent intent = new Intent(INTENT_MAX_DURATION_TIME);
                intent.putExtra(EXTRA_SESSIONID, vshSessionEstablishedEvent.mSessionId);
                pendingIntent = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
            } else {
                pendingIntent = null;
            }
            session.sessioinEstablished(vshSessionEstablishedEvent.mResolution, pendingIntent);
            this.mVshTranslation.broadcastConnected(session.getContent().shareId, session.getContent().shareContactUri);
            session.setPhoneOrientation(DeviceOrientationStatus.getDeviceOrientation(this.mContext));
            return;
        }
        Log.e(LOG_TAG, "Session is Not found");
    }

    private void vshSessionTerminatedEvent(VshSessionTerminatedEvent vshSessionTerminatedEvent) {
        String str = LOG_TAG;
        Log.i(str, "vshSessionTerminatedEvent session #" + vshSessionTerminatedEvent.mSessionId + " Reason : " + vshSessionTerminatedEvent.mReason);
        VideoShare session = getSession(vshSessionTerminatedEvent.mSessionId);
        ImsRegistration imsRegistration = getImsRegistration();
        if (session != null) {
            session.sessionTerminatedByStack();
            CshInfo content = session.getContent();
            if (imsRegistration != null) {
                getServiceModuleManager().getCapabilityDiscoveryModule().exchangeCapabilitiesForVSH(imsRegistration.getPhoneId(), true);
            }
            int vshReasonTranslator = vshReasonTranslator(vshSessionTerminatedEvent.mReason);
            content.reasonCode = vshReasonTranslator;
            this.mVshTranslation.broadcastCanceled(content.shareId, content.shareContactUri, content.shareDirection, vshReasonTranslator);
            if (vshSessionTerminatedEvent.mReason == CshErrorReason.RTP_RTCP_TIMEOUT) {
                this.mVshTranslation.broadcastCshServiceNotReady();
            }
            if (vshSessionTerminatedEvent.mReason == CshErrorReason.CSH_CAM_ERROR) {
                this.mVshTranslation.broadcastCshCamError();
                return;
            }
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
        if (this.mVshInComingEntered) {
            this.mInComingTerminateId = vshSessionTerminatedEvent.mSessionId;
        }
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        int i = message.what;
        if (i == 2) {
            vshIncomingSessionEvent((VshIncomingSessionEvent) ((AsyncResult) message.obj).result);
        } else if (i == 3) {
            vshSessionEstablishedEvent((VshSessionEstablishedEvent) ((AsyncResult) message.obj).result);
        } else if (i == 4) {
            vshSessionTerminatedEvent((VshSessionTerminatedEvent) ((AsyncResult) message.obj).result);
        } else if (i == 5) {
            cancelShare(((Long) message.obj).longValue());
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.csh.VideoShareModule$5  reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason;

        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|32) */
        /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason[] r0 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason = r0
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.CANCELED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.USER_BUSY     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.REJECTED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.TEMPORAIRLY_NOT_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.ENGINE_ERROR     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.FILE_IO     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.FORMAT_NOT_SUPPORTED     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.REQUEST_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.USER_NOT_FOUND     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.ACK_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.BEARER_LOST     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.NORMAL     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.FORBIDDEN     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.RTP_RTCP_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.csh.VideoShareModule.AnonymousClass5.<clinit>():void");
        }
    }

    private int vshReasonTranslator(CshErrorReason cshErrorReason) {
        switch (AnonymousClass5.$SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[cshErrorReason.ordinal()]) {
            case 1:
                return 2;
            case 2:
            case 3:
            case 4:
                return 4;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return 3;
            case 12:
            case 13:
                return 10;
            case 14:
                return 13;
            case 15:
                return 6;
            default:
                return 9;
        }
    }

    public void handleIntent(Intent intent) {
        this.mVshTranslation.handleIntent(intent);
    }

    public void setVshPhoneOrientation(VshOrientation vshOrientation) {
        this.mImsService.setVshPhoneOrientation(vshOrientation);
    }

    public void setVshVideoDisplay(VshVideoDisplayParams vshVideoDisplayParams) {
        this.mImsService.setVshVideoDisplay(vshVideoDisplayParams);
    }

    public void resetVshVideoDisplay(VshVideoDisplayParams vshVideoDisplayParams) {
        this.mImsService.resetVshVideoDisplay(vshVideoDisplayParams);
    }

    private void readConfig(int i) {
        this.mVsAuth[i] = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_VS_AUTH, i), 0).intValue();
        String str = LOG_TAG;
        Log.i(str, "readConfig: VsAuth " + this.mVsAuth[i]);
        this.maxDurationTime[i] = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId("maxtimevideoshare", i), 0).intValue();
        if (SimUtil.getSimMno(i) == Mno.SPRINT && this.mVsAuth[i] == 1) {
            Log.d(LOG_TAG, "readconfig: vsauth true but forced disable for SPRINT");
            disableVshFeature();
            this.mVsAuth[i] = 0;
        }
    }

    public boolean isVsEnabled(int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "networkType is " + i + ", VsAuth is " + this.mVsAuth[i2]);
        int i3 = this.mVsAuth[i2];
        if (i3 == 0) {
            return false;
        }
        if ((i3 & 1) > 0) {
            return true;
        }
        switch (i) {
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 12:
                return (i3 & 4) > 0;
            case 10:
            case 15:
                return (i3 & 8) > 0;
            case 13:
            case 20:
                return (i3 & 16) > 0;
            case 18:
                return (i3 & 2) > 0;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public void updateServiceStatus(int i) {
        super.updateServiceStatus(i);
        boolean z = !this.mIsDuringMultipartyCall && getImsRegistration() != null && CapabilityUtil.hasFeature(this.mEnabledFeatures[i], (long) Capabilities.FEATURE_VSH) && this.mRemoteCapabilities.hasFeature(Capabilities.FEATURE_VSH);
        if (this.mIsServiceReady != z) {
            this.mIsServiceReady = z;
            if (z) {
                this.mVshTranslation.broadcastServiceReady();
            } else {
                this.mVshTranslation.broadcastServiceNotReady();
            }
        }
    }

    public ImsRegistration getImsRegistration() {
        if (this.mRegistrationId != -1) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId);
        }
        return null;
    }

    private void updateFeatures(int i) {
        readConfig(i);
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
        if (telephonyManager != null) {
            int networkType = telephonyManager.getNetworkType();
            int[] iArr = this.mNetworkType;
            if (iArr[i] != 18) {
                iArr[i] = blurNetworkType(networkType);
            }
        }
        this.mHasVideoShareSupport[i] = isVsEnabled(this.mNetworkType[i], i);
        Log.i(LOG_TAG, "updateFeatures: phoneId " + i + ", HasVideoShareSupport = " + this.mHasVideoShareSupport[i]);
        boolean z = false;
        boolean z2 = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, i) == 1;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "vs", i) == 1) {
            z = true;
        }
        if (this.mVsAuth[i] == 0 || !z2 || !z) {
            Log.d(LOG_TAG, "updateFeatures: RCS is disabled.");
            this.mEnabledFeatures[i] = 0;
        } else {
            this.mEnabledFeatures[i] = (long) Capabilities.FEATURE_VSH;
        }
        this.mInitialFeatures[i] = this.mEnabledFeatures[i];
    }
}
