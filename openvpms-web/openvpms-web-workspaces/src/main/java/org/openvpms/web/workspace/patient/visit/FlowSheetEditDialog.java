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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.visit;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.checkin.FlowSheetPanel;

/**
 * A dialog to customization of flow sheets before they are sent to SFS.
 *
 * @author bencharlton on 13/04/2016.
 */
public class FlowSheetEditDialog extends ModalDialog {

    /**
     * The field panel.
     */
    private final FlowSheetPanel panel;

    /**
     * Constructs a {@link FlowSheetEditDialog}.
     *
     * @param factory      the FlowSheetServiceFactory
     * @param location     the practice Location
     * @param departmentId the identifier of the department that is selected by default. May be {@code -1} to select
     *                     the first
     * @param templateName the templateName that is selected by default
     * @param stayDuration the durations of the stay in days
     * @param skip         provide a skip button if {@code true}
     */
    public FlowSheetEditDialog(FlowSheetServiceFactory factory, Party location, int departmentId, String templateName,
                               int stayDuration, boolean skip) {
        super(Messages.get("workflow.flowsheet.edit.title"), null, (skip) ? OK_SKIP : OK_CANCEL);
        panel = new FlowSheetPanel(factory, location, departmentId, templateName, stayDuration, false,
                                   ServiceHelper.getArchetypeService());
    }

    /**
     * Returns the selected department identifier.
     *
     * @return the selected department identifier, or {@code -1} if none is selected
     */
    public int getDepartmentId() {
        return panel.getDepartmentId();
    }

    /**
     * Returns the selected stay duration.
     *
     * @return the expected no. of days
     */
    public int getExpectedStay() {
        return panel.getExpectedStay();
    }

    /**
     * Returns the selected treatment template.
     *
     * @return the selected treatment template. May be {@code null}
     */
    public String getTemplate() {
        return panel.getTemplate();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        getLayout().add(ColumnFactory.create(Styles.INSET, panel.getComponent()));
        FocusGroup parent = getFocusGroup();
        parent.add(0, panel.getFocusGroup()); // add before buttons
    }

}
