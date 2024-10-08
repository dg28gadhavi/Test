package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import com.gsma.services.rcs.CommonServiceConfiguration;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.tapi.service.api.interfaces.ITapiServiceManager;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.ims.util.TapiServiceUtil;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;

public class TapiServiceManager extends ServiceModuleBase implements ITapiServiceManager {
    private static CapabilityServiceImpl mCapabilityServiceImpl;
    private static ChatServiceImpl mChatServiceImpl;
    private static ContactServiceImpl mContactServiceImpl;
    private static FileTransferingServiceImpl mFileTransferingServiceImpl;
    private static FileUploadServiceImpl mFileUploadServiceImpl;
    private static GeolocSharingServiceImpl mGeolocSharingServiceImpl;
    private static HistoryLogServiceImpl mHistoryLogServiceImpl;
    private static ImageSharingServiceImpl mImageSharingServiceImpl;
    private static MultimediaSessionServiceImpl mMultimediaSessionServiceImpl;
    private static VideoSharingServiceImpl mVideoSharingServiceImpl;
    private final String LOG_TAG = TapiServiceManager.class.getSimpleName();
    private Context mContext;

    public void handleIntent(Intent intent) {
    }

    public TapiServiceManager(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
        createTapiServices();
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        super.onRegistered(imsRegistration);
        String str = this.LOG_TAG;
        Log.d(str, "onRegistered() services : " + imsRegistration.getServices());
        if (RcsSettingsUtils.getInstance(this.mContext) != null) {
            RcsSettingsUtils.getInstance().updateSettings();
            RcsSettingsUtils.getInstance().updateTapiSettings();
            RcsSettingsUtils.getInstance().loadCCAndAC();
            PhoneUtils.initialize();
        }
        notifyRegistrationStatusToTapiClient(true, 0);
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        super.onDeregistered(imsRegistration, i);
        notifyRegistrationStatusToTapiClient(false, i);
    }

    public static boolean isSupportTapi() {
        return TapiServiceUtil.isSupportTapi();
    }

    public void broadcastServiceUp() {
        Log.i(this.LOG_TAG, "broadcastServiceUp");
        if (isSupportTapi()) {
            this.mContext.sendBroadcast(new Intent("com.gsma.services.rcs.action.SERVICE_UP"));
        }
    }

