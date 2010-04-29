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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.system.service.uuid;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.safehaus.uuid.UUIDGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * Exercise the {@link JUGGenerator}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class JUGGeneratorTestCase {

    /**
     * Test the creation of multiple UUIDs.
     */
    @Test
    public void testMultipleUUIDCreation() {
        JUGGenerator generator = new JUGGenerator(
                UUIDGenerator.getInstance().getDummyAddress().toString());

        Set<String> ids = new HashSet<String>();
        for (int index = 0; index < 1000; index++) {
            String id = generator.nextId();
            assertTrue(ids.add(id));
        }
    }

    /**
     * Test the creation of UUIDs with a prefix.
     */
    @Test
    public void testPrefixGeneration() {
        JUGGenerator generator = new JUGGenerator(
                UUIDGenerator.getInstance().getDummyAddress().toString());

        String prefix = "prefix:";
        Set<String> ids = new HashSet<String>();
        for (int index = 0; index < 1000; index++) {
            String id = generator.nextId(prefix);
            assertTrue(id.startsWith(prefix));
            assertTrue(ids.add(id));
        }
    }

}
