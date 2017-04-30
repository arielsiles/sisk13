package com.encens.khipus.exception.employees;

/**
 * @author
 * @version 2.26
 */

public class GrantedBonusNotFoundException extends Exception {
    public GrantedBonusNotFoundException() {
    }

    public GrantedBonusNotFoundException(String message) {
        super(message);
    }

    public GrantedBonusNotFoundException(Throwable cause) {
        super(cause);
    }
}