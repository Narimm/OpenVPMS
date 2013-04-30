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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.customer.charge.ChargeEditorQueue;
import org.openvpms.web.app.patient.charge.VisitChargeEditor;
import org.openvpms.web.app.patient.history.PatientHistoryQuery;
import org.openvpms.web.app.patient.visit.VisitBrowserCRUDWindow;
import org.openvpms.web.app.patient.visit.VisitCRUDWindow;
import org.openvpms.web.app.patient.visit.VisitChargeCRUDWindow;
import org.openvpms.web.app.patient.visit.VisitEditor;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.workflow.TaskContext;

/**
 * Helper to edit invoices.
 * This is required to automatically close popup dialogs.
 *
 * @author Tim Anderson
 */
public class TestEditVisitTask extends EditVisitTask {

    /**
     * The popup dialog manager.
     */
    private ChargeEditorQueue queue = new ChargeEditorQueue();

    /**
     * Returns the popup dialog manager.
     *
     * @return the popup dialog manager
     */
    public ChargeEditorQueue getEditorQueue() {
        return queue;
    }

    /**
     * Creates a new visit editor.
     *
     * @param event   the event
     * @param invoice the invoice
     * @param patient the patient
     * @param context the task context
     * @param help    the help context
     * @return a new editor
     */
    @Override
    protected VisitEditor createVisitEditor(Act event, FinancialAct invoice, Party patient, TaskContext context,
                                            final HelpContext help) {
        return new VisitEditor(patient, event, invoice, context, context.getHelpContext()) {
            @Override
            protected VisitBrowserCRUDWindow createVisitBrowserCRUDWindow(Context context) {
                return new TestVisitBrowserCRUDWindow(getQuery(), context);
            }

            @Override
            protected VisitChargeCRUDWindow createVisitChargeCRUDWindow(Act event, Context context) {
                return new VisitChargeCRUDWindow(event, context, help) {
                    @Override
                    protected VisitChargeEditor createVisitChargeEditor(FinancialAct charge, Act event,
                                                                        LayoutContext context) {
                        return new TestVisitChargeEditor(TestEditVisitTask.this, charge, event, context);
                    }
                };
            }
        };
    }

    private class TestVisitBrowserCRUDWindow extends VisitBrowserCRUDWindow {
        /**
         * Constructs a {@code VisitBrowserCRUDWindow}.
         *
         * @param query   the patient medical record query
         * @param context the context
         */
        public TestVisitBrowserCRUDWindow(PatientHistoryQuery query, Context context) {
            super(query, context, new HelpContext("foo", null));
        }

        protected VisitCRUDWindow createWindow(Context context) {
            return new TestVisitCRUDWindow(context);
        }

    }

}
