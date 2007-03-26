package org.openvpms.etl;/*
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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

import junit.framework.TestCase;


/**
 * Tests the {@link ETLValueDAO} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLValueDAOTestCase extends TestCase {

    public void testSave() {
        ETLValueDAO dao = new ETLValueDAO();

        ETLValue object = new ETLValue("ID1.1", "party.customerperson", "ID1");
        ETLValue firstName = new ETLValue("ID1.1", "party.customerperson",
                                          "ID1", "firstName", "Foo");
        ETLValue lastName = new ETLValue("ID1.1", "party.customerperson", "ID1",
                                         "lastName", "Bar");
        dao.save(object);
        dao.save(firstName);
        dao.save(lastName);

        object = dao.get(object.getValueId());
        assertNotNull(object);
    }
}
