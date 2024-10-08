package com.sec.internal.ims.core.iil;

import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;

public class IilIpcMessage extends IpcMessage {
    private static final String LOG_TAG = "IilIpcMessage";
    private static final int MAX_IIL_REGISTRATION = 268;

    public IilIpcMessage() {
    }

    public IilIpcMessage(int i, int i2, int i3) {
        super(i, i2, i3);
    }

    public static IilIpcMessage encodeIilConnected() {
        IilIpcMessage iilIpcMessage = new IilIpcMessage(112, 18, 3);
        iilIpcMessage.mIpcBody = null;
        iilIpcMessage.createIpcMessage();
        return iilIpcMessage;
    }

    public static IilIpcMessage encodeImsRegisgtrationInfo(int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i2, int i3, int i4, int i5, int i6, int i7, String str, int i8) {
        int i9;
        byte[] bArr;
        boolean z6 = z;
        boolean z7 = z2;
        boolean z8 = z3;
        boolean z9 = z4;
        boolean z10 = z5;
        int i10 = i4;
        int i11 = i6;
        int i12 = i7;
        String str2 = str;
        int i13 = i8;
        IilIpcMessage iilIpcMessage = new IilIpcMessage(112, 1, 3);
        if (str2 != null) {
            bArr = str2.getBytes(Charset.forName("UTF-8"));
            i9 = bArr.length;
            if (i9 > 256) {
                i9 = 256;
            }
        } else {
            bArr = null;
            i9 = 0;
        }
        byte[] bArr2 = new byte[268];
        IMSLog.i(LOG_TAG, "rat=" + i13 + ", isVoLte=" + z6 + ", isSmsIp=" + z7 + ", isRcs=" + z8 + ", isPsVT=" + z9 + ", isCdpn=" + z10 + ", ecmp=" + i10);
        if (z7) {
            z6 |= true;
        }
        if (z8) {
            z6 |= true;
        }
        if (z9) {
            z6 |= true;
        }
        if (z10) {
            z6 |= true;
        }
        bArr2[0] = (byte) i;
        bArr2[1] = z6 ? (byte) 1 : 0;
        bArr2[2] = (byte) i2;
        bArr2[3] = (byte) i3;
        bArr2[4] = (byte) i10;
        bArr2[5] = (byte) i5;
        bArr2[6] = (byte) (i11 >> 8);
        bArr2[7] = (byte) i11;
        bArr2[8] = (byte) (i12 >> 8);
        bArr2[9] = (byte) i12;
        bArr2[10] = (byte) i9;
        int i14 = 11;
        int i15 = 0;
        while (i15 < i9) {
            bArr2[i14] = bArr[i15];
            i15++;
            i14++;
        }
        while (i14 < 267) {
            bArr2[i14] = 0;
            i14++;
        }
        bArr2[267] = (byte) i13;
        iilIpcMessage.mIpcBody = bArr2;
        iilIpcMessage.createIpcMessage();
        return iilIpcMessage;
    }

    public static IilIpcMessage encodeImsPreferenceNoti(IilImsPreference iilImsPreference, int i) {
        IilIpcMessage iilIpcMessage = new IilIpcMessage(112, 6, 3);
        IMSLog.i(LOG_TAG, iilImsPreference.toString() + "NotiType : " + i);
        iilIpcMessage.mIpcBody = IilImsPreference.toByteArray(iilImsPreference, i);
        iilIpcMessage.createIpcMessage();
        return iilIpcMessage;
    }

    public static IilIpcMessage encodeImsPreferenceResp(IilImsPreference iilImsPreference) {
        IilIpcMessage iilIpcMessage = new IilIpcMessage(112, 6, 2);
        IMSLog.i(LOG_TAG, iilImsPreference.toString());
        iilIpcMessage.mIpcBody = IilImsPreference.toByteArray(iilImsPreference, 0);
        iilIpcMessage.createIpcMessage();
        return iilIpcMessage;
    }

    public static IilIpcMessage encodeImsRetryOverNoti(int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i2, int i3) {
        IilIpcMessage iilIpcMessage = new IilIpcMessage(112, 12, 3);
        byte[] bArr = new byte[4];
        IMSLog.i(LOG_TAG, "isVoLte " + z + " isSmsIp " + z2 + " isRcs " + z3 + " isPsVT " + z4 + " isCdpn " + z5 + " ecmp" + i3);
        if (z2) {
            z |= true;
        }
        if (z3) {
            z |= true;
        }
        if (z4) {
            z |= true;
        }
        if (z5) {
            z |= true;
        }
        bArr[0] = (byte) i;
        bArr[1] = z ? (byte) 1 : 0;
        bArr[2] = (byte) i2;
        bArr[3] = (byte) i3;
        iilIpcMessage.mIpcBody = bArr;
        iilIpcMessage.createIpcMessage();
        return iilIpcMessage;
    }

    public static IilIpcMessage ImsChangePreferredNetwork() {
        IilIpcMessage iilIpcMessage = new IilIpcMessage(112, 21, 3);
        IMSLog.i(LOG_TAG, "ImsChangePreferredNetwork()");
        iilIpcMessage.mIpcBody = null;
        iilIpcMessage.createIpcMessage();
        return iilIpcMessage;
    }
}
