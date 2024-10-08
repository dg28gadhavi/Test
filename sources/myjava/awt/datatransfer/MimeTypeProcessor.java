package myjava.awt.datatransfer;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

final class MimeTypeProcessor {
    private static MimeTypeProcessor instance;

    private static boolean isMeaningfulChar(char c) {
        return c >= '!' && c <= '~';
    }

    private static boolean isTSpecialChar(char c) {
        return c == '(' || c == ')' || c == '[' || c == ']' || c == '<' || c == '>' || c == '@' || c == ',' || c == ';' || c == ':' || c == '\\' || c == '\"' || c == '/' || c == '?' || c == '=';
    }

    private MimeTypeProcessor() {
    }

    static MimeType parse(String str) {
        if (instance == null) {
            instance = new MimeTypeProcessor();
        }
        MimeType mimeType = new MimeType();
        if (str != null) {
            StringPosition stringPosition = new StringPosition((StringPosition) null);
            retrieveType(str, mimeType, stringPosition);
            retrieveParams(str, mimeType, stringPosition);
        }
        return mimeType;
    }

    static String assemble(MimeType mimeType) {
        StringBuilder sb = new StringBuilder();
        sb.append(mimeType.getFullType());
        Enumeration keys = mimeType.parameters.keys();
        while (keys.hasMoreElements()) {
            String str = (String) keys.nextElement();
            sb.append("; ");
            sb.append(str);
            sb.append("=\"");
            sb.append((String) mimeType.parameters.get(str));
            sb.append('\"');
        }
        return sb.toString();
    }

    private static void retrieveType(String str, MimeType mimeType, StringPosition stringPosition) {
        mimeType.primaryType = retrieveToken(str, stringPosition).toLowerCase();
        int nextMeaningfulIndex = getNextMeaningfulIndex(str, stringPosition.i);
        stringPosition.i = nextMeaningfulIndex;
        if (nextMeaningfulIndex >= str.length() || str.charAt(stringPosition.i) != '/') {
            throw new IllegalArgumentException();
        }
        stringPosition.i++;
        mimeType.subType = retrieveToken(str, stringPosition).toLowerCase();
    }

