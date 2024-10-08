package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.IGroupFileTransferListener;
import com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo;
import com.sec.internal.helper.os.IntentUtil;
import java.util.ArrayList;
import java.util.Set;

public class GroupFileTransferBroadcaster implements IGroupFileTransferBroadcaster {
    private static final String LOG_TAG = "GroupFileTransferBroadcaster";
    private Context mContext;
    private final RemoteCallbackList<IGroupFileTransferListener> mGroupFileTransferListeners = new RemoteCallbackList<>();

    public GroupFileTransferBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addGroupFileTransferListener(IGroupFileTransferListener iGroupFileTransferListener) {
        this.mGroupFileTransferListeners.register(iGroupFileTransferListener);
    }

    public void removeGroupFileTransferListener(IGroupFileTransferListener iGroupFileTransferListener) {
        this.mGroupFileTransferListeners.unregister(iGroupFileTransferListener);
    }

    public void broadcastTransferStateChanged(String str, String str2, FileTransfer.State state, FileTransfer.ReasonCode reasonCode) {
        int beginBroadcast = this.mGroupFileTransferListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onStateChanged(str, str2, state, reasonCode);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str3 = LOG_TAG;
                Log.e(str3, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastTransferprogress(String str, String str2, long j, long j2) {
        int beginBroadcast = this.mGroupFileTransferListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onProgressUpdate(str, str2, j, j2);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastGroupDeliveryInfoStateChanged(String str, String str2, ContactId contactId, GroupDeliveryInfo.Status status, GroupDeliveryInfo.ReasonCode reasonCode) {
        int beginBroadcast = this.mGroupFileTransferListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onDeliveryInfoChanged(str, str2, contactId, status, reasonCode);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str3 = LOG_TAG;
                Log.e(str3, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastDeleted(String str, Set<String> set) {
        int beginBroadcast = this.mGroupFileTransferListeners.beginBroadcast();
        ArrayList arrayList = new ArrayList(set);
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mGroupFileTransferListeners.getBroadcastItem(i).onDeleted(str, arrayList);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str2 = LOG_TAG;
                Log.e(str2, "Can't notify listener : " + e);
            }
        }
        this.mGroupFileTransferListeners.finishBroadcast();
    }

    public void broadcastFileTransferInvitation(String str, UserHandle userHandle) {
        Intent intent = new Intent("com.gsma.services.rcs.filetransfer.action.NEW_FILE_TRANSFER");
        intent.putExtra("transferId", str);
        IntentUtil.sendBroadcast(this.mContext, intent, userHandle, "com.gsma.services.permission.RCS");
    }

    public void broadcastResumeFileTransfer(String str, UserHandle userHandle) {
        Intent intent = new Intent("com.gsma.services.rcs.filetransfer.action.RESUME_FILE_TRANSFER");
        intent.putExtra("transferId", str);
        IntentUtil.sendBroadcast(this.mContext, intent, userHandle, "com.gsma.services.permission.RCS");
    }
}
