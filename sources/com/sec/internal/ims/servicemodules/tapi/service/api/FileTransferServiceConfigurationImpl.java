package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.filetransfer.FileTransferServiceConfiguration;
import com.gsma.services.rcs.filetransfer.IFileTransferServiceConfiguration;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.tapi.service.defaultconst.FileTransferDefaultConst;
import com.sec.internal.ims.util.RcsSettingsUtils;

public class FileTransferServiceConfigurationImpl extends IFileTransferServiceConfiguration.Stub {
    private static final String LOG_TAG = FileTransferServiceConfigurationImpl.class.getSimpleName();
    private ImConfig mConfig;
    private RcsSettingsUtils rcsSetting = null;

    public long getMaxAudioMessageLength() throws RemoteException {
        return 600;
    }

    public int getMaxFileTransfers() throws RemoteException {
        return 10;
    }

    public boolean isGroupFileTransferSupported() throws RemoteException {
        return true;
    }

    public FileTransferServiceConfigurationImpl(ImConfig imConfig) {
        this.mConfig = imConfig;
        this.rcsSetting = RcsSettingsUtils.getInstance();
        String str = LOG_TAG;
        Log.d(str, "rcsSetting: " + this.rcsSetting);
    }

    public void setAutoAccept(boolean z) throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FILE_TRANSFER, z);
        }
    }

    public void setAutoAcceptInRoaming(boolean z) throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FT_IN_ROAMING, z);
        }
    }

    public void setImageResizeOption(int i) throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeParameter(ImSettings.KEY_IMAGE_RESIZE_OPTION, String.valueOf(i));
        }
    }

    public int getImageResizeOption() throws RemoteException {
        FileTransferServiceConfiguration.ImageResizeOption imageResizeOption = FileTransferDefaultConst.DEFALUT_IMAGERESIZEOPTION;
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            String readParameter = rcsSettingsUtils.readParameter(ImSettings.KEY_IMAGE_RESIZE_OPTION);
            int parseInt = Integer.parseInt(readParameter);
            FileTransferServiceConfiguration.ImageResizeOption valueOf = FileTransferServiceConfiguration.ImageResizeOption.valueOf(parseInt);
            String str = LOG_TAG;
            Log.d(str, "start : getImageResizeOption() mValue:" + readParameter + ", value:" + parseInt + ", imageResizeOption=" + valueOf);
            imageResizeOption = valueOf;
        }
        int i = imageResizeOption.toInt();
        String str2 = LOG_TAG;
        Log.d(str2, "start : getImageResizeOption() vv=" + i);
        return i;
    }

    public long getMaxSize() throws RemoteException {
        ImConfig imConfig = this.mConfig;
        if (imConfig != null) {
            return imConfig.getMaxSizeFileTr();
        }
        return 0;
    }

    public long getWarnSize() throws RemoteException {
        ImConfig imConfig = this.mConfig;
        if (imConfig != null) {
            return imConfig.getFtWarnSize();
        }
        return 0;
    }

    public boolean isAutoAcceptEnabled() throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        boolean parseBoolean = rcsSettingsUtils != null ? Boolean.parseBoolean(rcsSettingsUtils.readParameter(ImSettings.AUTO_ACCEPT_FILE_TRANSFER)) : false;
        ImConfig imConfig = this.mConfig;
        return imConfig != null ? imConfig.isFtAutAccept() : parseBoolean;
    }

    public boolean isAutoAcceptInRoamingEnabled() throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        boolean parseBoolean = rcsSettingsUtils != null ? Boolean.parseBoolean(rcsSettingsUtils.readParameter(ImSettings.AUTO_ACCEPT_FT_IN_ROAMING)) : false;
        ImConfig imConfig = this.mConfig;
        return imConfig != null ? imConfig.isFtAutAccept() : parseBoolean;
    }

    public boolean isAutoAcceptModeChangeable() throws RemoteException {
        RcsSettingsUtils rcsSettingsUtils = this.rcsSetting;
        if (rcsSettingsUtils != null) {
            return Boolean.parseBoolean(rcsSettingsUtils.readParameter(ImSettings.AUTO_ACCEPT_FT_CHANGEABLE));
        }
        return false;
    }
}
