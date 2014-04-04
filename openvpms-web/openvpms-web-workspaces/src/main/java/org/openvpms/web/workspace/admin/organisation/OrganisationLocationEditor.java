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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.property.Property;

/**
 * Editor for <em>party.organisationLocation</em>
 * <p/>
 * This:
 * <ul>
 * <li>displays a password field for the "mailPassword" node.
 * <li>displays a list of a available printers for the "defaultPrinter" node
 * </ul>
 *
 * @author Tim Anderson
 */
public class OrganisationLocationEditor extends AbstractIMObjectEditor {

    /**
     * Constructs an {@link OrganisationLocationEditor}
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context. May be {@code null}.
     */
    public OrganisationLocationEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LocationLayoutStrategy();
    }

    private static class LocationLayoutStrategy extends AbstractLayoutStrategy {

        /**
         * The nodes.
         */
        private static final ArchetypeNodes NODES = new ArchetypeNodes().simple("pricingLocation");

        /**
         * Creates a component for a property.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display {@code property}
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            ComponentState result;
            if (property.getName().equals("mailPassword")) {
                result = new ComponentState(BoundTextComponentFactory.createPassword(property), property);
            } else if (property.getName().equals("defaultPrinter")) {
                DefaultListModel model = new DefaultListModel(PrintHelper.getPrinters());
                SelectField field = BoundSelectFieldFactory.create(property, model);
                result = new ComponentState(field, property);
            } else {
                result = super.createComponent(property, parent, context);
            }
            return result;
        }

        /**
         * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
         *
         * @return the archetype nodes
         */
        @Override
        protected ArchetypeNodes getArchetypeNodes() {
            return NODES;
        }
    }

}
