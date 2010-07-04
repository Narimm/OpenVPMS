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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.test.TestHelper;


/**
 * Tests the {@link CustomerQuery} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerQueryTestCase extends AbstractQueryTest<Party> {

    /**
     * Customer archetype short names.
     */
    private static final String[] SHORT_NAMES = new String[]{CustomerArchetypes.PERSON, CustomerArchetypes.OTC};

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    protected Query<Party> createQuery() {
        return new CustomerQuery(SHORT_NAMES);
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if <tt>true</tt> save the object, otherwise don't save it
     * @return the new object
     */
    protected Party createObject(String value, boolean save) {
        return TestHelper.createCustomer("foo", value, save);
    }

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    protected String getUniqueValue() {
        return "ZCustomer-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }
}
