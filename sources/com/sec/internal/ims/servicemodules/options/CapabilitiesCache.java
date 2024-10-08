package com.sec.internal.ims.servicemodules.options;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.LruCache;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class CapabilitiesCache {
    private static final String LOG_TAG = "CapabilitiesCache";
    static final int MaxCacheSize = 10000;
    private static final int PERSIST_MAX_SIZE = 100;
    private static final int PERSIST_TIMEOUT = 500;
    final LruCache<ImsUri, Capabilities> mCapabilitiesList = new LruCache<>(10000);
    CapabilityStorage mCapabilityStorage = null;
    Handler mCapabilityStorageHandler;
    private Context mContext;
    boolean mIsPersistPosted = false;
    private boolean mPersistTimeout = false;
    private int mPhoneId;
    private HandlerThread mThread = new HandlerThread("CapabilityStorage", 10);
    ArrayList<ImsUri> mUriListToDelete = new ArrayList<>();
    ConcurrentHashMap<ImsUri, Capabilities> mUriListToUpdate = new ConcurrentHashMap<>();

    public static int getMaxCacheSize() {
        return 10000;
    }

    public CapabilitiesCache(Context context, int i) {
        this.mContext = context;
        this.mPhoneId = i;
        initCapabilityStorage();
    }

    private void initCapabilityStorage() {
        this.mThread.start();
        this.mCapabilityStorageHandler = new Handler(this.mThread.getLooper());
        this.mCapabilityStorage = new CapabilityStorage(this.mContext, this, this.mPhoneId);
    }

    public void loadCapabilityStorage() {
        this.mCapabilitiesList.evictAll();
        this.mCapabilityStorageHandler.post(new CapabilitiesCache$$ExternalSyntheticLambda3(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$loadCapabilityStorage$0() {
        this.mCapabilityStorage.load();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$resetCapabilityStorage$1() {
        this.mCapabilityStorage.reset();
    }

    private void resetCapabilityStorage() {
        this.mCapabilityStorageHandler.post(new CapabilitiesCache$$ExternalSyntheticLambda1(this));
    }

    /* access modifiers changed from: package-private */
    public void tryPersist(boolean z) {
        this.mCapabilityStorageHandler.post(new CapabilitiesCache$$ExternalSyntheticLambda2(this, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$tryPersist$3(boolean z) {
        if (z || this.mPersistTimeout || this.mUriListToUpdate.size() >= 100) {
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "tryPersist: force = " + z + ", timeout = " + this.mPersistTimeout);
            this.mIsPersistPosted = false;
            this.mPersistTimeout = false;
            this.mCapabilityStorage.persist();
        } else if (!this.mIsPersistPosted) {
            this.mIsPersistPosted = true;
            new Handler().postDelayed(new CapabilitiesCache$$ExternalSyntheticLambda4(this), 500);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$tryPersist$2() {
        if (this.mUriListToUpdate.size() > 0) {
            int i = this.mPhoneId;
            IMSLog.i(LOG_TAG, i, "tryPersist: try remainder " + this.mUriListToUpdate.size());
            this.mPersistTimeout = true;
            tryPersist(false);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$persistToContactDB$4(Capabilities capabilities, boolean z) {
        this.mCapabilityStorage.persistToContactDB(capabilities, z);
    }

    /* access modifiers changed from: package-private */
    public void persistToContactDB(Capabilities capabilities, boolean z) {
        this.mCapabilityStorageHandler.post(new CapabilitiesCache$$ExternalSyntheticLambda5(this, capabilities, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$deleteNonRcsDataFromContactDB$5() {
        this.mCapabilityStorage.deleteNonRcsDataFromContactDB();
    }

    public void deleteNonRcsDataFromContactDB() {
        this.mCapabilityStorageHandler.post(new CapabilitiesCache$$ExternalSyntheticLambda6(this));
    }

    public List<String> getCapabilitiesNumberWithContactId() {
        return this.mCapabilityStorage.getCapabilitiesNumberWithContactId();
    }

    public Collection<Capabilities> getCapabilitiesCache() {
        return this.mCapabilitiesList.snapshot().values();
    }

    public Collection<Capabilities> getAllCapabilities() {
        return this.mCapabilityStorage.getAllCapabilities();
    }

    private int getAmountCapabilities() {
        return this.mCapabilityStorage.getAmountCapabilities();
    }

    private int getAmountRcsCapabilities() {
        return this.mCapabilityStorage.getAmountRcsCapabilities();
    }

    public void sendRCSCInfoToHQM() {
        this.mCapabilityStorageHandler.post(new CapabilitiesCache$$ExternalSyntheticLambda0(this));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$sendRCSCInfoToHQM$6() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.RCSC_KEY_NCAP, String.valueOf(getAmountCapabilities()));
        contentValues.put(DiagnosisConstants.RCSC_KEY_NRCS, String.valueOf(getAmountRcsCapabilities()));
        ImsLogAgentUtil.sendLogToAgent(this.mPhoneId, this.mContext, "RCSC", contentValues);
    }

    public void add(Capabilities capabilities) {
        if (capabilities == null || capabilities.getUri() == null) {
            Log.i(LOG_TAG, "add: null CapexInfo.");
            return;
        }
        ImsUri uri = capabilities.getUri();
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "add: " + uri);
        this.mCapabilitiesList.put(uri, capabilities);
    }

    public void remove(List<ImsUri> list) {
        if (list != null) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "remove: " + list);
            if (list.size() > 0) {
                for (ImsUri remove : list) {
                    this.mCapabilitiesList.remove(remove);
                }
                this.mUriListToDelete.addAll(list);
                tryPersist(true);
            }
        }
    }

    public boolean update(ImsUri imsUri, long j, long j2, String str, long j3, Date date, List<ImsUri> list, String str2) {
        String str3;
        Date date2;
        boolean z;
        boolean z2;
        ImsUri imsUri2 = imsUri;
        long j4 = j;
        long j5 = j3;
        List<ImsUri> list2 = list;
        String str4 = str2;
        if (imsUri2 == null) {
            return false;
        }
        Capabilities capabilities = get(imsUri);
        if (capabilities == null) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "update: Add new capabilities from Unknown Uri = " + imsUri2);
            String msisdnNumber = UriUtil.getMsisdnNumber(imsUri);
            Capabilities capabilities2 = r1;
            str3 = LOG_TAG;
            Capabilities capabilities3 = new Capabilities(imsUri, msisdnNumber, (String) null, -1, (String) null);
            add(capabilities2);
            capabilities = capabilities2;
        } else {
            str3 = LOG_TAG;
        }
        int i2 = this.mPhoneId;
        IMSLog.i(str3, i2, "update: feature changed " + capabilities.getFeature() + " to " + j4);
        if (isAvailable(j4) != isAvailable(capabilities.getFeature()) || (isAvailable(j4) && j4 != capabilities.getFeature())) {
            z = true;
            date2 = date;
        } else {
            date2 = date;
            z = false;
        }
        capabilities.setTimestamp(date2);
        capabilities.setUri(imsUri2);
        capabilities.setFeatures(j4);
        capabilities.setAvailableFeatures(j2);
        capabilities.setAvailiable(isAvailable(j4));
        capabilities.setPidf(str);
        capabilities.setPhoneId(this.mPhoneId);
        if (str4 != null) {
            capabilities.setExtFeature(new ArrayList(Arrays.asList(str4.split(","))));
        }
        Log.i(str3, "update: setting last seen in capabilities " + j5);
        capabilities.setLastSeen(j5);
        List<ImsUri> list3 = list;
        String str5 = str3;
        if (list3 != null) {
            capabilities.setPAssertedId(list3);
        }
        if (!capabilities.getLegacyLatching() || (!capabilities.isFeatureAvailable(Capabilities.FEATURE_CHAT_CPM) && !capabilities.isFeatureAvailable(Capabilities.FEATURE_CHAT_SIMPLE_IM) && !capabilities.isFeatureAvailable(Capabilities.FEATURE_FT) && !capabilities.isFeatureAvailable(Capabilities.FEATURE_FT_HTTP) && !capabilities.isFeatureAvailable(Capabilities.FEATURE_FT_STORE))) {
            z2 = false;
        } else {
            z2 = false;
            capabilities.setLegacyLatching(false);
            IMSLog.i(str5, this.mPhoneId, "update: Legacy Latching clear !!");
        }
        this.mUriListToUpdate.put(imsUri2, capabilities);
        tryPersist(z2);
        persistToContactDB(capabilities, z);
        return z;
    }

    public void updateContactInfo(ImsUri imsUri, String str, String str2, String str3, boolean z, Capabilities capabilities) {
        if (capabilities != null) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "updateContactInfo: update " + imsUri);
            capabilities.updateCapabilities(str, str2, str3);
        } else {
            int i2 = this.mPhoneId;
            IMSLog.s(LOG_TAG, i2, "updateContactInfo: new capabilities update for uri " + imsUri);
            capabilities = new Capabilities(imsUri, str, str2, -1, str3);
            add(capabilities);
        }
        capabilities.setTimestamp(new Date());
        capabilities.setPhoneId(this.mPhoneId);
        if (z) {
            persistCachedUri(imsUri, capabilities);
        }
        persistToContactDB(capabilities, false);
    }

    public void persistCachedUri(ImsUri imsUri, Capabilities capabilities) {
        if (capabilities != null) {
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "persistCachedUri: uri = " + imsUri);
            this.mUriListToUpdate.put(imsUri, capabilities);
            tryPersist(false);
        }
    }

    public boolean isAvailable(long j) {
        return (j == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) || j == ((long) Capabilities.FEATURE_NON_RCS_USER) || j == ((long) Capabilities.FEATURE_NOT_UPDATED)) ? false : true;
    }

    public Capabilities get(int i) {
        for (Capabilities next : this.mCapabilitiesList.snapshot().values()) {
            if (next.getId() == ((long) i)) {
                IMSLog.s(LOG_TAG, "get: found. Id " + i);
                return next;
            }
        }
        Capabilities capabilities = this.mCapabilityStorage.get((long) i);
        if (capabilities != null) {
            this.mCapabilitiesList.put(capabilities.getUri(), capabilities);
        }
        return capabilities;
    }

    public Capabilities get(ImsUri imsUri) {
        if (imsUri == null) {
            return null;
        }
        Capabilities hasCapabilitiesCache = hasCapabilitiesCache(imsUri);
        if (hasCapabilitiesCache != null) {
            IMSLog.s(LOG_TAG, "get: found. Uri " + imsUri);
        } else {
            hasCapabilitiesCache = this.mCapabilityStorage.get(imsUri);
            if (hasCapabilitiesCache != null) {
                this.mCapabilitiesList.put(hasCapabilitiesCache.getUri(), hasCapabilitiesCache);
            }
        }
        return hasCapabilitiesCache;
    }

    public TreeMap<Integer, ImsUri> getCapabilitiesForPolling(int i, long j, long j2, long j3, boolean z) {
        return this.mCapabilityStorage.getCapabilitiesForPolling(i, j, j2, j3, z);
    }

    private Capabilities hasCapabilitiesCache(ImsUri imsUri) {
        return this.mCapabilitiesList.get(imsUri);
    }

    public void clear() {
        this.mCapabilitiesList.evictAll();
        resetCapabilityStorage();
    }

    public ConcurrentHashMap<ImsUri, Capabilities> getUpdatedUriList() {
        ConcurrentHashMap<ImsUri, Capabilities> concurrentHashMap = new ConcurrentHashMap<>();
        synchronized (this.mUriListToUpdate) {
            concurrentHashMap.putAll(this.mUriListToUpdate);
            this.mUriListToUpdate.clear();
        }
        return concurrentHashMap;
    }

    public List<ImsUri> getTrashedUriList() {
        List<ImsUri> list;
        synchronized (this.mUriListToDelete) {
            list = (List) this.mUriListToDelete.clone();
            this.mUriListToDelete.clear();
        }
        return list;
    }

    public String toString() {
        return "CapabilitiesCache: " + this.mCapabilitiesList.toString();
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, toString() + ", sizeInUse=" + this.mCapabilitiesList.size());
        if (Extensions.Build.IS_DEBUGGABLE) {
            for (Capabilities capabilities : this.mCapabilitiesList.snapshot().values()) {
                IMSLog.dump(LOG_TAG, capabilities.toString());
            }
        }
        IMSLog.decreaseIndent(LOG_TAG);
    }
}
