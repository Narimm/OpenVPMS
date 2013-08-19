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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import echopointng.TabbedPane;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.view.DefaultIMObjectComponent;
import org.openvpms.web.component.im.view.IMObjectComponent;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.im.view.SelectionHelper;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.tabpane.TabPane;


/**
 * An {@link TabPane} that implements {@link IMObjectComponent} in order to track user selection.
 *
 * @author Tim Anderson
 */
public class IMObjectTabPane extends TabPane implements IMObjectComponent {

    /**
     * Constructs an {@link IMObjectTabPane}.
     *
     * @param model the model
     */
    public IMObjectTabPane(IMObjectTabPaneModel model) {
        super(model);
        ComponentFactory.setDefaultStyle(this);
        setTabBorderStyle(TabbedPane.TAB_STRIP_ONLY);
    }

    /**
     * Returns the object that this component renders.
     *
     * @return {@code null}. This component doesn't render a single object
     */
    @Override
    public IMObject getObject() {
        return null;
    }

    /**
     * Returns the node that this component renders.
     * <p/>
     * If this component renders multiple nodes, returns the selected node.
     *
     * @return the node name from the selected tab, or {@code null} if no tab is selected
     */
    @Override
    public String getNode() {
        Property property = null;
        int selectedIndex = getSelectedIndex();
        if (selectedIndex != -1) {
            IMObjectTabPaneModel model = getModel();
            property = model.getProperty(selectedIndex);
        }
        return (property != null) ? property.getName() : null;
    }

    /**
     * Returns the first child {@link IMObjectComponent} that is selected.
     *
     * @return the first selected child, or {@code null} if this component has no selected children
     */
    @Override
    public IMObjectComponent getSelected() {
        IMObjectComponent result = null;
        int selectedIndex = getSelectedIndex();
        if (selectedIndex != -1) {
            Component component = getModel().getTabContentAt(selectedIndex);
            if (component != null) {
                result = SelectionHelper.getComponent(component);
            }
            if (result == null) {
                result = new DefaultIMObjectComponent(getNode(), null);
            }
        }
        return result;
    }

    /**
     * Selects the object/node identified by the supplied selection.
     *
     * @param selection the selection
     * @return {@code true} if the selection was successful
     */
    @Override
    public boolean select(Selection selection) {
        boolean result = false;
        String node = selection.getNode();
        for (int i = 0; i < getModel().size(); ++i) {
            Property property = getModel().getProperty(i);
            if (property != null && property.getName().equals(node)) {
                setSelectedIndex(i);
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex != -1) {
            return getModel().getTabContentAt(selectedIndex);
        }
        return null;
    }

    /**
     * Returns the {@link IMObjectTabPaneModel} used to provide the panes tabs and content.
     *
     * @return returns the model
     */
    @Override
    public IMObjectTabPaneModel getModel() {
        return (IMObjectTabPaneModel) super.getModel();
    }

}
