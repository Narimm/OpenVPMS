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

package org.openvpms.etl.kettle;

import junit.framework.TestCase;
import org.openvpms.etl.Reference;
import org.openvpms.etl.ReferenceParser;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReferenceParserTestCase extends TestCase {

    public void testReferenceParser() {
        Reference ref1 = ReferenceParser.parse("<party.customerperson>1234");
        assertNotNull(ref1);
        assertEquals("party.customerperson", ref1.getArchetype());
        assertEquals("1234", ref1.getLegacyId());
        assertNull(ref1.getName());
        assertNull(ref1.getValue());


        Reference ref2 = ReferenceParser.parse(
                "<lookup.contactPurpose>code=MAILING");
        assertNotNull(ref2);
        assertEquals("lookup.contactPurpose", ref2.getArchetype());
        assertEquals("code", ref2.getName());
        assertEquals("MAILING", ref2.getValue());
    }

}
