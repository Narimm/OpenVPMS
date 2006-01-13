package org.openvpms.web.component.workspace;


/**
 * Represents an action that can be performed in a {@link Workspace}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class Action {

    /**
     * Unique identifier for the action.
     */
    private final String _id;

    /**
     * Title of the action.
     */
    private final String _title;

    /**
     * Construct a new <code>Action</code>
     *
     * @param id    the unique identifier for the action
     * @param title the action's title
     */
    public Action(String id, String title) {
        _id = id;
        _title = title;
    }

    public String getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }

}
