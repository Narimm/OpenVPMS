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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.Functions;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.report.AbstractReportTest;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ReportException;
import org.openvpms.report.jasper.function.EvaluateFunction;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link TemplatedJasperIMObjectReport} class.
 *
 * @author Tim Anderson
 */
public class TemplatedJasperIMObjectReportTestCase extends AbstractReportTest {

    /**
     * The functions.
     */
    @Autowired
    private Functions functions;

    /**
     * Tests the {@link TemplatedJasperIMObjectReport#generate(Iterable, Map, Map, String)} method.
     * <p>
     * This verifies that a parameter may be passed and accessed using the {@link EvaluateFunction#EVALUATE(String)}
     * method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGenerate() throws Exception {
        Party location = (Party) create(PracticeArchetypes.LOCATION);
        location.setName("Main Clinic");
        Document document = getDocument("src/test/reports/party.customerperson.jrxml", DocFormats.XML_TYPE);
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(document, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        Party customer = createCustomer("Foo", "Bar");
        List<IMObject> list = Collections.<IMObject>singletonList(customer);

        // verify a field can be supplied
        Map<String, Object> fields = new HashMap<>();
        fields.put("Globals.Location", location);

        // generate the report as a CSV to allow comparison
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "Hello"); // this is passed to the report, and accessed using EVALUATE()
        Document csv = report.generate(list, parameters, fields, DocFormats.CSV_TYPE);
        String string = IOUtils.toString(getHandlers().get(document).getContent(csv), "UTF-8");
        assertEquals("Foo,Bar,Main Clinic,Hello", string.trim());
    }

    /**
     * Verifies that SQL reports are supported.
     */
    @Test
    public void testGenerateForSQLQuery() throws Exception {
        Party location = (Party) create(PracticeArchetypes.LOCATION);
        location.setName("Branch Clinic");
        Document document = getDocument("src/test/reports/sqlreport.jrxml", DocFormats.XML_TYPE);
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(document, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        Party customer = createCustomer("Foo", "Bar");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("customerId", customer.getId());
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        Connection connection = dataSource.getConnection();
        parameters.put(JRParameter.REPORT_CONNECTION, connection);

        // verify a field can be supplied
        Map<String, Object> fields = new HashMap<>();
        fields.put("Globals.Location", location);

        // generate the report as a CSV to allow comparison
        Document csv = report.generate(parameters, fields, DocFormats.CSV_TYPE);
        String string = IOUtils.toString(getHandlers().get(document).getContent(csv), "UTF-8");
        assertEquals("Foo,Bar,Branch Clinic", string.trim());
        connection.close();
    }

    /**
     * Verifies that sub-reports can be loaded at evaluation time.
     */
    @Test
    public void testReportWithSubReports() {
        Document template = loadReport("REPORT", "src/test/reports/Test Invoice.jrxml");
        // Note: incorrect archetype, but means that the test invoice won't clash with other test data
        loadReport("SUBREPORT", "src/test/reports/Test Title.jrxml");
        loadReport("SUBREPORT", "src/test/reports/Test Invoice Items.jrxml");
        loadReport("SUBREPORT", "src/test/reports/Test Invoice Reminders.jrxml");
        loadReport("SUBREPORT", "src/test/reports/Test Invoice Notes.jrxml");
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(template, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        JasperTemplateLoader loader = report.getLoader();
        assertEquals(0, loader.getSubReports().size());

        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(
                BigDecimal.TEN, TestHelper.createCustomer(), TestHelper.createPatient(), TestHelper.createProduct(),
                ActStatus.POSTED);
        save(invoice);
        Document document = report.generate(Collections.singletonList((IMObject) invoice.get(0)));
        assertNotNull(document);

        Map<String, JasperReport> subReports = loader.getSubReports();
        assertEquals(4, subReports.size());
        assertTrue(subReports.containsKey("Test Title.jrxml"));
        assertTrue(subReports.containsKey("Test Invoice Items.jrxml"));
        assertTrue(subReports.containsKey("Test Invoice Reminders.jrxml"));
        assertTrue(subReports.containsKey("Test Invoice Notes.jrxml"));
    }

    /**
     * Verifies that an exception is thrown if a sub-report cannot be found.
     */
    @Test
    public void testMissingSubReport() {
        Document template = loadReport("REPORT", "src/test/reports/missingsubreport.jrxml");
        Functions functions = applicationContext.getBean(Functions.class);
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(template, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(
                BigDecimal.TEN, TestHelper.createCustomer(), TestHelper.createPatient(), TestHelper.createProduct(),
                ActStatus.POSTED);
        save(invoice);
        try {
            report.generate(Collections.singletonList((IMObject) invoice.get(0)));
            fail("Expected report generation to fail");
        } catch (ReportException expected) {
            assertEquals(ReportException.ErrorCode.FailedToFindSubReport, expected.getErrorCode());
            assertEquals("There is no sub-report named: No Such Subreport.jrxml\n" +
                         "This is needed by report: missingsubreport.jrxml", expected.getMessage());
        }
    }

    /**
     * Verifies that a letterhead sub-report can be loaded based on fields.
     */
    @Test
    public void testLetterhead() {
        Document template = getDocument("src/test/reports/letterheadtest.jrxml", DocFormats.XML_TYPE);
        loadReport("SUBREPORT", "src/test/reports/letterhead-A4.jrxml");
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(template, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        JasperTemplateLoader loader = report.getLoader();
        assertEquals(0, loader.getSubReports().size());

        Party location = TestHelper.createLocation();
        IMObject letterhead = create(DocumentArchetypes.LETTERHEAD);
        IMObjectBean letterheadBean = new IMObjectBean(letterhead);
        letterheadBean.setValue("name", "X Letterhead");
        letterheadBean.setValue("logoFile", "logo.png");
        letterheadBean.setValue("subreport", "letterhead-A4.jrxml");
        IMObjectBean bean = new IMObjectBean(location);
        bean.addNodeTarget("letterhead", letterhead);
        save(location, letterhead);

        Map<String, Object> fields = new HashMap<>();
        fields.put("OpenVPMS.location", location);

        Party customer = TestHelper.createCustomer();
        Document document = report.generate(Collections.singletonList((IMObject) customer),
                                            Collections.<String, Object>emptyMap(), fields);
        assertNotNull(document);

        Map<String, JasperReport> subReports = loader.getSubReports();
        assertEquals(1, subReports.size());
        assertTrue(subReports.containsKey("letterhead-A4.jrxml"));
    }

    /**
     * Verify that when a report has an unsupported font, an exception is thrown that includes the template name.
     */
    @Test
    public void testInvalidFont() {
        Document template = loadReport("REPORT", "src/test/reports/invalidfont.jrxml");
        Functions functions = applicationContext.getBean(Functions.class);
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(template, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        Party patient = TestHelper.createPatient(false);
        try {
            report.generate(Collections.<IMObject>singletonList(patient));
            fail("Expected report generation to fail");
        } catch (ReportException expected) {
            assertEquals(ReportException.ErrorCode.FailedToGenerateReport, expected.getErrorCode());
            assertEquals("Failed to generate report invalidfont.jrxml: Font \"BadFont\" is not available to the JVM. " +
                         "See the Javadoc for more details.", expected.getMessage());
        }
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
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        bean.setNodeParticipant("template", template);
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
