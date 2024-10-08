package com.sec.internal.ims.servicemodules.volte2;

public interface IDiagnosisController {
    boolean isCallDrop(int i);

    void sendPSCallInfo();

    void sendPSDailyInfo();
}
