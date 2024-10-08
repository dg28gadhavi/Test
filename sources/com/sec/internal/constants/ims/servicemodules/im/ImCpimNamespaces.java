package com.sec.internal.constants.ims.servicemodules.im;

import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ImCpimNamespaces {
    private final Map<String, CpimNamespace> mNamespaces = new HashMap();

    public static class CpimNamespace {
        protected Map<String, ArrayList<String>> mHeaders = new HashMap();
        protected String mName;
        protected String mUri;

        public CpimNamespace(String str, String str2) {
            this.mName = str;
            this.mUri = str2;
        }

        public void addHeader(String str, String str2) {
            if (str != null) {
                String lowerCase = str.toLowerCase(Locale.US);
                if (this.mHeaders.containsKey(lowerCase)) {
                    this.mHeaders.get(lowerCase).add(str2);
                    return;
                }
                ArrayList arrayList = new ArrayList();
                arrayList.add(str2);
                this.mHeaders.put(lowerCase, arrayList);
            }
        }

        public String toString() {
            return "CpimNamespace [mName=" + this.mName + ", mUri=" + this.mUri + ", headers=" + this.mHeaders + "]";
        }
    }

    public void addNamespace(String str, String str2) {
        this.mNamespaces.put(str, new CpimNamespace(str, str2));
    }

    public CpimNamespace getNamespace(String str) {
        return this.mNamespaces.get(str);
    }

    public String getFirstHeaderValue(String str, String str2) {
        String lowerCase = str2.toLowerCase(Locale.US);
        if (!this.mNamespaces.containsKey(str)) {
            return null;
        }
        CpimNamespace cpimNamespace = this.mNamespaces.get(str);
        if (cpimNamespace.mHeaders.containsKey(lowerCase)) {
            return (String) cpimNamespace.mHeaders.get(lowerCase).get(0);
        }
        return null;
    }

    public String toString() {
        return "ImCpimNamespaces [mNamespaces=" + IMSLog.checker(this.mNamespaces) + "]";
    }
}
