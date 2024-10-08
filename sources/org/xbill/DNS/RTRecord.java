package org.xbill.DNS;

public class RTRecord extends U16NameBase {
    private static final long serialVersionUID = -3206215651648278098L;

    RTRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new RTRecord();
    }
}
