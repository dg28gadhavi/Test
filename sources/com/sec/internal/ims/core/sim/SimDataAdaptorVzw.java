package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber$PhoneNumber;
import com.sec.internal.helper.MccTable;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorVzw extends SimDataAdaptor {
    private static final String LOG_TAG = "SimDataAdaptorVzw";

    SimDataAdaptorVzw(SimManager simManager) {
        super(simManager);
    }

    /* access modifiers changed from: package-private */
    public boolean checkAvailableImpu(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "checkAvailableImpu:");
        String simOperator = this.mSimManager.getSimOperator();
        if (this.mSimManager.isLabSimCard()) {
            Log.i(str2, "LAB SIM inserted. return true..");
            return true;
        }
        String msisdn = this.mSimManager.getMsisdn();
        if (!TextUtils.isEmpty(msisdn) && !TextUtils.isEmpty(str)) {
            List asList = Arrays.asList(str.replaceAll("[^+?0-9]+", " ").trim().split(" "));
            if (asList.size() > 0) {
                IMSLog.s(str2, "impuNumber: " + ((String) asList.get(0)) + ", msisdn: " + msisdn);
                if (TextUtils.isEmpty(simOperator) || simOperator.length() < 3) {
                    Log.e(str2, "SimController : refresh: SIM operator is unknown.");
                } else {
                    PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
                    try {
                        String upperCase = MccTable.countryCodeForMcc(Integer.parseInt(simOperator.substring(0, 3))).toUpperCase();
                        Phonenumber$PhoneNumber parse = instance.parse((CharSequence) asList.get(0), upperCase);
                        Phonenumber$PhoneNumber parse2 = instance.parse(msisdn, upperCase);
                        IMSLog.s(str2, "impu: " + parse + ", msisdn: " + parse2);
                        PhoneNumberUtil.MatchType isNumberMatch = instance.isNumberMatch(parse, parse2);
                        StringBuilder sb = new StringBuilder();
                        sb.append("checkAvailableImpu: ");
                        sb.append(isNumberMatch);
                        IMSLog.s(str2, sb.toString());
                        if (isNumberMatch != PhoneNumberUtil.MatchType.NO_MATCH) {
                            return true;
                        }
                        return false;
                    } catch (NumberParseException | NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public String getEmergencyImpu(List<String> list) {
        Log.i(LOG_TAG, "getEmergencyImpu:");
        if (!(list == null || list.size() == 0)) {
            for (String next : list) {
                if (checkAvailableImpu(next)) {
                    return next;
                }
            }
        }
        return null;
    }

    public String getImpuFromList(List<String> list) {
        Log.i(LOG_TAG, "getImpuFromList:");
        if (!(list == null || list.size() == 0)) {
            String impuFromList = super.getImpuFromList(list);
            if (list.size() > 1 && !TextUtils.isEmpty(list.get(1)) && list.get(1).equals(impuFromList) && checkAvailableImpu(impuFromList)) {
                return impuFromList;
            }
            if (!TextUtils.isEmpty(list.get(0))) {
                return list.get(0);
            }
        }
        return null;
    }
}
