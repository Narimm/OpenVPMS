package org.openvpms.web.workspace.reporting;

import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SQLReportPrinter}.
 *
 * @author Tim Anderson
 */
public class SQLReportPrinterTestCase extends AbstractAppTest {

    /**
     * The document handlers.
     */
    @Autowired
    private DocumentHandlers handlers;

    /**
     * Verifies that the {@link Context} is available to SQL reports.
     *
     * @throws Exception for any error
     */
    @Test
    public void testContextFields() throws Exception {
        // set up the context
        Context context = DocumentTestHelper.createReportContext();

        // set up the printer to generate an SQL report
        Document document = DocumentTestHelper.createDocument("/sqlreport.jrxml");
        DocumentTemplate template = new DocumentTemplate((Entity) create("entity.documentTemplate"),
                                                         getArchetypeService());
        SQLReportPrinter printer = new SQLReportPrinter(template, document, context);

        // pass the customer id as a parameter
        Party customer = TestHelper.createCustomer("Foo", "Bar", true);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("customerId", customer.getId());
        printer.setParameters(parameters);

        // generate the report as a CSV to allow comparison
        Document csv = printer.getDocument(DocFormats.CSV_TYPE, false);
        String result = DocumentTestHelper.toString(csv, handlers).trim();

        // the customer name should be followed by each of the context fields.
        assertEquals("Foo,Bar,Vets R Us,Main Clinic,Main Stock,\"Smith,J\",Fido,Vet Supplies,Acepromazine,"
                     + "Main Deposit,Main Till,Vet,User,Visit,Invoice,Appointment,Task", result);
    }


}
