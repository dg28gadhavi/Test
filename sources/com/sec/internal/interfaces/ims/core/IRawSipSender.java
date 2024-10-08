package com.sec.internal.interfaces.ims.core;

import android.os.Message;

public interface IRawSipSender {
    void send(int i, String str, Message message);
}
