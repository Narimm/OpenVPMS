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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.organisation;

import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandler;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandlerFactory;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.admin.job.JobDeletionHandler;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link OrganisationLocationDeletionHandler}.
 *
 * @author Tim Anderson
 */
public class OrganisationLocationDeletionHandlerTestCase extends AbstractAppTest {

    /**
     * Verifies that a location with a logo can be deleted, and the logo is also removed.
     */
    @Test
    public void testDelete() {
        Party location = TestHelper.createLocation();
        Document document = DocumentTestHelper.createDocument("/blank.jrxml");
        DocumentAct act = (DocumentAct) create(DocumentArchetypes.LOGO_ACT);
        act.setDocument(document.getObjectReference());
        ActBean bean = new ActBean(act);
        bean.addParticipation(DocumentArchetypes.LOGO_PARTICIPATION, location);
        save(location, act, document);

        OrganisationLocationDeletionHandler handler = createDeletionHandler(location);
        assertTrue(handler.canDelete());

        handler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(location));
        assertNull(get(document));
        assertNull(get(act));
    }

    /**
     * Verifies that the {@link IMObjectDeletionHandlerFactory} returns {@link JobDeletionHandler} for jobs.
     */
    @Test
    public void testFactory() {
        IMObjectDeletionHandlerFactory factory = new IMObjectDeletionHandlerFactory(getArchetypeService());
        factory.setApplicationContext(applicationContext);

        Party location = TestHelper.createLocation();
        IMObjectDeletionHandler<Party> handler = factory.create(location);
        assertTrue(handler instanceof OrganisationLocationDeletionHandler);
        assertTrue(handler.canDelete());

        handler.delete(new LocalContext(), new HelpContext("foo", null));
        assertNull(get(location));
    }

    /**
     * Creates a new deletion handler for a location.
     *
     * @param location the location
     * @return a new deletion handler
     */
    protected OrganisationLocationDeletionHandler createDeletionHandler(Party location) {
        IMObjectEditorFactory factory = applicationContext.getBean(IMObjectEditorFactory.class);
        return new OrganisationLocationDeletionHandler(location, factory, ServiceHelper.getTransactionManager(),
                                                       ServiceHelper.getArchetypeService());
    }

}