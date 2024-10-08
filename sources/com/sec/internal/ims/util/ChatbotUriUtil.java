package com.sec.internal.ims.util;

import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import java.util.Collections;

public class ChatbotUriUtil {
    private static final String LOG_TAG = "ChatbotUriUtil";

    private ChatbotUriUtil() {
    }

    public static boolean isChatbotUri(ImsUri imsUri, int i) {
        return hasChatbotUri(Collections.singleton(imsUri), i);
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x000e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean hasChatbotUri(java.util.Collection<com.sec.ims.util.ImsUri> r3, int r4) {
        /*
            r0 = 0
            if (r3 != 0) goto L_0x0004
            return r0
        L_0x0004:
            java.util.Iterator r3 = r3.iterator()
        L_0x0008:
            boolean r1 = r3.hasNext()
            if (r1 == 0) goto L_0x002e
            java.lang.Object r1 = r3.next()
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1
            boolean r2 = hasUriBotPlatform(r1, r4)
            if (r2 != 0) goto L_0x002c
            boolean r2 = hasChatbotRoleSession(r1, r4)
            if (r2 != 0) goto L_0x002c
            boolean r2 = isKnownBotServiceId(r1, r4)
            if (r2 != 0) goto L_0x002c
            boolean r1 = hasChatbotRoleCapability(r4, r1)
            if (r1 == 0) goto L_0x0008
        L_0x002c:
            r3 = 1
            return r3
        L_0x002e:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.ChatbotUriUtil.hasChatbotUri(java.util.Collection, int):boolean");
    }

    public static boolean hasUriBotPlatform(ImsUri imsUri, int i) {
        if (imsUri != null && imsUri.getUriType() == ImsUri.UriType.SIP_URI && !TextUtils.isEmpty(imsUri.getHost())) {
            String string = ImsRegistry.getString(i, GlobalSettingsConstants.RCS.BOT_SERVICE_ID_PREFIX_LIST, "");
            if (TextUtils.isEmpty(string)) {
                return false;
            }
            for (String contains : string.split(",")) {
                if (imsUri.getHost().contains(contains)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasChatbotRoleSession(ImsUri imsUri, int i) {
        return ImCache.getInstance().isChatbotRoleUri(imsUri, SimManagerFactory.getImsiFromPhoneId(i));
    }

    public static boolean isKnownBotServiceId(ImsUri imsUri, int i) {
        return imsUri != null && BotServiceIdTranslator.getInstance().contains(imsUri.toString(), i).booleanValue();
    }

    public static void updateChatbotCapability(int i, ImsUri imsUri, boolean z) {
        int i2 = i;
        Capabilities capabilities = null;
        ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsRegistry.isReady() ? ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule() : null;
        if (capabilityDiscoveryModule != null) {
            capabilities = capabilityDiscoveryModule.getCapabilitiesCache(i2).get(imsUri);
        }
        if (capabilities != null) {
            if (z) {
                capabilities.addFeature(Capabilities.FEATURE_CHATBOT_ROLE);
            } else {
                capabilities.removeFeature(Capabilities.FEATURE_CHATBOT_ROLE);
            }
            String str = LOG_TAG;
            Log.i(str, "addChatbotCapability : capabilities" + capabilities);
            capabilityDiscoveryModule.getCapabilitiesCache(i2).update(capabilities.getUri(), capabilities.getFeature(), capabilities.getAvailableFeatures(), capabilities.getPidf(), capabilities.getLastSeen(), capabilities.getTimestamp(), capabilities.getPAssertedId(), capabilities.getExtFeatureAsJoinedString());
        }
    }

    private static boolean hasChatbotRoleCapability(int i, ImsUri imsUri) {
        Capabilities capabilities = null;
        ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsRegistry.isReady() ? ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule() : null;
        if (capabilityDiscoveryModule != null) {
            capabilities = capabilityDiscoveryModule.getCapabilities(imsUri, CapabilityRefreshType.DISABLED, i);
        }
        if (capabilities == null) {
            return false;
        }
        return RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.USE_AVAILABLE_FEATURES_FOR_CHATBOT) ? capabilities.isFeatureAvailable(Capabilities.FEATURE_CHATBOT_ROLE) : capabilities.hasFeature(Capabilities.FEATURE_CHATBOT_ROLE);
    }
}
