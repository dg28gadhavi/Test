package com.sec.internal.ims.util;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.CellIdentity;
import com.android.internal.telephony.ISemTelephony;
import com.sec.internal.helper.SimUtil;

public class SemTelephonyAdapter {
    private static ISemTelephony getISemTelephony() throws UnsupportedOperationException {
        ISemTelephony asInterface = ISemTelephony.Stub.asInterface(ServiceManager.getService("isemtelephony"));
        if (asInterface != null) {
            return asInterface;
        }
        throw new UnsupportedOperationException("Unable to find ISemTelephony interface.");
    }

    public static boolean isSupportLteCapaOptionC(int i) {
        try {
            return getISemTelephony().isSupportLteCapaOptionC(i);
        } catch (RemoteException | IllegalArgumentException | UnsupportedOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isSupportedNrca(int i) {
        try {
            return getISemTelephony().getSupportedNrca(i);
        } catch (RemoteException | IllegalArgumentException | UnsupportedOperationException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static CellIdentity getCellIdentityFromSemTelephony(int i, String str, String str2) {
        try {
            return getISemTelephony().getCellLocationBySubId(SimUtil.getSubId(i), str, str2);
        } catch (RemoteException | IllegalArgumentException | UnsupportedOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendVolteState(int i, boolean z) {
        try {
            getISemTelephony().sendVolteState(SimUtil.getSubId(i), z);
        } catch (RemoteException | IllegalArgumentException | UnsupportedOperationException e) {
            e.printStackTrace();
        }
    }

    public enum CpUacType {
        CP_UAC_NOT_SUPPORT(0),
        CP_UAC_SUPPORT_REL15(1),
        CP_UAC_SUPPORT_REL16(2);
        
        private final int uacType;

        private CpUacType(int i) {
            this.uacType = i;
        }

        public static CpUacType fromValue(int i) throws IllegalArgumentException {
            try {
                return values()[i];
            } catch (ArrayIndexOutOfBoundsException unused) {
                throw new IllegalArgumentException("Unknown enum value:" + i);
            }
        }
    }

    public static CpUacType getSupportUacType(int i) {
        CpUacType cpUacType = CpUacType.CP_UAC_NOT_SUPPORT;
        try {
            return CpUacType.fromValue(getISemTelephony().getSupportUacType(i));
        } catch (RemoteException | IllegalArgumentException | UnsupportedOperationException e) {
            e.printStackTrace();
            return cpUacType;
        }
    }
}
