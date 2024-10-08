package com.sec.internal.constants.ims.core;

import android.os.Message;

public class SimConstants {
    public static final String DSDA_DI = "DSDA_DI";
    public static final String DSDS_DI = "DSDS_DI";
    public static final String SINGLE = "SINGLE";

    public enum SIM_STATE {
        UNKNOWN(0),
        ABSENT(1),
        LOCKED(2),
        INVALID_ISIM(3),
        LOADED(200);
        
        private int mState;

        private SIM_STATE(int i) {
            this.mState = i;
        }

        public boolean isOneOf(SIM_STATE... sim_stateArr) {
            for (SIM_STATE sim_state : sim_stateArr) {
                if (this == sim_state) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum ISIM_VALIDITY {
        IMPU_NOT_EXISTS(1),
        IMPU_INVALID(2),
        IMPI_NOT_EXIST(4),
        HOME_DOMAIN_NOT_EXIST(8);
        
        private int mCode;

        private ISIM_VALIDITY(int i) {
            this.mCode = i;
        }

        public int getValue() {
            return this.mCode;
        }
    }

    public enum SIM_VALIDITY {
        GBA_NOT_SUPPORTED(1),
        MSISDN_INVALID(2);
        
        private int mCode;

        private SIM_VALIDITY(int i) {
            this.mCode = i;
        }

        public int getValue() {
            return this.mCode;
        }
    }

    public static class SoftphoneAccount {
        public int mId;
        public String mImpi;
        public String mNonce;
        public Message mResponse;

        public SoftphoneAccount(String str, int i, String str2, Message message) {
            this.mNonce = str;
            this.mId = i;
            this.mImpi = str2;
            this.mResponse = message;
        }
    }
}
