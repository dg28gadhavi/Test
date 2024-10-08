package myjava.awt.datatransfer;

import com.sec.internal.constants.ims.MIMEContentType;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import myjava.awt.datatransfer.MimeTypeProcessor;
import org.apache.harmony.awt.datatransfer.DTK;
import org.apache.harmony.awt.internal.nls.Messages;

public class DataFlavor implements Externalizable, Cloneable {
    public static final DataFlavor javaFileListFlavor = new DataFlavor("application/x-java-file-list; class=java.util.List", "application/x-java-file-list");
    @Deprecated
    public static final DataFlavor plainTextFlavor = new DataFlavor("text/plain; charset=unicode; class=java.io.InputStream", "Plain Text");
    private static DataFlavor plainUnicodeFlavor = null;
    private static final long serialVersionUID = 8367026044764648243L;
    private static final String[] sortedTextFlavors = {"text/sgml", "text/xml", "text/html", "text/rtf", "text/enriched", "text/richtext", "text/uri-list", "text/tab-separated-values", "text/t140", "text/rfc822-headers", "text/parityfec", "text/directory", "text/css", "text/calendar", "application/x-java-serialized-object", MIMEContentType.PLAIN_TEXT};
    public static final DataFlavor stringFlavor = new DataFlavor("application/x-java-serialized-object; class=java.lang.String", "Unicode String");
    private String humanPresentableName;
    private MimeTypeProcessor.MimeType mimeInfo;
    private Class<?> representationClass;

    private static boolean isCharsetSupported(String str) {
        try {
            return Charset.isSupported(str);
        } catch (IllegalCharsetNameException unused) {
            return false;
        }
    }

    public DataFlavor() {
        this.mimeInfo = null;
        this.humanPresentableName = null;
        this.representationClass = null;
    }

    public DataFlavor(String str, String str2) {
        try {
            init(str, str2, (ClassLoader) null);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(Messages.getString("awt.16C", (Object) this.mimeInfo.getParameter("class")), e);
        }
    }

    private void init(String str, String str2, ClassLoader classLoader) throws ClassNotFoundException {
        Class<?> cls;
        try {
            MimeTypeProcessor.MimeType parse = MimeTypeProcessor.parse(str);
            this.mimeInfo = parse;
            if (str2 != null) {
                this.humanPresentableName = str2;
            } else {
                this.humanPresentableName = String.valueOf(parse.getPrimaryType()) + '/' + this.mimeInfo.getSubType();
            }
            String parameter = this.mimeInfo.getParameter("class");
            if (parameter == null) {
                this.mimeInfo.addParameter("class", "java.io.InputStream");
                parameter = "java.io.InputStream";
            }
            if (classLoader == null) {
                cls = Class.forName(parameter);
            } else {
                cls = classLoader.loadClass(parameter);
            }
            this.representationClass = cls;
        } catch (IllegalArgumentException unused) {
            throw new IllegalArgumentException(Messages.getString("awt.16D", (Object) str));
        }
    }

    private String getCharset() {
        if (this.mimeInfo == null || isCharsetRedundant()) {
            return "";
        }
        String parameter = this.mimeInfo.getParameter("charset");
        if (isCharsetRequired() && (parameter == null || parameter.length() == 0)) {
            return DTK.getDTK().getDefaultCharset();
        }
        if (parameter == null) {
            return "";
        }
        return parameter;
    }

    private boolean isCharsetRequired() {
        String fullType = this.mimeInfo.getFullType();
        return fullType.equals("text/sgml") || fullType.equals("text/xml") || fullType.equals("text/html") || fullType.equals("text/enriched") || fullType.equals("text/richtext") || fullType.equals("text/uri-list") || fullType.equals("text/directory") || fullType.equals("text/css") || fullType.equals("text/calendar") || fullType.equals("application/x-java-serialized-object") || fullType.equals(MIMEContentType.PLAIN_TEXT);
    }

    private boolean isCharsetRedundant() {
        String fullType = this.mimeInfo.getFullType();
        return fullType.equals("text/rtf") || fullType.equals("text/tab-separated-values") || fullType.equals("text/t140") || fullType.equals("text/rfc822-headers") || fullType.equals("text/parityfec");
    }

    public String getMimeType() {
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType != null) {
            return MimeTypeProcessor.assemble(mimeType);
        }
        return null;
    }

    public synchronized void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(this.humanPresentableName);
        objectOutput.writeObject(this.mimeInfo);
    }

    public synchronized void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        this.humanPresentableName = (String) objectInput.readObject();
        MimeTypeProcessor.MimeType mimeType = (MimeTypeProcessor.MimeType) objectInput.readObject();
        this.mimeInfo = mimeType;
        this.representationClass = mimeType != null ? Class.forName(mimeType.getParameter("class")) : null;
    }

    public Object clone() throws CloneNotSupportedException {
        DataFlavor dataFlavor = new DataFlavor();
        dataFlavor.humanPresentableName = this.humanPresentableName;
        dataFlavor.representationClass = this.representationClass;
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        dataFlavor.mimeInfo = mimeType != null ? (MimeTypeProcessor.MimeType) mimeType.clone() : null;
        return dataFlavor;
    }

    public String toString() {
        return getClass().getName() + "[MimeType=(" + getMimeType() + ");humanPresentableName=" + this.humanPresentableName + "]";
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DataFlavor)) {
            return false;
        }
        return equals((DataFlavor) obj);
    }

    public boolean equals(DataFlavor dataFlavor) {
        if (dataFlavor == this) {
            return true;
        }
        if (dataFlavor == null) {
            return false;
        }
        MimeTypeProcessor.MimeType mimeType = this.mimeInfo;
        if (mimeType == null) {
            return dataFlavor.mimeInfo == null;
        }
        if (!mimeType.equals(dataFlavor.mimeInfo) || !this.representationClass.equals(dataFlavor.representationClass)) {
            return false;
        }
        if (!this.mimeInfo.getPrimaryType().equals("text") || isUnicodeFlavor()) {
            return true;
        }
        String charset = getCharset();
        String charset2 = dataFlavor.getCharset();
        if (!isCharsetSupported(charset) || !isCharsetSupported(charset2)) {
            return charset.equalsIgnoreCase(charset2);
        }
        return Charset.forName(charset).equals(Charset.forName(charset2));
    }

    public int hashCode() {
        return getKeyInfo().hashCode();
    }

    private String getKeyInfo() {
        String str = String.valueOf(this.mimeInfo.getFullType()) + ";class=" + this.representationClass.getName();
        if (!this.mimeInfo.getPrimaryType().equals("text") || isUnicodeFlavor()) {
            return str;
        }
        return String.valueOf(str) + ";charset=" + getCharset().toLowerCase();
    }

    private boolean isUnicodeFlavor() {
        Class<?> cls = this.representationClass;
        if (cls != null) {
            return cls.equals(Reader.class) || this.representationClass.equals(String.class) || this.representationClass.equals(CharBuffer.class) || this.representationClass.equals(char[].class);
        }
        return false;
    }
}
