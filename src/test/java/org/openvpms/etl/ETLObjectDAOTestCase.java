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
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLObjectDAOTestCase extends TestCase {

    public void testSave() {
        ETLObjectDAO dao = new ETLObjectDAO();

        ETLObject object = new ETLObject("party.customerperson");
        object.addNode(new ETLValueNode("firstName", "Foo"));
        object.addNode(new ETLValueNode("lastName", "Bar"));

        dao.save(object);

        ETLCollectionNode collection = new ETLCollectionNode();
        ETLObject contact = new ETLObject("contact.phoneNumber");
        contact.addNode(new ETLValueNode("telephoneNumber", "12345678"));
        dao.save(contact);

        collection.addValue(new ETLReference(contact));
        object.addNode(collection);

        dao.save(object);

        object = dao.get(object.getObjectId());
        assertNotNull(object);

    }
}
