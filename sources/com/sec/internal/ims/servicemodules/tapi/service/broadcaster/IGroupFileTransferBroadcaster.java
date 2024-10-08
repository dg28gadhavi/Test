package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.os.UserHandle;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo;
import java.util.Set;

public interface IGroupFileTransferBroadcaster {
    void broadcastDeleted(String str, Set<String> set);

    void broadcastFileTransferInvitation(String str, UserHandle userHandle);

    void broadcastGroupDeliveryInfoStateChanged(String str, String str2, ContactId contactId, GroupDeliveryInfo.Status status, GroupDeliveryInfo.ReasonCode reasonCode);

    void broadcastResumeFileTransfer(String str, UserHandle userHandle);

    void broadcastTransferStateChanged(String str, String str2, FileTransfer.State state, FileTransfer.ReasonCode reasonCode);

    void broadcastTransferprogress(String str, String str2, long j, long j2);
}
