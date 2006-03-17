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

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the JXPath expressions on etity objects and descriptors.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectTestCase extends BaseTestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IMObjectTestCase.class);
    }

    /**
     * Constructor for IMObjectTestCase.
     * 
     * @param arg0
     */
    public IMObjectTestCase(String name) {
        super(name);
    }

    /**
     * Test OVPMS-149 IMObject equality
     */
    public void testOVPMS149() 
    throws Exception {
        IMObject obj1 = new Party();
        obj1.setArchetypeId(new ArchetypeId("openvpms-party-party.customerperson.1.0"));
        obj1.setName("jima");

        IMObject obj2 = new Party();
        obj2.setArchetypeId(new ArchetypeId("openvpms-party-party.customerperson.1.0"));
        obj2.setName("jima");
        
        IMObject obj3 = obj1;
        
        assertTrue(obj1.equals(obj1));
        assertTrue(obj1.equals(obj2) == false);
        assertTrue(obj1.equals(obj3));
        assertTrue(obj1.equals(null) == false);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

}
