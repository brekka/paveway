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

package org.brekka.paveway.web.support;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * ContentRangeTest
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ContentRangeTest {

    @Test
    public void testValid() {
        ContentRange contentRange = ContentRange.valueOf("bytes 2000000-2269525/2269526");
        assertEquals(2000000L, contentRange.getFirstBytePosition());
        assertEquals(2269525L, contentRange.getLastBytePosition());
        assertEquals(2269526L, contentRange.getLength());
    }

}
