package org.openvpms.web.component.subsystem;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link Subsystem} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
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
