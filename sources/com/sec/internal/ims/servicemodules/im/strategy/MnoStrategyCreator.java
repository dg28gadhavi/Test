package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;

public class MnoStrategyCreator {
    private static final String LOG_TAG = "MnoStrategyCreator";

    public static RcsPolicySettings.RcsPolicyType getPolicyType(Mno mno, int i, Context context) {
        RcsPolicySettings.RcsPolicyType rcsPolicyType;
        String acsServerType = ConfigUtil.getAcsServerType(i);
        String rcsProfileLoaderInternalWithFeature = ConfigUtil.getRcsProfileLoaderInternalWithFeature(context, mno.getName(), i);
        RcsPolicySettings.RcsPolicyType rcsPolicyType2 = RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
        if (!TextUtils.isEmpty(acsServerType)) {
            rcsPolicyType = getPolicyTypeByRcsAs(acsServerType, mno);
        } else {
            rcsPolicyType = !TextUtils.isEmpty(rcsProfileLoaderInternalWithFeature) ? getPolicyTypeByRcsProfile(rcsProfileLoaderInternalWithFeature, mno) : rcsPolicyType2;
        }
        if (rcsPolicyType == rcsPolicyType2) {
            rcsPolicyType = getPolicyTypeByMno(mno);
        }
        Log.i(LOG_TAG, "getPolicyType: phone" + i + " " + mno + " => " + rcsPolicyType);
        return rcsPolicyType;
    }

    private static RcsPolicySettings.RcsPolicyType getPolicyTypeByRcsAs(String str, Mno mno) {
        if (ImsConstants.RCS_AS.JIBE.equals(str)) {
            if (mno == Mno.ORANGE_ROMANIA || mno == Mno.ORANGE_SLOVAKIA || mno == Mno.ORANGE_SPAIN || mno == Mno.ORANGE_BELGIUM) {
                return RcsPolicySettings.RcsPolicyType.ORANGE_UP;
            }
            if (mno == Mno.VODAFONE_INDIA || mno == Mno.IDEA_INDIA) {
                return RcsPolicySettings.RcsPolicyType.VODAFONE_IN_UP;
            }
            return RcsPolicySettings.RcsPolicyType.JIBE_UP;
        } else if (!ImsConstants.RCS_AS.SEC.equals(str)) {
            return RcsPolicySettings.RcsPolicyType.DEFAULT_UP;
        } else {
            if (mno == Mno.KT) {
                return RcsPolicySettings.RcsPolicyType.KT_UP;
            }
            return RcsPolicySettings.RcsPolicyType.SEC_UP;
        }
    }

    private static RcsPolicySettings.RcsPolicyType getPolicyTypeByRcsProfile(String str, Mno mno) {
        if (!str.startsWith("UP")) {
            return RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
        }
        if (mno == Mno.BELL) {
            return RcsPolicySettings.RcsPolicyType.BMC_UP;
        }
        if (mno == Mno.SPRINT) {
            return RcsPolicySettings.RcsPolicyType.SPR_UP;
        }
        if (mno == Mno.VZW) {
            return RcsPolicySettings.RcsPolicyType.VZW_UP;
        }
        if (mno.isVodafone()) {
            return RcsPolicySettings.RcsPolicyType.VODA_UP;
        }
        if (mno.isTmobile() || mno == Mno.TELEKOM_ALBANIA) {
            return RcsPolicySettings.RcsPolicyType.TMOBILE_UP;
        }
        if (mno == Mno.SWISSCOM) {
            return RcsPolicySettings.RcsPolicyType.SWISSCOM_UP;
        }
        if (mno == Mno.CMCC) {
            return RcsPolicySettings.RcsPolicyType.CMCC;
        }
        if (mno == Mno.CTC) {
            return RcsPolicySettings.RcsPolicyType.CTC;
        }
        if (mno == Mno.CU) {
            return RcsPolicySettings.RcsPolicyType.CU;
        }
        if (mno.isRjil()) {
            return RcsPolicySettings.RcsPolicyType.RJIL_UP;
        }
        return RcsPolicySettings.RcsPolicyType.DEFAULT_UP;
    }

