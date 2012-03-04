package org.brekka.paveway.core;

import org.brekka.commons.lang.ErrorCode;

/**
 * Error types relating to the Paveway subsystem.
 * 
 * @author Andrew Taylor
 */
public enum PavewayErrorCode implements ErrorCode {

    PW100,
    
    PW200,
    
    PW400,
    PW401,
    
    ;
    
    private static final Area AREA = ErrorCode.Utils.createArea("PW");
    private int number = 0;

    @Override
    public int getNumber() {
        return (this.number == 0 ? this.number = ErrorCode.Utils.extractErrorNumber(name(), getArea()) : this.number);
    }
    @Override
    public Area getArea() { return AREA; }
}
