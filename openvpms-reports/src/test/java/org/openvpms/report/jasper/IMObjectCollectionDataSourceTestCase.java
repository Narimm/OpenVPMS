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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.jasper;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link IMObjectCollectionDataSource} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectCollectionDataSourceTestCase extends AbstractIMObjectDataSourceTestCase {

    /**
     * Tests the {@link IMObjectCollectionDataSource#getExpressionDataSource(String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExpressionDataSource() throws Exception {
        Party customer = TestHelper.createCustomer(false);
        List<IMObject> objects = Arrays.<IMObject>asList(customer);
        IMObjectCollectionDataSource ds = new IMObjectCollectionDataSource(objects.iterator(), getArchetypeService(),
                                                                           handlers);
        assertTrue(ds.next());
        checkExpressionDataSource(ds);
    }

}
