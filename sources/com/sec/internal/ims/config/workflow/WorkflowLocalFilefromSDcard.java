package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import java.util.Map;

public class WorkflowLocalFilefromSDcard extends WorkflowLocalFile {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowLocalFilefromSDcard.class.getSimpleName();
    Mno mMno;

    public WorkflowLocalFilefromSDcard(Looper looper, Context context, IConfigModule iConfigModule, Mno mno, int i) {
        super(looper, context, iConfigModule, mno, i);
        this.mMno = mno;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        if (i == 1) {
            return new WorkflowBase.Initialize() {
                public WorkflowBase.Workflow run() throws Exception {
                    WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard = WorkflowLocalFilefromSDcard.this;
                    workflowLocalFilefromSDcard.addEventLog(WorkflowLocalFilefromSDcard.LOG_TAG + "local filename: " + ConfigUtil.SDCARD_CONFIG_FILE);
                    WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard2 = WorkflowLocalFilefromSDcard.this;
                    workflowLocalFilefromSDcard2.mSharedInfo.setXml(ConfigUtil.getResourcesFromFile(workflowLocalFilefromSDcard2.mContext, workflowLocalFilefromSDcard2.mPhoneId, ConfigUtil.SDCARD_CONFIG_FILE, "utf-8"));
                    return WorkflowLocalFilefromSDcard.this.getNextWorkflow(6);
                }
            };
        }
        if (i == 6) {
            return new WorkflowBase.Parse() {
                public WorkflowBase.Workflow run() throws Exception {
                    WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard = WorkflowLocalFilefromSDcard.this;
                    Map<String, String> parse = workflowLocalFilefromSDcard.mXmlParser.parse(workflowLocalFilefromSDcard.mSharedInfo.getXml());
                    if (parse == null) {
                        throw new InvalidXmlException("no parsed xml data.");
                    } else if (parse.get("root/vers/version") == null || parse.get("root/vers/validity") == null) {
                        throw new InvalidXmlException("config xml must contain atleast 2 items(version & validity).");
                    } else {
                        WorkflowLocalFilefromSDcard.this.mParamHandler.parseParamForLocalFile(parse);
                        WorkflowLocalFilefromSDcard.this.mParamHandler.moveHttpParam(parse);
                        WorkflowLocalFilefromSDcard.this.mSharedInfo.setParsedXml(parse);
                        return WorkflowLocalFilefromSDcard.this.getNextWorkflow(7);
                    }
                }
            };
        }
        if (i != 7) {
            return super.getNextWorkflow(i);
        }
        return new WorkflowBase.Store() {
            public WorkflowBase.Workflow run() throws Exception {
                WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard = WorkflowLocalFilefromSDcard.this;
                if (RcsUtils.isImsSingleRegiRequired(workflowLocalFilefromSDcard.mContext, workflowLocalFilefromSDcard.mPhoneId)) {
                    WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard2 = WorkflowLocalFilefromSDcard.this;
                    if (ConfigUtil.isGoogDmaPackageInuse(workflowLocalFilefromSDcard2.mContext, workflowLocalFilefromSDcard2.mPhoneId)) {
                        WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard3 = WorkflowLocalFilefromSDcard.this;
                        workflowLocalFilefromSDcard3.setOpMode(workflowLocalFilefromSDcard3.getOpMode(workflowLocalFilefromSDcard3.mSharedInfo.getParsedXml()), WorkflowLocalFilefromSDcard.this.mSharedInfo.getParsedXml());
                        WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard4 = WorkflowLocalFilefromSDcard.this;
                        String str = "";
                        workflowLocalFilefromSDcard4.mStorage.write(ConfigConstants.PATH.INFO_RCS_VERSION, TextUtils.isEmpty(workflowLocalFilefromSDcard4.mRcsVersion) ? str : WorkflowLocalFilefromSDcard.this.mRcsVersion);
                        WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard5 = WorkflowLocalFilefromSDcard.this;
                        workflowLocalFilefromSDcard5.mStorage.write(ConfigConstants.PATH.INFO_RCS_PROFILE, TextUtils.isEmpty(workflowLocalFilefromSDcard5.mRcsProfile) ? str : WorkflowLocalFilefromSDcard.this.mRcsProfile);
                        WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard6 = WorkflowLocalFilefromSDcard.this;
                        workflowLocalFilefromSDcard6.mStorage.write(ConfigConstants.PATH.INFO_CLIENT_VENDOR, TextUtils.isEmpty(workflowLocalFilefromSDcard6.mClientVendor) ? str : WorkflowLocalFilefromSDcard.this.mClientVendor);
                        WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard7 = WorkflowLocalFilefromSDcard.this;
                        IStorageAdapter iStorageAdapter = workflowLocalFilefromSDcard7.mStorage;
                        if (!TextUtils.isEmpty(workflowLocalFilefromSDcard7.mClientVersion)) {
                            str = WorkflowLocalFilefromSDcard.this.mClientVersion;
                        }
                        iStorageAdapter.write(ConfigConstants.PATH.INFO_CLIENT_VERSION, str);
                        return WorkflowLocalFilefromSDcard.this.getNextWorkflow(8);
                    }
                }
                WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard8 = WorkflowLocalFilefromSDcard.this;
                boolean userAccept = workflowLocalFilefromSDcard8.mParamHandler.getUserAccept(workflowLocalFilefromSDcard8.mSharedInfo.getParsedXml());
                WorkflowLocalFilefromSDcard workflowLocalFilefromSDcard9 = WorkflowLocalFilefromSDcard.this;
                workflowLocalFilefromSDcard9.mStartForce = userAccept || workflowLocalFilefromSDcard9.mStartForce;
                workflowLocalFilefromSDcard9.mParamHandler.setOpModeWithUserAccept(userAccept, workflowLocalFilefromSDcard9.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
                return WorkflowLocalFilefromSDcard.this.getNextWorkflow(8);
            }
        };
    }
}
