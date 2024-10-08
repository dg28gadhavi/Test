package com.sec.internal.ims.config;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.config.adapters.StorageAdapter;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigProvider extends ContentProvider {
    public static final String CONFIG_DB_NAME_PREFIX = "config_";
    private static final String LOG_TAG = "ConfigProvider";
    private static final int MAX_SERVER_COUNT;
    private static final int N_PARAMETER = 1;
    private static final IntentFilter SIM_STATE_CHANGED_INTENT_FILTER;
    static final Map<String, List<String>> mAppIdMap = new TreeMap();
    final Map<Integer, Map<String, Integer>> mAppIdServerIdMap = new ConcurrentHashMap();
    private Map<String, IReadConfigParam> mConfigTableMap = new ConcurrentHashMap();
    private final IStorageAdapter mEmptyStorage = new StorageAdapter();
    private UriMatcher mMatcher;
    final Map<Integer, Map<Integer, IStorageAdapter>> mServerIdStorageMap = new HashMap();

    private interface IReadConfigParam {
        Map<String, String> readParam(String str, int i);
    }

    static {
        Map<String, String> map = ConfigConstants.APPID_MAP;
        MAX_SERVER_COUNT = map.size();
        IntentFilter intentFilter = new IntentFilter();
        SIM_STATE_CHANGED_INTENT_FILTER = intentFilter;
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        for (Map.Entry next : map.entrySet()) {
            Map<String, List<String>> map2 = mAppIdMap;
            List list = map2.get(next.getValue());
            if (list == null) {
                list = new ArrayList();
            }
            list.add((String) next.getKey());
            map2.put((String) next.getValue(), list);
        }
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "ConfigProvider was created");
        initConfigTable();
        UriMatcher uriMatcher = new UriMatcher(0);
        this.mMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.autoconfigurationprovider", "parameter/*", 1);
        getContext().registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    String stringExtra = intent.getStringExtra("ss");
                    int intExtra = intent.getIntExtra(PhoneConstants.PHONE_KEY, 0);
                    if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(stringExtra)) {
                        Log.i("ConfigProvider[" + intExtra + "]", "SIM LOADED");
                        IStorageAdapter unused = ConfigProvider.this.initStorage(context, intExtra, (List<String>) null);
                    }
                }
            }
        }, SIM_STATE_CHANGED_INTENT_FILTER);
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        Map<String, String> map;
        String uri2 = uri.toString();
        Log.i(LOG_TAG, "query uri:" + IMSLog.checker(uri2));
        if (uri2.matches("^content://com.samsung.rcs.autoconfigurationprovider/[\\.\\w-_/*#]*")) {
            if (uri2.contains("root/*") || uri2.contains("root/application/*") || uri2.contains("content://com.samsung.rcs.autoconfigurationprovider/*")) {
                map = queryMultipleStorage(uri);
            } else {
                map = queryStorage(uri, getStorageByUri(uri));
            }
            if (map == null) {
                Log.i(LOG_TAG, "can not find readData from mStorage");
                return null;
            }
            String[] strArr3 = new String[map.keySet().size()];
            String[] strArr4 = new String[map.keySet().size()];
            int i = 0;
            for (Map.Entry next : map.entrySet()) {
                strArr3[i] = (String) next.getKey();
                strArr4[i] = (String) next.getValue();
                i++;
            }
            MatrixCursor matrixCursor = new MatrixCursor(strArr3);
            matrixCursor.addRow(strArr4);
            return matrixCursor;
        }
        throw new IllegalArgumentException(uri2 + " is not a correct AutoConfigurationProvider Uri");
    }

    private Map<String, String> queryStorage(Uri uri, IStorageAdapter iStorageAdapter) {
        String uri2 = uri.toString();
        Log.i(LOG_TAG, "queryStorage path " + uri2);
        if (iStorageAdapter.getState() != 1) {
            Log.i(LOG_TAG, "provider is not ready, return empty!");
            return null;
        } else if (this.mMatcher.match(uri) != 1) {
            return iStorageAdapter.readAll(uri2.replaceFirst(ConfigConstants.CONFIG_URI, "").replaceAll("#simslot\\d", ""));
        } else {
            return readDataByParam(uri2.replaceFirst("content://com.samsung.rcs.autoconfigurationprovider/parameter/", "").replaceAll("#simslot\\d", ""), UriUtil.getSimSlotFromUri(uri));
        }
    }

    private Map<String, String> queryMultipleStorage(Uri uri) {
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        Map map = this.mServerIdStorageMap.get(Integer.valueOf(simSlotFromUri));
        String uri2 = uri.toString();
        TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        Log.i(LOG_TAG, "queryMultipleStorage path " + uri2 + " map " + map);
        if (map == null) {
            return null;
        }
        String str = "root/application/*";
        if (uri2.contains(str) || uri2.contains("content://com.samsung.rcs.autoconfigurationprovider/*")) {
            for (IStorageAdapter queryStorage : map.values()) {
                treeMap.putAll(queryStorage(uri, queryStorage));
            }
        } else {
            treeMap.putAll(queryStorage(uri, (IStorageAdapter) map.get(0)));
            if (uri2.contains("root")) {
                str = "application/*";
            }
            Uri parse = Uri.parse(ImsUtil.getPathWithPhoneId(uri2.replaceAll("\\*#simslot\\d", "") + str, simSlotFromUri));
            for (int i = 1; i < map.keySet().size(); i++) {
                if (map.get(Integer.valueOf(i)) != null) {
                    treeMap.putAll(queryStorage(parse, (IStorageAdapter) map.get(Integer.valueOf(i))));
                }
            }
        }
        return treeMap;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        String uri2 = uri.toString();
        Log.i(LOG_TAG, "delete uri:" + IMSLog.checker(uri2));
        if (uri2.matches("^content://com.samsung.rcs.autoconfigurationprovider/[\\.\\w-_/#]*")) {
            IStorageAdapter storageByUri = getStorageByUri(uri);
            if (storageByUri.getState() != 1) {
                Log.i(LOG_TAG, "provider is not ready, return empty!");
                return 0;
            }
            String replaceAll = uri2.replaceFirst(ConfigConstants.CONFIG_URI, "").replaceAll("#simslot\\d", "");
            int delete = storageByUri.delete(replaceAll);
            getContext().getContentResolver().notifyChange(Uri.parse(replaceAll), (ContentObserver) null);
            return delete;
        }
        throw new IllegalArgumentException(uri2 + " is not a correct AutoConfigurationProvider Uri");
    }

    public String getType(Uri uri) {
        throw new UnsupportedOperationException("not supported");
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        String uri2 = uri.toString();
        Log.i(LOG_TAG, "insert uri:" + uri);
        Log.i(LOG_TAG, "insert uri:" + IMSLog.checker(uri2));
        if (uri2.matches("^content://com.samsung.rcs.autoconfigurationprovider/[\\.\\w-_/#]*")) {
            IStorageAdapter storageByUri = getStorageByUri(uri);
            if (storageByUri.getState() != 1) {
                Log.i(LOG_TAG, "provider is not ready, return empty!");
                return null;
            }
            HashMap hashMap = new HashMap();
            for (Map.Entry next : contentValues.valueSet()) {
                if (next.getValue() instanceof String) {
                    hashMap.put(uri2.replaceFirst(ConfigConstants.CONFIG_URI, "").replaceAll("#simslot\\d", "") + ((String) next.getKey()), (String) next.getValue());
                }
            }
            storageByUri.writeAll(hashMap);
            getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
            return uri;
        }
        throw new IllegalArgumentException(uri2 + " is not a correct AutoConfigurationProvider Uri");
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        insert(uri, contentValues);
        return contentValues.size();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0116  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0167  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.interfaces.ims.config.IStorageAdapter initStorage(android.content.Context r10, int r11, java.util.List<java.lang.String> r12) {
        /*
            r9 = this;
            java.lang.String r0 = getIdentityByPhoneId(r10, r11)
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 == 0) goto L_0x0028
            java.lang.String r10 = "ConfigProvider"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r0 = "initStorage: phone:"
            r12.append(r0)
            r12.append(r11)
            java.lang.String r11 = " no identity"
            r12.append(r11)
            java.lang.String r11 = r12.toString()
            android.util.Log.i(r10, r11)
            com.sec.internal.interfaces.ims.config.IStorageAdapter r9 = r9.mEmptyStorage
            return r9
        L_0x0028:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "config_"
            r1.append(r2)
            java.lang.String r2 = com.sec.internal.helper.HashManager.generateMD5(r0)
            r1.append(r2)
            java.lang.String r7 = r1.toString()
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, com.sec.internal.interfaces.ims.config.IStorageAdapter>> r1 = r9.mServerIdStorageMap
            monitor-enter(r1)
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, com.sec.internal.interfaces.ims.config.IStorageAdapter>> r2 = r9.mServerIdStorageMap     // Catch:{ all -> 0x0171 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0171 }
            java.lang.Object r2 = r2.get(r3)     // Catch:{ all -> 0x0171 }
            if (r2 != 0) goto L_0x005a
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, com.sec.internal.interfaces.ims.config.IStorageAdapter>> r2 = r9.mServerIdStorageMap     // Catch:{ all -> 0x0171 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0171 }
            java.util.concurrent.ConcurrentHashMap r4 = new java.util.concurrent.ConcurrentHashMap     // Catch:{ all -> 0x0171 }
            r4.<init>()     // Catch:{ all -> 0x0171 }
            r2.put(r3, r4)     // Catch:{ all -> 0x0171 }
        L_0x005a:
            com.sec.internal.interfaces.ims.config.IConfigModule r2 = com.sec.internal.ims.registry.ImsRegistry.getConfigModule()     // Catch:{ all -> 0x0171 }
            if (r2 == 0) goto L_0x0082
            com.sec.internal.interfaces.ims.config.IStorageAdapter r2 = r2.getStorage(r11)     // Catch:{ all -> 0x0171 }
            if (r2 == 0) goto L_0x0083
            java.lang.String r3 = "ConfigProvider"
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r4.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r5 = "initStorage: phone:"
            r4.append(r5)     // Catch:{ all -> 0x0171 }
            r4.append(r11)     // Catch:{ all -> 0x0171 }
            java.lang.String r5 = " get storage from configmodule"
            r4.append(r5)     // Catch:{ all -> 0x0171 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x0171 }
            goto L_0x0083
        L_0x0082:
            r2 = 0
        L_0x0083:
            r3 = 0
            if (r2 != 0) goto L_0x009c
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, com.sec.internal.interfaces.ims.config.IStorageAdapter>> r2 = r9.mServerIdStorageMap     // Catch:{ all -> 0x0171 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0171 }
            java.lang.Object r2 = r2.get(r4)     // Catch:{ all -> 0x0171 }
            java.util.Map r2 = (java.util.Map) r2     // Catch:{ all -> 0x0171 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0171 }
            java.lang.Object r2 = r2.get(r4)     // Catch:{ all -> 0x0171 }
            com.sec.internal.interfaces.ims.config.IStorageAdapter r2 = (com.sec.internal.interfaces.ims.config.IStorageAdapter) r2     // Catch:{ all -> 0x0171 }
        L_0x009c:
            r4 = 1
            if (r2 != 0) goto L_0x00c3
            java.lang.String r5 = "ConfigProvider"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r6.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = "initStorage: phone:"
            r6.append(r8)     // Catch:{ all -> 0x0171 }
            r6.append(r11)     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = " no storage :"
            r6.append(r8)     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = com.sec.internal.log.IMSLog.checker(r0)     // Catch:{ all -> 0x0171 }
            r6.append(r8)     // Catch:{ all -> 0x0171 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x0171 }
        L_0x00c1:
            r5 = r3
            goto L_0x0114
        L_0x00c3:
            java.lang.String r5 = r2.getIdentity()     // Catch:{ all -> 0x0171 }
            boolean r5 = r7.equals(r5)     // Catch:{ all -> 0x0171 }
            if (r5 != 0) goto L_0x0113
            java.lang.String r5 = "ConfigProvider"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r6.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = "initStorage: phone:"
            r6.append(r8)     // Catch:{ all -> 0x0171 }
            r6.append(r11)     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = " different identity :"
            r6.append(r8)     // Catch:{ all -> 0x0171 }
            java.lang.String r8 = com.sec.internal.log.IMSLog.checker(r0)     // Catch:{ all -> 0x0171 }
            r6.append(r8)     // Catch:{ all -> 0x0171 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.i(r5, r6)     // Catch:{ all -> 0x0171 }
            r2.close()     // Catch:{ all -> 0x0171 }
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, com.sec.internal.interfaces.ims.config.IStorageAdapter>> r5 = r9.mServerIdStorageMap     // Catch:{ all -> 0x0171 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0171 }
            r5.remove(r6)     // Catch:{ all -> 0x0171 }
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, com.sec.internal.interfaces.ims.config.IStorageAdapter>> r5 = r9.mServerIdStorageMap     // Catch:{ all -> 0x0171 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0171 }
            java.util.concurrent.ConcurrentHashMap r8 = new java.util.concurrent.ConcurrentHashMap     // Catch:{ all -> 0x0171 }
            r8.<init>()     // Catch:{ all -> 0x0171 }
            r5.put(r6, r8)     // Catch:{ all -> 0x0171 }
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.String, java.lang.Integer>> r5 = r9.mAppIdServerIdMap     // Catch:{ all -> 0x0171 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0171 }
            r5.remove(r6)     // Catch:{ all -> 0x0171 }
            goto L_0x00c1
        L_0x0113:
            r5 = r4
        L_0x0114:
            if (r5 != 0) goto L_0x012e
            com.sec.internal.ims.config.adapters.StorageAdapter r2 = new com.sec.internal.ims.config.adapters.StorageAdapter     // Catch:{ all -> 0x0171 }
            r2.<init>()     // Catch:{ all -> 0x0171 }
            java.util.Map<java.lang.Integer, java.util.Map<java.lang.Integer, com.sec.internal.interfaces.ims.config.IStorageAdapter>> r5 = r9.mServerIdStorageMap     // Catch:{ all -> 0x0171 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r11)     // Catch:{ all -> 0x0171 }
            java.lang.Object r5 = r5.get(r6)     // Catch:{ all -> 0x0171 }
            java.util.Map r5 = (java.util.Map) r5     // Catch:{ all -> 0x0171 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0171 }
            r5.put(r3, r2)     // Catch:{ all -> 0x0171 }
        L_0x012e:
            r8 = r2
            int r2 = r8.getState()     // Catch:{ all -> 0x0171 }
            if (r2 == r4) goto L_0x015e
            java.lang.String r2 = "ConfigProvider"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0171 }
            r3.<init>()     // Catch:{ all -> 0x0171 }
            java.lang.String r4 = "initStorage: phone:"
            r3.append(r4)     // Catch:{ all -> 0x0171 }
            r3.append(r11)     // Catch:{ all -> 0x0171 }
            java.lang.String r4 = " open storage :"
            r3.append(r4)     // Catch:{ all -> 0x0171 }
            java.lang.String r0 = com.sec.internal.log.IMSLog.checker(r0)     // Catch:{ all -> 0x0171 }
            r3.append(r0)     // Catch:{ all -> 0x0171 }
            java.lang.String r0 = r3.toString()     // Catch:{ all -> 0x0171 }
            android.util.Log.i(r2, r0)     // Catch:{ all -> 0x0171 }
            android.content.Context r0 = r9.getContext()     // Catch:{ all -> 0x0171 }
            r8.open(r0, r7, r11)     // Catch:{ all -> 0x0171 }
        L_0x015e:
            java.lang.String r0 = "root/access-control/server/0/app-id/0"
            java.lang.String r0 = r8.read(r0)     // Catch:{ all -> 0x0171 }
            if (r0 == 0) goto L_0x016f
            r3 = r9
            r4 = r10
            r5 = r11
            r6 = r12
            com.sec.internal.interfaces.ims.config.IStorageAdapter r8 = r3.initAdditionalStorage(r4, r5, r6, r7, r8)     // Catch:{ all -> 0x0171 }
        L_0x016f:
            monitor-exit(r1)     // Catch:{ all -> 0x0171 }
            return r8
        L_0x0171:
            r9 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0171 }
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.ConfigProvider.initStorage(android.content.Context, int, java.util.List):com.sec.internal.interfaces.ims.config.IStorageAdapter");
    }

    private IStorageAdapter initAdditionalStorage(Context context, int i, List<String> list, String str, IStorageAdapter iStorageAdapter) {
        Log.i(LOG_TAG, "initAdditionalStorage: phoneId: " + i);
        if (this.mAppIdServerIdMap.get(Integer.valueOf(i)) == null) {
            this.mAppIdServerIdMap.put(Integer.valueOf(i), new ConcurrentHashMap());
            for (int i2 = 0; i2 < MAX_SERVER_COUNT; i2++) {
                String read = iStorageAdapter.read("root/access-control/default/app-id/" + i2);
                if (read == null) {
                    break;
                }
                this.mAppIdServerIdMap.get(Integer.valueOf(i)).put(read, 0);
            }
            int i3 = 0;
            while (i3 < MAX_SERVER_COUNT) {
                for (int i4 = 0; i4 < MAX_SERVER_COUNT; i4++) {
                    String read2 = iStorageAdapter.read("root/access-control/server/" + i3 + "/app-id/" + i4);
                    if (read2 == null) {
                        break;
                    }
                    this.mAppIdServerIdMap.get(Integer.valueOf(i)).put(read2, Integer.valueOf(i3 + 1));
                }
                i3++;
                if (!this.mAppIdServerIdMap.get(Integer.valueOf(i)).containsValue(Integer.valueOf(i3))) {
                    break;
                }
                StorageAdapter storageAdapter = new StorageAdapter();
                storageAdapter.open(context, str + "_" + i3, i);
                this.mServerIdStorageMap.get(Integer.valueOf(i)).put(Integer.valueOf(i3), storageAdapter);
            }
            Log.i(LOG_TAG, "mAppIdServerIdMap " + this.mAppIdServerIdMap);
            Log.i(LOG_TAG, "mServerIdStorageMap " + this.mServerIdStorageMap);
        }
        if (list == null) {
            return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(i)).get(0);
        }
        for (String str2 : list) {
            IStorageAdapter iStorageAdapter2 = (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(i)).get((Integer) this.mAppIdServerIdMap.get(Integer.valueOf(i)).get(str2));
            if (iStorageAdapter2 != null) {
                return iStorageAdapter2;
            }
        }
        return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(i)).get(0);
    }

    private void setConfigTable(String str, IReadConfigParam iReadConfigParam) {
        this.mConfigTableMap.put(str.toLowerCase(Locale.US), iReadConfigParam);
    }

    private void initConfigTable() {
        ReadRootParm readRootParm = new ReadRootParm();
        setConfigTable("version", readRootParm);
        setConfigTable("validity", readRootParm);
        setConfigTable("token", readRootParm);
        ReadRootAppParm readRootAppParm = new ReadRootAppParm();
        setConfigTable(ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY, readRootAppParm);
        setConfigTable("address", readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.TIMER_T1, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.TIMER_T2, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.TIMER_T4, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.REG_RETRY_BASE_TIME, readRootAppParm);
        setConfigTable(ConfigConstants.ConfigTable.REG_RETRY_MAX_TIME, readRootAppParm);
        ReadExtParm readExtParm = new ReadExtParm();
        setConfigTable(ConfigConstants.ConfigTable.EXT_MAX_SIZE_IMAGE_SHARE, readExtParm);
        setConfigTable("maxtimevideoshare", readExtParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_Q_VALUE, readExtParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_INT_URL_FORMAT, readExtParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_NAT_URL_FORMAT, readExtParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_RCS_VOLTE_SINGLE_REGISTRATION, readExtParm);
        setConfigTable(ConfigConstants.ConfigTable.EXT_END_USER_CONF_REQID, readExtParm);
        setConfigTable("uuid_Value", readExtParm);
        ReadAppAuthParm readAppAuthParm = new ReadAppAuthParm();
        setConfigTable("UserName", readAppAuthParm);
        setConfigTable("UserPwd", readAppAuthParm);
        setConfigTable("realm", readAppAuthParm);
        ReadServiceParm readServiceParm = new ReadServiceParm();
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SUPPORTED_RCS_VERSIONS, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SUPPORTED_RCS_PROFILE_VERSIONS, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_STATE, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_DISABLED_STATE, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_PRESENCE_PRFL, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_GROUP_CHAT_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_FT_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SLM_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_GEOPULL_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_VS_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_IS_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_IPVOICECALL_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_IPVIDEOCALL_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_IR94_VIDEO_AUTH, readServiceParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_ALLOW_RCS_EXTENSIONS, readServiceParm);
        ReadDataOffParm readDataOffParm = new ReadDataOffParm();
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_RCS_MESSAGING, readDataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_FILE_TRANSFER, readDataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_SMSOIP, readDataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_MMS, readDataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_VOLTE, readDataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_IP_VIDEO, readDataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_PROVISIONING, readDataOffParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_SYNC, readDataOffParm);
        ReadCapDiscoveryParm readCapDiscoveryParm = new ReadCapDiscoveryParm();
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_DISABLE_INITIAL_SCAN, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_PERIOD, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE_PERIOD, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_CAPINFO_EXPIRY, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_CAP_DISC_COMMON_STACK, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_MAX_ENTRIES_IN_LIST, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, readCapDiscoveryParm);
        setConfigTable(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE, readCapDiscoveryParm);
        ReadPresenceParm readPresenceParm = new ReadPresenceParm();
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_PUBLISH_TIMER, readPresenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH, readPresenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST, readPresenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_RLS_URI, readPresenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_LOC_INFO_MAX_VALID_TIME, readPresenceParm);
        setConfigTable(ConfigConstants.ConfigTable.PRESENCE_CLIENT_OBJ_DATALIMIT, readPresenceParm);
        ReadImFtParm readImFtParm = new ReadImFtParm();
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_MSG_TECH, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_CAP_ALWAYS_ON, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_WARN_SF, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_GROUP_CHAT_FULL_STAND_FWD, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_GROUP_CHAT_ONLY_F_STAND_FWD, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_SMS_FALLBACK_AUTH, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_CAP_NON_RCS, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_WARN_IW, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_AUT_ACCEPT, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_IM_SESSION_START, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FIRST_MSG_INVITE, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_TIMER_IDLE, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_CONCURRENT_SESSION, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MULTIMEDIA_CHAT, readImFtParm);
        setConfigTable("MaxSize", readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_1, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_M, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_WARN_SIZE, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR_INCOMING, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_THUMB, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_ST_AND_FW_ENABLED, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_CAP_ALWAYS_ON, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_DEFAULT_MECH, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_PRES_SRV_CAP, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_DEFERRED_MSG_FUNC_URI, readImFtParm);
        setConfigTable("max_adhoc_group_size", readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_CONF_FCTY_URI, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXPLODER_URI, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MASS_FCTY_URI, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_FT_WARN_SIZE, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_MAX_SIZE_FILE_TR, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_FT_HTTP_MAX_SIZE_FILE_TR_INCOMING, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_MAX_ADHOC_OPEN_GROUP_SIZE, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI, readImFtParm);
        setConfigTable(ConfigConstants.ConfigTable.IM_EXT_MAX_IMDN_AGGREGATION, readImFtParm);
        ReadEnrichedCallingParm readEnrichedCallingParm = new ReadEnrichedCallingParm();
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, readEnrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, readEnrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, readEnrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, readEnrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_VBC_AUTH, readEnrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_CONTENT_SHARE, readEnrichedCallingParm);
        setConfigTable(ConfigConstants.ConfigTable.DATAOFF_PRE_AND_POST_CALL, readEnrichedCallingParm);
        ReadStandalonMsgParm readStandalonMsgParm = new ReadStandalonMsgParm();
        setConfigTable(ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE, readStandalonMsgParm);
        setConfigTable("MaxSize", readStandalonMsgParm);
        setConfigTable(ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE, readStandalonMsgParm);
        ReadCpmMessageStoreParm readCpmMessageStoreParm = new ReadCpmMessageStoreParm();
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_URL, readCpmMessageStoreParm);
        setConfigTable("AuthProt", readCpmMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_NAME, readCpmMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_PWD, readCpmMessageStoreParm);
        setConfigTable("EventRpting", readCpmMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_AUTH_ARCHIVE, readCpmMessageStoreParm);
        setConfigTable("SyncTimer", readCpmMessageStoreParm);
        setConfigTable("DataConnectionSyncTimer", readCpmMessageStoreParm);
        setConfigTable("SMSStore", readCpmMessageStoreParm);
        setConfigTable("MMSStore", readCpmMessageStoreParm);
        ReadOtherParm readOtherParm = new ReadOtherParm();
        setConfigTable(ConfigConstants.ConfigTable.OTHER_WARN_SIZE_IMAGE_SHARE, readOtherParm);
        setConfigTable("maxtimevideoshare", readOtherParm);
        setConfigTable(ConfigConstants.ConfigTable.OTHER_EXTENSIONS_MAX_MSRP_SIZE, readOtherParm);
        setConfigTable(ConfigConstants.ConfigTable.OTHER_CALL_COMPOSER_TIMER_IDLE, readOtherParm);
        setConfigTable(ConfigConstants.ConfigTable.XDMS_XCAP_ROOT_URI, new ReadXdmsParm());
        ReadTransportProtoParm readTransportProtoParm = new ReadTransportProtoParm();
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_MEDIA, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_SIGNALLING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_RT_MEDIA, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_SIGNALLING_ROAMING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_MEDIA_ROAMING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_PS_RT_MEDIA_ROAMING, readTransportProtoParm);
        setConfigTable(ConfigConstants.ConfigTable.TRANSPORTPROTO_WIFI_RT_MEDIA, readTransportProtoParm);
        ReadPublicAccountParm readPublicAccountParm = new ReadPublicAccountParm();
        setConfigTable(ConfigConstants.ConfigTable.PUBLIC_ACCOUNT_ADDR, readPublicAccountParm);
        setConfigTable(ConfigConstants.ConfigTable.PUBLIC_ACCOUNT_ADDRTYPE, readPublicAccountParm);
        ReadPersonalProfileParm readPersonalProfileParm = new ReadPersonalProfileParm();
        setConfigTable(ConfigConstants.ConfigTable.PERSONAL_PROFILE_ADDR, readPersonalProfileParm);
        setConfigTable(ConfigConstants.ConfigTable.PERSONAL_PROFILE_ADDRTYPE, readPersonalProfileParm);
        ReadUxParm readUxParm = new ReadUxParm();
        setConfigTable(ConfigConstants.ConfigTable.UX_MESSAGING_UX, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_USER_ALIAS_AUTH, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_SPAM_NOTIFICATION_TEXT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_TOKEN_LINK_NOTIFICATION_TEXT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_UNAVAILABLE_ENDPOINT_TEXT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_VIDEO_AND_ENCALL_UX, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_IR51_SWITCH_UX, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_FT_FB_DEFAULT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_CALL_LOG_BEARER_DIFFER, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_ALLOW_ENRICHED_CHATBOT_SEARCH_DEFAULT, readUxParm);
        setConfigTable(ConfigConstants.ConfigTable.UX_REALTIME_USER_ALIAS_AUTH, readUxParm);
        ReadClientControlParm readClientControlParm = new ReadClientControlParm();
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY, readClientControlParm);
        setConfigTable(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_HTTP_CAP_ALWAYS_ON, readClientControlParm);
        ReadMsisdnParm readMsisdnParm = new ReadMsisdnParm();
        setConfigTable(ConfigConstants.ConfigTable.MSISDN_SKIP_COUNT, readMsisdnParm);
        setConfigTable(ConfigConstants.ConfigTable.MSISDN_MSGUI_DISPLAY, readMsisdnParm);
        ReadChatbotParm readChatbotParm = new ReadChatbotParm();
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_CHATBOTDIRECTORY, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_BOTINFOFQDNROOT, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_CHATBOTBLACKLIST, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_MSGHISTORYSELECTABLE, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_SPECIFIC_CHATBOTS_LIST, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_IDENTITY_IN_ENRICHED_SEARCH, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_PRIVACY_DISABLE, readChatbotParm);
        setConfigTable(ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH, readChatbotParm);
        ReadMessageStoreParm readMessageStoreParm = new ReadMessageStoreParm();
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_URL, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_NOTIF_URL, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_AUTH, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_USER_NAME, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.MESSAGESTORE_MSG_STORE_USER_PWD, readMessageStoreParm);
        setConfigTable(ConfigConstants.ConfigTable.PLUGINS_CATALOGURI, new ReadPluginsParm());
        ReadServiceExtParm readServiceExtParm = new ReadServiceExtParm();
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_STATE, readServiceExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICES_RCS_DISABLED_STATE, readServiceExtParm);
        ReadServiceProviderExtParm readServiceProviderExtParm = new ReadServiceProviderExtParm();
        setConfigTable(ConfigConstants.ConfigTable.SPG_URL, readServiceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SPG_PARAMS_URL, readServiceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.NMS_URL, readServiceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.NC_URL, readServiceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_FTHTTPGROUPCHAT, readServiceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_USER_NAME, readServiceProviderExtParm);
        setConfigTable(ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_PASSWORD, readServiceProviderExtParm);
    }

    private Map<String, String> readDataByParam(String str, int i) {
        HashMap hashMap = new HashMap();
        if (str == null || str.isEmpty()) {
            return hashMap;
        }
        Map<String, IReadConfigParam> map = this.mConfigTableMap;
        Locale locale = Locale.US;
        IReadConfigParam iReadConfigParam = map.get(str.toLowerCase(locale));
        return iReadConfigParam != null ? iReadConfigParam.readParam(str.toLowerCase(locale), i) : hashMap;
    }

    class ReadRootParm implements IReadConfigParam {
        ReadRootParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            HashMap hashMap = new HashMap();
            if ("version".equalsIgnoreCase(str) || "validity".equalsIgnoreCase(str)) {
                IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.VERS_PATH, i);
                return storageByPath.readAll(ConfigConstants.ConfigPath.VERS_PATH + str);
            } else if (!"token".equalsIgnoreCase(str)) {
                return hashMap;
            } else {
                IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TOKEN_PATH, i);
                return storageByPath2.readAll(ConfigConstants.ConfigPath.TOKEN_PATH + str);
            }
        }
    }

    static Map<String, String> getPublicUserIdentities(String str, IStorageAdapter iStorageAdapter) {
        Map<String, String> map;
        HashMap hashMap = new HashMap();
        String lowerCase = str.toLowerCase(Locale.US);
        int i = 0;
        while (true) {
            lowerCase.hashCode();
            char c = 65535;
            switch (lowerCase.hashCode()) {
                case -635263783:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_PATH)) {
                        c = 0;
                        break;
                    }
                    break;
                case 952202542:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1002817916:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP10_PATH)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    map = iStorageAdapter.readAll(str + ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY + "/" + i);
                    break;
                case 1:
                    map = iStorageAdapter.readAll(str + "node/" + i + "/" + ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY);
                    break;
                case 2:
                    map = iStorageAdapter.readAll(str + ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY + (i + 1));
                    break;
                default:
                    map = null;
                    break;
            }
            if (map == null || map.isEmpty()) {
                return hashMap;
            }
            hashMap.putAll(map);
            i++;
        }
        return hashMap;
    }

    static Map<String, String> getLboPcscfAddresses(String str, String str2, IStorageAdapter iStorageAdapter) {
        Map<String, String> map;
        HashMap hashMap = new HashMap();
        Locale locale = Locale.US;
        String lowerCase = str.toLowerCase(locale);
        String lowerCase2 = str2.toLowerCase(locale);
        if (!lowerCase2.equals("address") && !lowerCase2.equals(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE)) {
            return hashMap;
        }
        int i = 0;
        while (true) {
            lowerCase.hashCode();
            char c = 65535;
            switch (lowerCase.hashCode()) {
                case -1083842858:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP20_PATH)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1820524985:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_PATH)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1894509373:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP10_PATH)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1916188420:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                case 2:
                    map = iStorageAdapter.readAll(str + lowerCase2 + (i + 1));
                    break;
                case 1:
                    map = iStorageAdapter.readAll(str + lowerCase2 + "/" + i);
                    break;
                case 3:
                    map = iStorageAdapter.readAll(str + "node/" + i + "/" + lowerCase2);
                    break;
                default:
                    map = null;
                    break;
            }
            if (map == null || map.isEmpty()) {
                return hashMap;
            }
            hashMap.putAll(map);
            i++;
        }
        return hashMap;
    }

    /* access modifiers changed from: private */
    public Map<String, String> getPublicUserIdentities(IStorageAdapter iStorageAdapter) {
        Map<String, String> publicUserIdentities = getPublicUserIdentities(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_PATH, iStorageAdapter);
        if (publicUserIdentities.isEmpty()) {
            publicUserIdentities = getPublicUserIdentities(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP10_PATH, iStorageAdapter);
        }
        return !publicUserIdentities.isEmpty() ? publicUserIdentities : getPublicUserIdentities(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH, iStorageAdapter);
    }

    /* access modifiers changed from: private */
    public Map<String, String> getLboPcscfAddresses(String str, IStorageAdapter iStorageAdapter) {
        Map<String, String> lboPcscfAddresses = getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_PATH, str, iStorageAdapter);
        if (lboPcscfAddresses.isEmpty()) {
            lboPcscfAddresses = getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP10_PATH, str, iStorageAdapter);
        }
        if (lboPcscfAddresses.isEmpty()) {
            lboPcscfAddresses = getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH, str, iStorageAdapter);
        }
        return !lboPcscfAddresses.isEmpty() ? lboPcscfAddresses : getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP20_PATH, str, iStorageAdapter);
    }

    class ReadRootAppParm implements IReadConfigParam {
        ReadRootAppParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            HashMap hashMap = new HashMap();
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH, i);
            if (ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.TIMER_T1.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.TIMER_T2.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.TIMER_T4.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.REG_RETRY_BASE_TIME.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.REG_RETRY_MAX_TIME.equalsIgnoreCase(str)) {
                Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH + str);
                if (readAll != null && !readAll.isEmpty()) {
                    return readAll;
                }
                return storageByPath.readAll(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_UP20_PATH + str);
            } else if (ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY.equalsIgnoreCase(str)) {
                return ConfigProvider.this.getPublicUserIdentities(storageByPath);
            } else {
                if ("address".equalsIgnoreCase(str) || ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE.equalsIgnoreCase(str)) {
                    return ConfigProvider.this.getLboPcscfAddresses(str, storageByPath);
                }
                return hashMap;
            }
        }
    }

    class ReadExtParm implements IReadConfigParam {
        ReadExtParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath("root/application/0/3gpp_ims/ext/gsma/", i);
            return storageByPath2.readAll("root/application/0/3gpp_ims/ext/gsma/" + str);
        }
    }

    class ReadAppAuthParm implements IReadConfigParam {
        ReadAppAuthParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath("root/application/0/3gpp_ims/ext/gsma/", i);
            return storageByPath2.readAll("root/application/0/3gpp_ims/ext/gsma/" + str);
        }
    }

    class ReadServiceParm implements IReadConfigParam {
        ReadServiceParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH + str);
        }
    }

    static Map<String, String> getCapAllowedPrefixes(String str, IStorageAdapter iStorageAdapter) {
        HashMap hashMap = new HashMap();
        Map<String, String> readAll = iStorageAdapter.readAll(str + 1);
        int i = 1;
        while (readAll != null && !readAll.isEmpty()) {
            hashMap.putAll(readAll);
            i++;
            readAll = iStorageAdapter.readAll(str + i);
        }
        return hashMap;
    }

    class ReadCapDiscoveryParm implements IReadConfigParam {
        ReadCapDiscoveryParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, i);
            if (ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES.equalsIgnoreCase(str)) {
                return ConfigProvider.getCapAllowedPrefixes(ConfigConstants.ConfigPath.CAPDISCOVERY_ALLOWED_PREFIXES_PATH, storageByPath);
            }
            if (ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE.equalsIgnoreCase(str)) {
                return storageByPath.readAll(ConfigConstants.ConfigPath.CAPDISCOVERY_EXT_JOYN_PATH + str);
            }
            return storageByPath.readAll(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadPresenceParm implements IReadConfigParam {
        ReadPresenceParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH, i);
            if (ConfigConstants.ConfigTable.PRESENCE_LOC_INFO_MAX_VALID_TIME.equalsIgnoreCase(str)) {
                return storageByPath.readAll(ConfigConstants.ConfigPath.PRESENCE_LOCATION_PATH + str);
            }
            return storageByPath.readAll(ConfigConstants.ConfigPath.PRESENCE_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadImFtParm implements IReadConfigParam {
        ReadImFtParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            if (ConfigProvider.this.isImExtraParam(str)) {
                return storageByPath.readAll(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH + str);
            } else if (ConfigProvider.this.isChatParam(str)) {
                return storageByPath.readAll(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + str);
            } else if (ConfigProvider.this.isFtExtraParam(str)) {
                return storageByPath.readAll(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + str);
            } else if (!ConfigConstants.ConfigTable.IM_EXPLODER_URI.equalsIgnoreCase(str)) {
                return readAll;
            } else {
                return storageByPath.readAll(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + str);
            }
        }
    }

    class ReadEnrichedCallingParm implements IReadConfigParam {
        ReadEnrichedCallingParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            Map<String, String> map;
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH, i);
            if (ConfigConstants.ConfigTable.DATAOFF_CONTENT_SHARE.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.DATAOFF_PRE_AND_POST_CALL.equalsIgnoreCase(str)) {
                map = storageByPath.readAll(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH + str);
            } else {
                map = storageByPath.readAll(ConfigConstants.ConfigPath.SERVICE_CHARACTERISTIC_PATH + str);
            }
            if (map != null && !map.isEmpty()) {
                return map;
            }
            IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.ENRICHED_CALLING_CHARACTERISTIC_PATH, i);
            return storageByPath2.readAll(ConfigConstants.ConfigPath.ENRICHED_CALLING_CHARACTERISTIC_PATH + str);
        }
    }

    /* access modifiers changed from: private */
    public boolean isImExtraParam(String str) {
        return ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_EXT_MAX_IMDN_AGGREGATION.equalsIgnoreCase(str);
    }

    /* access modifiers changed from: private */
    public boolean isChatParam(String str) {
        return ConfigConstants.ConfigTable.IM_AUT_ACCEPT.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_CONF_FCTY_URI.equalsIgnoreCase(str) || "max_adhoc_group_size".equalsIgnoreCase(str) || "MaxSize".equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_TIMER_IDLE.equalsIgnoreCase(str);
    }

    /* access modifiers changed from: private */
    public boolean isFtExtraParam(String str) {
        return ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_WARN_SIZE.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR.equalsIgnoreCase(str);
    }

    class ReadStandalonMsgParm implements IReadConfigParam {
        ReadStandalonMsgParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SLM_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.SLM_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            if (!ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE.equalsIgnoreCase(str) && !"MaxSize".equalsIgnoreCase(str) && !ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE.equalsIgnoreCase(str)) {
                return readAll;
            }
            IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH, i);
            return storageByPath2.readAll(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadCpmMessageStoreParm implements IReadConfigParam {
        ReadCpmMessageStoreParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.CPM_MESSAGESTORE_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            if (ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_NAME.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_PWD.equalsIgnoreCase(str)) {
                return storageByPath.readAll(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH + str);
            } else if (!"EventRpting".equalsIgnoreCase(str) && !ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_AUTH_ARCHIVE.equalsIgnoreCase(str) && !"SMSStore".equalsIgnoreCase(str) && !"MMSStore".equalsIgnoreCase(str)) {
                return readAll;
            } else {
                IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, i);
                return storageByPath2.readAll(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH + str);
            }
        }
    }

    class ReadOtherParm implements IReadConfigParam {
        ReadOtherParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.OTHER_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadXdmsParm implements IReadConfigParam {
        ReadXdmsParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.XDMS_CHARACTERISTIC_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.XDMS_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadTransportProtoParm implements IReadConfigParam {
        ReadTransportProtoParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP10_PATH, i);
            Map<String, String> readAll2 = storageByPath2.readAll(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP10_PATH + str);
            if (readAll2 != null && !readAll2.isEmpty()) {
                return readAll2;
            }
            IStorageAdapter storageByPath3 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP20_PATH, i);
            return storageByPath3.readAll(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP20_PATH + str);
        }
    }

    class ReadPublicAccountParm implements IReadConfigParam {
        ReadPublicAccountParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath("root/application/1/", i);
            return storageByPath.readAll("root/application/1/" + str);
        }
    }

    class ReadPersonalProfileParm implements IReadConfigParam {
        ReadPersonalProfileParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath("root/", i);
            return storageByPath.readAll("root/" + str);
        }
    }

    class ReadUxParm implements IReadConfigParam {
        ReadUxParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.UX_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            if (!ConfigConstants.ConfigTable.UX_MESSAGING_UX.equalsIgnoreCase(str) && !ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT.equalsIgnoreCase(str)) {
                return readAll;
            }
            IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.JOYN_UX_PATH, i);
            return storageByPath2.readAll(ConfigConstants.ConfigPath.JOYN_UX_PATH + str);
        }
    }

    class ReadClientControlParm implements IReadConfigParam {
        ReadClientControlParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH, i);
            Map<String, String> readAll = storageByPath.readAll(ConfigConstants.ConfigPath.CLIENT_CONTROL_CHARACTERISTIC_PATH + str);
            if (readAll != null && !readAll.isEmpty()) {
                return readAll;
            }
            if (ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER.equalsIgnoreCase(str)) {
                IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH, i);
                Map<String, String> readAll2 = storageByPath2.readAll(ConfigConstants.ConfigPath.JOYN_MESSAGING_CHARACTERISTIC_PATH + str);
                if (readAll2 != null && !readAll2.isEmpty()) {
                    return readAll2;
                }
                return storageByPath2.readAll(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + str);
            } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY.equalsIgnoreCase(str)) {
                IStorageAdapter storageByPath3 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH, i);
                return storageByPath3.readAll(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH + str);
            } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(str)) {
                IStorageAdapter storageByPath4 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MESSAGING_CHARACTERISTIC_PATH, i);
                return storageByPath4.readAll(ConfigConstants.ConfigPath.MESSAGING_CHARACTERISTIC_PATH + str);
            } else if (!ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(str)) {
                return readAll;
            } else {
                IStorageAdapter storageByPath5 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH, i);
                return storageByPath5.readAll(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + str);
            }
        }
    }

    class ReadMsisdnParm implements IReadConfigParam {
        ReadMsisdnParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MSISDN_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.MSISDN_PATH + str);
        }
    }

    class ReadChatbotParm implements IReadConfigParam {
        ReadChatbotParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.CHATBOT_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadMessageStoreParm implements IReadConfigParam {
        ReadMessageStoreParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadPluginsParm implements IReadConfigParam {
        ReadPluginsParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.PLUGINS_CHARACTERISTIC_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.PLUGINS_CHARACTERISTIC_PATH + str);
        }
    }

    class ReadDataOffParm implements IReadConfigParam {
        ReadDataOffParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.SERVICE_EXT_DATAOFF_PATH + str);
        }
    }

    class ReadServiceExtParm implements IReadConfigParam {
        ReadServiceExtParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_PATH, i);
            return storageByPath.readAll(ConfigConstants.ConfigPath.SERVICE_PATH + str);
        }
    }

    class ReadServiceProviderExtParm implements IReadConfigParam {
        ReadServiceProviderExtParm() {
        }

        public Map<String, String> readParam(String str, int i) {
            if (ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_FTHTTPGROUPCHAT.equalsIgnoreCase(str)) {
                IStorageAdapter storageByPath = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH, i);
                return storageByPath.readAll(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH + str);
            } else if (ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_USER_NAME.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.SERVICEPROVIDEREXT_CHATBOT_PASSWORD.equalsIgnoreCase(str)) {
                IStorageAdapter storageByPath2 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHATBOT_PATH, i);
                return storageByPath2.readAll(ConfigConstants.ConfigPath.SERVICEPROVIDEREXT_CHARACTERISTIC_PATH + str);
            } else {
                IStorageAdapter storageByPath3 = ConfigProvider.this.getStorageByPath(ConfigConstants.ConfigPath.SERVICE_PROVIDER_EXT_PATH, i);
                return storageByPath3.readAll(ConfigConstants.ConfigPath.SERVICE_PROVIDER_EXT_PATH + str);
            }
        }
    }

    static String getIdentityByPhoneId(Context context, int i) {
        if (context.checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            return null;
        }
        return ConfigUtil.buildIdentity(context, i);
    }

    /* access modifiers changed from: package-private */
    public IStorageAdapter getStorageByUri(Uri uri) {
        List list;
        int simSlotFromUri = UriUtil.getSimSlotFromUri(uri);
        String uri2 = uri.toString();
        if (uri2.contains(AECNamespace.Path.APPLICATION)) {
            String substring = uri2.substring(69);
            if (substring.indexOf(47) != -1) {
                list = mAppIdMap.get(substring.substring(0, substring.indexOf(47)));
            } else {
                list = mAppIdMap.get("0");
            }
        } else {
            list = null;
        }
        return initStorage(getContext(), simSlotFromUri, list);
    }

    /* access modifiers changed from: package-private */
    public IStorageAdapter getStorageByPath(String str, int i) {
        IStorageAdapter iStorageAdapter;
        List<String> list = null;
        if (str.contains(AECNamespace.Path.APPLICATION)) {
            String substring = str.substring(17);
            if (substring.indexOf(47) != -1) {
                list = mAppIdMap.get(substring.substring(0, substring.indexOf(47)));
            }
        }
        if (str.contains(ConfigConstants.ConfigPath.VERS_PATH) && this.mAppIdServerIdMap.get(Integer.valueOf(i)) != null) {
            list = mAppIdMap.get("0");
        }
        if (list == null || this.mAppIdServerIdMap.get(Integer.valueOf(i)) == null) {
            return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(i)).get(0);
        }
        for (String str2 : list) {
            Integer num = (Integer) this.mAppIdServerIdMap.get(Integer.valueOf(i)).get(str2);
            if (num != null && (iStorageAdapter = (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(i)).get(num)) != null) {
                return iStorageAdapter;
            }
        }
        return (IStorageAdapter) this.mServerIdStorageMap.get(Integer.valueOf(i)).get(0);
    }
}
