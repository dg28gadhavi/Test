package com.sec.sve;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICmcMediaEventListener extends IInterface {
    public static final String DESCRIPTOR = "com.sec.sve.ICmcMediaEventListener";

    public static class Default implements ICmcMediaEventListener {
        public IBinder asBinder() {
            return null;
        }

        public void onCmcRecordEvent(int i, int i2, int i3) throws RemoteException {
        }

        public void onCmcRecorderStoppedEvent(int i, int i2, String str) throws RemoteException {
        }

        public void onRelayChannelEvent(int i, int i2) throws RemoteException {
        }

        public void onRelayEvent(int i, int i2) throws RemoteException {
        }

        public void onRelayRtpStats(int i, int i2, int i3, int i4, int i5, int i6, int i7) throws RemoteException {
        }

        public void onRelayStreamEvent(int i, int i2, int i3) throws RemoteException {
        }
    }

    void onCmcRecordEvent(int i, int i2, int i3) throws RemoteException;

    void onCmcRecorderStoppedEvent(int i, int i2, String str) throws RemoteException;

    void onRelayChannelEvent(int i, int i2) throws RemoteException;

    void onRelayEvent(int i, int i2) throws RemoteException;

    void onRelayRtpStats(int i, int i2, int i3, int i4, int i5, int i6, int i7) throws RemoteException;

    void onRelayStreamEvent(int i, int i2, int i3) throws RemoteException;

    public static abstract class Stub extends Binder implements ICmcMediaEventListener {
        static final int TRANSACTION_onCmcRecordEvent = 5;
        static final int TRANSACTION_onCmcRecorderStoppedEvent = 6;
        static final int TRANSACTION_onRelayChannelEvent = 4;
        static final int TRANSACTION_onRelayEvent = 1;
        static final int TRANSACTION_onRelayRtpStats = 3;
        static final int TRANSACTION_onRelayStreamEvent = 2;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, ICmcMediaEventListener.DESCRIPTOR);
        }

        public static ICmcMediaEventListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(ICmcMediaEventListener.DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof ICmcMediaEventListener)) {
                return new Proxy(iBinder);
            }
            return (ICmcMediaEventListener) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(ICmcMediaEventListener.DESCRIPTOR);
            }
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        int readInt = parcel.readInt();
                        int readInt2 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onRelayEvent(readInt, readInt2);
                        parcel2.writeNoException();
                        break;
                    case 2:
                        int readInt3 = parcel.readInt();
                        int readInt4 = parcel.readInt();
                        int readInt5 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onRelayStreamEvent(readInt3, readInt4, readInt5);
                        parcel2.writeNoException();
                        break;
                    case 3:
                        int readInt6 = parcel.readInt();
                        int readInt7 = parcel.readInt();
                        int readInt8 = parcel.readInt();
                        int readInt9 = parcel.readInt();
                        int readInt10 = parcel.readInt();
                        int readInt11 = parcel.readInt();
                        int readInt12 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onRelayRtpStats(readInt6, readInt7, readInt8, readInt9, readInt10, readInt11, readInt12);
                        parcel2.writeNoException();
                        break;
                    case 4:
                        int readInt13 = parcel.readInt();
                        int readInt14 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onRelayChannelEvent(readInt13, readInt14);
                        parcel2.writeNoException();
                        break;
                    case 5:
                        int readInt15 = parcel.readInt();
                        int readInt16 = parcel.readInt();
                        int readInt17 = parcel.readInt();
                        parcel.enforceNoDataAvail();
                        onCmcRecordEvent(readInt15, readInt16, readInt17);
                        parcel2.writeNoException();
                        break;
                    case 6:
                        int readInt18 = parcel.readInt();
                        int readInt19 = parcel.readInt();
                        String readString = parcel.readString();
                        parcel.enforceNoDataAvail();
                        onCmcRecorderStoppedEvent(readInt18, readInt19, readString);
                        parcel2.writeNoException();
                        break;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
                return true;
            }
            parcel2.writeString(ICmcMediaEventListener.DESCRIPTOR);
            return true;
        }

        private static class Proxy implements ICmcMediaEventListener {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return ICmcMediaEventListener.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onRelayEvent(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ICmcMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onRelayStreamEvent(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ICmcMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onRelayRtpStats(int i, int i2, int i3, int i4, int i5, int i6, int i7) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ICmcMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    obtain.writeInt(i5);
                    obtain.writeInt(i6);
                    obtain.writeInt(i7);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onRelayChannelEvent(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ICmcMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onCmcRecordEvent(int i, int i2, int i3) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ICmcMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void onCmcRecorderStoppedEvent(int i, int i2, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(ICmcMediaEventListener.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeString(str);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
