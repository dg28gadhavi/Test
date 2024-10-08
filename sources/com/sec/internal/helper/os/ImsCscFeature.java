package com.sec.internal.helper.os;

import com.samsung.android.feature.SemCscFeature;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.util.Hashtable;

public class ImsCscFeature {
    private static volatile ImsCscFeature sInstance;
    private SemCscFeature mCscFeature = SemCscFeature.getInstance();
    private Hashtable<String, String> mFeatureList = new Hashtable<>();
    private Hashtable<String, String> mFeatureList_2 = new Hashtable<>();

    public static ImsCscFeature getInstance() {
        if (sInstance == null) {
            synchronized (ImsCscFeature.class) {
                if (sInstance == null) {
                    sInstance = new ImsCscFeature();
                }
            }
        }
        return sInstance;
    }

    public String getString(String str) {
        if (this.mFeatureList.containsKey(str)) {
            return this.mFeatureList.get(str);
        }
        return this.mCscFeature.getString(str);
    }

    public String getString(int i, String str) {
        if (i != 1) {
            return getString(str);
        }
        if (this.mFeatureList_2.containsKey(str)) {
            return this.mFeatureList_2.get(str);
        }
        return this.mCscFeature.getString(i, str);
    }

    public boolean getBoolean(String str) {
        if (this.mFeatureList.containsKey(str)) {
            return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(this.mFeatureList.get(str));
        }
        return this.mCscFeature.getBoolean(str);
    }

    public boolean getBoolean(int i, String str) {
        if (i != 1) {
            return getBoolean(str);
        }
        if (this.mFeatureList_2.containsKey(str)) {
            return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(this.mFeatureList_2.get(str));
        }
        return this.mCscFeature.getBoolean(i, str);
    }

    public void put(String str, String str2) {
        this.mFeatureList.put(str, str2);
    }

    public void put(int i, String str, String str2) {
        if (i != 1) {
            put(str, str2);
        } else {
            this.mFeatureList_2.put(str, str2);
        }
    }

    public void remove(String str) {
        this.mFeatureList.remove(str);
    }

    public void remove(int i, String str) {
        if (i != 1) {
            remove(str);
        } else {
            this.mFeatureList_2.remove(str);
        }
    }

    public void clear() {
        this.mFeatureList.clear();
    }

    public void clear(int i) {
        if (i != 1) {
            clear();
        } else {
            this.mFeatureList_2.clear();
        }
    }
}
