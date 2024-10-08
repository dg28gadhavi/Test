package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.FileTransferLog;
import com.gsma.services.rcs.filetransfer.IFileTransfer;
import com.gsma.services.rcs.filetransfer.IFileTransferService;
import com.gsma.services.rcs.filetransfer.IFileTransferServiceConfiguration;
import com.gsma.services.rcs.filetransfer.IGroupFileTransferListener;
import com.gsma.services.rcs.filetransfer.IOneToOneFileTransferListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GroupFileTransferBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneFileTransferBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class FileTransferingServiceImpl extends IFileTransferService.Stub implements IFtEventListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = FileTransferingServiceImpl.class.getSimpleName();
    private static Hashtable<String, IFileTransfer> mIFtSessions = new Hashtable<>();
    Context mContext;
    private GroupFileTransferBroadcaster mGroupFileTransferBroadcaster = null;
    private IImModule mImModule;
    private Object mLock = new Object();
    private OneToOneFileTransferBroadcaster mOneToOneFileTransferBroadcaster = null;
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public void clearFileTransferDeliveryExpiration(List<String> list) throws RemoteException {
    }

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public void onCancelRequestFailed(FtMessage ftMessage) {
    }

    public void onFileResizingNeeded(FtMessage ftMessage, long j) {
    }

    public void onFileTransferAttached(FtMessage ftMessage) {
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
    }

    public void onNotifyCloudMsgFtEvent(FtMessage ftMessage) {
    }

    public void onTransferStarted(FtMessage ftMessage) {
    }

    public FileTransferingServiceImpl(Context context, IImModule iImModule) {
        this.mContext = context;
        this.mImModule = iImModule;
        this.mOneToOneFileTransferBroadcaster = new OneToOneFileTransferBroadcaster(this.mContext);
        this.mGroupFileTransferBroadcaster = new GroupFileTransferBroadcaster(this.mContext);
        this.mImModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA, this);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            return false;
        }
        for (ImsRegistration imsRegistration : registrationManager.getRegistrationInfo()) {
            if (imsRegistration.hasService("ft") || imsRegistration.hasService("ft_http")) {
                return true;
            }
        }
        return false;
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

    public IFileTransferServiceConfiguration getConfiguration() throws ServerApiException {
        return new FileTransferServiceConfigurationImpl(this.mImModule.getImConfig());
    }

    public List<IBinder> getFileTransfers() throws ServerApiException {
        Log.d(LOG_TAG, "getFileTransfers get all transfered file.");
        ArrayList arrayList = new ArrayList(mIFtSessions.size());
        Enumeration<IFileTransfer> elements = mIFtSessions.elements();
        while (elements.hasMoreElements()) {
            arrayList.add(elements.nextElement().asBinder());
        }
        return arrayList;
    }

    public IFileTransfer getFileTransfer(String str) throws ServerApiException {
        return mIFtSessions.get(str);
    }

    public OneToOneFileTransferImpl getFileTransferByID(String str) {
        return mIFtSessions.get(str);
    }

    public IFileTransfer transferFile(ContactId contactId, Uri uri, FileTransfer.Disposition disposition, boolean z) throws ServerApiException {
        String str = "tel:" + PhoneUtils.extractNumberFromUri(contactId.toString());
        String fileNameFromPath = FileUtils.getFileNameFromPath(FileUtils.getFilePathFromUri(this.mContext, uri));
        String str2 = LOG_TAG;
        Log.d(str2, "transferFile, contentUri = " + uri.toString());
        try {
            FtMessage ftMessage = this.mImModule.attachFileToSingleChat(0, fileNameFromPath, uri, ImsUri.parse(str), EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), (String) null, MIMEContentType.FT_HTTP, false, false, false, false, (String) null, disposition == FileTransfer.Disposition.RENDER ? FileDisposition.RENDER : FileDisposition.ATTACH).get();
            if (ftMessage == null) {
                Log.e(str2, "attachFileToSingleChat failed, return null!");
                return null;
            }
            OneToOneFileTransferImpl oneToOneFileTransferImpl = new OneToOneFileTransferImpl(ftMessage, this.mImModule);
            addFileTransferingSession(String.valueOf(ftMessage.getId()), oneToOneFileTransferImpl);
            return oneToOneFileTransferImpl;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void addOneToOneFileTransferListener(IOneToOneFileTransferListener iOneToOneFileTransferListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mOneToOneFileTransferBroadcaster.addOneToOneFileTransferListener(iOneToOneFileTransferListener);
        }
    }

    public void removeOneToOneFileTransferListener(IOneToOneFileTransferListener iOneToOneFileTransferListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mOneToOneFileTransferBroadcaster.removeOneToOneFileTransferListener(iOneToOneFileTransferListener);
        }
    }

    public void addGroupFileTransferListener(IGroupFileTransferListener iGroupFileTransferListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupFileTransferBroadcaster.addGroupFileTransferListener(iGroupFileTransferListener);
        }
    }

    public void removeGroupFileTransferListener(IGroupFileTransferListener iGroupFileTransferListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupFileTransferBroadcaster.removeGroupFileTransferListener(iGroupFileTransferListener);
        }
    }

    public static void addFileTransferingSession(String str, OneToOneFileTransferImpl oneToOneFileTransferImpl) {
        if (!mIFtSessions.containsKey(str)) {
            mIFtSessions.put(str, oneToOneFileTransferImpl);
        }
    }

    public static void removeFileTransferingSession(String str) {
        if (mIFtSessions.containsKey(str)) {
            mIFtSessions.remove(str);
        }
    }

    public void removeFileTransferingSessions(List<String> list) {
        for (String remove : list) {
            mIFtSessions.remove(remove);
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
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(reasonCode);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public IFileTransfer transferFileToGroupChat(String str, Uri uri, FileTransfer.Disposition disposition, boolean z) throws ServerApiException {
        if (!canTransferFileToGroupChat(str)) {
            return null;
        }
        String fileNameFromPath = FileUtils.getFileNameFromPath(FileUtils.getFilePathFromUri(this.mContext, uri));
        String str2 = LOG_TAG;
        Log.d(str2, "transferFileToGroupChat, file = " + uri.toString());
        try {
            FtMessage ftMessage = this.mImModule.attachFileToGroupChat(str, fileNameFromPath, uri, EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), (String) null, (String) null, false, false, false, false, (String) null, disposition == FileTransfer.Disposition.RENDER ? FileDisposition.RENDER : FileDisposition.ATTACH).get();
            if (ftMessage == null) {
                Log.e(str2, "attachFileToGroupChat failed, return null!");
                return null;
            }
            OneToOneFileTransferImpl oneToOneFileTransferImpl = new OneToOneFileTransferImpl(ftMessage, this.mImModule);
            addFileTransferingSession(String.valueOf(ftMessage.getId()), oneToOneFileTransferImpl);
            return oneToOneFileTransferImpl;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean isAllowedTotransferFile(ContactId contactId) throws ServerApiException {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            Capabilities ownCapabilities = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
            if (ownCapabilities == null || !ownCapabilities.hasFeature(Capabilities.FEATURE_FT)) {
                Binder.restoreCallingIdentity(clearCallingIdentity);
                return false;
            }
            Binder.restoreCallingIdentity(clearCallingIdentity);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(clearCallingIdentity);
            throw th;
        }
    }

    public boolean canTransferFileToGroupChat(String str) throws ServerApiException {
        if (ImCache.getInstance().getImSession(str) != null) {
            return true;
        }
        String str2 = LOG_TAG;
        Log.d(str2, "attachFileToGroupChat: chat not exist - " + str);
        return false;
    }

    public void markFileTransferAsRead(String str) throws ServerApiException {
        FtMessage ftMessage = ImCache.getInstance().getFtMessage(Integer.valueOf(str).intValue());
        if (ftMessage != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(str);
            this.mImModule.readMessages(ftMessage.getChatId(), arrayList);
            ftMessage.updateNotificationStatus(NotificationStatus.DELIVERED);
            this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(ftMessage.getChatId()))), str, FileTransfer.State.DISPLAYED, FileTransfer.ReasonCode.UNSPECIFIED);
        }
    }

    public void notifyChangeForDelete() {
        this.mContext.getContentResolver().notifyChange(FileTransferLog.CONTENT_URI, (ContentObserver) null);
    }

    public void deleteOneToOneFileTransfers() throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteOneToOneFileTransfers()");
        Map<String, Set<String>> fileTransfers = getFileTransfers(false, "is_filetransfer = 1");
        if (fileTransfers != null) {
            ArrayList arrayList = new ArrayList();
            for (Map.Entry next : fileTransfers.entrySet()) {
                arrayList.addAll((Collection) next.getValue());
                String imSessionByChatId = getImSessionByChatId((String) next.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneFileTransferBroadcaster.broadcastDeleted(PhoneUtils.extractNumberFromUri(imSessionByChatId), (Set) next.getValue());
                }
            }
            this.mImModule.deleteMessages(arrayList, false);
            removeFileTransferingSessions(arrayList);
            notifyChangeForDelete();
        }
    }

    public void deleteGroupFileTransfers() throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteGroupFileTransfers()");
        Map<String, Set<String>> fileTransfers = getFileTransfers(true, "is_filetransfer = 1");
        if (fileTransfers != null) {
            ArrayList arrayList = new ArrayList();
            for (Map.Entry next : fileTransfers.entrySet()) {
                arrayList.addAll((Collection) next.getValue());
                synchronized (this.mLock) {
                    this.mGroupFileTransferBroadcaster.broadcastDeleted((String) next.getKey(), (Set) next.getValue());
                }
            }
            this.mImModule.deleteMessages(arrayList, false);
            removeFileTransferingSessions(arrayList);
            notifyChangeForDelete();
        }
    }

    public void deleteOneToOneFileTransfersByContactId(ContactId contactId) throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "start : deleteOneToOneFileTransfersByContactId()");
        HashSet hashSet = new HashSet();
        hashSet.add(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contactId.toString())));
        ImSession imSessionByParticipants = ImCache.getInstance().getImSessionByParticipants(hashSet, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        if (imSessionByParticipants == null) {
            Log.e(str, "deleteOneToOneFileTransfersByContactId, no session for ft");
            return;
        }
        Map<String, Set<String>> fileTransfers = getFileTransfers(false, "is_filetransfer = 1 and chat_id = '" + imSessionByParticipants.getChatId() + "'");
        if (fileTransfers != null) {
            ArrayList arrayList = new ArrayList();
            for (Map.Entry next : fileTransfers.entrySet()) {
                arrayList.addAll((Collection) next.getValue());
                String imSessionByChatId = getImSessionByChatId((String) next.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneFileTransferBroadcaster.broadcastDeleted(PhoneUtils.extractNumberFromUri(imSessionByChatId), (Set) next.getValue());
                }
            }
            this.mImModule.deleteMessages(arrayList, false);
            removeFileTransferingSessions(arrayList);
            notifyChangeForDelete();
        }
    }

    public void deleteGroupFileTransfersByChatId(String str) throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteGroupFileTransfersByChatId()");
        Map<String, Set<String>> fileTransfers = getFileTransfers(true, "is_filetransfer = 1 and chat_id = '" + str + "'");
        if (fileTransfers != null) {
            ArrayList arrayList = new ArrayList();
            for (Map.Entry next : fileTransfers.entrySet()) {
                arrayList.addAll((Collection) next.getValue());
                synchronized (this.mLock) {
                    this.mGroupFileTransferBroadcaster.broadcastDeleted((String) next.getKey(), (Set) next.getValue());
                }
            }
            this.mImModule.deleteMessages(arrayList, false);
            removeFileTransferingSessions(arrayList);
            notifyChangeForDelete();
        }
    }

    public void deleteFileTransfer(String str) throws ServerApiException {
        Log.d(LOG_TAG, "start : deleteFileTransfer()");
        ArrayList arrayList = new ArrayList();
        arrayList.add(str);
        this.mImModule.deleteMessages(arrayList, false);
        OneToOneFileTransferImpl fileTransferByID = getFileTransferByID(str);
        if (fileTransferByID != null) {
            HashSet hashSet = new HashSet();
            hashSet.add(str);
            try {
                boolean isGroupTransfer = fileTransferByID.isGroupTransfer();
                String chatId = fileTransferByID.getChatId();
                ContactId remoteContact = fileTransferByID.getRemoteContact();
                if (isGroupTransfer) {
                    this.mGroupFileTransferBroadcaster.broadcastDeleted(chatId, hashSet);
                } else if (remoteContact != null) {
                    this.mOneToOneFileTransferBroadcaster.broadcastDeleted(remoteContact.toString(), hashSet);
                } else {
                    return;
                }
                removeFileTransferingSessions(arrayList);
                notifyChangeForDelete();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setAutoAccept(boolean z) throws ServerApiException {
        if (isFtAutoAcceptedModeChangeable()) {
            this.mImModule.setAutoAcceptFt(1);
            return;
        }
        throw new ServerApiException("Auto accept mode is not changeable");
    }

    public void setAutoAcceptInRoaming(boolean z) throws ServerApiException {
        if (!isFtAutoAcceptedModeChangeable()) {
            throw new ServerApiException("Auto accept mode is not changeable");
        } else if (isFileTransferAutoAccepted()) {
            this.mImModule.setAutoAcceptFt(2);
        } else {
            throw new ServerApiException("Auto accept mode in normal conditions must be enabled");
        }
    }

    public void setImageResizeOption(int i) throws ServerApiException {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ImSettings.KEY_IMAGE_RESIZE_OPTION, String.valueOf(i));
        this.mContext.getContentResolver().insert(ConfigConstants.CONTENT_URI, contentValues);
    }

    public List<String> getUndeliveredFileTransfers(ContactId contactId) throws ServerApiException {
        Log.d(LOG_TAG, "start : getUndeliveredFileTransfers()");
        ImsUri parse = ImsUri.parse("tel:" + contactId.toString());
        HashSet hashSet = new HashSet();
        hashSet.add(parse);
        ImSession imSessionByParticipants = ImCache.getInstance().getImSessionByParticipants(hashSet, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        ArrayList arrayList = new ArrayList();
        if (imSessionByParticipants == null) {
            return arrayList;
        }
        Cursor queryMessages = ImCache.getInstance().queryMessages(new String[]{"_id"}, "chat_id = '" + imSessionByParticipants.getChatId() + "' and " + "notification_status" + " = " + NotificationStatus.NONE.getId() + " and " + "direction" + " = " + ImDirection.OUTGOING.getId() + " and " + ImContract.ChatItem.IS_FILE_TRANSFER + " = 1", (String[]) null, (String) null);
        if (queryMessages != null) {
            while (queryMessages.moveToNext()) {
                try {
                    arrayList.add(String.valueOf(queryMessages.getInt(queryMessages.getColumnIndexOrThrow("_id"))));
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
        }
        if (queryMessages != null) {
            queryMessages.close();
        }
        return arrayList;
        throw th;
    }

    public void markUndeliveredFileTransfersAsProcessed(List<String> list) throws ServerApiException {
        Log.d(LOG_TAG, "start : markUndeliveredFileTransfersAsProcessed()");
        ImCache instance = ImCache.getInstance();
        for (String next : list) {
            ImMessage imMessage = instance.getImMessage(Integer.valueOf(next).intValue());
            if (imMessage != null) {
                imMessage.updateStatus(ImConstants.Status.SENT);
                instance.removeFromPendingList(Integer.valueOf(next).intValue());
            }
        }
    }

    public void handleTransferState(FtMessage ftMessage) {
        FileTransfer.ReasonCode reasonCode;
        CancelReason cancelReason = ftMessage.getCancelReason();
        FtRejectReason rejectReason = ftMessage.getRejectReason();
        FileTransfer.ReasonCode reasonCode2 = FileTransfer.ReasonCode.UNSPECIFIED;
        int stateId = ftMessage.getStateId();
        ImDirection direction = ftMessage.getDirection();
        if (rejectReason != null) {
            reasonCode = ftRejectReasonTranslator(rejectReason);
        } else {
            reasonCode = ftCancelReasonTranslator(cancelReason);
        }
        FileTransfer.State state = FileTransfer.State.FAILED;
        switch (stateId) {
            case 0:
            case 6:
                if (ImDirection.INCOMING != direction) {
                    state = FileTransfer.State.INITIATING;
                    break;
                } else {
                    state = FileTransfer.State.INVITED;
                    break;
                }
            case 1:
                if (ImDirection.INCOMING == direction) {
                    state = FileTransfer.State.ACCEPTING;
                    break;
                }
                break;
            case 2:
            case 9:
                state = FileTransfer.State.STARTED;
                break;
            case 3:
                state = FileTransfer.State.TRANSFERRED;
                break;
            case 4:
            case 7:
                state = FileTransfer.State.ABORTED;
                break;
            case 5:
            case 8:
                state = FileTransfer.State.QUEUED;
                break;
        }
        ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(ftMessage.getChatId())));
        ImSession imSession = ImCache.getInstance().getImSession(ftMessage.getChatId());
        if (imSession == null) {
            String str = LOG_TAG;
            Log.d(str, "handleTransferState: " + state + ", cannot get ImSession from chatId : " + ftMessage.getChatId());
            return;
        }
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi())));
        if (subscriptionUserHandle == null) {
            subscriptionUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        if (imSession.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(ftMessage.getChatId(), String.valueOf(ftMessage.getId()), state, reasonCode);
        } else {
            this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(contactId, String.valueOf(ftMessage.getId()), state, reasonCode);
        }
        this.mOneToOneFileTransferBroadcaster.broadcastUndeliveredFileTransfer(contactId, subscriptionUserHandle);
        removeFileTransferingSession(String.valueOf(ftMessage.getId()));
    }

    public void handleTransferingProgress(FtMessage ftMessage) {
        long transferredBytes = ftMessage.getTransferredBytes();
        long fileSize = ftMessage.getFileSize();
        ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(ftMessage.getChatId())));
        ImSession imSession = ImCache.getInstance().getImSession(ftMessage.getChatId());
        if (imSession == null) {
            String str = LOG_TAG;
            Log.d(str, "handleTransferingProgress, cannot get ImSession from chatId : " + ftMessage.getChatId());
        } else if (imSession.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferprogress(ftMessage.getChatId(), String.valueOf(ftMessage.getId()), transferredBytes, fileSize);
        } else {
            this.mOneToOneFileTransferBroadcaster.broadcastTransferprogress(contactId, String.valueOf(ftMessage.getId()), transferredBytes, fileSize);
        }
    }

    public void handleContentTransfered(FtMessage ftMessage) {
        ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(ftMessage.getChatId())));
        ImSession imSession = ImCache.getInstance().getImSession(ftMessage.getChatId());
        if (imSession == null) {
            String str = LOG_TAG;
            Log.d(str, "handleContentTransfered, cannot get ImSession from chatId : " + ftMessage.getChatId());
        } else if (imSession.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(ftMessage.getChatId(), String.valueOf(ftMessage.getId()), FileTransfer.State.TRANSFERRED, FileTransfer.ReasonCode.UNSPECIFIED);
        } else {
            this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(contactId, String.valueOf(ftMessage.getId()), FileTransfer.State.TRANSFERRED, FileTransfer.ReasonCode.UNSPECIFIED);
        }
    }

    public void handleTransferReceived(FtMessage ftMessage) {
        ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(ftMessage.getChatId())));
        ImSession imSession = ImCache.getInstance().getImSession(ftMessage.getChatId());
        if (imSession == null) {
            String str = LOG_TAG;
            Log.d(str, "handleTransferReceived, cannot get ImSession from chatId : " + ftMessage.getChatId());
            return;
        }
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi())));
        if (subscriptionUserHandle == null) {
            subscriptionUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        if (imSession.isGroupChat()) {
            this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(ftMessage.getChatId(), String.valueOf(ftMessage.getId()), FileTransfer.State.INVITED, FileTransfer.ReasonCode.UNSPECIFIED);
            this.mGroupFileTransferBroadcaster.broadcastFileTransferInvitation(String.valueOf(ftMessage.getId()), subscriptionUserHandle);
            return;
        }
        this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(contactId, String.valueOf(ftMessage.getId()), FileTransfer.State.INVITED, FileTransfer.ReasonCode.UNSPECIFIED);
        this.mOneToOneFileTransferBroadcaster.broadcastFileTransferInvitation(String.valueOf(ftMessage.getId()), subscriptionUserHandle);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x009a, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessageDeliveryStatus(com.sec.internal.ims.servicemodules.im.MessageBase r8, com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r9) {
        /*
            r7 = this;
            java.lang.String r0 = r8.getChatId()
            java.lang.String r0 = r7.getImSessionByChatId(r0)
            com.gsma.services.rcs.contact.ContactId r4 = new com.gsma.services.rcs.contact.ContactId
            java.lang.String r0 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r0)
            r4.<init>(r0)
            com.gsma.services.rcs.filetransfer.FileTransfer$State r0 = com.gsma.services.rcs.filetransfer.FileTransfer.State.FAILED
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED
            if (r1 != r9) goto L_0x001a
            com.gsma.services.rcs.filetransfer.FileTransfer$State r0 = com.gsma.services.rcs.filetransfer.FileTransfer.State.DELIVERED
            goto L_0x0020
        L_0x001a:
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED
            if (r1 != r9) goto L_0x0020
            com.gsma.services.rcs.filetransfer.FileTransfer$State r0 = com.gsma.services.rcs.filetransfer.FileTransfer.State.DISPLAYED
        L_0x0020:
            java.lang.Object r9 = r7.mLock
            monitor-enter(r9)
            com.sec.internal.ims.servicemodules.im.ImCache r1 = com.sec.internal.ims.servicemodules.im.ImCache.getInstance()     // Catch:{ all -> 0x009b }
            java.lang.String r2 = r8.getChatId()     // Catch:{ all -> 0x009b }
            com.sec.internal.ims.servicemodules.im.ImSession r1 = r1.getImSession(r2)     // Catch:{ all -> 0x009b }
            if (r1 != 0) goto L_0x0055
            java.lang.String r7 = LOG_TAG     // Catch:{ all -> 0x009b }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x009b }
            r1.<init>()     // Catch:{ all -> 0x009b }
            java.lang.String r2 = "handleMessageDeliveryStatus: "
            r1.append(r2)     // Catch:{ all -> 0x009b }
            r1.append(r0)     // Catch:{ all -> 0x009b }
            java.lang.String r0 = ", cannot get ImSession from chatId : "
            r1.append(r0)     // Catch:{ all -> 0x009b }
            java.lang.String r8 = r8.getChatId()     // Catch:{ all -> 0x009b }
            r1.append(r8)     // Catch:{ all -> 0x009b }
            java.lang.String r8 = r1.toString()     // Catch:{ all -> 0x009b }
            android.util.Log.d(r7, r8)     // Catch:{ all -> 0x009b }
            monitor-exit(r9)     // Catch:{ all -> 0x009b }
            return
        L_0x0055:
            boolean r1 = r1.isGroupChat()     // Catch:{ all -> 0x009b }
            if (r1 == 0) goto L_0x008a
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GroupFileTransferBroadcaster r1 = r7.mGroupFileTransferBroadcaster     // Catch:{ all -> 0x009b }
            java.lang.String r2 = r8.getChatId()     // Catch:{ all -> 0x009b }
            int r3 = r8.getId()     // Catch:{ all -> 0x009b }
            java.lang.String r3 = java.lang.String.valueOf(r3)     // Catch:{ all -> 0x009b }
            com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo$Status r5 = com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo.Status.DELIVERED     // Catch:{ all -> 0x009b }
            com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo$ReasonCode r6 = com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x009b }
            r1.broadcastGroupDeliveryInfoStateChanged(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x009b }
            int r1 = r8.getNotDisplayedCounter()     // Catch:{ all -> 0x009b }
            if (r1 != 0) goto L_0x0099
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GroupFileTransferBroadcaster r7 = r7.mGroupFileTransferBroadcaster     // Catch:{ all -> 0x009b }
            java.lang.String r1 = r8.getChatId()     // Catch:{ all -> 0x009b }
            int r8 = r8.getId()     // Catch:{ all -> 0x009b }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x009b }
            com.gsma.services.rcs.filetransfer.FileTransfer$ReasonCode r2 = com.gsma.services.rcs.filetransfer.FileTransfer.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x009b }
            r7.broadcastTransferStateChanged(r1, r8, r0, r2)     // Catch:{ all -> 0x009b }
            goto L_0x0099
        L_0x008a:
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneFileTransferBroadcaster r7 = r7.mOneToOneFileTransferBroadcaster     // Catch:{ all -> 0x009b }
            int r8 = r8.getId()     // Catch:{ all -> 0x009b }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x009b }
            com.gsma.services.rcs.filetransfer.FileTransfer$ReasonCode r1 = com.gsma.services.rcs.filetransfer.FileTransfer.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x009b }
            r7.broadcastTransferStateChanged(r4, r8, r0, r1)     // Catch:{ all -> 0x009b }
        L_0x0099:
            monitor-exit(r9)     // Catch:{ all -> 0x009b }
            return
        L_0x009b:
            r7 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x009b }
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl.handleMessageDeliveryStatus(com.sec.internal.ims.servicemodules.im.MessageBase, com.sec.internal.constants.ims.servicemodules.im.NotificationStatus):void");
    }

    public void onFileTransferCreated(FtMessage ftMessage) {
        if (mIFtSessions.containsKey(String.valueOf(ftMessage.getId()))) {
            this.mImModule.sendFile(ftMessage.getImdnId());
            ImSession imSession = ImCache.getInstance().getImSession(ftMessage.getChatId());
            if (imSession == null) {
                String str = LOG_TAG;
                Log.d(str, "onFileTransferCreated, cannot get ImSession from chatId : " + ftMessage.getChatId());
            } else if (imSession.isGroupChat()) {
                this.mGroupFileTransferBroadcaster.broadcastTransferStateChanged(ftMessage.getChatId(), String.valueOf(ftMessage.getId()), FileTransfer.State.STARTED, FileTransfer.ReasonCode.UNSPECIFIED);
            } else {
                this.mOneToOneFileTransferBroadcaster.broadcastTransferStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(ftMessage.getChatId()))), String.valueOf(ftMessage.getId()), FileTransfer.State.STARTED, FileTransfer.ReasonCode.UNSPECIFIED);
            }
        }
    }

    public void onFileTransferReceived(FtMessage ftMessage) {
        addFileTransferingSession(String.valueOf(ftMessage.getId()), new OneToOneFileTransferImpl(ftMessage, this.mImModule));
        handleTransferReceived(ftMessage);
    }

    public void onTransferProgressReceived(FtMessage ftMessage) {
        handleTransferingProgress(ftMessage);
    }

    public void onTransferCompleted(FtMessage ftMessage) {
        handleContentTransfered(ftMessage);
    }

    public void onTransferCanceled(FtMessage ftMessage) {
        handleTransferState(ftMessage);
    }

    public void onImdnNotificationReceived(FtMessage ftMessage, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        handleMessageDeliveryStatus(ftMessage, ftMessage.getNotificationStatus());
    }

    public static FileTransfer.ReasonCode ftCancelReasonTranslator(CancelReason cancelReason) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[cancelReason.ordinal()]) {
            case 1:
                return FileTransfer.ReasonCode.ABORTED_BY_USER;
            case 2:
                return FileTransfer.ReasonCode.ABORTED_BY_REMOTE;
            case 3:
                return FileTransfer.ReasonCode.ABORTED_BY_SYSTEM;
            case 4:
                return FileTransfer.ReasonCode.REJECTED_BY_REMOTE;
            case 5:
                return FileTransfer.ReasonCode.REJECTED_BY_TIMEOUT;
            case 6:
                return FileTransfer.ReasonCode.REJECTED_LOW_SPACE;
            case 7:
                return FileTransfer.ReasonCode.REJECTED_MAX_SIZE;
            default:
                return FileTransfer.ReasonCode.UNSPECIFIED;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason;

        /* JADX WARNING: Can't wrap try/catch for region: R(46:0|(2:1|2)|3|(2:5|6)|7|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|(3:49|50|52)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(47:0|(2:1|2)|3|5|6|7|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|(3:49|50|52)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(49:0|(2:1|2)|3|5|6|7|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|52) */
        /* JADX WARNING: Can't wrap try/catch for region: R(50:0|1|2|3|5|6|7|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|52) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x002e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0038 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0043 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x004e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0059 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0064 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x006f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x007b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0087 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x0093 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x009f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x00ab */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00b7 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00c3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00cf */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00db */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00e7 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00f3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00ff */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x010b */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason[] r0 = com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason.FORBIDDEN_MAX_SIZE_EXCEEDED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason r3 = com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason.DECLINE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason[] r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason = r2
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r3 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_USER     // Catch:{ NoSuchFieldError -> 0x002e }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x002e }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x002e }
            L_0x002e:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0038 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_REMOTE     // Catch:{ NoSuchFieldError -> 0x0038 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0038 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0038 }
            L_0x0038:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0043 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_SYSTEM     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x004e }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REJECTED_BY_REMOTE     // Catch:{ NoSuchFieldError -> 0x004e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004e }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004e }
            L_0x004e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0059 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.TIME_OUT     // Catch:{ NoSuchFieldError -> 0x0059 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0059 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0059 }
            L_0x0059:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0064 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.LOW_MEMORY     // Catch:{ NoSuchFieldError -> 0x0064 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0064 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0064 }
            L_0x0064:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x006f }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.TOO_LARGE     // Catch:{ NoSuchFieldError -> 0x006f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006f }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006f }
            L_0x006f:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x007b }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.NOT_AUTHORIZED     // Catch:{ NoSuchFieldError -> 0x007b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x007b }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x007b }
            L_0x007b:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0087 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_BLOCKED     // Catch:{ NoSuchFieldError -> 0x0087 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0087 }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0087 }
            L_0x0087:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0093 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ NoSuchFieldError -> 0x0093 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0093 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0093 }
            L_0x0093:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x009f }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x009f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009f }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009f }
            L_0x009f:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00ab }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.VALIDITY_EXPIRED     // Catch:{ NoSuchFieldError -> 0x00ab }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ab }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00ab }
            L_0x00ab:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00b7 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.INVALID_REQUEST     // Catch:{ NoSuchFieldError -> 0x00b7 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b7 }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b7 }
            L_0x00b7:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00c3 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_USER_INVALID     // Catch:{ NoSuchFieldError -> 0x00c3 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c3 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c3 }
            L_0x00c3:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00cf }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.NO_RESPONSE     // Catch:{ NoSuchFieldError -> 0x00cf }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cf }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cf }
            L_0x00cf:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00db }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.FORBIDDEN_NO_RETRY_FALLBACK     // Catch:{ NoSuchFieldError -> 0x00db }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00db }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00db }
            L_0x00db:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00e7 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CONTENT_REACHED_DOWNSIZE     // Catch:{ NoSuchFieldError -> 0x00e7 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e7 }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e7 }
            L_0x00e7:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00f3 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.LOCALLY_ABORTED     // Catch:{ NoSuchFieldError -> 0x00f3 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f3 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f3 }
            L_0x00f3:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00ff }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CONNECTION_RELEASED     // Catch:{ NoSuchFieldError -> 0x00ff }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ff }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00ff }
            L_0x00ff:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x010b }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.DEVICE_UNREGISTERED     // Catch:{ NoSuchFieldError -> 0x010b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x010b }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x010b }
            L_0x010b:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0117 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0117 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0117 }
                r2 = 21
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0117 }
            L_0x0117:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl.AnonymousClass1.<clinit>():void");
        }
    }

    public static FileTransfer.ReasonCode ftRejectReasonTranslator(FtRejectReason ftRejectReason) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$FtRejectReason[ftRejectReason.ordinal()];
        if (i == 1) {
            return FileTransfer.ReasonCode.REJECTED_MAX_SIZE;
        }
        if (i != 2) {
            return FileTransfer.ReasonCode.UNSPECIFIED;
        }
        return FileTransfer.ReasonCode.FAILED_INITIATION;
    }

    public String getImSessionByChatId(String str) {
        ImSession imSession = ImCache.getInstance().getImSession(str);
        if (imSession == null) {
            return null;
        }
        List<String> participantsString = imSession.getParticipantsString();
        if (participantsString.size() > 0) {
            return participantsString.get(0);
        }
        return null;
    }

    public Map<String, Set<String>> getFileTransfers(boolean z, String str) {
        ImCache instance = ImCache.getInstance();
        TreeMap treeMap = new TreeMap();
        Cursor queryMessages = instance.queryMessages(new String[]{"_id", "chat_id"}, str, (String[]) null, (String) null);
        if (queryMessages != null) {
            try {
                if (queryMessages.getCount() != 0) {
                    while (queryMessages.moveToNext()) {
                        String string = queryMessages.getString(queryMessages.getColumnIndexOrThrow("chat_id"));
                        ImSession imSession = instance.getImSession(string);
                        if (imSession != null) {
                            if (imSession.isGroupChat() == z) {
                                addRecord(string, String.valueOf(queryMessages.getInt(queryMessages.getColumnIndexOrThrow("_id"))), treeMap);
                            }
                        }
                    }
                    queryMessages.close();
                    return treeMap;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        Log.e(LOG_TAG, "getFileTransfers: Message not found.");
        if (queryMessages != null) {
            queryMessages.close();
        }
        return null;
        throw th;
    }

    private void addRecord(String str, String str2, Map<String, Set<String>> map) {
        Set set = map.get(str);
        if (set == null) {
            HashSet hashSet = new HashSet();
            hashSet.add(str2);
            map.put(str, hashSet);
            return;
        }
        set.add(str2);
    }

    public boolean isFtAutoAcceptedModeChangeable() {
        RcsSettingsUtils instance = RcsSettingsUtils.getInstance();
        if (instance != null) {
            return Boolean.parseBoolean(instance.readParameter(ImSettings.AUTO_ACCEPT_FT_CHANGEABLE));
        }
        return false;
    }

    public boolean isFileTransferAutoAccepted() {
        return this.mImModule.getImConfig().isFtAutAccept();
    }

    public boolean isAllowedToTransferFile(ContactId contactId) throws RemoteException {
        Capabilities capabilities;
        if (contactId == null || (capabilities = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getCapabilities(contactId.toString(), (long) Capabilities.FEATURE_FT, SimUtil.getActiveDataPhoneId())) == null || !capabilities.hasFeature(Capabilities.FEATURE_FT)) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x007c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.gsma.services.rcs.filetransfer.IFileTransfer transferAudioMessage(com.gsma.services.rcs.contact.ContactId r17, android.net.Uri r18) throws android.os.RemoteException {
        /*
            r16 = this;
            r1 = r16
            r0 = r18
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "tel:"
            r2.append(r3)
            java.lang.String r3 = r17.toString()
            java.lang.String r3 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r3)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.content.Context r3 = r1.mContext
            java.lang.String r3 = com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils.getFilePathFromUri(r3, r0)
            java.lang.String r4 = com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils.getFileNameFromPath(r3)
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "transferAudioMessage, contentUri = "
            r5.append(r6)
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r3, r5)
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r3 = r1.mImModule
            r5 = 0
            com.sec.ims.util.ImsUri r6 = com.sec.ims.util.ImsUri.parse(r2)
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r2 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED
            com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r7 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED
            java.util.EnumSet r7 = java.util.EnumSet.of(r2, r7)
            r8 = 0
            java.lang.String r9 = "application/audio-message"
            r10 = 0
            r11 = 0
            r12 = 0
            r13 = 0
            r14 = 0
            com.sec.internal.constants.ims.servicemodules.im.FileDisposition r15 = com.sec.internal.constants.ims.servicemodules.im.FileDisposition.ATTACH
            r2 = r3
            r3 = r5
            r5 = r18
            java.util.concurrent.Future r0 = r2.attachFileToSingleChat(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15)
            r2 = 0
            java.lang.Object r0 = r0.get()     // Catch:{ InterruptedException -> 0x0075, ExecutionException -> 0x0070 }
            com.sec.internal.ims.servicemodules.im.FtMessage r0 = (com.sec.internal.ims.servicemodules.im.FtMessage) r0     // Catch:{ InterruptedException -> 0x0075, ExecutionException -> 0x0070 }
            int r0 = r0.getId()     // Catch:{ InterruptedException -> 0x0075, ExecutionException -> 0x0070 }
            java.lang.String r0 = java.lang.String.valueOf(r0)     // Catch:{ InterruptedException -> 0x0075, ExecutionException -> 0x0070 }
            goto L_0x007a
        L_0x0070:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0079
        L_0x0075:
            r0 = move-exception
            r0.printStackTrace()
        L_0x0079:
            r0 = r2
        L_0x007a:
            if (r0 != 0) goto L_0x0084
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "attachFileToSingleChat failed, return null!"
            android.util.Log.e(r0, r1)
            return r2
        L_0x0084:
            com.sec.internal.ims.servicemodules.tapi.service.api.OneToOneFileTransferImpl r2 = new com.sec.internal.ims.servicemodules.tapi.service.api.OneToOneFileTransferImpl
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r1 = r1.mImModule
            r2.<init>((java.lang.String) r0, (com.sec.internal.interfaces.ims.servicemodules.im.IImModule) r1)
            addFileTransferingSession(r0, r2)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl.transferAudioMessage(com.gsma.services.rcs.contact.ContactId, android.net.Uri):com.gsma.services.rcs.filetransfer.IFileTransfer");
    }
}
