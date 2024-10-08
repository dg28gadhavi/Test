package com.sec.internal.ims.cmstore;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.httpclient.SerializableCookie;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.CookiePersister;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class PersistentHttp3CookieJar implements CookieJar, CookiePersister {
    private static final String COOKIE_NAME_PREFIX = "cookie_";
    private static final String COOKIE_PREFS = "CookiePrefsFile";
    private static final String LOG_TAG = "PersistentHttp3CookieJar";
    private final SharedPreferences cookiePrefs;
    protected final ConcurrentHashMap<String, ConcurrentHashMap<String, Cookie>> cookies;

    public PersistentHttp3CookieJar(Context context, int i) {
        Cookie decodeCookie;
        if (!CmsUtil.isMcsSupported(context, i) || i != 1) {
            this.cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        } else {
            this.cookiePrefs = context.getSharedPreferences(COOKIE_PREFS + i, 0);
        }
        this.cookies = new ConcurrentHashMap<>();
        for (Map.Entry next : this.cookiePrefs.getAll().entrySet()) {
            if (next.getValue() != null && !((String) next.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                for (String str : TextUtils.split((String) next.getValue(), ",")) {
                    String string = this.cookiePrefs.getString(COOKIE_NAME_PREFIX + str, (String) null);
                    if (!(string == null || (decodeCookie = new SerializableCookie().decodeCookie(string)) == null)) {
                        String domain = decodeCookie.domain();
                        if (TextUtils.isEmpty(domain)) {
                            Cookie.Builder builder = new Cookie.Builder();
                            builder.domain((String) next.getKey());
                            builder.build();
                            domain = decodeCookie.domain();
                        }
                        try {
                            this.cookies.putIfAbsent(domain, new ConcurrentHashMap());
                        } catch (NullPointerException e) {
                            IMSLog.e(LOG_TAG, e.toString());
                        }
                        this.cookies.get(domain).put(str, decodeCookie);
                    }
                }
            }
        }
    }

    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry next : this.cookies.entrySet()) {
            if (httpUrl.host().endsWith((String) next.getKey())) {
                arrayList.addAll(this.cookies.get(next.getKey()).values());
            }
        }
        IMSLog.i(LOG_TAG, "load cookie, url:" + IMSLog.numberChecker(String.valueOf(httpUrl)) + " cookie:" + arrayList);
        return arrayList;
    }

    private Cookie setCookieDomain(Cookie cookie, String str) {
        IMSLog.d(LOG_TAG, "setCookieDomain: " + cookie + " host:" + str);
        Cookie.Builder builder = new Cookie.Builder();
        builder.expiresAt(cookie.expiresAt()).name(cookie.name()).path(cookie.path()).domain(str);
        if (cookie.httpOnly()) {
            builder.httpOnly();
        }
        if (cookie.secure()) {
            builder.secure();
        }
        return builder.build();
    }

    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
        IMSLog.i(LOG_TAG, "saveFromResponse - url: " + IMSLog.numberChecker(String.valueOf(httpUrl)) + ", cookie: " + list.toString());
        Iterator<Cookie> it = list.iterator();
        while (it.hasNext()) {
            Cookie next = it.next();
            if (TextUtils.isEmpty(next.domain())) {
                next = setCookieDomain(next, httpUrl.host());
            }
            if (ATTGlobalVariables.isAmbsPhaseIV()) {
                Log.d(LOG_TAG, "Before==================================");
                for (Map.Entry next2 : this.cookies.entrySet()) {
                    try {
                        for (Map.Entry key : ((ConcurrentHashMap) next2.getValue()).entrySet()) {
                            IMSLog.s(LOG_TAG, "Domain=" + ((String) next2.getKey()) + " ,name=" + ((String) key.getKey()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                IMSLog.i(LOG_TAG, "==================================");
                ArrayList<String> arrayList = new ArrayList<>();
                for (Cookie next3 : getCookies()) {
                    if (next.name().equals(next3.name())) {
                        String name = next.name();
                        String name2 = next3.name();
                        if (!name.equals(name2)) {
                            if (name.endsWith(name2)) {
                                next = setCookieDomain(next, name2);
                            } else if (name2.endsWith(name) && !arrayList.contains(name2)) {
                                arrayList.add(name2);
                            }
                        }
                    }
                }
                if (!arrayList.isEmpty()) {
                    for (String str : arrayList) {
                        ConcurrentHashMap concurrentHashMap = this.cookies.get(str);
                        if (concurrentHashMap != null) {
                            try {
                                this.cookies.putIfAbsent(next.name(), new ConcurrentHashMap());
                            } catch (NullPointerException e2) {
                                IMSLog.e(LOG_TAG, e2.toString());
                            }
                            for (Map.Entry value : concurrentHashMap.entrySet()) {
                                Cookie cookieDomain = setCookieDomain((Cookie) value.getValue(), next.domain());
                                this.cookies.get(next.domain()).put(getCookieToken(cookieDomain), cookieDomain);
                            }
                        }
                        this.cookies.remove(str);
                    }
                }
                IMSLog.d(LOG_TAG, "After==================================");
                for (Map.Entry next4 : this.cookies.entrySet()) {
                    try {
                        for (Map.Entry key2 : ((ConcurrentHashMap) next4.getValue()).entrySet()) {
                            IMSLog.s(LOG_TAG, "Domain=" + ((String) next4.getKey()) + " ,name=" + ((String) key2.getKey()));
                        }
                    } catch (Exception e3) {
                        e3.printStackTrace();
                        return;
                    }
                }
                IMSLog.i(LOG_TAG, "==================================");
            }
            String cookieToken = getCookieToken(next);
            IMSLog.i(LOG_TAG, "cookieName:" + cookieToken + " expired:" + isCookieExpired(next));
            if (!isCookieExpired(next)) {
                try {
                    this.cookies.putIfAbsent(next.domain(), new ConcurrentHashMap());
                    IMSLog.d(LOG_TAG, "cookie domain addition in pref");
                } catch (NullPointerException e4) {
                    IMSLog.e(LOG_TAG, e4.toString());
                }
                this.cookies.get(next.domain()).put(cookieToken, next);
            } else if (this.cookies.containsKey(next.domain())) {
                this.cookies.get(next.domain()).remove(cookieToken);
            }
            SharedPreferences.Editor edit = this.cookiePrefs.edit();
            edit.putString(next.domain(), TextUtils.join(",", this.cookies.get(next.domain()).keySet()));
            String encodeCookie = new SerializableCookie().encodeCookie(next);
            edit.putString(COOKIE_NAME_PREFIX + cookieToken, encodeCookie);
            edit.apply();
        }
    }

    public List<Cookie> getCookies() {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, ConcurrentHashMap<String, Cookie>> value : this.cookies.entrySet()) {
            arrayList.addAll(((ConcurrentHashMap) value.getValue()).values());
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public String getCookieToken(Cookie cookie) {
        return cookie.name() + cookie.domain();
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    public void removeAll() {
        SharedPreferences.Editor edit = this.cookiePrefs.edit();
        edit.clear();
        edit.apply();
        this.cookies.clear();
    }
}
