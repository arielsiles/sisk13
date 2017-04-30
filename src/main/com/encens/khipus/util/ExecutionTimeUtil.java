package com.encens.khipus.util;

import java.util.concurrent.TimeUnit;

/**
 * ExecutionTimeUtil, this is a utility that obtain the execution time
 * by startExecutionTime and endExecutionTime.
 *
 * @author
 * @version 1.1.9
 */
public class ExecutionTimeUtil {

    private Long startExecutionTime;
    private Long durationTime;

    public ExecutionTimeUtil() {
    }

    public void startExecution() {
        startExecutionTime = System.nanoTime();
        System.out.println("startExecutionTime = " + startExecutionTime);
    }

    /**
     * The method endExecution return the duration execution time in seconds
     */
    public void endExecution() {
        Long endExecutionTime = System.nanoTime();
        System.out.println("endExecutionTime = " + endExecutionTime);
        durationTime = endExecutionTime - startExecutionTime + 1;
        System.out.println("durationTime = " + durationTime);
    }

    public Long timeInNanosecons() {
        System.out.println("timeInNanosecons()=" + durationTime);
        return durationTime;
    }

    public Long timeInMillis() {
        System.out.println("timeInMillis()=" + TimeUnit.NANOSECONDS.toMillis(durationTime));
        return TimeUnit.NANOSECONDS.toMillis(durationTime);
    }


    public Long timeInSecons() {
        System.out.println("timeInSecons()=" + TimeUnit.NANOSECONDS.toSeconds(durationTime));
        return TimeUnit.NANOSECONDS.toSeconds(durationTime);
    }

    public static void main(String srg[]) {
        ExecutionTimeUtil executionTimeUtil = new ExecutionTimeUtil();
        executionTimeUtil.startExecution();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // InterruptedException
        }
        executionTimeUtil.endExecution();
        System.out.println("executionTimeUtil.timeInSecons()=" + executionTimeUtil.timeInSecons());
    }
}
