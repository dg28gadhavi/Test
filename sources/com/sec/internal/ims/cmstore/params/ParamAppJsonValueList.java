package com.sec.internal.ims.cmstore.params;

import java.util.ArrayList;
import java.util.Iterator;

public class ParamAppJsonValueList {
    public ArrayList<ParamAppJsonValue> mOperationList = new ArrayList<>();

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("mOperationList: ");
        Iterator<ParamAppJsonValue> it = this.mOperationList.iterator();
        while (it.hasNext()) {
            stringBuffer.append(it.next());
        }
        return stringBuffer.toString();
    }
}
