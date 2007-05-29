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

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.doc.DocumentTemplateQuery;
import org.openvpms.web.test.AbstractAppTest;


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
    public void testDefaultQuery() {
        checkCreate("lookup.*", DefaultQuery.class);
    }

    /**
     * Verifies that a {@link EntityQuery} is returned for
     * <em>party.customer*</em> short names.
     */
    public void testCustomerEntityQuery() {
        checkCreate("party.customer*", EntityQuery.class);
    }

    /**
     * Verifies that a {@link PatientQuery} is returned for
     * <em>party.patient*</em> short names.
     */
    public void testPatientQuery() {
        checkCreate("party.patient*", PatientQuery.class);
    }

    /**
     * Verifies that a {@link AutoQuery} is returned for
     * <em>party.organisation*</em> short names.
     */
    public void testOrganisationAutoQuery() {
        checkCreate("party.organisation*", AutoQuery.class);
    }

    /**
     * Verifies that a {@link DocumentTemplateQuery} is returned for
     * <em>entity.documentTemplate*</em> short names.
     */
    public void testDocumentTemplate() {
        checkCreate("entity.documentTemplate*", DocumentTemplateQuery.class);
    }

    /**
     * Verifies that a {@link EntityQuery} is returned for
     * <em>party.supplier*</em> short names.
     */
    public void testSupplierEntityQuery() {
        checkCreate("party.supplier*", EntityQuery.class);
    }

    /**
     * Verifies that a {@link ProductQuery} is returned for
     * <em>product.**</em> short names.
     */
    public void testProductQuery() {
        checkCreate("product.*", ProductQuery.class);
    }

    /**
     * Verifies that the query implementation returned by {@link
     * QueryFactory#create} matches that expected.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @param type      the expected query class
     */
    private void checkCreate(String shortName, Class type) {
        String[] shortNames = {shortName};
        Context context = new LocalContext();
        Query query = QueryFactory.create(shortNames, context);
        assertNotNull("Failed to create Query", query);
        assertEquals(type, query.getClass());
    }
}
