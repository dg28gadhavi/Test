package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.TextUtils;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.XmlUtils;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.Map;
import org.xmlpull.v1.XmlPullParserException;

public class UserConfigStorage {
    private static final String KEY_LOADED = "loaded";
    private static final String LOG_TAG = "UserConfigStorage";
    private Context mContext;
    private SimpleEventLog mEventLog;
    private final Object mLock = new Object();
    private String mMnoname;
    private int mPhoneId;

    protected UserConfigStorage(Context context, String str, int i) {
        IMSLog.d(LOG_TAG, i, "UserConfigStorage()");
        this.mContext = context;
        this.mMnoname = str;
        this.mPhoneId = i;
        this.mEventLog = new SimpleEventLog(context, i, LOG_TAG, 100);
    }

    public void insert(ContentValues contentValues) {
        IMSLog.d(LOG_TAG, this.mPhoneId, "insert()");
        ImsSharedPrefHelper.put(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, contentValues);
    }

    public Cursor query(String[] strArr) {
        IMSLog.d(LOG_TAG, this.mPhoneId, "query()");
        if (!isLoaded()) {
            this.mEventLog.logAndAdd(this.mPhoneId, "initUserConfig");
            synchronized (this.mLock) {
                initUserConfiguration();
            }
        }
        if (strArr == null) {
            return null;
        }
        Map<String, String> stringArray = ImsSharedPrefHelper.getStringArray(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, strArr);
        MatrixCursor matrixCursor = new MatrixCursor((String[]) stringArray.keySet().toArray(new String[0]));
        matrixCursor.addRow(stringArray.values());
        return matrixCursor;
    }

    public boolean isLoaded() {
        boolean z;
        synchronized (this.mLock) {
            z = ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, KEY_LOADED, false);
        }
        return z;
    }

    public void reset(String str) {
        IMSLog.d(LOG_TAG, this.mPhoneId, "reset()");
        synchronized (this.mLock) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            if (simManagerFromSimSlot == null || simManagerFromSimSlot.isSimLoaded()) {
                String string = ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "mnoname", "");
                SimpleEventLog simpleEventLog = this.mEventLog;
                int i = this.mPhoneId;
                simpleEventLog.logAndAdd(i, "mnoname=" + str + ", prevMnoName=" + string);
                if (Mno.KDDI.getName().equals(str)) {
                    boolean z = ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, KEY_LOADED, false);
                    if (TextUtils.isEmpty(string) && z) {
                        this.mEventLog.logAndAdd(this.mPhoneId, "prevMnoName was empty, put it back again");
                        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "mnoname", str);
                        return;
                    }
                }
                if (string.equals(str)) {
                    int i2 = this.mPhoneId;
                    IMSLog.d(LOG_TAG, i2, "reset() same mnoname " + str);
                    return;
                }
                ImsSharedPrefHelper.clear(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG);
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, KEY_LOADED, false);
                this.mMnoname = str;
                initUserConfiguration();
                return;
            }
            IMSLog.d(LOG_TAG, this.mPhoneId, "reset() sim not available");
        }
    }

    private void initUserConfiguration() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "initUserConfiguration()");
        if (!"DEFAULT".equals(this.mMnoname) && !this.mMnoname.isEmpty()) {
            XmlResourceParser xml = this.mContext.getResources().getXml(R.xml.userconfiguration);
            ContentValues contentValues = new ContentValues();
            ContentValues contentValues2 = new ContentValues();
            try {
                XmlUtils.beginDocument(xml, "configurations");
                String str = null;
                String str2 = null;
                while (true) {
                    int next = xml.next();
                    if (next == 1) {
                        break;
                    } else if (next == 2) {
                        int attributeCount = xml.getAttributeCount();
                        for (int i = 0; i < attributeCount; i++) {
                            if ("name".equalsIgnoreCase(xml.getAttributeName(i))) {
                                str = xml.getAttributeValue(i);
                            } else if ("mnoname".equalsIgnoreCase(xml.getAttributeName(i))) {
                                str2 = xml.getAttributeValue(i);
                            } else {
                                contentValues2.put(xml.getAttributeName(i), xml.getAttributeValue(i));
                            }
                        }
                        if (!"default".equalsIgnoreCase(str)) {
                            if (str2 != null && str2.equals(this.mMnoname)) {
                                contentValues.putAll(contentValues2);
                                break;
                            }
                        } else {
                            contentValues.putAll(contentValues2);
                        }
                        contentValues2.clear();
                    }
                }
                insert(contentValues);
                this.mEventLog.logAndAdd(this.mPhoneId, str + ", " + this.mMnoname + ":" + contentValues.toString());
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, KEY_LOADED, true);
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.USER_CONFIG, "mnoname", this.mMnoname);
            } catch (IOException | XmlPullParserException e) {
                this.mEventLog.logAndAdd(this.mPhoneId, "initUserConfiguration Exception");
                e.printStackTrace();
            }
        }
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, this.mPhoneId, "Dump of UserConfigStorage:");
        this.mEventLog.dump();
    }
}
