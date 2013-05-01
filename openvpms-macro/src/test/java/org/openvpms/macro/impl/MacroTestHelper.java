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
package org.openvpms.macro.impl;

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;


/**
 * Macro test helper.
 *
 * @author Tim Anderson
 */
public class MacroTestHelper {

    /**
     * Helper to create and save a macro.
     *
     * @param code       the macro code
     * @param expression the macro expression
     * @return the macro
     */
    public static Lookup createMacro(String code, String expression) {
        Lookup macro = TestHelper.getLookup("lookup.macro", code, false);
        IMObjectBean bean = new IMObjectBean(macro);
        bean.setValue("code", code);
        bean.setValue("name", code);
        bean.setValue("expression", expression);
        bean.setValue("active", true);
        bean.save();
        return macro;
    }

}
