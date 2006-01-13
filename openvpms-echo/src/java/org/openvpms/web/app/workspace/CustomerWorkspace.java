package org.openvpms.web.app.workspace;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.web.component.workspace.Action;
import org.openvpms.web.util.Messages;


/**
 * Customer workspace.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class CustomerWorkspace extends CRUDWorkspace {

    /**
     * Construct a new <code>CustomerWorkspace</code>.
     */
    public CustomerWorkspace() {
        super("party", "person", "person", "customer");
    }

    /**
     * Returns the actions which may be performed in this workspace.
     *
     * @return the actions which may be performed in this workspace
     */
    public List<Action> getActions() {
        Action merge = new Action("merge", Messages.get("workspace.customer.merge"));
        List<Action> result = new ArrayList<Action>();
        result.add(merge);
        return result;
    }

}
