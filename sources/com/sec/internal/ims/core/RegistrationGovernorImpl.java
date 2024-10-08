package com.sec.internal.ims.core;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegistrationGovernorImpl extends RegistrationGovernorBase {
    private static final String LOG_TAG = "RegiGvnImpl";

    public RegistrationGovernorImpl(RegistrationManagerInternal registrationManagerInternal, ITelephonyManager iTelephonyManager, RegisterTask registerTask, PdnController pdnController, IVolteServiceModule iVolteServiceModule, IConfigModule iConfigModule, Context context) {
        super(registrationManagerInternal, iTelephonyManager, registerTask, pdnController, iVolteServiceModule, iConfigModule, context);
    }

    public Set<String> filterService(Set<String> set, int i) {
        if (!DeviceUtil.getGcfMode() || this.mMno != Mno.GCF || SemSystemProperties.getInt(ImsConstants.SystemProperties.IMS_TEST_MODE_PROP, 0) != 1) {
            return super.filterService(set, i);
        }
        Log.i(LOG_TAG, "by GCF(VZW) IMS_TEST_MODE_PROP - remove all service");
        return new HashSet();
    }

    private boolean checkGcfStatus(int i) {
        boolean z;
        if (this.mTask.getProfile().getPdnType() == 11 && this.mTask.getState() != RegistrationConstants.RegisterTaskState.REGISTERED) {
            List<String> gcfInitialRegistrationRat = getGcfInitialRegistrationRat();
            Log.i(LOG_TAG, "gcfIntialRegistrationRat = " + gcfInitialRegistrationRat);
            Iterator<String> it = gcfInitialRegistrationRat.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (ImsProfile.getNetworkType(it.next()) == i) {
                        z = true;
                        break;
                    }
                } else {
                    z = false;
                    break;
                }
            }
            if (z) {
                Log.i(LOG_TAG, "GCF, Initial Rat condition is matched");
            } else {
                Log.i(LOG_TAG, "GCF, Initial Rat condition is not matched");
                return false;
            }
        }
        return true;
    }

    private List<String> getGcfInitialRegistrationRat() {
        Cursor query;
        String str = "";
        try {
            query = this.mContext.getContentResolver().query(Uri.parse("content://com.sec.ims.settings/gcfinitrat"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (query != null) {
                if (query.moveToFirst()) {
                    str = query.getString(query.getColumnIndex("rat"));
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (Exception unused) {
            Log.e(LOG_TAG, "failed to get getGcfInitialRegistrationRat");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return Arrays.asList(TextUtils.split(str, ","));
        throw th;
    }

    /* access modifiers changed from: protected */
    public void handleForbiddenError(long j) {
        Log.e(LOG_TAG, "onRegistrationError: Permanently prohibited.");
        this.mIsPermanentStopped = true;
    }

    public boolean isReadyToRegister(int i) {
        return super.isReadyToRegister(i) && checkGcfStatus(i);
    }

    public boolean determineDeRegistration(int i, int i2) {
        if (i != 0 || this.mTelephonyManager.getCallState() == 0) {
            return super.determineDeRegistration(i, i2);
        }
        int i3 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i3, "determineDeRegistration: no IMS service for network " + i2 + ". Deregister.");
        RegisterTask registerTask = this.mTask;
        registerTask.setReason("no IMS service for network : " + i2);
        this.mTask.setDeregiReason(4);
        this.mRegMan.tryDeregisterInternal(this.mTask, false, true);
        return true;
    }

    public RegisterTask onManualDeregister(boolean z) {
        if ((!this.mTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERING, RegistrationConstants.RegisterTaskState.EMERGENCY) && (this.mTask.getState() != RegistrationConstants.RegisterTaskState.DEREGISTERING || this.mTask.getUserAgent() != null)) || !this.mPdnController.isConnected(this.mTask.getPdnType(), this.mTask)) {
            return super.onManualDeregister(z);
        }
        Log.i(LOG_TAG, "onManualDeregister: gcf enabled. Do nothing..");
        return null;
    }
}
