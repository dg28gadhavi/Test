package com.sec.sve;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IImsMediaEventListener extends IInterface {
    public static final String DESCRIPTOR = "com.sec.sve.IImsMediaEventListener";

    public static class Default implements IImsMediaEventListener {
        public IBinder asBinder() {
            return null;
        }

        public void onAudioInjectionEnded(long j, long j2) throws RemoteException {
        }

        public void onAudioRtpRtcpTimeout(int i, int i2) throws RemoteException {
        }

        public void onDtmfEvent(int i, int i2) throws RemoteException {
        }

        public void onRecordEvent(int i, int i2) throws RemoteException {
        }

        public void onRecordingStopped(long j, long j2, String str) throws RemoteException {
        }

        public void onRtpLossRate(int i, int i2, float f, float f2, int i3) throws RemoteException {
        }

        public void onRtpStats(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException {
        }

        public void onTextReceive(int i, int i2, String str, int i3, int i4) throws RemoteException {
        }

        public void onTextRtpRtcpTimeout(int i, int i2) throws RemoteException {
        }

        public void onVideoEvent(int i, int i2, int i3, int i4, int i5) throws RemoteException {
        }
    }

    void onAudioInjectionEnded(long j, long j2) throws RemoteException;

    void onAudioRtpRtcpTimeout(int i, int i2) throws RemoteException;

    void onDtmfEvent(int i, int i2) throws RemoteException;

    void onRecordEvent(int i, int i2) throws RemoteException;

    void onRecordingStopped(long j, long j2, String str) throws RemoteException;

    void onRtpLossRate(int i, int i2, float f, float f2, int i3) throws RemoteException;

    void onRtpStats(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    void onTextReceive(int i, int i2, String str, int i3, int i4) throws RemoteException;

    void onTextRtpRtcpTimeout(int i, int i2) throws RemoteException;

    void onVideoEvent(int i, int i2, int i3, int i4, int i5) throws RemoteException;

    public static abstract class Stub extends Binder implements IImsMediaEventListener {
        static final int TRANSACTION_onAudioInjectionEnded = 9;
        static final int TRANSACTION_onAudioRtpRtcpTimeout = 1;
        static final int TRANSACTION_onDtmfEvent = 7;
        static final int TRANSACTION_onRecordEvent = 8;
        static final int TRANSACTION_onRecordingStopped = 10;
        static final int TRANSACTION_onRtpLossRate = 2;
        static final int TRANSACTION_onRtpStats = 3;
        static final int TRANSACTION_onTextReceive = 5;
        static final int TRANSACTION_onTextRtpRtcpTimeout = 6;
        static final int TRANSACTION_onVideoEvent = 4;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IImsMediaEventListener.DESCRIPTOR);
        }

        public static IImsMediaEventListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IImsMediaEventListener.DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IImsMediaEventListener)) {
                return new Proxy(iBinder);
            }
            return (IImsMediaEventListener) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(IImsMediaEventListener.DESCRIPTOR);
            }
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        int readInt = parcel.readInt();
                        int readInt2 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onAudioRtpRtcpTimeout(readInt, readInt2);
                        parcel2.writeNoException();
                        break;
                    case 2:
                        int readInt3 = parcel.readInt();
                        int readInt4 = parcel.readInt();
                        float readFloat = parcel.readFloat();
                        float readFloat2 = parcel.readFloat();
                        int readInt5 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onRtpLossRate(readInt3, readInt4, readFloat, readFloat2, readInt5);
                        parcel2.writeNoException();
                        break;
                    case 3:
                        int readInt6 = parcel.readInt();
                        int readInt7 = parcel.readInt();
                        int readInt8 = parcel.readInt();
                        int readInt9 = parcel.readInt();
                        int readInt10 = parcel.readInt();
                        int readInt11 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onRtpStats(readInt6, readInt7, readInt8, readInt9, readInt10, readInt11);
                        parcel2.writeNoException();
                        break;
                    case 4:
                        int readInt12 = parcel.readInt();
                        int readInt13 = parcel.readInt();
                        int readInt14 = parcel.readInt();
                        int readInt15 = parcel.readInt();
                        int readInt16 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onVideoEvent(readInt12, readInt13, readInt14, readInt15, readInt16);
                        parcel2.writeNoException();
                        break;
                    case 5:
                        int readInt17 = parcel.readInt();
                        int readInt18 = parcel.readInt();
                        String readString = parcel.readString();
                        int readInt19 = parcel.readInt();
                        int readInt20 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onTextReceive(readInt17, readInt18, readString, readInt19, readInt20);
                        parcel2.writeNoException();
                        break;
                    case 6:
                        int readInt21 = parcel.readInt();
                        int readInt22 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onTextRtpRtcpTimeout(readInt21, readInt22);
                        parcel2.writeNoException();
                        break;
                    case 7:
                        int readInt23 = parcel.readInt();
                        int readInt24 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onDtmfEvent(readInt23, readInt24);
                        parcel2.writeNoException();
                        break;
                    case 8:
                        int readInt25 = parcel.readInt();
                        int readInt26 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onRecordEvent(readInt25, readInt26);
                        parcel2.writeNoException();
                        break;
                    case 9:
                        long readLong = parcel.readLong();
                        long readLong2 = parcel.readLong();
                        parcel.enforceNoDataAvail();
                        onAudioInjectionEnded(readLong, readLong2);
                        parcel2.writeNoException();
                        break;
                    case 10:
                        long readLong3 = parcel.readLong();
                        long readLong4 = parcel.readLong();
                        String readString2 = parcel.readString();
                        parcel.enforceNoDataAvail();
                        onRecordingStopped(readLong3, readLong4, readString2);
                        parcel2.writeNoException();
                        break;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
                return true;
            }
            parcel2.writeString(IImsMediaEventListener.DESCRIPTOR);
            return true;
        }

        private static class Proxy implements IImsMediaEventListener {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return IImsMediaEventListener.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onAudioRtpRtcpTimeout(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onRtpLossRate(int i, int i2, float f, float f2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeFloat(f);
                    obtain.writeFloat(f2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onRtpStats(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onVideoEvent(int i, int i2, int i3, int i4, int i5) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onTextReceive(int i, int i2, String str, int i3, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeString(str);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onTextRtpRtcpTimeout(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onDtmfEvent(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onRecordEvent(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onAudioInjectionEnded(long j, long j2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeLong(j);
                    obtain.writeLong(j2);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onRecordingStopped(long j, long j2, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IImsMediaEventListener.DESCRIPTOR);
                    obtain.writeLong(j);
                    obtain.writeLong(j2);
                    obtain.writeString(str);
                    this.mRemote.transact(10, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
