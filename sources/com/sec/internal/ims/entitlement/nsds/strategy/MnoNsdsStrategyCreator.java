package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class MnoNsdsStrategyCreator {
    private static final String LOG_TAG = "MnoNsdsStrategyCreator";
    private static Map<Integer, MnoNsdsStrategyCreator> sInstanceMap;
    private static Map<Mno, Class<?>> sMnoSpecificStrategyGenerator;
    private IMnoNsdsStrategy sMnoStrategy;

    private MnoNsdsStrategyCreator(Context context, int i) {
        if (sMnoSpecificStrategyGenerator == null) {
            initMnoSpecificStrategy();
        }
        this.sMnoStrategy = createMnoStrategy(context, i);
    }

    public static synchronized void resetMnoStrategy() {
        synchronized (MnoNsdsStrategyCreator.class) {
            Map<Integer, MnoNsdsStrategyCreator> map = sInstanceMap;
            if (map != null) {
                map.clear();
            }
        }
    }

    public static synchronized MnoNsdsStrategyCreator getInstance(Context context, int i) {
        MnoNsdsStrategyCreator mnoNsdsStrategyCreator;
        synchronized (MnoNsdsStrategyCreator.class) {
            if (sInstanceMap == null) {
                sInstanceMap = new HashMap();
            }
            if (sInstanceMap.get(Integer.valueOf(i)) == null) {
                sInstanceMap.put(Integer.valueOf(i), new MnoNsdsStrategyCreator(context, i));
            }
            mnoNsdsStrategyCreator = sInstanceMap.get(Integer.valueOf(i));
        }
        return mnoNsdsStrategyCreator;
    }

    private static void initMnoSpecificStrategy() {
        HashMap hashMap = new HashMap();
        sMnoSpecificStrategyGenerator = hashMap;
        hashMap.put(Mno.TMOUS, TmoNsdsStrategy.class);
        sMnoSpecificStrategyGenerator.put(Mno.ATT, AttNsdsStrategy.class);
        sMnoSpecificStrategyGenerator.put(Mno.GCI, XaaNsdsStrategy.class);
    }

    private IMnoNsdsStrategy createMnoStrategy(Context context, int i) {
        try {
            Mno simMno = SimUtil.getSimMno(i);
            String str = LOG_TAG;
            IMSLog.i(str, i, "createMnoStrategy: Mno = " + simMno);
            if (!sMnoSpecificStrategyGenerator.containsKey(simMno)) {
                return null;
            }
            return (IMnoNsdsStrategy) sMnoSpecificStrategyGenerator.get(simMno).getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
        } catch (IllegalAccessException | IllegalArgumentException | IllegalStateException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "exception" + e.getMessage());
            return null;
        }
    }

    public IMnoNsdsStrategy getMnoStrategy() {
        return this.sMnoStrategy;
    }
}
