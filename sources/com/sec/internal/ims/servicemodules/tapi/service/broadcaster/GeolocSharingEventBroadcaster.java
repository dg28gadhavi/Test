package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharingListener;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.imscr.LogClass;
import java.util.List;

public class GeolocSharingEventBroadcaster implements IGeolocSharingEventBroadcaster {
    private static final String LOG_TAG = "GeolocSharingEventBroadcaster";
    private Context mContext;
    private final RemoteCallbackList<IGeolocSharingListener> mGeolocSharingListeners = new RemoteCallbackList<>();

    public GeolocSharingEventBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addEventListener(IGeolocSharingListener iGeolocSharingListener) {
        this.mGeolocSharingListeners.register(iGeolocSharingListener);
    }

    public void removeEventListener(IGeolocSharingListener iGeolocSharingListener) {
        this.mGeolocSharingListeners.unregister(iGeolocSharingListener);
    }

    public void broadcastGeolocSharingStateChanged(ContactId contactId, String str, GeolocSharing.State state, GeolocSharing.ReasonCode reasonCode) {
        Log.d(LOG_TAG, " broadcastGeolocSharingStateChanged()");
        int beginBroadcast = this.mGeolocSharingListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mGeolocSharingListeners.getBroadcastItem(i).onStateChanged(contactId, str, state, reasonCode);
            } catch (RemoteException e) {
                if (e.getMessage() != null) {
                    Log.i(LOG_TAG, e.getMessage());
                } else {
                    e.printStackTrace();
                }
            }
        }
        this.mGeolocSharingListeners.finishBroadcast();
    }

    public void broadcastGeolocSharingprogress(ContactId contactId, String str, long j, long j2) {
        Log.d(LOG_TAG, " broadcastGeolocSharingprogress()");
        int beginBroadcast = this.mGeolocSharingListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mGeolocSharingListeners.getBroadcastItem(i).onProgressUpdate(contactId, str, j, j2);
            } catch (RemoteException e) {
                if (e.getMessage() != null) {
                    Log.i(LOG_TAG, e.getMessage());
                } else {
                    e.printStackTrace();
                }
            }
        }
        this.mGeolocSharingListeners.finishBroadcast();
    }

    public void broadcastDeleted(ContactId contactId, List<String> list) {
        Log.d(LOG_TAG, " broadcastDeleted()");
        int beginBroadcast = this.mGeolocSharingListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mGeolocSharingListeners.getBroadcastItem(i).onDeleted(contactId, list);
            } catch (RemoteException e) {
                e.printStackTrace();
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mGeolocSharingListeners.finishBroadcast();
    }

    public void broadcastGeolocSharingInvitation(String str, UserHandle userHandle) {
        Log.d(LOG_TAG, " broadcastGeolocSharingInvitation()");
        Intent intent = new Intent("com.gsma.services.rcs.sharing.geoloc.action.NEW_GEOLOC_SHARING");
        intent.addFlags(LogClass.SIM_EVENT);
        intent.putExtra("sharingId", str);
        IntentUtil.sendBroadcast(this.mContext, intent, userHandle, "com.gsma.services.permission.RCS");
    }
}
