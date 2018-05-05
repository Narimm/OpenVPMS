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

package org.openvpms.web.workspace.patient.charge;

import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link VisitChargeEditor}.
 *
 * @author Tim Anderson
 */
public class VisitChargeEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link VisitChargeEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        Act event = (Act) create(PatientArchetypes.CLINICAL_EVENT);
        LocalContext localContext = new LocalContext();
        localContext.setPractice(TestHelper.getPractice());
        localContext.setCustomer(TestHelper.createCustomer());
        LayoutContext context = new DefaultLayoutContext(localContext, new HelpContext("foo", null));
        VisitChargeEditor editor = new VisitChargeEditor(charge, event, context);

        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof VisitChargeEditor);
        assertEquals(charge, newInstance.getObject());
    }
}
