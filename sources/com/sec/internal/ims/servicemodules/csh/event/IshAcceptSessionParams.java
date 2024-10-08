package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Message;

public class IshAcceptSessionParams extends CshAcceptSessionParams {
    public String mPath;

    public IshAcceptSessionParams(int i, String str, Message message) {
        super(i, message);
        this.mPath = str;
    }
}
