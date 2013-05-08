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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.DefaultIMObjectActions;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.im.view.act.ActRelationshipCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.echo.help.HelpContext;


/**
 * CRUD window for the financial workspace. This provides selection and event
 * notification for child acts and print support.
 *
 * @author Tim Anderson
 */
public class FinancialActCRUDWindow
    extends AbstractViewCRUDWindow<FinancialAct> {

    /**
     * Constructs a {@code FinancialActCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create. If {@code null}, the subclass must override
     *                   {@link #getArchetypes}
     * @param context    the context
     * @param help       the help context
     */
    public FinancialActCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, DefaultIMObjectActions.<FinancialAct>getInstance(), context, help);
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     */
    @Override
    protected IMObjectViewer createViewer(IMObject object) {
        LayoutContext context = createViewLayoutContext();
        return new IMObjectViewer(object, null, new LayoutStrategy(), context);
    }

    /**
     * Invoked when a child act is selected/deselected.
     *
     * @param child the child act. May be {@code null}
     */
    protected void onChildActSelected(FinancialAct child) {
    }

    /**
     * Layout strategy that creates an {@link Viewer} for the items node.
     */
    private class LayoutStrategy extends ActLayoutStrategy {

        /**
         * Creates a component for the items node.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display {@code property}
         */
        @Override
        protected ComponentState createItems(Property property, IMObject parent,
                                             LayoutContext context) {
            Viewer viewer = new Viewer((CollectionProperty) property,
                                       parent, context);
            return new ComponentState(viewer.getComponent(), property);
        }
    }

    /**
     * Viewer that invokes <em>onChildActSelected()</em> when an act is
     * selected.
     */
    private class Viewer extends ActRelationshipCollectionViewer {

        /**
         * Construct a new {@code Viewer}.
         *
         * @param property the collection to view
         * @param parent   the parent object
         * @param context  the layout context. May be {@code null}
         */
        public Viewer(CollectionProperty property, IMObject parent,
                      LayoutContext context) {
            super(property, parent, context);
        }

        /**
         * Browse an act.
         *
         * @param act the act to browse
         */
        @Override
        protected void browseTarget(IMObject act) {
            onChildActSelected((FinancialAct) act);
            super.browseTarget(act);
        }

    }
}
