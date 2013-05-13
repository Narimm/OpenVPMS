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

package org.openvpms.web.component.workspace;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Workspaces} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractWorkspaces implements Workspaces {

    /**
     * The identity of the workspace group.
     */
    private final String id;

    /**
     * The workspaces.
     */
    private final List<Workspace> workspaces = new ArrayList<Workspace>();

    /**
     * The current workspace.
     */
    private Workspace workspace;


    /**
     * Constructs an {@code AbstractWorkspaces}.
     *
     * @param id the workspaces identity
     */
    public AbstractWorkspaces(String id) {
        this.id = id;
    }

    /**
     * Returns the resource bundle key for the subsystem title.
     * The corresponding title may contain keyboard shortcuts.
     *
     * @return the resource bundle key the subsystem title.
     */
    public String getTitleKey() {
        return "subsystem." + id;
    }

    /**
     * Add a workspace.
     *
     * @param workspace the workspace to add
     */
    public void addWorkspace(Workspace workspace) {
        workspaces.add(workspace);
    }

    /**
     * Returns the current workspace.
     *
     * @return the current workspace, or  {@code null} if there is no current workspace
     */
    public Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Sets the current workspace.
     *
     * @param workspace the current workspace
     */
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Returns the default workspace.
     *
     * @return the default workspace, or {@code null}  if there is no default workspace
     */
    public Workspace getDefaultWorkspace() {
        return (!workspaces.isEmpty()) ? workspaces.get(0) : null;
    }

    /**
     * Returns the workspaces.
     *
     * @return a list of the woprkspaces
     */
    public List<Workspace> getWorkspaces() {
        return workspaces;
    }

    /**
     * Returns the first workspace that can handle a particular archetype.
     *
     * @param shortName the archetype's short name.
     * @return a workspace that supports the specified archetype or
     *         {@code null} if no workspace supports it
     */
    public Workspace getWorkspaceForArchetype(String shortName) {
        for (Workspace workspace : workspaces) {
            if (workspace.canUpdate(shortName)) {
                return workspace;
            }
        }
        return null;
    }

}
