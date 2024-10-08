package com.sec.internal.ims.core.imslogger;

import android.content.Context;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;
import java.util.ArrayList;
import java.util.Iterator;

public class ExternalSupporter {
    private static final String DM_PACKAGE = "com.sec.imslogger";
    private static final String LOG_TAG = ExternalSupporter.class.getSimpleName();
    private static final String STTS_PACKAGE = "com.googlecode.android_scripting";
    private static final ArrayList<ISignallingNotifier> mPackages = new ArrayList<>();
    private ImsLoggerPlus mImsLogger = null;
    private SttsSipChecker mSttsSipChecker = null;

    public ExternalSupporter(Context context) {
        if (SemEmergencyManager.isEmergencyMode(context)) {
            SemEmergencyManager instance = SemEmergencyManager.getInstance(context);
            if (instance == null) {
                return;
            }
            if (SystemUtil.checkUltraPowerSavingMode(instance)) {
                Log.i(LOG_TAG, "UPSM mode skip package add");
            } else if (instance.checkModeType(16)) {
                Log.i(LOG_TAG, "EMERGENCY mode skip package add");
            }
        } else if (Debug.ALLOW_DIAGNOSTICS || DeviceUtil.isOtpAuthorized()) {
            Log.i(LOG_TAG, "package add");
            ArrayList<ISignallingNotifier> arrayList = mPackages;
            arrayList.add(new ExternalPackage(context, "com.hugeland.cdsplus"));
            ImsLoggerPlus imsLoggerPlus = new ImsLoggerPlus(context, DM_PACKAGE, "com.sec.imslogger.services.ImsDmService");
            this.mImsLogger = imsLoggerPlus;
            arrayList.add(imsLoggerPlus);
            SttsSipChecker sttsSipChecker = new SttsSipChecker(context, STTS_PACKAGE, "com.googlecode.android_scripting.facade.imslogger.ImsLoggerService");
            this.mSttsSipChecker = sttsSipChecker;
            arrayList.add(sttsSipChecker);
        }
    }

    public boolean send(Object obj) {
        Iterator<ISignallingNotifier> it = mPackages.iterator();
        while (it.hasNext()) {
            it.next().send(obj);
        }
        return true;
    }

    public ISignallingNotifier.PackageStatus checkPackageStatus(String str) {
        SttsSipChecker sttsSipChecker;
        ImsLoggerPlus imsLoggerPlus;
        if (DM_PACKAGE.equals(str) && (imsLoggerPlus = this.mImsLogger) != null) {
            return imsLoggerPlus.checkPackageStatus();
        }
        if (!STTS_PACKAGE.equals(str) || (sttsSipChecker = this.mSttsSipChecker) == null) {
            return ISignallingNotifier.PackageStatus.NOT_INSTALLED;
        }
        return sttsSipChecker.checkPackageStatus();
    }

    public void initializeDmEvent(String str) {
        SttsSipChecker sttsSipChecker;
        ImsLoggerPlus imsLoggerPlus;
        if (DM_PACKAGE.equals(str) && (imsLoggerPlus = this.mImsLogger) != null) {
            imsLoggerPlus.initializeDmEvent();
        } else if (STTS_PACKAGE.equals(str) && (sttsSipChecker = this.mSttsSipChecker) != null) {
            sttsSipChecker.initializeDmEvent();
        }
    }
}
