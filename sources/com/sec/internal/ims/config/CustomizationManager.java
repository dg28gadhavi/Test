package com.sec.internal.ims.config;

import android.content.Context;
import android.os.Looper;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceTelstra;
import com.sec.internal.ims.config.workflow.WorkflowAtt;
import com.sec.internal.ims.config.workflow.WorkflowBell;
import com.sec.internal.ims.config.workflow.WorkflowChn;
import com.sec.internal.ims.config.workflow.WorkflowInterop;
import com.sec.internal.ims.config.workflow.WorkflowJibe;
import com.sec.internal.ims.config.workflow.WorkflowLocalFile;
import com.sec.internal.ims.config.workflow.WorkflowLocalFilefromSDcard;
import com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice;
import com.sec.internal.ims.config.workflow.WorkflowRjil;
import com.sec.internal.ims.config.workflow.WorkflowSec;
import com.sec.internal.ims.config.workflow.WorkflowTmo;
import com.sec.internal.ims.config.workflow.WorkflowUp;
import com.sec.internal.ims.config.workflow.WorkflowVzw;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.log.IMSLog;

public class CustomizationManager {
    private static final String LOG_TAG = "CustomizationManager";

    public static IWorkflow getConfigManager(Looper looper, Context context, ConfigModule configModule, int i, boolean z) {
        Mno simMno = SimUtil.getSimMno(i);
        int autoconfigSourceWithFeature = ConfigUtil.getAutoconfigSourceWithFeature(i, 0);
        if (autoconfigSourceWithFeature == 2) {
            IMSLog.i(LOG_TAG, i, "get config from local file.");
            return new WorkflowLocalFile(looper, context, configModule, simMno, i);
        } else if (autoconfigSourceWithFeature == 3) {
            IMSLog.i(LOG_TAG, i, "get config from SDcard.");
            return new WorkflowLocalFilefromSDcard(looper, context, configModule, simMno, i);
        } else {
            String rcsProfileLoaderInternalWithFeature = ConfigUtil.getRcsProfileLoaderInternalWithFeature(context, simMno.getName(), i);
            IMSLog.c(LogClass.CZM_RCSP, i + "," + simMno + ",RCSP:" + rcsProfileLoaderInternalWithFeature);
            if (ImsProfile.isRcsUpProfile(rcsProfileLoaderInternalWithFeature)) {
                String acsServerType = ConfigUtil.getAcsServerType(i);
                if (ImsConstants.RCS_AS.JIBE.equals(acsServerType) && z) {
                    IMSLog.i(LOG_TAG, i, "WorkflowJibe");
                    return new WorkflowJibe(looper, context, configModule, simMno, i);
                } else if (ImsConstants.RCS_AS.SEC.equals(acsServerType)) {
                    IMSLog.i(LOG_TAG, i, "WorkflowSec");
                    return new WorkflowSec(looper, context, configModule, simMno, i);
                } else if (simMno.equals(Mno.VZW)) {
                    IMSLog.i(LOG_TAG, i, "WorkflowVzw");
                    return new WorkflowVzw(looper, context, configModule, simMno, i);
                } else if (simMno.equals(Mno.BELL)) {
                    IMSLog.i(LOG_TAG, i, "WorkflowBell");
                    return new WorkflowBell(looper, context, configModule, simMno, i);
                } else if (simMno.equals(Mno.RJIL)) {
                    IMSLog.i(LOG_TAG, i, "WorkflowRjil");
                    return new WorkflowRjil(looper, context, configModule, simMno, i);
                } else if (ConfigUtil.isRcsChn(simMno)) {
                    IMSLog.i(LOG_TAG, i, "WorkflowChn");
                    return new WorkflowChn(looper, context, configModule, simMno, i);
                } else if (ImsConstants.RCS_AS.INTEROP.equals(acsServerType)) {
                    IMSLog.i(LOG_TAG, i, "WorkflowInterop");
                    return new WorkflowInterop(looper, context, configModule, simMno, i);
                } else {
                    IMSLog.i(LOG_TAG, i, "WorkflowUp");
                    return new WorkflowUp(looper, context, configModule, simMno, i);
                }
            } else if (ConfigUtil.isRcsChn(simMno)) {
                IMSLog.i(LOG_TAG, i, "WorkflowChn");
                return new WorkflowChn(looper, context, configModule, simMno, i);
            } else if (simMno.equals(Mno.RJIL)) {
                IMSLog.i(LOG_TAG, i, "WorkflowRjil");
                return new WorkflowRjil(looper, context, configModule, simMno, i);
            } else if (simMno.equals(Mno.ATT)) {
                if (SimUtil.isSoftphoneEnabled()) {
                    IMSLog.i(LOG_TAG, i, "Use local config for SoftPhone");
                    return new WorkflowLocalFile(looper, context, configModule, simMno, i);
                }
                IMSLog.i(LOG_TAG, i, "WorkflowAtt");
                return new WorkflowAtt(looper, context, configModule, simMno, i);
            } else if (simMno.equals(Mno.TELSTRA)) {
                IMSLog.i(LOG_TAG, i, "WorkflowPrimaryDevice for Telstra");
                return new WorkflowPrimaryDevice(looper, context, configModule, simMno, new TelephonyAdapterPrimaryDeviceTelstra(context, configModule, i), i);
            } else if (simMno.equals(Mno.TMOUS)) {
                IMSLog.i(LOG_TAG, i, "WorkflowTmo");
                return new WorkflowTmo(looper, context, configModule, simMno, i);
            } else {
                IMSLog.i(LOG_TAG, i, "WorkflowPrimaryDevice");
                return new WorkflowPrimaryDevice(looper, context, configModule, simMno, new TelephonyAdapterPrimaryDevice(context, configModule, i), i);
            }
        }
    }
}
