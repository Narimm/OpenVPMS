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
 * Represents a {@code Component} that renders an {@link IMObject} or collection of {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public interface IMObjectComponent {

    /**
     * Returns the object that this component renders.
     *
     * @return the object, or {@code null} if this component doesn't render an object
     */
    IMObject getObject();

    /**
     * Returns the node that this component renders.
     * <p/>
     * If this component renders multiple nodes, returns the selected node.
     *
     * @return the node name, or {@code null} if this component doesn't render a node
     */
    String getNode();

    /**
     * Returns the first child {@link IMObjectComponent} that is selected.
     *
     * @return the first selected child, or {@code null} if this component has no selected children
     */
    IMObjectComponent getSelected();

    /**
     * Selects the object/node identified by the supplied selection.
     *
     * @param selection the selection
     * @return {@code true} if the selection was successful
     */
    boolean select(Selection selection);

    /**
     * Returns the component.
     *
     * @return the component
     */
    Component getComponent();
}
