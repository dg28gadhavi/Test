package com.sec.internal.ims.servicemodules.im.interfaces;

import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;

public interface IRcsBigDataProcessor {
    void onMessageCancelSent(int i, int i2);

    void onMessageReceived(int i, MessageBase messageBase, ImSession imSession);

    void onMessageReceived(MessageBase messageBase, ImSession imSession);

    void onMessageSendingFailed(MessageBase messageBase, Result result, IMnoStrategy.StrategyResponse strategyResponse);

    void onMessageSendingFailed(MessageBase messageBase, Result result, String str, IMnoStrategy.StrategyResponse strategyResponse);

    void onMessageSendingSucceeded(MessageBase messageBase);
}
