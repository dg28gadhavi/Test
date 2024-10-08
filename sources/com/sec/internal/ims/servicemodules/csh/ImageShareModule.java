package com.sec.internal.ims.servicemodules.csh;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.gsma.services.rcs.sharing.image.ImageSharing;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.ims.servicemodules.csh.event.CshErrorReason;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.csh.event.IIshServiceInterface;
import com.sec.internal.ims.servicemodules.csh.event.IshFileTransfer;
import com.sec.internal.ims.servicemodules.csh.event.IshIncomingSessionEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshSessionEstablishedEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferCompleteEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferFailedEvent;
import com.sec.internal.ims.servicemodules.csh.event.IshTransferProgressEvent;
import com.sec.internal.ims.servicemodules.csh.event.OpenApiTranslationFilter;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StorageEnvironment;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ImageShareModule extends CshModuleBase implements IImageShareModule {
    private static final int EVENT_INCOMING_SESSION = 2;
    private static final int EVENT_SESSION_ESTABLISHED = 3;
    private static final int EVENT_TRANSFER_COMPLETE = 4;
    private static final int EVENT_TRANSFER_FAILED = 6;
    private static final int EVENT_TRANSFER_PROGRESS = 5;
    /* access modifiers changed from: private */
    public static String LOG_TAG = ImageShareModule.class.getSimpleName();
    public static final String NAME = ImageShareModule.class.getSimpleName();
    private boolean[] mHasImageShareSupport = {false, false};
    private IIshServiceInterface mImsService;
    private IshTranslation mIshTranslation;
    private final List<IImageShareEventListener> mListeners = new ArrayList();
    private long[] mMaxSize = {0, 0};
    private int mRegistrationId = -1;

    public long getWarnSize() {
        return 0;
    }

    public void notifyLimitExceeded(long j, ImsUri imsUri) {
    }

    public void onNetworkChanged(NetworkEvent networkEvent, int i) {
    }

    public ImageShareModule(Looper looper, Context context, IIshServiceInterface iIshServiceInterface) {
        super(looper, context);
        this.mImsService = iIshServiceInterface;
        this.mCache = CshCache.getInstance(iIshServiceInterface);
        this.mIshTranslation = new IshTranslation(this.mContext, this);
    }

    public String getName() {
        return NAME;
    }

    public String[] getServicesRequiring() {
        return new String[]{"is"};
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        updateFeatures(i);
    }

    public void start() {
        if (!isRunning()) {
            super.start();
            this.mImsService.registerForIshIncomingSession(this, 2, (Object) null);
            this.mImsService.registerForIshSessionEstablished(this, 3, (Object) null);
            this.mImsService.registerForIshTransferComplete(this, 4, (Object) null);
            this.mImsService.registerForIshTransferProgress(this, 5, (Object) null);
            this.mImsService.registerForIshTransferFailed(this, 6, (Object) null);
        }
    }

    public void stop() {
        for (int i = 0; i < this.mCache.getSize(); i++) {
            if (this.mCache.getSessionAt(i).getContent().shareType == 1) {
                ((ImageShare) this.mCache.getSessionAt(i)).sessionFailed();
            }
        }
        super.stop();
        disableIshFeature();
        this.mImsService.unregisterForIshIncomingSession(this);
        this.mImsService.unregisterForIshSessionEstablished(this);
        this.mImsService.unregisterForIshTransferComplete(this);
        this.mImsService.unregisterForIshTransferProgress(this);
        this.mImsService.unregisterForIshTransferFailed(this);
    }

    public void onConfigured(int i) {
        String str = LOG_TAG;
        Log.d(str, "onConfigured: phoneId = " + i);
        updateFeatures(i);
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        Log.i(LOG_TAG, "onRegistered");
        if (imsRegistration.getImsProfile() != null) {
            this.mRegistrationId = getRegistrationInfoId(imsRegistration);
        }
        updateServiceStatus(imsRegistration.getPhoneId());
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        Log.i(LOG_TAG, "onDeregistered");
        super.onDeregistered(imsRegistration, i);
        if (getImsRegistration() != null) {
            this.mRegistrationId = -1;
            updateServiceStatus(imsRegistration.getPhoneId());
        }
    }

    public long getMaxSize() {
        return this.mMaxSize[this.mActiveCallPhoneId];
    }

    public Future<ImageShare> createShare(final ImsUri imsUri, final String str) {
        FutureTask futureTask = new FutureTask(new Callable<ImageShare>() {
            public ImageShare call() throws Exception {
                Log.i(ImageShareModule.LOG_TAG, "createShare");
                ImageShareModule imageShareModule = ImageShareModule.this;
                ImageShare newOutgoingImageShare = imageShareModule.mCache.newOutgoingImageShare(imageShareModule, imsUri, str);
                if (newOutgoingImageShare != null) {
                    newOutgoingImageShare.startQutgoingSession();
                }
                return newOutgoingImageShare;
            }
        });
        post(futureTask);
        return futureTask;
    }

    public void acceptShare(long j) {
        String str = LOG_TAG;
        Log.i(str, "acceptShare sharedId " + j);
        ImageShare session = getSession(j);
        if (session == null) {
            String str2 = LOG_TAG;
            Log.w(str2, "Detected illegal share id passed from intent. Was " + j);
            this.mIshTranslation.broadcastCommunicationError();
        } else if (StorageEnvironment.isSdCardStateFine(session.getFileSize())) {
            session.acceptIncomingSession();
        } else {
            session.incomingSessionPreReject();
        }
    }

    public void cancelShare(long j) {
        String str = LOG_TAG;
        Log.i(str, "cancelShare sharedId " + j);
        ImageShare session = getSession(j);
        if (session != null) {
            session.cancelByLocalSession();
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Detected illegal share id passed from intent. Was " + j);
        this.mIshTranslation.broadcastCommunicationError();
    }

    public ImageShare getSession(long j) {
        try {
            return (ImageShare) super.getSession(j);
        } catch (ClassCastException unused) {
            String str = LOG_TAG;
            Log.w(str, j + " is not Image Share");
            return null;
        }
    }

    public void ishSessionEstablishedEvent(IshSessionEstablishedEvent ishSessionEstablishedEvent) {
        ImageShare imageShare = (ImageShare) this.mCache.getSession(ishSessionEstablishedEvent.mSessionId);
        String str = LOG_TAG;
        Log.i(str, "ishSessionEstablishedEvent sessionId : " + ishSessionEstablishedEvent.mSessionId);
        if (imageShare != null) {
            imageShare.mContent.reasonCode = Integer.valueOf(ImageSharing.ReasonCode.FAILED_SHARING.toString()).intValue();
            imageShare.sessioinEstablished();
            this.mIshTranslation.broadcastConnected(imageShare.getContent().shareId, imageShare.getContent().shareContactUri);
            return;
        }
        Log.e(LOG_TAG, "Session is Not found");
    }

    public void ishTransferFailedEvent(IshTransferFailedEvent ishTransferFailedEvent) {
        int i = ishTransferFailedEvent.mSessionId;
        CshErrorReason cshErrorReason = ishTransferFailedEvent.mReason;
        String str = LOG_TAG;
        Log.i(str, "ishTransferFailedEvent sessionId : " + i + " Reason : " + cshErrorReason);
        ImageShare imageShare = (ImageShare) this.mCache.getSession(i);
        ImsRegistration imsRegistration = getImsRegistration();
        if (cshErrorReason == CshErrorReason.FORBIDDEN) {
            IRegistrationGovernor registrationGovernor = imsRegistration != null ? ImsRegistry.getRegistrationManager().getRegistrationGovernor(imsRegistration.getHandle()) : null;
            if (registrationGovernor != null) {
                registrationGovernor.onSipError("ish_tapi", new SipError(403, "Forbidden"));
            }
        }
        if (imageShare != null) {
            imageShare.sessionFailed();
            if (imsRegistration != null) {
                getServiceModuleManager().getCapabilityDiscoveryModule().exchangeCapabilitiesForVSH(imsRegistration.getPhoneId(), true);
            }
            IshTranslation ishTranslation = this.mIshTranslation;
            CshInfo cshInfo = imageShare.mContent;
            ishTranslation.broadcastCanceled(cshInfo.shareId, cshInfo.shareContactUri, cshInfo.shareDirection, ishReasonTranslator(cshErrorReason));
            if (ishTransferFailedEvent.mReason == CshErrorReason.MSRP_TIMEOUT) {
                this.mIshTranslation.broadcastCshServiceNotReady();
                return;
            }
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
    }

    public void ishCancelFailed(int i) {
        ImageShare imageShare = (ImageShare) this.mCache.getSession(i);
        if (imageShare != null) {
            imageShare.sessionFailed();
            IshTranslation ishTranslation = this.mIshTranslation;
            CshInfo cshInfo = imageShare.mContent;
            ishTranslation.broadcastCanceled(cshInfo.shareId, cshInfo.shareContactUri, cshInfo.shareDirection, 12);
            String str = LOG_TAG;
            Log.i(str, "ishCancelFailed sessionId : " + i + " broadcast finished");
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
    }

    public void ishTransferCompleteEvent(IshTransferCompleteEvent ishTransferCompleteEvent) {
        ImageShare imageShare = (ImageShare) this.mCache.getSession(ishTransferCompleteEvent.mSessionId);
        if (imageShare != null) {
            imageShare.transferCompleted();
            this.mIshTranslation.broadcastCompleted(imageShare.mContent.shareId, imageShare.getContent().shareContactUri);
            if (imageShare.getContent().shareDirection == 0) {
                this.mIshTranslation.broadcastSystemRefresh(imageShare.getContent().dataPath);
                return;
            }
            return;
        }
        Log.d(LOG_TAG, "Already removed session");
    }

    public void ishTransferProgressEvent(IshTransferProgressEvent ishTransferProgressEvent) {
        ImageShare imageShare = (ImageShare) this.mCache.getSession(ishTransferProgressEvent.mSessionId);
        if (imageShare != null) {
            long j = ishTransferProgressEvent.mProgress;
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = LOG_TAG;
            Log.i(str, "progressing for in_progress state: " + ((100 * j) / imageShare.getContent().dataSize) + "%");
            imageShare.getContent().dataProgress = j;
            contentResolver.notifyChange(ICshConstants.ShareDatabase.ACTIVE_SESSIONS_URI, (ContentObserver) null);
            this.mIshTranslation.broadcastProgress(imageShare.mContent.shareId, imageShare.getContent().shareContactUri, j, imageShare.getContent().dataSize);
            for (IImageShareEventListener onIshTransferProgressEvent : this.mListeners) {
                onIshTransferProgressEvent.onIshTransferProgressEvent(String.valueOf(imageShare.mContent.shareId), j);
            }
            return;
        }
        Log.e(LOG_TAG, "Session is Not found");
    }

    public void ishIncomingSessionEvent(IshIncomingSessionEvent ishIncomingSessionEvent) {
        final int i = ishIncomingSessionEvent.mSessionId;
        final ImsUri imsUri = ishIncomingSessionEvent.mRemoteUri;
        String str = ishIncomingSessionEvent.mUserAlias;
        final IshFileTransfer ishFileTransfer = ishIncomingSessionEvent.mFt;
        String str2 = LOG_TAG;
        Log.i(str2, "onIshIncomingSessionEvent( #" + i + ", " + IMSLog.checker(imsUri) + "," + IMSLog.checker(str) + "): Enter");
        if (ishFileTransfer.getMimeType().startsWith(OpenApiTranslationFilter.SOS_CONTENT_TYPE_PREFIX)) {
            Log.v(LOG_TAG, "Skipping OpenAPI incoming session message");
        } else {
            post(new Runnable() {
                public void run() {
                    ImageShareModule imageShareModule = ImageShareModule.this;
                    ImageShare newIncommingImageShare = imageShareModule.mCache.newIncommingImageShare(imageShareModule, i, imsUri, ishFileTransfer);
                    if (newIncommingImageShare != null) {
                        Log.d(ImageShareModule.LOG_TAG, "created incoming session");
                        newIncommingImageShare.incomingSessionDone();
                    }
                }
            });
        }
    }

    public void notityIncommingSession(long j, ImsUri imsUri, String str, long j2) {
        this.mIshTranslation.broadcastIncomming(j, imsUri, str, j2);
    }

    public void notifyInvalidDataPath(String str) {
        this.mIshTranslation.broadcastInvalidDataPath(str);
    }

    private void disableIshFeature() {
        Log.d(LOG_TAG, "disableIshFeature");
        ModuleChannel.createChannel(ModuleChannel.CAPDISCOVERY, this).disableFeature((long) Capabilities.FEATURE_ISH);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.csh.ImageShareModule$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
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
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.REMOTE_CONNECTION_CLOSED     // Catch:{ NoSuchFieldError -> 0x001d }
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
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.USER_BUSY     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.TEMPORAIRLY_NOT_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.NOT_REACHABLE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.ENGINE_ERROR     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.FILE_IO     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.FORMAT_NOT_SUPPORTED     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.USER_NOT_FOUND     // Catch:{ NoSuchFieldError -> 0x0078 }
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
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.NONE     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.REQUEST_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.ACK_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.ims.servicemodules.csh.event.CshErrorReason r1 = com.sec.internal.ims.servicemodules.csh.event.CshErrorReason.FORBIDDEN     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.csh.ImageShareModule.AnonymousClass3.<clinit>():void");
        }
    }

    private int ishReasonTranslator(CshErrorReason cshErrorReason) {
        switch (AnonymousClass3.$SwitchMap$com$sec$internal$ims$servicemodules$csh$event$CshErrorReason[cshErrorReason.ordinal()]) {
            case 1:
            case 2:
                return 10;
            case 3:
            case 4:
            case 5:
            case 6:
                return 4;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                return 3;
            case 13:
            case 14:
                return 6;
            case 15:
                return 12;
            default:
                return 9;
        }
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        int i = message.what;
        if (i == 2) {
            ishIncomingSessionEvent((IshIncomingSessionEvent) ((AsyncResult) message.obj).result);
        } else if (i == 3) {
            ishSessionEstablishedEvent((IshSessionEstablishedEvent) ((AsyncResult) message.obj).result);
        } else if (i == 4) {
            ishTransferCompleteEvent((IshTransferCompleteEvent) ((AsyncResult) message.obj).result);
        } else if (i == 5) {
            ishTransferProgressEvent((IshTransferProgressEvent) ((AsyncResult) message.obj).result);
        } else if (i == 6) {
            ishTransferFailedEvent((IshTransferFailedEvent) ((AsyncResult) message.obj).result);
        }
    }

    public void handleIntent(Intent intent) {
        this.mIshTranslation.handleIntent(intent);
    }

    private void readConfig(int i) {
        this.mHasImageShareSupport[i] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_IS_AUTH, i), Boolean.FALSE).booleanValue();
        this.mMaxSize[i] = (long) RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_MAX_SIZE_IMAGE_SHARE, i), 0).intValue();
        String str = LOG_TAG;
        Log.i(str, "readConfig phonId : " + i + " ImageShare enable " + this.mHasImageShareSupport[i] + ", ImageShare Max size " + this.mMaxSize[i]);
        if (SimUtil.getSimMno(i) == Mno.SPRINT && this.mHasImageShareSupport[i]) {
            Log.d(LOG_TAG, "readconfig: isauth true but forced disable for SPRINT");
            disableIshFeature();
            this.mHasImageShareSupport[i] = false;
        }
    }

    /* access modifiers changed from: protected */
    public void updateServiceStatus(int i) {
        super.updateServiceStatus(i);
        boolean z = !this.mIsDuringMultipartyCall && getImsRegistration() != null && CapabilityUtil.hasFeature(this.mEnabledFeatures[i], (long) Capabilities.FEATURE_ISH) && this.mRemoteCapabilities.hasFeature(Capabilities.FEATURE_ISH);
        if (this.mIsServiceReady != z) {
            this.mIsServiceReady = z;
            if (z) {
                this.mIshTranslation.broadcastServiceReady();
            } else {
                this.mIshTranslation.broadcastServiceNotReady();
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
        String str = LOG_TAG;
        Log.i(str, "updateFeatures: phoneId: " + i);
        readConfig(i);
        boolean z = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, i) == 1;
        if (!this.mHasImageShareSupport[i] || !z || DmConfigHelper.getImsSwitchValue(this.mContext, "is", i) != 1) {
            Log.d(LOG_TAG, "updateFeatures: RCS is disabled.");
            this.mEnabledFeatures[i] = 0;
            return;
        }
        this.mEnabledFeatures[i] = (long) Capabilities.FEATURE_ISH;
    }

    public void registerImageShareEventListener(IImageShareEventListener iImageShareEventListener) {
        this.mListeners.add(iImageShareEventListener);
    }
}
