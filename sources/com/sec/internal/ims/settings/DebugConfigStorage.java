package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import com.sec.internal.helper.ImsSharedPrefHelper;
import java.util.Map;

public class DebugConfigStorage {
    private Context mContext;

    protected DebugConfigStorage(Context context) {
        this.mContext = context;
    }

    public void insert(int i, ContentValues contentValues) {
        ImsSharedPrefHelper.put(i, this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, contentValues);
    }

    public Cursor query(int i, String[] strArr) {
        if (strArr == null) {
            return null;
        }
        Map<String, String> stringArray = ImsSharedPrefHelper.getStringArray(i, this.mContext, ImsSharedPrefHelper.DEBUG_CONFIG, strArr);
        MatrixCursor matrixCursor = new MatrixCursor((String[]) stringArray.keySet().toArray(new String[0]));
        matrixCursor.addRow(stringArray.values());
        return matrixCursor;
    }
}
