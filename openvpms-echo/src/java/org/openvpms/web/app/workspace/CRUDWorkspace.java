package org.openvpms.web.app.workspace;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Component;

import org.openvpms.web.component.workspace.Action;
import org.openvpms.web.component.workspace.Workspace;
import org.openvpms.web.util.Messages;


/**
 * Generic CRUD workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class CRUDWorkspace implements Workspace {

    /**
     * The archetype reference model name, used to query objects.
     */
    private final String _refModelName;

    /**
     * The archetype entity name, used to query objects. May be
     * <code>null</code>.
     */
    private final String _entityName;

    /**
     * The archetype concept name, used to query objects. May be
     * <code>null</code>.
     */
    private final String _conceptName;

    /**
     * The localisation id.
     */
    private final String _id;

    /**
     * The edit pane.
     */
    private Component _editPane;


    /**
     * Construct a new <code>CRUDWorkspace</code>.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param id           the localisation identfifier
     */
    public CRUDWorkspace(String refModelName, String entityName,
                         String conceptName,
                         String id) {
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
        _id = id;
    }

    /**
     * Returns the localised title for this workspace.
     *
     * @return the localised title for this workspace
     */
    public String getTitle() {
        return Messages.get("workspace." + _id);
    }

    /**
     * Returns the actions which may be performed in this workspace.
     *
     * @return the actions which may be performed in this workspace
     */
    public List<Action> getActions() {
        return new ArrayList<Action>();
    }

    /**
     * Returns the the default action.
     *
     * @return the default action
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
        if (_editPane == null) {
            _editPane = new CRUDPane(_refModelName, _entityName,
                    _conceptName, _id);
        }
        return _editPane;
    }
}
