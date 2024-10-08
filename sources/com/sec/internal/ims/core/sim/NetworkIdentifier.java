package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

class NetworkIdentifier {
    private static final String LOG_TAG = "MnoMap_NetworkIdentifier";
    private String mGid1;
    private String mGid2;
    private String mMccMnc;
    private String mMnoName;
    private String mSpName;
    private String mSubset;

    static class Builder {
        private static final List<String> LAB_SIM = Arrays.asList(new String[]{"00101", "001001", "001010", "00101f", "99999"});
        private String mGid1;
        private boolean mIsHexadecimal;
        private String mMccMnc;
        private String mNetworkName;
        private String mSpName;
        private String mSubset;

        Builder() {
        }

        /* access modifiers changed from: package-private */
        public String getNetworkName() {
            return this.mNetworkName;
        }

        /* access modifiers changed from: package-private */
        public void setNext(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
            String name = xmlPullParser.getName();
            if ("NetworkInfo".equalsIgnoreCase(name)) {
                clean();
            } else if (ISimManager.KEY_NW_NAME.equalsIgnoreCase(name)) {
                this.mNetworkName = getNextText(xmlPullParser);
            } else if ("MCCMNC".equalsIgnoreCase(name)) {
                this.mMccMnc = getNextText(xmlPullParser);
            } else if ("SPCode".equalsIgnoreCase(name)) {
                this.mSubset = getNextText(xmlPullParser);
            } else if ("CodeType".equalsIgnoreCase(name)) {
                this.mIsHexadecimal = "HEX".equalsIgnoreCase(getNextText(xmlPullParser));
            } else if ("SubsetCode".equalsIgnoreCase(name)) {
                this.mGid1 = getNextText(xmlPullParser);
            } else if ("Spname".equalsIgnoreCase(name)) {
                this.mSpName = getNextText(xmlPullParser);
            }
        }

        private void clean() {
            this.mNetworkName = "";
            this.mMccMnc = "";
            this.mSubset = "";
            this.mIsHexadecimal = false;
            this.mGid1 = "";
            this.mSpName = "";
        }

        private String getNextText(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
            do {
            } while (xmlPullParser.next() != 4);
            return xmlPullParser.getText();
        }

        /* access modifiers changed from: package-private */
        public NetworkIdentifier build() {
            if (!this.mIsHexadecimal) {
                convertGid1ToHex();
            }
            return new NetworkIdentifier(this.mMccMnc, this.mSubset, this.mGid1, "", this.mSpName);
        }

        private void convertGid1ToHex() {
            try {
                this.mGid1 = Integer.toHexString(Integer.parseInt(this.mGid1)).toUpperCase();
            } catch (NumberFormatException unused) {
                Log.i(NetworkIdentifier.LOG_TAG, "invalid NetworkInfo have CodeType, but no gid1");
                this.mGid1 = "";
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isInvalid() {
            return TextUtils.isEmpty(this.mNetworkName) || "GCF".equalsIgnoreCase(this.mNetworkName) || TextUtils.isEmpty(this.mMccMnc) || LAB_SIM.contains(this.mMccMnc);
        }

        public String toString() {
            return String.format(Locale.US, "mccmnc: %s, networkName: %s, subset: %s, gid1: %s, spname: %s", new Object[]{this.mMccMnc, this.mNetworkName, this.mSubset, this.mGid1, this.mSpName});
        }
    }

    public NetworkIdentifier(String str, String str2, String str3, String str4, String str5) {
        this.mMccMnc = str;
        this.mSubset = str2;
        this.mGid1 = str3;
        this.mGid2 = str4;
        this.mSpName = str5;
        this.mMnoName = "default";
    }

    public NetworkIdentifier(String str, String str2, String str3, String str4, String str5, String str6) {
        this.mMccMnc = str;
        this.mSubset = str2;
        this.mGid1 = str3;
        this.mGid2 = str4;
        this.mSpName = str5;
        this.mMnoName = str6;
    }

    public String getMccMnc() {
        return this.mMccMnc;
    }

    public void setMccMnc(String str) {
        this.mMccMnc = str;
    }

    public String getSubset() {
        return this.mSubset;
    }

    public void setSubset(String str) {
        this.mSubset = str;
    }

    public String getGid1() {
        return this.mGid1;
    }

    public void setGid1(String str) {
        this.mGid1 = str;
    }

    public String getGid2() {
        return this.mGid2;
    }

    public void setGid2(String str) {
        this.mGid2 = str;
    }

    public String getSpName() {
        return this.mSpName;
    }

    public void setSpName(String str) {
        this.mSpName = str;
    }

    public String getMnoName() {
        return this.mMnoName;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkIdentifier)) {
            return false;
        }
        NetworkIdentifier networkIdentifier = (NetworkIdentifier) obj;
        if (!this.mMccMnc.equals(networkIdentifier.mMccMnc) || !this.mSubset.equals(networkIdentifier.mSubset) || !this.mGid1.equals(networkIdentifier.mGid1) || !this.mGid2.equals(networkIdentifier.mGid2) || !this.mSpName.equals(networkIdentifier.mSpName) || !this.mMnoName.equalsIgnoreCase(networkIdentifier.mMnoName)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mMccMnc, this.mMnoName, this.mGid1, this.mGid2, this.mSubset, this.mSpName});
    }

    public boolean equalsWithoutMnoName(NetworkIdentifier networkIdentifier) {
        if (!this.mMccMnc.equals(networkIdentifier.mMccMnc) || !this.mSubset.equals(networkIdentifier.mSubset) || !this.mGid1.equals(networkIdentifier.mGid1) || !this.mGid2.equals(networkIdentifier.mGid2) || !this.mSpName.equals(networkIdentifier.mSpName)) {
            return false;
        }
        Log.i(LOG_TAG, "equalsWithoutMnoName: L" + toString() + ", R" + networkIdentifier.toString());
        return true;
    }

    public void setMnoName(String str) {
        this.mMnoName = str;
    }

    public boolean contains(NetworkIdentifier networkIdentifier) {
        if (!this.mMccMnc.equals(networkIdentifier.mMccMnc)) {
            return false;
        }
        if (!this.mGid1.isEmpty() && !this.mGid1.equals(networkIdentifier.mGid1)) {
            return false;
        }
        Log.i(LOG_TAG, "contains: L" + toString() + ", R" + networkIdentifier.toString());
        return true;
    }

    public String toString() {
        return "(" + this.mMccMnc + "," + this.mSubset + "," + this.mGid1 + "," + this.mGid2 + "," + this.mSpName + "=>" + this.mMnoName + ")";
    }
}
