package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.XmlUtils;
import com.sec.internal.ims.core.sim.NetworkIdentifier;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

class CscNetParser {
    private static final String CUSTOMER_CSC_FILE_NAME = "/customer.xml";
    private static final String CUSTOMER_CSC_FILE_PATH = "/system/csc";
    private static final String LOG_TAG = "MnoMap_CscNetParser";
    private static final String NETWORK_INFO_PATH = "CustomerData.GeneralInfo.NetworkInfo";
    private FileInputStream mFileInputStream;
    public ArrayList<CscNetwork> mNetworkInfoList = new ArrayList<>();
    private int mPhoneId;

    static class CscNetwork {
        private static final String LOG_TAG = "MnoMap_CscNetwork";
        private ArrayList<NetworkIdentifier> mIdentifiers = new ArrayList<>();
        private String mNetworkName;

        public CscNetwork(String str) {
            this.mNetworkName = str;
        }

        public String getNetworkName() {
            return this.mNetworkName;
        }

        public ArrayList<NetworkIdentifier> getIdentifiers() {
            return this.mIdentifiers;
        }

        public void addIdentifier(NetworkIdentifier networkIdentifier) {
            this.mIdentifiers.add(networkIdentifier);
        }

        public void setMnoName(String str) {
            Log.i(LOG_TAG, "setMnoName for " + this.mNetworkName + " " + str);
            Iterator<NetworkIdentifier> it = this.mIdentifiers.iterator();
            while (it.hasNext()) {
                it.next().setMnoName(str);
            }
        }
    }

    public CscNetParser(int i) {
        this.mPhoneId = i;
    }

    public ArrayList<CscNetwork> getCscNetworkInfo() {
        parseNetworkInfo();
        return this.mNetworkInfoList;
    }

    private void parseNetworkInfo() {
        XmlPullParser cscCustomerParser = getCscCustomerParser();
        if (cscCustomerParser == null || isInvalidCustomerFile(cscCustomerParser)) {
            Log.e(LOG_TAG, cscCustomerParser == null ? "XmlPullParser is null!" : "Can not find <NetworkInfo> from customer.xml");
            closeFileInputStream();
            return;
        }
        try {
            NetworkIdentifier.Builder builder = new NetworkIdentifier.Builder();
            while (true) {
                int next = cscCustomerParser.next();
                if (next == 1) {
                    break;
                } else if (next == 2) {
                    builder.setNext(cscCustomerParser);
                } else if (next != 3) {
                    continue;
                } else if ("NetworkInfo".equalsIgnoreCase(cscCustomerParser.getName())) {
                    int i = this.mPhoneId;
                    IMSLog.i(LOG_TAG, i, "" + builder);
                    if (!builder.isInvalid()) {
                        addNewCscNetwork(builder.getNetworkName(), builder.build());
                    }
                } else if ("GeneralInfo".equalsIgnoreCase(cscCustomerParser.getName())) {
                    break;
                }
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            closeFileInputStream();
            throw th;
        }
        closeFileInputStream();
    }

    private boolean isInvalidCustomerFile(XmlPullParser xmlPullParser) {
        return !XmlUtils.search(xmlPullParser, NETWORK_INFO_PATH);
    }

    private void addNewCscNetwork(String str, NetworkIdentifier networkIdentifier) {
        ((CscNetwork) this.mNetworkInfoList.stream().filter(new CscNetParser$$ExternalSyntheticLambda0(str)).findFirst().orElseGet(new CscNetParser$$ExternalSyntheticLambda1(this, str))).addIdentifier(networkIdentifier);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ CscNetwork lambda$addNewCscNetwork$1(String str) {
        CscNetwork cscNetwork = new CscNetwork(str);
        this.mNetworkInfoList.add(cscNetwork);
        return cscNetwork;
    }

    private XmlPullParser getCscCustomerParser() {
        String str;
        IMSLog.i(LOG_TAG, this.mPhoneId, "getCscCustomerParser:");
        String omcNwPath = OmcCode.getOmcNwPath(this.mPhoneId);
        if (SimUtil.getSimMno(this.mPhoneId) == Mno.DEFAULT) {
            return null;
        }
        if (!TextUtils.isEmpty(omcNwPath)) {
            str = omcNwPath + CUSTOMER_CSC_FILE_NAME;
        } else {
            str = "/system/csc/customer.xml";
        }
        Log.i(LOG_TAG, "customerPath = " + str);
        File file = new File(str);
        if (!file.exists()) {
            Log.e(LOG_TAG, "customer.xml file does not exists");
            return null;
        }
        try {
            XmlPullParserFactory newInstance = XmlPullParserFactory.newInstance();
            newInstance.setNamespaceAware(true);
            XmlPullParser newPullParser = newInstance.newPullParser();
            FileInputStream fileInputStream = new FileInputStream(file);
            this.mFileInputStream = fileInputStream;
            newPullParser.setInput(fileInputStream, (String) null);
            return newPullParser;
        } catch (FileNotFoundException | XmlPullParserException e) {
            e.printStackTrace();
            closeFileInputStream();
            return null;
        }
    }

    private void closeFileInputStream() {
        FileInputStream fileInputStream = this.mFileInputStream;
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                this.mFileInputStream = null;
                throw th;
            }
            this.mFileInputStream = null;
        }
    }
}
