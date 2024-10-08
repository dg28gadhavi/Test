package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.ServiceTuple;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.GlobalSettingsManager;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;

public class FeatureUpdater {
    private static final String LOG_TAG = "FeatureUpdater";
    private final Context mContext;
    private long mFeatures;
    private ImConfig mImConfig;
    private final ImModule mImModule;
    private IMnoStrategy mMnoStrategy;
    private int mPhoneId;

    public FeatureUpdater(Context context, ImModule imModule) {
        this.mContext = context;
        this.mImModule = imModule;
    }

    public long updateFeatures(int i, ImConfig imConfig) {
        boolean z = true;
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, i) != 1) {
            z = false;
        }
        if (!z || imConfig == null) {
            Log.i(LOG_TAG, "RCS is disabled.");
            ImsUtil.listToDumpFormat(LogClass.IM_SWITCH_OFF, i, MessageContextValues.none);
            return 0;
        }
        this.mFeatures = 0;
        this.mPhoneId = i;
        this.mImConfig = imConfig;
        this.mMnoStrategy = this.mImModule.getRcsStrategy(i);
        updateImFeatures();
        updateFtFeatures();
        updateSlmFeatures();
        updateGlsFeatures();
        updateChatBotFeatures();
        updateMnoCustomizedFeatures();
        updateExtendedBotMsgFeature();
        String str = LOG_TAG;
        Log.i(str, "updateFeatures: " + Capabilities.dumpFeature(this.mFeatures));
        return this.mFeatures;
    }

    public long updateExtendedBotMsgFeature(int i, long j) {
        this.mPhoneId = i;
        this.mFeatures = j;
        updateExtendedBotMsgFeature();
        return this.mFeatures;
    }

    private void updateMnoCustomizedFeatures() {
        IMnoStrategy iMnoStrategy = this.mMnoStrategy;
        if (iMnoStrategy != null) {
            if (iMnoStrategy.isCustomizedFeature((long) Capabilities.FEATURE_FT_VIA_SMS)) {
                this.mFeatures |= (long) Capabilities.FEATURE_FT_VIA_SMS;
            }
            if (this.mMnoStrategy.isCustomizedFeature(Capabilities.FEATURE_PUBLIC_MSG)) {
                this.mFeatures |= Capabilities.FEATURE_PUBLIC_MSG;
            }
        }
    }

    private void updateSlmFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "slm", this.mPhoneId) == 1 && this.mImConfig.getSlmAuth() != ImConstants.SlmAuth.DISABLED) {
            this.mFeatures |= (long) Capabilities.FEATURE_STANDALONE_MSG;
        }
    }

    private boolean isGlsEnabled(int i) {
        Boolean readBoolParam = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, i));
        String str = LOG_TAG;
        Log.i(str, "isEnableGls: " + readBoolParam);
        return readBoolParam.booleanValue();
    }

    private void updateImFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "im", this.mPhoneId) != 1) {
            return;
        }
        if (this.mImConfig.getChatEnabled() || this.mImConfig.getGroupChatEnabled()) {
            if (this.mImConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
                this.mFeatures |= (long) Capabilities.FEATURE_CHAT_SIMPLE_IM;
            } else {
                this.mFeatures |= (long) Capabilities.FEATURE_CHAT_CPM;
            }
            if (this.mImConfig.isGroupChatFullStandFwd() || this.mImConfig.isFullSFGroupChat()) {
                this.mFeatures |= (long) Capabilities.FEATURE_SF_GROUP_CHAT;
            }
            if (this.mImConfig.isJoynIntegratedMessaging() && this.mImModule.isDefaultMessageAppInUse()) {
                this.mFeatures |= (long) Capabilities.FEATURE_INTEGRATED_MSG;
            }
            if (GlobalSettingsManager.getInstance(this.mContext, this.mPhoneId).getInt(GlobalSettingsConstants.RCS.SUPPORT_CANCEL_MESSAGE, 0) == 1) {
                this.mFeatures |= Capabilities.FEATURE_CANCEL_MESSAGE;
            }
            if (DmConfigHelper.getImsSwitchValue(this.mContext, "plug-in", this.mPhoneId) == 1 && this.mImConfig.getPlugInEnabled()) {
                this.mFeatures |= Capabilities.FEATURE_PLUG_IN;
            }
        }
    }

    private void updateFtFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "ft", this.mPhoneId) == 1 && this.mImConfig.getFtEnabled()) {
            IMnoStrategy iMnoStrategy = this.mMnoStrategy;
            if (iMnoStrategy == null || !iMnoStrategy.isFtHttpOnlySupported(false)) {
                this.mFeatures |= (long) Capabilities.FEATURE_FT;
            }
            if (this.mImConfig.isFtThumb()) {
                this.mFeatures |= (long) Capabilities.FEATURE_FT_THUMBNAIL;
            }
        }
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "ft_http", this.mPhoneId) == 1 && this.mImConfig.getFtHttpEnabled()) {
            this.mFeatures |= (long) Capabilities.FEATURE_FT_HTTP;
        }
    }

    private void updateGlsFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "gls", this.mPhoneId) == 1 && isGlsEnabled(this.mPhoneId)) {
            if (this.mImConfig.getGlsPushEnabled()) {
                this.mFeatures |= (long) Capabilities.FEATURE_GEOLOCATION_PUSH;
                IMnoStrategy iMnoStrategy = this.mMnoStrategy;
                if (iMnoStrategy != null && iMnoStrategy.isCustomizedFeature((long) Capabilities.FEATURE_GEO_VIA_SMS)) {
                    this.mFeatures |= (long) Capabilities.FEATURE_GEO_VIA_SMS;
                }
            }
            if (this.mImConfig.getGlsPullEnabled()) {
                this.mFeatures |= (long) Capabilities.FEATURE_GEOLOCATION_PULL;
            }
        }
    }

    private void updateChatBotFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, this.mPhoneId) == 1) {
            if (this.mImConfig.getChatEnabled() && (this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM || this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.SESSION_ONLY)) {
                this.mFeatures |= Capabilities.FEATURE_CHATBOT_CHAT_SESSION;
            }
            if (this.mImConfig.getSlmAuth() == ImConstants.SlmAuth.DISABLED) {
                return;
            }
            if (this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM || this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.SLM_ONLY) {
                this.mFeatures |= Capabilities.FEATURE_CHATBOT_STANDALONE_MSG;
            }
        }
    }

    private void updateExtendedBotMsgFeature() {
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        ServiceTuple serviceTuple = ServiceTuple.getServiceTuple(Capabilities.FEATURE_CHATBOT_EXTENDED_MSG);
        Log.i(LOG_TAG, "FEATURE_CHATBOT_EXTENDED_MSG enabled ver:" + serviceTuple.version);
        if (!simMno.isKor() || TextUtils.equals("0.0", serviceTuple.version)) {
            this.mFeatures &= ~Capabilities.FEATURE_CHATBOT_EXTENDED_MSG;
        } else if (CapabilityUtil.hasFeature(this.mFeatures, Capabilities.FEATURE_CHATBOT_CHAT_SESSION) || CapabilityUtil.hasFeature(this.mFeatures, Capabilities.FEATURE_CHATBOT_STANDALONE_MSG)) {
            this.mFeatures |= Capabilities.FEATURE_CHATBOT_EXTENDED_MSG;
        }
    }
}
