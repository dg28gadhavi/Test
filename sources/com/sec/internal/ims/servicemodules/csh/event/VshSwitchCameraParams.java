package com.sec.internal.ims.servicemodules.csh.event;

public class VshSwitchCameraParams {
    public ICshSuccessCallback mCallback;

    public VshSwitchCameraParams(ICshSuccessCallback iCshSuccessCallback) {
        this.mCallback = iCshSuccessCallback;
    }
}
