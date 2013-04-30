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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentTemplateQuery;
import org.openvpms.web.component.im.product.ProductQuery;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * {@link QueryFactory} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class QueryFactoryTestCase extends AbstractAppTest {

    /**
     * Verifies that {@link DefaultQuery} is returned when no other class is
     * specified.
     */
    @Test
    public void testDefaultQuery() {
        checkCreate("lookup.*", DefaultQuery.class, Lookup.class);
    }

    /**
     * Verifies that a {@link CustomerQuery} is returned for
     * <em>party.customer*</em> short names.
     */
    @Test
    public void testCustomerQuery() {
        checkCreate("party.customer*", CustomerQuery.class, Party.class);
    }

    /**
     * Verifies that a {@link PatientQuery} is returned for
     * <em>party.patient*</em> short names.
     */
    @Test
    public void testPatientQuery() {
        checkCreate("party.patient*", PatientQuery.class, Party.class);
    }

    /**
     * Verifies that a {@link EntityQuery} is returned for
     * <em>party.organisation*</em> short names, except
     * <em>party.organisationOTC</em> which returns {@link CustomerQuery}.
     */
    @Test
    public void testOrganisationQuery() {
        checkCreate("party.organisationOTC", CustomerQuery.class, Party.class);
        String[] shortNames
            = DescriptorHelper.getShortNames("party.organisationOTC");
        for (String shortName : shortNames) {
            if (shortName.equals("party.organisationOTC")) {
                checkCreate(shortName, CustomerQuery.class, Party.class);
            } else {
                checkCreate(shortName, EntityQuery.class, Party.class);
            }
        }
    }

    /**
     * Verifies that a {@link DocumentTemplateQuery} is returned for
     * <em>entity.documentTemplate*</em> short names.
     */
    @Test
    public void testDocumentTemplate() {
        checkCreate("entity.documentTemplate*", DocumentTemplateQuery.class,
                    Entity.class);
    }

    /**
     * Verifies that a {@link EntityQuery} is returned for
     * <em>party.supplier*</em> short names.
     */
    @Test
    public void testSupplierEntityQuery() {
        checkCreate("party.supplier*", EntityQuery.class, Party.class);
    }

    /**
     * Verifies that a {@link ProductQuery} is returned for
     * <em>product.**</em> short names.
     */
    @Test
    public void testProductQuery() {
        checkCreate("product.*", ProductQuery.class, Product.class);
    }

    /**
     * Verifies that the query implementation returned by {@link
     * QueryFactory#create} matches that expected.
     *
     * @param shortName  the archetype short name. May contain wildcards
     * @param type       the expected query class
     * @param resultType the expected result type class
     */
    private void checkCreate(String shortName, Class type, Class resultType) {
        String[] shortNames = {shortName};
        Context context = new LocalContext();
        Query query = QueryFactory.create(shortNames, context, resultType);
        assertNotNull("Failed to create Query", query);
        assertEquals(type, query.getClass());
        assertEquals(resultType, query.getType());
    }
}
