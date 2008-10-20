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
 *
 *  $Id$
 */

package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.beans.PropertyChangeListener;


/**
 * Manages the user interface for a set of related actions. Each action
 * typically corresponds to a use case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface Workspace<T extends IMObject> {

    /**
     * The summary property name, used in event notification.
     */
    final String SUMMARY_PROPERTY = "summary";

    /**
     * Returns the resource bundle key for the workspace title.
     * The corresponding title may contain keyboard shortcuts.
     *
     * @return the resource bundle key the workspace title
     */
    String getTitleKey();

    /**
     * Renders the workspace.
     *
     * @return the component representing the workspace
     */
    Component getComponent();

    /**
     * Invoked when the workspace is displayed.
     */
    void show();

    /**
     * Invoked when the workspace is hidden.
     */
    void hide();

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    Component getSummary();

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    boolean canHandle(String shortName);

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be <code>null</code>
     */
    void setObject(T object);

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    T getObject();

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <code>null</code>
     */
    void setIMObject(IMObject object);

    /**
     * Add a property change listener.
     *
     * @param name     the property name to listen on
     * @param listener the listener
     */
    void addPropertyChangeListener(String name,
                                   PropertyChangeListener listener);

    /**
     * Remove a property change listener.
     *
     * @param name     the property name to remove the listener for
     * @param listener the listener to remove
     */
    void removePropertyChangeListener(String name,
                                      PropertyChangeListener listener);

}
