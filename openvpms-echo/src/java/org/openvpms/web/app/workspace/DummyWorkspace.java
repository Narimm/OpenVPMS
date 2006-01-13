package org.openvpms.web.app.workspace;

import java.util.List;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;

import org.openvpms.web.component.workspace.Action;
import org.openvpms.web.component.workspace.Workspace;
import org.openvpms.web.util.Messages;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class DummyWorkspace extends Column implements Workspace {

    /**
     * The title.
     */
    private final String _title;


    /**
     * Construct a new <code>DummyWorkspace</code>.
     *
     * @param id the localisation identifier.
     */
    public DummyWorkspace(String id) {
        _title = Messages.get("workspace." + id);
    }

    /**
     * Returns the localised title for this workspace.
     *
     * @return the localised title for this workspace
     */
    public String getTitle() {
        return _title;
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
        return this;
    }
}
