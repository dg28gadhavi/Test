package com.sec.internal.imsphone;

import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsInfo;
import android.text.TextUtils;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener;
import com.sec.ims.ss.IImsUtEventListener;
import com.sec.internal.ims.servicemodules.ss.UtConstant;

public class ImsUtEventListener extends IImsUtEventListener.Stub {
    ImsUtImpl mImsUtImpl;

    ImsUtEventListener(ImsUtImpl imsUtImpl) {
        this.mImsUtImpl = imsUtImpl;
    }

    public void onUtConfigurationUpdateFailed(int i, Bundle bundle) throws RemoteException {
        if (this.mImsUtImpl.mListener != null) {
            int i2 = bundle.getInt("errorCode", 0);
            this.mImsUtImpl.mListener.utConfigurationUpdateFailed((IImsUt) null, i, new ImsReasonInfo(DataTypeConvertor.convertUtErrorReasonToFw(i2), 0, bundle.getString("errorMsg")));
        }
    }

    public void onUtConfigurationQueryFailed(int i, Bundle bundle) throws RemoteException {
        if (this.mImsUtImpl.mListener != null) {
            int i2 = bundle.getInt("errorCode", 0);
            this.mImsUtImpl.mListener.utConfigurationQueryFailed((IImsUt) null, i, new ImsReasonInfo(DataTypeConvertor.convertUtErrorReasonToFw(i2), 0, bundle.getString("errorMsg")));
        }
    }

    public void onUtConfigurationQueried(int i, Bundle bundle) throws RemoteException {
        IImsUtListener iImsUtListener = this.mImsUtImpl.mListener;
        if (iImsUtListener != null) {
            iImsUtListener.utConfigurationQueried((IImsUt) null, i, bundle);
        }
    }

    public void onUtConfigurationUpdated(int i) throws RemoteException {
        IImsUtListener iImsUtListener = this.mImsUtImpl.mListener;
        if (iImsUtListener != null) {
            iImsUtListener.utConfigurationUpdated((IImsUt) null, i);
        }
    }

    public void onUtConfigurationCallWaitingQueried(int i, boolean z) throws RemoteException {
        if (this.mImsUtImpl.mListener != null) {
            ImsSsInfo[] imsSsInfoArr = (ImsSsInfo[]) ImsSsInfo.CREATOR.newArray(1);
            imsSsInfoArr[0] = new ImsSsInfo.Builder(z ? 1 : 0).build();
            this.mImsUtImpl.mListener.utConfigurationCallWaitingQueried((IImsUt) null, i, imsSsInfoArr);
        }
    }

    public void onUtConfigurationCallForwardQueried(int i, Bundle[] bundleArr) throws RemoteException {
        if (this.mImsUtImpl.mListener != null) {
            ImsCallForwardInfo[] imsCallForwardInfoArr = (ImsCallForwardInfo[]) ImsCallForwardInfo.CREATOR.newArray(bundleArr.length);
            for (int i2 = 0; i2 < bundleArr.length; i2++) {
                int i3 = bundleArr[i2].getInt("status", 0);
                int i4 = bundleArr[i2].getInt(UtConstant.CONDITION, 0);
                int i5 = bundleArr[i2].getInt("NoReplyTimer", 0);
                int i6 = bundleArr[i2].getInt(UtConstant.TOA, 0);
                String string = bundleArr[i2].getString("number");
                if (TextUtils.isEmpty(string)) {
                    string = "";
                }
                imsCallForwardInfoArr[i2] = new ImsCallForwardInfo(i4, i3, i6, bundleArr[i2].getInt(UtConstant.SERVICECLASS, 1), string, i5);
            }
            this.mImsUtImpl.mListener.utConfigurationCallForwardQueried((IImsUt) null, i, imsCallForwardInfoArr);
        }
    }

    public void onUtConfigurationCallBarringQueried(int i, Bundle[] bundleArr) throws RemoteException {
        if (this.mImsUtImpl.mListener != null) {
            ImsSsInfo[] imsSsInfoArr = (ImsSsInfo[]) ImsSsInfo.CREATOR.newArray(bundleArr.length);
            for (int i2 = 0; i2 < bundleArr.length; i2++) {
                int i3 = bundleArr[i2].getInt("status", 0);
                if (bundleArr[i2].getInt(UtConstant.CONDITION, 0) == 10) {
                    imsSsInfoArr[i2] = new ImsSsInfo.Builder(i3).setIncomingCommunicationBarringNumber(bundleArr[i2].getString("number")).build();
                } else {
                    imsSsInfoArr[i2] = new ImsSsInfo.Builder(i3).setServiceClass(bundleArr[i2].getInt(UtConstant.SERVICECLASS, 1)).build();
                }
            }
            this.mImsUtImpl.mListener.utConfigurationCallBarringQueried((IImsUt) null, i, imsSsInfoArr);
        }
    }
}
