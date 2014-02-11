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

package org.openvpms.web.component.im.lookup;

import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link NodeLookupQuery} class.
 *
 * @author Tim Anderson
 */
public class NodeLookupQueryTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link NodeLookupQuery#getLookups()} method when constructed using the
     * {@link NodeLookupQuery#NodeLookupQuery(String, String)} constructor.
     */
    @Test
    public void testGetLookupsForNodeName() {
        NodeLookupQuery query = new NodeLookupQuery(CustomerArchetypes.PERSON, "title");
        checkLookups(query);
    }

    /**
     * Tests the {@link NodeLookupQuery#getLookups()} method when constructed using the
     * {@link NodeLookupQuery#NodeLookupQuery(IMObject, NodeDescriptor)} constructor.
     */
    @Test
    public void testGetLookupsForDescriptor() {
        Party customer = TestHelper.createCustomer(false);
        ArchetypeDescriptor archetype = getArchetypeService().getArchetypeDescriptor(customer.getArchetypeId());
        NodeDescriptor descriptor = archetype.getNodeDescriptor("title");
        assertNotNull(descriptor);
        NodeLookupQuery query = new NodeLookupQuery(customer, descriptor);
        checkLookups(query);
    }

    /**
     * Tests the {@link NodeLookupQuery#getLookups()} method.
     *
     * @param query the query to check
     */
    private void checkLookups(NodeLookupQuery query) {
        Collection<Lookup> titles = LookupServiceHelper.getLookupService().getLookups("lookup.personTitle");
        List<Lookup> expected = new ArrayList<Lookup>();
        for (Lookup title : titles) {
            if (title.isActive()) {
                expected.add(title);
            }
        }

        List<Lookup> actual = query.getLookups();

        assertEquals(expected.size(), actual.size());
        for (Lookup l : expected) {
            assertTrue(actual.contains(l));
        }
    }

}
