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

import java.util.List;


/**
 * Manages the user interface for a business domain subsystem. A subsystem is
 * essentially a set of related {@link Workspace}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface Subsystem {

    /**
     * Returns the resource bundle key for the subsystem title.
     * The corresponding title may contain keyboard shortcuts.
     *
     * @return the resource bundle key the subsystem title
     */
    String getTitleKey();

    /**
     * Returns the current workspace.
     *
     * @return the current workspace, or  <code>null</code> if there is no
     *         current workspace
     */
    Workspace getWorkspace();

    /**
     * Sets the current workspace.
     *
     * @param workspace the current workspace
     */
    void setWorkspace(Workspace workspace);

    /**
     * Returns the default workspace.
     *
     * @return the default workspace, or <code>null</code>  if there is no
     *         default workspace
     */
    Workspace getDefaultWorkspace();

    /**
     * Returns the workspaces.
     *
     * @return a list of the woprkspaces
     */
    List<Workspace> getWorkspaces();

    /**
     * Returns the first workspace that can handle a particular archetype.
     *
     * @param shortName the archetype's short name.
     * @return a workspace that supports the specified archetype or
     *         <code>null</code> if no workspace supports it
     */
    Workspace getWorkspaceForArchetype(String shortName);

}
