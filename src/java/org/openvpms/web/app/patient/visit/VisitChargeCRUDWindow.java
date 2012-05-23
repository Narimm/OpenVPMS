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
package org.openvpms.web.app.patient.visit;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.charge.VisitChargeEditor;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.subsystem.AbstractCRUDWindow;


/**
 * Visit charge CRUD window.
 *
 * @author Tim Anderson
 */
public class VisitChargeCRUDWindow extends AbstractCRUDWindow<FinancialAct> {

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The charge editor.
     */
    private VisitChargeEditor editor;

    /**
     * The charge viewer.
     */
    private IMObjectViewer viewer;

    /**
     * Determines if the charge is posted.
     */
    private boolean posted;

    /**
     * Post button identifier.
     */
    public static final String POSTED_ID = "button.post";

    /**
     * Completed button identifier.
     */
    public static final String COMPLETED_ID = "button.completed";

    /**
     * In Progress button identifier.
     */
    public static final String IN_PROGRESS_ID = "button.inprogress";


    /**
     * Constructs a {@code VisitChargeCRUDWindow}.
     */
    public VisitChargeCRUDWindow(Party patient) {
        super(Archetypes.create(CustomerAccountArchetypes.INVOICE, FinancialAct.class),
              DefaultActActions.<FinancialAct>getInstance());
        this.patient = patient;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(FinancialAct object) {
        if (object != null) {
            posted = ActStatus.POSTED.equals(object.getStatus());
            if (posted) {
                viewer = new IMObjectViewer(object, null);
                editor = null;
            } else {
                editor = new VisitChargeEditor(object, createLayoutContext());
                viewer = null;
            }
        } else {
            editor = null;
            viewer = null;
        }
        super.setObject(object);
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        enableButtons(getButtons(), getObject() != null);
        return editor != null ? editor.getComponent() : viewer != null ? viewer.getComponent() : new Column();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        // button layout is handled by the parent dialog
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        if (buttons != null) {
            if (enable) {
                enable = !posted;
            }
            buttons.setEnabled(POSTED_ID, enable);
            buttons.setEnabled(IN_PROGRESS_ID, enable);
            buttons.setEnabled(COMPLETED_ID, enable);
        }
    }

    /**
     * Saves the invoice.
     *
     * @return {@code true} if the invoice was saved
     */
    public boolean save() {
        boolean result;
        if (editor != null && !posted) {
            Validator validator = new Validator();
            if (editor.validate(validator)) {
                result = SaveHelper.save(editor);
                posted = ActStatus.POSTED.equals(getObject().getStatus());
            } else {
                result = false;
                ValidationHelper.showError(validator);
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Marks the charge POSTED and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean post() {
        boolean result = false;
        if (editor != null && !posted) {
            editor.setStatus(ActStatus.POSTED);
            result = save();
        }
        return result;
    }

    /**
     * Marks the charge IN_PROGRESS and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean inProgress() {
        boolean result = false;
        if (editor != null && !posted) {
            editor.setStatus(ActStatus.IN_PROGRESS);
            result = save();
        }
        return result;
    }

    /**
     * Marks the charge COMPLETED and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean complete() {
        boolean result = false;
        if (editor != null && !posted) {
            editor.setStatus(ActStatus.COMPLETED);
            result = save();
        }
        return result;
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @return a new layout context.
     */
    @Override
    protected LayoutContext createLayoutContext() {
        LayoutContext layoutContext = super.createLayoutContext();
        LocalContext context = new LocalContext(GlobalContext.getInstance());
        context.setPatient(patient);
        layoutContext.setContext(context);
        return layoutContext;
    }
}
