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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Load tests the OpenOffice interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration(locations = "applicationContextNoOOPool.xml", inheritLocations = false)
public abstract class AbstractOpenOfficeLoadTestCase
        extends AbstractOpenOfficeDocumentTest {

    /**
     * Load tests the OpenOffice interface.
     *
     * @throws Exception for any error
     */
    @Test
    public void test() throws Exception {
        createCustomer(); // hack to force init of jxpath function cache to
        // avoid ConcurrentModificationException

        Thread[] threads = new Thread[10];
        Reporter[] reporters = new Reporter[threads.length];
        int count = 1000;
        for (int i = 0; i < threads.length; ++i) {
            reporters[i] = new Reporter(count);
            threads[i] = new Thread(reporters[i]);
        }
        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        for (Reporter reporter : reporters) {
            assertFalse(reporter.failed());
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void onSetUp() {
        new OpenOfficeHelper(createPool(), null);
    }

    /**
     * Creates a new connection pool.
     *
     * @return a new connection pool
     */
    protected abstract OOConnectionPool createPool();


    private class Reporter implements Runnable {

        private final int count;

        boolean failed = false;

        public Reporter(int count) {
            this.count = count;
        }

        public void run() {
            try {
                for (int i = 0; i < count; ++i) {
                    System.out.println(Thread.currentThread().getName()
                                       + " iteration " + (i + 1));
                    test();
                }
            } catch (Throwable exception) {
                exception.printStackTrace();
                synchronized (this) {
                    failed = true;
                }
            }
        }

        public synchronized boolean failed() {
            return failed;
        }

        @Test
        public void test() {
            Document doc = getDocument(
                    "src/test/reports/act.customerEstimation.odt",
                    DocFormats.ODT_TYPE);

            IMReport<IMObject> report = new OpenOfficeIMReport<IMObject>(
                    doc, getHandlers());

            Party party = createCustomer();
            ActBean act = createAct("act.customerEstimation");
            act.setValue("startTime", java.sql.Date.valueOf("2006-08-04"));
            act.setValue("lowTotal", new BigDecimal("100"));
            act.setParticipant("participation.customer", party);

            List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
            Document result = report.generate(objects.iterator(),
                                              DocFormats.ODT_TYPE);
            Map<String, String> fields = getUserFields(result);
            assertEquals("4/08/2006",
                         fields.get("startTime"));  // @todo localise
            assertEquals("$100.00", fields.get("lowTotal"));
            assertEquals("J", fields.get("firstName"));
            assertEquals("Zoo", fields.get("lastName"));
            assertEquals("2.00", fields.get("expression"));
            assertEquals("1234 Foo St\nMelbourne VIC 3001",
                         fields.get("address"));
            assertEquals("Invalid property name: invalid",
                         fields.get("invalid"));

        }
    }

}
