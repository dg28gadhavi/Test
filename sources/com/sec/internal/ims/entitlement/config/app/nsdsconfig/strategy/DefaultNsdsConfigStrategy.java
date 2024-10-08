package com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.Settings;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.EntitlementNamespaces;
import com.sec.internal.ims.cmstore.MStoreDebugTool;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.operation.DefaultNsdsOperation;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.operation.TmoNsdsOperation;
import com.sec.internal.ims.entitlement.storagehelper.EntitlementConfigDBHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.interfaces.ims.entitlement.config.IMnoNsdsConfigStrategy;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class DefaultNsdsConfigStrategy implements IMnoNsdsConfigStrategy {
    private static final long DEFAULT_REFRESH_TIME_IN_SECS = 86400;
    private static final String LOG_TAG = "DefaultNsdsConfigStrategy";
    protected Context mContext;
    protected NsdsConfigStrategyType mStrategyType = NsdsConfigStrategyType.DEFAULT;
    protected final Map<String, Integer> sMapEntitlementServices;

    public DefaultNsdsConfigStrategy(Context context) {
        HashMap hashMap = new HashMap();
        this.sMapEntitlementServices = hashMap;
        this.mContext = context;
        hashMap.put("vowifi", 1);
    }

    public final boolean isDeviceProvisioned() {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            z = true;
        }
        String str = LOG_TAG;
        IMSLog.i(str, "isDeviceProvisioned: " + z);
        return z;
    }

    public final String getEntitlementServerUrl(String str) {
        if (!this.mStrategyType.isOneOf(NsdsConfigStrategyType.TMOUS)) {
            return NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, str, "http://ses.ericsson-magic.net:10080/generic_devices");
        }
        String entitlementServerUrl = NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, str, (String) null);
        String str2 = LOG_TAG;
        IMSLog.i(str2, "getEntitlementServerUrl: url in sp " + entitlementServerUrl);
        return entitlementServerUrl == null ? EntitlementConfigDBHelper.getNsdsUrlFromDeviceConfig(this.mContext, MStoreDebugTool.DEFAULT_PRO_ENTITLEMENT) : entitlementServerUrl;
    }

    public final int getNextOperation(int i, int i2) {
        if (this.mStrategyType.isOneOf(NsdsConfigStrategyType.TMOUS)) {
            return TmoNsdsOperation.getOperation(i, i2);
        }
        return DefaultNsdsOperation.getOperation(i, i2);
    }

    public final void scheduleRefreshDeviceConfig(int i) {
        Cursor query;
        boolean isOneOf = this.mStrategyType.isOneOf(NsdsConfigStrategyType.TMOUS);
        long j = DEFAULT_REFRESH_TIME_IN_SECS;
        if (isOneOf) {
            try {
                query = this.mContext.getContentResolver().query(EntitlementConfigContract.DeviceConfig.buildXPathExprUri("//configInfo/configRefreshTime"), (String[]) null, (String) null, (String[]) null, (String) null);
                if (query != null) {
                    if (query.moveToFirst()) {
                        long j2 = query.getLong(1);
                        if (j2 > 0) {
                            j = j2;
                        }
                    }
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLException e) {
                String str = LOG_TAG;
                IMSLog.s(str, "Ignore sqlexception:" + e.getMessage());
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "scheduleRefreshDeviceConfig: " + j);
        if (j > 0) {
            IntentScheduler.scheduleTimer(this.mContext, i, EntitlementNamespaces.EntitlementActions.ACTION_REFRESH_DEVICE_CONFIG, j * 1000);
            return;
        }
        return;
        throw th;
    }

    protected enum NsdsConfigStrategyType {
        DEFAULT,
        TMOUS,
        END_OF_NSDSCONFIGSTRATEGY;

        /* access modifiers changed from: protected */
        public boolean isOneOf(NsdsConfigStrategyType... nsdsConfigStrategyTypeArr) {
            for (NsdsConfigStrategyType nsdsConfigStrategyType : nsdsConfigStrategyTypeArr) {
                if (this == nsdsConfigStrategyType) {
                    return true;
                }
            }
            return false;
        }
    }
}