    public void notifyRegistrationStatusToTapiClient(boolean z, int i) {
        String str = this.LOG_TAG;
        Log.i(str, "notifyRegistrationStatusToTapiClient : " + z);
        RcsServiceRegistration.ReasonCode reasonCode = RcsServiceRegistration.ReasonCode.UNSPECIFIED;
        if (i != 200) {
            reasonCode = RcsServiceRegistration.ReasonCode.CONNECTION_LOST;
        }
        if (isSupportTapi()) {
            ChatServiceImpl chatServiceImpl = mChatServiceImpl;
            if (chatServiceImpl != null) {
                chatServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
            FileTransferingServiceImpl fileTransferingServiceImpl = mFileTransferingServiceImpl;
            if (fileTransferingServiceImpl != null) {
                fileTransferingServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
            FileUploadServiceImpl fileUploadServiceImpl = mFileUploadServiceImpl;
            if (fileUploadServiceImpl != null) {
                fileUploadServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
            ImageSharingServiceImpl imageSharingServiceImpl = mImageSharingServiceImpl;
            if (imageSharingServiceImpl != null) {
                imageSharingServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
            GeolocSharingServiceImpl geolocSharingServiceImpl = mGeolocSharingServiceImpl;
            if (geolocSharingServiceImpl != null) {
                geolocSharingServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
            VideoSharingServiceImpl videoSharingServiceImpl = mVideoSharingServiceImpl;
            if (videoSharingServiceImpl != null) {
                videoSharingServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
            CapabilityServiceImpl capabilityServiceImpl = mCapabilityServiceImpl;
            if (capabilityServiceImpl != null) {
                capabilityServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
            MultimediaSessionServiceImpl multimediaSessionServiceImpl = mMultimediaSessionServiceImpl;
            if (multimediaSessionServiceImpl != null) {
                multimediaSessionServiceImpl.notifyRegistrationEvent(z, reasonCode);
            }
        }
    }

    public void createTapiServices() {
        Log.i(this.LOG_TAG, "createTapiServices");
        IServiceModuleManager serviceModuleManager = getServiceModuleManager();
        IImModule imModule = serviceModuleManager.getImModule();
        if (imModule != null) {
            setmFileTransferingServiceImpl(new FileTransferingServiceImpl(this.mContext, imModule));
            setmChatServiceImpl(new ChatServiceImpl(this.mContext, imModule));
            setmFileUploadServiceImpl(new FileUploadServiceImpl(this.mContext, imModule));
        }
        IImageShareModule imageShareModule = serviceModuleManager.getImageShareModule();
        if (imageShareModule != null) {
            setmImageSharingServiceImpl(new ImageSharingServiceImpl(imageShareModule));
        }
        IGlsModule glsModule = serviceModuleManager.getGlsModule();
        if (glsModule != null) {
            setmGeolocSharingServiceImpl(new GeolocSharingServiceImpl(this.mContext, glsModule));
        }
        IVideoShareModule videoShareModule = serviceModuleManager.getVideoShareModule();
        if (videoShareModule != null) {
            setmVideoSharingServiceImpl(new VideoSharingServiceImpl(videoShareModule));
        }
        setmContactServiceImpl(new ContactServiceImpl(this.mContext));
        setmCapabilityServiceImpl(new CapabilityServiceImpl(this.mContext));
        ISessionModule sessionModule = serviceModuleManager.getSessionModule();
        if (sessionModule != null) {
            setmMultimediaSessionServiceImpl(new MultimediaSessionServiceImpl(sessionModule));
        }
        setmHistoryLogServiceImpl(new HistoryLogServiceImpl());
        if (RcsSettingsUtils.getInstance(this.mContext) != null) {
            RcsSettingsUtils.getInstance().updateTapiSettings();
        }
        broadcastServiceUp();
    }

    public static void setmChatServiceImpl(ChatServiceImpl chatServiceImpl) {
        mChatServiceImpl = chatServiceImpl;
    }

    public static void setmFileTransferingServiceImpl(FileTransferingServiceImpl fileTransferingServiceImpl) {
        mFileTransferingServiceImpl = fileTransferingServiceImpl;
    }

    public static void setmFileUploadServiceImpl(FileUploadServiceImpl fileUploadServiceImpl) {
        mFileUploadServiceImpl = fileUploadServiceImpl;
    }

    public static void setmImageSharingServiceImpl(ImageSharingServiceImpl imageSharingServiceImpl) {
        mImageSharingServiceImpl = imageSharingServiceImpl;
    }

    public static void setmGeolocSharingServiceImpl(GeolocSharingServiceImpl geolocSharingServiceImpl) {
        mGeolocSharingServiceImpl = geolocSharingServiceImpl;
    }

    public static void setmVideoSharingServiceImpl(VideoSharingServiceImpl videoSharingServiceImpl) {
        mVideoSharingServiceImpl = videoSharingServiceImpl;
    }

    public static void setmContactServiceImpl(ContactServiceImpl contactServiceImpl) {
        mContactServiceImpl = contactServiceImpl;
    }

    public static void setmCapabilityServiceImpl(CapabilityServiceImpl capabilityServiceImpl) {
        mCapabilityServiceImpl = capabilityServiceImpl;
    }

    public static void setmMultimediaSessionServiceImpl(MultimediaSessionServiceImpl multimediaSessionServiceImpl) {
        mMultimediaSessionServiceImpl = multimediaSessionServiceImpl;
    }

    public static void setmHistoryLogServiceImpl(HistoryLogServiceImpl historyLogServiceImpl) {
        mHistoryLogServiceImpl = historyLogServiceImpl;
    }

    public static ChatServiceImpl getChatService() {
        return mChatServiceImpl;
    }

    public static FileTransferingServiceImpl getFtService() {
        return mFileTransferingServiceImpl;
    }

    public static FileUploadServiceImpl getfileUpService() {
        return mFileUploadServiceImpl;
    }

    public static ImageSharingServiceImpl getIshService() {
        return mImageSharingServiceImpl;
    }

    public static VideoSharingServiceImpl getVshService() {
        return mVideoSharingServiceImpl;
    }

    public static GeolocSharingServiceImpl getGlsService() {
        return mGeolocSharingServiceImpl;
    }

    public static ContactServiceImpl getContactService() {
        return mContactServiceImpl;
    }

    public static CapabilityServiceImpl getCapService() {
        return mCapabilityServiceImpl;
    }

    public static HistoryLogServiceImpl getHistoryService() {
        return mHistoryLogServiceImpl;
    }

    public static MultimediaSessionServiceImpl getMulSessionService() {
        return mMultimediaSessionServiceImpl;
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        if (i != SimUtil.getActiveDataPhoneId()) {
            Log.i(this.LOG_TAG, "ServiceSwitch not updated for active data phoneId, return.");
            return;
        }
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, i) != 1) {
            z = false;
        }
        Log.i(this.LOG_TAG + "[" + i + "]", "ImsServiceSwitch active:" + z);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("value", Boolean.toString(z));
        contentResolver.update(CommonServiceConfiguration.Settings.CONTENT_URI, contentValues2, McsConstants.BundleData.KEY + "=?", new String[]{"ServiceActivated"});
    }

    public String[] getServicesRequiring() {
        return new String[]{"im", "slm", "ft", "ft_http", "options", SipMsg.EVENT_PRESENCE, "is", "vs"};
    }
}
