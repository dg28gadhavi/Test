package com.sec.internal.ims.servicemodules.im.util;

import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.MaapNamespace;
import com.sec.internal.constants.ims.servicemodules.im.RcsNamespace;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;

public class ImCpimNamespacesHelper {
    public static ImDirection extractImDirection(int i, ImCpimNamespaces imCpimNamespaces) {
        ImDirection imDirection = ImDirection.INCOMING;
        if (imCpimNamespaces == null) {
            return imDirection;
        }
        String firstHeaderValue = imCpimNamespaces.getFirstHeaderValue("MD", "direction");
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        return rcsStrategy != null ? rcsStrategy.convertToImDirection(firstHeaderValue) : imDirection;
    }

    public static String extractMaapTrafficType(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(MaapNamespace.NAME, "Traffic-Type");
        }
        return null;
    }

    public static String extractRcsReferenceId(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, RcsNamespace.REFERENCE_ID_KEY);
        }
        return null;
    }

    public static String extractRcsReferenceType(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, RcsNamespace.REFERENCE_TYPE_KEY);
        }
        return null;
    }

    public static String extractRcsReferenceValue(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, RcsNamespace.REFERENCE_VALUE_KEY);
        }
        return null;
    }

    public static String extractRcsTrafficType(ImCpimNamespaces imCpimNamespaces) {
        if (imCpimNamespaces != null) {
            return imCpimNamespaces.getFirstHeaderValue(RcsNamespace.KOR.NAME, "Traffic-Type");
        }
        return null;
    }
}
