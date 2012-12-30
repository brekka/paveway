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

import org.brekka.commons.lang.BaseException;


/**
 * Exception thrown in response to problems within the Paveway module.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class PavewayException extends BaseException {

    /**
     * Serial UID
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
