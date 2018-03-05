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

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.DefaultTableModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundAbsoluteTimeField;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.table.DefaultTableCellRenderer;
import org.openvpms.web.echo.table.DefaultTableHeaderRenderer;
import org.openvpms.web.resource.i18n.Messages;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

/**
 * Layout strategy for <em>entity.onlineBookingTimesType</em>.
 *
 * @author Tim Anderson
 */
public class OnlineBookingTimesLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Day node name prefixes.
     */
    static final String[] DAY_PREFIXES = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};

    /**
     * Apply the layout strategy.
     * <p/>
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
        boolean edit = context.isEdit();
        ArchetypeNodes nodes = new ArchetypeNodes();
        for (String prefix : DAY_PREFIXES) {
            final String openName = prefix + "Open";
            String startName = prefix + "StartTime";
            String endName = prefix + "EndTime";
            final Property open = properties.get(openName);
            final Property startTime = addTimeField(properties, startName, edit);
            final Property endTime = addTimeField(properties, endName, edit);
            addComponent(createComponent(open, parent, context));
            if (edit) {
                open.addModifiableListener(new ModifiableListener() {
                    @Override
                    public void modified(Modifiable modifiable) {
                        if (!open.getBoolean()) {
                            startTime.setValue(null);
                            endTime.setValue(null);
                            FocusHelper.setFocus(getComponent(open).getComponent());
                        }
                    }
                });
            }
            nodes.exclude(openName, startName, endName);
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
        super.doSimpleLayout(object, parent, properties, container, context);
        // need to set focus traversal after the grid has been created.
        for (String prefix : DAY_PREFIXES) {
            String openName = prefix + "Open";
            String startName = prefix + "StartTime";
            String endName = prefix + "EndTime";
            ComponentState open = getComponent(openName);
            ComponentState start = getComponent(startName);
            ComponentState end = getComponent(endName);
            setFocusTraversal(open);
            setFocusTraversal(start);
            setFocusTraversal(end);
        }
    }

    /**
     * Lays out components in a grid.
     *
     * @param object     the object to lay out
     * @param properties the properties
     * @param context    the layout context
     */
    @Override
    protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context) {
        ComponentGrid grid = super.createGrid(object, properties, context);
        Object[][] data = new Object[7][4];
        Object[] names = {Messages.get("workflow.scheduling.onlinebooking.day"),
                          Messages.get("workflow.scheduling.onlinebooking.open"),
                          Messages.get("workflow.scheduling.onlinebooking.start"),
                          Messages.get("workflow.scheduling.onlinebooking.end")};
        for (int i = 0; i < 7; ++i) {
            String prefix = DAY_PREFIXES[i];
            String openName = prefix + "Open";
            String startName = prefix + "StartTime";
            String endName = prefix + "EndTime";
            ComponentState open = getComponent(openName);
            ComponentState start = getComponent(startName);
            ComponentState end = getComponent(endName);
            data[i][0] = getWeekday(i);
            data[i][1] = open.getComponent();
            data[i][2] = start.getComponent();
            data[i][3] = end.getComponent();
        }
        TableModel model = new DefaultTableModel(data, names);
        Table table = TableFactory.create(model);
        table.setDefaultRenderer(Object.class, DefaultTableCellRenderer.INSTANCE);
        table.setDefaultHeaderRenderer(DefaultTableHeaderRenderer.DEFAULT);
        table.setRolloverEnabled(false);
        table.setSelectionEnabled(false);
        Label label = LabelFactory.create("workflow.scheduling.onlinebooking.times");
        grid.add(label, table);
        return grid;
    }

    /**
     * Adds a field to view/edit a time, in the range 00:00..24:00.
     *
     * @param properties the properties
     * @param name       the property name
     * @param edit       if {@code true}, the field is for editing
     * @return the property
     */
    protected Property addTimeField(PropertySet properties, String name, boolean edit) {
        Property property = properties.get(name);
        BoundAbsoluteTimeField component = new BoundAbsoluteTimeField(property, false);
        if (!edit) {
            component.setEnabled(false);
        }
        addComponent(new ComponentState(component, property));
        return property;
    }


    /**
     * Returns a weekday name, given 0=Monday  and 6=Sunday.
     *
     * @param index the weekday index
     * @return the corresponding weekday name
     */
    private String getWeekday(int index) {
        String result;
        String[] weekdays = DateFormatSymbols.getInstance().getWeekdays();
        switch (index) {
            case 0:
                result = weekdays[Calendar.MONDAY];
                break;
            case 1:
                result = weekdays[Calendar.TUESDAY];
                break;
            case 2:
                result = weekdays[Calendar.WEDNESDAY];
                break;
            case 3:
                result = weekdays[Calendar.THURSDAY];
                break;
            case 4:
                result = weekdays[Calendar.FRIDAY];
                break;
            case 5:
                result = weekdays[Calendar.SATURDAY];
                break;
            default:
                result = weekdays[Calendar.SUNDAY];
                break;

        }
        return result;
    }

}
