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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeParserTestCase extends TestCase {

    public void testNodeParser() {
        Node node1 = NodeParser.parse("<party.customerPerson>firstName");
        assertNotNull(node1);
        checkNode(node1, "party.customerPerson", "firstName", -1);

        Node node2 = NodeParser.parse(
                "<party.customerPerson>contacts[0]<contact.location>address");
        assertNotNull(node2);
        checkNode(node2, "party.customerPerson", "contacts", 0);
        checkNode(node2.getChild(), "contact.location", "address", -1);
    }

    private void checkNode(Node node, String archetype, String name,
                           int index) {
        assertNotNull(node);
        assertEquals(archetype, node.getArchetype());
        assertEquals(name, node.getName());
        assertEquals(index, node.getIndex());
        if (index == -1) {
            assertNull(node.getChild());
        }
    }
}
