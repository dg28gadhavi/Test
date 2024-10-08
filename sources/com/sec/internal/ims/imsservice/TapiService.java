package com.sec.internal.ims.imsservice;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.gsma.services.rcs.capability.ICapabilityService;
import com.gsma.services.rcs.chat.IChatService;
import com.gsma.services.rcs.contact.IContactService;
import com.gsma.services.rcs.extension.IMultimediaSessionService;
import com.gsma.services.rcs.filetransfer.IFileTransferService;
import com.gsma.services.rcs.history.IHistoryService;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharingService;
import com.gsma.services.rcs.sharing.image.IImageSharingService;
import com.gsma.services.rcs.sharing.video.IVideoSharingService;
import com.gsma.services.rcs.upload.IFileUploadService;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager;
import com.sec.internal.ims.servicemodules.tapi.service.extension.ServiceExtensionManager;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.ValidationHelper;

public class TapiService extends ImsServiceBase {
    private static final String LOG_TAG = TapiService.class.getSimpleName();

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    /* JADX WARNING: type inference failed for: r4v33, types: [com.sec.internal.ims.servicemodules.tapi.service.api.MultimediaSessionServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v34, types: [com.sec.internal.ims.servicemodules.tapi.service.api.HistoryLogServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v35, types: [com.sec.internal.ims.servicemodules.tapi.service.api.ContactServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v36, types: [com.sec.internal.ims.servicemodules.tapi.service.api.GeolocSharingServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v37, types: [com.sec.internal.ims.servicemodules.tapi.service.api.FileUploadServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v38, types: [com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v39, types: [com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v40, types: [com.sec.internal.ims.servicemodules.tapi.service.api.VideoSharingServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v41, types: [com.sec.internal.ims.servicemodules.tapi.service.api.CapabilityServiceImpl, android.os.IBinder] */
    /* JADX WARNING: type inference failed for: r4v42, types: [com.sec.internal.ims.servicemodules.tapi.service.api.ImageSharingServiceImpl, android.os.IBinder] */
    public IBinder onBind(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onBind: intent " + intent);
        if (ValidationHelper.isTapiAuthorisationSupports()) {
            String string = intent.getExtras() != null ? intent.getExtras().getString("packages") : "";
            if (string == null) {
                Log.d(str, "packagename is null ");
                return null;
            } else if (!isTapiAuthorised(getApplicationContext(), string)) {
                Log.e(str, "Client package is not authorized" + string);
                return null;
            }
        }
        if (Extensions.UserHandle.myUserId() != 0) {
            Log.d(str, "Do not allow bind on non-system user");
            return null;
        } else if (!isTAPISupported()) {
            return null;
        } else {
            if (IImageSharingService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getIshService();
            }
            if (ICapabilityService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getCapService();
            }
            if (IVideoSharingService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getVshService();
            }
            if (IFileTransferService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getFtService();
            }
            if (IChatService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getChatService();
            }
            if (IFileUploadService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getfileUpService();
            }
            if (IGeolocSharingService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getGlsService();
            }
            if (IContactService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getContactService();
            }
            if (IHistoryService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getHistoryService();
            }
            if (IMultimediaSessionService.class.getName().equals(intent.getAction())) {
                return TapiServiceManager.getMulSessionService();
            }
            return null;
        }
    }

    public static boolean isTAPISupported() {
        return TapiServiceManager.isSupportTapi();
    }

    private static boolean isTapiAuthorised(Context context, String str) {
        return ServiceExtensionManager.isAppAuthorised(context, str);
    }
}
