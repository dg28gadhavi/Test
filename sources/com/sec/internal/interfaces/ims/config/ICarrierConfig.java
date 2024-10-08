package com.sec.internal.interfaces.ims.config;

import android.os.PersistableBundle;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.config.BooleanCarrierConfig;
import com.sec.internal.constants.ims.config.IntCarrierConfig;
import com.sec.internal.constants.ims.config.LongCarrierConfig;
import com.sec.internal.constants.ims.config.StringArrayCarrierConfig;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ICarrierConfig {
    void putOverrideConfig(PersistableBundle persistableBundle, JsonObject jsonObject);

    static List<ICarrierConfig> getAllConfigs() {
        return (List) Stream.of(new Enum[][]{BooleanCarrierConfig.values(), IntCarrierConfig.values(), LongCarrierConfig.values(), StringArrayCarrierConfig.values()}).flatMap(new ICarrierConfig$$ExternalSyntheticLambda0()).collect(Collectors.toList());
    }
}
