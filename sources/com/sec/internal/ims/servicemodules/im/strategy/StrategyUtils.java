package com.sec.internal.ims.servicemodules.im.strategy;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber$PhoneNumber;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.log.IMSLog;
import java.io.PrintStream;

class StrategyUtils {
    private static final String LOG_TAG = "StrategyUtils";

    StrategyUtils() {
    }

    static boolean isCapabilityValidUriForUS(ImsUri imsUri, int i) {
        if (imsUri == null) {
            return false;
        }
        String msisdnNumber = UriUtil.getMsisdnNumber(imsUri);
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        try {
            Phonenumber$PhoneNumber parse = instance.parse(msisdnNumber, "US");
            PhoneNumberUtil.ValidationResult isPossibleNumberWithReason = instance.isPossibleNumberWithReason(parse);
            if (isPossibleNumberWithReason != PhoneNumberUtil.ValidationResult.IS_POSSIBLE) {
                String str = LOG_TAG;
                IMSLog.s(str, "isCapabilityValidUri: msdn " + msisdnNumber);
                if (isPossibleNumberWithReason != PhoneNumberUtil.ValidationResult.TOO_LONG) {
                    IMSLog.i(str, i, "isCapabilityValidUri: Impossible phone number");
                    return false;
                } else if (msisdnNumber == null || !msisdnNumber.startsWith("+1") || msisdnNumber.length() < 12) {
                    IMSLog.i(str, i, "isCapabilityValidUri: Impossible too long phone number");
                    return false;
                }
            }
            parse.clearCountryCode();
            String valueOf = String.valueOf(parse.getNationalNumber());
            if (valueOf.length() > 3) {
                String substring = valueOf.substring(0, 3);
                if ("900".equals(substring) || (substring.charAt(0) == '8' && substring.charAt(1) == substring.charAt(2))) {
                    IMSLog.i(LOG_TAG, i, "isCapabilityValidUri: 900 8YY contact. invalid request");
                } else {
                    try {
                        if (instance.parse(msisdnNumber, "US").getCountryCode() == 1 && UriUtil.isShortCode(msisdnNumber, "US")) {
                            String str2 = LOG_TAG;
                            IMSLog.i(str2, i, "isCapabilityValidUri: ShortCode. invalid request. msdn " + IMSLog.numberChecker(msisdnNumber));
                            return false;
                        }
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            return false;
        } catch (NumberParseException e2) {
            PrintStream printStream = System.err;
            printStream.println("Not a valid number. NumberParseException was thrown: " + e2);
            return false;
        }
    }
}
