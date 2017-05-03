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
package org.openvpms.web.workspace.admin.lookup;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.layout.TableLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.echo.colour.ColourHelper;


/**
 * Table model for <em>lookup.customerAlertType</em> and <em>entity.patientAlertType</em> objects.
 *
 * @author Tim Anderson
 */
public class AlertTypeTableModel extends DescriptorTableModel<IMObject> {

    /**
     * Constructs an {@link AlertTypeTableModel}.
     * <p/>
     * The column model must be set using {@link #setTableColumnModel}.
     *
     * @param context the layout context
     */
    public AlertTypeTableModel(LayoutContext context) {
        super(context);
    }

    /**
     * Constructs an {@link AlertTypeTableModel}.
     * <p/>
     * This displays the archetype column if the short names reference multiple archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public AlertTypeTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @param row    the row
     * @return the value for the column
     */
    @Override
    protected Object getValue(IMObject object, DescriptorTableColumn column, int row) {
        Object result = super.getValue(object, column, row);
        if (result instanceof Component && column.getName().equals("colour")) {
            Component component = (Component) result;
            String value = (String) column.getValue(object);
            Color colour = ColourHelper.getColor(value);
            if (colour != null) {
                TableLayoutData layout = new TableLayoutData();
                layout.setBackground(colour);
                component.setForeground(ColourHelper.getTextColour(colour));
                component.setLayoutData(layout);
            }
        }
        return result;
    }
}