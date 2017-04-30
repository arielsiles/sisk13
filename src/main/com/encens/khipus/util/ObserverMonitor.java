package com.encens.khipus.util;

import java.util.Observable;

/**
 * ObserverMonitor
 *
 * @author
 * @version 2.2
 */
public class ObserverMonitor extends Observable {

    public void startNotify(Object notifyData) {
        setChanged();
        notifyObservers(notifyData);
    }
}
