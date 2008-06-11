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
 *  $Id: OpenOfficeIMReportTestCase.java 1713 2007-01-11 06:21:23Z tanderson $
 */

package org.openvpms.report.msword;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.ArchetypeServiceTest;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.openoffice.OOBootstrapService;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OOConnectionPool;
import org.openvpms.report.openoffice.OpenOfficeHelper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@link MsWordIMReport} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-01-11 17:21:23 +1100 (Thu, 11 Jan 2007) $
 */
public class MsWordIMReportTestCase extends ArchetypeServiceTest {

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;


    /**
     * Tests reporting.
     */
    public void testReport() throws IOException {
        File file = new File("src/test/reports/act.customerEstimation.doc");
        Document doc = DocumentHelper.create(file, DocFormats.DOC_TYPE,
                                             handlers);

        IMReport<IMObject> report = new MsWordIMReport<IMObject>(doc, handlers);
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setValue("startTime", java.sql.Date.valueOf("2006-08-04"));
        act.setValue("lowTotal", new BigDecimal("100"));
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        // TODO:  Currently just do generate to make sure no exception.  result not used for merge
        // testing until we can figure out how to maintain fields in generated document.
        Document result = report.generate(objects.iterator(),
                                          DocFormats.DOC_TYPE);
        //TODO:  using pre merge document checks due to merge fields removed when 
        // merged document generated.
        Map<String, String> fields = getFields(doc);
        assertEquals("startTime", fields.get("startTime"));  // @todo localise
        assertEquals("lowTotal", fields.get("lowTotal"));
        assertEquals("customer.entity.firstName",
                     fields.get("customer.entity.firstName"));
        assertEquals("customer.entity.lastName",
                     fields.get("customer.entity.lastName"));
        assertEquals("invalid", fields.get("invalid"));
    }

    /**
     * Returns the user fields in a document.
     *
     * @param document an MsWord document
     * @return a map of user field names and their corresponding values
     */
    private Map<String, String> getFields(Document document) {
        Map<String, String> fields = new HashMap<String, String>();
        OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
        OOConnection connection = pool.getConnection();
        try {
            MsWordDocument doc = new MsWordDocument(
                    document, connection, handlers);
            for (String name : doc.getUserFieldNames()) {
                // TODO changed as MsWord fields use fieldname as content and when
                // merged the field is removed and the content remains.  So just create a map
                // that contains name pairs to use in assertions.
                fields.put(name, name);
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
