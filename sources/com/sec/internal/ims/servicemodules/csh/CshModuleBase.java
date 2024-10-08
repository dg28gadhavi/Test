package com.sec.internal.ims.servicemodules.csh;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.csh.ICshModule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CshModuleBase extends ServiceModuleBase implements ICshModule {
    private static String LOG_TAG = CshModuleBase.class.getSimpleName();
    protected Map<Integer, List<ICall>> mActiveCallLists = new HashMap();
    protected int mActiveCallPhoneId = 0;
    protected CshCache mCache;
    protected Context mContext;
    protected boolean mIsDuringMultipartyCall = false;
    protected boolean mIsServiceReady = false;
    protected int mNPrevConnectedCalls = 0;
    protected Capabilities mRemoteCapabilities = new Capabilities();

    protected CshModuleBase(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public void putSession(IContentShare iContentShare) {
        this.mCache.putSession(iContentShare);
    }

    public void deleteSession(int i) {
        this.mCache.deleteSession(i);
    }

    public IContentShare getSession(long j) {
        if (j < 0) {
            return null;
        }
        return this.mCache.getSession(j);
    }

    public void notifyContentChange(IContentShare iContentShare) {
        String str = LOG_TAG;
        Log.d(str, "Update share [" + iContentShare.getContent() + "]");
        this.mContext.getContentResolver().notifyChange(ICshConstants.ShareDatabase.ACTIVE_SESSIONS_URI, (ContentObserver) null);
    }

    public void onCallStateChanged(int i, List<ICall> list) {
        processCallStateChanged(i, new CopyOnWriteArrayList(list));
    }

    private int countConnectedCall(List<ICall> list) {
        int i = 0;
        for (ICall isConnected : list) {
            if (isConnected.isConnected()) {
                i++;
            }
        }
        return i;
    }

    private void processCallStateChanged(int i, CopyOnWriteArrayList<ICall> copyOnWriteArrayList) {
        int countConnectedCall = countConnectedCall(copyOnWriteArrayList);
        List arrayList = new ArrayList();
        if (this.mActiveCallLists.containsKey(Integer.valueOf(i))) {
            arrayList = this.mActiveCallLists.get(Integer.valueOf(i));
        }
        int countConnectedCall2 = countConnectedCall(arrayList);
        Iterator<ICall> it = copyOnWriteArrayList.iterator();
        while (it.hasNext()) {
            ICall next = it.next();
            ICall call = getCall(arrayList, next.getNumber());
            if (call == null) {
                if (next.isConnected() && countConnectedCall == 1) {
                    this.mActiveCallPhoneId = i;
                    this.mIsDuringMultipartyCall = false;
                    this.mCache.init();
                } else if (countConnectedCall > 1) {
                    this.mIsDuringMultipartyCall = true;
                }
            } else if ((!call.isConnected() || countConnectedCall2 > 1) && next.isConnected() && countConnectedCall == 1) {
                this.mActiveCallPhoneId = i;
                this.mIsDuringMultipartyCall = false;
            } else if ((call.isConnected() && countConnectedCall2 == 1 && (!next.isConnected() || countConnectedCall > 1)) || (!call.isConnected() && next.isConnected() && countConnectedCall > 1)) {
                this.mIsDuringMultipartyCall = true;
            }
            arrayList.remove(call);
        }
        this.mActiveCallLists.put(Integer.valueOf(i), copyOnWriteArrayList);
        this.mNPrevConnectedCalls = countConnectedCall;
        updateServiceStatus(i);
    }

    private ICall getCall(List<ICall> list, String str) {
        for (ICall next : list) {
            if (TextUtils.equals(next.getNumber(), str)) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void updateServiceStatus(int i) {
        String str = LOG_TAG;
        Log.d(str, "updateServiceStatus: mIsServiceReady=" + this.mIsServiceReady + ", mEnabledFeatures=" + this.mEnabledFeatures[i] + ", mRemoteCapabilities=" + this.mRemoteCapabilities.getFeature() + ", mIsDuringMultipartyCall=" + this.mIsDuringMultipartyCall);
    }

    public void onRemoteCapabilitiesChanged(Capabilities capabilities) {
        this.mRemoteCapabilities = capabilities;
        updateServiceStatus(this.mActiveDataPhoneId);
    }
}
