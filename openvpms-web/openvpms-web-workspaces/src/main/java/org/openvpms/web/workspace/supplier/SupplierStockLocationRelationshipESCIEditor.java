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
package org.openvpms.web.workspace.supplier;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.esci.adapter.client.SupplierServiceLocator;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * An editor for <em>entityRelationship.supplierStockLocationESCI</em> relationships that allows service URLs to be
 * tested.
 *
 * @author Tim Anderson
 */
public class SupplierStockLocationRelationshipESCIEditor extends EntityRelationshipEditor {

    /**
     * Constructs a {@link SupplierStockLocationRelationshipESCIEditor}.
     *
     * @param object        the object to edit. An <em>entity.ESCIConfigurationSOAP</em>.
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context. May be {@code null}.
     */
    public SupplierStockLocationRelationshipESCIEditor(EntityRelationship object, IMObject parent,
                                                       LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy() {
            @Override
            public ComponentState apply(IMObject object, PropertySet properties, IMObject parent,
                                        LayoutContext context) {
                addComponent(createServiceURLComponent(getProperty("serviceURL"), parent, context));
                return super.apply(object, properties, parent, context);
            }
        };
    }

    /**
     * Creates a component that displays the order service URL and enables the user to test the URL.
     *
     * @param property the orderServiceURL property.
     * @param parent   the parent object
     * @param context  the layout context
     * @return a new component
     */
    private ComponentState createServiceURLComponent(Property property, IMObject parent, LayoutContext context) {
        ComponentState state = context.getComponentFactory().create(property, parent);
        Component field = state.getComponent();
        Button test = ButtonFactory.create("test", new ActionListener() {
            public void onAction(ActionEvent onEvent) {
                onTest();
            }
        });
        FocusGroup focus = state.getFocusGroup();
        focus.add(test);
        Component container = RowFactory.create(Styles.CELL_SPACING, field, test);
        return new ComponentState(container, property, focus);
    }

    /**
     * Attempts to locate the supplier's order service, displaying a dialog indicating success or failure.
     */
    private void onTest() {
        try {
            String url = getProperty("serviceURL").getString();
            String user = getProperty("username").getString();
            String password = getProperty("password").getString();
            SupplierServiceLocator locator = ServiceHelper.getSupplierServiceLocator();
            locator.getOrderService(url, user, password);
            InformationDialog.show(Messages.get("supplier.esci.connection.OK"));
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }
}
