package com.sec.internal.helper.entitlement.softphone;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.log.IMSLog;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

public class SoftphoneResponseUtils {
    private static final String LOG_TAG = "SoftphoneResponseUtils";
    public static final XmlParserCreator PARSER_CREATOR = new XmlParserCreator() {
        /* JADX WARNING: Code restructure failed: missing block: B:4:?, code lost:
            r2 = org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser();
            r2.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0013, code lost:
            return r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
            throw new java.lang.RuntimeException(r2);
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0005 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public org.xmlpull.v1.XmlPullParser createParser() {
            /*
                r2 = this;
                org.xmlpull.v1.XmlPullParser r2 = android.util.Xml.newPullParser()     // Catch:{ Exception -> 0x0005 }
                return r2
            L_0x0005:
                org.xmlpull.v1.XmlPullParserFactory r2 = org.xmlpull.v1.XmlPullParserFactory.newInstance()     // Catch:{ Exception -> 0x0014 }
                org.xmlpull.v1.XmlPullParser r2 = r2.newPullParser()     // Catch:{ Exception -> 0x0014 }
                java.lang.String r0 = "http://xmlpull.org/v1/doc/features.html#process-namespaces"
                r1 = 1
                r2.setFeature(r0, r1)     // Catch:{ Exception -> 0x0014 }
                return r2
            L_0x0014:
                r2 = move-exception
                java.lang.RuntimeException r0 = new java.lang.RuntimeException
                r0.<init>(r2)
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.entitlement.softphone.SoftphoneResponseUtils.AnonymousClass1.createParser():org.xmlpull.v1.XmlPullParser");
        }
    };

    private SoftphoneResponseUtils() {
    }

    public static <T> T parseJsonResponse(String str, Class<T> cls) {
        if (str == null) {
            return null;
        }
        JsonParser jsonParser = new JsonParser();
        try {
            return new Gson().fromJson(jsonParser.parse(str), cls);
        } catch (JsonSyntaxException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "cannot parse result" + e.getMessage());
            return null;
        }
    }

    static GsonXml createGsonXml(boolean z) {
        return new GsonXmlBuilder().setXmlParserCreator(PARSER_CREATOR).setTreatNamespaces(z).setSameNameLists(true).create();
    }

    public static <T> T parseXmlResponse(String str, Class<T> cls, boolean z) {
        if (str == null) {
            return null;
        }
        try {
            return createGsonXml(z).fromXml(str, cls);
        } catch (Exception e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "cannot parse result" + e.getMessage());
            return null;
        }
    }

    public static <T> T parseJsonResponse(HttpResponseParams httpResponseParams, Class<T> cls, int i) {
        T t = null;
        if (httpResponseParams == null) {
            try {
                t = cls.newInstance();
                cls.getField("mSuccess").setBoolean(t, false);
                cls.getField("mReason").set(t, "Null response");
                cls.getField("mStatusCode").setInt(t, 0);
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
                String str = LOG_TAG;
                IMSLog.s(str, "cannot parse result" + e.getMessage());
            }
        } else if (httpResponseParams.getStatusCode() == i) {
            T parseJsonResponse = parseJsonResponse(httpResponseParams.getDataString(), cls);
            String str2 = LOG_TAG;
            IMSLog.i(str2, "parseJsonResponse(): parsed response: " + parseJsonResponse);
            if (parseJsonResponse == null) {
                try {
                    parseJsonResponse = cls.newInstance();
                } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e2) {
                    String str3 = LOG_TAG;
                    IMSLog.s(str3, "cannot parse result" + e2.getMessage());
                    return parseJsonResponse;
                }
            }
            cls.getField("mSuccess").setBoolean(parseJsonResponse, true);
            return parseJsonResponse;
        } else {
            try {
                t = cls.newInstance();
                cls.getField("mSuccess").setBoolean(t, false);
                cls.getField("mReason").set(t, getErrorString(httpResponseParams));
                cls.getField("mStatusCode").setInt(t, httpResponseParams.getStatusCode());
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e3) {
                String str4 = LOG_TAG;
                IMSLog.s(str4, "cannot parse result" + e3.getMessage());
            }
        }
        return t;
    }

    public static <T> T parseXmlResponse(HttpResponseParams httpResponseParams, Class<T> cls, int i, boolean z) {
        T t = null;
        if (httpResponseParams == null) {
            try {
                t = cls.newInstance();
                cls.getField("mSuccess").setBoolean(t, false);
                cls.getField("mReason").set(t, "Null response");
                cls.getField("mStatusCode").setInt(t, 0);
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e) {
                String str = LOG_TAG;
                IMSLog.s(str, "cannot parse result" + e.getMessage());
            }
        } else if (httpResponseParams.getStatusCode() == i) {
            T parseXmlResponse = parseXmlResponse(httpResponseParams.getDataString(), cls, z);
            if (parseXmlResponse == null) {
                try {
                    parseXmlResponse = cls.newInstance();
                } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e2) {
                    String str2 = LOG_TAG;
                    IMSLog.s(str2, "cannot parse result" + e2.getMessage());
                    return parseXmlResponse;
                }
            }
            cls.getField("mSuccess").setBoolean(parseXmlResponse, true);
            return parseXmlResponse;
        } else {
            try {
                t = cls.newInstance();
                cls.getField("mSuccess").setBoolean(t, false);
                cls.getField("mReason").set(t, getErrorString(httpResponseParams));
                cls.getField("mStatusCode").setInt(t, httpResponseParams.getStatusCode());
            } catch (IllegalAccessException | InstantiationException | NoSuchFieldException e3) {
                String str3 = LOG_TAG;
                IMSLog.s(str3, "cannot parse result" + e3.getMessage());
            }
        }
        return t;
    }

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00d2  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ee  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getErrorString(com.sec.internal.helper.httpclient.HttpResponseParams r4) {
        /*
            int r0 = r4.getStatusCode()
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "HTTP Response Code: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r1, r2)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "error"
            r1.append(r2)
            java.lang.String r2 = ":"
            r1.append(r2)
            java.lang.String r3 = java.lang.Integer.toString(r0)
            r1.append(r3)
            r1.append(r2)
            r3 = -1
            if (r0 != r3) goto L_0x0040
            java.lang.String r4 = "Unable to get response"
            r1.append(r4)
            java.lang.String r4 = r1.toString()
            return r4
        L_0x0040:
            r3 = 200(0xc8, float:2.8E-43)
            if (r0 == r3) goto L_0x00ba
            r3 = 408(0x198, float:5.72E-43)
            if (r0 == r3) goto L_0x00b4
            r3 = 411(0x19b, float:5.76E-43)
            if (r0 == r3) goto L_0x00ae
            r3 = 414(0x19e, float:5.8E-43)
            if (r0 == r3) goto L_0x00a8
            r3 = 500(0x1f4, float:7.0E-43)
            if (r0 == r3) goto L_0x00a2
            r3 = 400(0x190, float:5.6E-43)
            if (r0 == r3) goto L_0x00ba
            r3 = 401(0x191, float:5.62E-43)
            if (r0 == r3) goto L_0x008d
            switch(r0) {
                case 403: goto L_0x0087;
                case 404: goto L_0x0081;
                case 405: goto L_0x007b;
                default: goto L_0x005f;
            }
        L_0x005f:
            switch(r0) {
                case 502: goto L_0x0075;
                case 503: goto L_0x006f;
                case 504: goto L_0x0069;
                default: goto L_0x0062;
            }
        L_0x0062:
            java.lang.String r0 = "Unexpected response status."
            r1.append(r0)
            goto L_0x00cf
        L_0x0069:
            java.lang.String r0 = "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI or some other auxiliary server it needed to access in attempting to complete the request."
            r1.append(r0)
            goto L_0x00cf
        L_0x006f:
            java.lang.String r0 = "The server is currently unable to receive requests; please retry."
            r1.append(r0)
            goto L_0x00cf
        L_0x0075:
            java.lang.String r0 = "The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request."
            r1.append(r0)
            goto L_0x00cf
        L_0x007b:
            java.lang.String r0 = "A request was made of a resource using a request method not supported by that resource."
            r1.append(r0)
            goto L_0x00cf
        L_0x0081:
            java.lang.String r0 = "The server has not found anything matching the Request-URI."
            r1.append(r0)
            goto L_0x00cf
        L_0x0087:
            java.lang.String r0 = "Access permission error."
            r1.append(r0)
            goto L_0x00cf
        L_0x008d:
            java.lang.String r0 = r4.getDataString()
            java.lang.Class<com.sec.internal.constants.ims.entitilement.softphone.responses.PolicyExceptionResponse> r3 = com.sec.internal.constants.ims.entitilement.softphone.responses.PolicyExceptionResponse.class
            java.lang.Object r0 = parseJsonResponse(r0, r3)
            com.sec.internal.constants.ims.entitilement.softphone.responses.PolicyExceptionResponse r0 = (com.sec.internal.constants.ims.entitilement.softphone.responses.PolicyExceptionResponse) r0
            if (r0 == 0) goto L_0x00cf
            com.sec.internal.constants.ims.entitilement.softphone.responses.PolicyExceptionResponse$RequestError r0 = r0.mRequestError
            if (r0 == 0) goto L_0x00cf
            com.sec.internal.constants.ims.entitilement.softphone.responses.ExceptionResponse r0 = r0.mException
            goto L_0x00d0
        L_0x00a2:
            java.lang.String r0 = "The server encountered an internal error or timed out; please retry."
            r1.append(r0)
            goto L_0x00cf
        L_0x00a8:
            java.lang.String r0 = "The Request-URI is longer than the server is willing to interpret."
            r1.append(r0)
            goto L_0x00cf
        L_0x00ae:
            java.lang.String r0 = "The Content-Length header was not specified."
            r1.append(r0)
            goto L_0x00cf
        L_0x00b4:
            java.lang.String r0 = "The client did not produce a request within the time that the server was prepared to wait."
            r1.append(r0)
            goto L_0x00cf
        L_0x00ba:
            java.lang.String r0 = r4.getDataString()
            java.lang.Class<com.sec.internal.constants.ims.entitilement.softphone.responses.ServiceExceptionResponse> r3 = com.sec.internal.constants.ims.entitilement.softphone.responses.ServiceExceptionResponse.class
            java.lang.Object r0 = parseJsonResponse(r0, r3)
            com.sec.internal.constants.ims.entitilement.softphone.responses.ServiceExceptionResponse r0 = (com.sec.internal.constants.ims.entitilement.softphone.responses.ServiceExceptionResponse) r0
            if (r0 == 0) goto L_0x00cf
            com.sec.internal.constants.ims.entitilement.softphone.responses.ServiceExceptionResponse$RequestError r0 = r0.mRequestError
            if (r0 == 0) goto L_0x00cf
            com.sec.internal.constants.ims.entitilement.softphone.responses.ExceptionResponse r0 = r0.mException
            goto L_0x00d0
        L_0x00cf:
            r0 = 0
        L_0x00d0:
            if (r0 == 0) goto L_0x00ee
            java.lang.String r4 = r0.mMessageId
            r1.append(r4)
            r1.append(r2)
            java.lang.String r4 = r0.mText
            r1.append(r4)
            java.lang.String r4 = r0.mVariables
            if (r4 == 0) goto L_0x00e6
            r1.append(r4)
        L_0x00e6:
            java.lang.String r4 = r0.mValues
            if (r4 == 0) goto L_0x0103
            r1.append(r4)
            goto L_0x0103
        L_0x00ee:
            java.lang.String r4 = r4.getDataString()
            java.lang.Class<com.sec.internal.constants.ims.entitilement.softphone.responses.GeneralErrorResponse> r0 = com.sec.internal.constants.ims.entitilement.softphone.responses.GeneralErrorResponse.class
            java.lang.Object r4 = parseJsonResponse(r4, r0)
            com.sec.internal.constants.ims.entitilement.softphone.responses.GeneralErrorResponse r4 = (com.sec.internal.constants.ims.entitilement.softphone.responses.GeneralErrorResponse) r4
            if (r4 == 0) goto L_0x0103
            java.lang.String r4 = r4.mError
            if (r4 == 0) goto L_0x0103
            r1.append(r4)
        L_0x0103:
            java.lang.String r4 = r1.toString()
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.entitlement.softphone.SoftphoneResponseUtils.getErrorString(com.sec.internal.helper.httpclient.HttpResponseParams):java.lang.String");
    }
}
