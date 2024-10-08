package com.sec.internal.ims.cmstore.helper;

import android.content.Context;
import com.sec.internal.helper.SimpleEventLog;
import java.util.ArrayList;
import java.util.List;

public class EventLogHelper {
    public static List<SimpleEventLog> mEventLogList = new ArrayList(2);

    public static void dump(int i) {
        if (mEventLogList.get(i) != null) {
            mEventLogList.get(i).dump();
        }
    }

    public static void initialise(Context context, String str, int i, int i2) {
        if (i2 < 2) {
            mEventLogList.add(i2, new SimpleEventLog(context, str, i));
        }
    }

    public static void infoLogAndAdd(String str, int i, String str2) {
        if (mEventLogList.get(i) != null) {
            mEventLogList.get(i).infoLogAndAdd(str + "[" + i + "]", str2);
        }
    }

    public static void debugLogAndAdd(String str, int i, String str2) {
        if (mEventLogList.get(i) != null) {
            mEventLogList.get(i).debugLogAndAdd(str, str2);
        }
    }

    public static void add(String str, int i, String str2) {
        if (mEventLogList.get(i) != null) {
            mEventLogList.get(i).add(str + ": " + str2);
        }
    }
}
