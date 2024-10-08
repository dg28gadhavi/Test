package org.xbill.DNS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RRset implements Serializable {
    private static final long serialVersionUID = -3270249290171239695L;
    private short nsigs;
    private short position;
    private List rrs;

    private synchronized Iterator iterator(boolean z, boolean z2) {
        int i;
        int i2;
        int size = this.rrs.size();
        if (z) {
            i = size - this.nsigs;
        } else {
            i = this.nsigs;
        }
        if (i == 0) {
            return Collections.EMPTY_LIST.iterator();
        }
        if (!z) {
            i2 = size - this.nsigs;
        } else if (!z2) {
            i2 = 0;
        } else {
            if (this.position >= i) {
                this.position = 0;
            }
            i2 = this.position;
            this.position = (short) (i2 + 1);
        }
        ArrayList arrayList = new ArrayList(i);
        if (z) {
            arrayList.addAll(this.rrs.subList(i2, i));
            if (i2 != 0) {
                arrayList.addAll(this.rrs.subList(0, i2));
            }
        } else {
            arrayList.addAll(this.rrs.subList(i2, size));
        }
        return arrayList.iterator();
    }

    public Name getName() {
        return first().getName();
    }

    public int getType() {
        return first().getRRsetType();
    }

    public int getDClass() {
        return first().getDClass();
    }

    public synchronized long getTTL() {
        return first().getTTL();
    }

    public synchronized Record first() {
        if (this.rrs.size() != 0) {
        } else {
            throw new IllegalStateException("rrset is empty");
        }
        return (Record) this.rrs.get(0);
    }

    private String iteratorToString(Iterator it) {
        StringBuffer stringBuffer = new StringBuffer();
        while (it.hasNext()) {
            stringBuffer.append("[");
            stringBuffer.append(((Record) it.next()).rdataToString());
            stringBuffer.append("]");
            if (it.hasNext()) {
                stringBuffer.append(" ");
            }
        }
        return stringBuffer.toString();
    }

    public String toString() {
        if (this.rrs.size() == 0) {
            return "{empty}";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{ ");
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(getName());
        stringBuffer2.append(" ");
        stringBuffer.append(stringBuffer2.toString());
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer3.append(getTTL());
        stringBuffer3.append(" ");
        stringBuffer.append(stringBuffer3.toString());
        StringBuffer stringBuffer4 = new StringBuffer();
        stringBuffer4.append(DClass.string(getDClass()));
        stringBuffer4.append(" ");
        stringBuffer.append(stringBuffer4.toString());
        StringBuffer stringBuffer5 = new StringBuffer();
        stringBuffer5.append(Type.string(getType()));
        stringBuffer5.append(" ");
        stringBuffer.append(stringBuffer5.toString());
        stringBuffer.append(iteratorToString(iterator(true, false)));
        if (this.nsigs > 0) {
            stringBuffer.append(" sigs: ");
            stringBuffer.append(iteratorToString(iterator(false, false)));
        }
        stringBuffer.append(" }");
        return stringBuffer.toString();
    }
}
