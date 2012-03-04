package org.brekka.paveway.core;

import org.brekka.commons.lang.BaseException;


/**
 * @author Andrew Taylor
 */
public class PavewayException extends BaseException {

    /**
     * PhalanxErrorCode.java
     */
    private static final long serialVersionUID = -3832985168814340668L;


    /**
     * @param errorCode
     * @param message
     * @param messageArgs
     */
    public PavewayException(PavewayErrorCode errorCode, String message, Object... messageArgs) {
        super(errorCode, message, messageArgs);
    }

    /**
     * @param errorCode
     * @param cause
     * @param message
     * @param messageArgs
     */
    public PavewayException(PavewayErrorCode errorCode, Throwable cause, String message, Object... messageArgs) {
        super(errorCode, cause, message, messageArgs);
    }

}
