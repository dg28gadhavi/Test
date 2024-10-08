package javax.activation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Locale;

public class MimeType implements Externalizable {
    private MimeTypeParameterList parameters;
    private String primaryType;
    private String subType;

    public MimeType() {
        this.primaryType = "application";
        this.subType = "*";
        this.parameters = new MimeTypeParameterList();
    }

    public MimeType(String str) throws MimeTypeParseException {
        parse(str);
    }

    private void parse(String str) throws MimeTypeParseException {
        int indexOf = str.indexOf(47);
        int indexOf2 = str.indexOf(59);
        if (indexOf < 0 && indexOf2 < 0) {
            throw new MimeTypeParseException("Unable to find a sub type.");
        } else if (indexOf >= 0 || indexOf2 < 0) {
            if (indexOf >= 0 && indexOf2 < 0) {
                String trim = str.substring(0, indexOf).trim();
                Locale locale = Locale.ENGLISH;
                this.primaryType = trim.toLowerCase(locale);
                this.subType = str.substring(indexOf + 1).trim().toLowerCase(locale);
                this.parameters = new MimeTypeParameterList();
            } else if (indexOf < indexOf2) {
                String trim2 = str.substring(0, indexOf).trim();
                Locale locale2 = Locale.ENGLISH;
                this.primaryType = trim2.toLowerCase(locale2);
                this.subType = str.substring(indexOf + 1, indexOf2).trim().toLowerCase(locale2);
                this.parameters = new MimeTypeParameterList(str.substring(indexOf2));
            } else {
                throw new MimeTypeParseException("Unable to find a sub type.");
            }
            if (!isValidToken(this.primaryType)) {
                throw new MimeTypeParseException("Primary type is invalid.");
            } else if (!isValidToken(this.subType)) {
                throw new MimeTypeParseException("Sub type is invalid.");
            }
        } else {
            throw new MimeTypeParseException("Unable to find a sub type.");
        }
    }

    public String toString() {
        return String.valueOf(getBaseType()) + this.parameters.toString();
    }

    public String getBaseType() {
        return String.valueOf(this.primaryType) + "/" + this.subType;
    }

    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(toString());
        objectOutput.flush();
    }

    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        try {
            parse(objectInput.readUTF());
        } catch (MimeTypeParseException e) {
            throw new IOException(e.toString());
        }
    }

    private static boolean isTokenChar(char c) {
        return c > ' ' && c < 127 && "()<>@,;:/[]?=\\\"".indexOf(c) < 0;
    }

    private boolean isValidToken(String str) {
        int length = str.length();
        if (length <= 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!isTokenChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
