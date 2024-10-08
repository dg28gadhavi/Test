package com.sec.internal.ims.core.imslogger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class ImsDiagnosticMonitorNotifier implements ISignallingNotifier {
    private static final int MAX_PENDING_QUEUE = 10;
    protected String LOG_TAG = "";
    protected boolean mAllowPending;
    protected final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(ImsDiagnosticMonitorNotifier.this.LOG_TAG, "onServiceConnected()");
            ImsDiagnosticMonitorNotifier imsDiagnosticMonitorNotifier = ImsDiagnosticMonitorNotifier.this;
            imsDiagnosticMonitorNotifier.mDmBinder = iBinder;
            imsDiagnosticMonitorNotifier.mPackageStatus = ISignallingNotifier.PackageStatus.DM_CONNECTED;
            imsDiagnosticMonitorNotifier.mIsBound = true;
            imsDiagnosticMonitorNotifier.sendPendingObject();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(ImsDiagnosticMonitorNotifier.this.LOG_TAG, "onServiceDisconnected()");
            ImsDiagnosticMonitorNotifier imsDiagnosticMonitorNotifier = ImsDiagnosticMonitorNotifier.this;
            imsDiagnosticMonitorNotifier.mPackageStatus = ISignallingNotifier.PackageStatus.DM_DISCONNECTED;
            imsDiagnosticMonitorNotifier.mPendingQueue.clear();
            ImsDiagnosticMonitorNotifier.this.disconnectService();
        }
    };
    protected Context mContext;
    protected IBinder mDmBinder;
    protected String mDmSreviceName;
    protected boolean mIsBound = false;
    protected String mPackageName;
    protected ISignallingNotifier.PackageStatus mPackageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
    /* access modifiers changed from: private */
    public BlockingQueue<Object> mPendingQueue = new LinkedBlockingQueue();

    public ImsDiagnosticMonitorNotifier(Context context, String str, String str2, boolean z) {
        this.mContext = context;
        this.mPackageName = str;
        this.mDmSreviceName = str2;
        this.mAllowPending = z;
    }

    public ISignallingNotifier.PackageStatus checkPackageStatus() {
        this.mPackageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
        Context context = this.mContext;
        if (context != null) {
            try {
                context.getPackageManager().getServiceInfo(new ComponentName(this.mPackageName, this.mDmSreviceName), 128);
                this.mPackageStatus = ISignallingNotifier.PackageStatus.DM_DISCONNECTED;
                if (checkBinderAvailable()) {
                    this.mPackageStatus = ISignallingNotifier.PackageStatus.DM_CONNECTED;
                }
            } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                String str = this.LOG_TAG;
                Log.i(str, "checkPackageStatus() : " + e);
                return this.mPackageStatus;
            }
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "checkPackageStatus(): " + this.mPackageStatus);
        return this.mPackageStatus;
    }

    /* access modifiers changed from: protected */
    public boolean checkBinderAvailable() {
        return this.mDmBinder != null && this.mIsBound;
    }

    /* access modifiers changed from: protected */
    public boolean allowedDmEvent() {
        if (Debug.ALLOW_DIAGNOSTICS || DeviceUtil.isOtpAuthorized()) {
            return true;
        }
        disconnectService();
        return false;
    }

    public void initializeDmEvent() {
        if (allowedDmEvent()) {
            Log.i(this.LOG_TAG, "connectService");
            connectService();
        }
    }

    /* access modifiers changed from: protected */
    public void connectService() {
        if (this.mPackageStatus == ISignallingNotifier.PackageStatus.DM_DISCONNECTED && !this.mIsBound) {
            this.mContext.bindService(new Intent().setClassName(this.mPackageName, this.mDmSreviceName), this.mConnection, 1);
        }
    }

    /* access modifiers changed from: protected */
    public void disconnectService() {
        if (checkBinderAvailable()) {
            try {
                this.mContext.unbindService(this.mConnection);
            } catch (IllegalArgumentException unused) {
            }
            this.mDmBinder = null;
            this.mIsBound = false;
        }
    }

    private void addPendingObject(Object obj) {
        if (this.mAllowPending) {
            String str = this.LOG_TAG;
            Log.i(str, "addPendingObject size:" + this.mPendingQueue.size());
            if (this.mPendingQueue.size() > 10) {
                this.mPendingQueue.poll();
            }
            this.mPendingQueue.add(obj);
        }
    }

    /* access modifiers changed from: private */
    public void sendPendingObject() {
        if (this.mAllowPending) {
            while (!this.mPendingQueue.isEmpty() && this.mDmBinder != null) {
                Object peek = this.mPendingQueue.peek();
                if (peek != null && send(peek)) {
                    this.mPendingQueue.poll();
                    Log.i(this.LOG_TAG, "succeed send pending requests");
                }
            }
        }
    }

    private boolean sendDmNotification(Object obj) {
        Parcel obtain = Parcel.obtain();
        try {
            boolean z = !(obj instanceof Bundle);
            obtain.writeValue(obj);
            IBinder iBinder = this.mDmBinder;
            if (iBinder != null) {
                iBinder.transact(z ? 1 : 0, obtain, (Parcel) null, 0);
                obtain.recycle();
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            this.mDmBinder = null;
            this.mIsBound = false;
        } catch (Throwable th) {
            obtain.recycle();
            throw th;
        }
        obtain.recycle();
        return false;
    }

    public boolean send(Object obj) {
        ISignallingNotifier.PackageStatus packageStatus;
        if (!allowedDmEvent() || (packageStatus = this.mPackageStatus) == ISignallingNotifier.PackageStatus.NOT_INSTALLED || packageStatus == ISignallingNotifier.PackageStatus.EMERGENCY_MODE) {
            return false;
        }
        String str = this.LOG_TAG;
        Log.i(str, "send() with " + obj.getClass().getSimpleName() + " status: " + this.mPackageStatus);
        ISignallingNotifier.PackageStatus packageStatus2 = this.mPackageStatus;
        if (packageStatus2 == ISignallingNotifier.PackageStatus.DM_CONNECTED) {
            return sendDmNotification(obj);
        }
        if (packageStatus2 != ISignallingNotifier.PackageStatus.DM_DISCONNECTED) {
            return false;
        }
        addPendingObject(obj);
        return false;
    }
}
