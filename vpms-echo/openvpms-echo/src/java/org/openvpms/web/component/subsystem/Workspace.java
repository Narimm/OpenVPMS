package org.openvpms.web.component.subsystem;

import java.util.List;

import nextapp.echo2.app.Component;


/**
 * Manages the user interface for a set of related actions. Rach action
 * typically corresponds to a use case.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public interface Workspace {

    /**
     * Returns the localised title of this workspace.
     *
     * @return the localised title of this workspace
     */
    String getTitle();

    /**
     * Returns the actions which may be performed in this workspace.
     *
     * @return the actions which may be performed in this workspace. May be
     *         <code>null</code>
     */
    List<Action> getActions();

    /**
     * Returns the the default action.
     *
     * @return the default action. May be <code>null</code>
     */
    Action getDefaultAction();

    /**
     * Sets the current action.
     *
     * @param id the current action
     */
    void setAction(String id);

    /**
     * Returns the component representing the current action.
     *
     * @return the component for the current action
     */
    Component getComponent();

}
