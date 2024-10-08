package com.sec.internal.constants.ims.servicemodules.options;

import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class BotServiceIdTranslator {
    private static final String TAG = "BotServiceIdTranslator";
    private static BotServiceIdTranslator mInstance;
    private Map<String, Map<String, String>> mBotServiceIdMap = new HashMap();

    private BotServiceIdTranslator() {
    }

    public static BotServiceIdTranslator getInstance() {
        if (mInstance == null) {
            synchronized (BotServiceIdTranslator.class) {
                if (mInstance == null) {
                    mInstance = new BotServiceIdTranslator();
                }
            }
        }
        return mInstance;
    }

    public void register(String str, String str2, int i) {
        if (str2 != null) {
            IMSLog.s(TAG, i, "register: msisdn = " + str + ", serviceId = " + str2);
            getOrCreateBotServiceIdMap(i).put(str, str2);
        }
    }

    public String translate(String str, int i) {
        String str2 = getOrCreateBotServiceIdMap(i).get(str);
        IMSLog.s(TAG, i, "translate: msisdn = " + str + ", serviceId = " + str2);
        return str2;
    }

    public Boolean contains(String str, int i) {
        IMSLog.s(TAG, i, "contains: serviceId = " + str);
        return Boolean.valueOf(getOrCreateBotServiceIdMap(i).containsValue(str));
    }

    private Map<String, String> getOrCreateBotServiceIdMap(int i) {
        String imsiFromPhoneId = SimManagerFactory.getImsiFromPhoneId(i);
        if (imsiFromPhoneId == null) {
            return new HashMap();
        }
        Map<String, String> map = this.mBotServiceIdMap.get(imsiFromPhoneId);
        if (map == null) {
            synchronized (this.mBotServiceIdMap) {
                map = this.mBotServiceIdMap.get(imsiFromPhoneId);
                if (map == null) {
                    map = new HashMap<>();
                    this.mBotServiceIdMap.put(imsiFromPhoneId, map);
                }
            }
        }
        return map;
    }
}
