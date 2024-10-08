package com.sec.internal.ims.config.workflow;

import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WorkflowCookieHandler {
    private static final String LOG_TAG = "WorkflowCookieHandler";
    protected final CookieManager mCookieManager;
    protected int mPhoneId;
    protected WorkflowBase mWorkflowBase;

    public WorkflowCookieHandler(WorkflowBase workflowBase, int i) {
        CookieManager cookieManager = new CookieManager();
        this.mCookieManager = cookieManager;
        this.mWorkflowBase = workflowBase;
        this.mPhoneId = i;
        CookieHandler.setDefault(cookieManager);
    }

    /* access modifiers changed from: protected */
    public URI getUri() {
        try {
            return new URI(this.mWorkflowBase.mSharedInfo.getUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public List<HttpCookie> getCookie(URI uri) {
        return this.mCookieManager.getCookieStore().get(uri);
    }

    /* access modifiers changed from: protected */
    public void displayCookieInfo(HttpCookie httpCookie) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "cookie name:" + httpCookie.getName());
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "cookie value:" + httpCookie.getValue());
        int i3 = this.mPhoneId;
        IMSLog.i(str, i3, "cookie domain:" + httpCookie.getDomain());
        int i4 = this.mPhoneId;
        IMSLog.i(str, i4, "cookie path:" + httpCookie.getPath());
        int i5 = this.mPhoneId;
        IMSLog.i(str, i5, "cookie max age:" + httpCookie.getMaxAge());
    }

    /* access modifiers changed from: protected */
    public boolean isCookie(IHttpAdapter.Response response) {
        if (response.getHeader().containsKey(HttpController.HEADER_SET_COOKIE)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "cookie exist");
            for (HttpCookie displayCookieInfo : getCookie(getUri())) {
                displayCookieInfo(displayCookieInfo);
            }
            return true;
        }
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "cookie does not exist");
        IMSLog.c(LogClass.WFB_NO_COOKIE, this.mWorkflowBase.mPhoneId + ",NOC");
        WorkflowBase workflowBase = this.mWorkflowBase;
        workflowBase.addEventLog(str + ": cookie does not exist");
        return false;
    }

    /* access modifiers changed from: protected */
    public void clearCookie() {
        URI uri = getUri();
        for (HttpCookie next : getCookie(uri)) {
            this.mCookieManager.getCookieStore().remove(uri, next);
            displayRemovedCookieInfo(next.getName());
        }
    }

    /* access modifiers changed from: protected */
    public void displayRemovedCookieInfo(String str) {
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "removed cookie: " + str);
    }

    /* access modifiers changed from: protected */
    public void handleCookie(IHttpAdapter.Response response) {
        if (response != null && response.getHeader().containsKey(HttpController.HEADER_SET_COOKIE)) {
            Mno mno = SimUtil.getMno();
            String str = LOG_TAG;
            IMSLog.i(str, this.mPhoneId, "handleCookie: cookie exists, adding in header");
            if (ConfigUtil.isRcsChn(mno)) {
                ArrayList arrayList = new ArrayList();
                List<String> list = this.mWorkflowBase.mSharedInfo.getHttpResponse().getHeader().get(HttpController.HEADER_SET_COOKIE);
                IMSLog.i(str, this.mPhoneId, "handleCookie: cookie = " + list);
                for (String split : list) {
                    String[] split2 = split.split(";");
                    StringBuilder sb = new StringBuilder();
                    for (String str2 : split2) {
                        if (!str2.startsWith("Max-Age")) {
                            if (sb.length() == 0) {
                                sb.append(str2);
                            } else {
                                sb.append(";" + str2);
                            }
                        }
                    }
                    arrayList.add(sb.toString());
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleCookie: remove Max-Age = " + arrayList);
                this.mWorkflowBase.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, arrayList);
            } else if (!ConfigUtil.shallUsePreviousCookie(response.getStatusCode(), mno)) {
                StringBuilder sb2 = new StringBuilder();
                for (String split3 : response.getHeader().get(HttpController.HEADER_SET_COOKIE)) {
                    for (String trim : split3.split(";")) {
                        String trim2 = trim.trim();
                        if (sb2.length() != 0) {
                            sb2.append("; ");
                        }
                        sb2.append(trim2);
                    }
                }
                if (sb2.length() != 0) {
                    ArrayList arrayList2 = new ArrayList(1);
                    arrayList2.add(sb2.toString());
                    this.mWorkflowBase.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, arrayList2);
                }
            }
        }
    }
}
