package com.sec.internal.ims.translate;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.translate.TranslationException;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EucTranslatorUtil {
    private static final String LOG_TAG = "EucTranslatorUtil";

    private EucTranslatorUtil() {
    }

    public static String getOwnIdentity(int i) throws TranslationException {
        IRegistrationManager registrationManager;
        String str = null;
        if (ImsRegistry.isReady() && (registrationManager = ImsRegistry.getRegistrationManager()) != null) {
            str = registrationManager.getImsiByUserAgentHandle(i);
        }
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        IMSLog.e(LOG_TAG, "Cannot obtain own identity!");
        throw new TranslationException(Integer.valueOf(i));
    }

    static void checkTextLangPair(String str, String str2, boolean z) throws TranslationException {
        if (!z && TextUtils.isEmpty(str2)) {
            throw new TranslationException("RCC.15: A language (lang) attribute must be present with the two letter language codes according to the ISO 639-1");
        } else if (str == null) {
            throw new TranslationException("null text is not allowed");
        }
    }

    static String addLanguage(String str, Set<String> set) {
        if (TextUtils.isEmpty(str)) {
            Log.v(LOG_TAG, "Language is empty, using default!");
            return DeviceLocale.DEFAULT_LANG_VALUE;
        }
        set.add(str);
        return str;
    }

    static String getValue(String str, Map<String, String> map) {
        String str2 = map.get(str);
        if (!TextUtils.isEmpty(str2)) {
            return str2;
        }
        String str3 = LOG_TAG;
        Log.v(str3, "Value for language = " + str + " is empty, getting first in values!");
        Iterator<String> it = map.values().iterator();
        return it.hasNext() ? it.next() : str2;
    }

    static String nullIfEmpty(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return str;
    }
}
