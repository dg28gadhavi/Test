package com.sec.internal.ims.servicemodules.options;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.ICapabilityService;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ImsFrameworkState;

public class RcsUriProvider extends ContentProvider {
    private static final String AUTHORITY = "com.sec.ims.android.rcsuriprovider";
    private static final String[] ENABLED_PROJECTION = {"_id", "sip_uri", Columns.IS_ENABLED};
    private static final String LOG_TAG = "RcsUriProvider";
    static final int N_RCSENABLE_URIS = 1;
    static UriMatcher mMatcher;
    Context mContext = null;
    ICapabilityService mService = null;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        mMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "rcsenableduri", 1);
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "onCreate()");
        Context context = getContext();
        this.mContext = context;
        ImsFrameworkState.getInstance(context).registerForFrameworkState(new RcsUriProvider$$ExternalSyntheticLambda0(this));
        return false;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: initCapabilityService */
    public void lambda$onCreate$0() {
        Log.i(LOG_TAG, "Connecting to CapabilityDiscoveryService.");
        Intent intent = new Intent();
        intent.setClassName("com.sec.imsservice", "com.sec.internal.ims.imsservice.CapabilityService");
        ContextExt.bindServiceAsUser(this.mContext, intent, new ServiceConnection() {
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.i(RcsUriProvider.LOG_TAG, "Connected.");
                RcsUriProvider.this.mService = ICapabilityService.Stub.asInterface(iBinder);
            }

            public void onServiceDisconnected(ComponentName componentName) {
                Log.i(RcsUriProvider.LOG_TAG, "Disconnected.");
                RcsUriProvider.this.mService = null;
            }
        }, 1, ContextExt.CURRENT_OR_SELF);
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        if (mMatcher.match(uri) == 1) {
            Log.i(LOG_TAG, "N_RCSENABLE_URIS | Operation for uri: ".concat(uri.toString()));
            MatrixCursor matrixCursor = new MatrixCursor(ENABLED_PROJECTION);
            ICapabilityService iCapabilityService = this.mService;
            if (iCapabilityService == null) {
                Log.e(LOG_TAG, "Binder is not initialized! Returning empty response");
                return matrixCursor;
            }
            try {
                Capabilities[] allCapabilities = iCapabilityService.getAllCapabilities(simSlotFromUri);
                if (allCapabilities == null) {
                    return matrixCursor;
                }
                if (allCapabilities.length == 0) {
                    Log.i(LOG_TAG, "N_RCSENABLE_URIS: not found.");
                    return matrixCursor;
                }
                int length = allCapabilities.length;
                int i = 0;
                int i2 = 1;
                while (i < length) {
                    int i3 = i2 + 1;
                    matrixCursor.addRow(new Object[]{Integer.valueOf(i2), allCapabilities[i].getUri().toString(), 1});
                    i++;
                    i2 = i3;
                }
                return matrixCursor;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(LOG_TAG, "UNDEFINED CATEGORY! | Operation for uri: ".concat(uri.toString()));
            throw new UnsupportedOperationException("Operation not supported for uri: ".concat(uri.toString()));
        }
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("Operation not supported for uri:".concat(uri.toString()));
    }
}
