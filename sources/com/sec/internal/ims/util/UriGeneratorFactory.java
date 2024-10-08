package com.sec.internal.ims.util;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class UriGeneratorFactory {
    private static final String LOG_TAG = "UriGeneratorFactory";
    static volatile UriGeneratorFactory sUriFactory;
    private ImsUri DEFAULT_URI = ImsUri.parse("sip:default@default");
    private Map<UriGenerator.URIServiceType, ImsUri[]> mPrimaryImpuMap = new ConcurrentHashMap();
    private ITelephonyManager mTelephonyManager;
    private Map<UriGenerator.URIServiceType, Map<ImsUri, UriGenerator>> mUriGenerators = new ConcurrentHashMap();

    public static UriGeneratorFactory getInstance() {
        if (sUriFactory == null) {
            Context context = ImsRegistry.getContext();
            synchronized (UriGeneratorFactory.class) {
                if (sUriFactory == null) {
                    sUriFactory = new UriGeneratorFactory(context);
                }
            }
        }
        return sUriFactory;
    }

    public UriGeneratorFactory(Context context) {
        ITelephonyManager instance = TelephonyManagerWrapper.getInstance(context);
        this.mTelephonyManager = instance;
        int phoneCount = instance.getPhoneCount();
        for (UriGenerator.URIServiceType uRIServiceType : UriGenerator.URIServiceType.values()) {
            ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
            concurrentHashMap.put(this.DEFAULT_URI, new UriGeneratorImpl(ImsUri.UriType.SIP_URI, "us", "example.com", this.mTelephonyManager, SubscriptionManager.getActiveDataSubscriptionId(), SimUtil.getActiveDataPhoneId()));
            this.mUriGenerators.put(uRIServiceType, concurrentHashMap);
            ImsUri[] imsUriArr = new ImsUri[phoneCount];
            Arrays.fill(imsUriArr, (Object) null);
            this.mPrimaryImpuMap.put(uRIServiceType, imsUriArr);
        }
    }

    /* JADX WARNING: type inference failed for: r9v4 */
    /* JADX WARNING: type inference failed for: r0v25, types: [com.sec.internal.ims.util.UriGeneratorImpl] */
    /* JADX WARNING: type inference failed for: r0v26, types: [com.sec.internal.ims.util.UriGeneratorRjil] */
    /* JADX WARNING: type inference failed for: r0v27, types: [com.sec.internal.ims.util.UriGeneratorDT] */
    /* JADX WARNING: type inference failed for: r0v28, types: [com.sec.internal.ims.util.UriGeneratorChn] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.util.UriGenerator create(com.sec.ims.ImsRegistration r12, com.sec.ims.util.ImsUri.UriType r13) {
        /*
            r11 = this;
            com.sec.ims.settings.ImsProfile r8 = r12.getImsProfile()
            java.lang.String r0 = r8.getMnoName()
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.fromName(r0)
            if (r9 == 0) goto L_0x0013
            java.lang.String r0 = r9.getCountryCode()
            goto L_0x0015
        L_0x0013:
            java.lang.String r0 = ""
        L_0x0015:
            r2 = r0
            int r6 = android.telephony.SubscriptionManager.getActiveDataSubscriptionId()
            if (r9 == 0) goto L_0x0035
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.VZW
            if (r9 != r0) goto L_0x0035
            com.sec.internal.ims.util.VzwUriGenerator r7 = new com.sec.internal.ims.util.VzwUriGenerator
            java.lang.String r2 = r12.getDomain()
            com.sec.internal.helper.os.ITelephonyManager r3 = r11.mTelephonyManager
            int r5 = r12.getPhoneId()
            r0 = r7
            r1 = r13
            r4 = r6
            r6 = r8
            r0.<init>(r1, r2, r3, r4, r5, r6)
            goto L_0x0102
        L_0x0035:
            java.lang.String r0 = "us"
            boolean r0 = r0.equalsIgnoreCase(r2)
            if (r0 == 0) goto L_0x0053
            com.sec.internal.ims.util.UriGeneratorUs r7 = new com.sec.internal.ims.util.UriGeneratorUs
            java.lang.String r2 = r12.getDomain()
            com.sec.internal.helper.os.ITelephonyManager r3 = r11.mTelephonyManager
            int r5 = r12.getPhoneId()
            r0 = r7
            r1 = r13
            r4 = r6
            r6 = r8
            r0.<init>(r1, r2, r3, r4, r5, r6)
            goto L_0x0102
        L_0x0053:
            java.lang.String r0 = "cn"
            boolean r0 = r0.equalsIgnoreCase(r2)
            if (r0 == 0) goto L_0x0072
            com.sec.internal.ims.util.UriGeneratorChn r9 = new com.sec.internal.ims.util.UriGeneratorChn
            java.lang.String r3 = r12.getDomain()
            com.sec.internal.helper.os.ITelephonyManager r4 = r11.mTelephonyManager
            int r11 = r12.getPhoneId()
            r0 = r9
            r1 = r13
            r5 = r6
            r6 = r11
            r7 = r8
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
        L_0x006f:
            r7 = r9
            goto L_0x0102
        L_0x0072:
            if (r9 == 0) goto L_0x00aa
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.TMOBILE
            if (r9 == r0) goto L_0x0084
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.EE
            if (r9 == r0) goto L_0x0084
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.EE_ESN
            if (r9 == r0) goto L_0x0084
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.TMOBILE_PL
            if (r9 != r0) goto L_0x00aa
        L_0x0084:
            com.sec.internal.ims.util.UriGeneratorDT r9 = new com.sec.internal.ims.util.UriGeneratorDT
            java.lang.String r3 = r12.getDomain()
            com.sec.ims.settings.ImsProfile r0 = r12.getImsProfile()
            java.lang.String r0 = r0.getMcc()
            com.sec.ims.settings.ImsProfile r1 = r12.getImsProfile()
            java.lang.String r1 = r1.getMnc()
            java.lang.String r4 = r11.getDerivedDomainFromImsi(r0, r1)
            com.sec.internal.helper.os.ITelephonyManager r5 = r11.mTelephonyManager
            int r7 = r12.getPhoneId()
            r0 = r9
            r1 = r13
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8)
            goto L_0x006f
        L_0x00aa:
            if (r9 == 0) goto L_0x00d1
            java.lang.String r0 = "kr"
            boolean r0 = r0.equalsIgnoreCase(r2)
            if (r0 == 0) goto L_0x00d1
            com.sec.internal.ims.util.UriGeneratorKr r10 = new com.sec.internal.ims.util.UriGeneratorKr
            java.lang.String r3 = r12.getDomain()
            com.sec.internal.helper.os.ITelephonyManager r4 = r11.mTelephonyManager
            int r11 = r12.getPhoneId()
            r0 = r10
            r1 = r13
            r5 = r6
            r6 = r11
            r7 = r8
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            java.lang.String r11 = r9.getName()
            r10.setMnoName(r11)
            r7 = r10
            goto L_0x0102
        L_0x00d1:
            if (r9 == 0) goto L_0x00ec
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.RJIL
            if (r9 != r0) goto L_0x00ec
            com.sec.internal.ims.util.UriGeneratorRjil r9 = new com.sec.internal.ims.util.UriGeneratorRjil
            java.lang.String r3 = r12.getDomain()
            com.sec.internal.helper.os.ITelephonyManager r4 = r11.mTelephonyManager
            int r11 = r12.getPhoneId()
            r0 = r9
            r1 = r13
            r5 = r6
            r6 = r11
            r7 = r8
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x006f
        L_0x00ec:
            com.sec.internal.ims.util.UriGeneratorImpl r9 = new com.sec.internal.ims.util.UriGeneratorImpl
            java.lang.String r3 = r12.getDomain()
            com.sec.internal.helper.os.ITelephonyManager r4 = r11.mTelephonyManager
            int r11 = r12.getPhoneId()
            r0 = r9
            r1 = r13
            r5 = r6
            r6 = r11
            r7 = r8
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            goto L_0x006f
        L_0x0102:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.UriGeneratorFactory.create(com.sec.ims.ImsRegistration, com.sec.ims.util.ImsUri$UriType):com.sec.internal.ims.util.UriGenerator");
    }

    private String getDerivedDomainFromImsi(String str, String str2) {
        Log.d(LOG_TAG, "getImsiBasedDomain: mcc=" + str + " mnc=" + str2);
        return (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) ? "" : String.format(Locale.US, "ims.mnc%03d.mcc%03d.3gppnetwork.org", new Object[]{Integer.valueOf(Integer.parseInt(str2)), Integer.valueOf(Integer.parseInt(str))});
    }

    public void add(ImsUri imsUri, UriGenerator uriGenerator, UriGenerator.URIServiceType uRIServiceType) {
        if (imsUri != null) {
            int phoneId = uriGenerator.getPhoneId();
            ImsUri[] imsUriArr = this.mPrimaryImpuMap.get(uRIServiceType);
            Objects.requireNonNull(imsUriArr);
            if (imsUriArr[phoneId] == null) {
                this.mPrimaryImpuMap.get(uRIServiceType)[phoneId] = imsUri;
            }
            this.mUriGenerators.get(uRIServiceType).put(imsUri, uriGenerator);
        }
    }

    public void removeByPhoneId(int i, UriGenerator.URIServiceType uRIServiceType) {
        ImsUri[] imsUriArr = this.mPrimaryImpuMap.get(uRIServiceType);
        Objects.requireNonNull(imsUriArr);
        imsUriArr[i] = null;
        for (ImsUri imsUri : this.mUriGenerators.get(uRIServiceType).keySet()) {
            if (!this.DEFAULT_URI.equals(imsUri) && ((UriGenerator) this.mUriGenerators.get(uRIServiceType).get(imsUri)).getPhoneId() == i) {
                this.mUriGenerators.get(uRIServiceType).remove(imsUri);
            }
        }
    }

    public boolean contains(ImsUri imsUri, UriGenerator.URIServiceType uRIServiceType) {
        return this.mUriGenerators.get(uRIServiceType).containsKey(imsUri);
    }

    public UriGenerator get(UriGenerator.URIServiceType uRIServiceType) {
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        ImsUri[] imsUriArr = this.mPrimaryImpuMap.get(uRIServiceType);
        Objects.requireNonNull(imsUriArr);
        UriGenerator uriGenerator = imsUriArr[activeDataPhoneId] != null ? (UriGenerator) this.mUriGenerators.get(uRIServiceType).get(this.mPrimaryImpuMap.get(uRIServiceType)[activeDataPhoneId]) : null;
        return uriGenerator == null ? (UriGenerator) this.mUriGenerators.get(uRIServiceType).get(this.DEFAULT_URI) : uriGenerator;
    }

    public UriGenerator get(int i, UriGenerator.URIServiceType uRIServiceType) {
        ImsUri[] imsUriArr = this.mPrimaryImpuMap.get(uRIServiceType);
        Objects.requireNonNull(imsUriArr);
        UriGenerator uriGenerator = imsUriArr[i] != null ? (UriGenerator) this.mUriGenerators.get(uRIServiceType).get(this.mPrimaryImpuMap.get(uRIServiceType)[i]) : null;
        return uriGenerator == null ? (UriGenerator) this.mUriGenerators.get(uRIServiceType).get(this.DEFAULT_URI) : uriGenerator;
    }

    public UriGenerator get(ImsUri imsUri, UriGenerator.URIServiceType uRIServiceType) {
        if (imsUri == null) {
            return get(uRIServiceType);
        }
        UriGenerator uriGenerator = (UriGenerator) this.mUriGenerators.get(uRIServiceType).get(imsUri);
        if (uriGenerator != null) {
            return uriGenerator;
        }
        Log.d(LOG_TAG, "get: UriGenerator not found for uri=" + IMSLog.checker(imsUri) + ". use default.");
        return get(uRIServiceType);
    }

    public void updateUriGenerator(ImsRegistration imsRegistration, ImsUri.UriType uriType) {
        UriGenerator uriGenerator;
        for (NameAddr uri : imsRegistration.getImpuList()) {
            boolean hasVolteService = imsRegistration.hasVolteService();
            boolean hasRcsService = imsRegistration.hasRcsService();
            ImsUri uri2 = uri.getUri();
            for (UriGenerator.URIServiceType uRIServiceType : UriGenerator.URIServiceType.values()) {
                if ((hasVolteService || uRIServiceType != UriGenerator.URIServiceType.VOLTE_URI) && (hasRcsService || uRIServiceType != UriGenerator.URIServiceType.RCS_URI)) {
                    if (!contains(uri2, uRIServiceType)) {
                        uriGenerator = create(imsRegistration, uriType);
                        uriGenerator.extractOwnAreaCode(imsRegistration.getPreferredImpu().getUri().getMsisdn());
                        uriGenerator.updateRat(imsRegistration.getCurrentRat());
                    } else {
                        uriGenerator = get(uri2, uRIServiceType);
                    }
                    if (hasVolteService) {
                        uriGenerator.updateNetworkPreferredUriType(UriGenerator.URIServiceType.VOLTE_URI, uriType);
                    }
                    if (hasRcsService) {
                        uriGenerator.updateNetworkPreferredUriType(UriGenerator.URIServiceType.RCS_URI, uriType);
                    }
                    add(uri2, uriGenerator, uRIServiceType);
                }
            }
        }
    }
}
