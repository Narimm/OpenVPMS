package org.openvpms.web.component.workspace;

import java.util.List;

import nextapp.echo2.app.Component;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public interface Workspace {

    /**
     * Returns the localised title for this workspace.
     *
     * @return the localised title for this workspace
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
