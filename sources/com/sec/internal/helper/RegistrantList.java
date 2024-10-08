package com.sec.internal.helper;

import android.os.Handler;
import java.util.ArrayList;
import java.util.Iterator;

public class RegistrantList {
    ArrayList<Registrant> registrants = new ArrayList<>();

    public synchronized void add(Handler handler, int i, Object obj) {
        add(new Registrant(handler, i, obj));
    }

    public synchronized void addUnique(Handler handler, int i, Object obj) {
        remove(handler);
        add(new Registrant(handler, i, obj));
    }

    public synchronized void add(Registrant registrant) {
        removeCleared();
        this.registrants.add(registrant);
    }

    public synchronized void removeCleared() {
        for (int size = this.registrants.size() - 1; size >= 0; size--) {
            if (this.registrants.get(size).refH == null) {
                this.registrants.remove(size);
            }
        }
    }

    public synchronized int size() {
        return this.registrants.size();
    }

    private synchronized void internalNotifyRegistrants(Object obj, Throwable th) {
        int size = this.registrants.size();
        for (int i = 0; i < size; i++) {
            this.registrants.get(i).internalNotifyRegistrant(obj, th);
        }
    }

    public void notifyRegistrants() {
        internalNotifyRegistrants((Object) null, (Throwable) null);
    }

    public void notifyResult(Object obj) {
        internalNotifyRegistrants(obj, (Throwable) null);
    }

    public void notifyRegistrants(AsyncResult asyncResult) {
        internalNotifyRegistrants(asyncResult.result, asyncResult.exception);
    }

    public synchronized void remove(Handler handler) {
        int size = this.registrants.size();
        for (int i = 0; i < size; i++) {
            Registrant registrant = this.registrants.get(i);
            Handler handler2 = registrant.getHandler();
            if (handler2 == null || handler2 == handler) {
                registrant.clear();
            }
        }
        removeCleared();
    }

    public int find(Handler handler) {
        Iterator<Registrant> it = this.registrants.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().getHandler() == handler) {
                i++;
            }
        }
        return i;
    }
}