    private static RcsPolicySettings.RcsPolicyType getPolicyTypeByMno(Mno mno) {
        if (mno.isTmobile() || mno == Mno.TELEKOM_ALBANIA) {
            return RcsPolicySettings.RcsPolicyType.TMOBILE;
        }
        if (mno.isOrange()) {
            return RcsPolicySettings.RcsPolicyType.ORANGE;
        }
        if (mno.isVodafone()) {
            return RcsPolicySettings.RcsPolicyType.VODA;
        }
        if (mno == Mno.ATT) {
            return RcsPolicySettings.RcsPolicyType.ATT;
        }
        if (mno == Mno.TMOUS) {
            return RcsPolicySettings.RcsPolicyType.TMOUS;
        }
        if (mno == Mno.SPRINT) {
            return RcsPolicySettings.RcsPolicyType.SPR;
        }
        if (mno == Mno.USCC) {
            return RcsPolicySettings.RcsPolicyType.USCC;
        }
        if (mno == Mno.VZW) {
            return RcsPolicySettings.RcsPolicyType.VZW;
        }
        if (mno == Mno.BELL) {
            return RcsPolicySettings.RcsPolicyType.BMC;
        }
        if (mno == Mno.CMCC) {
            return RcsPolicySettings.RcsPolicyType.CMCC;
        }
        if (mno == Mno.CTC) {
            return RcsPolicySettings.RcsPolicyType.CTC;
        }
        if (mno == Mno.CU) {
            return RcsPolicySettings.RcsPolicyType.CU;
        }
        if (mno == Mno.SINGTEL) {
            return RcsPolicySettings.RcsPolicyType.SINGTEL;
        }
        if (mno == Mno.TCE) {
            return RcsPolicySettings.RcsPolicyType.TCE;
        }
        if (mno == Mno.TELSTRA) {
            return RcsPolicySettings.RcsPolicyType.TELSTRA;
        }
        if (mno.isOneOf(Mno.TELENOR_NORWAY, Mno.TELENOR_SWE)) {
            return RcsPolicySettings.RcsPolicyType.TELENOR;
        }
        if (mno.isOneOf(Mno.TELIA_NORWAY, Mno.TELIA_SWE)) {
            return RcsPolicySettings.RcsPolicyType.TELIA;
        }
        if (mno == Mno.RJIL) {
            return RcsPolicySettings.RcsPolicyType.RJIL;
        }
        return RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
    }

