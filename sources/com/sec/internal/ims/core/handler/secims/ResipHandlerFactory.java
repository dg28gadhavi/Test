package com.sec.internal.ims.core.handler.secims;

import android.content.Context;
import android.os.Looper;
import com.sec.internal.ims.core.handler.HandlerFactory;
import com.sec.internal.ims.core.handler.MiscHandler;
import com.sec.internal.ims.core.handler.RawSipHandler;
import com.sec.internal.ims.core.handler.VolteHandler;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.handler.IRegistrationInterface;

public class ResipHandlerFactory extends HandlerFactory {
    public ResipHandlerFactory(Looper looper, Context context, IImsFramework iImsFramework) {
        super(looper);
        HandlerFactory.mRegistrationHandler = new ResipRegistrationManager(looper, context, iImsFramework);
        HandlerFactory.mAndroidVolteHandler = new ResipVolteHandler(looper, context, iImsFramework);
        HandlerFactory.mMediaHandler = new ResipMediaHandler(looper, context, iImsFramework);
        HandlerFactory.mVshHandler = new ResipVshHandler(looper, context, iImsFramework);
        HandlerFactory.mIshHandler = new ResipIshHandler(looper, iImsFramework);
        HandlerFactory.mSmsHandler = new ResipSmsHandler(looper, iImsFramework);
        HandlerFactory.mOptionsHandler = new ResipOptionsHandler(looper, iImsFramework);
        HandlerFactory.mPresenceHandler = new ResipPresenceHandler(looper, iImsFramework);
        ResipImdnHandler resipImdnHandler = new ResipImdnHandler(looper, iImsFramework);
        HandlerFactory.mImHandler = new ResipImHandler(looper, iImsFramework, resipImdnHandler);
        HandlerFactory.mSlmHandler = new ResipSlmHandler(looper, iImsFramework, resipImdnHandler);
        HandlerFactory.mEucHandler = new ResipEucHandler(looper, iImsFramework);
        HandlerFactory.mMiscHandler = new ResipMiscHandler(looper, context, iImsFramework);
        HandlerFactory.mRawSipHandler = new ResipRawSipHandler(looper, iImsFramework);
        HandlerFactory.mCmcHandler = new ResipCmcHandler(looper, context, iImsFramework);
        HandlerFactory.mVolteHandler = new VolteHandler(looper) {
        };
    }

    public IRegistrationInterface getRegistrationStackAdaptor() {
        return HandlerFactory.mRegistrationHandler;
    }

    public VolteHandler getVolteStackAdaptor() {
        return HandlerFactory.mAndroidVolteHandler;
    }

    public RawSipHandler getRawSipHandler() {
        return HandlerFactory.mRawSipHandler;
    }

    public MiscHandler getMiscHandler() {
        return HandlerFactory.mMiscHandler;
    }
}
