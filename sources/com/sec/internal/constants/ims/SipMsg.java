package com.sec.internal.constants.ims;

import android.telephony.ims.SipMessage;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Pair;
import com.sec.ims.options.Capabilities;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SipMsg {
    static final Set<String> ALLOWED_TAGS = new HashSet<String>() {
        {
            add(SipMsg.FEATURE_TAG_PRESENCE);
            add(SipMsg.FEATURE_TAG_SLM_PAGER);
            add(SipMsg.FEATURE_TAG_SLM_LARGE);
            add(SipMsg.FEATURE_TAG_SLM_DEFERRED);
            add(SipMsg.FEATURE_TAG_SLM_PAGER_LARGE);
            add(SipMsg.FEATURE_TAG_CHAT_CPM);
            add(SipMsg.FEATURE_TAG_CHAT_IM);
            add(SipMsg.FEATURE_TAG_FT_CPM);
            add(SipMsg.FEATURE_TAG_FT_THUMBNAIL);
            add(SipMsg.FEATURE_TAG_FT_HTTP);
            add(SipMsg.FEATURE_TAG_FT_VIA_SMS);
            add(SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER);
            add(SipMsg.FEATURE_TAG_ENRICHED_POST_CALL);
            add(SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP);
            add(SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH);
            add(SipMsg.FEATURE_TAG_GEO_PUSH);
            add(SipMsg.FEATURE_TAG_GEO_PUSH_VIA_SMS);
            add(SipMsg.FEATURE_TAG_CHATBOT_COMMUNICATION_SESSION);
            add(SipMsg.FEATURE_TAG_CHATBOT_COMMUNICATION_STAND_ALONE);
            add(SipMsg.FEATURE_TAG_CHATBOT_VER_1);
            add(SipMsg.FEATURE_TAG_CHATBOT_VER_2);
            add(SipMsg.FEATURE_TAG_CHATBOT_VER_1_2);
            add(SipMsg.FEATURE_TAG_CALL_COMPOSER_VIA_TELEPHONY);
            add(SipMsg.FEATURE_TAG_MMTEL);
            add(SipMsg.FEATURE_TAG_MMTEL_VIDEO);
            add(SipMsg.FEATURE_TAG_SMSIP);
        }
    };
    static final String AUTHENTICATE_NONCE = "nonce";
    static final String CONTACT_EXPIRES = "expires";
    static final String CONTACT_SIP_INSTANCE = "+sip.instance";
    static final String CRLF = "\r\n";
    protected static final String[] DEFAULT_EMPTY_PARAM = {""};
    public static final String DEFAULT_SIP = "SIP/2.0";
    public static final String EVENT_PRESENCE = "presence";
    public static final String EVENT_REG = "reg";
    public static final String FEATURE_TAG_CALL_COMPOSER_VIA_TELEPHONY = Capabilities.FEATURE_TAG_MMTEL_CALL_COMPOSER;
    public static final String FEATURE_TAG_CHATBOT_COMMUNICATION_SESSION = "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot\"";
    public static final String FEATURE_TAG_CHATBOT_COMMUNICATION_STAND_ALONE = "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.chatbot.sa\"";
    public static final String FEATURE_TAG_CHATBOT_VER_1 = "+g.gsma.rcs.botversion=\"#=1\"";
    public static final String FEATURE_TAG_CHATBOT_VER_1_2 = "+g.gsma.rcs.botversion=\"#=1,#=2\"";
    public static final String FEATURE_TAG_CHATBOT_VER_2 = "+g.gsma.rcs.botversion=\"#=2\"";
    public static final String FEATURE_TAG_CHATBOT_VER_PREFIX = "+g.gsma.rcs.botversion=";
    public static final String FEATURE_TAG_CHAT_CPM = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.session\"";
    public static final String FEATURE_TAG_CHAT_IM = Capabilities.FEATURE_TAG_CHAT;
    public static final String FEATURE_TAG_ENRICHED_CALL_COMPOSER = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\"";
    public static final String FEATURE_TAG_ENRICHED_POST_CALL = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callunanswered\"";
    public static final String FEATURE_TAG_ENRICHED_SHARED_MAP = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\"";
    public static final String FEATURE_TAG_ENRICHED_SHARED_SKETCH = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\"";
    public static final String FEATURE_TAG_FT_CPM = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.filetransfer\"";
    public static final String FEATURE_TAG_FT_HTTP = Capabilities.FEATURE_TAG_FT_HTTP;
    public static final String FEATURE_TAG_FT_THUMBNAIL = Capabilities.FEATURE_TAG_FT_THUMBNAIL;
    public static final String FEATURE_TAG_FT_VIA_SMS = "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.ftsms\"";
    public static final String FEATURE_TAG_GEO_PUSH = Capabilities.FEATURE_TAG_GEOLOCATION_PUSH;
    public static final String FEATURE_TAG_GEO_PUSH_VIA_SMS = "+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geosms\"";
    public static final String FEATURE_TAG_MMTEL = Capabilities.FEATURE_TAG_MMTEL;
    public static final String FEATURE_TAG_MMTEL_VIDEO = "video";
    public static final String FEATURE_TAG_PRESENCE = Capabilities.FEATURE_TAG_PRESENCE_DISCOVERY;
    public static final String FEATURE_TAG_SLM_DEFERRED = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.deferred\"";
    public static final String FEATURE_TAG_SLM_LARGE = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.largemsg\"";
    public static final String FEATURE_TAG_SLM_PAGER = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.msg\"";
    public static final String FEATURE_TAG_SLM_PAGER_LARGE = "+g.gsma.rcs.cpm.pager-large";
    public static final String FEATURE_TAG_SMSIP = "+g.3gpp.smsip";
    static final String IARI_REF_TAG = "+g.3gpp.iari-ref";
    static final String ICSI_REF_TAG = "+g.3gpp.icsi-ref";
    private static final String LOG_TAG = "SipMsg";
    static final String REGEXP_COMMA_NOT_IN_DQUOTE = "\\s*,\\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    static final String REGEXP_SEMI_NOT_IN_DQUOTE = "\\s*;\\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
    static final Map<String, String> SERVICES_FROM_TAGS;
    static final Map<String, List<String>> SERVICE_TO_TAG_LIST = new HashMap();
    static final String SUPPORTED_GRUU = "gruu";
    static final String SUPPORTED_SEC_AGREE = "sec-agree";
    static final String VIA_BRANCH = "branch";
    static final String VIA_TRANSPORT = "transport";
    Map<String, List<String>> mHeaderValue;
    boolean mIsOutGoing;
    StartLine mStartLine;
    SipMessage mTelephonySipMsg;

    static {
        AnonymousClass2 r0 = new HashMap<String, String>() {
            {
                put(SipMsg.FEATURE_TAG_PRESENCE, SipMsg.EVENT_PRESENCE);
                put(SipMsg.FEATURE_TAG_SLM_PAGER, "slm");
                put(SipMsg.FEATURE_TAG_SLM_LARGE, "slm");
                put(SipMsg.FEATURE_TAG_SLM_DEFERRED, "slm");
                put(SipMsg.FEATURE_TAG_SLM_PAGER_LARGE, "slm");
                put(SipMsg.FEATURE_TAG_CHAT_CPM, "im");
                put(SipMsg.FEATURE_TAG_CHAT_IM, "im");
                put(SipMsg.FEATURE_TAG_FT_CPM, "ft");
                put(SipMsg.FEATURE_TAG_FT_THUMBNAIL, "ft");
                put(SipMsg.FEATURE_TAG_FT_HTTP, "ft_http");
                put(SipMsg.FEATURE_TAG_FT_VIA_SMS, "ft_http");
                put(SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER, "ec");
                put(SipMsg.FEATURE_TAG_ENRICHED_POST_CALL, "ec");
                put(SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP, "ec");
                put(SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH, "ec");
                put(SipMsg.FEATURE_TAG_GEO_PUSH, "gls");
                put(SipMsg.FEATURE_TAG_GEO_PUSH_VIA_SMS, "gls");
                put(SipMsg.FEATURE_TAG_CHATBOT_COMMUNICATION_SESSION, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
                put(SipMsg.FEATURE_TAG_CHATBOT_COMMUNICATION_STAND_ALONE, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
                put(SipMsg.FEATURE_TAG_CHATBOT_VER_1, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
                put(SipMsg.FEATURE_TAG_CHATBOT_VER_2, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
                put(SipMsg.FEATURE_TAG_CHATBOT_VER_1_2, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
                put(SipMsg.FEATURE_TAG_CALL_COMPOSER_VIA_TELEPHONY, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
            }
        };
        SERVICES_FROM_TAGS = r0;
        r0.forEach(new SipMsg$$ExternalSyntheticLambda35());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ List lambda$static$0(String str) {
        return new ArrayList();
    }

    public enum HeaderName {
        UNKNOWN(""),
        ACCEPT_CONTACT("Accept-Contact", "a"),
        ALLOW("Allow"),
        AUTHENTICATION_INFO("Authentication-Info"),
        AUTHORIZATION("Authorization"),
        CALL_ID("Call-ID", "i"),
        CALL_INFO("Call-Info"),
        CONTACT("Contact", "m"),
        CONTENT_LENGTH("Content-Length", "l"),
        CONTENT_TYPE("Content-Type", "c"),
        CSEQ("CSeq"),
        EVENT("Event"),
        EXPIRES(HttpController.HEADER_EXPIRES),
        FROM(AttributeNames.from, "f"),
        IN_REPLY_TO("In-Reply-To"),
        MIN_EXPIRES("Min-Expires"),
        P_ACCESS_NETWORK_INFO("P-Access-Network-Info"),
        P_ASSOCIATED_URI("P-Associated-URI"),
        P_LAST_ACCESS_NETWORK_INFO("P-Last-Access-Network-Info"),
        PROXY_AUTHENTICATE("Proxy-Authenticate"),
        PROXY_AUTHORIZATION(HttpController.HEADER_PROXY_AUTHORIZATION),
        PROXY_REQUIRE("Proxy-Require"),
        RECORD_ROUTE("Record-Route"),
        REQUIRE("Require"),
        RETRY_AFTER(HttpRequest.HEADER_RETRY_AFTER),
        ROUTE("Route"),
        SECURITY_VERIFY("Security-Verify"),
        SERVICE_ROUTE("Service-Route"),
        SUPPORTED("Supported", "k"),
        TO(AttributeNames.to, "t"),
        USER_AGENT("User-Agent"),
        VIA("Via", CloudMessageProviderContract.BufferDBMMSpdu.V),
        WWW_AUTHENTICATE("WWW-Authenticate");
        
        String mCompact;
        String mStr;

        private HeaderName(String str) {
            this.mStr = str.toUpperCase(Locale.US);
        }

        private HeaderName(String str, String str2) {
            this(r1, r2, str);
            this.mCompact = str2;
        }

        /* access modifiers changed from: package-private */
        public String get() {
            return this.mStr;
        }

        /* access modifiers changed from: package-private */
        public boolean isOneOf(HeaderName... headerNameArr) {
            return Arrays.stream(headerNameArr).anyMatch(new SipMsg$HeaderName$$ExternalSyntheticLambda0(this));
        }
    }

    static class Header {
        HeaderName mHeaderName;
        String[] mValuesStr;

        Header() {
            this.mHeaderName = HeaderName.UNKNOWN;
        }

        Header(HeaderName headerName, String str) {
            this.mHeaderName = headerName;
            this.mValuesStr = str.split(SipMsg.REGEXP_COMMA_NOT_IN_DQUOTE);
        }

        /* access modifiers changed from: package-private */
        public String[] getValues() {
            return this.mValuesStr;
        }
    }

    public static abstract class StartLine {
        String sipVer;

        public String getSipVer() {
            return this.sipVer;
        }

        public RequestLine asRequestLine() {
            return this instanceof RequestLine ? (RequestLine) this : new RequestLine(new String[]{"", "", ""});
        }

        public StatusLine asStatusLine() {
            return this instanceof StatusLine ? (StatusLine) this : new StatusLine(new String[]{"", "0", ""});
        }
    }

    public static class RequestLine extends StartLine {
        String method;
        String uri;

        protected RequestLine(String[] strArr) {
            this.method = strArr[0].toUpperCase(Locale.US);
            this.uri = strArr[1];
            this.sipVer = strArr[2];
        }

        public String getMethod() {
            return this.method;
        }

        public String getUri() {
            return this.uri;
        }

        public String toString() {
            return "Request: " + this.method;
        }
    }

    public static class StatusLine extends StartLine {
        int code;
        String reason;

        protected StatusLine(String[] strArr) {
            this.sipVer = strArr[0];
            try {
                this.code = Integer.parseInt(strArr[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            this.reason = strArr[2];
        }

        public int getCode() {
            return this.code;
        }

        public String getReason() {
            return this.reason;
        }

        public String toString() {
            return "Status: " + this.code + " " + this.reason;
        }
    }

    public static SipMsg from(String str, boolean z, byte[] bArr) {
        return new SipMsg(str, z, bArr);
    }

    public SipMsg(String str, boolean z, byte[] bArr) {
        this.mIsOutGoing = z;
        List asList = Arrays.asList(str.split(CRLF));
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        StringBuilder sb = null;
        for (String str2 : asList.subList(1, asList.size())) {
            if (!str2.startsWith(" ") && str2.indexOf(":") > 0) {
                String[] split = str2.split(":", 2);
                StringBuilder sb2 = new StringBuilder(split[1].trim());
                String upperCase = split[0].trim().toUpperCase(Locale.US);
                if (!linkedHashMap.containsKey(upperCase)) {
                    linkedHashMap.put(upperCase, new ArrayList(Collections.singletonList(sb2)));
                } else {
                    Optional.ofNullable((List) linkedHashMap.get(upperCase)).ifPresent(new SipMsg$$ExternalSyntheticLambda22(sb2));
                }
                sb = sb2;
            } else if (str2.startsWith(" ") && sb != null) {
                sb.append(str2.trim());
            }
        }
        String trim = ((String) asList.get(0)).trim();
        this.mStartLine = parseStartLine(trim);
        this.mHeaderValue = (Map) linkedHashMap.entrySet().stream().collect(Collectors.toMap(new SipMsg$$ExternalSyntheticLambda23(), new SipMsg$$ExternalSyntheticLambda24()));
        String str3 = str.split("\r\n\r\n", 2)[0];
        try {
            this.mTelephonySipMsg = new SipMessage(trim + CRLF, str3.substring(str3.indexOf("\n") + 1) + CRLF, bArr);
        } catch (IllegalArgumentException e) {
            IMSLog.e("SipMsg", "Failed to parse SipMessage! " + e);
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ List lambda$new$3(Map.Entry entry) {
        return (List) ((List) entry.getValue()).stream().map(new SipMsg$$ExternalSyntheticLambda21()).collect(Collectors.toList());
    }

    private StartLine parseStartLine(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String[] split = str.split(" ", 3);
        if (split.length != 3) {
            return null;
        }
        if (split[0].equals(DEFAULT_SIP)) {
            return new StatusLine(split);
        }
        if (split[2].equals(DEFAULT_SIP)) {
            return new RequestLine(split);
        }
        return null;
    }

    public static String getServicefromTag(String str) {
        Map<String, String> map = SERVICES_FROM_TAGS;
        if (map.containsKey(str)) {
            return map.get(str);
        }
        return null;
    }

    public static Set<String> getTagsForServices(Set<String> set) {
        ArraySet arraySet = new ArraySet();
        set.forEach(new SipMsg$$ExternalSyntheticLambda30(arraySet));
        return arraySet;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$getTagsForServices$4(Set set, String str) {
        Optional ofNullable = Optional.ofNullable(SERVICE_TO_TAG_LIST.get(str));
        Objects.requireNonNull(set);
        ofNullable.ifPresent(new SipMsg$$ExternalSyntheticLambda8(set));
    }

    public boolean isOutGoing() {
        return this.mIsOutGoing;
    }

    public boolean isWellFormed() {
        return (this.mStartLine == null || this.mTelephonySipMsg == null) ? false : true;
    }

    public StartLine getStartLine() {
        return this.mStartLine;
    }

    public String getRequestLineUri() {
        return this.mStartLine.asRequestLine().getUri();
    }

    public String getCSeqMethod() {
        return ((String[]) Optional.ofNullable(getHeader(HeaderName.CSEQ)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM))[0].trim().split(" ")[1];
    }

    public SipMessage getTelephonySipMessage() {
        return this.mTelephonySipMsg;
    }

    public boolean isRegister() {
        return "REGISTER".equalsIgnoreCase(getCSeqMethod());
    }

    public boolean isNonDialogMethod() {
        return "MESSAGE".equalsIgnoreCase(getCSeqMethod());
    }

    /* access modifiers changed from: package-private */
    public Header getHeader(HeaderName headerName) {
        return (Header) Optional.ofNullable(this.mHeaderValue.get(headerName.get())).map(new SipMsg$$ExternalSyntheticLambda11(headerName)).orElse(new Header());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Header lambda$getHeader$5(HeaderName headerName, List list) {
        return new Header(headerName, String.join(",", list));
    }

    private String[] getParameters(HeaderName headerName) {
        String[] values = getHeader(headerName).getValues();
        if (!CollectionUtils.isNullOrEmpty((Object[]) values)) {
            return values[0].split(REGEXP_SEMI_NOT_IN_DQUOTE);
        }
        IMSLog.e("SipMsg", "getParam: Wrong header [" + headerName + "]");
        return new String[0];
    }

    private String getParam(HeaderName headerName, String str) {
        if (!CollectionUtils.isNullOrEmpty((Object[]) getHeader(headerName).getValues())) {
            return (String) Arrays.stream(getParameters(headerName)).filter(new SipMsg$$ExternalSyntheticLambda25(str)).findFirst().map(new SipMsg$$ExternalSyntheticLambda26()).map(new SipMsg$$ExternalSyntheticLambda2()).orElse("");
        }
        IMSLog.e("SipMsg", "getParam: Wrong header [" + headerName + "]");
        return "";
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$getParam$7(String str) {
        return str.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR)[1];
    }

    public boolean isFeatureTagMatched(String str) {
        Set<String> set = ALLOWED_TAGS;
        return getFeatureTags(set).contains(str) || getFeatureTagsFromAcceptContact(set).contains(str);
    }

    public Set<String> getFeatureTags() {
        return getFeatureTags(ALLOWED_TAGS);
    }

    public Set<String> getFeatureTags(Set<String> set) {
        String[] values = getHeader(HeaderName.CONTACT).getValues();
        if (!CollectionUtils.isNullOrEmpty((Object[]) values)) {
            return getFeatureTags(new String[]{getTopPriorityContact(values)}, set);
        }
        IMSLog.e("SipMsg", "getFeatureTags: Wrong Contact header");
        return Collections.emptySet();
    }

    public Set<String> getFeatureTagsFromAcceptContact() {
        return getFeatureTagsFromAcceptContact(ALLOWED_TAGS);
    }

    public Set<String> getFeatureTagsFromAcceptContact(Set<String> set) {
        String[] acceptContacts = getAcceptContacts();
        if (!CollectionUtils.isNullOrEmpty((Object[]) acceptContacts)) {
            return getFeatureTags(acceptContacts, set);
        }
        IMSLog.e("SipMsg", "getFeatureTagsFromAcceptContact: Wrong Accept-Contact header");
        return Collections.emptySet();
    }

    private Set<String> getFeatureTags(String[] strArr, Set<String> set) {
        return (Set) Arrays.stream(strArr).flatMap(new SipMsg$$ExternalSyntheticLambda1()).map(new SipMsg$$ExternalSyntheticLambda2()).collect(Collector.of(new SipMsg$$ExternalSyntheticLambda3(), new SipMsg$$ExternalSyntheticLambda4(set), new SipMsg$$ExternalSyntheticLambda5(), new Collector.Characteristics[0]));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ void lambda$getFeatureTags$10(Set set, ArraySet arraySet, String str) {
        if (set.contains(str)) {
            arraySet.add(str);
        } else if (str.startsWith("+g.3gpp.iari-ref=") || str.startsWith("+g.3gpp.icsi-ref=")) {
            String[] split = str.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            arraySet.addAll((Collection) Arrays.stream(split[1].replace(CmcConstants.E_NUM_STR_QUOTE, "").split(REGEXP_COMMA_NOT_IN_DQUOTE)).map(new SipMsg$$ExternalSyntheticLambda27(split[0])).filter(new SipMsg$$ExternalSyntheticLambda28(set)).collect(Collectors.toSet()));
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$getFeatureTags$9(String str, String str2) {
        return str + "=\"" + str2 + CmcConstants.E_NUM_STR_QUOTE;
    }

    private String getTopPriorityContact(String[] strArr) {
        double d;
        String str = strArr[0];
        if (strArr.length == 1) {
            return str;
        }
        double d2 = -1.0d;
        for (String str2 : strArr) {
            String[] split = str2.split(REGEXP_SEMI_NOT_IN_DQUOTE);
            int length = split.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String trim = split[i].trim();
                if (trim.startsWith("q=")) {
                    try {
                        d = Double.parseDouble(trim.replace("q=", ""));
                    } catch (NumberFormatException unused) {
                        IMSLog.e("SipMsg", "Wrong q parameter [" + trim + "]");
                        d = -1.0d;
                    }
                    if (d > d2) {
                        str = str2;
                        d2 = d;
                    }
                } else {
                    i++;
                }
            }
        }
        return str;
    }

    public String getContactUser() {
        String[] values = getHeader(HeaderName.CONTACT).getValues();
        if (!CollectionUtils.isNullOrEmpty((Object[]) values)) {
            return (String) Arrays.stream(getTopPriorityContact(values).split(REGEXP_SEMI_NOT_IN_DQUOTE)).filter(new SipMsg$$ExternalSyntheticLambda15()).map(new SipMsg$$ExternalSyntheticLambda16()).findFirst().orElse("");
        }
        IMSLog.e("SipMsg", "getContactUser: Wrong Contact header");
        return "";
    }

    public String getContactImei() {
        return getParam(HeaderName.CONTACT, CONTACT_SIP_INSTANCE).replace("\"<urn:gsma:imei:", "").replace(">\"", "");
    }

    public boolean isContactUriHasSos() {
        String[] values = getHeader(HeaderName.CONTACT).getValues();
        if (!CollectionUtils.isNullOrEmpty((Object[]) values)) {
            return getTopPriorityContact(values).contains(";sos>;");
        }
        IMSLog.e("SipMsg", "isContactUriHasSos: Wrong Contact header");
        return false;
    }

    public Pair<String, Integer> getViaHostPort() {
        Pair pair = new Pair("", (Object) null);
        return (Pair) Arrays.stream(getParameters(HeaderName.VIA)).filter(new SipMsg$$ExternalSyntheticLambda9()).findFirst().map(new SipMsg$$ExternalSyntheticLambda10(pair)).orElse(pair);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Pair lambda$getViaHostPort$15(Pair pair, String str) {
        String str2 = str.split(" ")[1];
        if (str2.contains("[")) {
            str2 = str2.replace("[", "").replace("]", "");
        }
        int lastIndexOf = str2.lastIndexOf(":");
        try {
            return new Pair(str2.substring(0, lastIndexOf), Integer.valueOf(str2.substring(lastIndexOf + 1)));
        } catch (NumberFormatException unused) {
            return pair;
        }
    }

    public String getViaBranch() {
        return getParam(HeaderName.VIA, VIA_BRANCH);
    }

    public String getViaTransport() {
        return getParam(HeaderName.VIA, VIA_TRANSPORT);
    }

    public Boolean isSupported(String str) {
        return (Boolean) Optional.ofNullable(getHeader(HeaderName.SUPPORTED)).map(new SipMsg$$ExternalSyntheticLambda6()).map(new SipMsg$$ExternalSyntheticLambda14(str)).orElse((Object) null);
    }

    public String[] getCallIds() {
        return (String[]) Optional.ofNullable(getHeader(HeaderName.CALL_ID)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM);
    }

    public String getEvent() {
        return ((String[]) Optional.ofNullable(getHeader(HeaderName.EVENT)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM))[0];
    }

    public boolean isOneOfEvent(String... strArr) {
        return Arrays.stream(strArr).anyMatch(new SipMsg$$ExternalSyntheticLambda7(getEvent()));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mIsOutGoing ? "--> " : "<-- ");
        sb.append((String) Optional.ofNullable(this.mStartLine).map(new SipMsg$$ExternalSyntheticLambda0()).orElse("WRONG SIP"));
        return sb.toString();
    }

    public int getExpire() {
        String[] values = getHeader(HeaderName.CONTACT).getValues();
        if (CollectionUtils.isNullOrEmpty((Object[]) values)) {
            IMSLog.e("SipMsg", "getExpire: Wrong Contact header");
            return -1;
        }
        try {
            return Integer.parseInt((String) Arrays.stream(getTopPriorityContact(values).split(REGEXP_SEMI_NOT_IN_DQUOTE)).map(new SipMsg$$ExternalSyntheticLambda31()).filter(new SipMsg$$ExternalSyntheticLambda32()).map(new SipMsg$$ExternalSyntheticLambda33()).findFirst().orElseGet(new SipMsg$$ExternalSyntheticLambda34(this)));
        } catch (NumberFormatException e) {
            IMSLog.e("SipMsg", "getExpire: Wrong expires value! " + e);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ String lambda$getExpire$21() {
        return ((String[]) Optional.ofNullable(getHeader(HeaderName.EXPIRES)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM))[0];
    }

    public String[] getServiceRoutes() {
        return (String[]) Optional.ofNullable(getHeader(HeaderName.SERVICE_ROUTE)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM);
    }

    public String[] getRoutes() {
        return (String[]) Optional.ofNullable(getHeader(HeaderName.ROUTE)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM);
    }

    public String getUserAgent() {
        return ((String[]) Optional.ofNullable(getHeader(HeaderName.USER_AGENT)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM))[0];
    }

    public String[] getSecurityVerify() {
        return (String[]) Optional.ofNullable(getHeader(HeaderName.SECURITY_VERIFY)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM);
    }

    public String getPAccessNetworkInfo() {
        return ((String[]) Optional.ofNullable(getHeader(HeaderName.P_ACCESS_NETWORK_INFO)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM))[0];
    }

    public String getPLastAccessNetworkInfo() {
        return ((String[]) Optional.ofNullable(getHeader(HeaderName.P_LAST_ACCESS_NETWORK_INFO)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM))[0];
    }

    public String[] getPAssociatedUris() {
        return (String[]) Optional.ofNullable(getHeader(HeaderName.P_ASSOCIATED_URI)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM);
    }

    public String getAuthenticate() {
        String str;
        int intValue = ((Integer) Optional.ofNullable(this.mStartLine).map(new SipMsg$$ExternalSyntheticLambda17()).map(new SipMsg$$ExternalSyntheticLambda18()).orElse(0)).intValue();
        if (intValue != 401 && intValue != 407) {
            return "";
        }
        if (intValue == 401) {
            str = HeaderName.WWW_AUTHENTICATE.get();
        } else {
            str = HeaderName.PROXY_AUTHENTICATE.get();
        }
        return (String) ((List) Optional.ofNullable(this.mHeaderValue.get(str)).orElseGet(new SipMsg$$ExternalSyntheticLambda19(str))).stream().findFirst().orElse("");
    }

    public String getAuthenticateNonce() {
        return (String) Arrays.stream(getAuthenticate().split(REGEXP_COMMA_NOT_IN_DQUOTE)).map(new SipMsg$$ExternalSyntheticLambda2()).filter(new SipMsg$$ExternalSyntheticLambda12()).findFirst().map(new SipMsg$$ExternalSyntheticLambda13()).orElse("");
    }

    public String[] getAcceptContacts() {
        return (String[]) Optional.ofNullable(getHeader(HeaderName.ACCEPT_CONTACT)).map(new SipMsg$$ExternalSyntheticLambda6()).orElse(DEFAULT_EMPTY_PARAM);
    }
}
