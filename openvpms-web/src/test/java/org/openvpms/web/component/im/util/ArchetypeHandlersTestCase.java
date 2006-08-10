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

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.query.AutoQuery;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.test.AbstractAppTest;


/**
 * {@link ArchetypeHandlers} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ArchetypeHandlersTestCase extends AbstractAppTest {

    /**
     * The handlers.
     */
    private ArchetypeHandlers _handlers;

    /**
     * Test properties.
     */
    private static final String PROPERTIES
            = "org/openvpms/web/component/im/util/"
            + "ArchetypeHandlersTestCase.properties";


    /**
     * Verifies that {@link AutoQuery} can be created for a <em>lookup.*</em>
     * short name.
     *
     * @throws Exception for any error
     */
    public void testCreateAutoQuery() throws Exception {
        String[] shortNames = DescriptorHelper.getShortNames("lookup.*");
        ArchetypeHandler lookup = _handlers.getHandler("lookup.*");
        assertNotNull(lookup);
        assertEquals(lookup.getType(), AutoQuery.class);
        Query query = (Query) lookup.create(new Object[]{shortNames});
        assertNotNull(query);
    }

    /**
     * Verifies that {@link PatientQuery} can be created for a
     * <em>patient.*</em>short name, and that maxRows is configured for 25.
     */
    public void testCreatePatientQuery() throws Exception {
        String[] shortNames = DescriptorHelper.getShortNames("party.patient*");
        ArchetypeHandler patient = _handlers.getHandler("party.patient*");
        assertNotNull(patient);
        assertEquals(patient.getType(), PatientQuery.class);
        Query query = (Query) patient.create(new Object[]{shortNames});
        assertNotNull(query);
        assertEquals(25, query.getMaxRows());
    }

    /**
     * Verifies that no handler is returned if there is no match for the
     * specified archetypes.
     */
    public void testNoMatch() {
        ArchetypeHandler handler1 = _handlers.getHandler("act.*");
        assertNull(handler1);

        ArchetypeHandler handler2 = _handlers.getHandler(
                new String[]{"act.*", "actRelationship.*"});
        assertNull(handler2);
    }

    /**
     * Verifies that no handler is returned if a handler doesn't support
     * the entire range of archetypes.
     */
    public void testNoCompleteMatch() {
        ArchetypeHandler handler1 = _handlers.getHandler("*.*");
        assertNull(handler1);

        ArchetypeHandler handler2 = _handlers.getHandler(
                new String[]{"party.patient*", "lookup.*"});
        assertNull(handler2);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        _handlers = new ArchetypeHandlers(PROPERTIES, Query.class);
    }
}
