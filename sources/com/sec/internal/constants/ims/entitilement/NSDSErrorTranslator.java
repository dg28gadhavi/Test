package com.sec.internal.constants.ims.entitilement;

import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.util.HashMap;
import java.util.Map;

public class NSDSErrorTranslator {
    private static final int GET_MSISDN = 7;
    private static final int GET_TOKEN = 8;
    private static final String LOG_TAG = "NSDSErrorTranslator";
    private static final int MANAGE_CONNECTIVITY = 4;
    private static final int MANAGE_LOC_AND_TC = 5;
    private static final int MANAGE_PUSH_TOKEN = 6;
    private static final int MANAGE_SERVICE = 3;
    private static final int REGISTERED_DEVICES = 9;
    private static final int REGISTERED_MSISDN = 2;
    private static final int REQ_3GPP_AUTH = 1;
    private static final int SERVICE_ENTITLEMENT_STATUS = 10;
    private static Map<String, Integer> mapNSDSMethodNames;

    static {
        HashMap hashMap = new HashMap();
        mapNSDSMethodNames = hashMap;
        hashMap.put(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH, 1);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_MSISDN, 2);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_SERVICE, 3);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 4);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, 5);
        mapNSDSMethodNames.put("managePushToken", 6);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN, 7);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.GET_TOKEN, 8);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.REGISTERED_DEVICES, 9);
        mapNSDSMethodNames.put(NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, 10);
    }

    public static int translate(String str, int i, int i2) {
        if (i2 == 1006) {
            return 1006;
        }
        int intValue = mapNSDSMethodNames.get(str).intValue();
        if (intValue == 9) {
            return RegisteredDevicesErrorTranslator.translate(i2);
        }
        switch (intValue) {
            case 1:
                return Request3gppAuthErrorTranslator.translate(i2);
            case 2:
                return RegisteredMsisdnErrorTranslator.translate(i2);
            case 3:
                return ManageServiceErrorTranslator.translate(i, i2);
            case 4:
                return ManageConnectivityErrorTranslator.translate(i, i2);
            case 5:
                return ManageLocationAndTCErrorTranslator.translate(i2);
            case 6:
                return ManagePushTokenErrorTranslator.translate(i2);
            default:
                String str2 = LOG_TAG;
                Log.d(str2, "could not translate nsds error code unsupported method name:" + str);
                return -1;
        }
    }

    static class Request3gppAuthErrorTranslator {
        Request3gppAuthErrorTranslator() {
        }

        public static int translate(int i) {
            if (i == 1004) {
                return 1001;
            }
            if (i == 1111) {
                return 1002;
            }
            Log.d("RegisteredMsisdnErrorTranslator", "could not translate nsds error code:" + i);
            return -1;
        }
    }

    static class RegisteredMsisdnErrorTranslator {
        RegisteredMsisdnErrorTranslator() {
        }

        public static int translate(int i) {
            if (i == 1004 || i == 1029 || i == 1061 || i == 1111) {
                return 1100;
            }
            Log.d("RegisteredMsisdnErrorTranslator", "could not translate nsds error code:" + i);
            return -1;
        }
    }

    static class RegisteredDevicesErrorTranslator {
        RegisteredDevicesErrorTranslator() {
        }

        public static int translate(int i) {
            if (i == 1004 || i == 1029 || i == 1111) {
                return 2000;
            }
            Log.d("RegisteredDevicesErrorTranslator", "could not translate nsds error code:" + i);
            return -1;
        }
    }

    static class ManageLocationAndTCErrorTranslator {
        ManageLocationAndTCErrorTranslator() {
        }

        public static int translate(int i) {
            if (i == 1004 || i == 1029 || i == 1041 || i == 1111) {
                return 1800;
            }
            Log.d("ManageLocationAndTCErrorTranslator", "could not translate nsds error code:" + i);
            return -1;
        }
    }

    static class ManagePushTokenErrorTranslator {
        ManagePushTokenErrorTranslator() {
        }

        public static int translate(int i) {
            if (i == 1004 || i == 1029 || i == 1046 || i == 1111) {
                return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_PUSH_TOKEN_GEN_FAILURE;
            }
            Log.d("ManagePushTokenErrorTranslator", "could not translate nsds error code:" + i);
            return -1;
        }
    }

    static class ManageConnectivityErrorTranslator {
        ManageConnectivityErrorTranslator() {
        }

        public static int translate(int i, int i2) {
            if (i == 0) {
                if (!(i2 == 1004 || i2 == 1022 || i2 == 1025)) {
                    if (i2 == 1054) {
                        return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_ACTIVATE_INVALID_DEVICE_GROUP;
                    }
                    if (i2 != 1111) {
                        Log.d("ManageConnectivityErrorTranslator", "could not translate nsds error code:" + i2);
                    }
                }
                return 1300;
            } else if (i != 3) {
                Log.d("ManageConnectivityErrorTranslator", "could not translate operation:" + i);
            } else if (i2 == 1004 || i2 == 1054 || i2 == 1111) {
                return 1400;
            } else {
                Log.d("ManageConnectivityErrorTranslator", "could not translate nsds error code:" + i2);
            }
            return -1;
        }
    }

    static class ManageServiceErrorTranslator {
        ManageServiceErrorTranslator() {
        }

        public static int translate(int i, int i2) {
            if (i != 0) {
                if (i != 1) {
                    if (i != 2) {
                        if (i != 5) {
                            if (i != 7) {
                                Log.d("ManageServiceErrorTranslator", "could not translate operation:" + i);
                            } else if (!(i2 == 1004 || i2 == 1024 || i2 == 1029 || i2 == 1053 || i2 == 1111)) {
                                Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + i2);
                            }
                        } else if (i2 == 1004 || i2 == 1024 || i2 == 1029 || i2 == 1053 || i2 == 1111) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_GEN_FAILURE;
                        } else {
                            Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + i2);
                        }
                    } else if (!(i2 == 1004 || i2 == 1024)) {
                        if (i2 == 1029) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_DEVICE_STATUS;
                        }
                        if (i2 == 1053) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_INVALID_SVC_INST_ID;
                        }
                        if (i2 != 1111) {
                            Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + i2);
                        }
                    }
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_REMOVE_GEN_FAILURE;
                } else if (i2 == 1004 || i2 == 1024 || i2 == 1029 || i2 == 1048 || i2 == 1053 || i2 == 1111) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_RENEW_GEN_FAILURE;
                } else {
                    Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + i2);
                }
            } else if (i2 == 1004) {
                return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_GEN_FAILURE;
            } else {
                if (i2 == 1024) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_INVALID_OWNER_ID;
                }
                if (i2 == 1029) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_GEN_FAILURE;
                }
                if (i2 == 1044) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_MAX_SVC_INST_REACHED;
                }
                if (i2 == 1048) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_SVC_NOT_ENTITLED;
                }
                if (i2 == 1111 || i2 == 1040 || i2 == 1041) {
                    return NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_SERVICE_PROVISION_GEN_FAILURE;
                }
                Log.d("ManageServiceErrorTranslator", "could not translate nsds error code:" + i2);
            }
            return -1;
        }
    }
}
