package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.upload.FileUpload;
import com.gsma.services.rcs.upload.FileUploadInfo;
import com.gsma.services.rcs.upload.FileUploadService;
import com.gsma.services.rcs.upload.FileUploadServiceConfiguration;
import com.gsma.services.rcs.upload.IFileUpload;
import com.gsma.services.rcs.upload.IFileUploadListener;
import com.gsma.services.rcs.upload.IFileUploadService;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload.FileUploadMessage;
import com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload.IFileUploadTaskListener;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class FileUploadServiceImpl extends IFileUploadService.Stub implements IFileUploadTaskListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = FileUploadService.class.getSimpleName();
    private Context mContext;
    private ImConfig mImConfig = null;
    private IImModule mImModule;
    private Object mLock = new Object();
    private int mMaxUploadCnt = 0;
    private final RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();
    private final RemoteCallbackList<IFileUploadListener> mUploadListeners = new RemoteCallbackList<>();
    private Hashtable<String, IFileUpload> mUploadTasks = new Hashtable<>();

    public int getServiceVersion() throws RemoteException {
        return 2;
    }

    public FileUploadServiceImpl(Context context, IImModule iImModule) {
        this.mContext = context;
        this.mImModule = iImModule;
        this.mImConfig = iImModule.getImConfig();
    }

    public FileUploadServiceConfiguration getConfiguration() throws ServerApiException {
        return new FileUploadServiceConfiguration(Math.max(this.mImConfig.getMaxSizeExtraFileTr(), this.mImConfig.getMaxSizeFileTr()));
    }

    public IFileUpload uploadFile(Uri uri, boolean z) throws ServerApiException {
        if (uri != null) {
            int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
            ImConfig imConfig = this.mImModule.getImConfig(activeDataPhoneId);
            this.mImConfig = imConfig;
            if (imConfig.getFtHttpCsUri() == null) {
                Log.e(LOG_TAG, "Can't find proper http content server.");
                throw new ServerApiException("Can't find proper http content server.");
            } else if (this.mMaxUploadCnt == 0 || this.mUploadTasks.size() < this.mMaxUploadCnt) {
                String filePathFromUri = FileUtils.getFilePathFromUri(this.mContext, uri);
                if (filePathFromUri != null) {
                    File file = new File(filePathFromUri);
                    long max = Math.max(this.mImConfig.getMaxSizeExtraFileTr(), this.mImConfig.getMaxSizeFileTr());
                    if (max == 0 || file.length() <= max) {
                        FileUploadImpl fileUploadImpl = new FileUploadImpl(new FileUploadMessage(activeDataPhoneId, this.mContext, this.mImConfig, this.mImModule.getLooper(), uri, filePathFromUri, file.getName(), file.length(), z), this);
                        addFileUploadTask(fileUploadImpl);
                        fileUploadImpl.startUpload();
                        return fileUploadImpl;
                    }
                    Log.e(LOG_TAG, "Max file size exceeds!");
                    throw new ServerApiException("Max file size exceeds");
                }
                String str = LOG_TAG;
                Log.e(str, "Can't retrieve file path from uri: " + uri);
                throw new ServerApiException("Can't retrieve file path from uri: " + uri);
            } else {
                Log.e(LOG_TAG, "Max file transfer tasks achieved!");
                throw new ServerApiException("Max file transfer tasks achieved");
            }
        } else {
            Log.e(LOG_TAG, "Invalid file uri!");
            throw new ServerApiException("Invalid file uri");
        }
    }

    public boolean canUploadFile() throws ServerApiException {
        return this.mMaxUploadCnt == 0 || this.mUploadTasks.size() < this.mMaxUploadCnt;
    }

    public List<IBinder> getFileUploads() throws ServerApiException {
        ArrayList arrayList = new ArrayList(this.mUploadTasks.size());
        Enumeration<IFileUpload> elements = this.mUploadTasks.elements();
        while (elements.hasMoreElements()) {
            arrayList.add(elements.nextElement().asBinder());
        }
        return arrayList;
    }

    public IFileUpload getFileUpload(String str) throws ServerApiException {
        return this.mUploadTasks.get(str);
    }

    private void addFileUploadTask(FileUploadImpl fileUploadImpl) {
        this.mUploadTasks.put(fileUploadImpl.getUploadId(), fileUploadImpl);
    }

    private void removeFileUploadTask(String str) {
        this.mUploadTasks.remove(str);
    }

    public void addFileUploadEventListener(IFileUploadListener iFileUploadListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mUploadListeners.register(iFileUploadListener);
        }
    }

    public void removeFileUploadEventListener(IFileUploadListener iFileUploadListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mUploadListeners.unregister(iFileUploadListener);
        }
    }

    private void broadcastFileUploadStateChanged(String str, FileUpload.State state) {
        int beginBroadcast = this.mUploadListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mUploadListeners.getBroadcastItem(i).onStateChanged(str, state);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mUploadListeners.finishBroadcast();
    }

    private void broadcastFileUploadProgress(String str, long j, long j2) {
        int beginBroadcast = this.mUploadListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mUploadListeners.getBroadcastItem(i).onProgressUpdate(str, j, j2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mUploadListeners.finishBroadcast();
    }

    private void broadcastFileUploadComplete(String str, FileUploadInfo fileUploadInfo) {
        int beginBroadcast = this.mUploadListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mUploadListeners.getBroadcastItem(i).onUploaded(str, fileUploadInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mUploadListeners.finishBroadcast();
    }

    public void onUploadStateChanged(String str, FileUpload.State state, boolean z) {
        synchronized (this.mLock) {
            broadcastFileUploadStateChanged(str, state);
            if (z) {
                removeFileUploadTask(str);
            }
        }
    }

    public void onUploadProgress(String str, long j, long j2) {
        synchronized (this.mLock) {
            broadcastFileUploadProgress(str, j, j2);
        }
    }

    public void onUploadComplete(String str, FileUploadInfo fileUploadInfo) {
        synchronized (this.mLock) {
            broadcastFileUploadComplete(str, fileUploadInfo);
        }
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null || registrationManager.getRegistrationInfo().length <= 0 || this.mImModule.getImConfig().getFtDefaultMech() != ImConstants.FtMech.HTTP) {
            return false;
        }
        return true;
    }

    public void addEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.register(iRcsServiceRegistrationListener);
        }
    }

    public void removeEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.unregister(iRcsServiceRegistrationListener);
        }
    }

    public void notifyRegistrationEvent(boolean z, RcsServiceRegistration.ReasonCode reasonCode) {
        synchronized (this.mLock) {
            int beginBroadcast = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                if (z) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        String str = LOG_TAG;
                        Log.e(str, "Can't notify listener: " + e);
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(reasonCode);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }
}
