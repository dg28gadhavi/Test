package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharingLog;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.im.ImCache;

public class GeolocProvider extends ContentProvider {
    private static final String AUTHORITY;
    private static final String LOG_TAG = GeolocProvider.class.getSimpleName();
    private static final int RCSAPI = 1;
    private static final int RCSAPI_ID = 2;
    private static final UriMatcher sUriMatcher;
    private final String[] MESSAGE_COLUMNS = {"_id", "sharing_id", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "content", "mime_type", "direction", "timestamp", "state", "reason_code"};
    private ImCache mCache;

    public String getType(Uri uri) {
        return null;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        String authority = GeolocSharingLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        uriMatcher.addURI(authority, "geolocshare", 1);
        uriMatcher.addURI(authority, "geolocshare/#", 2);
    }

    public int delete(Uri uri, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        this.mCache = ImCache.getInstance();
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (!this.mCache.isLoaded()) {
            Log.e(LOG_TAG, "ImCache is not ready yet.");
            return null;
        }
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            return buildMessagesCursor();
        }
        if (match == 2) {
            return buildMessagesCursor(uri);
        }
        Log.d(LOG_TAG, "return null");
        return null;
    }

    private Cursor buildMessagesCursor() {
        MatrixCursor matrixCursor = new MatrixCursor(this.MESSAGE_COLUMNS);
        fillMessageCursor(matrixCursor, (String) null);
        return matrixCursor;
    }

    private Cursor buildMessagesCursor(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        MatrixCursor matrixCursor = new MatrixCursor(this.MESSAGE_COLUMNS);
        fillMessageCursor(matrixCursor, lastPathSegment);
        if (matrixCursor.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        }
        return matrixCursor;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x0107  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void fillMessageCursor(android.database.MatrixCursor r11, java.lang.String r12) {
        /*
            r10 = this;
            java.lang.String r0 = "_id"
            java.lang.String r1 = "chat_id"
            java.lang.String r2 = "remote_uri"
            java.lang.String r3 = "content_type"
            java.lang.String r4 = "direction"
            java.lang.String r5 = "ext_info"
            java.lang.String r6 = "reason"
            java.lang.String r7 = "delivered_timestamp"
            java.lang.String r8 = "state"
            java.lang.String[] r0 = new java.lang.String[]{r0, r1, r2, r3, r4, r5, r6, r7, r8}
            java.lang.String r1 = "_id= ? "
            java.lang.String[] r2 = new java.lang.String[]{r12}
            r3 = 0
            if (r12 != 0) goto L_0x0029
            com.sec.internal.ims.servicemodules.im.ImCache r12 = r10.mCache     // Catch:{ all -> 0x0104 }
            android.database.Cursor r12 = r12.queryMessages(r0, r3, r3, r3)     // Catch:{ all -> 0x0104 }
            goto L_0x002f
        L_0x0029:
            com.sec.internal.ims.servicemodules.im.ImCache r12 = r10.mCache     // Catch:{ all -> 0x0104 }
            android.database.Cursor r12 = r12.queryMessages(r0, r1, r2, r3)     // Catch:{ all -> 0x0104 }
        L_0x002f:
            if (r12 == 0) goto L_0x00f4
            int r0 = r12.getCount()     // Catch:{ all -> 0x0101 }
            if (r0 != 0) goto L_0x0039
            goto L_0x00f4
        L_0x0039:
            r0 = 0
            r1 = r0
        L_0x003b:
            boolean r2 = r12.moveToNext()     // Catch:{ all -> 0x0101 }
            if (r2 == 0) goto L_0x00f0
            java.lang.String r2 = "content_type"
            int r2 = r12.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0101 }
            java.lang.String r2 = r12.getString(r2)     // Catch:{ all -> 0x0101 }
            java.lang.String r4 = "ext_info"
            int r4 = r12.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x0101 }
            java.lang.String r4 = r12.getString(r4)     // Catch:{ all -> 0x0101 }
            java.lang.String r5 = "remote_uri"
            int r5 = r12.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x0101 }
            java.lang.String r5 = r12.getString(r5)     // Catch:{ all -> 0x0101 }
            java.lang.String r6 = "direction"
            int r6 = r12.getColumnIndexOrThrow(r6)     // Catch:{ all -> 0x0101 }
            int r6 = r12.getInt(r6)     // Catch:{ all -> 0x0101 }
            if (r2 == 0) goto L_0x003b
            java.lang.String r7 = "application/vnd.gsma.rcspushlocation+xml"
            boolean r7 = r2.equals(r7)     // Catch:{ all -> 0x0101 }
            if (r7 == 0) goto L_0x003b
            if (r4 == 0) goto L_0x003b
            r7 = 9
            java.lang.Object[] r7 = new java.lang.Object[r7]     // Catch:{ all -> 0x0101 }
            int r8 = r1 + 1
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0101 }
            r7[r0] = r1     // Catch:{ all -> 0x0101 }
            java.lang.String r1 = "_id"
            int r1 = r12.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0101 }
            java.lang.String r1 = r12.getString(r1)     // Catch:{ all -> 0x0101 }
            java.lang.String r1 = java.lang.String.valueOf(r1)     // Catch:{ all -> 0x0101 }
            r9 = 1
            r7[r9] = r1     // Catch:{ all -> 0x0101 }
            if (r5 == 0) goto L_0x0096
            goto L_0x0097
        L_0x0096:
            r5 = r3
        L_0x0097:
            java.lang.String r1 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r5)     // Catch:{ all -> 0x0101 }
            r5 = 2
            r7[r5] = r1     // Catch:{ all -> 0x0101 }
            r1 = 3
            r7[r1] = r2     // Catch:{ all -> 0x0101 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x0101 }
            r2 = 4
            r7[r2] = r1     // Catch:{ all -> 0x0101 }
            r1 = 5
            r7[r1] = r4     // Catch:{ all -> 0x0101 }
            java.lang.String r1 = "reason"
            int r1 = r12.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0101 }
            int r1 = r12.getInt(r1)     // Catch:{ all -> 0x0101 }
            int r1 = r10.transReason(r1)     // Catch:{ all -> 0x0101 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0101 }
            r2 = 6
            r7[r2] = r1     // Catch:{ all -> 0x0101 }
            java.lang.String r1 = "delivered_timestamp"
            int r1 = r12.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0101 }
            int r1 = r12.getInt(r1)     // Catch:{ all -> 0x0101 }
            long r1 = (long) r1     // Catch:{ all -> 0x0101 }
            java.lang.Long r1 = java.lang.Long.valueOf(r1)     // Catch:{ all -> 0x0101 }
            r2 = 7
            r7[r2] = r1     // Catch:{ all -> 0x0101 }
            java.lang.String r1 = "state"
            int r1 = r12.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0101 }
            int r1 = r12.getInt(r1)     // Catch:{ all -> 0x0101 }
            int r1 = r10.transState(r1, r6)     // Catch:{ all -> 0x0101 }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x0101 }
            r2 = 8
            r7[r2] = r1     // Catch:{ all -> 0x0101 }
            r11.addRow(r7)     // Catch:{ all -> 0x0101 }
            r1 = r8
            goto L_0x003b
        L_0x00f0:
            r12.close()
            return
        L_0x00f4:
            java.lang.String r10 = LOG_TAG     // Catch:{ all -> 0x0101 }
            java.lang.String r11 = "buildMessageCursor: Message not found."
            android.util.Log.e(r10, r11)     // Catch:{ all -> 0x0101 }
            if (r12 == 0) goto L_0x0100
            r12.close()
        L_0x0100:
            return
        L_0x0101:
            r10 = move-exception
            r3 = r12
            goto L_0x0105
        L_0x0104:
            r10 = move-exception
        L_0x0105:
            if (r3 == 0) goto L_0x010a
            r3.close()
        L_0x010a:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.provider.GeolocProvider.fillMessageCursor(android.database.MatrixCursor, java.lang.String):void");
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }

    private int transState(int i, int i2) {
        int ordinal = GeolocSharing.State.INVITED.ordinal();
        if (!(i == 0 || i == 1)) {
            if (i == 2) {
                return GeolocSharing.State.STARTED.ordinal();
            }
            if (i == 3) {
                return GeolocSharing.State.RINGING.ordinal();
            }
            if (i != 4) {
                if (i != 6) {
                    if (i != 7) {
                        return ordinal;
                    }
                }
            }
            return GeolocSharing.State.ABORTED.ordinal();
        }
        if (ImDirection.INCOMING.getId() == i2) {
            return GeolocSharing.State.INVITED.ordinal();
        }
        return ImDirection.OUTGOING.getId() == i2 ? GeolocSharing.State.INITIATING.ordinal() : ordinal;
    }

    private int transReason(int i) {
        CancelReason valueOf = CancelReason.valueOf(i);
        if (valueOf == null) {
            return GeolocSharing.ReasonCode.UNSPECIFIED.ordinal();
        }
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[valueOf.ordinal()]) {
            case 1:
                return GeolocSharing.ReasonCode.ABORTED_BY_USER.ordinal();
            case 2:
                return GeolocSharing.ReasonCode.ABORTED_BY_REMOTE.ordinal();
            case 3:
                return GeolocSharing.ReasonCode.ABORTED_BY_SYSTEM.ordinal();
            case 4:
                return GeolocSharing.ReasonCode.REJECTED_BY_REMOTE.ordinal();
            case 5:
                return GeolocSharing.ReasonCode.FAILED_SHARING.ordinal();
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                return GeolocSharing.ReasonCode.FAILED_INITIATION.ordinal();
            case 17:
            case 18:
            case 19:
            case 20:
                return GeolocSharing.ReasonCode.FAILED_SHARING.ordinal();
            default:
                return 0;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.provider.GeolocProvider$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason;

        /* JADX WARNING: Can't wrap try/catch for region: R(42:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|42) */
        /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00b4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x00c0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00cc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00d8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00e4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason[] r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason = r0
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_USER     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_REMOTE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_SYSTEM     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REJECTED_BY_REMOTE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.TIME_OUT     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.TOO_LARGE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.NOT_AUTHORIZED     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CONNECTION_RELEASED     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CONTENT_REACHED_DOWNSIZE     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.DEVICE_UNREGISTERED     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.FORBIDDEN_NO_RETRY_FALLBACK     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.INVALID_REQUEST     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.LOCALLY_ABORTED     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.LOW_MEMORY     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.NO_RESPONSE     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_BLOCKED     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_USER_INVALID     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.VALIDITY_EXPIRED     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.provider.GeolocProvider.AnonymousClass1.<clinit>():void");
        }
    }
}
