package com.encens.khipus.exception;

import javax.ejb.ApplicationException;


/**
 * Exception thrown in the encryption or decryption process.
 *
 * @author
 * @version 1.0
 */
@ApplicationException(rollback = true)
//TODO: put the respective exception in pages.xml
public class UrlCipherException extends RuntimeException {
    public UrlCipherException() {
    }

    public UrlCipherException(String message) {
        super(message);
    }

    public UrlCipherException(String message, Throwable cause) {
        super(message, cause);
    }
}
