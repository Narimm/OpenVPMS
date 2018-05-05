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

package org.openvpms.report.tools;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.report.AbstractReportTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link TemplateLoader}.
 *
 * @author Tim Anderson
 */
public class TemplateLoaderTestCase extends AbstractReportTest {

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // need to remove old templates
        removeTemplate("Test Invoice.jrxml");
        removeTemplate("Test Invoice Items.jrxml");
        removeTemplate("Test Invoice Notes.jrxml");
        removeTemplate("Test Invoice Reminders.jrxml");
        removeTemplate("Test Title.jrxml");
    }

    /**
     * Tests loading templates.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLoadDocumentTemplate() throws Exception {
        TemplateLoader loader = new TemplateLoader(getArchetypeService(), handlers);
        loader.load("src/test/reports/templates.xml");

        checkTemplate("Test Invoice", "Invoice template", "act.customerAccountChargesInvoice", "Test Invoice.jrxml",
                      "text/xml", DocumentArchetypes.DEFAULT_DOCUMENT, null);
        checkTemplate("Test Invoice Items", "Invoice Items template", "SUBREPORT", "Test Invoice Items.jrxml",
                      "text/xml", DocumentArchetypes.DEFAULT_DOCUMENT, null);
    }

    /**
     * Tests loading email templates.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLoadEmailTemplates() throws Exception {
        TemplateLoader loader = new TemplateLoader(getArchetypeService(), handlers);
        loader.load("src/test/reports/email-templates.xml");

        Entity email1 = checkEmailTemplate("Test email 1", "Email template", "Email subject 1", "TEXT",
                                           "Test Invoice Notes.jrxml", "text/xml",
                                           DocumentArchetypes.USER_EMAIL_TEMPLATE);
        Entity email2 = checkEmailTemplate("Test email 2", "Email template", "Email subject 2", "TEXT",
                                           "Test Title.jrxml", "text/xml", DocumentArchetypes.SYSTEM_EMAIL_TEMPLATE);
        checkTemplate("Test template with email 1", "Invoice template", "act.customerAccountChargesInvoice",
                      "Test Invoice.jrxml", "text/xml", DocumentArchetypes.DEFAULT_DOCUMENT, email1);
        checkTemplate("Test template with email 2", "Invoice template", "act.customerAccountChargesInvoice",
                      "Test Invoice Reminders.jrxml", "text/xml", DocumentArchetypes.DEFAULT_DOCUMENT, email2);
    }

    /**
     * Verifies an entity.documentTemplate matches that expected.
     *
     * @param name          the template name
     * @param description   the expected template description
     * @param archetype     the expected 'archetype' (see entity.documentTemplate/archetype)
     * @param fileName      the expected template file name
     * @param mimeType      the expected template mime type
     * @param docType       the expected template document archetype
     * @param emailTemplate the expected email template. May be {@code null}
     */
    private void checkTemplate(String name, String description, String archetype, String fileName, String mimeType,
                               String docType, Entity emailTemplate) {
        Entity template = getTemplate(name, DocumentArchetypes.DOCUMENT_TEMPLATE);
        assertNotNull(template);
        assertEquals(name, template.getName());
        assertEquals(description, template.getDescription());
        IMObjectBean bean = new IMObjectBean(template);
        assertEquals(archetype, bean.getString("archetype"));
        assertEquals(bean.getNodeTargetObject("email"), emailTemplate);
        checkDocument(template, fileName, mimeType, docType);
    }

    private Entity checkEmailTemplate(String name, String description, String subject, String subjectType,
                                      String fileName, String mimeType, String emailType) {
        Entity template = getTemplate(name, emailType);
        assertNotNull(template);
        assertEquals(name, template.getName());
        assertEquals(description, template.getDescription());
        IMObjectBean bean = new IMObjectBean(template);
        assertEquals(subject, bean.getString("subject"));
        assertEquals(subjectType, bean.getString("subjectType"));
        checkDocument(template, fileName, mimeType, DocumentArchetypes.DEFAULT_DOCUMENT);
        return template;
    }

    private void checkDocument(Entity template, String fileName, String mimeType, String docType) {
        TemplateHelper helper = new TemplateHelper(getArchetypeService());
        DocumentAct act = helper.getDocumentAct(template);
        assertNotNull(act);
        assertEquals(fileName, act.getName());
        assertEquals(mimeType, act.getMimeType());
        assertNotNull(act.getDocument());
        Document document = (Document) get(act.getDocument());
        assertNotNull(document);
        assertEquals(docType, document.getArchetypeId().getShortName());
        assertEquals(fileName, document.getName());
        assertEquals(mimeType, document.getMimeType());
    }


    private Entity getTemplate(String name, String type) {
        ArchetypeQuery query = new ArchetypeQuery(type, false);
        query.add(Constraints.eq("name", name));
        query.add(Constraints.sort("id", false));
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    private void removeTemplate(String name) {
        ArchetypeQuery query = new ArchetypeQuery(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT, false);
        query.add(Constraints.eq("fileName", name));
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            DocumentAct act = iterator.next();
            ActBean bean = new ActBean(act);
            Entity template = bean.getNodeParticipant("template");
            Document document = (Document) get(act.getDocument());
            remove(act);
            if (document != null) {
                remove(document);
            }
            remove(template);
        }
    }
}
