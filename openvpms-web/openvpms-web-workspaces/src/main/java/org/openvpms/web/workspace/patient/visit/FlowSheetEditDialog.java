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

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.ReferenceDataService;
import org.openvpms.smartflow.model.Department;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.component.im.list.PairListModel;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * A dialog to customization of flow sheets before they are sent to SFS.
 *
 * @author bencharlton on 13/04/2016.
 */
public class FlowSheetEditDialog extends PopupDialog {

    /**
     * The available departments.
     */
    private final SelectField departments;

    /*
    * The available treatment template names.
     */
    private final SelectField templates;

    /*
    * A spin box which holds the pets expected stay in days.
     */
    private final SpinBox expectedStay;

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
                               int stayDuration,
                               boolean skip) {
        super(Messages.get("workflow.flowsheet.edit.title"), (skip) ? OK_SKIP : OK_CANCEL);
        setModal(true);
        ReferenceDataService service = factory.getReferenceDataService(location);
        departments = getDepartments(service.getDepartments());
        setDepartmentId(departmentId);
        List<String> names = service.getTreatmentTemplates();
        templates = SelectFieldFactory.create(names);
        setTemplate(templateName);
        expectedStay = new SpinBox(0, 99);
        setExpectedStay(stayDuration);
    }

    /**
     * Sets the department identifier.
     *
     * @param departmentId the identifier of the department, or (@code -1} to indicate no selection
     */
    public void setDepartmentId(int departmentId) {
        if (departmentId > -1) {
            departments.setSelectedItem(departmentId);
        }
    }

    /**
     * Returns the selected department identifier.
     *
     * @return the selected department identifier, or {@code -1} if none is selected
     */
    public int getDepartmentId() {
        int result = -1;
        Object value = departments.getSelectedItem();
        if (value instanceof Integer) {
            result = (Integer) value;
        }
        return result;
    }

    /**
     * Set the expected stay duration.
     *
     * @param days the expected no. of days
     */
    public void setExpectedStay(int days) {
        if (days > 0) {
            expectedStay.setValue(days);
        } else {
            expectedStay.setValue(2);
        }
    }

    /**
     * Returns the selected stay duration.
     *
     * @return the expected no. of days
     */
    public int getExpectedStay() {
        return expectedStay.getValue();
    }

    /**
     * Sets the treatment template.
     *
     * @param template the treatment template name. May be {@code null}
     */
    public void setTemplate(String template) {
        DefaultListModel model = (DefaultListModel) templates.getModel();
        int index = model.indexOf(template);
        if (index != -1) {
            templates.setSelectedIndex(index);
        }
    }

    /**
     * Returns the selected treatment template.
     *
     * @return the selected treatment template. May be {@code null}
     */
    public String getTemplate() {
        return (String) templates.getSelectedItem();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING);
        doLayout(column);
        getLayout().add(ColumnFactory.create(Styles.INSET, column));
    }

    /**
     * Lays out the dialog.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        FocusGroup parent = getFocusGroup();
        FocusGroup child = new FocusGroup("FlowSheetDialog");
        child.add(templates);
        child.add(expectedStay.getFocusGroup());
        parent.add(0, child); // insert before buttons
        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("workflow.flowsheet.department"));
        grid.add(departments);
        grid.add(LabelFactory.create("workflow.flowsheet.template"));
        grid.add(templates);
        grid.add(LabelFactory.create("workflow.flowsheet.expectedStay"));
        grid.add(expectedStay);
        setFocus(templates);
        container.add(grid);
    }

    /**
     * Returns a dropdown of the available departments.
     *
     * @param departments the departments
     * @return the departments dropdown
     */
    private SelectField getDepartments(List<Department> departments) {
        PairListModel model = new PairListModel();
        for (Department department : departments) {
            model.add(department.getDepartmentId(), department);
        }
        SelectField field = SelectFieldFactory.create(model);
        field.setCellRenderer(PairListModel.RENDERER);
        return field;
    }
}
