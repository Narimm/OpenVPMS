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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.alert;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.list.AbstractListComponent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.list.StyledListCell;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.echo.colour.ColourHelper;


/**
 * An editor for <em>act.customerAlert</em> and <em>act.patientAlert</em> acts.
 * <p/>
 * This includes a field to display the selected alert type's priority and colour.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractAlertActEditor extends AbstractActEditor {

    /**
     * The alert type selector.
     */
    private SelectField alertType;

    /**
     * The field to display the alert type priority.
     */
    private final TextField priority;


    /**
     * Constructs an <tt>AbstractAlertActEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public AbstractAlertActEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        priority = TextComponentFactory.create();
        Property property = getProperty("alertType");
        property.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                refreshAlertType();
            }
        });
        alertType = LookupFieldFactory.create(property, act);
        alertType.setCellRenderer(new AlertTypeCellRenderer());
        refreshAlertType();
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
        LookupListModel model = (LookupListModel) alertType.getModel();
        int index = alertType.getSelectedIndex();
        Lookup lookup = (index != -1) ? model.getLookup(index) : null;
        if (lookup != null) {
            Color background = AlertHelper.getColour(lookup);
            Color foreground = ColourHelper.getTextColour(background);
            alertType.setBackground(background);
            alertType.setForeground(foreground);
            priority.setText(AlertHelper.getPriorityName(lookup));
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
            Color background = AlertHelper.getColour(lookup);
            Color foreground = ColourHelper.getTextColour(background);
            return new StyledListCell(lookup.getName(), background, foreground);
        }
    }
}
