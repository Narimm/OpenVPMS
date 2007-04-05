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
 * Tests the {@link NodeParser}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeParserTestCase extends TestCase {

    /**
     * Tests a single node.
     */
    public void testSingleNode() {
        Node node = NodeParser.parse("<party.customerperson>firstName");
        assertNotNull(node);
        checkNode(node, "party.customerperson", "firstName", -1);
        assertEquals("<party.customerperson>firstName", node.getNodePath());
        assertEquals("<party.customerperson>", node.getObjectPath());
    }

    /**
     * Tests a collection node.
     */
    public void testCollectionNode() {
        Node node = NodeParser.parse(
                "<party.customerperson>contacts[0]<contact.location>address");
        assertNotNull(node);
        checkNode(node, "party.customerperson", "contacts", 0);
        checkNode(node.getChild(), "contact.location", "address", -1);

        assertEquals("<party.customerperson>contacts[0]", node.getNodePath());
        assertEquals("<party.customerperson>", node.getObjectPath());
        assertEquals(
                "<party.customerperson>contacts[0]<contact.location>address",
                node.getChild().getNodePath());
        assertEquals("<party.customerperson>contacts[0]<contact.location>",
                     node.getChild().getObjectPath());
    }

    /**
     * Tests that invalid nodes can't be parsed.
     */
    public void testInvalid() {
        assertNull(NodeParser.parse(""));
        assertNull(NodeParser.parse("<party.customerperson>"));
        assertNull(NodeParser.parse("<party.customerperson>[0]"));
        assertNull(NodeParser.parse("<party.customerperson>xnode[0]ynode"));
    }

    /**
     * Checks a node.
     *
     * @param node      the node to check
     * @param archetype the expected archetype
     * @param name      the expected name
     * @param index     the expected index
     */
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
