package org.apache.harmony.awt.internal.nls;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static ResourceBundle bundle;

    public static String getString(String str) {
        ResourceBundle resourceBundle = bundle;
        if (resourceBundle == null) {
            return str;
        }
        try {
            return resourceBundle.getString(str);
        } catch (MissingResourceException unused) {
            return "Missing message: " + str;
        }
    }

    public static String getString(String str, Object obj) {
        return getString(str, new Object[]{obj});
    }

    public static String getString(String str, Object[] objArr) {
        ResourceBundle resourceBundle = bundle;
        if (resourceBundle != null) {
            try {
                str = resourceBundle.getString(str);
            } catch (MissingResourceException unused) {
            }
        }
        return format(str, objArr);
    }

    public static String format(String str, Object[] objArr) {
        int i;
        StringBuilder sb = new StringBuilder(str.length() + (objArr.length * 20));
        int length = objArr.length;
        String[] strArr = new String[length];
        int i2 = 0;
        for (int i3 = 0; i3 < objArr.length; i3++) {
            Object obj = objArr[i3];
            if (obj == null) {
                strArr[i3] = "<null>";
            } else {
                strArr[i3] = obj.toString();
            }
        }
        while (true) {
            int indexOf = str.indexOf(123, i2);
            if (indexOf < 0) {
                break;
            }
            if (indexOf != 0) {
                int i4 = indexOf - 1;
                if (str.charAt(i4) == '\\') {
                    if (indexOf != 1) {
                        sb.append(str.substring(i2, i4));
                    }
                    sb.append('{');
                    i = indexOf + 1;
                    i2 = i;
                }
            }
            if (indexOf > str.length() - 3) {
                sb.append(str.substring(i2, str.length()));
                i = str.length();
            } else {
                int i5 = indexOf + 1;
                byte digit = (byte) Character.digit(str.charAt(i5), 10);
                if (digit < 0 || str.charAt(indexOf + 2) != '}') {
                    sb.append(str.substring(i2, i5));
                    i2 = i5;
                } else {
                    sb.append(str.substring(i2, indexOf));
                    if (digit >= length) {
                        sb.append("<missing argument>");
                    } else {
                        sb.append(strArr[digit]);
                    }
                    i = indexOf + 3;
                }
            }
            i2 = i;
        }
        if (i2 < str.length()) {
            sb.append(str.substring(i2, str.length()));
        }
        return sb.toString();
    }

    public static ResourceBundle setLocale(final Locale locale, final String str) {
        try {
            return (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction<Object>((ClassLoader) null) {
                public Object run() {
                    String str = str;
                    Locale locale = locale;
                    ClassLoader classLoader = null;
                    if (classLoader == null) {
                        classLoader = ClassLoader.getSystemClassLoader();
                    }
                    return ResourceBundle.getBundle(str, locale, classLoader);
                }
            });
        } catch (MissingResourceException unused) {
            return null;
        }
    }

    static {
        try {
            bundle = setLocale(Locale.getDefault(), "org.apache.harmony.awt.internal.nls.messages");
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
