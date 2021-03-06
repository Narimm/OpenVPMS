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

package org.openvpms.web.workspace.customer.communication;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.AbstractListComponent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.web.component.alert.Alert;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.list.StyledListCell;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.workspace.alert.AbstractAlertActEditor;
import org.openvpms.web.workspace.alert.AlertLayoutStrategy;


/**
 * An editor for <em>act.customerAlert</em> acts.
 *
 * @author Tim Anderson
 */
public class CustomerAlertActEditor extends AbstractAlertActEditor {

    /**
     * The field to display the alert type priority.
     */
    private final TextField priority;

    /**
     * The alert type selector.
     */
    private SelectField alertType;

    /**
     * Constructs a {@link CustomerAlertActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public CustomerAlertActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        priority = TextComponentFactory.create();
        Property property = getProperty("alertType");
        alertType = LookupFieldFactory.create(property, act);
        alertType.setCellRenderer(new AlertTypeCellRenderer());
        refreshAlertType();
        property.addModifiableListener(modifiable -> refreshAlertType());
    }

    /**
     * Returns the alert type.
     *
     * @return the alert type. May be {@code null}
     */
    @Override
    public Lookup getAlertType() {
        LookupListModel model = (LookupListModel) alertType.getModel();
        int index = alertType.getSelectedIndex();
        return (index != -1) ? model.getLookup(index) : null;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        AlertLayoutStrategy strategy = new AlertLayoutStrategy(priority);
        strategy.addComponent(new ComponentState(alertType, getProperty("alertType")));
        return strategy;
    }

    /**
     * Updates the alert type and priority fields.
     */
    private void refreshAlertType() {
        Lookup lookup = getAlertType();
        if (lookup != null) {
            Alert alert = new Alert(lookup);
            Color background = alert.getColour();
            Color foreground = alert.getTextColour();
            alertType.setBackground(background);
            alertType.setForeground(foreground);
            priority.setText(alert.getPriority().getName());
        } else {
            priority.setText("");
        }
    }

    /**
     * Renders the alert types cell background with that from the <em>lookup.*AlertType</em>.
     */
    private static class AlertTypeCellRenderer extends LookupListCellRenderer {

        /**
         * Renders an object.
         *
         * @param list   the list component
         * @param object the object to render
         * @param index  the object index
         * @return the rendered object
         */
        @Override
        protected Object getComponent(Component list, String object, int index) {
            AbstractListComponent l = (AbstractListComponent) list;
            LookupListModel model = (LookupListModel) l.getModel();
            Lookup lookup = model.getLookup(index);
            Alert alert = new Alert(lookup);
            Color background = alert.getColour();
            Color foreground = alert.getTextColour();
            return new StyledListCell(lookup.getName(), background, foreground);
        }
    }

}
