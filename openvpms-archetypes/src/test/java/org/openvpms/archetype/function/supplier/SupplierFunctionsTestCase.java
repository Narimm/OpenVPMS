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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.supplier;

import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Test;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceFunctions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link SupplierFunctions} class.
 *
 * @author Tim Anderson
 */
public class SupplierFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link SupplierFunctions#accountId(Object, Party)} method.
     */
    @Test
    public void testAccountId() {
        Party location = TestHelper.createLocation();
        Party supplier = TestHelper.createSupplier();

        JXPathContext ctx = createContext(supplier);
        ctx.getVariables().declareVariable("OpenVPMS.location", location);
        assertNull(ctx.getValue("supplier:accountId(" + supplier.getId() + ", $OpenVPMS.location)"));
        assertNull(ctx.getValue("supplier:accountId(., $OpenVPMS.location)"));
        assertNull(ctx.getValue("supplier:accountId(-1, $OpenVPMS.location)"));

        IMObjectBean supplierBean = new IMObjectBean(supplier);
        IMObjectRelationship relationship = supplierBean.addNodeTarget("locations", location);
        IMObjectBean bean = new IMObjectBean(relationship);
        String expected = "1234567";
        bean.setValue("accountId", expected);
        supplierBean.save();

        assertEquals(expected, ctx.getValue("supplier:accountId(" + supplier.getId() + ", $OpenVPMS.location)"));
        assertEquals(expected, ctx.getValue("supplier:accountId(., $OpenVPMS.location)"));
    }

    /**
     * Creates a new JXPathContext, with the party functions registered.
     *
     * @param object the context object
     * @return a new JXPathContext
     */
    private JXPathContext createContext(IMObject object) {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        ArchetypeServiceFunctions functions = new ArchetypeServiceFunctions(service, lookups);
        SupplierFunctions supplierFunctions = new SupplierFunctions(new SupplierRules(service));
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(new ObjectFunctions(functions, "openvpms"));
        library.addFunctions(new ObjectFunctions(supplierFunctions, "supplier"));
        return JXPathHelper.newContext(object, library);
    }

}
