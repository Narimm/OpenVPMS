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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.report.DocumentActReporter;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link DocumentGenerator}.
 *
 * @author Tim Anderson
 */
public class DocumentGeneratorTestCase extends AbstractAppTest {

    /**
     * The document handlers.
     */
    @Autowired
    DocumentHandlers handlers;

    /**
     * Verifies that {@link Context} fields are available to documents generated via {@link DocumentGenerator}.
     *
     * @throws Exception
     */
    @Test
    public void testContextFields() throws Exception {
        Party customer = TestHelper.createCustomer("Foo", "Bar", true);
        Document report = DocumentTestHelper.createDocument("/customerformtemplate.jrxml");
        Entity template = DocumentTestHelper.createDocumentTemplate(CustomerArchetypes.DOCUMENT_FORM, report);
        DocumentAct act = (DocumentAct) create(CustomerArchetypes.DOCUMENT_FORM);
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("customer", customer);
        bean.addNodeParticipation("documentTemplate", template);

        // set up the Context
        Context context = DocumentTestHelper.createReportContext();

        DocumentGenerator.Listener listener = new DocumentGenerator.Listener() {
            @Override
            public void generated(Document document) {
            }

            @Override
            public void cancelled() {
            }

            @Override
            public void skipped() {
            }

            @Override
            public void error() {
            }
        };
        DocumentGenerator generator = new DocumentGenerator(act, context, new HelpContext("foo", null), listener) {
            @Override
            protected Document generate(DocumentActReporter reporter) {
                // generate the document has a CSV to allow comparison
                return reporter.getDocument(DocFormats.CSV_TYPE, false);
            }
        };

        // generate the document, and convert it to a string
        generator.generate();
        Document document = generator.getDocument();
        assertNotNull(document);
        String result = DocumentTestHelper.toString(document, handlers).trim();

        // the customer name should be followed by each of the context fields.
        assertEquals("Foo,Bar,Vets R Us,Main Clinic,Main Stock,\"Smith,J\",Fido,Vet Supplies,Acepromazine,"
                     + "Main Deposit,Main Till,Vet,User,Visit,Invoice,Appointment,Task", result);
    }
}
