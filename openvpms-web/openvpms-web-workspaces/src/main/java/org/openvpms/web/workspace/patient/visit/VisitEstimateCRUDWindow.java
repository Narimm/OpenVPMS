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

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.estimate.EstimateCRUDWindow;
import org.openvpms.web.workspace.customer.estimate.EstimateInvoicerHelper;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.patient.estimate.VisitEstimateEditor;


/**
 * CRUD window for estimate acts.
 * <p/>
 * This allows estimates to be invoiced using the current visit invoice.
 *
 * @author Tim Anderson
 */
public class VisitEstimateCRUDWindow extends EstimateCRUDWindow {

    /**
     * The visit editor.
     */
    private VisitEditor visitEditor;

    /**
     * Constructs a {@link VisitEstimateCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public VisitEstimateCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(EstimateArchetypes.ESTIMATE, Act.class), context, help);
    }

    /**
     * Registers the visit editor.
     *
     * @param editor the editor. May be {@code null}
     */
    public void setVisitEditor(VisitEditor editor) {
        visitEditor = editor;
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        return getContainer();
    }

    /**
     * Creates a layout context for viewing objects.
     *
     * @return a new layout context
     */
    @Override
    protected LayoutContext createViewLayoutContext() {
        LayoutContext context = super.createViewLayoutContext();
        context.setContextSwitchListener(null);
        return context;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(Act object, LayoutContext context) {
        return new VisitEstimateEditor(object, null, context);
    }

    /**
     * Determines if an estimate can be invoiced.
     *
     * @param act the estimate
     * @return {@code true} if the estimate can be invoiced, otherwise {@code false}
     */
    @Override
    protected boolean canInvoice(Act act) {
        boolean result = super.canInvoice(act);
        if (result) {
            if (visitEditor == null || visitEditor.getChargeEditor() == null) {
                showStatusError(act, "patient.estimate.invoice.title", "patient.estimate.invoice.noinvoice");
                result = false;
            } else if (!getRules().isPatientEstimate(act, getContext().getPatient())) {
                showStatusError(act, "patient.estimate.invoice.title", "patient.estimate.invoice.toomanypets");
                result = false;
            }
        }
        return result;
    }

    /**
     * Invoices an estimate.
     *
     * @param estimate the estimation
     */
    @Override
    protected void invoice(Act estimate) {
        visitEditor.selectCharges();
        VisitChargeEditor editor = visitEditor.getChargeEditor();
        EstimateInvoicerHelper.invoice(estimate, editor);
        estimate.setStatus(EstimateActStatus.INVOICED);
        SaveHelper.save(estimate);
        onRefresh(estimate);
    }
}
