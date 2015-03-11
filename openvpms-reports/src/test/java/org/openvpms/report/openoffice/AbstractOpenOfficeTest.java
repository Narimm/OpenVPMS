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

package org.openvpms.report.openoffice;

import org.junit.After;
import org.openvpms.report.AbstractReportTest;


/**
 * Base class for tests requiring OpenOffice.
 *
 * @author Tim Anderson
 */
public abstract class AbstractOpenOfficeTest extends AbstractReportTest {

    /**
     * Tears down the test case.
     */
    @After
    public void tearDown() {
        OOBootstrapService service = (OOBootstrapService) applicationContext.getBean("OOSocketBootstrapService");
        service.stop();
    }

    /**
     * Returns the connection to OpenOffice.
     *
     * @return the connection
     */
    protected OOConnection getConnection() {
        OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
        return pool.getConnection();
    }

}
