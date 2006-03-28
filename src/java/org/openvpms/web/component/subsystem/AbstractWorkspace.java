package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Component;

import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link Workspace} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
