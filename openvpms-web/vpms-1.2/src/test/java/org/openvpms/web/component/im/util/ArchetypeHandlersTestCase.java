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
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.AutoQuery;
import org.openvpms.web.component.im.query.EntityQuery;
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
     * The handlers, configured from the properties file.
     */
    private ArchetypeHandlers propertiesHandlers;

    /**
     * The handlers, configured from the XML file.
     */
    private ArchetypeHandlers xmlHandlers;

    /**
     * Test properties.
     */
    private static final String PROPERTIES
            = "org/openvpms/web/component/im/util/"
            + "ArchetypeHandlersTestCase.properties";

    /**
     * Test xml.
     */
    private static final String XML = "org/openvpms/web/component/im/util/"
            + "ArchetypeHandlersTestCase.xml";

    /**
     * Verifies that {@link AutoQuery} can be created for a <em>lookup.*</em>
     * short name.
     *
     * @throws Exception for any error
     */
    public void testCreateAutoQuery() throws Exception {
        checkCreateAutoQuery(propertiesHandlers);
        checkCreateAutoQuery(xmlHandlers);
    }

    /**
     * Verifies that {@link PatientQuery} can be created for a
     * <em>patient.*</em>short name, and that maxRows is configured for 25.
     *
     * @throws Exception for any error
     */
    public void testCreatePatientQuery() throws Exception {
        checkCreatePatientQuery(propertiesHandlers);
        checkCreatePatientQuery(xmlHandlers);
    }

    /**
     * Verifies that no handler is returned if there is no match for the
     * specified archetypes.
     */
    public void testNoMatch() {
        checkNoMatch(propertiesHandlers);
        checkNoMatch(xmlHandlers);
    }

    /**
     * Verifies that no handler is returned if a handler doesn't support
     * the entire range of archetypes.
     */
    public void testNoCompleteMatch() {
        checkNoCompleteMatch(propertiesHandlers);
        checkNoCompleteMatch(xmlHandlers);
    }

    /**
     * Verifies that the correct handler is returned if multiple handlers
     * are registered with the same implementation type.
     */
    public void testSameHandlerImplementationType() {
        checkSameHandlerImplementationType(propertiesHandlers);
        checkSameHandlerImplementationType(xmlHandlers);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        propertiesHandlers = new ArchetypeHandlers<Query>(PROPERTIES,
                                                          Query.class);
        xmlHandlers = new ArchetypeHandlers<Query>(XML, Query.class);
    }

    /**
     * Verifies that {@link AutoQuery} can be created for a <em>lookup.*</em>
     * short name.
     *
     * @param handlers
     * @throws Exception
     */
    private void checkCreateAutoQuery(ArchetypeHandlers handlers)
            throws Exception {
        String[] shortNames = DescriptorHelper.getShortNames("lookup.*");
        ArchetypeHandler lookup = handlers.getHandler("lookup.*");
        assertNotNull(lookup);
        assertEquals(lookup.getType(), AutoQuery.class);
        Query query = (Query) lookup.create(new Object[]{shortNames});
        assertNotNull(query);
    }

    /**
     * Verifies that {@link PatientQuery} can be created for a
     * <em>patient.*</em>short name, and that maxRows is configured for 25.
     *
     * @param handlers the handlers
     * @throws Exception for any error
     */
    private void checkCreatePatientQuery(ArchetypeHandlers handlers)
            throws Exception {
        String[] shortNames = DescriptorHelper.getShortNames("party.patient*");
        ArchetypeHandler patient = handlers.getHandler("party.patient*");
        assertNotNull(patient);
        assertEquals(patient.getType(), PatientQuery.class);
        Query query = (Query) patient.create(
                new Object[]{shortNames, new LocalContext()});
        assertNotNull(query);
        assertEquals(25, query.getMaxResults());
    }

    /**
     * Verifies that no handler is returned if there is no match for the
     * specified archetypes.
     *
     * @param handlers the handlers
     */
    private void checkNoMatch(ArchetypeHandlers handlers) {
        ArchetypeHandler handler1 = handlers.getHandler("act.*");
        assertNull(handler1);

        ArchetypeHandler handler2 = handlers.getHandler(
                new String[]{"act.*", "actRelationship.*"});
        assertNull(handler2);
    }

    /**
     * Verifies that no handler is returned if a handler doesn't support
     * the entire range of archetypes.
     *
     * @param handlers the handlers
     */
    private void checkNoCompleteMatch(ArchetypeHandlers handlers) {
        ArchetypeHandler handler1 = handlers.getHandler("*.*");
        assertNull(handler1);

        ArchetypeHandler handler2 = handlers.getHandler(
                new String[]{"party.patient*", "lookup.*"});
        assertNull(handler2);
    }

    /**
     * Verifies that the correct handler is returned if multiple handlers
     * are registered with the same implementation type.
     *
     * @param handlers the handlers
     */
    private void checkSameHandlerImplementationType(
            ArchetypeHandlers handlers) {
        // make sure the AutoQuery class is returned for lookup.*,
        // security.*
        ArchetypeHandler handler = handlers.getHandler(
                new String[]{"lookup.*", "security.*"});
        assertNotNull(handler);
        assertEquals(handler.getType(), AutoQuery.class);

        // make sure the AutoQuery class is returned for party.organisation*
        ArchetypeHandler org = handlers.getHandler(
                new String[]{"party.organisation*"});
        assertNotNull(org);
        assertEquals(org.getType(), AutoQuery.class);

        // make sure the EntityQuery class is returned for party.customer*,
        // party.organisationOTC
        ArchetypeHandler entity = handlers.getHandler(
                new String[]{"party.organisationOTC", "party.customer*"});
        assertNotNull(entity);
        assertEquals(entity.getType(), EntityQuery.class);

        // make sure no handleris returned for lookup.*, security.*,
        // party.organisation* as the party.organisation* line has a different
        // configuration
        handler = handlers.getHandler(
                new String[]{"lookup.*", "security.*",
                             "party.organisation*"});
        assertNull(handler);
    }

}
