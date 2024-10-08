package com.sec.internal.ims.mdmi;

import com.sec.internal.ims.mdmi.MdmiServiceModule;

public interface MdmiE911Listener {
    void notifySipMsg(MdmiServiceModule.msgType msgtype, long j);

    void onCallEnded();
}
