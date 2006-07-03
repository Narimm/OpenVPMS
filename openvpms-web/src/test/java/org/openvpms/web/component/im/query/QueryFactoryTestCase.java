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

import org.openvpms.web.test.AbstractAppTest;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.util.List;


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
        checkCreate("party.customerperson", DefaultQuery.class);
    }

    /**
     * Verifies that a {@link AutoQuery} is returned for
     * <em>classification.*</em> short names.
     */
    public void testClassificationsAutoQuery() {
        checkCreate("classification.*", AutoQuery.class);
    }

    /**
     * Verifies that a {@link AutoQuery} is returned for <em>lookup.*</em> short
     * names.
     */
    public void testLookupsAutoQuery() {
        checkCreate("lookup.*", AutoQuery.class);
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
     * Verifies that a {@link AutoQuery} is returned for
     * <em>party.supplier*</em> short names.
     */
    public void testSupplierAutoQuery() {
        checkCreate("party.supplier*", AutoQuery.class);
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
        Query query = QueryFactory.create(shortNames);
        assertNotNull("Failed to create Query", query);
        assertEquals(type, query.getClass());

        // now try via the archetype long name  method
        List<ArchetypeDescriptor> archetypes
                = DescriptorHelper.getArchetypeDescriptors(shortNames);
        for (ArchetypeDescriptor archetype : archetypes) {
            ArchetypeId id = archetype.getType();
            query = QueryFactory.create(id.getRmName(), id.getEntityName(),
                                        id.getConcept());
            assertNotNull("Failed to create Query", query);
            assertEquals(type, query.getClass());
        }
    }
}
