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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.doc.DefaultDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.report.ReportFactory;
import org.openvpms.report.jasper.JRXMLDocumentHandler;
import org.openvpms.report.openoffice.Converter;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link EmailTemplateEvaluator}.
 *
 * @author Tim Anderson
 */
public class EmailTemplateEvaluatorTestCase extends AbstractAppTest {

    /**
     * The template evaluator.
     */
    private EmailTemplateEvaluator evaluator;

    @Before
    public void setUp() {
        super.setUp();
        Converter converter = Mockito.mock(Converter.class);
        evaluator = new EmailTemplateEvaluator(getArchetypeService(), getLookupService(), ServiceHelper.getMacros(),
                                               ServiceHelper.getBean(ReportFactory.class), converter);
    }

    /**
     * Tests a template with TEXT subject and content nodes.
     */
    @Test
    public void testPlainText() {
        Entity template = createTemplate(EmailTemplateEvaluator.TEXT, "Plain text subject &", "Plain\ntext\ncontent &");
        String subject = evaluator.getSubject(template, new Object(), new LocalContext());
        String message = evaluator.getMessage(template, new Object(), new LocalContext());

        assertEquals("Plain text subject &", subject);
        assertEquals("Plain<br/>text<br/>content &amp;", message);
    }

    /**
     * Tests a template with MACRO subject and content nodes.
     */
    @Test
    public void testMacro() {
        Lookup lookup = TestHelper.getLookup("lookup.macro", "@cpname", false);
        IMObjectBean macro = new IMObjectBean(lookup);
        macro.setValue("expression", "concat($customer.firstName, ' & ', $patient.name)");
        macro.save();
        Party customer = TestHelper.createCustomer("Sue", "Smith", false);
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName("Fido");
        Entity template = createTemplate(EmailTemplateEvaluator.MACRO, "Reminder for @cpname", "Dear @cpname\nplease");

        LocalContext context = new LocalContext();
        context.setCustomer(customer);
        context.setPatient(patient);
        String subject = evaluator.getSubject(template, new Object(), context);
        String message = evaluator.getMessage(template, new Object(), context);

        assertEquals("Reminder for Sue & Fido", subject);
        assertEquals("Dear Sue &amp; Fido<br/>please", message);
    }

    /**
     * Tests a template with XPATH subject and content nodes.
     */
    @Test
    public void testXPath() {
        Party customer = TestHelper.createCustomer("Sue", "Smith", false);
        Party patient = TestHelper.createPatient(customer, false);
        patient.setName("Fido");
        Entity template = createTemplate(EmailTemplateEvaluator.XPATH,
                                         "concat('Reminder for ', $customer.firstName, ' & ', $patient.name)",
                                         "concat('Dear ', $customer.firstName, ' & ', $patient.name,$nl,'please')");

        LocalContext context = new LocalContext();
        context.setCustomer(customer);
        context.setPatient(patient);
        String subject = evaluator.getSubject(template, new Object(), context);
        String message = evaluator.getMessage(template, new Object(), context);

        assertEquals("Reminder for Sue & Fido", subject);
        assertEquals("Dear Sue &amp; Fido<br/>please", message);
    }

    /**
     * Tests a template with a static HTML document.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testStaticHTML() throws IOException {
        Entity template = createTemplate(EmailTemplateEvaluator.TEXT, "Reminder", null);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("name", "Z Test static HTML email template");
        bean.setValue("contentType", EmailTemplateEvaluator.DOCUMENT);
        DocumentAct act = (DocumentAct) create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        act.setDescription("dummy");
        ActBean actBean = new ActBean(act);
        actBean.setNodeParticipant("template", template);
        DefaultDocumentHandler handler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT,
                                                                    ServiceHelper.getArchetypeService());
        String html = "<html><body>some html text</body></html>";
        Document document = handler.create("test.html", IOUtils.toInputStream(html, "UTF-8"), "text/html", -1);
        act.setDocument(document.getObjectReference());
        save(template, act, document);

        String subject = evaluator.getSubject(template, new Object(), new LocalContext());
        String message = evaluator.getMessage(template, new Object(), new LocalContext());

        assertEquals("Reminder", subject);
        assertEquals("<html><body>some html text</body></html>", message);
    }

    /**
     * Tests a template with a JasperReport.
     *
     * @throws Exception for any error
     */
    @Test
    public void testJasperReport() throws Exception {
        Entity template = createTemplate(EmailTemplateEvaluator.TEXT, "Reminder", null);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("name", "Z Test JRXML email template");
        bean.setValue("contentType", EmailTemplateEvaluator.DOCUMENT);
        DocumentAct act = (DocumentAct) create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        act.setDescription("dummy");
        ActBean actBean = new ActBean(act);
        actBean.setNodeParticipant("template", template);
        JRXMLDocumentHandler handler = new JRXMLDocumentHandler(getArchetypeService());
        String name = "EmailTemplateEvaluator.jrxml";
        InputStream stream = getClass().getResourceAsStream("/" + name);
        assertNotNull(stream);
        Document document = handler.create(name, stream, "text/xml", -1);
        act.setDocument(document.getObjectReference());
        save(template, act, document);

        Party customer = TestHelper.createCustomer("Joe", "Smith", false);
        String subject = evaluator.getSubject(template, customer, new LocalContext());
        String message = evaluator.getMessage(template, customer, new LocalContext());

        assertEquals("Reminder", subject);
        String expected = IOUtils.toString(getClass().getResourceAsStream("/EmailTemplateEvaluator.expected.html"));
        expected = expected.replaceAll("\r\n", "\n");
        assertEquals(expected, message);
    }

    /**
     * Helper to create an <em>entity.documentTemplateEmail</em>.
     *
     * @param type    the content type for both subject and content nodes.
     * @param subject the subject
     * @param content the content
     * @return a new template
     */
    private Entity createTemplate(String type, String subject, String content) {
        Entity template = (Entity) create(DocumentArchetypes.SYSTEM_EMAIL_TEMPLATE);
        IMObjectBean bean = new IMObjectBean(template);
        bean.setValue("subject", subject);
        bean.setValue("subjectType", type);
        bean.setValue("content", content);
        bean.setValue("contentType", type);
        return template;
    }
}


