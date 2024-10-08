package com.sec.internal.ims.xq.att.data;

import java.util.ArrayList;

public class XqEvent {
    private ArrayList<XqContent> mContent = new ArrayList<>();
    private XqMtrips mtrips;

    public enum XqContentType {
        UNDEFINED,
        UCHAR,
        USHORT,
        UINT32,
        STRING
    }

    public enum XqMtrips {
        UNDEFINED(0),
        M01(1),
        M02(2),
        M03(3),
        M04(4),
        M05(5),
        M06(6),
        SPTX(100),
        SPRX(101);
        
        private final int value;

        private XqMtrips(int i) {
            this.value = i;
        }

        public static XqMtrips castToType(int i) {
            for (XqMtrips xqMtrips : values()) {
                if (xqMtrips.getValue() == i) {
                    return xqMtrips;
                }
            }
            return UNDEFINED;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static class XqContent {
        int intVal;
        String strVal;
        XqContentType type;

        XqContent(int i, int i2, String str) {
            if (i == 1) {
                this.type = XqContentType.UCHAR;
            } else if (i == 2) {
                this.type = XqContentType.USHORT;
            } else if (i == 3) {
                this.type = XqContentType.UINT32;
            } else if (i != 4) {
                this.type = XqContentType.UNDEFINED;
            } else {
                this.type = XqContentType.STRING;
            }
            this.intVal = i2;
            this.strVal = str;
        }

        public boolean hasStrVal() {
            return this.strVal != null;
        }

        public String getStrVal() {
            return this.strVal;
        }

        public boolean hasIntVal() {
            return this.intVal >= 0;
        }

        public int getIntVal() {
            return this.intVal;
        }

        public XqContentType getType() {
            return this.type;
        }
    }

    public void setXqMtrips(int i) {
        this.mtrips = XqMtrips.castToType(i);
    }

    public XqMtrips getMtrip() {
        return this.mtrips;
    }

    public void setContent(int i, int i2, String str) {
        this.mContent.add(new XqContent(i, i2, str));
    }

    public ArrayList<XqContent> getMContentList() {
        return this.mContent;
    }

    public XqContent getMContent(int i) {
        return this.mContent.get(i);
    }
}
