/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.paveway.core;

import org.brekka.commons.lang.ErrorCode;

/**
 * Error types relating to the Paveway subsystem.
 * 
 * TODO review
 * 
 * @author Andrew Taylor
 */
public enum PavewayErrorCode implements ErrorCode {

    PW100,
    
    PW200,
    
    PW300,
    
    PW400,
    PW401,
    
    PW500,
    
    PW600,
    PW673,
    
    PW700,
    PW701,
    
    PW800,
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
