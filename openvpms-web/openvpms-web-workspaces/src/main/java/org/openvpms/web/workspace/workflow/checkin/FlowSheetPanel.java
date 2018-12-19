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

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.ReferenceDataService;
import org.openvpms.smartflow.model.Department;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.list.PairListModel;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;

import java.util.ArrayList;
import java.util.List;

/**
 * Flow Sheet details.
 *
 * @author Tim Anderson
 */
public class FlowSheetPanel {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Check box to enable/disable flow sheet creation.
     */
    private final CheckBox create;

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
     * The focus group.
     */
    private final FocusGroup focusGroup = new FocusGroup("FlowSheetPanel");

    /**
     * The panel component.
     */
    private Component component;

    /**
     * Constructs a {@link FlowSheetPanel}.
     *
     * @param factory      the FlowSheetServiceFactory
     * @param location     the practice Location
     * @param departmentId the identifier of the department that is selected by default. May be {@code -1} to select
     *                     the first
     * @param templateName the templateName that is selected by default
     * @param stayDuration the durations of the stay in days
     * @param canDisable   if {@code true} include a checkbox to disable flowsheet creation
     * @param service      the archetype service
     */
    public FlowSheetPanel(FlowSheetServiceFactory factory, Party location, int departmentId, String templateName,
                          int stayDuration, boolean canDisable, IArchetypeService service) {
        this.service = service;
        ReferenceDataService referenceData = factory.getReferenceDataService(location);
        departments = getDepartments(referenceData.getDepartments());
        setDepartmentId(departmentId);
        List<String> names = referenceData.getTreatmentTemplates();
        templates = SelectFieldFactory.create(names);
        templates.setStyleName(Styles.EDIT);
        setTemplate(templateName);
        expectedStay = new SpinBox(0, 99);
        expectedStay.setStyleName(Styles.EDIT);
        setExpectedStay(stayDuration);
        if (canDisable) {
            create = CheckBoxFactory.create(true);
            create.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                }
            });
            create.addPropertyChangeListener(evt -> onCreateChanged());

        } else {
            create = null;
        }
    }

    /**
     * Determines if a flow sheet should be created.
     *
     * @return {@code true} if a flow sheet should be created
     */
    public boolean createFlowSheet() {
        return create != null && create.isSelected();
    }

    /**
     * Determines if a flow sheet should be created.
     *
     * @param create if {@code true} indicates a flow sheet should be created
     */
    public void setCreateFlowSheet(boolean create) {
        if (this.create != null) {
            this.create.setSelected(create);
        }
    }

    /**
     * Populates the panel from a work list.
     *
     * @param worklist the work list
     */
    public void setWorkList(Entity worklist) {
        IMObjectBean bean = service.getBean(worklist);
        setCreateFlowSheet(bean.getBoolean("createFlowSheet"));
        setDepartmentId(bean.getInt("defaultFlowSheetDepartment", -1));
        setExpectedStay(bean.getInt("expectedHospitalStay"));
        setTemplate(bean.getString("defaultFlowSheetTemplate"));
    }

    /**
     * Return the component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            ComponentGrid grid = new ComponentGrid();
            doLayout(grid, 2);
            component = grid.createGrid();
        }
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
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
     * Lays out the panel in the grid.
     *
     * @param grid    the grid to use
     * @param columns the number of columns to use
     */
    public void layout(ComponentGrid grid, int columns) {
        doLayout(grid, columns);
    }

    /**
     * Lays out the panel.
     *
     * @param grid    the container
     * @param columns the no. of columns to use
     */
    protected void doLayout(ComponentGrid grid, int columns) {
        List<Component> components = new ArrayList<>();
        if (create != null) {
            components.add(LabelFactory.create("workflow.flowsheet.create"));
            components.add(create);
            focusGroup.add(create);
        }
        components.add(LabelFactory.create("workflow.flowsheet.department"));
        components.add(departments);
        components.add(LabelFactory.create("workflow.flowsheet.template"));
        components.add(templates);
        components.add(LabelFactory.create("workflow.flowsheet.expectedStay"));
        components.add(expectedStay);
        grid.arrange(columns, components.toArray(new Component[0]));

        focusGroup.add(departments);
        focusGroup.add(templates);
        focusGroup.add(expectedStay.getFocusGroup());
    }

    /**
     * Invoked when the create checkbox is toggled. Enables/disables the fields accordingly.
     */
    private void onCreateChanged() {
        boolean selected = create.isSelected();
        departments.setEnabled(selected);
        templates.setEnabled(selected);
        expectedStay.setEnabled(selected);
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
        field.setStyleName(Styles.EDIT);
        return field;
    }
}
