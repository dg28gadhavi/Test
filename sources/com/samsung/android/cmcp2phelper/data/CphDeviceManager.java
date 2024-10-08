package com.samsung.android.cmcp2phelper.data;

import android.os.Handler;
import android.util.Log;
import com.samsung.android.cmcp2phelper.MdmnServiceInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class CphDeviceManager {
    public static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + CphDeviceManager.class.getSimpleName());
    private static ConcurrentHashMap<String, CphMessage> cacheMap = new ConcurrentHashMap<>();
    private static Handler sHandler;
    private static int sMaxPeer;
    private static int sWhat;

    public static synchronized void clearCache() {
        synchronized (CphDeviceManager.class) {
            cacheMap.clear();
        }
    }

    public static void addToCache(CphMessage cphMessage) {
        if (cacheMap.put(cphMessage.getDeviceId(), cphMessage) != null) {
            String str = LOG_TAG;
            Log.d(str, "DeviceId (" + cphMessage.getDeviceId() + ") is already added");
            return;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "New peer is discovered : " + cphMessage.toString() + ", count : " + cacheMap.size());
        if (cacheMap.size() == sMaxPeer) {
            Log.i(str2, "All peers (" + sMaxPeer + ") are discovered");
            notify(true);
        }
    }

    public static Collection<MdmnServiceInfo> getDeviceList(String str) {
        ArrayList arrayList = new ArrayList();
        Log.i(LOG_TAG, "--- P2P reachable peer list ----");
        for (CphMessage next : cacheMap.values()) {
            if (next.getLineId().equalsIgnoreCase(str)) {
                MdmnServiceInfo mdmnServiceInfo = new MdmnServiceInfo(next.getDeviceId(), next.getLineId());
                String str2 = LOG_TAG;
                Log.i(str2, "Reachable peer : " + mdmnServiceInfo.toString());
                arrayList.add(mdmnServiceInfo);
            }
        }
        Log.i(LOG_TAG, "--- end ----");
        return arrayList;
    }

    public static synchronized void setCallback(Handler handler, int i) {
        synchronized (CphDeviceManager.class) {
            sHandler = handler;
            sWhat = i;
            if (handler != null) {
                handler.sendMessageDelayed(handler.obtainMessage(i, 0, 0), 1500);
            }
        }
    }

    public static synchronized void setMaxPeer(int i) {
        synchronized (CphDeviceManager.class) {
            sMaxPeer = i;
        }
    }

    public static synchronized void notify(boolean z) {
        synchronized (CphDeviceManager.class) {
            Handler handler = sHandler;
            if (handler != null) {
                handler.removeCallbacksAndMessages((Object) null);
                if (z) {
                    Handler handler2 = sHandler;
                    handler2.sendMessage(handler2.obtainMessage(sWhat, 1, 0));
                } else {
                    Handler handler3 = sHandler;
                    handler3.sendMessage(handler3.obtainMessage(sWhat, 0, 0));
                }
                sHandler = null;
            }
        }
    }
}
