package org.xbill.DNS;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public final class Options {
    private static Map table;

    static {
        try {
            refresh();
        } catch (SecurityException unused) {
        }
    }

    public static void refresh() {
        String property = System.getProperty("dnsjava.options");
        if (property != null) {
            StringTokenizer stringTokenizer = new StringTokenizer(property, ",");
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                int indexOf = nextToken.indexOf(61);
                if (indexOf == -1) {
                    set(nextToken);
                } else {
                    set(nextToken.substring(0, indexOf), nextToken.substring(indexOf + 1));
                }
            }
        }
    }

    public static void set(String str) {
        if (table == null) {
            table = new HashMap();
        }
        table.put(str.toLowerCase(), CloudMessageProviderContract.JsonData.TRUE);
    }

    public static void set(String str, String str2) {
        if (table == null) {
            table = new HashMap();
        }
        table.put(str.toLowerCase(), str2.toLowerCase());
    }

    public static boolean check(String str) {
        Map map = table;
        if (map == null || map.get(str.toLowerCase()) == null) {
            return false;
        }
        return true;
    }
}
