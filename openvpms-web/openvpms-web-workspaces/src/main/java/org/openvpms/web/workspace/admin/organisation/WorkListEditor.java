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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.ReferenceDataService;
import org.openvpms.smartflow.model.Department;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.PairListModel;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collections;
import java.util.List;

/**
 * Work List editor.
 *
 * @author Tim Anderson
 */
public class WorkListEditor extends AbstractIMObjectEditor {

    /**
     * The flow sheet service factory.
     */
    private FlowSheetServiceFactory flowSheetServiceFactory;

    /**
     * Constructs an {@link AbstractIMObjectEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public WorkListEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        Property department = getProperty("defaultFlowSheetDepartment");
        Property treatment = getProperty("defaultFlowSheetTemplate");
        Party location = getLayoutContext().getContext().getLocation();
        if (location != null && flowSheetServiceFactory.isSmartFlowSheetEnabled(location)) {
            ReferenceDataService service = flowSheetServiceFactory.getReferenceDataService(location);
            ComponentState departments = getDepartments(department, service);
            ComponentState treatments = getTreatments(treatment, service);
            strategy.addComponent(departments);
            strategy.addComponent(treatments);
        } else {
            strategy.addComponent(new ComponentState(LabelFactory.create(), department));
            strategy.addComponent(new ComponentState(LabelFactory.create(), treatment));
        }
        return strategy;
    }

    /**
     * Returns a dropdown of the available departments.
     *
     * @param property the department property
     * @param service  the reference data service
     * @return the departments dropdown
     */
    private ComponentState getDepartments(Property property, ReferenceDataService service) {
        PairListModel model = new PairListModel();
        try {
            for (Department department : service.getDepartments()) {
                model.add(department.getDepartmentId(), department);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }

        SelectField field = BoundSelectFieldFactory.create(property, model);
        field.setCellRenderer(PairListModel.RENDERER);
        return new ComponentState(field, property);
    }

    /**
     * Returns a dropdown of the available treatment templates.
     *
     * @param property the treatment property
     * @param service  the reference data service
     * @return the treatment templates dropdown
     */
    private ComponentState getTreatments(Property property, ReferenceDataService service) {
        List<String> names = Collections.emptyList();
        try {
            names = service.getTreatmentTemplates();
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
        SelectField field = BoundSelectFieldFactory.create(property, new DefaultListModel(names.toArray()));
        return new ComponentState(field, property);
    }

}
