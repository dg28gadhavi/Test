package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.IOneToOneFileTransferListener;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import java.util.ArrayList;
import java.util.Set;

public class OneToOneFileTransferBroadcaster implements IOneToOneFileTransferBroadcaster {
    private static final String LOG_TAG = "OneToOneFileTransferBroadcaster";
    private Context mContext;
    private final RemoteCallbackList<IOneToOneFileTransferListener> mOneToOneFileTransferListeners = new RemoteCallbackList<>();

    public OneToOneFileTransferBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addOneToOneFileTransferListener(IOneToOneFileTransferListener iOneToOneFileTransferListener) {
        this.mOneToOneFileTransferListeners.register(iOneToOneFileTransferListener);
    }

    public void removeOneToOneFileTransferListener(IOneToOneFileTransferListener iOneToOneFileTransferListener) {
        this.mOneToOneFileTransferListeners.unregister(iOneToOneFileTransferListener);
    }

    public void broadcastTransferStateChanged(ContactId contactId, String str, FileTransfer.State state, FileTransfer.ReasonCode reasonCode) {
        Log.d(LOG_TAG, "start : broadcastMessageStatusChanged()");
        int beginBroadcast = this.mOneToOneFileTransferListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mOneToOneFileTransferListeners.getBroadcastItem(i).onStateChanged(contactId, str, state, reasonCode);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str2 = LOG_TAG;
                Log.e(str2, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneFileTransferListeners.finishBroadcast();
    }

    public void broadcastTransferprogress(ContactId contactId, String str, long j, long j2) {
        Log.d(LOG_TAG, "start : broadcastTransferprogress()");
        int beginBroadcast = this.mOneToOneFileTransferListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mOneToOneFileTransferListeners.getBroadcastItem(i).onProgressUpdate(contactId, str, j, j2);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneFileTransferListeners.finishBroadcast();
    }

    public void broadcastDeleted(String str, Set<String> set) {
        Log.d(LOG_TAG, "start : broadcastDeleted()");
        ContactId contactId = new ContactId(str);
        ArrayList arrayList = new ArrayList(set);
        int beginBroadcast = this.mOneToOneFileTransferListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mOneToOneFileTransferListeners.getBroadcastItem(i).onDeleted(contactId, arrayList);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str2 = LOG_TAG;
                Log.e(str2, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneFileTransferListeners.finishBroadcast();
    }

    public void broadcastFileTransferInvitation(String str, UserHandle userHandle) {
        Log.d(LOG_TAG, "start : broadcastFileTransferInvitation()");
        Intent intent = new Intent("com.gsma.services.rcs.filetransfer.action.NEW_FILE_TRANSFER");
        intent.putExtra("transferId", str);
        IntentUtil.sendBroadcast(this.mContext, intent, userHandle, "com.gsma.services.permission.RCS");
    }

    public void broadcastResumeFileTransfer(String str, UserHandle userHandle) {
        Log.d(LOG_TAG, "start : broadcastResumeFileTransfer()");
        Intent intent = new Intent("com.gsma.services.rcs.filetransfer.action.RESUME_FILE_TRANSFER");
        intent.putExtra("transferId", str);
        IntentUtil.sendBroadcast(this.mContext, intent, userHandle, "com.gsma.services.permission.RCS");
    }

    public void broadcastUndeliveredFileTransfer(ContactId contactId, UserHandle userHandle) {
        Log.d(LOG_TAG, "start : broadcastResumeFileTransfer()");
        Intent intent = new Intent("com.gsma.services.rcs.filetransfer.action.UNDELIVERED_FILE_TRANSFERS");
        intent.putExtra(ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, contactId);
        IntentUtil.sendBroadcast(this.mContext, intent, userHandle, "com.gsma.services.permission.RCS");
    }
}
