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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.im.doc.LogoParticipationEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.print.PrintHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Editor for <em>party.organisationLocation</em>
 * <p>
 * This:
 * <ul>
 * <li>displays a password field for the "mailPassword" node.
 * <li>displays a list of a available printers for the "defaultPrinter" node
 * <li>displays an editor for the practice location logo</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class OrganisationLocationEditor extends AbstractIMObjectEditor {

    /**
     * The logo participation editor.
     */
    private IMObjectEditor logoParticipationEditor;

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().simple("pricingGroup");


    /**
     * Constructs an {@link OrganisationLocationEditor}
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public OrganisationLocationEditor(Party object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        Participation participation = QueryHelper.getParticipation(object, DocumentArchetypes.LOGO_PARTICIPATION);
        if (participation == null) {
            participation = (Participation) IMObjectCreator.create(DocumentArchetypes.LOGO_PARTICIPATION);
        }

        logoParticipationEditor = new LogoParticipationEditor(participation, object, layoutContext);
        getEditors().add(logoParticipationEditor);
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

    private class LocationLayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Constructs an {@link LocationLayoutStrategy}.
         */
        public LocationLayoutStrategy() {
            super(NODES);
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
            ComponentState logo = new ComponentState(logoParticipationEditor.getComponent(),
                                                     null, logoParticipationEditor.getFocusGroup(),
                                                     Messages.get("admin.practice.logo"));
            grid.add(logo, 2);
            return grid;
        }

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

    }

}
