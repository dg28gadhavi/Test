package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import com.gsma.services.rcs.extension.IMultimediaSessionServiceConfiguration;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;

public class MultimediaSessionServiceConfigurationImpl extends IMultimediaSessionServiceConfiguration.Stub {
    private static MultimediaSessionServiceConfigurationImpl multimediaSessionServiceConfigurationImpl;
    private ISessionModule mSessionModule;

    private MultimediaSessionServiceConfigurationImpl(ISessionModule iSessionModule) {
        this.mSessionModule = iSessionModule;
    }

    public static MultimediaSessionServiceConfigurationImpl getInstance(ISessionModule iSessionModule) {
        if (multimediaSessionServiceConfigurationImpl == null) {
            multimediaSessionServiceConfigurationImpl = new MultimediaSessionServiceConfigurationImpl(iSessionModule);
        }
        return multimediaSessionServiceConfigurationImpl;
    }

    public boolean isServiceActivated(String str) throws RemoteException {
        ISessionModule iSessionModule = this.mSessionModule;
        if (iSessionModule != null) {
            return iSessionModule.isServiceActivated(str);
        }
        return false;
    }

    public long getInactivityTimeout() throws RemoteException {
        return this.mSessionModule.getInactivityTimeout();
    }

    public long getMessagingSessionInactivityTimeout(String str) throws RemoteException {
        return this.mSessionModule.getInactivityTimeout();
    }

    public int getMessageMaxLength() throws RemoteException {
        return this.mSessionModule.getMaxMsrpLengthForExtensions();
    }
}
