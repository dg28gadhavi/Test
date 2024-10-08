package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.SystemWrapper;
import com.sec.internal.ims.core.RegistrationGovernor;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.Contact;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ContactCache {
    private static final int DELAY_REFRESH_COUNT = 300;
    private static final int DELAY_REFRESH_TIME = 300;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "ContactCache";
    private static final int MAX_COUNT = 1000;
    protected Handler mBackgroundHandler = null;
    private final List<CapabilitiesCache> mCapabilitiesCacheList = new CopyOnWriteArrayList();
    protected ContactCacheHandler mContactCacheHandler = null;
    protected int mContactCurCount = 0;
    protected final Map<String, Contact> mContactList = new ConcurrentHashMap();
    Context mContext;
    protected String mCountryCode;
    private SimpleEventLog mEventLog;
    protected boolean mIsBlockedContactChange = false;
    protected boolean mIsBlockedInitialContactSyncBeforeRegi = false;
    private boolean mIsContactUpdated = false;
    protected boolean mIsLimiting = false;
    protected boolean mIsThrottle = false;
    protected String mLastRawId = null;
    private Map<Integer, Long> mLastRefreshTimeInMs = new HashMap();
    private final List<ContactEventListener> mListeners = new CopyOnWriteArrayList();
    private Mno mMno = Mno.DEFAULT;
    protected ContactObserver mObserver = null;
    private Map<Integer, Long> mPrevRefreshTimeInMs = new HashMap();
    private final List<String> mRemovedNumbers = new CopyOnWriteArrayList();
    final AtomicBoolean mResyncRequired = new AtomicBoolean();
    private int mStartIndex = 0;
    final AtomicBoolean mSyncInProgress = new AtomicBoolean();
    private UriGenerator mUriGenerator;
    private int mUserId = 0;

    public interface ContactEventListener {
        void onChanged();
    }

    private Uri getRemoteUriwithUserId(Uri uri) {
        return uri;
    }

    public ContactCache(Context context, Map<Integer, CapabilitiesCache> map) {
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 20);
        HandlerThread handlerThread = new HandlerThread("ContactCacheHandler", 10);
        handlerThread.start();
        this.mContactCacheHandler = new ContactCacheHandler(handlerThread.getLooper());
        for (Map.Entry<Integer, CapabilitiesCache> key : map.entrySet()) {
            Integer num = (Integer) key.getKey();
            this.mCapabilitiesCacheList.add(map.get(num));
            this.mLastRefreshTimeInMs.put(num, 0L);
            this.mPrevRefreshTimeInMs.put(num, 0L);
        }
    }

    public void registerListener(ContactEventListener contactEventListener) {
        this.mListeners.add(contactEventListener);
    }

    public void unregisterListener(ContactEventListener contactEventListener) {
        this.mListeners.remove(contactEventListener);
    }

    public void start() {
        String str = LOG_TAG;
        Log.i(str, "start:");
        this.mIsThrottle = false;
        HandlerThread handlerThread = new HandlerThread("BackgroundHandler", 10);
        handlerThread.start();
        this.mBackgroundHandler = new Handler(handlerThread.getLooper());
        if (this.mObserver == null) {
            this.mObserver = new ContactObserver(new Handler());
            Log.i(str, "start: Contact observer for userId " + this.mUserId);
            this.mUserId = Extensions.ActivityManager.getCurrentUser();
            try {
                Extensions.ContentResolver.registerContentObserver(this.mContext.getContentResolver(), ContactsContract.Contacts.CONTENT_URI, false, this.mObserver, this.mUserId);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        Log.i(LOG_TAG, "stop:");
        if (this.mObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
        }
        this.mContactList.clear();
        this.mRemovedNumbers.clear();
        this.mIsBlockedContactChange = false;
        this.mIsBlockedInitialContactSyncBeforeRegi = false;
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            this.mLastRefreshTimeInMs.put(Integer.valueOf(i), 0L);
            this.mPrevRefreshTimeInMs.put(Integer.valueOf(i), 0L);
        }
    }

    public boolean isReady(int i) {
        return this.mLastRefreshTimeInMs.get(Integer.valueOf(i)).longValue() > 0;
    }

    public void setLastRefreshTime(long j, int i) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "setLastRefreshTime: mLastRefreshTimeInMs is " + j);
        this.mLastRefreshTimeInMs.put(Integer.valueOf(i), Long.valueOf(j));
    }

    public long getLastRefreshTime(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, "getLastRefreshTime: mLastRefreshTimeInMs is " + this.mLastRefreshTimeInMs);
        return this.mLastRefreshTimeInMs.get(Integer.valueOf(i)).longValue();
    }

    public List<String> getNumberlistByContactId(String str) {
        Cursor query;
        ArrayList arrayList = new ArrayList();
        try {
            query = this.mContext.getContentResolver().query(getRemoteUriwithUserId(ContactsContract.CommonDataKinds.Phone.CONTENT_URI), new String[]{"data1", "data4"}, "contact_id = ?", new String[]{str}, (String) null);
            if (query == null) {
                Log.e(LOG_TAG, "getNumberlistByContactId: no contact found");
                if (query != null) {
                    query.close();
                }
                return null;
            }
            String str2 = LOG_TAG;
            Log.i(str2, "getNumberlistByContactId: found " + query.getCount() + " contacts.");
            if (query.getCount() > 0) {
                while (query.moveToNext()) {
                    String string = query.getString(0);
                    String string2 = query.getString(1);
                    String checkNumberAndE164 = checkNumberAndE164(string, string2);
                    if (checkNumberAndE164 != null) {
                        String changeE164ByNumber = changeE164ByNumber(checkNumberAndE164, string2);
                        if (changeE164ByNumber != null) {
                            arrayList.add(changeE164ByNumber);
                        }
                    }
                }
            }
            query.close();
            return arrayList;
        } catch (RuntimeException e) {
            String str3 = LOG_TAG;
            Log.e(str3, "getNumberlistByContactId: Exception " + e.getMessage());
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public Map<String, Contact> getContacts() {
        HashMap hashMap = new HashMap(this.mContactList);
        this.mContactList.clear();
        return hashMap;
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> getAllNumber() {
        HashMap hashMap = new HashMap();
        for (int i = 0; i < this.mCapabilitiesCacheList.size(); i++) {
            for (String next : this.mCapabilitiesCacheList.get(i).getCapabilitiesNumberWithContactId()) {
                hashMap.put(next, next);
            }
        }
        return hashMap;
    }

    public List<String> getAndFlushRemovedNumbers() {
        ArrayList arrayList = new ArrayList(this.mRemovedNumbers);
        this.mRemovedNumbers.removeAll(arrayList);
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public String checkNumberAndE164(String str, String str2) {
        if (str == null) {
            return null;
        }
        if (str.startsWith("*67") || str.startsWith("*82")) {
            Log.i(LOG_TAG, "parsing for special character");
            str = str.substring(3);
        }
        if (str.contains("#") || str.contains("*") || str.contains(",") || str.contains("N")) {
            return null;
        }
        if ((str2 == null || "mx".equalsIgnoreCase(this.mCountryCode)) && !UriUtil.isValidNumber(str, this.mCountryCode)) {
            return null;
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public String changeE164ByNumber(String str, String str2) {
        return (str2 == null || "mx".equalsIgnoreCase(this.mCountryCode)) ? normalize(str) : str2;
    }

    /* access modifiers changed from: package-private */
    public String normalize(String str) {
        String replaceAll = str.replaceAll("[- ()]", "");
        if (this.mUriGenerator != null && "US".equalsIgnoreCase(this.mCountryCode)) {
            return UriUtil.getMsisdnNumber(this.mUriGenerator.getNormalizedUri(replaceAll, true));
        }
        ImsUri parseNumber = UriUtil.parseNumber(replaceAll, this.mCountryCode);
        if (parseNumber != null) {
            return UriUtil.getMsisdnNumber(parseNumber);
        }
        Log.e(LOG_TAG, "normalize: invalid number.");
        return replaceAll;
    }

    private String[] setProjection() {
        return new String[]{CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, "raw_contact_id", "display_name", "data1", "data4"};
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(13:24|(3:26|27|28)|29|30|31|32|(1:36)|37|(1:39)|40|(2:42|74)(7:43|(1:45)|(1:47)|48|(1:50)(1:51)|52|75)|72|22) */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0201, code lost:
        android.util.Log.e(LOG_TAG, "Exception in cur.getString");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0236, code lost:
        r14 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0237, code lost:
        if (r4 != null) goto L_0x0239;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0241, code lost:
        throw r14;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x00ac, B:29:0x017a] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x017a */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x01be A[Catch:{ Exception -> 0x0201, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x01c6 A[Catch:{ Exception -> 0x0201, all -> 0x0236 }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x01c7 A[Catch:{ Exception -> 0x0201, all -> 0x0236 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean refresh(int r15) {
        /*
            r14 = this;
            java.lang.String r0 = "refresh: found "
            java.util.HashMap r1 = new java.util.HashMap
            r1.<init>()
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "refresh: cc "
            r3.append(r4)
            java.lang.String r4 = r14.mCountryCode
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.s(r2, r3)
            android.content.Context r3 = r14.mContext
            android.content.ContentResolver r4 = r3.getContentResolver()
            java.lang.String[] r6 = r14.setProjection()
            java.lang.String r7 = "contact_last_updated_timestamp > ?"
            r3 = 1
            java.lang.String[] r8 = new java.lang.String[r3]
            boolean r5 = r14.mIsLimiting
            if (r5 == 0) goto L_0x0037
            java.util.Map<java.lang.Integer, java.lang.Long> r5 = r14.mPrevRefreshTimeInMs
            goto L_0x0039
        L_0x0037:
            java.util.Map<java.lang.Integer, java.lang.Long> r5 = r14.mLastRefreshTimeInMs
        L_0x0039:
            java.lang.Integer r9 = java.lang.Integer.valueOf(r15)
            java.lang.Object r5 = r5.get(r9)
            java.lang.Long r5 = (java.lang.Long) r5
            long r9 = r5.longValue()
            java.lang.String r5 = java.lang.Long.toString(r9)
            r10 = 0
            r8[r10] = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r9 = "raw_contact_id LIMIT "
            r5.append(r9)
            int r9 = r14.mStartIndex
            r5.append(r9)
            java.lang.String r9 = ","
            r5.append(r9)
            r9 = 1000(0x3e8, float:1.401E-42)
            r5.append(r9)
            java.lang.String r9 = r5.toString()
            android.net.Uri r5 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            android.net.Uri r5 = r14.getRemoteUriwithUserId(r5)
            com.sec.internal.helper.SimpleEventLog r11 = r14.mEventLog
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "refresh: mStartIndex "
            r12.append(r13)
            int r13 = r14.mStartIndex
            r12.append(r13)
            java.lang.String r12 = r12.toString()
            r11.logAndAdd(r15, r12)
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r12 = "N,REFR:"
            r11.append(r12)
            int r12 = r14.mStartIndex
            r11.append(r12)
            java.lang.String r11 = r11.toString()
            r12 = 302383104(0x12060000, float:4.2282945E-28)
            com.sec.internal.log.IMSLog.c(r12, r11)
            android.database.Cursor r4 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ RuntimeException -> 0x0242 }
            if (r4 != 0) goto L_0x00b5
            java.lang.String r14 = "refresh: no contact found"
            android.util.Log.e(r2, r14)     // Catch:{ all -> 0x0236 }
            if (r4 == 0) goto L_0x00b4
            r4.close()     // Catch:{ RuntimeException -> 0x0242 }
        L_0x00b4:
            return r10
        L_0x00b5:
            boolean r5 = r14.mIsLimiting     // Catch:{ all -> 0x0236 }
            if (r5 != 0) goto L_0x00ed
            java.util.Map<java.lang.Integer, java.lang.Long> r5 = r14.mPrevRefreshTimeInMs     // Catch:{ all -> 0x0236 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r15)     // Catch:{ all -> 0x0236 }
            java.util.Map<java.lang.Integer, java.lang.Long> r7 = r14.mLastRefreshTimeInMs     // Catch:{ all -> 0x0236 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r15)     // Catch:{ all -> 0x0236 }
            java.lang.Object r7 = r7.get(r8)     // Catch:{ all -> 0x0236 }
            java.lang.Long r7 = (java.lang.Long) r7     // Catch:{ all -> 0x0236 }
            r5.put(r6, r7)     // Catch:{ all -> 0x0236 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0236 }
            r5.<init>()     // Catch:{ all -> 0x0236 }
            java.lang.String r6 = "refresh: set mPrevRefreshTimeInMs = "
            r5.append(r6)     // Catch:{ all -> 0x0236 }
            java.util.Map<java.lang.Integer, java.lang.Long> r6 = r14.mPrevRefreshTimeInMs     // Catch:{ all -> 0x0236 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r15)     // Catch:{ all -> 0x0236 }
            java.lang.Object r6 = r6.get(r7)     // Catch:{ all -> 0x0236 }
            r5.append(r6)     // Catch:{ all -> 0x0236 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0236 }
            com.sec.internal.log.IMSLog.i(r2, r15, r5)     // Catch:{ all -> 0x0236 }
        L_0x00ed:
            java.util.Map<java.lang.Integer, java.lang.Long> r5 = r14.mLastRefreshTimeInMs     // Catch:{ all -> 0x0236 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r15)     // Catch:{ all -> 0x0236 }
            java.util.Date r7 = new java.util.Date     // Catch:{ all -> 0x0236 }
            r7.<init>()     // Catch:{ all -> 0x0236 }
            long r7 = r7.getTime()     // Catch:{ all -> 0x0236 }
            java.lang.Long r7 = java.lang.Long.valueOf(r7)     // Catch:{ all -> 0x0236 }
            r5.put(r6, r7)     // Catch:{ all -> 0x0236 }
            int r5 = r4.getCount()     // Catch:{ all -> 0x0236 }
            r14.mContactCurCount = r5     // Catch:{ all -> 0x0236 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0236 }
            r5.<init>()     // Catch:{ all -> 0x0236 }
            r5.append(r0)     // Catch:{ all -> 0x0236 }
            int r6 = r14.mContactCurCount     // Catch:{ all -> 0x0236 }
            r5.append(r6)     // Catch:{ all -> 0x0236 }
            java.lang.String r6 = " contacts. mLastRefreshTimeInMs = "
            r5.append(r6)     // Catch:{ all -> 0x0236 }
            java.util.Map<java.lang.Integer, java.lang.Long> r6 = r14.mLastRefreshTimeInMs     // Catch:{ all -> 0x0236 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r15)     // Catch:{ all -> 0x0236 }
            java.lang.Object r6 = r6.get(r7)     // Catch:{ all -> 0x0236 }
            r5.append(r6)     // Catch:{ all -> 0x0236 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0236 }
            com.sec.internal.log.IMSLog.i(r2, r15, r5)     // Catch:{ all -> 0x0236 }
            com.sec.internal.helper.SimpleEventLog r2 = r14.mEventLog     // Catch:{ all -> 0x0236 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0236 }
            r5.<init>()     // Catch:{ all -> 0x0236 }
            r5.append(r0)     // Catch:{ all -> 0x0236 }
            int r0 = r14.mContactCurCount     // Catch:{ all -> 0x0236 }
            r5.append(r0)     // Catch:{ all -> 0x0236 }
            java.lang.String r0 = " contacts."
            r5.append(r0)     // Catch:{ all -> 0x0236 }
            java.lang.String r0 = r5.toString()     // Catch:{ all -> 0x0236 }
            r2.add(r15, r0)     // Catch:{ all -> 0x0236 }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x0236 }
            r0.<init>()     // Catch:{ all -> 0x0236 }
            java.lang.String r2 = "N,REFR:FOUND:"
            r0.append(r2)     // Catch:{ all -> 0x0236 }
            int r2 = r14.mContactCurCount     // Catch:{ all -> 0x0236 }
            r0.append(r2)     // Catch:{ all -> 0x0236 }
            java.lang.String r0 = r0.toString()     // Catch:{ all -> 0x0236 }
            r2 = 302383105(0x12060001, float:4.228295E-28)
            com.sec.internal.log.IMSLog.c(r2, r0)     // Catch:{ all -> 0x0236 }
            int r0 = r4.getCount()     // Catch:{ all -> 0x0236 }
            if (r0 <= 0) goto L_0x0224
            r0 = r10
        L_0x016a:
            boolean r2 = r4.moveToNext()     // Catch:{ all -> 0x0236 }
            if (r2 == 0) goto L_0x020a
            int r0 = r0 + r3
            int r2 = r0 % 300
            if (r2 != 0) goto L_0x017a
            r5 = 300(0x12c, double:1.48E-321)
            java.lang.Thread.sleep(r5)     // Catch:{ InterruptedException -> 0x017a }
        L_0x017a:
            java.lang.String r2 = r4.getString(r10)     // Catch:{ Exception -> 0x0201 }
            java.lang.String r5 = r4.getString(r3)     // Catch:{ Exception -> 0x0201 }
            r6 = 2
            java.lang.String r6 = r4.getString(r6)     // Catch:{ Exception -> 0x0201 }
            r7 = 3
            java.lang.String r7 = r4.getString(r7)     // Catch:{ Exception -> 0x0201 }
            r8 = 4
            java.lang.String r8 = r4.getString(r8)     // Catch:{ Exception -> 0x0201 }
            java.lang.String r9 = r14.mLastRawId     // Catch:{ all -> 0x0236 }
            if (r9 == 0) goto L_0x01ba
            int r9 = java.lang.Integer.parseInt(r5)     // Catch:{ all -> 0x0236 }
            java.lang.String r11 = r14.mLastRawId     // Catch:{ all -> 0x0236 }
            int r11 = java.lang.Integer.parseInt(r11)     // Catch:{ all -> 0x0236 }
            if (r9 > r11) goto L_0x01ba
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x0236 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ all -> 0x0236 }
            r11.<init>()     // Catch:{ all -> 0x0236 }
            java.lang.String r12 = "refresh: ContactUpdated, rawId ="
            r11.append(r12)     // Catch:{ all -> 0x0236 }
            r11.append(r5)     // Catch:{ all -> 0x0236 }
            java.lang.String r11 = r11.toString()     // Catch:{ all -> 0x0236 }
            com.sec.internal.log.IMSLog.i(r9, r15, r11)     // Catch:{ all -> 0x0236 }
            r14.mIsContactUpdated = r3     // Catch:{ all -> 0x0236 }
        L_0x01ba:
            int r9 = r14.mContactCurCount     // Catch:{ all -> 0x0236 }
            if (r0 != r9) goto L_0x01c0
            r14.mLastRawId = r5     // Catch:{ all -> 0x0236 }
        L_0x01c0:
            java.lang.String r7 = r14.checkNumberAndE164(r7, r8)     // Catch:{ all -> 0x0236 }
            if (r7 != 0) goto L_0x01c7
            goto L_0x016a
        L_0x01c7:
            java.lang.String r8 = r14.changeE164ByNumber(r7, r8)     // Catch:{ all -> 0x0236 }
            java.lang.Object r9 = r1.get(r5)     // Catch:{ all -> 0x0236 }
            com.sec.internal.ims.servicemodules.options.Contact r9 = (com.sec.internal.ims.servicemodules.options.Contact) r9     // Catch:{ all -> 0x0236 }
            if (r9 != 0) goto L_0x01db
            java.util.Map<java.lang.String, com.sec.internal.ims.servicemodules.options.Contact> r9 = r14.mContactList     // Catch:{ all -> 0x0236 }
            java.lang.Object r9 = r9.get(r5)     // Catch:{ all -> 0x0236 }
            com.sec.internal.ims.servicemodules.options.Contact r9 = (com.sec.internal.ims.servicemodules.options.Contact) r9     // Catch:{ all -> 0x0236 }
        L_0x01db:
            if (r9 != 0) goto L_0x01e2
            com.sec.internal.ims.servicemodules.options.Contact r9 = new com.sec.internal.ims.servicemodules.options.Contact     // Catch:{ all -> 0x0236 }
            r9.<init>(r2, r5)     // Catch:{ all -> 0x0236 }
        L_0x01e2:
            r9.setId(r2)     // Catch:{ all -> 0x0236 }
            r9.setName(r6)     // Catch:{ all -> 0x0236 }
            com.sec.internal.ims.servicemodules.options.Contact$ContactNumber r2 = new com.sec.internal.ims.servicemodules.options.Contact$ContactNumber     // Catch:{ all -> 0x0236 }
            if (r8 != 0) goto L_0x01ee
            r6 = 0
            goto L_0x01f6
        L_0x01ee:
            java.lang.String r6 = "[- ()]"
            java.lang.String r11 = ""
            java.lang.String r6 = r8.replaceAll(r6, r11)     // Catch:{ all -> 0x0236 }
        L_0x01f6:
            r2.<init>(r7, r6)     // Catch:{ all -> 0x0236 }
            r9.insertContactNumberIntoList(r2)     // Catch:{ all -> 0x0236 }
            r1.put(r5, r9)     // Catch:{ all -> 0x0236 }
            goto L_0x016a
        L_0x0201:
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0236 }
            java.lang.String r5 = "Exception in cur.getString"
            android.util.Log.e(r2, r5)     // Catch:{ all -> 0x0236 }
            goto L_0x016a
        L_0x020a:
            com.sec.internal.helper.SimpleEventLog r0 = r14.mEventLog     // Catch:{ all -> 0x0236 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0236 }
            r2.<init>()     // Catch:{ all -> 0x0236 }
            java.lang.String r5 = "refresh: mLastRawId = "
            r2.append(r5)     // Catch:{ all -> 0x0236 }
            java.lang.String r5 = r14.mLastRawId     // Catch:{ all -> 0x0236 }
            r2.append(r5)     // Catch:{ all -> 0x0236 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0236 }
            r0.logAndAdd(r15, r2)     // Catch:{ all -> 0x0236 }
            goto L_0x0225
        L_0x0224:
            r3 = r10
        L_0x0225:
            java.util.Map<java.lang.String, com.sec.internal.ims.servicemodules.options.Contact> r15 = r14.mContactList     // Catch:{ all -> 0x0236 }
            r15.clear()     // Catch:{ all -> 0x0236 }
            java.util.Map<java.lang.String, com.sec.internal.ims.servicemodules.options.Contact> r15 = r14.mContactList     // Catch:{ all -> 0x0236 }
            r15.putAll(r1)     // Catch:{ all -> 0x0236 }
            r14.dumpContactList()     // Catch:{ all -> 0x0236 }
            r4.close()     // Catch:{ RuntimeException -> 0x0242 }
            return r3
        L_0x0236:
            r14 = move-exception
            if (r4 == 0) goto L_0x0241
            r4.close()     // Catch:{ all -> 0x023d }
            goto L_0x0241
        L_0x023d:
            r15 = move-exception
            r14.addSuppressed(r15)     // Catch:{ RuntimeException -> 0x0242 }
        L_0x0241:
            throw r14     // Catch:{ RuntimeException -> 0x0242 }
        L_0x0242:
            r14 = move-exception
            java.lang.String r15 = LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "refresh: Can not refresh : "
            r0.append(r1)
            java.lang.String r14 = r14.getMessage()
            r0.append(r14)
            java.lang.String r14 = r0.toString()
            android.util.Log.e(r15, r14)
            r14 = 302383106(0x12060002, float:4.2282954E-28)
            java.lang.String r15 = "N,REFR:ER"
            com.sec.internal.log.IMSLog.c(r14, r15)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.ContactCache.refresh(int):boolean");
    }

    private void dumpContactList() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Contact next : this.mContactList.values()) {
            i++;
            if (i > 40) {
                break;
            }
            sb.append("ContactId (");
            sb.append(next.getId());
            sb.append(")");
            sb.append("    RawId: ");
            sb.append(next.getRawId());
            sb.append(10);
            sb.append("    Name: ");
            sb.append(next.getName());
            sb.append(10);
            Iterator<Contact.ContactNumber> it = next.getContactNumberList().iterator();
            while (it.hasNext()) {
                Contact.ContactNumber next2 = it.next();
                sb.append("    Number: ");
                sb.append(next2.getNumber());
                sb.append(10);
                sb.append("    E164: ");
                sb.append(next2.getNormalizedNumber());
                sb.append(10);
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, "dump:\n" + sb.toString());
    }

    public void setUriGenerator(UriGenerator uriGenerator) {
        this.mUriGenerator = uriGenerator;
    }

    public void setThrottleContactSync(boolean z, int i) {
        Handler handler = this.mBackgroundHandler;
        if (handler != null) {
            handler.post(new ContactCache$$ExternalSyntheticLambda2(this, z, i));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$setThrottleContactSync$0(boolean z, int i) {
        String str = LOG_TAG;
        Log.i(str, "setThrottleContactSync : " + z);
        if (this.mIsThrottle != z) {
            this.mIsThrottle = z;
            if (z) {
                return;
            }
            if (this.mResyncRequired.get() || this.mIsLimiting) {
                if (this.mIsLimiting) {
                    if (this.mResyncRequired.get()) {
                        processChangeDuringLimiting(i);
                    }
                    this.mStartIndex += 1000;
                    Log.i(str, "setThrottleContactSync : Limiting, mStartIndex = " + this.mStartIndex);
                }
                Log.i(str, "setThrottleContactSync : try to resync");
                sendMessageContactSync();
                this.mResyncRequired.set(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void processChangeDuringLimiting(int i) {
        Cursor query;
        String str;
        String str2 = LOG_TAG;
        IMSLog.i(str2, i, "processChangeDuringLimiting: Start.");
        try {
            query = this.mContext.getContentResolver().query(getRemoteUriwithUserId(ContactsContract.CommonDataKinds.Phone.CONTENT_URI), setProjection(), "contact_last_updated_timestamp > ?", new String[]{Long.toString(this.mLastRefreshTimeInMs.get(Integer.valueOf(i)).longValue())}, "raw_contact_id");
            if (query == null) {
                Log.e(str2, "processChangeDuringLimiting: no contact found");
            } else {
                int count = query.getCount();
                if (count == 0) {
                    Log.i(str2, "processChangeDuringLimiting: found 0, removed");
                    processRemovedContact();
                } else {
                    Log.i(str2, "processChangeDuringLimiting: found " + count);
                    HashMap hashMap = new HashMap();
                    boolean z = false;
                    while (true) {
                        if (!query.moveToNext()) {
                            break;
                        }
                        String string = query.getString(1);
                        if (Integer.parseInt(string) > Integer.parseInt(this.mLastRawId)) {
                            Log.i(LOG_TAG, "processChangeDuringLimiting: rawId > mLastRawId, rawId = " + string + ", mLastRawId = " + this.mLastRawId);
                            break;
                        }
                        String string2 = query.getString(0);
                        String string3 = query.getString(2);
                        String string4 = query.getString(3);
                        String string5 = query.getString(4);
                        String checkNumberAndE164 = checkNumberAndE164(string4, string5);
                        if (checkNumberAndE164 != null) {
                            String changeE164ByNumber = changeE164ByNumber(checkNumberAndE164, string5);
                            Contact contact = (Contact) hashMap.get(string);
                            if (contact == null) {
                                contact = new Contact(string2, string);
                            }
                            contact.setId(string2);
                            contact.setName(string3);
                            if (changeE164ByNumber == null) {
                                str = null;
                            } else {
                                str = changeE164ByNumber.replaceAll("[- ()]", "");
                            }
                            contact.insertContactNumberIntoList(new Contact.ContactNumber(checkNumberAndE164, str));
                            hashMap.put(string, contact);
                        }
                        z = true;
                    }
                    if (z) {
                        this.mContactList.putAll(hashMap);
                        Log.i(LOG_TAG, "processChangeDuringLimiting: Done. contact updated.");
                        for (ContactEventListener onChanged : this.mListeners) {
                            onChanged.onChanged();
                        }
                    }
                }
            }
            if (query != null) {
                query.close();
                return;
            }
            return;
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "processChangeDuringLimiting: Exception " + e.getMessage());
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* access modifiers changed from: package-private */
    public boolean processRemovedContact() {
        String str = LOG_TAG;
        Log.i(str, "processRemovedContact: Start.");
        Map<String, String> allNumber = getAllNumber();
        if (allNumber == null || allNumber.size() == 0) {
            Log.i(str, "processRemovedContact: No cached numbers. return");
            return false;
        }
        List<String> allNumbersInContactDB = getAllNumbersInContactDB();
        if (allNumbersInContactDB == null || allNumbersInContactDB.size() == 0) {
            Log.i(str, "processRemovedContact: No numbers in Contact DB");
        } else {
            Log.i(str, "processRemovedContact: start remove");
            for (String remove : allNumbersInContactDB) {
                allNumber.remove(remove);
            }
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("processRemovedContact: Done. " + allNumber.size() + " numbers removed.");
        this.mRemovedNumbers.addAll(allNumber.values());
        if (this.mRemovedNumbers.size() > 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Can't wrap try/catch for region: R(7:16|(3:18|19|20)|21|22|(2:24|43)(2:25|(2:27|45)(1:44))|42|14) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0068 */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0076 A[Catch:{ all -> 0x0085, all -> 0x008c, RuntimeException -> 0x0091 }] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0077 A[Catch:{ all -> 0x0085, all -> 0x008c, RuntimeException -> 0x0091 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<java.lang.String> getAllNumbersInContactDB() {
        /*
            r8 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            android.content.Context r1 = r8.mContext
            android.content.ContentResolver r2 = r1.getContentResolver()
            java.lang.String r1 = "data1"
            java.lang.String r3 = "data4"
            java.lang.String[] r4 = new java.lang.String[]{r1, r3}
            android.net.Uri r1 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            android.net.Uri r3 = r8.getRemoteUriwithUserId(r1)
            r5 = 0
            r6 = 0
            r7 = 0
            r1 = 0
            android.database.Cursor r2 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ RuntimeException -> 0x0091 }
            if (r2 != 0) goto L_0x0030
            java.lang.String r8 = LOG_TAG     // Catch:{ all -> 0x0085 }
            java.lang.String r0 = "getAllNumbersInContactDB: no contact found"
            android.util.Log.e(r8, r0)     // Catch:{ all -> 0x0085 }
            if (r2 == 0) goto L_0x002f
            r2.close()     // Catch:{ RuntimeException -> 0x0091 }
        L_0x002f:
            return r1
        L_0x0030:
            java.lang.String r3 = LOG_TAG     // Catch:{ all -> 0x0085 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0085 }
            r4.<init>()     // Catch:{ all -> 0x0085 }
            java.lang.String r5 = "getAllNumbersInContactDB: found "
            r4.append(r5)     // Catch:{ all -> 0x0085 }
            int r5 = r2.getCount()     // Catch:{ all -> 0x0085 }
            r4.append(r5)     // Catch:{ all -> 0x0085 }
            java.lang.String r5 = " contacts."
            r4.append(r5)     // Catch:{ all -> 0x0085 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0085 }
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x0085 }
            int r3 = r2.getCount()     // Catch:{ all -> 0x0085 }
            if (r3 <= 0) goto L_0x0081
            r3 = 0
            r4 = r3
        L_0x0057:
            boolean r5 = r2.moveToNext()     // Catch:{ all -> 0x0085 }
            if (r5 == 0) goto L_0x0081
            r5 = 1
            int r4 = r4 + r5
            int r6 = r4 % 300
            if (r6 != 0) goto L_0x0068
            r6 = 300(0x12c, double:1.48E-321)
            java.lang.Thread.sleep(r6)     // Catch:{ InterruptedException -> 0x0068 }
        L_0x0068:
            java.lang.String r6 = r2.getString(r3)     // Catch:{ all -> 0x0085 }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ all -> 0x0085 }
            java.lang.String r6 = r8.checkNumberAndE164(r6, r5)     // Catch:{ all -> 0x0085 }
            if (r6 != 0) goto L_0x0077
            goto L_0x0057
        L_0x0077:
            java.lang.String r5 = r8.changeE164ByNumber(r6, r5)     // Catch:{ all -> 0x0085 }
            if (r5 == 0) goto L_0x0057
            r0.add(r5)     // Catch:{ all -> 0x0085 }
            goto L_0x0057
        L_0x0081:
            r2.close()     // Catch:{ RuntimeException -> 0x0091 }
            return r0
        L_0x0085:
            r8 = move-exception
            if (r2 == 0) goto L_0x0090
            r2.close()     // Catch:{ all -> 0x008c }
            goto L_0x0090
        L_0x008c:
            r0 = move-exception
            r8.addSuppressed(r0)     // Catch:{ RuntimeException -> 0x0091 }
        L_0x0090:
            throw r8     // Catch:{ RuntimeException -> 0x0091 }
        L_0x0091:
            r8 = move-exception
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "getAllNumbersInContactDB: Exception "
            r2.append(r3)
            java.lang.String r8 = r8.getMessage()
            r2.append(r8)
            java.lang.String r8 = r2.toString()
            android.util.Log.e(r0, r8)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.ContactCache.getAllNumbersInContactDB():java.util.List");
    }

    protected class ContactObserver extends ContentObserver {
        ContactObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z, Uri uri) {
            Log.i(ContactCache.LOG_TAG, "===== Contact updated. =====");
            ContactCache contactCache = ContactCache.this;
            if (contactCache.mCountryCode == null) {
                Log.i(ContactCache.LOG_TAG, "No SIM available. bail.");
            } else {
                contactCache.sendMessageContactSync();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowedContactSync() {
        if (RcsUtils.DualRcs.isDualRcsSettings()) {
            for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
                if (isAllowedContactSync(i)) {
                    return true;
                }
            }
        } else if (isAllowedContactSync(SimUtil.getActiveDataPhoneId())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowedInitialContactSyncBeforeRegi(int i) {
        return ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.RCS_INITIAL_CONTACT_SYNC_BEFORE_REGI, true);
    }

    private boolean isAllowedContactSync(int i) {
        boolean isRcsEnabledinSettings = RcsUtils.UiUtils.isRcsEnabledinSettings(this.mContext, i);
        boolean z = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.CONTACT_SYNC_IN_SWITCH_OFF, true);
        boolean z2 = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 0, i) == 1;
        if (!isRcsEnabledinSettings) {
            IMSLog.s(LOG_TAG, i, "isAllowedContactSync: rcs is off in customer.");
            return false;
        } else if (!z2 && !z) {
            IMSLog.s(LOG_TAG, i, "isAllowedContactSync: CONTACT_SYNC_IN_SWITCH_OFF is false.");
            return false;
        } else if (SimUtil.getSimMno(i).isChn()) {
            IMSLog.s(LOG_TAG, i, "isAllowedContactSync: Chn always false.");
            return false;
        } else {
            IMSLog.s(LOG_TAG, i, "isAllowedContactSync: contact sync is allowed");
            return true;
        }
    }

    public boolean getIsBlockedContactChange() {
        return this.mIsBlockedContactChange;
    }

    public void setIsBlockedContactChange(boolean z) {
        this.mIsBlockedContactChange = z;
    }

    public boolean getBlockedInitialContactSyncBeforeRegi() {
        return this.mIsBlockedInitialContactSyncBeforeRegi;
    }

    public void sendMessageContactSync() {
        if (!isAllowedContactSync()) {
            Log.i(LOG_TAG, "sendMessageContactSync: block the contact sync.");
            this.mIsBlockedContactChange = true;
        } else {
            this.mIsBlockedContactChange = false;
        }
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        if (isAllowedInitialContactSyncBeforeRegi(activeDataPhoneId) || this.mLastRefreshTimeInMs.get(Integer.valueOf(activeDataPhoneId)).longValue() != 0) {
            this.mIsBlockedInitialContactSyncBeforeRegi = false;
        } else {
            IMSLog.i(LOG_TAG, activeDataPhoneId, "sendMessageContactSync: block the initial contact sync before regi.");
            this.mIsBlockedInitialContactSyncBeforeRegi = true;
        }
        if (!this.mIsBlockedContactChange && !this.mIsBlockedInitialContactSyncBeforeRegi) {
            IMSLog.i(LOG_TAG, activeDataPhoneId, "sendMessageContactSync: Try contact sync after 3 sec.");
            this.mContactCacheHandler.removeMessages(0);
            ContactCacheHandler contactCacheHandler = this.mContactCacheHandler;
            contactCacheHandler.sendMessageDelayed(contactCacheHandler.obtainMessage(0), RegistrationGovernor.RETRY_AFTER_PDNLOST_MS);
        }
    }

    class ContactCacheHandler extends Handler {
        static final int HANDLE_START_CONTACT_SYNC = 0;

        public ContactCacheHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 0) {
                Log.i(ContactCache.LOG_TAG, "HANDLE_START_CONTACT_SYNC : ");
                int contactProviderStatus = ContactCache.this.getContactProviderStatus();
                if (contactProviderStatus == 0 || contactProviderStatus == 2) {
                    ContactCache.this.onStartContactSync();
                } else if (contactProviderStatus == 1) {
                    Log.i(ContactCache.LOG_TAG, "ContactProvider is in busy state");
                    IMSLog.c(LogClass.CC_START_SYNC, "N,CP:BUSY");
                    ContactCache.this.sendMessageContactSync();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onStartContactSync() {
        String str = LOG_TAG;
        Log.i(str, "onStartContactSync : ");
        if (this.mSyncInProgress.get() || this.mIsThrottle) {
            this.mResyncRequired.set(true);
            Log.i(str, "onStartContactSync : Sync In Progress. Sync will start later, mIsThrottle = " + this.mIsThrottle);
            return;
        }
        this.mSyncInProgress.set(true);
        startContactSync();
    }

    /* access modifiers changed from: package-private */
    public void startContactSync(Mno mno) {
        String str = LOG_TAG;
        Log.i(str, "startContactSync: " + mno);
        this.mMno = mno;
        Handler handler = this.mBackgroundHandler;
        if (handler != null) {
            handler.post(new ContactCache$$ExternalSyntheticLambda1(this));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$startContactSync$2() {
        if (this.mCountryCode == null) {
            Mno mno = this.mMno;
            if (mno == Mno.DEFAULT) {
                Log.e(LOG_TAG, "startContactSync: operator is unknown. bail");
                this.mSyncInProgress.set(false);
                return;
            }
            this.mCountryCode = mno.getCountryCode();
        }
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        String str = LOG_TAG;
        IMSLog.i(str, activeDataPhoneId, "startContactSync: start caching contacts.");
        boolean refresh = refresh(activeDataPhoneId);
        if (this.mContactCurCount == 1000) {
            this.mIsLimiting = true;
            setThrottleContactSync(true, activeDataPhoneId);
        } else {
            this.mIsLimiting = false;
            this.mStartIndex = 0;
        }
        this.mSyncInProgress.set(false);
        if (this.mResyncRequired.get()) {
            this.mResyncRequired.set(false);
            sendMessageContactSync();
        } else {
            if (!refresh) {
                IMSLog.i(str, activeDataPhoneId, "startContactSync: removed, check removed contacts.");
                refresh = processRemovedContact();
            } else if (this.mIsContactUpdated) {
                this.mIsContactUpdated = false;
                processRemovedContact();
            }
            if (!Debug.isProductShip()) {
                new Thread(new ContactCache$$ExternalSyntheticLambda0(this, activeDataPhoneId)).start();
            }
        }
        if (refresh) {
            IMSLog.i(str, activeDataPhoneId, "startContactSync: Done. contact updated.");
            for (ContactEventListener onChanged : this.mListeners) {
                onChanged.onChanged();
            }
            return;
        }
        IMSLog.i(str, activeDataPhoneId, "startContactSync: Done. contact no found.");
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$startContactSync$1(int i) {
        this.mEventLog.logAndAdd(i, "Explicit GC after sync");
        SystemWrapper.explicitGc();
    }

    /* access modifiers changed from: package-private */
    public void startContactSync() {
        startContactSync(this.mMno);
    }

    public void setMno(Mno mno) {
        String str = LOG_TAG;
        Log.i(str, "setMno: " + mno);
        this.mMno = mno;
        if (this.mCountryCode == null && mno != Mno.DEFAULT) {
            this.mCountryCode = mno.getCountryCode();
            Log.i(str, "setMno: mCountryCode = " + this.mCountryCode);
        }
    }

    public void resetRefreshTime(int i) {
        this.mLastRefreshTimeInMs.put(Integer.valueOf(i), 0L);
        this.mPrevRefreshTimeInMs.put(Integer.valueOf(i), 0L);
    }

    public int getContactProviderStatus() {
        Cursor query;
        int i = -1;
        try {
            query = this.mContext.getContentResolver().query(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "provider_status"), new String[]{"status"}, (String) null, (String[]) null, (String) null);
            if (query != null) {
                if (query.moveToFirst()) {
                    i = query.getInt(0);
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (Exception e) {
            String str = LOG_TAG;
            Log.e(str, "getContactProviderStatus: Exception " + e.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        String str2 = LOG_TAG;
        Log.i(str2, "getContactProviderStatus: " + i);
        return i;
        throw th;
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + ContactCache.class.getSimpleName() + ":");
        IMSLog.increaseIndent(str);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(str);
    }
}
