package com.sec.internal.ims.cmstore.utils;

import android.util.Log;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class NotificationListContainer {
    private static final int MAX_SIZE = 60;
    private static final String TAG = NotificationListContainer.class.getSimpleName();
    private static HashMap<Integer, NotificationListContainer> sInstance = new HashMap<>();
    private TreeMap<Long, NotificationList[]> container = new TreeMap<>();

    private NotificationListContainer() {
    }

    public static NotificationListContainer getInstance(int i) {
        if (sInstance.containsKey(Integer.valueOf(i))) {
            return sInstance.get(Integer.valueOf(i));
        }
        NotificationListContainer notificationListContainer = new NotificationListContainer();
        sInstance.put(Integer.valueOf(i), notificationListContainer);
        return notificationListContainer;
    }

    public synchronized void insertContainer(Long l, NotificationList[] notificationListArr) {
        String str = TAG;
        Log.d(str, "insertContainer, index=" + l + ",container.size()= " + this.container.size());
        if (this.container.size() < 60) {
            this.container.put(l, notificationListArr);
        }
    }

    public synchronized long peekFirstIndex() {
        if (this.container.isEmpty()) {
            return -1;
        }
        long longValue = this.container.firstKey().longValue();
        String str = TAG;
        Log.d(str, "peekFirstIndex, index=" + longValue);
        return longValue;
    }

    public synchronized Map.Entry<Long, NotificationList[]> popFirstEntry() {
        if (this.container.isEmpty()) {
            return null;
        }
        Map.Entry<Long, NotificationList[]> firstEntry = this.container.firstEntry();
        String str = TAG;
        Log.d(str, "popFirstEntry, index=" + firstEntry.getKey());
        this.container.remove(firstEntry.getKey());
        return firstEntry;
    }

    public synchronized boolean isEmpty() {
        return this.container.isEmpty();
    }

    public synchronized void clear() {
        this.container.clear();
    }
}
