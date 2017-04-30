package com.encens.khipus.initialize;

import java.util.Map;

/**
 * @author
 * @version 3.0
 */
public class CustomQuartzProcessorSetting {
    public static CustomQuartzProcessorSetting i = new CustomQuartzProcessorSetting();

    public CustomQuartzProcessorSetting() {
    }

    // Map that holds entries of seam service names and CustomQuartzProcessor config object
    Map<String, CustomQuartzProcessor> customQuartzProcessorMap;

    public Map<String, CustomQuartzProcessor> getCustomQuartzProcessorMap() {
        return customQuartzProcessorMap;
    }

    public void setCustomQuartzProcessorMap(Map<String, CustomQuartzProcessor> customQuartzProcessorMap) {
        this.customQuartzProcessorMap = customQuartzProcessorMap;
    }
}
