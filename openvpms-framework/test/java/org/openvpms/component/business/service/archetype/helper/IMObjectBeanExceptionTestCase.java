/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * {@link IMObjectBeanException} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectBeanExceptionTestCase extends TestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     4, IMObjectBeanException.ErrorCode.values().length);
        checkException(IMObjectBeanException.ErrorCode.NodeDescriptorNotFound,
                       "No node found named foo in archetype bar", "foo", "bar");
        checkException(IMObjectBeanException.ErrorCode.ArchetypeNotFound,
                       "Archetype with short name foo not found.", "foo");
        checkException(IMObjectBeanException.ErrorCode.InvalidClassCast, "Expected class of type foo but got bar",
                       "foo", "bar");
        checkException(IMObjectBeanException.ErrorCode.CannotAddTargetToNode,
                       "Cannot add target with archetype foo to node named bar", "foo", "bar");
    }

    /**
     * Creates an {@link org.openvpms.component.business.service.archetype.helper.IMObjectBeanException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void checkException(IMObjectBeanException.ErrorCode code,
                                String expected, Object ... args) {
        IMObjectBeanException exception = new IMObjectBeanException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }
}