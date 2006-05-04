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

import java.util.ArrayList;
import java.util.List;

import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link Subsystem} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractSubsystem implements Subsystem {

    /**
     * The identity of the subsystem.
     */
    private final String _id;

    /**
     * The workspaces.
     */
    private final List<Workspace> _workspaces = new ArrayList<Workspace>();

    /**
     * The current workspace.
     */
    private Workspace _workspace;


    /**
     * Construct a new <code>AbstractSubsystem</code>.
     */
    public AbstractSubsystem(String id) {
        _id = id;
    }

    /**
     * Returns a localised title for the subsystem.
     *
     * @return a localised title for the subsystem.
     */
    public String getTitle() {
        return Messages.get("subsystem." + _id);
    }

    /**
     * Add a workspace.
     *
     * @param workspace the workspace to add
     */
    public void addWorkspace(Workspace workspace) {
        _workspaces.add(workspace);
    }

    /**
     * Returns the current workspace.
     *
     * @return the current workspace, or  <code>null</code> if there is no
     *         current workspace
     */
    public Workspace getWorkspace() {
        return _workspace;
    }

    /**
     * Sets the current workspace.
     *
     * @param workspace the current workspace
     */
    public void setWorkspace(Workspace workspace) {
        _workspace = workspace;
    }

    /**
     * Returns the default workspace.
     *
     * @return the default workspace, or <code>null</code>  if there is no
     *         default workspace
     */
    public Workspace getDefaultWorkspace() {
        return (!_workspaces.isEmpty()) ? _workspaces.get(0) : null;
    }

    /**
     * Returns the workspaces.
     *
     * @return a list of the woprkspaces
     */
    public List<Workspace> getWorkspaces() {
        return _workspaces;
    }

    /**
     * Returns the first workspace that can handle a particular archetype.
     *
     * @param shortName the archetype's short name.
     * @return a workspace that supports the specifiad archetype or
     *         <code>null</code> if no workspace supports it
     */
    public Workspace getWorkspaceForArchetype(String shortName) {
        for (Workspace workspace : _workspaces) {
            if (workspace.canHandle(shortName)) {
                return workspace;
            }
        }
        return null;
    }

}
