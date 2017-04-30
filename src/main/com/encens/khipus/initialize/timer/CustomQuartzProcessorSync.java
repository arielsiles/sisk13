package com.encens.khipus.initialize.timer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class synchronize the CustomQuartzProcessor for execute one active process
 * by quartz execution.
 *
 * @author
 * @version 3.0
 */
public class CustomQuartzProcessorSync {
    public static CustomQuartzProcessorSync i = new CustomQuartzProcessorSync();
    private Map<String, Boolean> process;

    private CustomQuartzProcessorSync() {
        process = new HashMap<String, Boolean>();
    }

    public boolean begin(String serviceSeamName) {
        Boolean started = process.get(serviceSeamName);
        if (null == started || Boolean.FALSE.equals(started)) {
            process.put(serviceSeamName, Boolean.TRUE);
            System.out.println("The CustomQuartzProcessor(" + serviceSeamName + ") is begin...");
            return Boolean.TRUE;
        }
        System.out.println("The CustomQuartzProcessor(" + serviceSeamName + ") can't be processed, it's running...");
        return Boolean.FALSE;
    }

    public void end(String serviceSeamName) {
        if (Boolean.TRUE.equals(process.get(serviceSeamName))) {
            System.out.println("The CustomQuartzProcessor(" + serviceSeamName + ") is end...");
            process.put(serviceSeamName, Boolean.FALSE);
        }
    }
}
