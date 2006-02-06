package org.openvpms.web.component.subsystem;


/**
 * Represents an action that can be performed in a {@link Workspace}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
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
