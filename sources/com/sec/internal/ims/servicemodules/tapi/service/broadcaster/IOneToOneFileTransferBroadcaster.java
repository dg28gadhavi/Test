package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.os.UserHandle;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import java.util.Set;

public interface IOneToOneFileTransferBroadcaster {
    void broadcastDeleted(String str, Set<String> set);

    void broadcastFileTransferInvitation(String str, UserHandle userHandle);

    void broadcastResumeFileTransfer(String str, UserHandle userHandle);

    void broadcastTransferStateChanged(ContactId contactId, String str, FileTransfer.State state, FileTransfer.ReasonCode reasonCode);

    void broadcastTransferprogress(ContactId contactId, String str, long j, long j2);
}
