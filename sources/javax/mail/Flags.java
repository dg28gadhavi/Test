package javax.mail;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

public class Flags implements Cloneable, Serializable {
    private static final long serialVersionUID = 6243590407214169028L;
    private int system_flags = 0;
    private Hashtable user_flags = null;

    public static final class Flag {
        public static final Flag ANSWERED = new Flag(1);
        public static final Flag DELETED = new Flag(2);
        public static final Flag DRAFT = new Flag(4);
        public static final Flag FLAGGED = new Flag(8);
        public static final Flag RECENT = new Flag(16);
        public static final Flag SEEN = new Flag(32);
        public static final Flag USER = new Flag(Integer.MIN_VALUE);
        /* access modifiers changed from: private */
        public int bit;

        private Flag(int i) {
            this.bit = i;
        }
    }

    public Flags() {
    }

    public Flags(Flag flag) {
        this.system_flags = flag.bit | 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Flags)) {
            return false;
        }
        Flags flags = (Flags) obj;
        if (flags.system_flags != this.system_flags) {
            return false;
        }
        Hashtable hashtable = flags.user_flags;
        if (hashtable == null && this.user_flags == null) {
            return true;
        }
        if (hashtable == null || this.user_flags == null || hashtable.size() != this.user_flags.size()) {
            return false;
        }
        Enumeration keys = flags.user_flags.keys();
        while (keys.hasMoreElements()) {
            if (!this.user_flags.containsKey(keys.nextElement())) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int i = this.system_flags;
        Hashtable hashtable = this.user_flags;
        if (hashtable != null) {
            Enumeration keys = hashtable.keys();
            while (keys.hasMoreElements()) {
                i += ((String) keys.nextElement()).hashCode();
            }
        }
        return i;
    }

    public Object clone() {
        Flags flags;
        try {
            flags = (Flags) super.clone();
        } catch (CloneNotSupportedException unused) {
            flags = null;
        }
        Hashtable hashtable = this.user_flags;
        if (!(hashtable == null || flags == null)) {
            flags.user_flags = (Hashtable) hashtable.clone();
        }
        return flags;
    }
}
