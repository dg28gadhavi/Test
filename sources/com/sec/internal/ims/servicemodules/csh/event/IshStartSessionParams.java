package com.sec.internal.ims.servicemodules.csh.event;

import android.os.Message;

public class IshStartSessionParams extends CshStartSessionParams {
    public IshFile mfile;

    public IshStartSessionParams(String str, IshFile ishFile, Message message) {
        super(str, message);
        this.mfile = ishFile;
    }

    public String toString() {
        return "IshStartSessionParams " + super.toString() + " " + this.mfile.toString();
    }
}
