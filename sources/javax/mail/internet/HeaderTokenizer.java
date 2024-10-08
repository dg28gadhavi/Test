package javax.mail.internet;

public class HeaderTokenizer {
    private static final Token EOFToken = new Token(-4, (String) null);
    private int currentPos;
    private String delimiters;
    private int maxPos;
    private int nextPos;
    private int peekPos;
    private boolean skipComments;
    private String string;

    public static class Token {
        private int type;
        private String value;

        public Token(int i, String str) {
            this.type = i;
            this.value = str;
        }

        public int getType() {
            return this.type;
        }

        public String getValue() {
            return this.value;
        }
    }

    public HeaderTokenizer(String str, String str2, boolean z) {
        str = str == null ? "" : str;
        this.string = str;
        this.skipComments = z;
        this.delimiters = str2;
        this.peekPos = 0;
        this.nextPos = 0;
        this.currentPos = 0;
        this.maxPos = str.length();
    }

    public HeaderTokenizer(String str, String str2) {
        this(str, str2, true);
    }

    public Token next() throws ParseException {
        this.currentPos = this.nextPos;
        Token next = getNext();
        int i = this.currentPos;
        this.peekPos = i;
        this.nextPos = i;
        return next;
    }

    public String getRemainder() {
        return this.string.substring(this.nextPos);
    }

    private Token getNext() throws ParseException {
        char charAt;
        String str;
        String str2;
        if (this.currentPos >= this.maxPos) {
            return EOFToken;
        }
        if (skipWhiteSpace() == -4) {
            return EOFToken;
        }
        char charAt2 = this.string.charAt(this.currentPos);
        boolean z = false;
        while (charAt2 == '(') {
            int i = this.currentPos + 1;
            this.currentPos = i;
            int i2 = 1;
            while (i2 > 0) {
                int i3 = this.currentPos;
                if (i3 >= this.maxPos) {
                    break;
                }
                char charAt3 = this.string.charAt(i3);
                if (charAt3 == '\\') {
                    this.currentPos++;
                } else if (charAt3 != 13) {
                    if (charAt3 == '(') {
                        i2++;
                    } else if (charAt3 == ')') {
                        i2--;
                    }
                    this.currentPos++;
                }
                z = true;
                this.currentPos++;
            }
            if (i2 != 0) {
                throw new ParseException("Unbalanced comments");
            } else if (!this.skipComments) {
                if (z) {
                    str2 = filterToken(this.string, i, this.currentPos - 1);
                } else {
                    str2 = this.string.substring(i, this.currentPos - 1);
                }
                return new Token(-3, str2);
            } else if (skipWhiteSpace() == -4) {
                return EOFToken;
            } else {
                charAt2 = this.string.charAt(this.currentPos);
            }
        }
        if (charAt2 == '\"') {
            int i4 = this.currentPos + 1;
            this.currentPos = i4;
            while (true) {
                int i5 = this.currentPos;
                if (i5 < this.maxPos) {
                    char charAt4 = this.string.charAt(i5);
                    if (charAt4 == '\\') {
                        this.currentPos++;
                    } else if (charAt4 != 13) {
                        if (charAt4 == '\"') {
                            int i6 = this.currentPos + 1;
                            this.currentPos = i6;
                            if (z) {
                                str = filterToken(this.string, i4, i6 - 1);
                            } else {
                                str = this.string.substring(i4, i6 - 1);
                            }
                            return new Token(-2, str);
                        }
                        this.currentPos++;
                    }
                    z = true;
                    this.currentPos++;
                } else {
                    throw new ParseException("Unbalanced quoted string");
                }
            }
        } else if (charAt2 < ' ' || charAt2 >= 127 || this.delimiters.indexOf(charAt2) >= 0) {
            this.currentPos++;
            return new Token(charAt2, new String(new char[]{charAt2}));
        } else {
            int i7 = this.currentPos;
            while (true) {
                int i8 = this.currentPos;
                if (i8 < this.maxPos && (charAt = this.string.charAt(i8)) >= ' ' && charAt < 127 && charAt != '(' && charAt != ' ' && charAt != '\"' && this.delimiters.indexOf(charAt) < 0) {
                    this.currentPos++;
                }
            }
            return new Token(-1, this.string.substring(i7, this.currentPos));
        }
    }

    private int skipWhiteSpace() {
        while (true) {
            int i = this.currentPos;
            if (i >= this.maxPos) {
                return -4;
            }
            char charAt = this.string.charAt(i);
            if (charAt != ' ' && charAt != 9 && charAt != 13 && charAt != 10) {
                return this.currentPos;
            }
            this.currentPos++;
        }
    }

    private static String filterToken(String str, int i, int i2) {
        StringBuffer stringBuffer = new StringBuffer();
        boolean z = false;
        boolean z2 = false;
        while (i < i2) {
            char charAt = str.charAt(i);
            if (charAt != 10 || !z) {
                if (z2) {
                    stringBuffer.append(charAt);
                    z = false;
                    z2 = false;
                } else if (charAt == '\\') {
                    z = false;
                    z2 = true;
                } else if (charAt == 13) {
                    z = true;
                } else {
                    stringBuffer.append(charAt);
                }
                i++;
            }
            z = false;
            i++;
        }
        return stringBuffer.toString();
    }
}