    private static void retrieveParams(String str, MimeType mimeType, StringPosition stringPosition) {
        mimeType.parameters = new Hashtable();
        mimeType.systemParameters = new Hashtable();
        while (true) {
            int nextMeaningfulIndex = getNextMeaningfulIndex(str, stringPosition.i);
            stringPosition.i = nextMeaningfulIndex;
            if (nextMeaningfulIndex < str.length()) {
                if (str.charAt(stringPosition.i) == ';') {
                    stringPosition.i++;
                    retrieveParam(str, mimeType, stringPosition);
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                return;
            }
        }
    }

    private static void retrieveParam(String str, MimeType mimeType, StringPosition stringPosition) {
        String str2;
        String lowerCase = retrieveToken(str, stringPosition).toLowerCase();
        int nextMeaningfulIndex = getNextMeaningfulIndex(str, stringPosition.i);
        stringPosition.i = nextMeaningfulIndex;
        if (nextMeaningfulIndex >= str.length() || str.charAt(stringPosition.i) != '=') {
            throw new IllegalArgumentException();
        }
        int i = stringPosition.i + 1;
        stringPosition.i = i;
        int nextMeaningfulIndex2 = getNextMeaningfulIndex(str, i);
        stringPosition.i = nextMeaningfulIndex2;
        if (nextMeaningfulIndex2 < str.length()) {
            if (str.charAt(stringPosition.i) == '\"') {
                str2 = retrieveQuoted(str, stringPosition);
            } else {
                str2 = retrieveToken(str, stringPosition);
            }
            mimeType.parameters.put(lowerCase, str2);
            return;
        }
        throw new IllegalArgumentException();
    }

    private static String retrieveQuoted(String str, StringPosition stringPosition) {
        StringBuilder sb = new StringBuilder();
        stringPosition.i++;
        boolean z = true;
        do {
            if (str.charAt(stringPosition.i) != '\"' || !z) {
                int i = stringPosition.i;
                stringPosition.i = i + 1;
                char charAt = str.charAt(i);
                if (!z) {
                    z = true;
                } else if (charAt == '\\') {
                    z = false;
                }
                if (z) {
                    sb.append(charAt);
                }
            } else {
                stringPosition.i++;
                return sb.toString();
            }
        } while (stringPosition.i != str.length());
        throw new IllegalArgumentException();
    }

    private static String retrieveToken(String str, StringPosition stringPosition) {
        StringBuilder sb = new StringBuilder();
        int nextMeaningfulIndex = getNextMeaningfulIndex(str, stringPosition.i);
        stringPosition.i = nextMeaningfulIndex;
        if (nextMeaningfulIndex >= str.length() || isTSpecialChar(str.charAt(stringPosition.i))) {
            throw new IllegalArgumentException();
        }
        do {
            int i = stringPosition.i;
            stringPosition.i = i + 1;
            sb.append(str.charAt(i));
            if (stringPosition.i >= str.length() || !isMeaningfulChar(str.charAt(stringPosition.i)) || isTSpecialChar(str.charAt(stringPosition.i))) {
            }
            int i2 = stringPosition.i;
            stringPosition.i = i2 + 1;
            sb.append(str.charAt(i2));
            break;
        } while (isTSpecialChar(str.charAt(stringPosition.i)));
        return sb.toString();
    }

    private static int getNextMeaningfulIndex(String str, int i) {
        while (i < str.length() && !isMeaningfulChar(str.charAt(i))) {
            i++;
        }
        return i;
    }

    private static final class StringPosition {
        int i;

        private StringPosition() {
            this.i = 0;
        }

        /* synthetic */ StringPosition(StringPosition stringPosition) {
            this();
        }
    }

    static final class MimeType implements Cloneable, Serializable {
        private static final long serialVersionUID = -6693571907475992044L;
        /* access modifiers changed from: private */
        public Hashtable<String, String> parameters;
        /* access modifiers changed from: private */
        public String primaryType;
        /* access modifiers changed from: private */
        public String subType;
        /* access modifiers changed from: private */
        public Hashtable<String, Object> systemParameters;

        MimeType() {
            this.primaryType = null;
            this.subType = null;
            this.parameters = null;
            this.systemParameters = null;
        }

        MimeType(String str, String str2) {
            this.primaryType = str;
            this.subType = str2;
            this.parameters = new Hashtable<>();
            this.systemParameters = new Hashtable<>();
        }

        /* access modifiers changed from: package-private */
        public boolean equals(MimeType mimeType) {
            if (mimeType == null) {
                return false;
            }
            return getFullType().equals(mimeType.getFullType());
        }

        /* access modifiers changed from: package-private */
        public String getPrimaryType() {
            return this.primaryType;
        }

        /* access modifiers changed from: package-private */
        public String getSubType() {
            return this.subType;
        }

        /* access modifiers changed from: package-private */
        public String getFullType() {
            return String.valueOf(this.primaryType) + "/" + this.subType;
        }

        /* access modifiers changed from: package-private */
        public String getParameter(String str) {
            return this.parameters.get(str);
        }

        /* access modifiers changed from: package-private */
        public void addParameter(String str, String str2) {
            if (str2 != null) {
                if (str2.charAt(0) == '\"' && str2.charAt(str2.length() - 1) == '\"') {
                    str2 = str2.substring(1, str2.length() - 2);
                }
                if (str2.length() != 0) {
                    this.parameters.put(str, str2);
                }
            }
        }

        public Object clone() {
            MimeType mimeType = new MimeType(this.primaryType, this.subType);
            mimeType.parameters = (Hashtable) this.parameters.clone();
            mimeType.systemParameters = (Hashtable) this.systemParameters.clone();
            return mimeType;
        }
    }
}
