package com.voltecrypt.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.voltecrypt.service.SXICTQMVoLTECallBack;

public interface SXICTQMVoLTECryptService extends IInterface {
    void checkNeedGoClientApp(int i) throws RemoteException;

    int notifyAuthenticationStatus(int i, String str, String str2) throws RemoteException;

    void notifyLoginResult(int i, String str) throws RemoteException;

    int notifyPeerProfileStatus(int i, String str, String str2, String str3) throws RemoteException;

    int notifyQMKeyStatus(int i, String str, String str2, byte[] bArr, String str3) throws RemoteException;

    int notifyVoLTEStatus(int i, String str) throws RemoteException;

    int registerVoLTECallback(SXICTQMVoLTECallBack sXICTQMVoLTECallBack) throws RemoteException;

    public static abstract class Stub extends Binder implements SXICTQMVoLTECryptService {
        static final int TRANSACTION_checkNeedGoClientApp = 6;
        static final int TRANSACTION_notifyAuthenticationStatus = 2;
        static final int TRANSACTION_notifyLoginResult = 7;
        static final int TRANSACTION_notifyPeerProfileStatus = 3;
        static final int TRANSACTION_notifyQMKeyStatus = 4;
        static final int TRANSACTION_notifyVoLTEStatus = 5;
        static final int TRANSACTION_registerVoLTECallback = 1;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, "com.voltecrypt.service.SXICTQMVoLTECryptService");
        }

        public static SXICTQMVoLTECryptService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.voltecrypt.service.SXICTQMVoLTECryptService");
            if (queryLocalInterface == null || !(queryLocalInterface instanceof SXICTQMVoLTECryptService)) {
                return new Proxy(iBinder);
            }
            return (SXICTQMVoLTECryptService) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface("com.voltecrypt.service.SXICTQMVoLTECryptService");
            }
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        SXICTQMVoLTECallBack asInterface = SXICTQMVoLTECallBack.Stub.asInterface(parcel.readStrongBinder());
                        parcel.enforceNoDataAvail();
                        int registerVoLTECallback = registerVoLTECallback(asInterface);
                        parcel2.writeNoException();
                        parcel2.writeInt(registerVoLTECallback);
                        break;
                    case 2:
                        int readInt = parcel.readInt();
                        String readString = parcel.readString();
                        String readString2 = parcel.readString();
                        parcel.enforceNoDataAvail();
                        int notifyAuthenticationStatus = notifyAuthenticationStatus(readInt, readString, readString2);
                        parcel2.writeNoException();
                        parcel2.writeInt(notifyAuthenticationStatus);
                        break;
                    case 3:
                        int readInt2 = parcel.readInt();
                        String readString3 = parcel.readString();
                        String readString4 = parcel.readString();
                        String readString5 = parcel.readString();
                        parcel.enforceNoDataAvail();
                        int notifyPeerProfileStatus = notifyPeerProfileStatus(readInt2, readString3, readString4, readString5);
                        parcel2.writeNoException();
                        parcel2.writeInt(notifyPeerProfileStatus);
                        break;
                    case 4:
                        int readInt3 = parcel.readInt();
                        String readString6 = parcel.readString();
                        String readString7 = parcel.readString();
                        byte[] createByteArray = parcel.createByteArray();
                        String readString8 = parcel.readString();
                        parcel.enforceNoDataAvail();
                        int notifyQMKeyStatus = notifyQMKeyStatus(readInt3, readString6, readString7, createByteArray, readString8);
                        parcel2.writeNoException();
                        parcel2.writeInt(notifyQMKeyStatus);
                        break;
                    case 5:
                        int readInt4 = parcel.readInt();
                        String readString9 = parcel.readString();
                        parcel.enforceNoDataAvail();
                        int notifyVoLTEStatus = notifyVoLTEStatus(readInt4, readString9);
                        parcel2.writeNoException();
                        parcel2.writeInt(notifyVoLTEStatus);
                        break;
                    case 6:
                        int readInt5 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        checkNeedGoClientApp(readInt5);
                        parcel2.writeNoException();
                        break;
                    case 7:
                        int readInt6 = parcel.readInt();
                        String readString10 = parcel.readString();
                        parcel.enforceNoDataAvail();
                        notifyLoginResult(readInt6, readString10);
                        parcel2.writeNoException();
                        break;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
                return true;
            }
            parcel2.writeString("com.voltecrypt.service.SXICTQMVoLTECryptService");
            return true;
        }

        private static class Proxy implements SXICTQMVoLTECryptService {
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
