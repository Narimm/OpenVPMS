package org.openvpms.web.component.subsystem;

import java.util.List;

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
     * Returns the actions which may be performed in this workspace.
     *
     * @return the actions which may be performed in this workspace. May be
     *         <code>null</code>
     */
    public List<Action> getActions() {
        return null;
    }

    /**
     * Returns the the default action.
     *
     * @return the default action. May be <code>null</code>
     */
    public Action getDefaultAction() {
        return null;
    }

    /**
     * Sets the current action.
     *
     * @param id the current action
     */
    public void setAction(String id) {

    }

    /**
     * Returns the component representing the current action.
     *
     * @return the component for the current action
     */
    public Component getComponent() {
        if (_component == null) {
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

}
