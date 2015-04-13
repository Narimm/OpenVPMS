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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.party;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * {@link MergeException} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MergeExceptionTestCase extends TestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     2, MergeException.ErrorCode.values().length);

        checkException(MergeException.ErrorCode.InvalidType,
                       "Cannot merge foo", "foo");
        checkException(MergeException.ErrorCode.CannotMergeToSameObject,
                       "Cannot merge a foo with itself", "foo");
    }

    /**
     * Creates an {@link MergeException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(MergeException.ErrorCode code,
                                String expected, Object... args) {
        MergeException exception = new MergeException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }

}
