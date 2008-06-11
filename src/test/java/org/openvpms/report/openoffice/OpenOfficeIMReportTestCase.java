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

package org.openvpms.report.openoffice;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.ArchetypeServiceTest;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@link OpenOfficeIMReport} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OpenOfficeIMReportTestCase extends ArchetypeServiceTest {

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;


    /**
     * Tests reporting.
     */
    public void testReport() throws IOException {
        File file = new File("src/test/reports/act.customerEstimation.odt");
        Document doc = DocumentHelper.create(file, DocFormats.ODT_TYPE,
                                             handlers);

        IMReport<IMObject> report = new OpenOfficeIMReport<IMObject>(doc,
                                                                     handlers);
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setValue("startTime", java.sql.Date.valueOf("2006-08-04"));
        act.setValue("lowTotal", new BigDecimal("100"));
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects.iterator(),
                                          DocFormats.ODT_TYPE);
        Map<String, String> fields = getFields(result);
        assertEquals("4/08/2006", fields.get("startTime"));  // @todo localise
        assertEquals("$100.00", fields.get("lowTotal"));
        assertEquals("J", fields.get("firstName"));
        assertEquals("Zoo", fields.get("lastName"));
        assertEquals("2.00", fields.get("expression"));
        assertEquals("1234 Foo St\nMelbourne VIC 3001",
                     fields.get("address"));
        assertEquals("Invalid node name: invalid", fields.get("invalid"));
    }

    /**
     * Returns the user fields in a document.
     *
     * @param document an OpenOffice document
     * @return a map of user field names and their corresponding values
     */
    private Map<String, String> getFields(Document document) {
        Map<String, String> fields = new HashMap<String, String>();
        OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
        OOConnection connection = pool.getConnection();
        try {
            OpenOfficeDocument doc = new OpenOfficeDocument(
                    document, connection, handlers);
            for (String name : doc.getUserFieldNames()) {
                fields.put(name, doc.getUserField(name));
            }
        } finally {
            OpenOfficeHelper.close(connection);
        }
        return fields;
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        handlers = (DocumentHandlers) applicationContext.getBean(
                "documentHandlers");
        assertNotNull(handlers);
    }

    /**
     * Tears down the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onTearDown() throws Exception {
        super.onTearDown();
        OOBootstrapService service
                = (OOBootstrapService) applicationContext.getBean(
                "OOSocketBootstrapService");
        service.stop();
    }

}
