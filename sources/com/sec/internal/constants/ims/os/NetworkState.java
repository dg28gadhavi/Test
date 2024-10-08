package com.sec.internal.constants.ims.os;

import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.PreciseDataConnectionState;
import android.util.SparseArray;
import com.sec.ims.extensions.ServiceStateExt;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NetworkState {
    public static final int IWLAN_AS_PHYSICAL_INTERFACE = 1;
    private static final String LOG_TAG = "NetworkState";
    public static final int MOBILE_DATA_AS_PHYSICAL_INTERFACE = 2;
    private CellIdentity mCellIdentity;
    private List<CellInfo> mCellInfo;
    private boolean mDataConnectionState;
    private int mDataNetworkType;
    private int mDataRegState;
    private boolean mDataRoaming;
    private EmcBsIndication mEmcbsIndication;
    private boolean mEmergencyOnly;
    private boolean mEpdgAvailable;
    private int mEpdgPhysicalInterface;
    private boolean mInternationalRoaming;
    private boolean mIsEmergencyEpdgConnected;
    private boolean mIsEpdgConnected;
    private boolean mIsPsOnlyReg;
    private Object mLock = new Object();
    private int mMobileDataNetworkType;
    private int mMobileDataRegState;
    private String mOperatorNumeric;
    private boolean mPendedEPDGWeakSignal;
    private SparseArray<PreciseDataConnectionState> mPreciseDataConnectionState;
    private int mSimSlot;
    private int mSnapshotState;
    private int mVoiceNetworkType;
    private int mVoiceRegState;
    private boolean mVoiceRoaming;
    private VoPsIndication mVopsIndication;

    public NetworkState(int i) {
        this.mSimSlot = i;
        this.mDataNetworkType = 0;
        this.mDataRegState = 1;
        this.mVoiceRegState = 1;
        this.mMobileDataRegState = 1;
        this.mSnapshotState = ServiceStateExt.SNAPSHOT_STATUS_DEACTIVATED;
    }

    public static NetworkState create(int i) {
        NetworkState networkState = new NetworkState(i);
        networkState.setEmcBsIndication(EmcBsIndication.UNKNOWN);
        networkState.setVopsIndication(VoPsIndication.UNKNOWN);
        networkState.setOperatorNumeric("");
        networkState.setAllCellInfo((List<CellInfo>) null);
        networkState.setCellIdentity((CellIdentity) null);
        return networkState;
    }

    public int getSimSlot() {
        return this.mSimSlot;
    }

    public void setDataNetworkType(int i) {
        this.mDataNetworkType = i;
    }

    public void setMobileDataNetworkType(int i) {
        this.mMobileDataNetworkType = i;
    }

    public int getDataNetworkType() {
        return this.mDataNetworkType;
    }

    public void setEpdgPhysicalInterface(int i) {
        this.mEpdgPhysicalInterface = i;
    }

    public int getEpdgPhysicalInterface() {
        return this.mEpdgPhysicalInterface;
    }

    public int getMobileDataNetworkType() {
        return this.mMobileDataNetworkType;
    }

    public int getMobileDataRegState() {
        return this.mMobileDataRegState;
    }

    public void setMobileDataRegState(int i) {
        this.mMobileDataRegState = i;
    }

    public void setDataRegState(int i) {
        this.mDataRegState = i;
    }

    public int getDataRegState() {
        return this.mDataRegState;
    }

    public void setVoiceRegState(int i) {
        this.mVoiceRegState = i;
    }

    public int getVoiceRegState() {
        return this.mVoiceRegState;
    }

    public void setSnapshotState(int i) {
        this.mSnapshotState = i;
    }

    public int getSnapshotState() {
        return this.mSnapshotState;
    }

    public void setVopsIndication(VoPsIndication voPsIndication) {
        this.mVopsIndication = voPsIndication;
    }

    public VoPsIndication getVopsIndication() {
        return this.mVopsIndication;
    }

    public void setEmcBsIndication(EmcBsIndication emcBsIndication) {
        this.mEmcbsIndication = emcBsIndication;
    }

    public EmcBsIndication getEmcBsIndication() {
        return this.mEmcbsIndication;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$setAllCellInfo$0(CellInfo cellInfo) {
        return !cellInfo.isRegistered();
    }

    public void setAllCellInfo(List<CellInfo> list) {
        if (list != null) {
            list.removeIf(new NetworkState$$ExternalSyntheticLambda0());
            list = ImsUtil.getAvailableCellInfo(list);
            if (list.isEmpty()) {
                IMSLog.i(LOG_TAG, "setAllCellInfo : There are no valid cellinfo !!!!");
                return;
            }
        } else {
            IMSLog.i(LOG_TAG, "setAllCellInfo : Cleared by null set !!!!");
        }
        synchronized (this.mLock) {
            this.mCellInfo = list;
        }
    }

    public List<CellInfo> getAllCellInfo() {
        ArrayList arrayList;
        synchronized (this.mLock) {
            arrayList = this.mCellInfo != null ? new ArrayList(this.mCellInfo) : null;
        }
        return arrayList;
    }

    public void setCellIdentity(CellIdentity cellIdentity) {
        this.mCellIdentity = cellIdentity;
    }

    public CellIdentity getCellIdentity() {
        return this.mCellIdentity;
    }

    public void setDataRoaming(boolean z) {
        this.mDataRoaming = z;
    }

    public void setDataConnectionState(boolean z) {
        this.mDataConnectionState = z;
    }

    public boolean isDataRoaming() {
        return this.mDataRoaming;
    }

    public boolean isDataConnectedState() {
        return this.mDataConnectionState;
    }

    public void setVoiceRoaming(boolean z) {
        this.mVoiceRoaming = z;
    }

    public boolean isVoiceRoaming() {
        return this.mVoiceRoaming;
    }

    public void setOperatorNumeric(String str) {
        this.mOperatorNumeric = str;
    }

    public String getOperatorNumeric() {
        return this.mOperatorNumeric;
    }

    public void setEmergencyOnly(boolean z) {
        this.mEmergencyOnly = z;
    }

    public boolean isEmergencyOnly() {
        return this.mEmergencyOnly;
    }

    public void setPsOnlyReg(boolean z) {
        this.mIsPsOnlyReg = z;
    }

    public boolean isPsOnlyReg() {
        return this.mIsPsOnlyReg;
    }

    public void setEpdgConnected(boolean z) {
        this.mIsEpdgConnected = z;
    }

    public boolean isEpdgConnected() {
        return this.mIsEpdgConnected;
    }

    public void setEpdgAvailable(boolean z) {
        this.mEpdgAvailable = z;
    }

    public boolean isEpdgAVailable() {
        return this.mEpdgAvailable;
    }

    public void setEmergencyEpdgConnected(boolean z) {
        this.mIsEmergencyEpdgConnected = z;
    }

    public boolean isEmergencyEpdgConnected() {
        return this.mIsEmergencyEpdgConnected;
    }

    public void setVoiceNetworkType(int i) {
        this.mVoiceNetworkType = i;
    }

    public int getVoiceNetworkType() {
        return this.mVoiceNetworkType;
    }

    public void setPendedEpdgWeakSignal(boolean z) {
        this.mPendedEPDGWeakSignal = z;
    }

    public boolean isPendedEPDGWeakSignal() {
        return this.mPendedEPDGWeakSignal;
    }

    public void setInternationalRoaming(boolean z) {
        this.mInternationalRoaming = z;
    }

    public boolean isInternationalRoaming() {
        return this.mInternationalRoaming;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ PreciseDataConnectionState lambda$getPreciseDataConnectionState$1(int i, SparseArray sparseArray) {
        return (PreciseDataConnectionState) sparseArray.get(i);
    }

    public PreciseDataConnectionState getPreciseDataConnectionState(int i) {
        return (PreciseDataConnectionState) Optional.ofNullable(this.mPreciseDataConnectionState).map(new NetworkState$$ExternalSyntheticLambda2(i)).orElse((Object) null);
    }

    public void setPreciseDataConnectionState(int i, PreciseDataConnectionState preciseDataConnectionState) {
        ((SparseArray) Optional.ofNullable(this.mPreciseDataConnectionState).orElseGet(new NetworkState$$ExternalSyntheticLambda1(this))).put(i, preciseDataConnectionState);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ SparseArray lambda$setPreciseDataConnectionState$2() {
        SparseArray<PreciseDataConnectionState> sparseArray = new SparseArray<>();
        this.mPreciseDataConnectionState = sparseArray;
        return sparseArray;
    }
}
