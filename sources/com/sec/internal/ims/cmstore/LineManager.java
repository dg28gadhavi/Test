package com.sec.internal.ims.cmstore;

import android.util.Log;
import com.sec.internal.interfaces.ims.cmstore.ILineStatusChangeCallBack;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineManager {
    private static final String TAG = "LineManager";
    private final ILineStatusChangeCallBack mILineStatusChangeCallBack;
    private final Map<String, LineWorkingStatus> mLineStatus = new HashMap();
    private final List<LineStatusObserver> mLineStatusOberserverList = new ArrayList();

    public interface LineStatusObserver {
        void onLineAdded(String str);
    }

    private enum LineWorkingStatus {
        WORKING
    }

    public LineManager(ILineStatusChangeCallBack iLineStatusChangeCallBack) {
        this.mILineStatusChangeCallBack = iLineStatusChangeCallBack;
    }

    public void registerLineStatusOberser(LineStatusObserver lineStatusObserver) {
        this.mLineStatusOberserverList.add(lineStatusObserver);
        if (this.mLineStatus.size() >= 1) {
            for (String onLineAdded : this.mLineStatus.keySet()) {
                lineStatusObserver.onLineAdded(onLineAdded);
            }
        }
    }

    public void initLineStatus() {
        List<String> notifyLoadLineStatus = this.mILineStatusChangeCallBack.notifyLoadLineStatus();
        if (notifyLoadLineStatus == null || notifyLoadLineStatus.size() == 0) {
            Log.i(TAG, "no line added yet");
            return;
        }
        for (String addLine : notifyLoadLineStatus) {
            addLine(addLine);
        }
    }

    public void addLine(String str) {
        String str2 = TAG;
        Log.i(str2, "addLine :: " + IMSLog.checker(str));
        this.mLineStatus.put(str, LineWorkingStatus.WORKING);
        for (LineStatusObserver onLineAdded : this.mLineStatusOberserverList) {
            onLineAdded.onLineAdded(str);
        }
    }
}
