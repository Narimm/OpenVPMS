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

package org.openvpms.web.workspace.admin.job.scheduledreport;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.IMObjectTabPane;
import org.openvpms.web.component.im.layout.IMObjectTabPaneModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.PairListModel;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Layout strategy for <em>entity.jobScheduledReport</em>.
 *
 * @author Tim Anderson
 */
public class ScheduledReportJobConfigurationLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The report parameters.
     */
    private Component parameterList;

    /**
     * Constructs a {@link ScheduledReportJobConfigurationLayoutStrategy}.
     */
    public ScheduledReportJobConfigurationLayoutStrategy() {
    }

    /**
     * Apply the layout strategy.
     * <p>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        ArchetypeNodes nodes = ArchetypeNodes.all();
        parameterList = createParameters(object, properties, nodes, context);

        StringBuilder builder = new StringBuilder();
        List<Property> emails = getEmailTo(properties);
        for (int i = 1; i < emails.size(); ++i) {
            // exclude all but the first emailTo property
            nodes.exclude(emails.get(i).getName());
        }
        if (!context.isEdit()) {
            // merge email-to addresses into a single field
            for (Property property : emails) {
                String address = property.getString();
                if (!StringUtils.isEmpty(address)) {
                    if (builder.length() != 0) {
                        builder.append("; ");
                    }
                    builder.append(address);
                }
            }
            Property first = emails.get(0);
            SimpleProperty emailTo = new SimpleProperty(first.getName(), builder.toString(), String.class,
                                                        first.getDisplayName());
            addComponent(createComponent(emailTo, object, context));
        }

        setArchetypeNodes(nodes);
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                  LayoutContext context) {
        List<Property> list = new ArrayList<>(properties);
        ComponentGrid grid = new ComponentGrid();

        // main fields
        List<Property> main = ArchetypeNodes.removeAll(list, "id", "report", "name", "description", "active",
                                                       "location");
        grid.add(createComponentSet(object, main, context), 2);

        // file fields
        List<Property> file1 = ArchetypeNodes.removeAll(list, "file", "directory");
        Property file2 = ArchetypeNodes.remove(list, "fileType");
        grid.add(createComponentSet(object, file1, context), 2);
        grid.add(new ComponentState(new Label()), createComponent(file2, object, context));

        // email fields
        List<Property> email1 = ArchetypeNodes.removeAll(list, "email", "emailFrom");
        Property email2 = ArchetypeNodes.remove(list, "emailTo0");
        Property email3 = ArchetypeNodes.remove(list, "attachmentType");
        grid.add(createComponentSet(object, email1, context), 2);
        grid.add(new ComponentState(new Label()), createComponent(email2, object, context));
        grid.add(new ComponentState(new Label()), createComponent(email3, object, context));

        // print fields
        List<Property> print = ArchetypeNodes.removeAll(list, "print", "printer");
        grid.add(createComponentSet(object, print, context), 2);

        // remaining fields
        grid.add(createComponentSet(object, list, context), 2);

        container.add(ColumnFactory.create(Styles.INSET, grid.createGrid()));
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                   LayoutContext context) {
        if (!properties.isEmpty() || parameterList != null) {
            IMObjectTabPaneModel model = doTabLayout(object, properties, container, context, false);
            IMObjectTabPane pane = new IMObjectTabPane(model);
            if (parameterList != null) {
                String label = Messages.get("scheduledreport.parameters");
                model.addTab(label, ColumnFactory.create(Styles.INSET, parameterList));
            }
            pane.setSelectedIndex(0);
            container.add(pane);
        }
    }

    /**
     * Returns the available emailTo properties.
     *
     * @param properties all properties
     * @return the emailTo properties
     */
    private List<Property> getEmailTo(PropertySet properties) {
        List<Property> result = new ArrayList<>();
        for (int i = 0; ; ++i) {
            Property property = properties.get("emailTo" + i);
            if (property == null) {
                break;
            } else {
                result.add(property);
            }
        }
        return result;
    }

    /**
     * Returns a class given its name.
     *
     * @param className the class name. May be {@code null}
     * @return the class, or {@code null}
     */
    private Class getClass(String className) {
        Class type = null;
        if (!StringUtils.isEmpty(className)) {
            try {
                type = ClassUtils.getClass(className);
            } catch (ClassNotFoundException exception) {
                // no-op
            }
        }
        return type;
    }

    /**
     * Creates a component to render the report parameters.
     *
     * @param object     the object
     * @param properties the properties
     * @param nodes      the nodes
     * @param context    the layout context
     * @return a new component, or {@code null} if the report has no parameters
     */
    private Component createParameters(IMObject object, PropertySet properties, ArchetypeNodes nodes,
                                       LayoutContext context) {
        Component result = null;
        List<Property> parameters = new ArrayList<>();
        int i = 0;
        while (true) {
            Property paramName = properties.get("paramName" + i);
            Property paramDisplayName = properties.get("paramDisplayName" + i);
            Property paramType = properties.get("paramType" + i);
            Property paramExpressionType = properties.get("paramExprType" + i);
            Property paramValue = properties.get("paramValue" + i);
            if (paramName != null && paramDisplayName != null && paramType != null && paramValue != null) {
                nodes.exclude(paramName.getName());
                nodes.exclude(paramDisplayName.getName());
                nodes.exclude(paramType.getName());
                nodes.exclude(paramExpressionType.getName());
                nodes.exclude(paramValue.getName());

                String name = paramName.getString();
                String displayName = paramDisplayName.getString();
                String typeName = paramType.getString();
                Class type = getClass(typeName);
                if (!StringUtils.isEmpty(name) && type != null) {
                    SimpleProperty property = new SimpleProperty(name, type);
                    if (Date.class.isAssignableFrom(type)) {
                        addComponent(createDateComponent(property, object, paramExpressionType, context));
                    }
                    if (context.isEdit()) {
                        property.addModifiableListener(modifiable -> paramValue.setValue(property.getValue()));
                    }
                    if (!StringUtils.isEmpty(displayName)) {
                        property.setDisplayName(displayName);
                    }
                    property.setValue(paramValue.getValue());
                    parameters.add(property);
                }
            } else {
                break;
            }
            i++;
        }
        if (!parameters.isEmpty()) {
            ComponentGrid grid = new ComponentGrid();
            grid.add(createComponentSet(object, parameters, context), 2);
            result = grid.createGrid();
        }
        return result;
    }

    /**
     * Creates a date component.
     *
     * @param property       the date property
     * @param parent         the parent object
     * @param expressionType the date expression type
     * @param context        the context
     * @return a new component
     */
    private ComponentState createDateComponent(SimpleProperty property, IMObject parent, Property expressionType,
                                               LayoutContext context) {
        PairListModel model = new PairListModel();
        addDateExpression(model, ExpressionType.VALUE, "scheduledreport.expression.date");
        addDateExpression(model, ExpressionType.NOW, "scheduledreport.expression.now");
        addDateExpression(model, ExpressionType.TODAY, "scheduledreport.expression.today");
        addDateExpression(model, ExpressionType.YESTERDAY, "scheduledreport.expression.yesterday");
        addDateExpression(model, ExpressionType.TOMORROW, "scheduledreport.expression.tomorrow");
        addDateExpression(model, ExpressionType.START_OF_MONTH, "scheduledreport.expression.startOfMonth");
        addDateExpression(model, ExpressionType.END_OF_MONTH, "scheduledreport.expression.endOfMonth");
        addDateExpression(model, ExpressionType.START_OF_LAST_MONTH, "scheduledreport.expression.startOfLastMonth");
        addDateExpression(model, ExpressionType.END_OF_LAST_MONTH, "scheduledreport.expression.endOfLastMonth");
        addDateExpression(model, ExpressionType.START_OF_NEXT_MONTH, "scheduledreport.expression.startOfNextMonth");
        addDateExpression(model, ExpressionType.END_OF_NEXT_MONTH, "scheduledreport.expression.endOfNextMonth");
        addDateExpression(model, ExpressionType.START_OF_YEAR, "scheduledreport.expression.startOfYear");
        addDateExpression(model, ExpressionType.END_OF_YEAR, "scheduledreport.expression.endOfYear");
        addDateExpression(model, ExpressionType.START_OF_LAST_YEAR, "scheduledreport.expression.startOfLastYear");
        addDateExpression(model, ExpressionType.END_OF_LAST_YEAR, "scheduledreport.expression.endOfLastYear");
        addDateExpression(model, ExpressionType.START_OF_NEXT_YEAR, "scheduledreport.expression.startOfNextYear");
        addDateExpression(model, ExpressionType.END_OF_NEXT_YEAR, "scheduledreport.expression.endOfNextYear");
        addDateExpression(model, ExpressionType.JUNE_30, "scheduledreport.expression.june30");
        addDateExpression(model, ExpressionType.LAST_JUNE_30, "scheduledreport.expression.lastJune30");
        addDateExpression(model, ExpressionType.NEXT_JUNE_30, "scheduledreport.expression.nextJune30");
        addDateExpression(model, ExpressionType.JULY_1, "scheduledreport.expression.july1");
        addDateExpression(model, ExpressionType.LAST_JULY_1, "scheduledreport.expression.lastJuly1");
        addDateExpression(model, ExpressionType.NEXT_JULY_1, "scheduledreport.expression.nextJuly1");

        SelectField field = BoundSelectFieldFactory.create(expressionType, model);
        if (field.getSelectedItem() == null) {
            field.setSelectedItem(ExpressionType.VALUE.toString());
        }
        field.setCellRenderer(PairListModel.RENDERER);
        Component date = createComponent(property, parent, context).getComponent();
        if (context.isEdit()) {
            // register with the field rather than with the property. If the report changes, the UI is recreated,
            // and any listeners need to be unregistered to avoid memory leaks.
            field.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    showDate(date, expressionType);
                }
            });
        }
        showDate(date, expressionType);

        return new ComponentState(RowFactory.create(Styles.CELL_SPACING, field, date), property);
    }

    /**
     * Shows/hides the date, based on the expression type property.
     *
     * @param date     the date component to show/hide
     * @param property the expression type property
     */
    private void showDate(Component date, Property property) {
        boolean visible = ExpressionType.VALUE.toString().equals(property.getString());
        date.setVisible(visible);
    }

    /**
     * Adds a date expression to a list model.
     *
     * @param model the model
     * @param type  the expression type
     * @param key   the localisation key
     */
    private void addDateExpression(PairListModel model, ExpressionType type, String key) {
        model.add(type.toString(), Messages.get(key));
    }

}
