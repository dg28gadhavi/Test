package com.sec.internal.ims.servicemodules.euc.persistence;

import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.tapi.IUserConsentListener;

public class UserConsentPersistenceNotifier {
    private IUserConsentListener mListener;

    private UserConsentPersistenceNotifier() {
        this.mListener = null;
    }

    private static class UserConsentPersistenceNotifierHolder {
        /* access modifiers changed from: private */
        public static UserConsentPersistenceNotifier mUserConsentPersistenceNotifier = new UserConsentPersistenceNotifier();

        private UserConsentPersistenceNotifierHolder() {
        }
    }

    public static UserConsentPersistenceNotifier getInstance() {
        return UserConsentPersistenceNotifierHolder.mUserConsentPersistenceNotifier;
    }

    public void setListener(IUserConsentListener iUserConsentListener) {
        this.mListener = iUserConsentListener;
    }

    public void notifyListener(int i) {
        IUserConsentListener iUserConsentListener = this.mListener;
        if (iUserConsentListener != null) {
            iUserConsentListener.notifyChanged(i);
        }
    }

    public void notifyListener(String str) {
        if (this.mListener != null) {
            for (ISimManager iSimManager : SimManagerFactory.getAllSimManagers()) {
                if (str.equals(iSimManager.getImsi())) {
                    this.mListener.notifyChanged(iSimManager.getSimSlotIndex());
                    return;
                }
            }
        }
    }
}
