package com.sec.internal.helper;

import android.os.Message;

public class AsyncResult {
    public Throwable exception;
    public Object result;
    public Object userObj;

    public static AsyncResult forMessage(Message message, Object obj, Throwable th) {
        AsyncResult asyncResult = new AsyncResult(message.obj, obj, th);
        message.obj = asyncResult;
        return asyncResult;
    }

    public AsyncResult(Object obj, Object obj2, Throwable th) {
        this.userObj = obj;
        this.result = obj2;
        this.exception = th;
    }
}
