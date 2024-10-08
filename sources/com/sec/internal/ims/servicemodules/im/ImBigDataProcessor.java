package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.ims.util.ChatbotUriUtil;

public class ImBigDataProcessor extends RcsBigDataProcessor {
    private ImModule mImModule;

    ImBigDataProcessor(Context context, ImModule imModule) {
        super(context);
        this.mImModule = imModule;
    }

    /* access modifiers changed from: protected */
    public ImSession getImSession(String str) {
        return this.mImModule.getImSession(str);
    }

    /* access modifiers changed from: protected */
    public boolean isChatBot(int i, ImSession imSession) {
        return !imSession.isGroupChat() && ChatbotUriUtil.hasChatbotUri(imSession.getParticipantsUri(), i);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImBigDataProcessor$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type = r0
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.MULTIMEDIA     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.LOCATION     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImBigDataProcessor.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public String getMessageType(MessageBase messageBase, boolean z) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$Type[messageBase.getType().ordinal()];
        String str = i != 1 ? i != 2 ? messageBase.getIsSlmSvcMsg() ? DiagnosisConstants.RCSM_MTYP_SLM : "IM" : DiagnosisConstants.RCSM_MTYP_GLS : "FT";
        if (!z) {
            return str;
        }
        return str + DiagnosisConstants.RCSM_MTYP_CHATBOT_POSTFIX;
    }

    /* access modifiers changed from: protected */
    public ImsRegistration getImsRegistration(int i) {
        return this.mImModule.getImsRegistration(i);
    }

    /* access modifiers changed from: protected */
    public boolean isWifiConnected() {
        return this.mImModule.isWifiConnected();
    }

    /* access modifiers changed from: protected */
    public String getMessageTypeForILA(String str) {
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -959159266:
                if (str.equals("GLS_CHATBOT")) {
                    c = 0;
                    break;
                }
                break;
            case 2254:
                if (str.equals("FT")) {
                    c = 1;
                    break;
                }
                break;
            case 70670:
                if (str.equals(DiagnosisConstants.RCSM_MTYP_GLS)) {
                    c = 2;
                    break;
                }
                break;
            case 82196:
                if (str.equals(DiagnosisConstants.RCSM_MTYP_SLM)) {
                    c = 3;
                    break;
                }
                break;
            case 619701044:
                if (str.equals("IM_CHATBOT")) {
                    c = 4;
                    break;
                }
                break;
            case 1207053092:
                if (str.equals("SLM_CHATBOT")) {
                    c = 5;
                    break;
                }
                break;
            case 1441962206:
                if (str.equals("FT_CHATBOT")) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return DiagnosisConstants.DRCS_KEY_MAAP_GLS_MO_SUCCESS;
            case 1:
                return DiagnosisConstants.DRCS_KEY_RCS_FT_MO_SUCCESS;
            case 2:
                return DiagnosisConstants.DRCS_KEY_RCS_GLS_MO_SUCCESS;
            case 3:
                return DiagnosisConstants.DRCS_KEY_RCS_SLM_MO_SUCCESS;
            case 4:
                return DiagnosisConstants.DRCS_KEY_MAAP_IM_MO_SUCCESS;
            case 5:
                return DiagnosisConstants.DRCS_KEY_MAAP_SLM_MO_SUCCESS;
            case 6:
                return DiagnosisConstants.DRCS_KEY_MAAP_FT_MO_SUCCESS;
            default:
                return DiagnosisConstants.DRCS_KEY_RCS_IM_MO_SUCCESS;
        }
    }
}
