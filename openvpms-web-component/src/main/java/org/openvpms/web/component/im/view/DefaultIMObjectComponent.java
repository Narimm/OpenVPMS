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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Default implementation of {@link IMObjectComponent}.
 *
 * @author Tim Anderson
 */
public class DefaultIMObjectComponent implements IMObjectComponent {

    /**
     * The object that the component renders, or {@code null} if the component doesn't render an object.
     */
    private final IMObject object;

    /**
     * The node that the component renders, or {@code null} if the component doesn't render a node.
     */
    private final String node;

    /**
     * The component. May be {@code null}
     */
    private final Component component;

    /**
     * Constructs an {@link DefaultIMObjectComponent} for a rendered object.
     *
     * @param object    the object
     * @param component the component. May be {@code null}
     */
    public DefaultIMObjectComponent(IMObject object, Component component) {
        this.object = object;
        this.node = null;
        this.component = component;
    }

    /**
     * Constructs an {@link DefaultIMObjectComponent} for a rendered node.
     *
     * @param node      the node
     * @param component the component. May be {@code null}
     */
    public DefaultIMObjectComponent(String node, Component component) {
        this.node = node;
        this.object = null;
        this.component = component;
    }

    /**
     * Returns the object that this component renders.
     *
     * @return the object, or {@code null} if this component doesn't render an object
     */
    @Override
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the node that this component renders.
     *
     * @return the node name, or {@code null} if this component doesn't render a node
     */
    @Override
    public String getNode() {
        return node;
    }

    /**
     * Returns the first child {@link IMObjectComponent} that is selected.
     *
     * @return the first selected child, or {@code null} if this component has no selected children
     */
    @Override
    public IMObjectComponent getSelected() {
        return component != null ? SelectionHelper.getSelected(component) : null;
    }

    /**
     * Selects the object/node identified by the supplied selection.
     *
     * @param selection the selection
     * @return {@code false}
     */
    @Override
    public boolean select(Selection selection) {
        return false;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        return component;
    }
}
