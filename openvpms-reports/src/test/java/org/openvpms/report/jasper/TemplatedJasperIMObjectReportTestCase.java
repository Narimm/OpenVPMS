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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRParameter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jxpath.Functions;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.report.AbstractReportTest;
import org.openvpms.report.DocFormats;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link TemplatedJasperIMObjectReport} class.
 *
 * @author Tim Anderson
 */
public class TemplatedJasperIMObjectReportTestCase extends AbstractReportTest {

    /**
     * Tests the {@link TemplatedJasperIMObjectReport#generate(Iterator, Map, Map, String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testGenerate() throws Exception {
        Party location = (Party) create(PracticeArchetypes.LOCATION);
        location.setName("Main Clinic");
        Document document = getDocument("src/test/reports/party.customerperson.jrxml", DocFormats.XML_TYPE);
        Functions functions = applicationContext.getBean(Functions.class);
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(document, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        Party customer = createCustomer("Foo", "Bar");
        Iterator<IMObject> iterator = Arrays.<IMObject>asList(customer).iterator();

        // verify a field can be supplied
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("Globals.Location", location);

        // generate the report as a CSV to allow comparison
        Document csv = report.generate(iterator, null, fields, DocFormats.CSV_TYPE);
        String string = IOUtils.toString(getHandlers().get(document).getContent(csv), "UTF-8");
        assertEquals("Foo,Bar,Main Clinic", string.trim());
    }

    /**
     * Verifies that SQL reports are supported.
     */
    @Test
    public void testGenerateForSQLQuery() throws Exception {
        Party location = (Party) create(PracticeArchetypes.LOCATION);
        location.setName("Branch Clinic");
        Document document = getDocument("src/test/reports/sqlreport.jrxml", DocFormats.XML_TYPE);
        Functions functions = applicationContext.getBean(Functions.class);
        TemplatedJasperIMObjectReport report = new TemplatedJasperIMObjectReport(document, getArchetypeService(),
                                                                                 getLookupService(), getHandlers(),
                                                                                 functions);
        Party customer = createCustomer("Foo", "Bar");

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("customerId", customer.getId());
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        Connection connection = dataSource.getConnection();
        parameters.put(JRParameter.REPORT_CONNECTION, connection);

        // verify a field can be supplied
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("Globals.Location", location);

        // generate the report as a CSV to allow comparison
        Document csv = report.generate(parameters, fields, DocFormats.CSV_TYPE);
        String string = IOUtils.toString(getHandlers().get(document).getContent(csv), "UTF-8");
        assertEquals("Foo,Bar,Branch Clinic", string.trim());
        connection.close();
    }

}
