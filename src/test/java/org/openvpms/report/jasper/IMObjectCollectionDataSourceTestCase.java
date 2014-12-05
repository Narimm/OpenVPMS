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
 */

package org.openvpms.report.jasper;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link IMObjectCollectionDataSource} class.
 *
 * @author Tim Anderson
 */
public class IMObjectCollectionDataSourceTestCase extends AbstractIMObjectDataSourceTestCase {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookups;

    /**
     * Tests the {@link IMObjectCollectionDataSource#getExpressionDataSource(String)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExpressionDataSource() throws Exception {
        Party customer = TestHelper.createCustomer(false);
        List<IMObject> objects = Arrays.<IMObject>asList(customer);
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("Globals.A", "A");
        fields.put("Globals.1", 1);
        PropertySet f = new ResolvingPropertySet(fields, getArchetypeService());
        IMObjectCollectionDataSource ds = new IMObjectCollectionDataSource(objects.iterator(), f,
                                                                           getArchetypeService(), lookups, handlers);
        assertTrue(ds.next());
        checkExpressionDataSource(ds, f);
    }

}
