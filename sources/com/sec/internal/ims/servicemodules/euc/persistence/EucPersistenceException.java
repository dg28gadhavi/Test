package com.sec.internal.ims.servicemodules.euc.persistence;

public class EucPersistenceException extends Exception {
    EucPersistenceException(String str) {
        super(str);
    }

    EucPersistenceException(String str, Throwable th) {
        super(str, th);
    }
}
