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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JasperReport;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.report.AbstractReportTest;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link JasperTemplateLoader}.
 *
 * @author Tim Anderson
 */
public class JasperTemplateLoaderTestCase extends AbstractReportTest {

    @Test
    public void testReportWithSubreports() {
        Document invoice = loadReport("REPORT", "src/test/reports/Test Invoice.jrxml");
        // Note: incorrect archetype, but means that the test invoice won't clash with other test data
        loadReport("SUBREPORT", "src/test/reports/Test Title.jrxml");
        loadReport("SUBREPORT", "src/test/reports/Test Invoice Items.jrxml");
        loadReport("SUBREPORT", "src/test/reports/Test Invoice Reminders.jrxml");
        loadReport("SUBREPORT", "src/test/reports/Test Invoice Notes.jrxml");
        JasperTemplateLoader loader = new JasperTemplateLoader(invoice, getArchetypeService(), getHandlers());
        JasperReport report = loader.getReport();
        assertNotNull(report);
        assertEquals("Invoice", report.getName());
        JasperReport[] subReports = loader.getSubReports();
        assertEquals(4, subReports.length);
        assertEquals("Title", subReports[0].getName());
        assertEquals("Invoice Items", subReports[1].getName());
        assertEquals("Invoice Reminders", subReports[2].getName());
        assertEquals("Invoice Notes", subReports[3].getName());
    }

    /**
     * Loads a JasperReport.
     *
     * @param type the report type
     * @param path the report path
     * @return the report, as a document
     */
    private Document loadReport(String type, String path) {
        TemplateHelper helper = new TemplateHelper(getArchetypeService());
        File file = new File(path);
        DocumentAct act = helper.getDocumentAct(file.getName());
        if (act == null) {
            act = (DocumentAct) create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        }
        ActBean bean = new ActBean(act);
        Document document = getDocument(path, "text/xml");
        Entity template = bean.getNodeParticipant("template");
        if (template == null) {
            template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
            bean.addNodeParticipation("template", template);
        }
        template.setName(document.getName());
        IMObjectBean templateBean = new IMObjectBean(template);
        templateBean.setValue("archetype", type);
        act.setFileName(file.getName());
        act.setDocument(document.getObjectReference());
        act.setDescription(file.getName());
        save(act, template, document);
        return document;
    }
}
