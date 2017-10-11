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

package org.openvpms.web.component.im.doc;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandler;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandlerFactory;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link DocumentTemplateDeletionHandler}.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateDeletionHandlerTestCase extends AbstractAppTest {

    /**
     * The <em>entity.documentTemplate</em>
     */
    private Entity template;

    /**
     * The document content.
     */
    private Document document;

    /**
     * The act linking the template and document.
     */
    private DocumentAct act;

    /**
     * Sets up the test case.
     */
    @Override
    @Before
    public void setUp() {
        super.setUp();
        // create a template with associated act.documentTemplate
        template = DocumentTestHelper.createDocumentTemplate(PatientArchetypes.DOCUMENT_FORM, "zblank");
        document = DocumentTestHelper.createDocument("/blank.jrxml");
        act = DocumentTestHelper.createDocumentTemplate(template, document);
    }

    /**
     * Verifies that a job with relationships can be deleted.
     */
    @Test
    public void testDelete() {
        IMObjectDeletionHandler handler = createDeletionHandler(template);
        assertTrue(handler.canDelete());
        handler.delete(new LocalContext(), new HelpContext("foo", null));

        assertNull(get(template));
        assertNull(get(document));
        assertNull(get(act));
    }

    /**
     * Verifies jobs can be deactivated.
     */
    @Test
    public void testDeactivate() {
        assertTrue(template.isActive());
        IMObjectDeletionHandler handler = createDeletionHandler(template);
        handler.deactivate();

        assertFalse(get(template).isActive());

        // deactivation doesn't propagate
        assertTrue(get(document).isActive());
        assertTrue(get(act).isActive());
    }

    /**
     * Verifies that the {@link IMObjectDeletionHandlerFactory} returns {@link DocumentTemplateDeletionHandler} for
     * templates.
     */
    @Test
    public void testFactory() {
        IMObjectDeletionHandlerFactory factory = new IMObjectDeletionHandlerFactory(getArchetypeService());
        factory.setApplicationContext(applicationContext);

        IMObjectDeletionHandler<Entity> handler = factory.create(template);
        assertTrue(handler instanceof DocumentTemplateDeletionHandler);
        handler.delete(new LocalContext(), new HelpContext("foo", null));

        assertNull(get(template));
        assertNull(get(document));
        assertNull(get(act));
    }

    /**
     * Verifies that <em>entity.documentTemplate</em> can't be deleted if it has participations
     * other than <em>participation.document</em>.
     */
    @Test
    public void testDeleteWithParticipations() {
        Act form = (Act) create(PatientArchetypes.DOCUMENT_FORM);
        Party patient = TestHelper.createPatient();
        ActBean bean = new ActBean(form);
        bean.setNodeParticipant("patient", patient);
        bean.setNodeParticipant("documentTemplate", template);
        save(form);

        IMObjectDeletionHandler handler = createDeletionHandler(template);
        assertFalse(handler.canDelete());

        try {
            handler.delete(new LocalContext(), new HelpContext("foo", null));
            fail("Expected IllegalStateException to be thrown");
        } catch (IllegalStateException expected) {
            // do nothing
        }

        // verify nothing was deleted
        assertNotNull(get(template));
        assertNotNull(get(document));
        assertNotNull(get(act));
        assertNotNull(get(form));

        // verify it can be deactivated
        handler.deactivate();
        assertFalse(get(template).isActive());

        // deactivation doesn't propagate
        assertTrue(get(document).isActive());
        assertTrue(get(act).isActive());
    }

    /**
     * Verifies that a template can be deleted if it has links to email and sms templates.
     */
    @Test
    public void testDeleteWithLinks() {
        Entity email = DocumentTestHelper.createEmailTemplate("foo", "bar");
        Entity sms = ReminderTestHelper.createSMSTemplate("TEXT", "text");
        IMObjectBean bean = new IMObjectBean(template);
        bean.addNodeTarget("email", email);
        bean.addNodeTarget("sms", sms);
        bean.save();

        IMObjectDeletionHandler handler = createDeletionHandler(template);
        assertTrue(handler.canDelete());
        handler.delete(new LocalContext(), new HelpContext("foo", null));

        // verify the template were deleted...
        assertNull(get(template));
        assertNull(get(document));
        assertNull(get(act));

        // ... but the targets weren't
        assertNotNull(get(email));
        assertNotNull(get(sms));
    }

    /**
     * Creates a new deletion handler for a job.
     *
     * @param job the job
     * @return a new deletion handler
     */
    protected DocumentTemplateDeletionHandler createDeletionHandler(Entity job) {
        IMObjectEditorFactory factory = applicationContext.getBean(IMObjectEditorFactory.class);
        return new DocumentTemplateDeletionHandler(job, factory, ServiceHelper.getTransactionManager(),
                                                   ServiceHelper.getArchetypeService());
    }

}
