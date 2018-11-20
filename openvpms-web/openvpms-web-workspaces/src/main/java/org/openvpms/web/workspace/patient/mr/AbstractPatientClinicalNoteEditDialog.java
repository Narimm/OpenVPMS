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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;

/**
 * Edit dialog for <em>act.patientClinicalNote</em>.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPatientClinicalNoteEditDialog extends EditDialog {

    /**
     * The patient history browser.
     */
    private PatientHistoryBrowser browser;

    /**
     * Constructs an {@link AbstractPatientClinicalNoteEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public AbstractPatientClinicalNoteEditDialog(IMObjectEditor editor, Context context) {
        super(editor, context);
    }

    /**
     * Saves the current object, if saving is enabled.
     */
    @Override
    protected void onApply() {
        super.onApply();
        if (browser != null) {
            // only refresh the page if the current event is being displayed
            Act event = getCurrentEvent();
            if (browser.getObjects().contains(event)) {
                browser.query();
                browser.setSelected(event, true);
            }
        }
    }

    /**
     * Sets the component.
     *
     * @param component the component
     * @param group     the focus group
     * @param context   the help context
     * @param focus     if {@code true}, move the focus
     */
    @Override
    protected void setComponent(Component component, FocusGroup group, HelpContext context, boolean focus) {
        Party patient = getPatient();
        PatientHistoryQuery query = new PatientHistoryQuery(patient, ServiceHelper.getPreferences());
        DefaultLayoutContext layoutContext = new DefaultLayoutContext(getContext(), context);
        browser = new PatientHistoryBrowser(query, layoutContext) {
            @Override
            protected void initTable(PagedIMTable<Act> table) {
                super.initTable(table);
                table.getTable().setSelectionEnabled(false); // to enable text selection
            }
        };
        SplitPane pane = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, getLayoutStyleName(), component,
                                                 browser.getComponent());
        group.add(browser.getFocusGroup());
        browser.setSelected(getCurrentEvent(), true);  // highlight the event, if any
        super.setComponent(pane, group, context, focus);
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    protected abstract Party getPatient();

    /**
     * Returns the split pane layout style to use.
     *
     * @return te layout style name
     */
    protected abstract String getLayoutStyleName();

    /**
     * Returns the current event.
     *
     * @return the current event. May be {@code null}
     */
    protected Act getCurrentEvent() {
        return (Act) getContext().getObject(PatientArchetypes.CLINICAL_EVENT);
    }
}
