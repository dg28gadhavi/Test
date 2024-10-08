package com.sec.internal.ims.gba;

import android.util.Base64;
import android.util.Log;
import com.sec.internal.helper.StrUtil;
import java.util.Arrays;

public class Nonce {
    private static final int AUTN_SIZE = 16;
    private static final int RAND_SIZE = 16;
    private static final String TAG = "Nonce";
    private byte[] autn;
    private byte[] rand;
    private byte[] randAutn;
    private byte[] serverData;
    private String strNonce;

    public byte[] getAutn() {
        return this.autn;
    }

    public void setAutn(byte[] bArr) {
        this.autn = bArr;
    }

    public byte[] getRand() {
        return this.rand;
    }

    public void setRand(byte[] bArr) {
        this.rand = bArr;
    }

    public void setServerData(byte[] bArr) {
        this.serverData = bArr;
    }

    public void setStrNonce(String str) {
        this.strNonce = str;
    }

    public String getStrNonce() {
        return this.strNonce;
    }

    public byte[] getAutnRand() {
        return this.randAutn;
    }

    public void setAutnRand(byte[] bArr) {
        this.randAutn = bArr;
    }

    public String toString() {
        String str;
        String str2;
        String str3;
        String str4;
        StringBuilder sb = new StringBuilder();
        sb.append("Nonce [");
        String str5 = "";
        if (this.autn != null) {
            str = "autn=" + Arrays.toString(this.autn) + ", ";
        } else {
            str = str5;
        }
        sb.append(str);
        if (this.rand != null) {
            str2 = "rand=" + Arrays.toString(this.rand) + ", ";
        } else {
            str2 = str5;
        }
        sb.append(str2);
        if (this.serverData != null) {
            str3 = "serverData=" + Arrays.toString(this.serverData) + ", ";
        } else {
            str3 = str5;
        }
        sb.append(str3);
        if (this.strNonce != null) {
            str4 = "strNonce=" + this.strNonce + ", ";
        } else {
            str4 = str5;
        }
        sb.append(str4);
        if (this.randAutn != null) {
            str5 = "autnRand=" + Arrays.toString(this.randAutn);
        }
        sb.append(str5);
        sb.append("]");
        return sb.toString();
    }

    public void parseNonce(String str) {
        byte[] bArr = new byte[17];
        byte[] bArr2 = new byte[17];
        byte[] bArr3 = new byte[34];
        setStrNonce(str);
        byte[] decode = Base64.decode(str, 0);
        if (decode.length >= 16) {
            bArr[0] = 16;
            System.arraycopy(decode, 0, bArr, 1, 16);
            setRand(bArr);
            System.arraycopy(getRand(), 0, bArr3, 0, 17);
        }
        Log.i(TAG, "HexRAND is: " + StrUtil.bytesToHexString(getRand()));
        if (decode.length >= 32) {
            bArr2[0] = 16;
            System.arraycopy(decode, 16, bArr2, 1, 16);
            setAutn(bArr2);
            System.arraycopy(getAutn(), 0, bArr3, 17, 17);
        }
        Log.i(TAG, "Hex Autn is: " + StrUtil.bytesToHexString(getAutn()));
        if (decode.length > 32) {
            setServerData(Arrays.copyOfRange(decode, 32, decode.length - 1));
        }
        Log.i(TAG, "Hex RandAutn is: " + StrUtil.bytesToHexString(bArr3));
        setAutnRand(bArr3);
        Log.d(TAG, toString());
    }
}