    public static IMnoStrategy makeInstance(Mno mno, int i, Context context) {
        IMnoStrategy iMnoStrategy;
        RcsPolicySettings.RcsPolicyType policyType = getPolicyType(mno, i, context);
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType[policyType.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
                iMnoStrategy = new DefaultRCSMnoStrategy(context, i);
                break;
            case 5:
                iMnoStrategy = new UsccStrategy(context, i);
                break;
            case 6:
            case 7:
            case 8:
                iMnoStrategy = new ChnStrategy(context, i);
                break;
            case 9:
                iMnoStrategy = new RjilStrategy(context, i);
                break;
            case 10:
                iMnoStrategy = new RjilUPStrategy(context, i);
                break;
            case 11:
                iMnoStrategy = new DTStrategy(context, i);
                break;
            case 12:
            case 13:
            case 14:
                iMnoStrategy = new EmeiaStrategy(context, i);
                break;
            case 15:
                iMnoStrategy = new AttStrategy(context, i);
                break;
            case 16:
                iMnoStrategy = new TmoStrategy(context, i);
                break;
            case 17:
                iMnoStrategy = new VzwStrategy(context, i);
                break;
            case 18:
                iMnoStrategy = new SprStrategy(context, i);
                break;
            case 19:
                iMnoStrategy = new BmcStrategy(context, i);
                break;
            case 20:
                iMnoStrategy = new BmcUPStrategy(context, i);
                break;
            case 21:
                iMnoStrategy = new TceStrategy(context, i);
                break;
            case 22:
            case 23:
                iMnoStrategy = new DefaultUPMnoStrategy(context, i);
                break;
            case 24:
                iMnoStrategy = new VzwUPStrategy(context, i);
                break;
            case 25:
                iMnoStrategy = new VodaUPStrategy(context, i);
                break;
            case 26:
            case 27:
            case 28:
            case 29:
                iMnoStrategy = new JibeUPStrategy(context, i);
                break;
            case 30:
                iMnoStrategy = new SecUPStrategy(context, i);
                break;
            case 31:
                iMnoStrategy = new KtUPStrategy(context, i);
                break;
            case 32:
                iMnoStrategy = new SwisscomUPStrategy(context, i);
                break;
            default:
                policyType = RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;
                iMnoStrategy = new DefaultRCSMnoStrategy(context, i);
                break;
        }
        iMnoStrategy.setPolicyType(policyType);
        return iMnoStrategy;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.MnoStrategyCreator$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType;

        /* JADX WARNING: Can't wrap try/catch for region: R(64:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|(3:63|64|66)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(66:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|66) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00b4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x00c0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00cc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00d8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x00e4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00f0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00fc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x0108 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x0114 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x0120 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:51:0x012c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:53:0x0138 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:55:0x0144 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:57:0x0150 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:59:0x015c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:61:0x0168 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:63:0x0174 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType[] r0 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType = r0
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.DEFAULT_RCS     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.TELSTRA     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.ORANGE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.SINGTEL     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.USCC     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.CMCC     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.CTC     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.CU     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.RJIL     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.RJIL_UP     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.TMOBILE     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.VODA     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.TELENOR     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.TELIA     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.ATT     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.TMOUS     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.VZW     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.SPR     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.BMC     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00f0 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.BMC_UP     // Catch:{ NoSuchFieldError -> 0x00f0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00f0 }
                r2 = 20
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00f0 }
            L_0x00f0:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x00fc }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.TCE     // Catch:{ NoSuchFieldError -> 0x00fc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00fc }
                r2 = 21
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00fc }
            L_0x00fc:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0108 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.DEFAULT_UP     // Catch:{ NoSuchFieldError -> 0x0108 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0108 }
                r2 = 22
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0108 }
            L_0x0108:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0114 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.TMOBILE_UP     // Catch:{ NoSuchFieldError -> 0x0114 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0114 }
                r2 = 23
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0114 }
            L_0x0114:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0120 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.VZW_UP     // Catch:{ NoSuchFieldError -> 0x0120 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0120 }
                r2 = 24
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0120 }
            L_0x0120:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x012c }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.VODA_UP     // Catch:{ NoSuchFieldError -> 0x012c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x012c }
                r2 = 25
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x012c }
            L_0x012c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0138 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.JIBE_UP     // Catch:{ NoSuchFieldError -> 0x0138 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0138 }
                r2 = 26
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0138 }
            L_0x0138:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0144 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.ORANGE_UP     // Catch:{ NoSuchFieldError -> 0x0144 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0144 }
                r2 = 27
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0144 }
            L_0x0144:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0150 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.VODAFONE_IN_UP     // Catch:{ NoSuchFieldError -> 0x0150 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0150 }
                r2 = 28
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0150 }
            L_0x0150:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x015c }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.SPR_UP     // Catch:{ NoSuchFieldError -> 0x015c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x015c }
                r2 = 29
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x015c }
            L_0x015c:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0168 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.SEC_UP     // Catch:{ NoSuchFieldError -> 0x0168 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0168 }
                r2 = 30
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0168 }
            L_0x0168:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0174 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.KT_UP     // Catch:{ NoSuchFieldError -> 0x0174 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0174 }
                r2 = 31
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0174 }
            L_0x0174:
                int[] r0 = $SwitchMap$com$sec$internal$ims$settings$RcsPolicySettings$RcsPolicyType     // Catch:{ NoSuchFieldError -> 0x0180 }
                com.sec.internal.ims.settings.RcsPolicySettings$RcsPolicyType r1 = com.sec.internal.ims.settings.RcsPolicySettings.RcsPolicyType.SWISSCOM_UP     // Catch:{ NoSuchFieldError -> 0x0180 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0180 }
                r2 = 32
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0180 }
            L_0x0180:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.MnoStrategyCreator.AnonymousClass1.<clinit>():void");
        }
    }
}
