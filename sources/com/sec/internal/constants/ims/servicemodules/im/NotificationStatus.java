package com.sec.internal.constants.ims.servicemodules.im;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import java.util.HashSet;
import java.util.Set;

public enum NotificationStatus implements IEnumerationWithId<NotificationStatus> {
    NONE(0),
    DELIVERED(1),
    DISPLAYED(2),
    INTERWORKING_SMS(3),
    INTERWORKING_MMS(4),
    CANCELED(5);
    
    private static final String LOG_TAG = null;
    private static final ReverseEnumMap<NotificationStatus> map = null;
    private final int id;

    static {
        Class<NotificationStatus> cls = NotificationStatus.class;
        LOG_TAG = cls.getSimpleName();
        map = new ReverseEnumMap<>(cls);
    }

    private NotificationStatus(int i) {
        this.id = i;
    }

    public int getId() {
        return this.id;
    }

    public NotificationStatus getFromId(int i) {
        return map.get(Integer.valueOf(i));
    }

    public static NotificationStatus fromId(int i) {
        return map.get(Integer.valueOf(i));
    }

    /* renamed from: com.sec.internal.constants.ims.servicemodules.im.NotificationStatus$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus = null;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus[] r0 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus = r0
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.AnonymousClass1.<clinit>():void");
        }
    }

    public static int encode(Set<NotificationStatus> set) {
        int i = 0;
        for (NotificationStatus next : set) {
            int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[next.ordinal()];
            if (i2 == 1 || i2 == 2) {
                i |= next.getId();
            } else {
                Log.e(LOG_TAG, "encode(): unsupported disposition notification!");
            }
        }
        return i;
    }

    public static Set<NotificationStatus> decode(int i) {
        HashSet hashSet = new HashSet();
        NotificationStatus notificationStatus = DELIVERED;
        if ((notificationStatus.getId() & i) != 0) {
            hashSet.add(notificationStatus);
        }
        NotificationStatus notificationStatus2 = DISPLAYED;
        if ((i & notificationStatus2.getId()) != 0) {
            hashSet.add(notificationStatus2);
        }
        return hashSet;
    }

    public static Set<NotificationStatus> toSet(String str) {
        HashSet hashSet = new HashSet();
        String str2 = LOG_TAG;
        Log.e(str2, "toSet(): disposition :" + str);
        if (str == null) {
            hashSet.add(DELIVERED);
            hashSet.add(DISPLAYED);
            return hashSet;
        }
        char c = 65535;
        switch (str.hashCode()) {
            case 3387192:
                if (str.equals(MessageContextValues.none)) {
                    c = 0;
                    break;
                }
                break;
            case 823466996:
                if (str.equals(ATTConstants.ATTDispositionType.DELIVERY)) {
                    c = 1;
                    break;
                }
                break;
            case 1671764162:
                if (str.equals(ATTConstants.ATTDispositionType.DISPLAY)) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                hashSet.add(NONE);
                break;
            case 1:
                hashSet.add(DELIVERED);
                break;
            case 2:
                hashSet.add(DISPLAYED);
                break;
            default:
                hashSet.add(DELIVERED);
                hashSet.add(DISPLAYED);
                break;
        }
        return hashSet;
    }
}
