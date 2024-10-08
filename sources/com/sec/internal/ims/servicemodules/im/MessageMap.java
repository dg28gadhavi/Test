package com.sec.internal.ims.servicemodules.im;

import android.text.TextUtils;
import android.util.Pair;
import android.util.SparseArray;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageMap {
    private final Map<String, Map<Integer, MessageBase>> mChatIdMap = new HashMap();
    private final Map<String, Map<Pair<String, ImDirection>, MessageBase>> mChatImdnIdMap = new HashMap();
    private final SparseArray<MessageBase> mIdMap = new SparseArray<>();
    private final Map<Pair<String, ImDirection>, MessageBase> mImdnIdMap = new HashMap();

    public boolean containsKey(int i) {
        boolean z;
        synchronized (this.mIdMap) {
            z = this.mIdMap.indexOfKey(i) >= 0;
        }
        return z;
    }

    public MessageBase get(int i) {
        MessageBase messageBase;
        synchronized (this.mIdMap) {
            messageBase = i >= 0 ? this.mIdMap.get(i) : null;
        }
        return messageBase;
    }

    public MessageBase get(String str, ImDirection imDirection) {
        MessageBase messageBase;
        synchronized (this.mIdMap) {
            if (TextUtils.isEmpty(str) || imDirection == null) {
                messageBase = null;
            } else {
                messageBase = this.mImdnIdMap.get(new Pair(str, imDirection));
            }
        }
        return messageBase;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
        r2 = r2.mChatImdnIdMap.get(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002f, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.MessageBase get(java.lang.String r3, com.sec.internal.constants.ims.servicemodules.im.ImDirection r4, java.lang.String r5) {
        /*
            r2 = this;
            android.util.SparseArray<com.sec.internal.ims.servicemodules.im.MessageBase> r0 = r2.mIdMap
            monitor-enter(r0)
            boolean r1 = android.text.TextUtils.isEmpty(r5)     // Catch:{ all -> 0x0030 }
            if (r1 == 0) goto L_0x000f
            com.sec.internal.ims.servicemodules.im.MessageBase r2 = r2.get(r3, r4)     // Catch:{ all -> 0x0030 }
            monitor-exit(r0)     // Catch:{ all -> 0x0030 }
            return r2
        L_0x000f:
            boolean r1 = android.text.TextUtils.isEmpty(r3)     // Catch:{ all -> 0x0030 }
            if (r1 != 0) goto L_0x002d
            if (r4 == 0) goto L_0x002d
            java.util.Map<java.lang.String, java.util.Map<android.util.Pair<java.lang.String, com.sec.internal.constants.ims.servicemodules.im.ImDirection>, com.sec.internal.ims.servicemodules.im.MessageBase>> r2 = r2.mChatImdnIdMap     // Catch:{ all -> 0x0030 }
            java.lang.Object r2 = r2.get(r5)     // Catch:{ all -> 0x0030 }
            java.util.Map r2 = (java.util.Map) r2     // Catch:{ all -> 0x0030 }
            if (r2 == 0) goto L_0x002d
            android.util.Pair r5 = new android.util.Pair     // Catch:{ all -> 0x0030 }
            r5.<init>(r3, r4)     // Catch:{ all -> 0x0030 }
            java.lang.Object r2 = r2.get(r5)     // Catch:{ all -> 0x0030 }
            com.sec.internal.ims.servicemodules.im.MessageBase r2 = (com.sec.internal.ims.servicemodules.im.MessageBase) r2     // Catch:{ all -> 0x0030 }
            goto L_0x002e
        L_0x002d:
            r2 = 0
        L_0x002e:
            monitor-exit(r0)     // Catch:{ all -> 0x0030 }
            return r2
        L_0x0030:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0030 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.MessageMap.get(java.lang.String, com.sec.internal.constants.ims.servicemodules.im.ImDirection, java.lang.String):com.sec.internal.ims.servicemodules.im.MessageBase");
    }

    public List<MessageBase> getAll() {
        ArrayList arrayList;
        synchronized (this.mIdMap) {
            arrayList = new ArrayList(this.mIdMap.size());
            for (int i = 0; i < this.mIdMap.size(); i++) {
                arrayList.add(this.mIdMap.valueAt(i));
            }
        }
        return arrayList;
    }

    public List<MessageBase> getAll(String str) {
        ArrayList arrayList;
        Map map;
        synchronized (this.mIdMap) {
            arrayList = new ArrayList();
            if (!TextUtils.isEmpty(str) && (map = this.mChatIdMap.get(str)) != null) {
                arrayList.addAll(map.values());
            }
        }
        return arrayList;
    }

    public void put(MessageBase messageBase) {
        synchronized (this.mIdMap) {
            if (messageBase != null) {
                if (messageBase.getId() > 0) {
                    this.mIdMap.put(messageBase.getId(), messageBase);
                }
                Pair pair = (TextUtils.isEmpty(messageBase.getImdnId()) || messageBase.getDirection() == null) ? null : new Pair(messageBase.getImdnId(), messageBase.getDirection());
                if (pair != null) {
                    this.mImdnIdMap.put(pair, messageBase);
                }
                if (!TextUtils.isEmpty(messageBase.getChatId())) {
                    if (messageBase.getId() > 0) {
                        this.mChatIdMap.computeIfAbsent(messageBase.getChatId(), new MessageMap$$ExternalSyntheticLambda0()).put(Integer.valueOf(messageBase.getId()), messageBase);
                    }
                    if (pair != null) {
                        this.mChatImdnIdMap.computeIfAbsent(messageBase.getChatId(), new MessageMap$$ExternalSyntheticLambda1()).put(pair, messageBase);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Map lambda$put$0(String str) {
        return new HashMap();
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Map lambda$put$1(String str) {
        return new HashMap();
    }

    public void remove(int i) {
        synchronized (this.mIdMap) {
            if (i > 0) {
                MessageBase messageBase = this.mIdMap.get(i);
                this.mIdMap.delete(i);
                if (messageBase != null) {
                    Pair pair = (TextUtils.isEmpty(messageBase.getImdnId()) || messageBase.getDirection() == null) ? null : new Pair(messageBase.getImdnId(), messageBase.getDirection());
                    if (pair != null) {
                        this.mImdnIdMap.remove(pair);
                    }
                    if (!TextUtils.isEmpty(messageBase.getChatId())) {
                        Map map = this.mChatIdMap.get(messageBase.getChatId());
                        if (map != null) {
                            map.remove(Integer.valueOf(i));
                        }
                        Map map2 = this.mChatImdnIdMap.get(messageBase.getChatId());
                        if (map2 != null) {
                            map2.remove(pair);
                        }
                    }
                }
            }
        }
    }
}
