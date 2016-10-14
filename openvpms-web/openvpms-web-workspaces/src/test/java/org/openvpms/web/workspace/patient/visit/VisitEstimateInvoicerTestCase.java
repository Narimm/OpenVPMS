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

package org.openvpms.web.workspace.patient.visit;

import org.junit.Test;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.estimate.AbstractEstimateInvoicerTestCase;
import org.openvpms.web.workspace.customer.estimate.EstimateInvoicer;
import org.openvpms.web.workspace.workflow.TestVisitChargeEditor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Tests the {@link VisitEstimateInvoicer} class.
 *
 * @author Tim Anderson
 */
public class VisitEstimateInvoicerTestCase extends AbstractEstimateInvoicerTestCase {

    /**
     * The clinical event.
     */
    private Act event;

    /**
     * Sets up the test case
     */
    @Override
    public void setUp() {
        super.setUp();
        event = PatientTestHelper.createEvent(getPatient());
        save(event);
    }

    /**
     * Tests invoicing of estimates.
     * <p/>
     * This verifies that template visit notes create a note in the patient history.
     */
    @Test
    @Override
    public void testInvoice() {
        super.testInvoice();
        ActBean bean = new ActBean(get(event));
        List<Act> items = bean.getNodeActs("items");
        assertEquals(3, items.size());
        Act note = IMObjectHelper.getObject(PatientArchetypes.CLINICAL_NOTE, items);
        assertNotNull(note);
        ActBean noteBean = new ActBean(note);
        assertEquals("Template Visit Note", noteBean.getString("note"));
    }

    /**
     * Creates a new {@link VisitEstimateInvoicer}.
     *
     * @return a new estimate invoicer
     */
    @Override
    protected EstimateInvoicer createEstimateInvoicer() {
        return new VisitEstimateInvoicer() {
            /**
             * Creates a new {@link CustomerChargeActEditor}.
             *
             * @param invoice the invoice
             * @param context the layout context
             * @return a new charge editor
             */
            @Override
            protected CustomerChargeActEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
                return new TestVisitChargeEditor(createEditorQueue(context.getContext()), invoice, event, context);
            }
        };
    }
}
