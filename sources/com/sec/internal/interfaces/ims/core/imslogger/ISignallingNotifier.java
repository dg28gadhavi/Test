package com.sec.internal.interfaces.ims.core.imslogger;

public interface ISignallingNotifier {
    public static final String ACTION_SIP_MESSAGE = "com.sec.imsservice.sip.signalling";
    public static final String PERMISSION = "com.sec.imsservice.sip.signalling.READ_PERMISSION";

    public enum PackageStatus {
        NOT_INSTALLED,
        INSTALLED,
        DM_CONNECTED,
        DM_DISCONNECTED,
        EMERGENCY_MODE
    }

    boolean send(Object obj);
}
