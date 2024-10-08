package com.sec.internal.ims.config.workflow;

import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.ims.config.exception.EmptyBodyAndCookieException;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WorkflowParamHandler {
    protected static final String CHARSET = "utf-8";
    protected static final int CLIENT_VENDOR = 2;
    protected static final int CLIENT_VERSION = 3;
    protected static final String GC_ACS_URL = "http://rcs-acs-mccXXX.jibe.google.com";
    protected static final String LOG_TAG = "WorkflowParamHandler";
    protected static final int MAX_RETRY = 15;
    protected static final int RCS_ENABLED_BY_USER = 4;
    protected static final int RCS_PROFILE = 1;
    protected static final int RCS_VERSION = 0;
    protected String mClientVendor;
    protected String mClientVersion;
    protected int mPhoneId;
    protected String mRcsEnabledByUser;
    protected String mRcsProfile;
    protected String mRcsVersion;
    protected ITelephonyAdapter mTelephony;
    protected WorkflowBase mWorkflowBase;

    enum UserAccept {
        ACCEPT,
        REJECT,
        NON_DEFAULT_MSG_APP
    }

    public WorkflowParamHandler(WorkflowBase workflowBase, int i, ITelephonyAdapter iTelephonyAdapter) {
        this.mWorkflowBase = workflowBase;
        this.mPhoneId = i;
        this.mTelephony = iTelephonyAdapter;
    }

    /* access modifiers changed from: protected */
    public String initUrl() throws NoInitialDataException {
        HashMap hashMap = new HashMap();
        getMccMncInfo(hashMap);
        return buildUrl(hashMap);
    }

    /* access modifiers changed from: protected */
    public String initUrl(String str) throws NoInitialDataException {
        if (CollectionUtils.isNullOrEmpty(str)) {
            return initUrl();
        }
        return "http://" + str;
    }

    /* access modifiers changed from: protected */
    public void getMccMncInfo(Map<String, String> map) throws NoInitialDataException {
        String imsi;
        map.put(ConfigConstants.URL.MCC_PNAME, this.mTelephony.getMcc());
        map.put(ConfigConstants.URL.MNC_PNAME, this.mTelephony.getMnc());
        if (TextUtils.isEmpty(map.get(ConfigConstants.URL.MCC_PNAME)) || TextUtils.isEmpty(map.get(ConfigConstants.URL.MNC_PNAME))) {
            throw new NoInitialDataException("MCC or MNC is not prepared");
        } else if (this.mWorkflowBase.mMno == Mno.SPRINT && (imsi = this.mTelephony.getImsi()) != null && imsi.length() >= 6) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "mcc, mnc from imsi");
            map.put(ConfigConstants.URL.MCC_PNAME, imsi.substring(0, 3));
            map.put(ConfigConstants.URL.MNC_PNAME, imsi.substring(3, 6));
        }
    }

    /* access modifiers changed from: protected */
    public String buildUrl(Map<String, String> map) throws NoInitialDataException {
        String mcc = this.mTelephony.getMcc();
        String mnc = this.mTelephony.getMnc();
        if (mcc == null || mnc == null) {
            throw new NoInitialDataException("MCC or MNC is not prepared");
        }
        String acsCustomServerUrl = ConfigUtil.getAcsCustomServerUrl(this.mPhoneId);
        if (isConfigProxy()) {
            return ConfigConstants.URL.INTERNAL_CONFIG_PROXY_TEMPLATE.replace(ConfigConstants.URL.MCC_PVALUE, map.get(ConfigConstants.URL.MCC_PNAME)).replace(ConfigConstants.URL.MNC_PVALUE, map.get(ConfigConstants.URL.MNC_PNAME));
        }
        if (TextUtils.isEmpty(acsCustomServerUrl)) {
            String replace = ConfigConstants.URL.CONFIG_TEMPLATE.replace(ConfigConstants.URL.MCC_PVALUE, map.get(ConfigConstants.URL.MCC_PNAME)).replace(ConfigConstants.URL.MNC_PVALUE, map.get(ConfigConstants.URL.MNC_PNAME));
            checkUrlConnection(replace);
            return replace;
        } else if (acsCustomServerUrl.equals(GC_ACS_URL)) {
            return GC_ACS_URL.replace("XXX", mcc);
        } else {
            return acsCustomServerUrl;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x003b, code lost:
        r1 = r1.mNetwork;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void checkUrlConnection(java.lang.String r7) throws com.sec.internal.ims.config.exception.NoInitialDataException {
        /*
            r6 = this;
            com.sec.internal.ims.config.workflow.WorkflowBase r0 = r6.mWorkflowBase
            com.sec.internal.constants.Mno r0 = r0.mMno
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.ATT
            if (r0 != r1) goto L_0x0013
            java.lang.String r7 = LOG_TAG
            int r6 = r6.mPhoneId
            java.lang.String r0 = "skip to checkUrlConnection"
            com.sec.internal.log.IMSLog.i(r7, r6, r0)
            return
        L_0x0013:
            java.lang.String r0 = LOG_TAG
            int r1 = r6.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "checkUrlConnection: url: "
            r2.append(r3)
            java.lang.String r3 = "https?://"
            java.lang.String r4 = ""
            java.lang.String r5 = r7.replaceFirst(r3, r4)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            com.sec.internal.ims.config.workflow.WorkflowBase r1 = r6.mWorkflowBase     // Catch:{ UnknownHostException -> 0x006b }
            com.sec.internal.constants.Mno r2 = r1.mMno     // Catch:{ UnknownHostException -> 0x006b }
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.VZW     // Catch:{ UnknownHostException -> 0x006b }
            if (r2 != r5) goto L_0x0048
            android.net.Network r1 = r1.mNetwork     // Catch:{ UnknownHostException -> 0x006b }
            if (r1 == 0) goto L_0x0048
            java.lang.String r7 = r7.replaceFirst(r3, r4)     // Catch:{ UnknownHostException -> 0x006b }
            java.net.InetAddress r7 = r1.getByName(r7)     // Catch:{ UnknownHostException -> 0x006b }
            goto L_0x0050
        L_0x0048:
            java.lang.String r7 = r7.replaceFirst(r3, r4)     // Catch:{ UnknownHostException -> 0x006b }
            java.net.InetAddress r7 = java.net.InetAddress.getByName(r7)     // Catch:{ UnknownHostException -> 0x006b }
        L_0x0050:
            int r6 = r6.mPhoneId     // Catch:{ UnknownHostException -> 0x006b }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ UnknownHostException -> 0x006b }
            r1.<init>()     // Catch:{ UnknownHostException -> 0x006b }
            java.lang.String r2 = "addr: "
            r1.append(r2)     // Catch:{ UnknownHostException -> 0x006b }
            java.lang.String r7 = r7.toString()     // Catch:{ UnknownHostException -> 0x006b }
            r1.append(r7)     // Catch:{ UnknownHostException -> 0x006b }
            java.lang.String r7 = r1.toString()     // Catch:{ UnknownHostException -> 0x006b }
            com.sec.internal.log.IMSLog.i(r0, r6, r7)     // Catch:{ UnknownHostException -> 0x006b }
            return
        L_0x006b:
            r6 = move-exception
            r6.printStackTrace()
            com.sec.internal.ims.config.exception.NoInitialDataException r6 = new com.sec.internal.ims.config.exception.NoInitialDataException
            java.lang.String r7 = "connection is not prepared"
            r6.<init>(r7)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowParamHandler.checkUrlConnection(java.lang.String):void");
    }

    /* access modifiers changed from: protected */
    public boolean isConfigProxy() {
        int i = 0;
        if (ConfigUtil.getAutoconfigSourceWithFeature(this.mPhoneId, 0) != 1) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "config proxy is disabled.");
            return false;
        }
        while (i < 15) {
            try {
                HttpAdapter httpAdapter = new HttpAdapter(this.mPhoneId);
                httpAdapter.open(ConfigConstants.URL.INTERNAL_CONFIG_PROXY_AUTHORITY);
                IHttpAdapter.Response request = httpAdapter.request();
                httpAdapter.close();
                if (request != null && request.getStatusCode() == 200 && request.getBody() != null && new String(request.getBody(), CHARSET).compareToIgnoreCase(ConfigConstants.KEY.INTERNAL_CONFIG_PROXY_AUTHORITY) == 0) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mWorkflowBase.sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
            i++;
        }
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "config proxy is enabled and got exception (retry: " + i + ")");
        return true;
    }

    /* access modifiers changed from: protected */
    public String getModelInfoFromBuildVersion(String str, String str2, int i, boolean z) {
        String str3;
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split(CmcConstants.E_NUM_SLOT_SPLIT);
            if (split.length == 2 && (str3 = split[1]) != null && !str3.isEmpty()) {
                String str4 = split[1];
                if (str2.startsWith(str4) && str2.length() > str4.length()) {
                    str2 = str2.substring(str4.length());
                }
            }
        }
        if (str2.length() <= i) {
            return str2;
        }
        if (!z) {
            return str2.substring(0, i - 1);
        }
        int length = str2.length();
        return str2.substring(length - i, length);
    }

    /* access modifiers changed from: protected */
    public boolean isSupportCarrierVersion() {
        return SimUtil.isSupportCarrierVersion(this.mPhoneId);
    }

    /* access modifiers changed from: protected */
    public String getModelInfoFromCarrierVersion(String str, String str2, int i, boolean z) {
        String modelInfoFromBuildVersion = getModelInfoFromBuildVersion(str, str2, i, z);
        String rcsConfigMark = this.mWorkflowBase.mModule.getRcsConfigMark(this.mPhoneId);
        if (!TextUtils.isEmpty(rcsConfigMark)) {
            if (isSupportCarrierVersion()) {
                modelInfoFromBuildVersion = modelInfoFromBuildVersion + rcsConfigMark;
            } else {
                modelInfoFromBuildVersion = modelInfoFromBuildVersion + "om";
            }
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "terminal version [" + modelInfoFromBuildVersion + "] : adds [" + rcsConfigMark + "] to terminal version");
        return modelInfoFromBuildVersion;
    }

    /* access modifiers changed from: protected */
    public String encodeRFC3986(String str) {
        try {
            return URLEncoder.encode(str, CHARSET).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, e.toString());
            e.printStackTrace();
            return str;
        }
    }

    /* access modifiers changed from: protected */
    public String encodeRFC7254(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        String substring = str.length() > 14 ? str.substring(14) : "0";
        return "urn%3agsma%3aimei%3a" + String.format("%s-%s-%s", new Object[]{str.substring(0, 8), str.substring(8, 14), substring});
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getParsedXmlFromBody() {
        byte[] body = this.mWorkflowBase.mSharedInfo.getHttpResponse().getBody();
        if (body == null) {
            body = new String("").getBytes();
        }
        try {
            return this.mWorkflowBase.mXmlParser.parse(new String(body, CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Throwable unused) {
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isRequiredAuthentication(Map<String, String> map) throws Exception {
        if (map == null) {
            throw new InvalidXmlException("no parsedXml data");
        } else if (map.get("root/vers/version") != null && map.get("root/vers/validity") != null) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "isRequiredAuthentication: parsedXml need to contain version or validity item");
            WorkflowBase workflowBase = this.mWorkflowBase;
            if (workflowBase.mCookieHandler.isCookie(workflowBase.mSharedInfo.getHttpResponse())) {
                return true;
            }
            throw new EmptyBodyAndCookieException("no body and no cookie, something wrong");
        }
    }

    /* access modifiers changed from: protected */
    public void parseParam(Map<String, String> map) {
        String encryptParam;
        String str = ConfigConstants.PATH.USERPWD;
        String str2 = map.get(str);
        if (TextUtils.isEmpty(str2)) {
            str = ConfigConstants.PATH.USERPWD_UP20;
            str2 = map.get(str);
        }
        if (!(str2 == null || str2.isEmpty() || (encryptParam = ConfigUtil.encryptParam(str2)) == null)) {
            map.put(str, encryptParam);
            String str3 = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.s(str3, i, "encrypt data: " + encryptParam);
        }
        if (map.get(ConfigConstants.PATH.IM_MAX_SIZE) == null) {
            String str4 = map.get(ConfigConstants.PATH.IM_MAX_SIZE_1_TO_1);
            if (str4 != null) {
                String str5 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str5, i2, "maxsize is empty, use it as maxsize1to1 value: " + str4);
                map.put(ConfigConstants.PATH.IM_MAX_SIZE, str4);
            } else {
                map.put(ConfigConstants.PATH.IM_MAX_SIZE, "");
                map.put(ConfigConstants.PATH.IM_MAX_SIZE_1_TO_1, "");
            }
        }
        checkSetToGS(map);
    }

    /* access modifiers changed from: protected */
    public void parseParamForAtt(Map<String, String> map) {
        Boolean valueOf = Boolean.valueOf(RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId) && ConfigUtil.isGoogDmaPackageInuse(this.mWorkflowBase.mContext, this.mPhoneId) && ImsProfile.isRcsUp2Profile(this.mWorkflowBase.mRcsProfile));
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "isRcsUp2ProfileForGoogle : " + valueOf);
        String str2 = valueOf.booleanValue() ? ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH : ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH;
        Locale locale = Locale.US;
        String str3 = map.get((str2 + ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI).toLowerCase(locale));
        if (valueOf.booleanValue() && TextUtils.isEmpty(str3)) {
            IMSLog.i(str, this.mPhoneId, "ftHTTPCSURI is null. Try to read with UP 1.0 path");
            str3 = map.get((ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH + ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI).toLowerCase(locale));
            str2 = ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH;
        }
        if (str3 != null && !str3.toLowerCase(locale).startsWith(OMAGlobalVariables.HTTP)) {
            IMSLog.i(str, this.mPhoneId, "handleFtHttpCsUriValue: FT_HTTP_CS_URI has invalid URL");
            map.put((str2 + ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI).toLowerCase(locale), "");
        }
        ConfigUtil.encryptParams(map, str2 + ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER, str2 + ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD, str2 + ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI, "root/application/1/serviceproviderext/nms_url", "root/application/1/serviceproviderext/nc_url", "root/token/token");
        try {
            map.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, new String(this.mWorkflowBase.mSharedInfo.getHttpResponse().getBody(), CHARSET));
        } catch (UnsupportedEncodingException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Failed to put xml!");
            e.printStackTrace();
        }
        Locale locale2 = Locale.US;
        String str4 = map.get("root/application/1/im/ext/att/slmMaxRecipients".toLowerCase(locale2));
        String str5 = LOG_TAG;
        IMSLog.i(str5, this.mPhoneId, "slmMaxRecipients: " + str4);
        StringBuilder sb = new StringBuilder();
        sb.append(valueOf.booleanValue() ? ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH : ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH);
        sb.append("max_adhoc_group_size");
        String sb2 = sb.toString();
        if (TextUtils.isEmpty(str4)) {
            str4 = map.get(sb2.toLowerCase(locale2));
            if (TextUtils.isEmpty(str4) && valueOf.booleanValue()) {
                str4 = map.get(ConfigConstants.ConfigPath.IM_CHARACTERISTIC_PATH.toLowerCase(locale2));
            }
            IMSLog.i(str5, this.mPhoneId, "max_adhoc_group_size: " + str4);
        }
        if (!TextUtils.isEmpty(str4)) {
            map.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(locale2), str4);
        }
    }

    /* access modifiers changed from: protected */
    public void parseParamForLocalFile(Map<String, String> map) {
        map.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, this.mWorkflowBase.mSharedInfo.getXml());
        Locale locale = Locale.US;
        String str = map.get("root/application/1/im/ext/att/slmMaxRecipients".toLowerCase(locale));
        if (!TextUtils.isEmpty(str)) {
            map.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(locale), str);
            String str2 = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str2, i, "Using slmMaxRecipients: " + str);
            return;
        }
        String str3 = map.get("root/application/1/im/max_adhoc_group_size".toLowerCase(locale));
        if (!TextUtils.isEmpty(str3)) {
            map.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(locale), str3);
            String str4 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str4, i2, "slmMaxRecipients is null. Using max_adhoc_group_size instead: " + str3);
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "slmMaxRecipients and max_adhoc_group_size is null");
    }

    /* access modifiers changed from: protected */
    public void moveHttpParam(Map<String, String> map) {
        if (this.mWorkflowBase.mMno == Mno.TMOUS && !TextUtils.isEmpty(map.get("root/application/1/im/ext/max_adhoc_open_group_size"))) {
            Locale locale = Locale.US;
            map.put("root/application/1/im/ext/ftMSRPftWarnSize".toLowerCase(locale), map.get("root/application/1/im/ftWarnSize".toLowerCase(locale)));
            map.put("root/application/1/im/ext/ftMSRPMaxSizeFileTr".toLowerCase(locale), map.get("root/application/1/im/MaxSizeFileTr".toLowerCase(locale)));
            map.put("root/application/1/im/ext/ftMSRPMaxSizeFileTrIncoming".toLowerCase(locale), map.get("root/application/1/im/MaxSizeFileTrIncoming".toLowerCase(locale)));
            map.put("root/application/1/im/ext/max_adhoc_closed_group_size".toLowerCase(locale), map.get("root/application/1/im/max_adhoc_group_size".toLowerCase(locale)));
            map.put("root/application/1/im/ftWarnSize".toLowerCase(locale), map.get("root/application/1/im/ext/fthttpftwarnsize"));
            map.put("root/application/1/im/MaxSizeFileTr".toLowerCase(locale), map.get("root/application/1/im/ext/fthttpmaxsizefiletr"));
            map.put("root/application/1/im/MaxSizeFileTrIncoming".toLowerCase(locale), map.get("root/application/1/im/ext/fthttpmaxsizefiletrincoming"));
            map.put("root/application/1/im/max_adhoc_group_size".toLowerCase(locale), map.get("root/application/1/im/ext/max_adhoc_open_group_size"));
            map.remove("root/application/1/im/ext/fthttpftwarnsize");
            map.remove("root/application/1/im/ext/fthttpmaxsizefiletr");
            map.remove("root/application/1/im/ext/fthttpmaxsizefiletrincoming");
            map.remove("root/application/1/im/ext/max_adhoc_open_group_size");
        }
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getUserMessage(Map<String, String> map) {
        HashMap hashMap = new HashMap();
        for (Map.Entry next : map.entrySet()) {
            if (((String) next.getKey()).startsWith(ConfigConstants.PATH.MSG)) {
                hashMap.put((String) next.getKey(), (String) next.getValue());
            }
        }
        return hashMap;
    }

    /* access modifiers changed from: package-private */
    public UserAccept getUserAcceptDetailed(Map<String, String> map) {
        if (this.mWorkflowBase.mMno.isEur() && !ConfigUtil.isSecDmaPackageInuse(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return UserAccept.NON_DEFAULT_MSG_APP;
        }
        int version = this.mWorkflowBase.getVersion();
        int version2 = this.mWorkflowBase.getVersion(map);
        Map<String, String> userMessage = getUserMessage(map);
        boolean z = true;
        if (userMessage.size() == 4) {
            if (version <= (this.mWorkflowBase.mMno == Mno.ATT ? 1 : 0) || version2 <= 0) {
                z = getUserAcceptWithDialog(userMessage);
            }
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getUserAccept: userAccept: " + z + " oldVersion: " + version + ", newVersion: " + version2);
        return z ? UserAccept.ACCEPT : UserAccept.REJECT;
    }

    /* access modifiers changed from: protected */
    public boolean getUserAccept(Map<String, String> map) {
        return getUserAcceptDetailed(map) == UserAccept.ACCEPT;
    }

    /* access modifiers changed from: protected */
    public boolean getUserAcceptWithDialog(Map<String, String> map) {
        this.mWorkflowBase.mPowerController.release();
        boolean acceptReject = this.mWorkflowBase.mDialog.getAcceptReject(map.get(ConfigConstants.PATH.MSG_TITLE), map.get(ConfigConstants.PATH.MSG_MESSAGE), map.get(ConfigConstants.PATH.MSG_ACCEPT_BUTTON), map.get(ConfigConstants.PATH.MSG_REJECT_BUTTON), this.mPhoneId);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getUserAcceptWithDialog: userAccept: " + acceptReject);
        this.mWorkflowBase.mPowerController.lock();
        return acceptReject;
    }

    /* access modifiers changed from: protected */
    public void setOpModeWithUserAccept(boolean z, Map<String, String> map, WorkflowBase.OpMode opMode) {
        if (z) {
            WorkflowBase workflowBase = this.mWorkflowBase;
            workflowBase.setOpMode(workflowBase.getOpMode(map), map);
            return;
        }
        this.mWorkflowBase.setOpMode(opMode, (Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public void checkSetToGS(Map<String, String> map) {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "checkSetToGS:");
        setChatSettings(map);
        setGroupChatSettings(map);
        String setting = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.STANDALONE_MSG_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(setting)) {
            int i = this.mPhoneId;
            IMSLog.i(str, i, "SlmAuth set to " + setting);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/standaloneMsgAuth".toLowerCase(Locale.US), setting);
            } else {
                map.put("root/application/1/services/standaloneMsgAuth".toLowerCase(Locale.US), setting);
            }
        }
        String setting2 = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.GEOPUSH_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(setting2)) {
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "GeoPushAuth set to " + setting2);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/geolocPushAuth".toLowerCase(Locale.US), setting2);
            } else {
                map.put("root/application/1/services/geolocPushAuth".toLowerCase(Locale.US), setting2);
            }
        }
        setFtSettings(map);
        setUxSettings(map);
        setClientControlSettings(map);
        setCapabilitySettings(map);
    }

    /* access modifiers changed from: protected */
    public boolean setRcsClientConfiguration(String str, String str2, String str3, String str4) {
        if (!RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return false;
        }
        if (!isRcsClientConfigurationInfoChanged(str, str2, str3, str4)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "setRcsClientConfiguration: rcsClientConfigurationInfo is not changed");
            return false;
        }
        String str5 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str5, i, "setRcsClientConfiguration: rcsVersion: " + str + " rcsProfile: " + str2 + " clientVendor: " + str3 + " clientVersion: " + str4 + ": set this info in storage");
        this.mRcsVersion = str;
        this.mRcsProfile = str2;
        this.mClientVendor = str3;
        this.mClientVersion = str4;
        String str6 = "";
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.INFO_RCS_VERSION, TextUtils.isEmpty(str) ? str6 : this.mRcsVersion);
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.INFO_RCS_PROFILE, TextUtils.isEmpty(this.mRcsProfile) ? str6 : this.mRcsProfile);
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.INFO_CLIENT_VENDOR, TextUtils.isEmpty(this.mClientVendor) ? str6 : this.mClientVendor);
        IStorageAdapter iStorageAdapter = this.mWorkflowBase.mStorage;
        if (!TextUtils.isEmpty(this.mClientVersion)) {
            str6 = this.mClientVersion;
        }
        iStorageAdapter.write(ConfigConstants.PATH.INFO_CLIENT_VERSION, str6);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean setRcsSwitchValue(String str) {
        if (!RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return false;
        }
        if (!isRcsEnabledByUserChanged(str)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "setRcsSwitchValue: RcsSwitchValue is not changed");
            return false;
        }
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "setRcsSwitchValue: rcsEnabledByUser: " + str);
        this.mRcsEnabledByUser = str;
        this.mWorkflowBase.mStorage.write(ConfigConstants.PATH.INFO_RCS_ENABLED_BY_USER, TextUtils.isEmpty(str) ? "" : this.mRcsEnabledByUser);
        return true;
    }

    /* access modifiers changed from: protected */
    public String getRcsVersionFromStorage() {
        return this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.INFO_RCS_VERSION);
    }

    /* access modifiers changed from: protected */
    public String getRcsVersion(boolean z) {
        if (!RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return "";
        }
        String rcsVersionFromStorage = getRcsVersionFromStorage();
        if (TextUtils.isEmpty(rcsVersionFromStorage)) {
            rcsVersionFromStorage = SecImsNotifier.getInstance().getRcsClientConfiguration(this.mPhoneId, 0);
        }
        if (TextUtils.isEmpty(rcsVersionFromStorage) && z) {
            rcsVersionFromStorage = ConfigConstants.PVALUE.GOOG_DEFAULT_RCS_VERSION;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getRcsVersion: " + rcsVersionFromStorage);
        return rcsVersionFromStorage;
    }

    /* access modifiers changed from: protected */
    public String getRcsProfileFromStorage() {
        return this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.INFO_RCS_PROFILE);
    }

    /* access modifiers changed from: protected */
    public String getRcsProfile(boolean z) {
        if (!RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return "";
        }
        String rcsProfileFromStorage = getRcsProfileFromStorage();
        if (TextUtils.isEmpty(rcsProfileFromStorage)) {
            rcsProfileFromStorage = SecImsNotifier.getInstance().getRcsClientConfiguration(this.mPhoneId, 1);
        }
        if (TextUtils.isEmpty(rcsProfileFromStorage) && z) {
            rcsProfileFromStorage = "UP_1.0";
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getRcsProfile: " + rcsProfileFromStorage);
        return rcsProfileFromStorage;
    }

    /* access modifiers changed from: protected */
    public String getClientVendorFromStorage() {
        return this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.INFO_CLIENT_VENDOR);
    }

    /* access modifiers changed from: protected */
    public String getClientVendor(boolean z) {
        if (!RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return "";
        }
        String clientVendorFromStorage = getClientVendorFromStorage();
        if (TextUtils.isEmpty(clientVendorFromStorage)) {
            clientVendorFromStorage = SecImsNotifier.getInstance().getRcsClientConfiguration(this.mPhoneId, 2);
        }
        if (TextUtils.isEmpty(clientVendorFromStorage) && z) {
            clientVendorFromStorage = ConfigConstants.PVALUE.GOOG_DEFAULT_CLIENT_VENDOR;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getClientVendor: " + clientVendorFromStorage);
        return clientVendorFromStorage;
    }

    /* access modifiers changed from: protected */
    public String getClientVersionFromStorage() {
        return this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.INFO_CLIENT_VERSION);
    }

    /* access modifiers changed from: protected */
    public String getClientVersion(boolean z) {
        if (!RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return "";
        }
        String clientVersionFromStorage = getClientVersionFromStorage();
        if (TextUtils.isEmpty(clientVersionFromStorage)) {
            clientVersionFromStorage = SecImsNotifier.getInstance().getRcsClientConfiguration(this.mPhoneId, 3);
        }
        if (TextUtils.isEmpty(clientVersionFromStorage) && z) {
            clientVersionFromStorage = ConfigConstants.PVALUE.GOOG_DEFAULT_CLIENT_VERSION;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getClientVersion: " + clientVersionFromStorage);
        return clientVersionFromStorage;
    }

    /* access modifiers changed from: protected */
    public String getRcsEnabledByUserFromStorage() {
        return this.mWorkflowBase.mStorage.read(ConfigConstants.PATH.INFO_RCS_ENABLED_BY_USER);
    }

    /* access modifiers changed from: protected */
    public String isRcsEnabledByUser(boolean z) {
        if (!RcsUtils.isImsSingleRegiRequired(this.mWorkflowBase.mContext, this.mPhoneId)) {
            return "";
        }
        String rcsEnabledByUserFromStorage = getRcsEnabledByUserFromStorage();
        if (TextUtils.isEmpty(rcsEnabledByUserFromStorage)) {
            rcsEnabledByUserFromStorage = SecImsNotifier.getInstance().getRcsClientConfiguration(this.mPhoneId, 4);
        }
        if (TextUtils.isEmpty(rcsEnabledByUserFromStorage) && z) {
            rcsEnabledByUserFromStorage = "1";
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "isRcsEnabledByUser: " + rcsEnabledByUserFromStorage);
        return rcsEnabledByUserFromStorage;
    }

    public boolean isRcsVersionChanged(String str) {
        this.mRcsVersion = !TextUtils.isEmpty(this.mRcsVersion) ? this.mRcsVersion : getRcsVersionFromStorage();
        return !TextUtils.isEmpty(str) && !TextUtils.equals(str, this.mRcsVersion);
    }

    public boolean isRcsProfileChanged(String str) {
        this.mRcsProfile = !TextUtils.isEmpty(this.mRcsProfile) ? this.mRcsProfile : getRcsProfileFromStorage();
        return !TextUtils.isEmpty(str) && !TextUtils.equals(str, this.mRcsProfile);
    }

    public boolean isClientVendorChanged(String str) {
        this.mClientVendor = !TextUtils.isEmpty(this.mClientVendor) ? this.mClientVendor : getClientVendorFromStorage();
        return !TextUtils.isEmpty(str) && !TextUtils.equals(str, this.mClientVendor);
    }

    public boolean isClientVersionChanged(String str) {
        this.mClientVersion = !TextUtils.isEmpty(this.mClientVersion) ? this.mClientVersion : getClientVersionFromStorage();
        return !TextUtils.isEmpty(str) && !TextUtils.equals(str, this.mClientVersion);
    }

    public boolean isRcsEnabledByUserChanged(String str) {
        this.mRcsEnabledByUser = !TextUtils.isEmpty(this.mRcsEnabledByUser) ? this.mRcsEnabledByUser : getRcsEnabledByUserFromStorage();
        return !TextUtils.isEmpty(str) && !TextUtils.equals(str, this.mRcsEnabledByUser);
    }

    public boolean isRcsClientConfigurationInfoChanged(String str, String str2, String str3, String str4) {
        return isRcsVersionChanged(str) || isRcsProfileChanged(str2) || isClientVendorChanged(str3) || isClientVersionChanged(str4);
    }

    public boolean isRcsClientConfigurationInfoNotSet() {
        if (TextUtils.isEmpty(getRcsVersion(false)) || TextUtils.isEmpty(getRcsProfile(false)) || TextUtils.isEmpty(getClientVendor(false)) || TextUtils.isEmpty(getClientVersion(false))) {
            return true;
        }
        return false;
    }

    private void setChatSettings(Map<String, String> map) {
        String setting = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.CHAT_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(setting)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "ChatAuth set to " + setting);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/ChatAuth".toLowerCase(Locale.US), setting);
            } else {
                map.put("root/application/1/services/ChatAuth".toLowerCase(Locale.US), setting);
            }
        }
        String setting2 = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.IM_SESSION_TIMER, this.mPhoneId);
        if (!TextUtils.isEmpty(setting2)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "ImSessionTimer set to " + setting2);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/TimerIdle".toLowerCase(Locale.US), setting2);
            } else {
                map.put("root/application/1/im/TimerIdle".toLowerCase(Locale.US), setting2);
            }
        }
    }

    private void setGroupChatSettings(Map<String, String> map) {
        String setting = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.GROUP_CHAT_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(setting)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "GroupChatAuth set to " + setting);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/services/GroupChatAuth".toLowerCase(Locale.US), setting);
            } else {
                map.put("root/application/1/services/GroupChatAuth".toLowerCase(Locale.US), setting);
            }
        }
        String setting2 = ConfigUtil.getSetting("max_adhoc_group_size", this.mPhoneId);
        if (!TextUtils.isEmpty(setting2)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "MaxAdhocGroupSize set to " + setting2);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US), setting2);
            } else {
                map.put("root/application/1/im/max_adhoc_group_size".toLowerCase(Locale.US), setting2);
            }
        }
        String setting3 = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.AUTO_ACCEPT_GROUP_CHAT, this.mPhoneId);
        if (!TextUtils.isEmpty(setting3)) {
            String str3 = LOG_TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str3, i3, "AutoAcceptGroupChat set to " + setting3);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/autacceptgroupchat".toLowerCase(Locale.US), setting3);
            } else {
                map.put("root/application/1/im/autacceptgroupchat".toLowerCase(Locale.US), setting3);
            }
        }
    }

    private void setFtSettings(Map<String, String> map) {
        String setting = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.FT_DEFAULT_MECH, this.mPhoneId);
        if (!TextUtils.isEmpty(setting)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "FtDefaultMech set to " + setting);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/im/ftDefaultMech".toLowerCase(Locale.US), setting);
            } else {
                map.put("root/application/1/im/ftDefaultMech".toLowerCase(Locale.US), setting);
            }
        }
    }

    private void setUxSettings(Map<String, String> map) {
        String setting = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.MESSAGING_UX, this.mPhoneId);
        if (!TextUtils.isEmpty(setting)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "MessagingUx set to " + setting);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/ux/messagingUX".toLowerCase(Locale.US), setting);
            } else {
                map.put("root/application/1/ux/messagingUX".toLowerCase(Locale.US), setting);
            }
        }
        String setting2 = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.USER_ALIAS_AUTH, this.mPhoneId);
        if (!TextUtils.isEmpty(setting2)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "UserAliasAuth set to " + setting2);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/ux/userAliasAuth".toLowerCase(Locale.US), setting2);
            } else {
                map.put("root/application/1/ux/userAliasAuth".toLowerCase(Locale.US), setting2);
            }
        }
    }

    private void setClientControlSettings(Map<String, String> map) {
        String setting = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.RECONNECT_GUARD_TIMER, this.mPhoneId);
        if (!TextUtils.isEmpty(setting)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "ReconGuardTimer set to " + setting);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/clientControl/reconnectGuardTimer".toLowerCase(Locale.US), setting);
            } else {
                map.put("root/application/1/clientControl/reconnectGuardTimer".toLowerCase(Locale.US), setting);
            }
        }
        String setting2 = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.MAX_1TO_MANY_RECIPIENTS, this.mPhoneId);
        if (!TextUtils.isEmpty(setting2)) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "Max1ToManyRecipients set to " + setting2);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/clientControl/max1toManyRecipients".toLowerCase(Locale.US), setting2);
            } else {
                map.put("root/application/1/clientControl/max1toManyRecipients".toLowerCase(Locale.US), setting2);
            }
        }
    }

    private void setCapabilitySettings(Map<String, String> map) {
        String setting = ConfigUtil.getSetting(GlobalSettingsConstants.RCS.CAPABILITY_DISCOVERY_MECH, this.mPhoneId);
        if (!TextUtils.isEmpty(setting)) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "CapDiscMech set to " + setting);
            if (map == null) {
                this.mWorkflowBase.mStorage.write("root/application/1/capdiscovery/defaultdisc".toLowerCase(Locale.US), setting);
            } else {
                map.put("root/application/1/capdiscovery/defaultdisc".toLowerCase(Locale.US), setting);
            }
        }
    }

    public byte[] getProvisioningXml(boolean z) {
        String str;
        RandomAccessFile randomAccessFile;
        Throwable th;
        Element element;
        Iterator<Map.Entry<String, String>> it;
        WorkflowParamHandler workflowParamHandler;
        String str2;
        Element element2;
        NodeList nodeList;
        String str3;
        String str4;
        Map.Entry entry;
        Iterator<Map.Entry<String, String>> it2;
        boolean z2;
        int indexOf;
        WorkflowParamHandler workflowParamHandler2 = this;
        Map<String, String> readAll = workflowParamHandler2.mWorkflowBase.mStorage.readAll();
        String str5 = "";
        if (readAll == null) {
            IMSLog.d(LOG_TAG, "readData is null!");
            return str5.getBytes();
        }
        try {
            Document newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element createElement = newDocument.createElement("wap-provisioningdoc");
            newDocument.appendChild(createElement);
            Attr createAttribute = newDocument.createAttribute("version");
            createAttribute.setValue("1.1");
            createElement.setAttributeNode(createAttribute);
            Iterator<Map.Entry<String, String>> it3 = readAll.entrySet().iterator();
            boolean z3 = false;
            while (it3.hasNext()) {
                Map.Entry next = it3.next();
                String str6 = (String) next.getKey();
                if (!str6.startsWith("root") || TextUtils.isEmpty((CharSequence) next.getValue())) {
                    boolean z4 = z;
                    workflowParamHandler = workflowParamHandler2;
                    it = it3;
                    str = str5;
                    element = createElement;
                } else {
                    IMSLog.d(LOG_TAG, "Path: " + str6);
                    int indexOf2 = str6.indexOf(47);
                    String str7 = str5;
                    String str8 = str7;
                    int i = indexOf2;
                    int indexOf3 = str6.indexOf(47, indexOf2 + 1);
                    while (true) {
                        str2 = "type";
                        if (indexOf3 <= 0) {
                            break;
                        }
                        int i2 = indexOf3 + 1;
                        if (isIndexTag(str6.substring(i2))) {
                            break;
                        }
                        String substring = str6.substring(i + 1, indexOf3);
                        if (!isIndexTag(substring)) {
                            it2 = it3;
                            NodeList elementsByTagName = newDocument.getElementsByTagName("characteristic");
                            str = str5;
                            int i3 = 0;
                            while (true) {
                                try {
                                    if (i3 >= elementsByTagName.getLength()) {
                                        entry = next;
                                        z2 = false;
                                        break;
                                    }
                                    Node item = elementsByTagName.item(i3);
                                    NodeList nodeList2 = elementsByTagName;
                                    entry = next;
                                    if (item.getNodeType() == 1) {
                                        Element element3 = (Element) item;
                                        if (!element3.getAttribute(str2).equalsIgnoreCase(substring)) {
                                            continue;
                                        } else if (isListTagName(substring)) {
                                            String substring2 = str6.substring(i2, indexOf3 + 2);
                                            if (!substring2.isEmpty() && element3.getAttribute("id").equalsIgnoreCase(substring2)) {
                                                break;
                                            }
                                        } else if (((Element) element3.getParentNode()).getAttribute(str2).equalsIgnoreCase(str7)) {
                                            break;
                                        }
                                    }
                                    i3++;
                                    elementsByTagName = nodeList2;
                                    next = entry;
                                } catch (TransformerException e) {
                                    e = e;
                                    e.printStackTrace();
                                    return str.getBytes();
                                } catch (IOException | ParserConfigurationException e2) {
                                    e = e2;
                                    e.printStackTrace();
                                    return str.getBytes();
                                }
                            }
                            z2 = true;
                            if (!z2) {
                                if (!str7.isEmpty()) {
                                    NodeList elementsByTagName2 = newDocument.getElementsByTagName("characteristic");
                                    int i4 = 0;
                                    while (i4 < elementsByTagName2.getLength()) {
                                        Node item2 = elementsByTagName2.item(i4);
                                        NodeList nodeList3 = elementsByTagName2;
                                        if (item2.getNodeType() == 1) {
                                            Element element4 = (Element) item2;
                                            if (element4.getAttribute(str2).equalsIgnoreCase(str7)) {
                                                String previousString = previousString(str6, str7);
                                                String attribute = ((Element) element4.getParentNode()).getAttribute(str2);
                                                if (attribute.isEmpty() || previousString.isEmpty() || attribute.equalsIgnoreCase(previousString)) {
                                                    if (isListTagName(str7)) {
                                                        String substring3 = str6.substring(i - 1, i);
                                                        if (!substring3.isEmpty() && !element4.getAttribute("id").equalsIgnoreCase(substring3)) {
                                                        }
                                                    }
                                                    Element createElement2 = newDocument.createElement("characteristic");
                                                    element4.appendChild(createElement2);
                                                    if (isListTagName(substring) && (indexOf = str6.indexOf(47, i2)) > 0) {
                                                        String substring4 = str6.substring(i2, indexOf);
                                                        Attr createAttribute2 = newDocument.createAttribute("id");
                                                        createAttribute2.setValue(substring4);
                                                        createElement2.setAttributeNode(createAttribute2);
                                                    }
                                                    Attr createAttribute3 = newDocument.createAttribute(str2);
                                                    createAttribute3.setValue(substring);
                                                    createElement2.setAttributeNode(createAttribute3);
                                                }
                                            }
                                        }
                                        i4++;
                                        elementsByTagName2 = nodeList3;
                                    }
                                } else {
                                    Element createElement3 = newDocument.createElement("characteristic");
                                    createElement.appendChild(createElement3);
                                    if (isListTagName(substring)) {
                                        String substring5 = str6.substring(i2, str6.indexOf(47, i2));
                                        Attr createAttribute4 = newDocument.createAttribute("id");
                                        createAttribute4.setValue(substring5);
                                        createElement3.setAttributeNode(createAttribute4);
                                    }
                                    Attr createAttribute5 = newDocument.createAttribute(str2);
                                    createAttribute5.setValue(substring);
                                    createElement3.setAttributeNode(createAttribute5);
                                }
                            }
                            str7 = substring;
                        } else {
                            it2 = it3;
                            str = str5;
                            entry = next;
                        }
                        str8 = str7;
                        i = indexOf3;
                        str5 = str;
                        next = entry;
                        indexOf3 = str6.indexOf(47, i2);
                        it3 = it2;
                    }
                    it = it3;
                    str = str5;
                    Map.Entry entry2 = next;
                    String substring6 = str6.substring(str6.lastIndexOf(47) + 1);
                    if (isIndexTag(substring6)) {
                        substring6 = str6.substring(i + 1, indexOf3);
                    }
                    if (str8.isEmpty()) {
                        Element createElement4 = newDocument.createElement("parm");
                        createElement.appendChild(createElement4);
                        Attr createAttribute6 = newDocument.createAttribute("name");
                        createAttribute6.setValue(substring6);
                        createElement4.setAttributeNode(createAttribute6);
                        Attr createAttribute7 = newDocument.createAttribute("value");
                        createAttribute7.setValue((String) entry2.getValue());
                        createElement4.setAttributeNode(createAttribute7);
                    } else {
                        NodeList elementsByTagName3 = newDocument.getElementsByTagName("characteristic");
                        int i5 = 0;
                        while (i5 < elementsByTagName3.getLength()) {
                            Node item3 = elementsByTagName3.item(i5);
                            if (item3.getNodeType() == 1) {
                                Element element5 = (Element) item3;
                                if (element5.getAttribute(str2).equalsIgnoreCase(str8)) {
                                    String attribute2 = element5.getAttribute("id");
                                    String attribute3 = ((Element) element5.getParentNode()).getAttribute(str2);
                                    nodeList = elementsByTagName3;
                                    String attribute4 = ((Element) element5.getParentNode()).getAttribute("id");
                                    element2 = createElement;
                                    if (!attribute3.isEmpty()) {
                                        str3 = str2;
                                        str4 = attribute3 + "/";
                                    } else {
                                        str3 = str2;
                                        str4 = str;
                                    }
                                    if (!attribute4.isEmpty()) {
                                        str4 = str4 + attribute4 + "/";
                                    }
                                    if (!attribute2.isEmpty()) {
                                        str4 = str4 + str8 + "/" + attribute2;
                                    }
                                    if (str4.isEmpty()) {
                                        str4 = str8 + "/";
                                    }
                                    if (str6.contains(str4)) {
                                        Element createElement5 = newDocument.createElement("parm");
                                        element5.appendChild(createElement5);
                                        Attr createAttribute8 = newDocument.createAttribute("name");
                                        createAttribute8.setValue(substring6);
                                        createElement5.setAttributeNode(createAttribute8);
                                        Attr createAttribute9 = newDocument.createAttribute("value");
                                        createAttribute9.setValue(ConfigUtil.decryptConfigParams(substring6, (String) entry2.getValue(), this.mWorkflowBase.mMno, z));
                                        createElement5.setAttributeNode(createAttribute9);
                                    } else {
                                        boolean z5 = z;
                                    }
                                    i5++;
                                    str2 = str3;
                                    elementsByTagName3 = nodeList;
                                    createElement = element2;
                                }
                            }
                            boolean z6 = z;
                            nodeList = elementsByTagName3;
                            element2 = createElement;
                            str3 = str2;
                            i5++;
                            str2 = str3;
                            elementsByTagName3 = nodeList;
                            createElement = element2;
                        }
                    }
                    workflowParamHandler = this;
                    boolean z7 = z;
                    element = createElement;
                    z3 = true;
                }
                workflowParamHandler2 = workflowParamHandler;
                it3 = it;
                str5 = str;
                createElement = element;
            }
            WorkflowParamHandler workflowParamHandler3 = workflowParamHandler2;
            str = str5;
            if (z3) {
                NodeList elementsByTagName4 = newDocument.getElementsByTagName("characteristic");
                for (int i6 = 0; i6 < elementsByTagName4.getLength(); i6++) {
                    Node item4 = elementsByTagName4.item(i6);
                    if (item4.getNodeType() == 1) {
                        Element element6 = (Element) item4;
                        if (!TextUtils.isEmpty(element6.getAttribute("id"))) {
                            element6.removeAttribute("id");
                        }
                    }
                }
                Transformer newTransformer = TransformerFactory.newInstance().newTransformer();
                newTransformer.setOutputProperty("indent", "yes");
                newTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", DiagnosisConstants.RCSM_ORST_HTTP);
                DOMSource dOMSource = new DOMSource(newDocument);
                File file = new File(workflowParamHandler3.mWorkflowBase.mContext.getFilesDir(), "composedXmlFile");
                newTransformer.transform(dOMSource, new StreamResult(file));
                try {
                    randomAccessFile = new RandomAccessFile(file, "rw");
                    long length = randomAccessFile.length();
                    if (length != 0) {
                        do {
                            length--;
                            randomAccessFile.seek(length);
                            if (randomAccessFile.readByte() == 10 || length <= 0) {
                                randomAccessFile.setLength(length);
                            }
                            length--;
                            randomAccessFile.seek(length);
                            break;
                        } while (length <= 0);
                        randomAccessFile.setLength(length);
                    }
                    randomAccessFile.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                return Files.readAllBytes(file.toPath());
            }
        } catch (TransformerException e4) {
            e = e4;
            str = str5;
            e.printStackTrace();
            return str.getBytes();
        } catch (IOException | ParserConfigurationException e5) {
            e = e5;
            str = str5;
            e.printStackTrace();
            return str.getBytes();
        }
        return str.getBytes();
        throw th;
    }

    public static boolean isListTagName(String str) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("application");
        arrayList.add("conref");
        arrayList.add("icsi");
        arrayList.add("icsi_resource_allocation_mode");
        arrayList.add("address");
        arrayList.add(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE);
        arrayList.add("phonecontext");
        arrayList.add(ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY);
        boolean z = false;
        for (String equalsIgnoreCase : arrayList) {
            if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                z = true;
            }
        }
        return z;
    }

    public static boolean isIndexTag(String str) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("0");
        arrayList.add("1");
        arrayList.add("2");
        arrayList.add(DiagnosisConstants.RCSM_ORST_REGI);
        arrayList.add(DiagnosisConstants.RCSM_ORST_HTTP);
        arrayList.add(DiagnosisConstants.RCSM_ORST_ITER);
        arrayList.add("6");
        arrayList.add("7");
        arrayList.add("8");
        arrayList.add("9");
        boolean z = false;
        for (String equalsIgnoreCase : arrayList) {
            if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                z = true;
            }
        }
        return z;
    }

    public static String previousString(String str, String str2) {
        String[] split = str.split("/");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equalsIgnoreCase(str2)) {
                int i2 = i - 1;
                if (isIndexTag(split[i2])) {
                    return split[i - 2];
                }
                return split[i2];
            }
        }
        return "";
    }
}
