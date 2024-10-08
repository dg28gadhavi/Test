package org.xbill.DNS;

import java.util.HashMap;

class Mnemonic {
    private static Integer[] cachedInts = new Integer[64];
    private String description;
    private int max = Integer.MAX_VALUE;
    private boolean numericok;
    private String prefix;
    private HashMap strings = new HashMap();
    private HashMap values = new HashMap();
    private int wordcase;

    static {
        int i = 0;
        while (true) {
            Integer[] numArr = cachedInts;
            if (i < numArr.length) {
                numArr[i] = new Integer(i);
                i++;
            } else {
                return;
            }
        }
    }

    public Mnemonic(String str, int i) {
        this.description = str;
        this.wordcase = i;
    }

    public void setMaximum(int i) {
        this.max = i;
    }

    public void setPrefix(String str) {
        this.prefix = sanitize(str);
    }

    public void setNumericAllowed(boolean z) {
        this.numericok = z;
    }

    public static Integer toInteger(int i) {
        if (i >= 0) {
            Integer[] numArr = cachedInts;
            if (i < numArr.length) {
                return numArr[i];
            }
        }
        return new Integer(i);
    }

    public void check(int i) {
        if (i < 0 || i > this.max) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(this.description);
            stringBuffer.append(" ");
            stringBuffer.append(i);
            stringBuffer.append("is out of range");
            throw new IllegalArgumentException(stringBuffer.toString());
        }
    }

    private String sanitize(String str) {
        int i = this.wordcase;
        if (i == 2) {
            return str.toUpperCase();
        }
        return i == 3 ? str.toLowerCase() : str;
    }

    public void add(int i, String str) {
        check(i);
        Integer integer = toInteger(i);
        String sanitize = sanitize(str);
        this.strings.put(sanitize, integer);
        this.values.put(integer, sanitize);
    }

    public void addAlias(int i, String str) {
        check(i);
        Integer integer = toInteger(i);
        this.strings.put(sanitize(str), integer);
    }

    public void addAll(Mnemonic mnemonic) {
        if (this.wordcase == mnemonic.wordcase) {
            this.strings.putAll(mnemonic.strings);
            this.values.putAll(mnemonic.values);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(mnemonic.description);
        stringBuffer.append(": wordcases do not match");
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    public String getText(int i) {
        check(i);
        String str = (String) this.values.get(toInteger(i));
        if (str != null) {
            return str;
        }
        String num = Integer.toString(i);
        if (this.prefix == null) {
            return num;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.prefix);
        stringBuffer.append(num);
        return stringBuffer.toString();
    }
}
