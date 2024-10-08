package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.tapi.UserConsentProviderContract;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.persistence.UserConsentPersistence;
import com.sec.internal.ims.servicemodules.euc.persistence.UserConsentPersistenceNotifier;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.tapi.IUserConsentListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class UserConsentProvider extends ContentProvider {
    private static final String LOG_TAG = UserConsentProvider.class.getSimpleName();
    private static final UriMatcher URI_MATCHER;
    private static final int USER_CONSENT_LIST = 1;
    private UserConsentPersistence mPersistence = null;
    private UserConsentPersistenceNotifier mUserConsentPersistenceNotifier;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        URI_MATCHER = uriMatcher;
        uriMatcher.addURI(UserConsentProviderContract.AUTHORITY, "#", 1);
    }

    public boolean onCreate() {
        UserConsentPersistenceNotifier instance = UserConsentPersistenceNotifier.getInstance();
        this.mUserConsentPersistenceNotifier = instance;
        instance.setListener(new IUserConsentListener() {
            public void notifyChanged(int i) {
                if (UserConsentProvider.this.getContext() != null) {
                    UserConsentProvider.this.getContext().getContentResolver().notifyChange(Uri.withAppendedPath(UserConsentProviderContract.CONTENT_URI, Integer.toString(i)), (ContentObserver) null);
                }
            }
        });
        this.mPersistence = new UserConsentPersistence(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3;
        boolean z;
        String str4 = LOG_TAG;
        Log.d(str4, "query(Uri, String[], String, String[], String) uri: " + uri);
        if (URI_MATCHER.match(uri) == 1) {
            int parseInt = Integer.parseInt(uri.getLastPathSegment());
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(parseInt);
            if (simManagerFromSimSlot != null) {
                str3 = simManagerFromSimSlot.getImsi();
                z = simManagerFromSimSlot.isSimAvailable();
            } else {
                str3 = null;
                z = false;
            }
            IMSLog.s(str4, "query: slot=" + parseInt + ", imsi=" + str3 + ", isSimAvailable=" + z);
            if (str3 == null || str3.isEmpty() || !z) {
                return null;
            }
            ArrayList arrayList = new ArrayList();
            for (String str5 : strArr2) {
                str5.hashCode();
                char c = 65535;
                switch (str5.hashCode()) {
                    case -1408244262:
                        if (str5.equals(UserConsentProviderContract.EUCR_ACKNOWLEDGEMENT_LABEL)) {
                            c = 0;
                            break;
                        }
                        break;
                    case -1382453013:
                        if (str5.equals(UserConsentProviderContract.EUCR_NOTIFICATION_LABEL)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1105400420:
                        if (str5.equals(UserConsentProviderContract.EUCR_VOLATILE_LABEL)) {
                            c = 2;
                            break;
                        }
                        break;
                    case 2139685:
                        if (str5.equals(UserConsentProviderContract.EULA_LABEL)) {
                            c = 3;
                            break;
                        }
                        break;
                    case 997554839:
                        if (str5.equals(UserConsentProviderContract.EUCR_PERSISTENT_LABEL)) {
                            c = 4;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        arrayList.add(EucType.ACKNOWLEDGEMENT);
                        break;
                    case 1:
                        arrayList.add(EucType.NOTIFICATION);
                        break;
                    case 2:
                        arrayList.add(EucType.VOLATILE);
                        break;
                    case 3:
                        arrayList.add(EucType.EULA);
                        break;
                    case 4:
                        arrayList.add(EucType.PERSISTENT);
                        break;
                }
            }
            UserConsentPersistence userConsentPersistence = this.mPersistence;
            if (str2 == null) {
                str2 = UserConsentProviderContract.UserConsentList.SORT_ORDER_DEFAULT;
            }
            return userConsentPersistence.getEucList(str, arrayList, str2, str3);
        }
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    public int delete(Uri uri, String str, String[] strArr) {
        if (URI_MATCHER.match(uri) == 1) {
            int parseInt = Integer.parseInt(uri.getLastPathSegment());
            int removeEuc = this.mPersistence.removeEuc(str, strArr);
            this.mUserConsentPersistenceNotifier.notifyListener(parseInt);
            return removeEuc;
        }
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException();
    }
}
