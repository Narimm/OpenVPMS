/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Tests the {@link IMObjectReference} class.
 *
 * @author Tim Anderson
 */
public class IMObjectReferenceTestCase {

    /**
     * Tests the {@link IMObjectReference#toString()} method.
     */
    @Test
    public void testToString() {
        IMObjectReference ref1 = new IMObjectReference("party.customerperson", 10001, "abcdef");
        IMObjectReference ref2 = new IMObjectReference("party.customerperson", -1, "abcdef");
        IMObjectReference ref3 = new IMObjectReference("party.customerperson", 10001);

        assertEquals("party.customerperson:10001:abcdef", ref1.toString());
        assertEquals("party.customerperson:-1:abcdef", ref2.toString());
        assertEquals("party.customerperson:10001", ref3.toString());
    }

    /**
     * Tests the {@link IMObjectReference#fromString(String)} method.
     */
    @Test
    public void testFromString() {
        assertNull(IMObjectReference.fromString(null));
        checkInvalidFromString("");
        checkInvalidFromString("party.customerperson");
        checkInvalidFromString("party.customerperson:abcdef");

        assertEquals(new IMObjectReference("party.customerperson", 10001, "abcdef"),
                     IMObjectReference.fromString("party.customerperson:10001:abcdef"));

        assertEquals(new IMObjectReference("party.customerperson", -1, "abcdef"),
                     IMObjectReference.fromString("party.customerperson:-1:abcdef"));

        assertEquals(new IMObjectReference("party.customerperson", 10001),
                     IMObjectReference.fromString("party.customerperson:10001"));
    }

    /**
     * Verifies that an IllegalArgumentException is thrown if an invalid value is passed to
     * {@link IMObjectReference#fromString(String)}.
     *
     * @param value the value
     */
    private void checkInvalidFromString(String value) {
        try {
            assertNull(IMObjectReference.fromString(value));
            fail("Expected fromString to fail");
        } catch (IllegalArgumentException expected) {
            // do nothing
        }
    }
}
