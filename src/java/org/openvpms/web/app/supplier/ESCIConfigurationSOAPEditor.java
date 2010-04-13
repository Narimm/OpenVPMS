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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.esci.adapter.SupplierServiceLocator;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.system.ServiceHelper;


/**
 * An editor for <em>entity.ESCIConfigurationSOAP</em> that allows service URLs to be tested.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ESCIConfigurationSOAPEditor extends AbstractIMObjectEditor {

    /**
     * Constructs an <tt>ESCIConfigurationSOAPEditor</tt>.
     *
     * @param object        the object to edit. An <em>entity.ESCIConfigurationSOAP</em>.
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public ESCIConfigurationSOAPEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractLayoutStrategy() {
            @Override
            protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
                if ("serviceURL".equals(property.getName())) {
                    return createServiceURLComponent(property, parent, context);
                }
                return super.createComponent(property, parent, context);
            }
        };
    }

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
        Component container = RowFactory.create("CellSpacing", field, test);
        return new ComponentState(container, property, focus);
    }

    private void onTest() {
        try {
            String url = (String) getProperty("serviceURL").getValue();
            String user = (String) getProperty("username").getValue();
            String password = (String) getProperty("password").getValue();
            SupplierServiceLocator locator = ServiceHelper.getSupplierServiceLocator();
            locator.getOrderService(url, user, password);
        } catch (Throwable exception) {
            ErrorHelper.show(exception, false);
        }
    }
}
