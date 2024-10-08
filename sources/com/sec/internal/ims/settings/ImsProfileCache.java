package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.ims.settings.ImsProfile;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImsProfileCache {
    private final String BUILD_INFO = "buildinfo";
    private final String TAG = ImsProfileCache.class.getSimpleName();
    private ImsAutoUpdate mAutoUpdate;
    private final Context mContext;
    private boolean mIsMvno;
    private String mMnoName;
    private int mNextId = 1;
    private String mPMnoName;
    private int mPhoneId = -1;
    private ImsProfile mProfileGlobalGC = null;
    private final Map<Integer, ImsProfile> mProfileMap = new ArrayMap();

    public ImsProfileCache(Context context, String str, int i) {
        this.mContext = context;
        this.mMnoName = str;
        int indexOf = str.indexOf(Mno.MVNO_DELIMITER);
        if (indexOf != -1) {
            this.mIsMvno = true;
            this.mPMnoName = this.mMnoName.substring(0, indexOf);
        } else {
            this.mIsMvno = false;
            this.mPMnoName = "";
        }
        this.mAutoUpdate = ImsAutoUpdate.getInstance(context, i);
        this.mPhoneId = i;
    }

    private boolean isVersionUpdated() {
        String str = SemSystemProperties.get("ro.build.PDA", "");
        String str2 = SemSystemProperties.get("ril.official_cscver", "");
        String string = ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, "buildinfo", "");
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("_");
        sb.append(str2);
        return !string.equals(sb.toString());
    }

    private void saveBuildInfo() {
        String str = SemSystemProperties.get("ro.build.PDA", "");
        String str2 = SemSystemProperties.get("ril.official_cscver", "");
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, "buildinfo", str + "_" + str2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x012d, code lost:
        r0 = r6.mProfileMap;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x012f, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r6.mProfileMap.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0137, code lost:
        if (r6.mIsMvno == false) goto L_0x0152;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x013d, code lost:
        if (r7.isEmpty() == false) goto L_0x014c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x013f, code lost:
        android.util.Log.e(r6.TAG, "load: This mno is MVNO but no profile defined. Use Parent profiles");
        r6.mProfileMap.putAll(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x014c, code lost:
        r6.mProfileMap.putAll(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0152, code lost:
        r6.mProfileMap.putAll(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0157, code lost:
        monitor-exit(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void load(boolean r7) {
        /*
            r6 = this;
            int r0 = r6.mPhoneId
            android.content.Context r1 = r6.mContext
            java.lang.String r2 = "imsprofile"
            r3 = 0
            android.content.SharedPreferences r0 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r0, r1, r2, r3, r3)
            java.util.Map r0 = r0.getAll()
            java.lang.String r1 = r6.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "load: mMnoName: "
            r2.append(r3)
            java.lang.String r3 = r6.mMnoName
            r2.append(r3)
            java.lang.String r3 = ", mPMnoName: "
            r2.append(r3)
            java.lang.String r3 = r6.mPMnoName
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.settings.ImsAutoUpdate r1 = r6.mAutoUpdate
            boolean r1 = r1.isUpdateNeeded()
            if (r1 != 0) goto L_0x015f
            boolean r1 = r0.isEmpty()
            if (r1 != 0) goto L_0x015f
            boolean r1 = r6.isVersionUpdated()
            if (r1 != 0) goto L_0x015f
            if (r7 == 0) goto L_0x0049
            goto L_0x015f
        L_0x0049:
            java.lang.String r7 = "buildinfo"
            r0.remove(r7)
            android.util.ArrayMap r7 = new android.util.ArrayMap
            r7.<init>()
            android.util.ArrayMap r1 = new android.util.ArrayMap
            r1.<init>()
            monitor-enter(r6)
            java.util.Collection r0 = r0.values()     // Catch:{ all -> 0x015c }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ all -> 0x015c }
        L_0x0061:
            boolean r2 = r0.hasNext()     // Catch:{ all -> 0x015c }
            if (r2 == 0) goto L_0x010a
            java.lang.Object r2 = r0.next()     // Catch:{ all -> 0x015c }
            java.lang.String r2 = (java.lang.String) r2     // Catch:{ all -> 0x015c }
            com.sec.ims.settings.ImsProfile r3 = new com.sec.ims.settings.ImsProfile     // Catch:{ all -> 0x015c }
            r3.<init>(r2)     // Catch:{ all -> 0x015c }
            java.lang.String r2 = r3.getName()     // Catch:{ all -> 0x015c }
            boolean r2 = android.text.TextUtils.isEmpty(r2)     // Catch:{ all -> 0x015c }
            if (r2 == 0) goto L_0x0088
            java.lang.String r7 = r6.TAG     // Catch:{ all -> 0x015c }
            java.lang.String r0 = "load: Invalid ImsProfile from sharedpref, reset to default"
            android.util.Log.e(r7, r0)     // Catch:{ all -> 0x015c }
            r6.createProfileMap()     // Catch:{ all -> 0x015c }
            monitor-exit(r6)     // Catch:{ all -> 0x015c }
            return
        L_0x0088:
            java.lang.String r2 = r6.TAG     // Catch:{ all -> 0x015c }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x015c }
            r4.<init>()     // Catch:{ all -> 0x015c }
            java.lang.String r5 = "load: MnoName: "
            r4.append(r5)     // Catch:{ all -> 0x015c }
            java.lang.String r5 = r3.getMnoName()     // Catch:{ all -> 0x015c }
            r4.append(r5)     // Catch:{ all -> 0x015c }
            java.lang.String r5 = ", Name: "
            r4.append(r5)     // Catch:{ all -> 0x015c }
            java.lang.String r5 = r3.getName()     // Catch:{ all -> 0x015c }
            r4.append(r5)     // Catch:{ all -> 0x015c }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x015c }
            android.util.Log.i(r2, r4)     // Catch:{ all -> 0x015c }
            int r2 = r6.mNextId     // Catch:{ all -> 0x015c }
            int r4 = r3.getId()     // Catch:{ all -> 0x015c }
            int r4 = r4 + 1
            int r2 = java.lang.Math.max(r2, r4)     // Catch:{ all -> 0x015c }
            r6.mNextId = r2     // Catch:{ all -> 0x015c }
            boolean r2 = r6.mIsMvno     // Catch:{ all -> 0x015c }
            if (r2 == 0) goto L_0x00f1
            java.lang.String r2 = r3.getMnoName()     // Catch:{ all -> 0x015c }
            java.lang.String r4 = r6.mPMnoName     // Catch:{ all -> 0x015c }
            boolean r2 = android.text.TextUtils.equals(r2, r4)     // Catch:{ all -> 0x015c }
            if (r2 == 0) goto L_0x00d8
            int r2 = r3.getId()     // Catch:{ all -> 0x015c }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x015c }
            r1.put(r2, r3)     // Catch:{ all -> 0x015c }
            goto L_0x0061
        L_0x00d8:
            java.lang.String r2 = r3.getMnoName()     // Catch:{ all -> 0x015c }
            java.lang.String r4 = r6.mMnoName     // Catch:{ all -> 0x015c }
            boolean r2 = android.text.TextUtils.equals(r2, r4)     // Catch:{ all -> 0x015c }
            if (r2 == 0) goto L_0x0061
            int r2 = r3.getId()     // Catch:{ all -> 0x015c }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x015c }
            r7.put(r2, r3)     // Catch:{ all -> 0x015c }
            goto L_0x0061
        L_0x00f1:
            java.lang.String r2 = r3.getMnoName()     // Catch:{ all -> 0x015c }
            java.lang.String r4 = r6.mMnoName     // Catch:{ all -> 0x015c }
            boolean r2 = android.text.TextUtils.equals(r2, r4)     // Catch:{ all -> 0x015c }
            if (r2 == 0) goto L_0x0061
            int r2 = r3.getId()     // Catch:{ all -> 0x015c }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x015c }
            r7.put(r2, r3)     // Catch:{ all -> 0x015c }
            goto L_0x0061
        L_0x010a:
            boolean r0 = r7.isEmpty()     // Catch:{ all -> 0x015c }
            if (r0 == 0) goto L_0x012c
            boolean r0 = r1.isEmpty()     // Catch:{ all -> 0x015c }
            if (r0 == 0) goto L_0x012c
            java.lang.String r0 = "DEFAULT"
            java.lang.String r2 = r6.mMnoName     // Catch:{ all -> 0x015c }
            boolean r0 = android.text.TextUtils.equals(r0, r2)     // Catch:{ all -> 0x015c }
            if (r0 != 0) goto L_0x012c
            java.lang.String r7 = r6.TAG     // Catch:{ all -> 0x015c }
            java.lang.String r0 = "load: Currently mno info different from mno is included in the SP"
            android.util.Log.e(r7, r0)     // Catch:{ all -> 0x015c }
            r6.createProfileMap()     // Catch:{ all -> 0x015c }
            monitor-exit(r6)     // Catch:{ all -> 0x015c }
            return
        L_0x012c:
            monitor-exit(r6)     // Catch:{ all -> 0x015c }
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r0 = r6.mProfileMap
            monitor-enter(r0)
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r2 = r6.mProfileMap     // Catch:{ all -> 0x0159 }
            r2.clear()     // Catch:{ all -> 0x0159 }
            boolean r2 = r6.mIsMvno     // Catch:{ all -> 0x0159 }
            if (r2 == 0) goto L_0x0152
            boolean r2 = r7.isEmpty()     // Catch:{ all -> 0x0159 }
            if (r2 == 0) goto L_0x014c
            java.lang.String r7 = r6.TAG     // Catch:{ all -> 0x0159 }
            java.lang.String r2 = "load: This mno is MVNO but no profile defined. Use Parent profiles"
            android.util.Log.e(r7, r2)     // Catch:{ all -> 0x0159 }
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r7 = r6.mProfileMap     // Catch:{ all -> 0x0159 }
            r7.putAll(r1)     // Catch:{ all -> 0x0159 }
            goto L_0x0157
        L_0x014c:
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r1 = r6.mProfileMap     // Catch:{ all -> 0x0159 }
            r1.putAll(r7)     // Catch:{ all -> 0x0159 }
            goto L_0x0157
        L_0x0152:
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r1 = r6.mProfileMap     // Catch:{ all -> 0x0159 }
            r1.putAll(r7)     // Catch:{ all -> 0x0159 }
        L_0x0157:
            monitor-exit(r0)     // Catch:{ all -> 0x0159 }
            goto L_0x0169
        L_0x0159:
            r6 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0159 }
            throw r6
        L_0x015c:
            r7 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x015c }
            throw r7
        L_0x015f:
            java.lang.String r7 = r6.TAG
            java.lang.String r0 = "load: map empty or version update or autoupdate needed or SIM MNO changed."
            android.util.Log.i(r7, r0)
            r6.createProfileMap()
        L_0x0169:
            java.lang.String r7 = r6.TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "load: mProfileMap size: "
            r0.append(r1)
            java.util.Map<java.lang.Integer, com.sec.ims.settings.ImsProfile> r6 = r6.mProfileMap
            int r6 = r6.size()
            r0.append(r6)
            java.lang.String r6 = r0.toString()
            android.util.Log.i(r7, r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsProfileCache.load(boolean):void");
    }

    private void createProfileMap() {
        clearAllFromStorage();
        initFromResource();
    }

    private static void removeNote(JsonElement jsonElement) {
        try {
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            while (asJsonObject.has("note")) {
                asJsonObject.remove("note");
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void initFromResource() {
        List<ImsProfile> init = init(false, this.mMnoName);
        Log.i(this.TAG, "initFromResource : Save to storage");
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, 0, false).edit();
        ArrayMap arrayMap = new ArrayMap();
        ArrayMap arrayMap2 = new ArrayMap();
        for (ImsProfile next : init) {
            edit.putString(String.valueOf(next.getId()), next.toJson());
            if (this.mIsMvno) {
                if (TextUtils.equals(next.getMnoName(), this.mPMnoName)) {
                    arrayMap2.put(Integer.valueOf(next.getId()), next);
                } else if (TextUtils.equals(next.getMnoName(), this.mMnoName)) {
                    arrayMap.put(Integer.valueOf(next.getId()), next);
                }
            } else if (TextUtils.equals(next.getMnoName(), this.mMnoName)) {
                arrayMap.put(Integer.valueOf(next.getId()), next);
            }
        }
        edit.apply();
        Log.i(this.TAG, "initFromResource : Prepare local cache");
        synchronized (this.mProfileMap) {
            this.mProfileMap.clear();
            if (!this.mIsMvno) {
                this.mProfileMap.putAll(arrayMap);
            } else if (arrayMap.isEmpty()) {
                Log.e(this.TAG, "init: This mno is MVNO but no profile defined. Use Parent profiles");
                this.mProfileMap.putAll(arrayMap2);
            } else {
                this.mProfileMap.putAll(arrayMap);
            }
        }
        String str = this.TAG;
        Log.i(str, "initFromResource : mProfileMap size: " + this.mProfileMap.size());
        saveBuildInfo();
    }

    private List<ImsProfile> init(boolean z, String str) {
        ArrayList arrayList = new ArrayList();
        Log.i(this.TAG, "init : imsprofile.json");
        if (z || !TextUtils.isEmpty(str)) {
            JsonParser jsonParser = new JsonParser();
            JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(this.mContext.getResources().openRawResource(R.raw.imsprofile))));
            JsonElement parse = jsonParser.parse(jsonReader);
            try {
                jsonReader.close();
            } catch (IOException unused) {
                Log.e(this.TAG, "init: Close failed. Keep going");
            }
            JsonArray asJsonArray = parse.getAsJsonObject().getAsJsonArray("profile");
            if (asJsonArray == null || asJsonArray.isJsonNull()) {
                Log.e(this.TAG, "init: parse failed.");
                return arrayList;
            }
            JsonArray jsonArray = new JsonArray();
            JsonElement jsonElement = JsonNull.INSTANCE;
            synchronized (this) {
                this.mNextId = 1;
                Iterator it = asJsonArray.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    JsonElement jsonElement2 = (JsonElement) it.next();
                    JsonElement asJsonObject = jsonElement2.getAsJsonObject();
                    if (!TextUtils.equals(jsonElement2.getAsJsonObject().get("name").getAsString(), "default")) {
                        int i = this.mNextId;
                        this.mNextId = i + 1;
                        asJsonObject.addProperty("id", String.valueOf(i));
                        if (!z) {
                            if (TextUtils.equals(str, "GoogleGC_ALL") && asJsonObject.get("mnoname").getAsString().equals("GoogleGC_ALL")) {
                                jsonArray.add(asJsonObject);
                                break;
                            } else if ((asJsonObject.get("mdmn_type") != null && asJsonObject.get("mdmn_type").getAsString().equals(str)) || asJsonObject.get("mnoname").getAsString().startsWith(str.split(":")[0])) {
                                jsonArray.add(asJsonObject);
                            }
                        } else {
                            jsonArray.add(asJsonObject);
                        }
                    } else {
                        jsonElement = asJsonObject;
                    }
                }
            }
            if (jsonElement.isJsonNull()) {
                Log.e(this.TAG, "init: No default profile.");
                return arrayList;
            }
            JsonElement applyImsProfileUpdate = this.mAutoUpdate.applyImsProfileUpdate(jsonArray, str);
            if (!applyImsProfileUpdate.isJsonNull() && applyImsProfileUpdate.isJsonArray()) {
                jsonArray = applyImsProfileUpdate.getAsJsonArray();
            }
            String str2 = this.TAG;
            Log.d(str2, "init: Found " + jsonArray.size() + " profiles to merge.");
            synchronized (this) {
                Iterator it2 = jsonArray.iterator();
                while (it2.hasNext()) {
                    JsonElement merge = JsonUtil.merge(jsonElement, (JsonElement) it2.next());
                    if (merge.isJsonNull()) {
                        Log.e(this.TAG, "init: merge failed! check json is valid.");
                    } else {
                        removeNote(merge);
                        ImsProfile imsProfile = new ImsProfile(merge.toString());
                        if (imsProfile.getId() == 0) {
                            String str3 = this.TAG;
                            Log.d(str3, "init: profile name[" + imsProfile.getName() + "]");
                            int i2 = this.mNextId;
                            this.mNextId = i2 + 1;
                            imsProfile.setId(i2);
                        }
                        arrayList.add(imsProfile);
                    }
                }
                String str4 = this.TAG;
                Log.i(str4, "init: merge completed. " + arrayList.size() + " profiles initiated.");
            }
            return arrayList;
        }
        Log.e(this.TAG, "init: selection is empty. Return no profile.");
        return arrayList;
    }

    public void resetToDefault() {
        clearAllFromStorage();
        load(false);
    }

    public List<ImsProfile> getProfileListByMnoName(String str) {
        return getProfileListByMnoName(str, false);
    }

    public List<ImsProfile> getProfileListByMnoName(String str, boolean z) {
        boolean z2;
        ArrayList arrayList = new ArrayList();
        if (!TextUtils.isEmpty(str)) {
            if (TextUtils.equals(this.mMnoName, str)) {
                Log.d(this.TAG, "getProfileList by loaded mno - " + str);
                synchronized (this.mProfileMap) {
                    for (ImsProfile next : this.mProfileMap.values()) {
                        if (!z || next.hasService("mmtel")) {
                            arrayList.add(new ImsProfile(next));
                        }
                    }
                }
            } else {
                int indexOf = str.indexOf(Mno.MVNO_DELIMITER);
                String str2 = "";
                if (indexOf != -1) {
                    str2 = str.substring(0, indexOf);
                    z2 = true;
                } else {
                    z2 = false;
                }
                Log.i(this.TAG, "getProfileList by new mno - " + str + ", loaded mno - " + this.mMnoName + ", isMvno - " + z2);
                ArrayList arrayList2 = new ArrayList();
                for (ImsProfile next2 : init(false, str)) {
                    if (TextUtils.equals(next2.getMnoName(), str) && (!z || next2.hasService("mmtel"))) {
                        arrayList.add(new ImsProfile(next2));
                    }
                    if (z2 && TextUtils.equals(next2.getMnoName(), str2)) {
                        Log.d(this.TAG, "getProfileList by new mno - " + str + ", Parent mno - " + next2.getMnoName());
                        if (!z || next2.hasService("mmtel")) {
                            arrayList2.add(new ImsProfile(next2));
                        }
                    }
                    if (z && this.mProfileGlobalGC == null && TextUtils.equals(next2.getMnoName(), "GoogleGC_ALL")) {
                        this.mProfileGlobalGC = new ImsProfile(next2);
                    }
                }
                if (z2 && arrayList.isEmpty()) {
                    arrayList = (ArrayList) arrayList2.clone();
                }
            }
            if (z) {
                ImsProfile imsProfile = this.mProfileGlobalGC;
                if (imsProfile != null) {
                    arrayList.add(imsProfile);
                } else {
                    for (ImsProfile next3 : init(false, "GoogleGC_ALL")) {
                        if (TextUtils.equals(next3.getMnoName(), "GoogleGC_ALL")) {
                            ImsProfile imsProfile2 = new ImsProfile(next3);
                            this.mProfileGlobalGC = imsProfile2;
                            arrayList.add(imsProfile2);
                        }
                    }
                }
            }
        }
        Log.d(this.TAG, "getProfileListByMnoName: " + arrayList);
        return arrayList;
    }

    public List<ImsProfile> getProfileListByMdmnType(String str) {
        ArrayList arrayList = new ArrayList();
        synchronized (this.mProfileMap) {
            for (ImsProfile next : this.mProfileMap.values()) {
                if (TextUtils.equals(next.getMdmnType(), str)) {
                    arrayList.add(new ImsProfile(next));
                }
            }
        }
        if (arrayList.isEmpty()) {
            Log.d(this.TAG, "not found from loaded profile by mdmn type");
            for (ImsProfile next2 : init(false, str)) {
                if (TextUtils.equals(next2.getMdmnType(), str)) {
                    arrayList.add(new ImsProfile(next2));
                }
            }
        }
        String str2 = this.TAG;
        Log.d(str2, "getProfileListByMdmnType: " + arrayList);
        return arrayList;
    }

    public List<ImsProfile> getAllProfileList() {
        return new ArrayList(getAllProfileFromStorage().values());
    }

    public ImsProfile getProfile(int i) {
        synchronized (this.mProfileMap) {
            if (!this.mProfileMap.containsKey(Integer.valueOf(i))) {
                return getAllProfileFromStorage().get(Integer.valueOf(i));
            }
            ImsProfile imsProfile = this.mProfileMap.get(Integer.valueOf(i));
            return imsProfile;
        }
    }

    public int insert(ImsProfile imsProfile) {
        synchronized (this) {
            int i = this.mNextId;
            this.mNextId = i + 1;
            imsProfile.setId(i);
        }
        synchronized (this.mProfileMap) {
            this.mProfileMap.put(Integer.valueOf(imsProfile.getId()), imsProfile);
        }
        saveToStorage(imsProfile);
        return imsProfile.getId();
    }

    public int update(int i, ContentValues contentValues) {
        ImsProfile profile = getProfile(i);
        if (profile == null) {
            Log.e(this.TAG, "update: profile not found.");
            return 0;
        }
        profile.update(contentValues);
        synchronized (this.mProfileMap) {
            if (this.mProfileMap.containsKey(Integer.valueOf(i))) {
                this.mProfileMap.put(Integer.valueOf(profile.getId()), profile);
            }
        }
        saveToStorage(profile);
        return 1;
    }

    public void remove(int i) {
        synchronized (this.mProfileMap) {
            this.mProfileMap.remove(Integer.valueOf(i));
        }
        removeFromStorage(i);
    }

    private void saveToStorage(ImsProfile imsProfile) {
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, String.valueOf(imsProfile.getId()), imsProfile.toJson());
    }

    private void removeFromStorage(int i) {
        ImsSharedPrefHelper.remove(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE, String.valueOf(i));
    }

    private void clearAllFromStorage() {
        ImsSharedPrefHelper.clear(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_PROFILE);
    }

    private Map<Integer, ImsProfile> getAllProfileFromStorage() {
        synchronized (this.mProfileMap) {
            if (this.mProfileMap.isEmpty()) {
                load(false);
            }
        }
        ArrayMap arrayMap = new ArrayMap();
        for (ImsProfile next : init(true, (String) null)) {
            arrayMap.put(Integer.valueOf(next.getId()), next);
        }
        return arrayMap;
    }

    public void dump() {
        synchronized (this.mProfileMap) {
            IMSLog.dump(this.TAG, "Dump of ImsProfileCache:");
            IMSLog.increaseIndent(this.TAG);
            this.mProfileMap.values().forEach(new ImsProfileCache$$ExternalSyntheticLambda0(this));
            IMSLog.decreaseIndent(this.TAG);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$dump$0(ImsProfile imsProfile) {
        IMSLog.dump(this.TAG, imsProfile.toString());
    }

    public boolean updateMno(ContentValues contentValues) {
        String str;
        boolean z;
        if (contentValues != null) {
            String asString = contentValues.getAsString("mnoname");
            String asString2 = contentValues.getAsString(ISimManager.KEY_MVNO_NAME);
            if (!TextUtils.isEmpty(asString2)) {
                z = true;
                String str2 = asString;
                asString = asString + ":" + asString2;
                str = str2;
            } else {
                str = "";
                z = false;
            }
            if (asString != null && !TextUtils.equals(asString, this.mMnoName)) {
                Log.i(this.TAG, "updateMno: Mno Changed from " + this.mMnoName + " to " + asString);
                this.mIsMvno = z;
                this.mPMnoName = str;
                this.mMnoName = asString;
                if (z) {
                    Log.d(this.TAG, "updateMno: This mno is MVNO, Parent Mno is " + this.mPMnoName);
                }
                load(true);
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(7:0|1|2|3|4|(3:8|(5:12|13|14|(3:16|24|21)(3:22|17|18)|9)|23)|19) */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x002e */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x004c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String readFromJsonFile(android.content.Context r4, java.lang.String r5, java.lang.String r6) {
        /*
            java.lang.String r0 = ""
            com.google.gson.JsonParser r1 = new com.google.gson.JsonParser
            r1.<init>()
            java.io.InputStreamReader r2 = new java.io.InputStreamReader
            android.content.res.Resources r4 = r4.getResources()
            r3 = 2131099650(0x7f060002, float:1.781166E38)
            java.io.InputStream r4 = r4.openRawResource(r3)
            r2.<init>(r4)
            java.io.BufferedReader r4 = new java.io.BufferedReader
            r4.<init>(r2)
            com.google.gson.stream.JsonReader r3 = new com.google.gson.stream.JsonReader
            r3.<init>(r4)
            com.google.gson.JsonElement r1 = r1.parse(r3)
            r2.close()     // Catch:{ IOException -> 0x002e }
            r4.close()     // Catch:{ IOException -> 0x002e }
            r3.close()     // Catch:{ IOException -> 0x002e }
        L_0x002e:
            com.google.gson.JsonObject r4 = r1.getAsJsonObject()     // Catch:{ NullPointerException -> 0x0070 }
            java.lang.String r1 = "profile"
            com.google.gson.JsonArray r4 = r4.getAsJsonArray(r1)     // Catch:{ NullPointerException -> 0x0070 }
            if (r4 == 0) goto L_0x0070
            boolean r1 = r4.isJsonNull()
            if (r1 == 0) goto L_0x0042
            goto L_0x0070
        L_0x0042:
            java.util.Iterator r4 = r4.iterator()
        L_0x0046:
            boolean r1 = r4.hasNext()
            if (r1 == 0) goto L_0x0070
            java.lang.Object r1 = r4.next()
            com.google.gson.JsonElement r1 = (com.google.gson.JsonElement) r1
            com.google.gson.JsonObject r1 = r1.getAsJsonObject()     // Catch:{ NullPointerException -> 0x0046 }
            java.lang.String r2 = "name"
            com.google.gson.JsonElement r2 = r1.get(r2)     // Catch:{ NullPointerException -> 0x0046 }
            java.lang.String r2 = r2.getAsString()     // Catch:{ NullPointerException -> 0x0046 }
            boolean r2 = r5.equals(r2)     // Catch:{ NullPointerException -> 0x0046 }
            if (r2 != 0) goto L_0x0067
            goto L_0x0046
        L_0x0067:
            com.google.gson.JsonElement r1 = r1.get(r6)     // Catch:{ NullPointerException -> 0x0046 }
            java.lang.String r4 = r1.getAsString()     // Catch:{ NullPointerException -> 0x0046 }
            return r4
        L_0x0070:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsProfileCache.readFromJsonFile(android.content.Context, java.lang.String, java.lang.String):java.lang.String");
    }
}
