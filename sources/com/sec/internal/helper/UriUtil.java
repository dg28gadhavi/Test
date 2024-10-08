package com.sec.internal.helper;

import android.net.Uri;
import android.util.Log;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber$PhoneNumber;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.util.ImsUri;
import java.util.ArrayList;
import java.util.Collection;

public final class UriUtil {
    private static final String LOG_TAG = "UriUtil";

    public static ImsUri parseNumber(String str) {
        return parseNumber(str, (String) null);
    }

    public static ImsUri parseNumber(String str, String str2) {
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        if (str2 == null) {
            str2 = "ZZ";
        }
        try {
            Phonenumber$PhoneNumber parse = instance.parse(str, str2.toUpperCase());
            if (!isShortCode(str, str2)) {
                str = instance.format(parse, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
            return ImsUri.parse("tel:" + str);
        } catch (NumberParseException e) {
            Log.e(LOG_TAG, e.getClass().getSimpleName() + "!! " + e.getMessage());
            return null;
        }
    }

    public static boolean equals(ImsUri imsUri, ImsUri imsUri2) {
        String str;
        if (imsUri == null || imsUri2 == null) {
            return false;
        }
        String str2 = null;
        if ("sip".equalsIgnoreCase(imsUri.getScheme())) {
            str = imsUri.toString().contains("user=phone") ? imsUri.getUser() : null;
        } else {
            str = getMsisdnNumber(imsUri);
        }
        if (!"sip".equalsIgnoreCase(imsUri2.getScheme())) {
            str2 = getMsisdnNumber(imsUri2);
        } else if (imsUri2.toString().contains("user=phone")) {
            str2 = imsUri2.getUser();
        }
        if ((str == null && str2 != null) || (str != null && str2 == null)) {
            return false;
        }
        if (str != null) {
            return str.equals(str2);
        }
        return imsUri.equals(imsUri2);
    }

    public static boolean hasMsisdnNumber(ImsUri imsUri) {
        if (imsUri == null) {
            return false;
        }
        if ("tel".equalsIgnoreCase(imsUri.getScheme())) {
            return true;
        }
        String user = imsUri.getUser();
        return imsUri.toString().contains("user=phone") || (user != null && user.matches("[\\+\\d]+"));
    }

    public static String getMsisdnNumber(ImsUri imsUri) {
        if (imsUri == null) {
            return null;
        }
        if ("tel".equalsIgnoreCase(imsUri.getScheme())) {
            String imsUri2 = imsUri.toString();
            int indexOf = imsUri2.indexOf(59);
            if (indexOf > 0) {
                return imsUri2.substring(4, indexOf);
            }
            return imsUri2.substring(4);
        }
        String user = imsUri.getUser();
        if (imsUri.toString().contains("user=phone")) {
            return user;
        }
        if (user == null) {
            Log.d(LOG_TAG, "user is null. uri: " + imsUri.toString());
            return null;
        } else if (user.matches("[\\+\\d]+")) {
            return user;
        } else {
            return null;
        }
    }

    public static boolean isValidNumber(String str, String str2) {
        if (!(str == null || str2 == null)) {
            if (str.contains("#") || str.contains("*") || str.contains(",") || str.contains("N")) {
                Log.e(LOG_TAG, "isValidNumber: invalid special character in number");
            } else {
                PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
                try {
                    PhoneNumberUtil.ValidationResult isPossibleNumberWithReason = instance.isPossibleNumberWithReason(instance.parse(str, str2.toUpperCase()));
                    if (isPossibleNumberWithReason == PhoneNumberUtil.ValidationResult.IS_POSSIBLE) {
                        return true;
                    }
                    if (isPossibleNumberWithReason == PhoneNumberUtil.ValidationResult.TOO_LONG && "US".equalsIgnoreCase(str2) && str.length() > 9) {
                        return true;
                    }
                    if (isPossibleNumberWithReason == PhoneNumberUtil.ValidationResult.IS_POSSIBLE_LOCAL_ONLY && str.length() == 3 && "KR".equalsIgnoreCase(str2)) {
                        return true;
                    }
                    return false;
                } catch (NumberParseException unused) {
                    return false;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean isShortCode(String str, String str2) {
        if ("US".equalsIgnoreCase(str2)) {
            if (str.length() < 10) {
                return true;
            }
            if (str.length() == 10 && (str.charAt(0) == '0' || str.charAt(0) == '1')) {
                return true;
            }
            if (str.length() == 11 && str.charAt(0) == '1' && (str.charAt(1) == '0' || str.charAt(1) == '1')) {
                return true;
            }
            if (str.startsWith("+1") && str.length() == 12 && (str.charAt(2) == '0' || str.charAt(2) == '1')) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static Uri buildUri(String str, int i) {
        Uri.Builder buildUpon = Uri.parse(str).buildUpon();
        return buildUpon.fragment("simslot" + i).build();
    }

    public static Uri buildUri(Uri uri, int i) {
        Uri.Builder buildUpon = uri.buildUpon();
        return buildUpon.fragment("simslot" + i).build();
    }

    public static int getSimSlotFromUri(Uri uri) {
        if (uri.getFragment() == null) {
            Log.i(LOG_TAG, "fragment is null. get simSlot from priority policy.");
            if (uri.toString().contains("#simslot")) {
                Log.d(LOG_TAG, "this should not happen: " + uri.toString());
            }
            return SimUtil.getSimSlotPriority();
        } else if (uri.getFragment().contains("subid")) {
            int numericValue = Character.getNumericValue(uri.getFragment().charAt(5));
            if (numericValue >= 0) {
                return Extensions.SubscriptionManager.getSlotId(numericValue);
            }
            Log.i(LOG_TAG, "Invalid subId:" + numericValue + ". get simSlot from priority policy");
            return SimUtil.getSimSlotPriority();
        } else if (uri.getFragment().contains("simslot")) {
            int numericValue2 = Character.getNumericValue(uri.getFragment().charAt(7));
            if (numericValue2 >= 0) {
                return numericValue2;
            }
            Log.i(LOG_TAG, "Invalid simslot:" + numericValue2 + ". get it from priority policy");
            return SimUtil.getSimSlotPriority();
        } else {
            Log.i(LOG_TAG, "Invalid fragment:" + uri.getFragment() + ". get simSlot from priority policy");
            return SimUtil.getSimSlotPriority();
        }
    }

    public static ArrayList<ImsUri> convertToUriList(Collection<String> collection) {
        ArrayList<ImsUri> arrayList = new ArrayList<>();
        for (String next : collection) {
            if (next != null) {
                arrayList.add(ImsUri.parse(next));
            }
        }
        return arrayList;
    }

    public static ArrayList<String> convertToStringList(Collection<ImsUri> collection) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (ImsUri next : collection) {
            if (next != null) {
                arrayList.add(next.toString());
            }
        }
        return arrayList;
    }
}
