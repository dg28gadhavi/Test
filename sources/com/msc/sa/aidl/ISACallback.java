package com.msc.sa.aidl;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISACallback extends IInterface {
    void onReceiveAccessToken(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveAuthCode(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveChecklistValidation(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveDisclaimerAgreement(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceivePasswordConfirmation(int i, boolean z, Bundle bundle) throws RemoteException;

    void onReceiveSCloudAccessToken(int i, boolean z, Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements ISACallback {
        static final int TRANSACTION_onReceiveAccessToken = 1;
        static final int TRANSACTION_onReceiveAuthCode = 4;
        static final int TRANSACTION_onReceiveChecklistValidation = 2;
        static final int TRANSACTION_onReceiveDisclaimerAgreement = 3;
        static final int TRANSACTION_onReceivePasswordConfirmation = 6;
        static final int TRANSACTION_onReceiveSCloudAccessToken = 5;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.msc.sa.aidl.ISACallback");
        }

        public static ISACallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.msc.sa.aidl.ISACallback");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof ISACallback)) {
                return new Proxy(iBinder);
            }
            return (ISACallback) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface("com.msc.sa.aidl.ISACallback");
            }
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceNoDataAvail();
                        onReceiveAccessToken(parcel.readInt(), parcel.readBoolean(), (Bundle) parcel.readTypedObject(Bundle.CREATOR));
                        parcel2.writeNoException();
                        break;
                    case 2:
                        parcel.enforceNoDataAvail();
                        onReceiveChecklistValidation(parcel.readInt(), parcel.readBoolean(), (Bundle) parcel.readTypedObject(Bundle.CREATOR));
                        parcel2.writeNoException();
                        break;
                    case 3:
                        parcel.enforceNoDataAvail();
                        onReceiveDisclaimerAgreement(parcel.readInt(), parcel.readBoolean(), (Bundle) parcel.readTypedObject(Bundle.CREATOR));
                        parcel2.writeNoException();
                        break;
                    case 4:
                        parcel.enforceNoDataAvail();
                        onReceiveAuthCode(parcel.readInt(), parcel.readBoolean(), (Bundle) parcel.readTypedObject(Bundle.CREATOR));
                        parcel2.writeNoException();
                        break;
                    case 5:
                        parcel.enforceNoDataAvail();
                        onReceiveSCloudAccessToken(parcel.readInt(), parcel.readBoolean(), (Bundle) parcel.readTypedObject(Bundle.CREATOR));
                        parcel2.writeNoException();
                        break;
                    case 6:
                        parcel.enforceNoDataAvail();
                        onReceivePasswordConfirmation(parcel.readInt(), parcel.readBoolean(), (Bundle) parcel.readTypedObject(Bundle.CREATOR));
                        parcel2.writeNoException();
                        break;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
                return true;
            }
            parcel2.writeString("com.msc.sa.aidl.ISACallback");
            return true;
        }

        private static class Proxy implements ISACallback {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }
        }
    }
}
