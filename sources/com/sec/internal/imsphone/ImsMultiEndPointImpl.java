package com.sec.internal.imsphone;

import android.net.Uri;
import android.os.RemoteException;
import android.telephony.ims.ImsExternalCallState;
import android.text.TextUtils;
import com.android.ims.internal.IImsExternalCallStateListener;
import com.android.ims.internal.IImsMultiEndpoint;
import com.sec.ims.Dialog;
import com.sec.ims.DialogEvent;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImsMultiEndPointImpl extends IImsMultiEndpoint.Stub {
    private List<ImsExternalCallState> mDialogList = new ArrayList();
    private IImsExternalCallStateListener mImsMultiEndpointListener = null;
    private int mPhoneId = 0;

    public ImsMultiEndPointImpl(int i) {
        this.mPhoneId = i;
    }

    public void setListener(IImsExternalCallStateListener iImsExternalCallStateListener) {
        this.mImsMultiEndpointListener = iImsExternalCallStateListener;
    }

    public IImsExternalCallStateListener getImsExternalCallStateListener() {
        return this.mImsMultiEndpointListener;
    }

    public void requestImsExternalCallStateInfo() throws RemoteException {
        this.mImsMultiEndpointListener.onImsExternalCallStateUpdate(this.mDialogList);
    }

    public void setDialogInfo(DialogEvent dialogEvent, int i) {
        int parseInt;
        this.mDialogList.clear();
        if (dialogEvent.getDialogList().size() == 0) {
            this.mDialogList.add(new ImsExternalCallState(-1, Uri.parse(""), false, 2, 0, false));
            return;
        }
        for (Dialog dialog : dialogEvent.getDialogList()) {
            if (dialog != null) {
                if (SimUtil.getSimMno(this.mPhoneId) == Mno.VZW) {
                    parseInt = ImsCallUtil.getIdForString(dialog.getSipCallId());
                } else {
                    try {
                        parseInt = Integer.parseInt(dialog.getDialogId());
                    } catch (NumberFormatException unused) {
                    }
                }
                int i2 = parseInt;
                String remoteUri = dialog.getRemoteUri();
                if (!TextUtils.isEmpty(remoteUri) && remoteUri.contains(":")) {
                    if (remoteUri.startsWith("tel:")) {
                        remoteUri = remoteUri.replace("tel:", "sip:");
                    }
                    boolean z = !(i == 2 || i == 4 || i == 8) || TextUtils.isEmpty(dialog.getRemoteDispName()) || !remoteUri.contains(dialog.getRemoteDispName());
                    if (!TextUtils.isEmpty(dialog.getRemoteDispName()) && z) {
                        remoteUri = remoteUri + ";displayName=" + dialog.getRemoteDispName();
                    }
                    if (i == 2 || i == 4 || i == 8) {
                        String substring = remoteUri.substring(remoteUri.indexOf(":") + 1);
                        if (!TextUtils.isEmpty(substring)) {
                            remoteUri = remoteUri + ";oir=" + getOirExtraFromDialingNumber(substring);
                            if (substring.contains("Conference call") || dialog.getCallType() == 5 || dialog.getCallType() == 6) {
                                remoteUri = remoteUri + ";cmc_pd_state=" + 1;
                            } else if (dialog.getCallType() == 7 || dialog.getCallType() == 8) {
                                remoteUri = remoteUri + ";cmc_pd_state=" + 2;
                            }
                        }
                        if (i == 4 || i == 8) {
                            i = 2;
                        }
                        remoteUri = remoteUri + ";cmc_type=" + i + ";callslot=" + dialog.getCallSlot();
                    }
                    this.mDialogList.add(new ImsExternalCallState(i2, Uri.parse(remoteUri), dialog.isPullAvailable(), dialog.getState(), DataTypeConvertor.convertToGoogleCallType(dialog.getCallType()), dialog.isHeld()));
                }
            }
        }
        StringBuffer stringBuffer = new StringBuffer("DE=");
        for (ImsExternalCallState next : this.mDialogList) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(next.getCallId() % 100000);
            sb.append(",");
            String str = "T";
            sb.append(next.getCallState() == 1 ? "C" : str);
            sb.append(",");
            sb.append(next.isCallHeld() ? "H" : "A");
            sb.append(",");
            if (!next.isCallPullable()) {
                str = "F";
            }
            sb.append(str);
            sb.append("]");
            stringBuffer.append(sb.toString());
        }
        IMSLog.c(LogClass.VOLTE_DIALOG_EVENT, stringBuffer.toString());
    }

    public List<ImsExternalCallState> getExternalCallStateList() {
        return this.mDialogList;
    }

    private int getOirExtraFromDialingNumber(String str) {
        if (NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equalsIgnoreCase(str)) {
            return 3;
        }
        if ("RESTRICTED".equalsIgnoreCase(str) || str.toLowerCase(Locale.US).contains("anonymous")) {
            return 1;
        }
        return "Coin line/payphone".equalsIgnoreCase(str) ? 4 : 2;
    }

    public void setP2pPushDialogInfo(DialogEvent dialogEvent, int i) throws RemoteException {
        this.mDialogList.clear();
        for (Dialog dialog : dialogEvent.getDialogList()) {
            if (dialog != null) {
                try {
                    int parseInt = Integer.parseInt(dialog.getDialogId());
                    this.mDialogList.add(new ImsExternalCallState(parseInt, Uri.parse("sip:D2D@samsungims.com;d2d.push"), dialog.isPullAvailable(), dialog.getState(), DataTypeConvertor.convertToGoogleCallType(dialog.getCallType()), dialog.isHeld()));
                } catch (NumberFormatException unused) {
                }
            }
        }
        requestImsExternalCallStateInfo();
    }
}
