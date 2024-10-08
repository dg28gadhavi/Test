package com.sec.internal.ims.cmstore.params;

import java.util.ArrayList;
import java.util.Iterator;

public class BufferDBChangeParamList {
    public ArrayList<BufferDBChangeParam> mChangelst = new ArrayList<>();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BufferDBChangeParamList: ");
        Iterator<BufferDBChangeParam> it = this.mChangelst.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
        }
        return sb.toString();
    }
}
