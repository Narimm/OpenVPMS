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

package org.openvpms.etl.load;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;


/**
 * Tests the {@link ReferenceParser}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReferenceParserTestCase {

    /**
     * Tests the archetype/rowId form.
     */
    @Test
    public void testArchetypeRowId() {
        checkReference("<party.customerperson>1234.1", "party.customerperson",
                       "1234.1", null, null);
    }

    /**
     * Tests the archetype/name/value form.
     */
    @Test
    public void testArchetypeNameValue() {
        checkReference("<lookup.contactPurpose>code=MAILING",
                       "lookup.contactPurpose", null, "code", "MAILING");
    }

    /**
     * Checks that a reference can be parsed.
     *
     * @param reference the reference
     * @param archetype the expected archetype
     * @param rowId     the expected row id
     * @param name      the expected name
     * @param value     the expected value
     */
    private void checkReference(String reference, String archetype,
                                String rowId, String name, String value) {
        Reference ref = ReferenceParser.parse(reference);
        assertNotNull(ref);
        assertEquals(archetype, ref.getArchetype());
        assertEquals(rowId, ref.getRowId());
        assertEquals(name, ref.getName());
        assertEquals(value, ref.getValue());
    }

}
