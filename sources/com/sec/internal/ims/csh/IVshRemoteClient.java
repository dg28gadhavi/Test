package com.sec.internal.ims.csh;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;

public interface IVshRemoteClient extends IInterface {
    public static final String DESCRIPTOR = "com.sec.internal.ims.csh.IVshRemoteClient";

    public static class Default implements IVshRemoteClient {
        public IBinder asBinder() {
            return null;
        }

        public int closeVshSource(long j, Surface surface, boolean z) throws RemoteException {
            return 0;
        }

        public int openVshSource(long j, Surface surface, int i, int i2, int i3, int i4) throws RemoteException {
            return 0;
        }

        public int setOrientationListenerType(int i, int i2) throws RemoteException {
            return 0;
        }
    }

    int closeVshSource(long j, Surface surface, boolean z) throws RemoteException;

    int openVshSource(long j, Surface surface, int i, int i2, int i3, int i4) throws RemoteException;

    int setOrientationListenerType(int i, int i2) throws RemoteException;

    public static abstract class Stub extends Binder implements IVshRemoteClient {
        static final int TRANSACTION_closeVshSource = 2;
        static final int TRANSACTION_openVshSource = 1;
        static final int TRANSACTION_setOrientationListenerType = 3;

        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, IVshRemoteClient.DESCRIPTOR);
        }

        public static IVshRemoteClient asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(IVshRemoteClient.DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IVshRemoteClient)) {
                return new Proxy(iBinder);
            }
            return (IVshRemoteClient) queryLocalInterface;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i >= 1 && i <= 16777215) {
                parcel.enforceInterface(IVshRemoteClient.DESCRIPTOR);
            }
            if (i != 1598968902) {
                if (i == 1) {
                    int readInt = parcel.readInt();
                    int readInt2 = parcel.readInt();
                    int readInt3 = parcel.readInt();
                    int readInt4 = parcel.readInt();
                    parcel.enforceNoDataAvail();
                    int openVshSource = openVshSource(parcel.readLong(), (Surface) parcel.readTypedObject(Surface.CREATOR), readInt, readInt2, readInt3, readInt4);
                    parcel2.writeNoException();
                    parcel2.writeInt(openVshSource);
                } else if (i == 2) {
                    boolean readBoolean = parcel.readBoolean();
                    parcel.enforceNoDataAvail();
                    int closeVshSource = closeVshSource(parcel.readLong(), (Surface) parcel.readTypedObject(Surface.CREATOR), readBoolean);
                    parcel2.writeNoException();
                    parcel2.writeInt(closeVshSource);
                } else if (i != 3) {
                    return super.onTransact(i, parcel, parcel2, i2);
                } else {
                    int readInt5 = parcel.readInt();
                    int readInt6 = parcel.readInt();
                    parcel.enforceNoDataAvail();
                    int orientationListenerType = setOrientationListenerType(readInt5, readInt6);
                    parcel2.writeNoException();
                    parcel2.writeInt(orientationListenerType);
                }
                return true;
            }
            parcel2.writeString(IVshRemoteClient.DESCRIPTOR);
            return true;
        }

        private static class Proxy implements IVshRemoteClient {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return IVshRemoteClient.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int openVshSource(long j, Surface surface, int i, int i2, int i3, int i4) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IVshRemoteClient.DESCRIPTOR);
                    obtain.writeLong(j);
                    obtain.writeTypedObject(surface, 0);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    obtain.writeInt(i4);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int closeVshSource(long j, Surface surface, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IVshRemoteClient.DESCRIPTOR);
                    obtain.writeLong(j);
                    obtain.writeTypedObject(surface, 0);
                    obtain.writeBoolean(z);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int setOrientationListenerType(int i, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain(asBinder());
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(IVshRemoteClient.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
