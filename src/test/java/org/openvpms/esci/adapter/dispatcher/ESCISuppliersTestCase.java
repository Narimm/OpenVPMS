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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.dispatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.esci.adapter.AbstractESCITest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Tests the {@link ESCISuppliers} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ESCISuppliersTestCase extends AbstractESCITest {

    /**
     * Tests the {@link ESCISuppliers#getSuppliers()} method.
     */
    @Test
    public void testGetSuppliers() {
        ESCISuppliers helper = new ESCISuppliers(getArchetypeService());
        List<Party> before = helper.getSuppliers();
        assertNotNull(before);
        Party supplier = getSupplier();

        List<Party> after = helper.getSuppliers();
        assertEquals(before.size(), after.size());
        assertFalse(after.contains(supplier));

        Party stockLocation = getStockLocation();
        addESCIConfiguration(supplier, stockLocation, "https://localhost:8443/foo/bar");

        after = helper.getSuppliers();
        assertEquals(before.size() + 1, after.size());
        assertTrue(after.contains(supplier));

        supplier.setActive(false);
        save(supplier);
        after = helper.getSuppliers();
        assertEquals(before.size(), after.size());
        assertFalse(after.contains(supplier));
    }

    /**
     * Tests the {@link ESCISuppliers#getESCIRelationships(Party)} method.
     */
    @Test
    public void testGetESCIRelationships() {
        Party supplier = getSupplier();
        ESCISuppliers suppliers = new ESCISuppliers(getArchetypeService());

        // make sure no relationships returned
        Collection<EntityRelationship> relationships = suppliers.getESCIRelationships(supplier);
        assertTrue(relationships.isEmpty());

        // now add some relationships
        Party stockLocation1 = createStockLocation();
        Party stockLocation2 = createStockLocation();
        String url1 = "https://url1";
        String url2 = "https://url2";
        EntityRelationship config1 = addESCIConfiguration(supplier, stockLocation1, url1);
        addESCIConfiguration(supplier, stockLocation2, url2);

        // make sure both are returned
        relationships = suppliers.getESCIRelationships(supplier);
        assertEquals(2, relationships.size());
        List<String> urls = getURLS(relationships);
        assertTrue(urls.contains(url1));
        assertTrue(urls.contains(url2));

        // deactivate the first relationship, and verify it is no longer returned
        config1.setActiveEndTime(new Date(System.currentTimeMillis() - 1000));
        save(supplier, stockLocation1);
        relationships = suppliers.getESCIRelationships(supplier);
        assertEquals(1, relationships.size());
        urls = getURLS(relationships);
        assertFalse(urls.contains(url1));
        assertTrue(urls.contains(url2));
    }

    /**
     * Helper to return a list of serviceURLs from a collection of
     * <em>entityRelationship.supplierStockLocationESCI</em>.
     *
     * @param relationships the relationships
     * @return the corresponding serviceURLs
     */
    private List<String> getURLS(Collection<EntityRelationship> relationships) {
        List<String> result = new ArrayList<String>();
        for (EntityRelationship config : relationships) {
            IMObjectBean bean = new IMObjectBean(config);
            result.add(bean.getString("serviceURL"));
        }
        return result;
    }
}
