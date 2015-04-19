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

package org.openvpms.component.business.domain.im.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.party.Party;

/**
 * Tests the {@link IMObject} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectTestCase {

    /**
     * Test OVPMS-149 IMObject equality.
     */
    @Test
    public void testOVPMS149() {
        IMObject obj1 = new Party();
        obj1.setArchetypeId(new ArchetypeId("party.customerperson.1.0"));
        obj1.setName("jima");

        IMObject obj2 = new Party();
        obj2.setArchetypeId(new ArchetypeId("party.customerperson.1.0"));
        obj2.setName("jima");

        assertTrue(obj1.equals(obj1));
        assertFalse(obj1.equals(obj2));
        assertTrue(obj1.equals(obj1));
        assertFalse(obj1.equals(null));
    }

}
