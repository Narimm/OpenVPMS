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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import nextapp.echo2.app.Component;

import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link Workspace} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractWorkspace implements Workspace {

    /**
     * The workspace component.
     */
    private Component _component;

    /**
     * The subsystem localistion id.
     */
    private final String _subsystemId;

    /**
     * The workspace localisation id.
     */
    private final String _workspaceId;

    /**
     * Property change listener notifier.
     */
    private PropertyChangeSupport _propertyChangeNotifier;


    /**
     * Construct a new <code>AbstractWorkspace</code>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     */
    public AbstractWorkspace(String subsystemId, String workspaceId) {
        _subsystemId = subsystemId;
        _workspaceId = workspaceId;
    }

    /**
     * Returns the localised title of this workspace.
     *
     * @return the localised title if this workspace
     */
    public String getTitle() {
        return Messages.get("workspace." + _subsystemId + "." + _workspaceId);
    }

    /**
     * Renders the workspace.
     *
     * @return the component representing the workspace
     */
    public Component getComponent() {
        if (_component == null || refreshWorkspace()) {
            _component = doLayout();
        }
        return _component;
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    public Component getSummary() {
        return null;
    }

    /**
     * Add a property change listener.
     *
     * @param name     the property name to listen on
     * @param listener the listener
     */
    public void addPropertyChangeListener(String name,
                                          PropertyChangeListener listener) {
        if (_propertyChangeNotifier == null) {
            _propertyChangeNotifier = new PropertyChangeSupport(this);
        }
        _propertyChangeNotifier.addPropertyChangeListener(name, listener);
    }

    /**
     * Remove a property change listener.
     *
     * @param name     the property name to remove the listener for
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String name,
                                             PropertyChangeListener listener) {
        if (_propertyChangeNotifier != null) {
            _propertyChangeNotifier.removePropertyChangeListener(
                    name, listener);
        }
    }

    /**
     * Report a bound property update to any registered listeners. No event is
     * fired if old and new are equal and non-null.
     *
     * @param name     the name of the property that was changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    protected void firePropertyChange(String name, Object oldValue,
                                      Object newValue) {
        if (_propertyChangeNotifier != null) {
            _propertyChangeNotifier.firePropertyChange(name, oldValue,
                                                       newValue);
        }
    }
    
    /**
     * Lays out the component.
     *
     * @return the component
     */
    protected Component doLayout() {
        return Heading.getHeading(_subsystemId, _workspaceId);
    }

    /**
     * Returns the subsystem localisation identifier.
     *
     * @return the subsystem localisation id.
     */
    protected String getSubsystemId() {
        return _subsystemId;
    }

    /**
     * Returns the workspace localisation identifier.
     *
     * @return the workspace localisation id,
     */
    protected String getWorkspaceId() {
        return _workspaceId;
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return <code>true</code> if the workspace should be refreshed,
     * otherwise <code>false</code>
     */
    protected boolean refreshWorkspace() {
        return false;
    }
}
