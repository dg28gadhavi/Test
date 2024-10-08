package com.sec.internal.ims.servicemodules.ss;

import android.os.Build;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.XmlElement;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import com.sec.internal.ims.servicemodules.ss.SsRuleData;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class UtUtils {
    public static final String DOMAIN_NAME = ".3gppnetwork.org";
    private static final String LOG_TAG = "UtUtils";
    private static final Pattern PATTERN_TEL_NUMBER = Pattern.compile("[+]?[#*\\-.()0-9]+");
    private static final Pattern PATTERN_WHITE_SPACES = Pattern.compile("\\s+");
    public static final String XCAP_DOMAIN_NAME = ".pub.3gppnetwork.org";
    public static final String XMLNS_CP = "urn:ietf:params:xml:ns:common-policy";
    public static final String XMLNS_SS = "http://uri.etsi.org/ngn/params/xml/simservs/xcap";

    public static int doconvertCBType(boolean z, int i) {
        switch (i) {
            case 1:
            case 5:
            case 6:
            case 9:
            case 10:
                return z ? 103 : 102;
            case 2:
            case 3:
            case 4:
            case 8:
                return z ? 105 : 104;
            case 7:
                return z ? 119 : 118;
            default:
                return 0;
        }
    }

    public static String doconvertCondition(int i) {
        return i != 1 ? i != 2 ? i != 3 ? i != 6 ? "" : "not-registered" : "not-reachable" : "no-answer" : "busy";
    }

    public static boolean isCallBarringType(int i) {
        return i == 102 || i == 103 || i == 104 || i == 105;
    }

    public static XmlElement makeMultipleXml(CallForwardingData callForwardingData, Mno mno, boolean z) {
        XmlElement xmlElement;
        XmlElement addAttribute = new XmlElement("communication-diversion").addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, CloudMessageProviderContract.JsonData.TRUE);
        if (z) {
            addAttribute.setNamespace("ss").addAttribute("xmlns:ss", XMLNS_SS);
        }
        int i = callForwardingData.replyTimer;
        if (i > 0 && mno != Mno.FET) {
            addAttribute.addChildElement(makeNoReplyTimerXml(i, z));
        }
        XmlElement addAttribute2 = new XmlElement("ruleset").setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX).addAttribute("xmlns:cp", XMLNS_CP);
        for (SsRuleData.SsRule next : callForwardingData.rules) {
            if (mno == Mno.FET && next.conditions.condition == 2) {
                xmlElement = makeSingleXml((CallForwardingData.Rule) next, z, mno, callForwardingData.replyTimer);
            } else {
                xmlElement = makeSingleXml((CallForwardingData.Rule) next, z, mno);
            }
            addAttribute2.addChildElement(xmlElement);
        }
        addAttribute.addChildElement(addAttribute2);
        return addAttribute;
    }

    public static XmlElement makeNoReplyTimerXml(int i, boolean z) {
        XmlElement value = new XmlElement("NoReplyTimer").setValue(i);
        if (z) {
            value.setNamespace("ss");
        }
        return value;
    }

    public static XmlElement makeMultipleXml(CallBarringData callBarringData, int i, Mno mno, boolean z) {
        XmlElement addAttribute = new XmlElement(i == 105 ? UtElement.ELEMENT_OCB : UtElement.ELEMENT_ICB).addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, CloudMessageProviderContract.JsonData.TRUE);
        if (z) {
            addAttribute.setNamespace("ss").addAttribute("xmlns:ss", XMLNS_SS);
        }
        XmlElement addAttribute2 = new XmlElement("ruleset").setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX).addAttribute("xmlns:cp", XMLNS_CP);
        Iterator<SsRuleData.SsRule> it = callBarringData.rules.iterator();
        while (it.hasNext()) {
            addAttribute2.addChildElement(makeSingleXml((CallBarringData.Rule) it.next(), mno, z));
        }
        addAttribute.addChildElement(addAttribute2);
        return addAttribute;
    }

    public static XmlElement makeSingleXml(String str, boolean z, boolean z2) {
        XmlElement addAttribute = new XmlElement(str).addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, z ? CloudMessageProviderContract.JsonData.TRUE : ConfigConstants.VALUE.INFO_COMPLETED);
        if (z2) {
            addAttribute.setNamespace("ss").addAttribute("xmlns:ss", XMLNS_SS);
        }
        return addAttribute;
    }

    public static XmlElement makeSingleXml(String str, int i, boolean z) {
        XmlElement xmlElement = new XmlElement(str);
        XmlElement xmlElement2 = new XmlElement(UtElement.ELEMENT_DEFAULT_BEHAV);
        if (z) {
            xmlElement.setNamespace("ss").addAttribute("xmlns:ss", XMLNS_SS);
            xmlElement2.setNamespace("ss");
        }
        xmlElement.addAttribute(SoftphoneNamespaces.SoftphoneCallHandling.ACTIVE, i == 0 ? ConfigConstants.VALUE.INFO_COMPLETED : CloudMessageProviderContract.JsonData.TRUE);
        xmlElement2.setValue(i == 1 ? UtElement.ELEMENT_CLI_RESTRICTED : UtElement.ELEMENT_CLI_NOT_RESTRICTED);
        xmlElement.addChildElement(xmlElement2);
        return xmlElement;
    }

    public static XmlElement makeSingleXml(CallForwardingData.Rule rule, boolean z, Mno mno) {
        return makeSingleXml(rule, z, mno, 0);
    }

    private static XmlElement setMediaElement(MEDIA media, boolean z) {
        XmlElement xmlElement = new XmlElement("media");
        if (z) {
            xmlElement.setNamespace("ss");
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA[media.ordinal()];
        if (i == 1) {
            xmlElement.setValue("audio");
        } else if (i == 2) {
            xmlElement.setValue(SipMsg.FEATURE_TAG_MMTEL_VIDEO);
        }
        if (media != MEDIA.ALL) {
            return xmlElement;
        }
        return null;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.ss.UtUtils$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.sec.internal.ims.servicemodules.ss.MEDIA[] r0 = com.sec.internal.ims.servicemodules.ss.MEDIA.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA = r0
                com.sec.internal.ims.servicemodules.ss.MEDIA r1 = com.sec.internal.ims.servicemodules.ss.MEDIA.AUDIO     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$ss$MEDIA     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.ss.MEDIA r1 = com.sec.internal.ims.servicemodules.ss.MEDIA.VIDEO     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.ss.UtUtils.AnonymousClass1.<clinit>():void");
        }
    }

    public static XmlElement makeSingleXml(CallForwardingData.Rule rule, boolean z, Mno mno, int i) {
        XmlElement xmlElement;
        CallForwardingData.Rule rule2 = rule;
        boolean z2 = z;
        Mno mno2 = mno;
        int i2 = i;
        XmlElement addAttribute = new XmlElement("rule").setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX).addAttribute("id", rule2.ruleId);
        XmlElement namespace = new XmlElement("conditions").setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX);
        if (!rule2.conditions.state) {
            XmlElement xmlElement2 = new XmlElement("rule-deactivated");
            if (z2) {
                xmlElement2.setNamespace("ss");
            }
            namespace.addChildElement(xmlElement2);
        }
        String doconvertCondition = doconvertCondition(rule2.conditions.condition);
        if (!doconvertCondition.isEmpty()) {
            XmlElement xmlElement3 = new XmlElement(doconvertCondition);
            if (z2) {
                xmlElement3.setNamespace("ss");
            }
            namespace.addChildElement(xmlElement3);
        }
        List<MEDIA> list = rule2.conditions.media;
        if (list != null && list.size() > 0) {
            for (MEDIA mediaElement : rule2.conditions.media) {
                XmlElement mediaElement2 = setMediaElement(mediaElement, z2);
                if (mediaElement2 != null) {
                    namespace.addChildElement(mediaElement2);
                }
            }
        }
        addAttribute.addChildElement(namespace);
        XmlElement namespace2 = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS).setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX);
        XmlElement xmlElement4 = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.FORWARD_TO);
        XmlElement xmlElement5 = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.TARGET);
        if (z2) {
            xmlElement4.setNamespace("ss");
            xmlElement5.setNamespace("ss");
        }
        if (!TextUtils.isEmpty(rule2.fwdElm.target)) {
            xmlElement5.setValue(rule2.fwdElm.target);
            xmlElement4.addChildElement(xmlElement5);
        } else if (!rule2.conditions.state && TextUtils.isEmpty(rule2.fwdElm.target)) {
            if (mno2 != Mno.ATT) {
                xmlElement4.addChildElement(xmlElement5);
            } else if (rule2.conditions.action == 4) {
                xmlElement4.addChildElement(xmlElement5);
            }
        }
        List<ForwardElm> list2 = rule2.fwdElm.fwdElm;
        if (list2 != null && list2.size() > 0) {
            for (int i3 = 0; i3 < rule2.fwdElm.fwdElm.size(); i3++) {
                String str = rule2.fwdElm.fwdElm.get(i3).id;
                String str2 = rule2.fwdElm.fwdElm.get(i3).value;
                String str3 = rule2.fwdElm.fwdElm.get(i3).attribute;
                String[] split = str.split(":");
                if (split.length == 1) {
                    xmlElement = new XmlElement(str, str2);
                } else if (split.length == 2) {
                    xmlElement = new XmlElement(split[1], str2, split[0]);
                } else {
                    Log.e(LOG_TAG, "This is out of specification. So never come here");
                }
                if (str3 != null) {
                    String[] split2 = str3.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    if (split2.length == 2 && (split2[1].startsWith(CmcConstants.E_NUM_STR_QUOTE) || split2[1].startsWith("'"))) {
                        String str4 = split2[0];
                        String str5 = split2[1];
                        xmlElement.addAttribute(str4, str5.substring(1, str5.length() - 1));
                        if (z2 && xmlElement.mNamespace == null && xmlElement.mAttributes.isEmpty()) {
                            xmlElement.setNamespace("ss");
                        }
                        xmlElement4.addChildElement(xmlElement);
                    }
                }
                xmlElement.setNamespace("ss");
                xmlElement4.addChildElement(xmlElement);
            }
        }
        namespace2.addChildElement(xmlElement4);
        if (i2 > 0) {
            namespace2.addChildElement(makeNoReplyTimerXml(i2, z2));
        }
        addAttribute.addChildElement(namespace2);
        return (mno2 == Mno.SMARTONE && rule2.conditions.action == 4) ? namespace : addAttribute;
    }

    public static XmlElement makeSingleXml(CallBarringData.Rule rule, Mno mno, boolean z) {
        XmlElement xmlElement;
        List<MEDIA> list;
        XmlElement addAttribute = new XmlElement("rule").setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX).addAttribute("id", rule.ruleId);
        XmlElement namespace = new XmlElement("conditions").setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX);
        String doconvertCbCondition = doconvertCbCondition(rule.conditions.condition);
        if (!doconvertCbCondition.isEmpty()) {
            if (rule.conditions.condition == 10 && mno == Mno.KDDI) {
                XmlElement namespace2 = new XmlElement(UtElement.ELEMENT_IDENTITY).setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX);
                for (String addAttribute2 : rule.target) {
                    namespace2.addChildElement(new XmlElement(UtElement.ELEMENT_ONE).setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX).addAttribute("id", addAttribute2));
                }
                namespace.addChildElement(namespace2);
            } else {
                XmlElement xmlElement2 = new XmlElement(doconvertCbCondition);
                if (doconvertCbCondition.equals(UtElement.ELEMENT_IDENTITY)) {
                    xmlElement2.setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX);
                } else if (z) {
                    xmlElement2.setNamespace("ss");
                }
                namespace.addChildElement(xmlElement2);
            }
        }
        Condition condition = rule.conditions;
        int i = condition.condition;
        if (!(i == 10 || i == 6 || (list = condition.media) == null || list.size() <= 0)) {
            for (MEDIA mediaElement : rule.conditions.media) {
                XmlElement mediaElement2 = setMediaElement(mediaElement, z);
                if (mediaElement2 != null) {
                    namespace.addChildElement(mediaElement2);
                }
            }
        }
        if (!rule.conditions.state) {
            XmlElement xmlElement3 = new XmlElement("rule-deactivated");
            if (z) {
                xmlElement3.setNamespace("ss");
            }
            namespace.addChildElement(xmlElement3);
        }
        XmlElement namespace3 = new XmlElement(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS).setNamespace(SoftphoneNamespaces.SoftphoneCallHandling.COMMON_POLICY_NS_PREFIX);
        XmlElement xmlElement4 = new XmlElement("allow");
        if (z) {
            xmlElement4.setNamespace("ss");
        }
        xmlElement4.setValue(ConfigConstants.VALUE.INFO_COMPLETED);
        namespace3.addChildElement(xmlElement4);
        if (mno.isOneOf(Mno.VIVACOM_BULGARIA, Mno.BATELCO_BAHRAIN, Mno.WIND_GREECE, Mno.CLARO_DOMINICAN, Mno.FET)) {
            for (ActionElm next : rule.actions) {
                String[] split = next.name.split(":");
                if (split.length == 1) {
                    xmlElement = new XmlElement(split[0], next.value);
                } else if (split.length == 2) {
                    xmlElement = new XmlElement(split[1], next.value, split[0]);
                } else {
                    Log.e(LOG_TAG, "This is out of specification. So never come here");
                }
                String str = next.attribute;
                if (str != null) {
                    String[] split2 = str.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    if (split2.length != 2) {
                        Log.e(LOG_TAG, "This is out of specification. So throw away attributes.");
                    } else if (split2[1].startsWith(CmcConstants.E_NUM_STR_QUOTE) || split2[1].startsWith("'")) {
                        String str2 = split2[0];
                        String str3 = split2[1];
                        xmlElement.addAttribute(str2, str3.substring(1, str3.length() - 1));
                    }
                }
                if (z && xmlElement.mNamespace == null && xmlElement.mAttributes.isEmpty()) {
                    xmlElement.setNamespace("ss");
                }
                namespace3.addChildElement(xmlElement);
            }
        }
        addAttribute.addChildElement(namespace);
        addAttribute.addChildElement(namespace3);
        return addAttribute;
    }

    public static String doconvertCbCondition(int i) {
        String str = LOG_TAG;
        Log.i(str, "convertICBtype type :" + i);
        if (i == 3) {
            return "international";
        }
        if (i == 4) {
            return "international-exHC";
        }
        if (i == 5) {
            return "roaming";
        }
        if (i != 6) {
            return i != 10 ? "" : UtElement.ELEMENT_IDENTITY;
        }
        return "anonymous";
    }

    public static int doconvertMediaTypeToSsClass(List<MEDIA> list) {
        if (list == null) {
            return 255;
        }
        if (list.contains(MEDIA.VIDEO)) {
            return 16;
        }
        return list.contains(MEDIA.AUDIO) ? 1 : 255;
    }

    public static MEDIA convertToMedia(int i) {
        if (i == 1) {
            return MEDIA.AUDIO;
        }
        if (i != 16) {
            return MEDIA.ALL;
        }
        return MEDIA.VIDEO;
    }

    public static int convertCbTypeToBitMask(int i) {
        if (i == 1) {
            return 8;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 2;
        }
        if (i == 4) {
            return 4;
        }
        if (i == 5) {
            return 10;
        }
        Log.e(LOG_TAG, "unexpected cbType");
        return 0;
    }

    public static int doConvertIpVersion(String str) {
        Log.i(LOG_TAG, "doConvertIpVersion type : " + str);
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1937632495:
                if (str.equals("ipv4only")) {
                    c = 0;
                    break;
                }
                break;
            case -1937599096:
                if (str.equals("ipv4pref")) {
                    c = 1;
                    break;
                }
                break;
            case -1935785453:
                if (str.equals("ipv6only")) {
                    c = 2;
                    break;
                }
                break;
            case -1935752054:
                if (str.equals("ipv6pref")) {
                    c = 3;
                    break;
                }
                break;
            case -1181903067:
                if (str.equals("ipv4v6")) {
                    c = 4;
                    break;
                }
                break;
            case 114167:
                if (str.equals("srv")) {
                    c = 5;
                    break;
                }
                break;
            case 104588379:
                if (str.equals("naptr")) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 5;
            case 2:
                return 2;
            case 3:
            case 4:
                return 6;
            case 5:
                return 4;
            case 6:
                return 3;
            default:
                return 0;
        }
    }

    private static String buildDomain(String str, String str2) {
        String str3;
        String str4;
        String lowerCase = str.toLowerCase(Locale.US);
        if (!lowerCase.contains("mncxxx.mccxxx")) {
            return lowerCase;
        }
        if (TextUtils.isEmpty(str2) || str2.length() < 5) {
            str4 = NSDSNamespaces.NSDSMigration.DEFAULT_KEY;
            str3 = str4;
        } else {
            str4 = str2.substring(0, 3);
            str3 = str2.substring(3);
            if (str3.length() == 2) {
                str3 = "0" + str3;
            }
        }
        return lowerCase.replace("mncxxx", "mnc" + str3).replace("mccxxx", "mcc" + str4);
    }

    public static String getNAFDomain(int i) {
        String str;
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.SS.AUTH_PROXY_IP, "");
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            return string;
        }
        Mno simMno = simManagerFromSimSlot.getSimMno();
        String simOperator = simManagerFromSimSlot.getSimOperator();
        if (simManagerFromSimSlot.hasNoSim()) {
            return string;
        }
        if (!TextUtils.isEmpty(string)) {
            return buildDomain(string, simOperator);
        }
        if (simManagerFromSimSlot.hasIsim()) {
            String impi = simManagerFromSimSlot.getImpi();
            if (impi == null) {
                return string;
            }
            if (simMno == Mno.BELL && ImsRegistry.getInt(i, GlobalSettingsConstants.SS.ENABLE_GBA, 0) == 1) {
                str = "naf." + impi.substring(impi.indexOf(64) + 1);
                Log.i(LOG_TAG, "xcapDomain :" + str);
            } else if (simMno == Mno.CMCC) {
                str = "xcap." + impi.substring(impi.indexOf(64) + 1);
                int indexOf = str.indexOf("mnc");
                if (indexOf > 0) {
                    str = str.replace(str.substring(indexOf, indexOf + 6), "mnc000");
                }
            } else {
                str = "xcap." + impi.substring(impi.indexOf(64) + 1);
            }
            String lowerCase = str.toLowerCase(Locale.US);
            return lowerCase.contains("3gppnetwork.org") ? lowerCase.replace("3gppnetwork.org", "pub.3gppnetwork.org") : lowerCase;
        }
        if (simOperator != null && simOperator.length() >= 5) {
            try {
                String substring = simOperator.substring(0, 3);
                String substring2 = simOperator.substring(3);
                if (simMno == Mno.CMCC) {
                    substring2 = NSDSNamespaces.NSDSMigration.DEFAULT_KEY;
                } else if (simMno == Mno.CTC) {
                    substring = "460";
                    substring2 = "011";
                } else if (simMno == Mno.CTCMO) {
                    substring = "455";
                    substring2 = "007";
                }
                return "xcap.ims.mnc" + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(Integer.parseInt(substring2))}) + ".mcc" + substring + XCAP_DOMAIN_NAME;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return string;
    }

    public static String getBSFDomain(int i) {
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.SS.BSF_IP, "");
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            return string;
        }
        String simOperator = simManagerFromSimSlot.getSimOperator();
        if (simManagerFromSimSlot.hasNoSim() || !TextUtils.isEmpty(string)) {
            return buildDomain(string, simOperator);
        }
        if (simManagerFromSimSlot.hasIsim()) {
            String impi = simManagerFromSimSlot.getImpi();
            if (impi == null) {
                Log.e(LOG_TAG, "NULL IMPI received from SIM :: Returning DEFAULT BSFIP !!");
                return string;
            }
            int indexOf = impi.indexOf(64);
            if (indexOf <= 0 || indexOf == impi.length()) {
                return string;
            }
            String trim = impi.substring(indexOf + 1).trim();
            if (trim.endsWith(DOMAIN_NAME)) {
                int indexOf2 = trim.indexOf(DOMAIN_NAME);
                if (indexOf2 <= 0) {
                    return string;
                }
                String substring = trim.substring(0, indexOf2);
                return "bsf." + substring + XCAP_DOMAIN_NAME;
            }
            return "bsf." + trim;
        }
        if (simOperator != null && simOperator.length() >= 5) {
            try {
                String substring2 = simOperator.substring(0, 3);
                String substring3 = simOperator.substring(3);
                return "bsf.mnc" + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(Integer.parseInt(substring3))}) + ".mcc" + substring2 + XCAP_DOMAIN_NAME;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return string;
    }

    public static String generate3GPPDomain(ISimManager iSimManager) {
        if (iSimManager == null) {
            return null;
        }
        String simOperator = iSimManager.getSimOperator();
        if (simOperator == null || simOperator.length() < 5) {
            Log.e(LOG_TAG, "Invalid operator.");
            return null;
        }
        try {
            String substring = simOperator.substring(0, 3);
            String substring2 = simOperator.substring(3);
            return "ims.mnc" + String.format(Locale.US, "%03d", new Object[]{Integer.valueOf(Integer.parseInt(substring2))}) + ".mcc" + substring + DOMAIN_NAME;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNumberFromURI(String str) {
        String str2;
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        String replaceAll = PATTERN_WHITE_SPACES.matcher(str).replaceAll("");
        Pattern pattern = PATTERN_TEL_NUMBER;
        if (pattern.matcher(replaceAll).matches()) {
            return replaceAll;
        }
        ImsUri parse = ImsUri.parse(replaceAll);
        if (parse != null) {
            str2 = parse.getMsisdn();
        } else {
            str2 = "";
        }
        if (str2 == null) {
            str2 = "";
        }
        if (!pattern.matcher(str2).matches()) {
            return "";
        }
        return str2;
    }

    public static String makeInternationalNumber(String str, Mno mno) {
        int countryCodeForRegion = PhoneNumberUtil.getInstance().getCountryCodeForRegion(mno.getCountryCode());
        if (countryCodeForRegion == 0) {
            Log.i(LOG_TAG, "Invalid Country Code. Country Code : " + mno.getCountryCode());
            return str;
        }
        String str2 = "+" + countryCodeForRegion;
        if (str.charAt(0) != '0') {
            return str2 + str;
        }
        return str2 + str.substring(1);
    }

    public static String removeUriPlusPrefix(String str, String str2) {
        return (str == null || str.length() <= str2.length() || !str.startsWith(str2)) ? str : str.replace(str2, "0");
    }

    public static String getAcceptEncoding(int i) {
        return SimUtil.getSimMno(i).isOneOf(Mno.H3G, Mno.SMARTFREN, Mno.TMOUS, Mno.DISH, Mno.TELE2_RUSSIA) ? "" : "*";
    }

    public static String cleanBarringNum(String str) {
        if (str.toLowerCase().contains("hidden")) {
            return str;
        }
        return str.replaceAll(CmcConstants.E_NUM_SLOT_SPLIT, "");
    }

    public static boolean isBsfDisableTls(int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot != null && simManagerFromSimSlot.getSimMno().isOneOf(Mno.AIS)) {
            return true;
        }
        return false;
    }

    public static String getDomain(String str) {
        int indexOf;
        if (str == null || (indexOf = str.indexOf("@")) <= 0) {
            return null;
        }
        return str.substring(indexOf + 1);
    }

    public static boolean isPutRequest(int i) {
        return i % 2 != 0;
    }

    public static String makeXcapUserAgentHeader(String str, int i) {
        String str2;
        String str3;
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        String str4 = SemSystemProperties.get("ro.build.PDA");
        if (str4 != null && str4.length() > 3) {
            str = str.replace("[BUILD_VERSION]", str4.substring(str4.length() - 3));
        }
        if (str4 != null && str4.length() > 8) {
            str = str.replace("[BUILD_VERSION_8_LETTER]", str4.substring(str4.length() - 8));
        }
        String str5 = Build.MODEL;
        if (NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equals(str5)) {
            str2 = str.replace("[PRODUCT_MODEL]", SemSystemProperties.get("ro.product.base_model"));
        } else {
            str2 = str.replace("[PRODUCT_MODEL]", str5);
        }
        if (DeviceUtil.isTablet()) {
            str3 = str2.replace("[PRODUCT_TYPE]", "device-type/tablet");
        } else {
            str3 = str2.replace("[PRODUCT_TYPE]", "device-type/smart-phone");
        }
        String replace = str3.replace("[OMCCODE]", OmcCode.getUserAgentNWCode(i, SimUtil.getSimMno(i)));
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        return (simManagerFromSimSlot == null || !simManagerFromSimSlot.isSimAvailable()) ? replace : replace.replace("[MCC_MNC]", simManagerFromSimSlot.getSimOperator());
    }

    protected static String[] getSetting(int i, String str, String[] strArr) {
        return ImsRegistry.getStringArray(i, str, strArr);
    }

    protected static boolean getSetting(int i, String str, boolean z) {
        return ImsRegistry.getBoolean(i, str, z);
    }

    protected static int getSetting(int i, String str, int i2) {
        return ImsRegistry.getInt(i, str, i2);
    }

    protected static String getSetting(int i, String str, String str2) {
        return ImsRegistry.getString(i, str, str2);
    }
}
